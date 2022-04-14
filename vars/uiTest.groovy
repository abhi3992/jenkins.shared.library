import com.utilities.ServiceConfig
import com.utilities.GlobalVars
import com.utilities.SlackNotification

def call(){
    def clusterEnv = params.ClusterEnv
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
    - name: selenium-chrome
      image: lldigital-docker.jfrog.io/jenkins-selenium-chrome-test:202109.0.0
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
        container('selenium-chrome') {
            try{
                stage('Clone Repo') {
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

                for (String tag in ServiceConfig.serviceParams[params.ArtifactName].testEnv[clusterEnv]) {
                    def testTag = tag
                    stage('Run UI test'){
                        try {
                            dir('testRepo'){
                                sh """
                                    mvn test -Denv=${clusterEnv} -Dcucumber.options="--tags ${testTag}"
                                """
                            }
                        }
                        catch(Exception ex) {
                            ex.printStackTrace()
                            buildStatus = GlobalVars.failureStatus
                        }

                    }

                    stage('Results file'){
                        dir('testRepo'){
                            sh"""
                                zip -r ${params.ArtifactName}-${testTag}-cucumber-html-reports.zip ./target/cucumber-reports
                            """
                        }
                    }

                    stage('Slack message'){
                        dir('testRepo'){
                            notificationMessage = "UI test results for ${params.ArtifactName}: ${buildStatus}"
                            def filePath = "${params.ArtifactName}-${testTag}-cucumber-html-reports.zip"
                            SlackNotification.notify(this, buildStatus, notificationsTo, filePath, customised_message)
                        }
                    }

                    if (buildStatus == GlobalVars.failureStatus){
                        throw new Exception("Build Failure: Build failed due to failure of ${testTag} cases")
                    }
                }
            }
            catch(Exception ex){
                buildStatus = GlobalVars.failureStatus
                throw ex
            }
            finally{
                notificationMessage = "*Job name*: ${env.JOB_NAME} for ${params.ArtifactName} \n *Build number*: ${env.BUILD_NUMBER} \n *Console URL:* <${env.BUILD_URL}console|Click Here> \n *Status:* ${buildStatus}"
                SlackNotification.notify(this, buildStatus, notificationsTo, null, notificationMessage)
            }            
        }
    }
    
}
}