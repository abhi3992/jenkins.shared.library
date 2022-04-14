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
    def environment = params.Environment
    def jobName = params.JobName
    def artifactName = ServiceConfig.getArtifactName(serviceName)
    def artifactVersion = params.ArtifactVersion
    def jenkinsImage = ServiceConfig.serviceParams[artifactName].buildImage
    def artifactoryServiceDependencyPath = "service-dependencies/${artifactName}/${artifactName}-${artifactVersion}.zip"
    def datalakeProjectId = DeploymentVars.airflowConfig[environment].datalakeProjectId
    def buildStatus = GlobalVars.successStatus
    def upstreamData = (params.containsKey("UpstreamData")) ? readJSON(text: params.UpstreamData) : [:]
    def notificationsTo = (upstreamData.containsKey("notificationsTo")) ? upstreamData.notificationsTo : null

podTemplate(yaml: """
apiVersion: v1
kind: Pod
metadata:
    labels:
      jenkins/label: jenkins-slave
spec:
    containers:
    - name: airflow-cd
      image: lldigital-docker.jfrog.io/${jenkinsImage}
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
""") {

node(POD_LABEL) {
        container('airflow-cd') {
            try{
                GcpService.authenticateGCP(this, env, "${datalakeProjectId}")
                stage('dowload service-dependencies'){
                    ArtifactoryService.downloadFileFromArtifactory("${artifactoryServiceDependencyPath}", "${env.WORKSPACE}", Artifactory)
                    sh """ 
                        unzip ${artifactName}-${artifactVersion}.zip
                    """
                }
                
                stage('download jar'){
                    def artifactoryJarPath = "gradle/**/${artifactName}-${artifactVersion}.jar"
                    ArtifactoryService.downloadFileFromArtifactory("${artifactoryJarPath}", "${env.WORKSPACE}", Artifactory)
                    sh "chmod 777 ${artifactName}-${artifactVersion}.jar"
                }
                
                def serviceDependencies = FileReader.getlistOfFilesAndFolders(this, "${env.WORKSPACE}/${artifactName}-${artifactVersion}/")
                
                if(serviceDependencies.contains("artifact-jobs")){
                    stage('Run Artifact'){
                        def jobContent = sh(script: "cat ${WORKSPACE}/${artifactName}-${artifactVersion}/artifact-jobs/${jobName}", returnStdout: true).trim()
                        def replacedFile = FileReader.replaceElements(jobContent, "${datalakeProjectId}", this, env)
                        replacedFile = replacedFile.replaceAll('<<artifactName>>', "${artifactName}-${artifactVersion}.jar")
                        echo "${replacedFile}"
                        def jobMap = FileReader.getMapFromFile(replacedFile)
                        if (jobMap[artifactName].containsKey('serviceAccountSecretKeyAndVersion')){
                            def serviceAccountKeyAndVersion = jobMap[artifactName].serviceAccountSecretKeyAndVersion
                            def serviceAccountKey = serviceAccountKeyAndVersion.split('/')[0]
                            def secretVersion = serviceAccountKeyAndVersion.split('/')[1]
                            env.GOOGLE_APPLICATION_CREDENTIALS = "${WORKSPACE}/serviceAccount.json"
                            def serviceAccount = GcpService.getSecretFromSecretManager("${datalakeProjectId}", "${serviceAccountKey}", "${secretVersion}", this, env)
                            writeFile(file: "serviceAccount.json", text: "${serviceAccount}")
                        }
                        def script = jobMap[artifactName].runCommand
                        
                        sh "touch run.sh && chmod 777 run.sh"
                        writeFile(file: "run.sh", text: "${script}")
                        sh 'cat run.sh'
                        sh './run.sh'
                    }
                }

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


