import com.utilities.GlobalVars
import com.utilities.ServiceConfig
import com.utilities.SlackNotification
import com.utilities.ArtifactRepoHelper
import com.utilities.ArtifactoryService
import com.utilities.FileReader
import jenkins.model.Jenkins

def call(Map config){
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
                checkout scm
                if("${env.BRANCH_NAME}" ==~ GlobalVars.buildPackageRegex){
                    def repoVersion = readFile(".metadata")
                    def artifactVersion = ArtifactRepoHelper.getFullTagWithPreRelease("${env.BRANCH_NAME}", repoVersion, "zip-local", config.artifactName, this, env)
                    def buildInfo = Artifactory.newBuildInfo("${config.artifactName}", artifactVersion)
                    
                    def workspaceContent = FileReader.getlistOfFilesAndFolders(this, "${env.WORKSPACE}")
                    if(workspaceContent.contains("src")){
                        stage('zip src and push to jfrog'){
                            def fileSpecs = []
                            sh "cd ${env.WORKSPACE}/src && zip -r ${config.artifactName}-${artifactVersion}.zip ."
                            fileSpec = [
                                pattern: "src/${config.artifactName}-${artifactVersion}.zip", 
                                target: "zip-local/${config.artifactName}/"
                            ]
                            fileSpecs.add(fileSpec)
                            def uploadSpec = ArtifactoryService.uploadFilesToJfrog(fileSpecs, Artifactory, buildInfo)
                            echo"${uploadSpec}"
                            sh "rm -f ${config.artifactName}-${artifactVersion}.zip"
                        }
                    }
                    

                    if(FileReader.isServiceDependenciesFolderPresent(this, "${env.WORKSPACE}")){
                        stage('upload service-dependencies'){
                            def fileSpecs = []
                            sh"""
                                cp -R service-dependencies ${config.artifactName}-${artifactVersion}
                                zip -r ${config.artifactName}-${artifactVersion}.zip ${config.artifactName}-${artifactVersion} 
                            """
                            fileSpec = [
                                pattern: "${config.artifactName}-${artifactVersion}.zip", 
                                target: "service-dependencies/${config.artifactName}/"
                            ]
                            fileSpecs.add(fileSpec)
                            def uploadSpec = ArtifactoryService.uploadFilesToJfrog(fileSpecs, Artifactory, buildInfo)
                            echo"${uploadSpec}"
                        }
                    }   
                }
            }
            catch(Exception ex){
                buildStatus = GlobalVars.failureStatus
                ex.printStackTrace()
                throw ex
            }
            finally{
                notificationMessage = "*Job name*: ${env.JOB_NAME} \n *Build number*: ${env.BUILD_NUMBER} \n *Console URL:* <${env.BUILD_URL}console|Click Here> \n *Status:* ${buildStatus}"
                SlackNotification.notify(this, buildStatus, notificationsTo, null, notificationMessage)
            }
        }
    }
}
}


