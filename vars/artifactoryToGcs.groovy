import com.utilities.ArtifactoryService
import com.utilities.GcsService
import com.utilities.UserAuthorization
import com.utilities.SlackNotification
import com.utilities.GitServices
import com.utilities.GlobalVars

def call(){

    gcsBucket = params.GcsBucket
    def buildStatus = GlobalVars.successStatus
    def notificationMessage = ""

podTemplate(yaml: '''
apiVersion: v1
kind: Pod
metadata:
    labels:
      jenkins/label: jenkins-slave
spec:
    containers:
    - name: jenkins-cd
      image: lldigital-jenkins-cd-pipeline.jfrog.io/jenkins-cd-pipeline:1625086338
      imagePullPolicy: Always
      tty: true
      volumeMounts:
        - name: docker
          mountPath: /var/run/docker.sock 
    volumes:
        - name: docker
          hostPath:
            path: /var/run/docker.sock
    imagePullSecrets: [ 'lldigital-jenkins-cd-pipeline' ]
''') {

    node(POD_LABEL) {
        container('jenkins-cd') {
            try{
                stage('check user authorization') {
                    UserAuthorization.isUserAuthorised(params.Environment, currentBuild)
                }

                stage('download artifacts from artifactory') {
                    try {
                        ArtifactoryService.downloadFileFromArtifactory(params.FilePathInArtifactory, "${env.WORKSPACE}", Artifactory)
                        sh """
                            ls -ltr
                        """
                    }
                    catch(Exception ex){
                        ex.printStackTrace()
                        notificationMessage = "Failed to download artifacts from Artifactory"
                        buildStatus = GlobalVars.failureStatus
                        throw new Exception("Failed to download artifacts from Artifactory")
                    }
                }

                stage('upload artifacts to GCS'){
                    GcpService.authenticateGCP(this, env, params.GcsProjectId)
                    try {
                        sh "gcloud auth activate-service-account --key-file=${GC_KEY}"
                        GcsService.uploadArtifactsToGcs(params.GcsProjectId, "${env.WORKSPACE}", params.GcsBucket, this)
                    }
                    catch(Exception ex){
                        ex.printStackTrace()
                        notificationMessage =  "Failed to upload artifacts to ${gcsBucket} bucket"
                        buildStatus = GlobalVars.failureStatus
                        throw new Exception("Failed to upload artifacts to ${gcsBucket} bucket")
                    }  
                }
            }
            catch (Exception ex){
                buildStatus = GlobalVars.failureStatus
                throw ex
            }
            finally{
                SlackNotification.notify(this, buildStatus, null, null, notificationMessage)
            }

        }
    }
    
}
}
