import com.utilities.DeploymentVars
import com.utilities.ServiceConfig
import com.utilities.ArtifactoryService
import com.utilities.UserAuthorization

def call(){
    def artifactName = ServiceConfig.getArtifactName(params.ServiceName)
    def promoteFrom = (params.PromoteFrom == "") ? "dev" : params.PromoteFrom
    def environment = params.PromoteTo.split('-')[0]

podTemplate(yaml: '''
apiVersion: v1
kind: Pod
metadata:
    labels:
      jenkins/label: jenkins-slave
spec:
    containers:
    - name: jenkins-cd
      image: lldigital-docker.jfrog.io/jenkins-spring-boot-gradle:1625184366
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
        container('jenkins-cd') {

            stage('promote'){
                UserAuthorization.isUserAuthorised(environment, currentBuild)
                ArtifactoryService.promoteArtifact(artifactName, params.ImageTag, promoteFrom, environment,Artifactory, this)
            }

            def newImageTag = params.ImageTag
            if(environment == "prod"){
                def preReleaseRegex = /(-dev|-rel)/
                newImageTag = newImageTag.split(preReleaseRegex)[0]
            }

            if(params.Deploy){
                build(job: "Deploy/DeployService", parameters: [
                    string(name: 'Environment', value: params.PromoteTo),
                    string(name: 'ServiceName', value: params.ServiceName),
                    string(name: 'ImageTag', value: newImageTag),
                    string(name: 'Namespace', value: "")
                ])
            }
        }
    }
}

}