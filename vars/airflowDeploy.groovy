import com.utilities.DeploymentVars
import com.utilities.ServiceConfig
import com.utilities.ArtifactoryService
import com.utilities.FileReader
import com.utilities.GcpService
import com.utilities.GlobalVars
import com.utilities.SlackNotification
import com.utilities.UserAuthorization

def call(){
    UserAuthorization.isUserAuthorised(params.Environment, currentBuild)

    def serviceName = params.ServiceName
    def artifactName = ServiceConfig.getArtifactName(serviceName)
    def artifactVersion = params.ArtifactVersion
    def environment = params.Environment
     
    def datalakeProjectId = DeploymentVars.airflowConfig[environment].datalakeProjectId
    def dependencyBucket =  DeploymentVars.airflowConfig.airflowFilesBucketPrefix + "${environment}"
    def composerName = DeploymentVars.airflowConfig[environment].gcpComposerName
    def artifactoryServiceDependencyPath = "service-dependencies/${artifactName}/${artifactName}-${artifactVersion}.zip"
    def buildStatus = GlobalVars.successStatus
    def upstreamData = (params.containsKey("UpstreamData")) ? readJSON(text: params.UpstreamData) : [:]
    def notificationsTo = (upstreamData.containsKey("notificationsTo")) ? upstreamData.notificationsTo : null

podTemplate(yaml: '''
apiVersion: v1
kind: Pod
metadata:
    labels:
      jenkins/label: jenkins-slave
spec:
    containers:
    - name: airflow-cd
      image: lldigital-docker.jfrog.io/jenkins-cd-pipeline:3.14-0.0
      imagePullPolicy: Always
      tty: true
      volumeMounts:
        - name: docker
          mountPath: /var/run/docker.sock 
    volumes:
        - name: docker
          hostPath:
            path: /var/run/docker.sock
    imagePullSecrets: [ 'lldigital-docker-jenkins' ]
''') {

node(POD_LABEL) {
        container('airflow-cd') {
            try{
                GcpService.authenticateGCP(this, env, "${datalakeProjectId}")
                def dagFolderPath = sh(script:"gcloud composer environments describe '${composerName}' --location='australia-southeast1' --format='get(config.dagGcsPrefix)'", returnStdout: true ).trim()
                def airflowGcpBucket = dagFolderPath.split('//')[1].toString().split('/')[0]
                def listOfOldJars= GcpService.findAllArtifactsWithArtifactPrefixInBucket("${datalakeProjectId}", "${artifactName}*.jar", "${dependencyBucket}", this)
                echo airflowGcpBucket                
            
                stage('dowload service-dependencies'){
                    ArtifactoryService.downloadFileFromArtifactory("${artifactoryServiceDependencyPath}", "${env.WORKSPACE}", Artifactory)
                    sh """ 
                        unzip ${artifactName}-${artifactVersion}.zip
                    """
                }
                
                stage('deploy-jar'){
                    def artifactoryJarPath = "gradle/**/${artifactName}-${artifactVersion}.jar"
                    ArtifactoryService.downloadFileFromArtifactory("${artifactoryJarPath}", "${env.WORKSPACE}/jar", Artifactory)
                    GcpService.uploadArtifactsToGcs("${datalakeProjectId}", "${env.WORKSPACE}/jar", "${dependencyBucket}", this)                    
                }
                                
                def serviceDependencies = FileReader.getlistOfFilesAndFolders(this, "${env.WORKSPACE}/${artifactName}-${artifactVersion}/")
                if(serviceDependencies.contains("dag")){
                    stage('deploy dag'){
                        GcpService.uploadArtifactsToGcs("${datalakeProjectId}", "${env.WORKSPACE}/${artifactName}-${artifactVersion}/dag/", "${airflowGcpBucket}/dags", this)
                    }
                }

                if(serviceDependencies.contains("airflow-plugins")){
                    stage('update plugins'){
                        def pluginBucket = DeploymentVars.airflowConfig[environment].airflowGcpBucketId
                        GcpService.uploadArtifactsToGcs("${datalakeProjectId}", "${WORKSPACE}/${artifactName}-${artifactVersion}/airflow-plugins/", "${airflowGcpBucket}/plugins", this)
                    }
                }
                if(serviceDependencies.contains("airflow-variables")){
                    stage('update variables'){
                        def airflowVariableFiles = FileReader.getlistOfFilesAndFolders(this, "${env.WORKSPACE}/${artifactName}-${artifactVersion}/airflow-variables/")
                        for (def variableFile in airflowVariableFiles) {
                            def variableFileContent = sh(script: "cat ${WORKSPACE}/${artifactName}-${artifactVersion}/airflow-variables/${variableFile}", returnStdout: true).trim()
                            variableFileContent = variableFileContent.replaceAll('<<artifactName>>', "${artifactName}-${artifactVersion}.jar")                                
                            def replacedFile = FileReader.replaceElements(variableFileContent, "${datalakeProjectId}", this, env)

                            echo "${replacedFile}"
                            sh """
                                chmod 777 ${WORKSPACE}/${artifactName}-${artifactVersion}/airflow-variables/${variableFile}
                                ls -ltr ${WORKSPACE}/${artifactName}-${artifactVersion}/airflow-variables/
                            """

                            writeFile(file: "${WORKSPACE}/${artifactName}-${artifactVersion}/airflow-variables/${variableFile}", text: "${replacedFile}")
                            sh """         
                                gcloud -q composer environments storage data import --source=${WORKSPACE}/${artifactName}-${artifactVersion}/airflow-variables/${variableFile} --environment="${composerName}" --location="australia-southeast1"
                                gcloud -q composer environments run ${composerName} variables --location="australia-southeast1" -- --i /home/airflow/gcs/data/${variableFile}                       
                            """
                        }
                    }

                }
                // stage ('removing older jar'){
                   // GcpService.deleteFromGcs("${datalakeProjectId}", listOfOldJars , this)
                // }
            } 

            catch(Exception ex){
                buildStatus = GlobalVars.failureStatus
                ex.printStackTrace()
                throw ex
            }
            finally {
                def build_message = "*Job name*: ${env.JOB_NAME} \n *Console URL:* <${env.BUILD_URL}console|Click Here> \n *Status:* ${buildStatus}"
                SlackNotification.notify(this, buildStatus, notificationsTo, null, build_message)
            }
        }
    }
}
}

