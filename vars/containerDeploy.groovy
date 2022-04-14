import com.utilities.DeploymentVars
import com.utilities.ServiceConfig
import com.utilities.UserAuthorization
import com.utilities.GlobalVars
import com.utilities.DeployService
import com.utilities.FileReader
import com.utilities.FunctionalTestService
import com.utilities.SlackNotification

import jenkins.model.Jenkins

def call(){
    UserAuthorization.isUserAuthorised(params.Environment, currentBuild)

    def chartPath = "devops/charts/podium-service"
    def serviceName = params.ServiceName
    def artifactName = ServiceConfig.getArtifactName(serviceName)    
    def helmEnvironment = ServiceConfig.getHelmFolder(artifactName,params.Environment)
    def namespace = (params.Namespace == "") ? ServiceConfig.getArtifactDefaultNamespace(artifactName, params.Environment) : params.Namespace
    def clusterName = DeploymentVars.clusterConfigs[params.Environment].gkeClusterName
    def projectID = DeploymentVars.clusterConfigs[params.Environment].gcpProjectId
    def gcpLocation = DeploymentVars.clusterConfigs[params.Environment].gcpLocation
    def helmValuesPath = "helm-vars/helm_vars/defaults/podium-service/${serviceName}/${helmEnvironment}/values.yaml"
    def helmSecretPath = "helm-vars/helm_vars/secrets/podium-service/${serviceName}/${helmEnvironment}/secrets.yaml"
    def gcsHelmSecretPath = "gs://pdp-ll01-ase1-ss-devops-prod/helm_vars/podium-service/${serviceName}/${helmEnvironment}/secrets.yaml"

    def upstreamData = (params.containsKey("UpstreamData")) ? readJSON(text: params.UpstreamData) : [:]
    def buildStatus = GlobalVars.successStatus
    def notificationsTo = (upstreamData.containsKey("notificationsTo")) ? upstreamData.notificationsTo : null

podTemplate(yaml: '''
apiVersion: v1
kind: Pod
metadata:
    labels:
      jenkins/label: jenkins-slave
spec:
    containers:
    - name: jenkins-cd
      image: lldigital-docker.jfrog.io/jenkins-cd-pipeline:1625086338
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
            try{
                stage('Clone projects') {
                    echo "${upstreamData}"
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: "master"]],
                        doGenerateSubmoduleConfigurations: false,
                        extensions: [[$class: 'RelativeTargetDirectory',
                        relativeTargetDir: 'devops']],
                        submoduleCfg: [],
                        userRemoteConfigs: [[
                            credentialsId: "git-credentials",
                            url: "git@bitbucket.org:lendlease_corp/podium-sandbox-infrastructure.git"
                        ]]
                    ])
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: "master"]],
                        doGenerateSubmoduleConfigurations: false,
                        extensions: [[$class: 'RelativeTargetDirectory',
                        relativeTargetDir: 'helm-vars']],
                        submoduleCfg: [],
                        userRemoteConfigs: [[
                            credentialsId: "git-credentials",
                            url: "git@bitbucket.org:lendlease_corp/podium-helm-vars.git"
                        ]]
                    ])
                }

                stage('Configuring GCloud CLI') {
                    withCredentials([file(credentialsId: 'lendlease-jenkins-seed-service-account', variable: 'GC_KEY')]) {

                        sh"""
                            gcloud auth activate-service-account --key-file=${GC_KEY}
                            gcloud config set project ${projectID}
                            gcloud container clusters get-credentials ${clusterName}  --zone ${gcpLocation}
                        """
                    }
                }

                stage('Fetch secrets from gsc'){
                    sh(script: """#!/bin/bash -xe
                        HAS_SECRET_FILE=true
                        gcloud config set project ${DeploymentVars.clusterConfigs.sharedServices.gcpProjectId}
                        mkdir -p helm-vars/helm_vars/secrets/podium-service/${serviceName}/${helmEnvironment}
                        touch ${helmSecretPath}
                        gsutil cp ${gcsHelmSecretPath} ${helmSecretPath} || HAS_SECRET_FILE=false
                        kubectl config view
                        CONTEXT_NAME=\$(kubectl config current-context)
                        kubectl config set-context \$CONTEXT_NAME --namespace ${namespace}
                        kubectl config use-context \$CONTEXT_NAME
                    """)
                }

                stage('Update helm chart dependencies'){
                    sh """
                        helm dependency build devops/charts/podium-service
                    """
                }

                stage('Dry run'){
                    DeploymentVars.helmUpdate(this,"${serviceName}", chartPath, namespace, helmValuesPath, helmSecretPath, params.ImageTag, true)
                }

                stage('Install or update service'){
                    DeploymentVars.helmUpdate(this,"${serviceName}", chartPath, namespace, helmValuesPath, helmSecretPath, params.ImageTag, false)
                }

                stage('run CT'){
                    try{
                        FunctionalTestService.testPipeline(params.Environment, serviceName, upstreamData, this)            
                    }
                    catch(Exception ex) {
                        ex.printStackTrace()                            
                        buildStatus = GlobalVars.failureStatus
                    }
                    
                }

                if (buildStatus == GlobalVars.failureStatus && GlobalVars.rollback){
                    stage('Revert deployment'){
                        sh"""
                            kubectl rollout undo deployment/${serviceName}-app --namespace ${namespace}
                        """                    
                        throw new Exception("Deployment failed")
                    }                    
                }
            }
            catch(Exception ex){
                buildStatus = GlobalVars.failureStatus
                ex.printStackTrace()
                throw ex
            }
            finally {
                def build_message = "*Job name*: ${env.JOB_NAME} for ${params.ServiceName} \n *Console URL:* <${env.BUILD_URL}console|Click Here> \n *Status:* ${buildStatus}"
                SlackNotification.notify(this, buildStatus, notificationsTo, null, build_message)
            }
            
        }
    }
}

}