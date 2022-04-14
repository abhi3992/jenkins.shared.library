import com.utilities.GlobalVars
import com.utilities.PodiumVars
import com.utilities.DeploymentVars
import com.utilities.ServiceConfig
import com.utilities.GitServices
import com.utilities.SlackNotification
import jenkins.model.Jenkins

def call(){
    def clusterEnv = params.containsKey('ClusterEnv') ? params.ClusterEnv : 'dev'
    def repoUrl = ServiceConfig.serviceParams[params.ArtifactName]['testRepoUrl']
    def notificationMessage = ""
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
    - name: jenkins-gradle-container
      image: lldigital-docker.jfrog.io/jenkins-spring-boot-gradle:2021-08.0.1
      imagePullPolicy: Always
      tty: true
      volumeMounts:
        - name: docker
          mountPath: /var/run/docker.sock 
    volumes:
        - name: docker
          hostPath:
            path: /var/run/docker.sock
    imagePullSecrets: ['lldigital-docker-jenkins']
'''){

    node(POD_LABEL) {  
        container('jenkins-gradle-container'){
            try{
                stage('clone test repo'){
                    echo "${upstreamData}"
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: "*/development"]],
                        doGenerateSubmoduleConfigurations: false,
                        extensions: [[$class: 'RelativeTargetDirectory',
                        relativeTargetDir: 'testRepo']],
                        submoduleCfg: [],
                        userRemoteConfigs: [[ 
                            credentialsId: "masterbitbucket",
                            url: "${repoUrl}"
                        ]]
                    ])
                }

                stage('cloud sql proxy'){
                    dir('testRepo'){
                        configFileProvider([configFile(fileId: "${clusterEnv}_cloud_sql_service_account", variable: "sql_service_account")]){
                        sh """
                                wget https://dl.google.com/cloudsql/cloud_sql_proxy.linux.amd64 -O cloud_sql_proxy
                                chmod +x ./cloud_sql_proxy
                                ./cloud_sql_proxy -instances ${DeploymentVars.clusterConfigs[clusterEnv].gcpProjectId}:${DeploymentVars.clusterConfigs[clusterEnv].gcpLocation}:podium-db-${clusterEnv}-lendleaseau=tcp:127.0.0.1:1234 -credential_file ${sql_service_account} &
                                ./cloud_sql_proxy -instances ${DeploymentVars.clusterConfigs[clusterEnv].gcpProjectId}:${DeploymentVars.clusterConfigs[clusterEnv].gcpLocation}:podium-db-${clusterEnv}-csc=tcp:127.0.0.1:1235 -credential_file ${sql_service_account} &
                            """ 
                        }                        
                    }
                }

            
                for (String tag in ServiceConfig.serviceParams[params.ArtifactName].testEnv[clusterEnv]) {
                    def testTag = tag
                    stage("run ${testTag} karate test"){
                        dir('testRepo'){
                            try {
                                if ("${repoUrl}" == "git@bitbucket.org:lendlease_corp/podium.api.automation.karate.git") {
                                    sh """
                                        chmod +x ./gradlew
                                        ./gradlew clean build -x test
                                        ./gradlew test "-Dkarate.options=--tags ${testTag}" "-Dkarate.env=${clusterEnv}"
                                    """
                                    
                                }
                                else {
                                    sh """
                                        mvn test "-Dkarate.options=--tags ${testTag} -Dkarate.env=${clusterEnv.toUpperCase()}"
                                    """
                                }
                            }
                            catch(Exception ex) {
                                ex.printStackTrace()
                                buildStatus = GlobalVars.failureStatus
                            }
                        }   
                    }
                
                    stage("package ${testTag} results file"){
                        dir('testRepo'){
                            def zipFilename = "${params.ArtifactName}-${testTag}-karate-reports.zip"
                            def reportPath = (repoUrl == "git@bitbucket.org:lendlease_corp/podium.api.automation.karate.git") ? './build/karate-reports' : './target/karate-reports'
                            sh "zip -r ${zipFilename} ${reportPath}"                            
                        }
                    }
                
                    stage("slack ${testTag} test reports"){
                        dir('testRepo'){
                            def file_path = "${params.ArtifactName}-${testTag}-karate-reports.zip"
                            notificationMessage = "*Job name*: ${env.JOB_NAME} for ${params.ArtifactName} \n *Build number*: ${env.BUILD_NUMBER} \n *Console URL:* <${env.BUILD_URL}console|Click Here> \n\nBackend API test results for ${params.ArtifactName}: ${buildStatus}"

                            SlackNotification.notify(this, buildStatus, notificationsTo, file_path, notificationMessage)
                        }
                    }

                    if (buildStatus == GlobalVars.failureStatus){
                        throw new Exception("Pipeline Failure: Build failed due to failure of ${testTag} cases in ${clusterEnv} environment")
                    }
                }
            }
            catch(Exception ex){
                buildStatus = GlobalVars.failureStatus
                ex.printStackTrace()
                throw ex
            }
            finally{
                notificationMessage = "*Job name*: ${env.JOB_NAME} for ${params.ArtifactName}\n *Build number*: ${env.BUILD_NUMBER} \n *Console URL:* <${env.BUILD_URL}console|Click Here> \n *Status:* ${buildStatus}"
                SlackNotification.notify(this, buildStatus, notificationsTo, null, notificationMessage)
            }
        }
    }
}
}


