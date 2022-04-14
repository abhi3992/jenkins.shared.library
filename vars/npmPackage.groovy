import com.utilities.GlobalVars
import com.utilities.PodiumVars

//required keys in config: {sonarProjectKey}
def call(Map config){
podTemplate(yaml: """
apiVersion: v1
kind: Pod
metadata:
    labels:
      jenkins/label: jenkins-slave
spec:
    containers:
    - name: react-container
      image: lldigital-docker.jfrog.io/jenkins-react-ui:1624979700
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
        container('react-container'){
            stage("set build parameters"){

                withCredentials([ file(credentialsId:"npmrc", variable: "npmrc_file") ]){
                    sh"""
                        cp ${npmrc_file} .npmrc
                        npm install -g json
                    """                        
                }

                env.TARGET_REPO = (config.containsKey("targetRepo")) ? config.targetRepo : "npm-local"

                env.REPO_VERSION = sh(script: "cat ./package.json | json version", returnStdout: true).trim()
                env.ARTIFACT_VERSION = ArtifactRepoHelper.getFullTagWithPreRelease("${env.BRANCH_NAME}", "${env.REPO_VERSION}", "npm", config.artifactName, this, env)
                env.ARTIFACT_DELETE_DAYS = GlobalVars.defaultMaxDays
                env.IS_DELETE_ARTIFACTS = GlobalVars.isDeleteArtifacts


                //check if version already exists
                env.PACKAGE_NAME = sh(script: "cat ./package.json | json name", returnStdout: true).trim()
                def packageExists = true
                def npmVersion = ""
                try{
                    npmVersion = sh(script: "npm view ${env.PACKAGE_NAME}@${env.ARTIFACT_VERSION} version", returnStdout: true).trim()
                }
                catch(exc){
                    packageExists = false
                    echo "${env.PACKAGE_NAME} does not exist"
                }
                    
                if(packageExists && npmVersion?.trim()){
                    throw new Exception("${env.PACKAGE_NAME}@${env.ARTIFACT_VERSION} already exists on jfrog repo")
                }

                if(config.containsKey("envVarFileId")){
                    configFileProvider([configFile(fileId: "${config.envVarFileId}", variable: "repo_env_vars")]){
                        script{
                            load "${repo_env_vars}"
                        }
                    }
                }
            }

            stage("install & test") {
                if ("${env.GIT_BRANCH}" ==~ GlobalVars.buildDepsRegex) {
                    sh """
                         #!/bin/bash
                        npm install
                        npm run publish:npm
                        npm install sonarqube-scanner
                        npm install jest-sonar-reporter
                        npm install --save-dev cross-env
                    """ 
                }
            }

            stage("send sonar scan") {
                if ("${env.GIT_BRANCH}" ==~ GlobalVars.qcStagesRegex){
                    withCredentials([string(credentialsId: 'sonartoken', variable: 'sonarscanner')]) {
                        withSonarQubeEnv("sonarscanner"){
                            sh """ 
                                ${tool("sonarscanner-maven")}/bin/sonar-scanner -X \
                                -Dsonar.organization=lendlease-digital \
                                -Dsonar.projectKey=${config.sonarProjectKey} \
                                -Dsonar.sources=./src \
                                -Dsonar.login=${sonarscanner} \
                                -Dsonar.host.url=https://sonarcloud.io \
                                -Dsonar.javascript.lcov.reportPaths=coverage/lcov.info \
                                -Dsonar.branch.autoconfig.disabled=true
                            """
                        }
                    }                    
                }
            } 

            stage("sonar scan results"){
                if ("${env.GIT_BRANCH}" ==~ GlobalVars.qcStagesRegex) {
                    timeout(time: 10, unit: 'MINUTES') {
                        waitForQualityGate abortPipeline: GlobalVars.abortPipeline
                    }                  
                }   
            }

            stage("publish build version package") {
                if ("${env.GIT_BRANCH}" ==~ GlobalVars.buildDepsRegex) {
                    script{
                        if(!("${env.GIT_BRANCH}" ==~ GlobalVars.gitTagRegex)){
                            sh" npm version --no-commit-hooks --no-git-tag-version ${ARTIFACT_VERSION}"
                        }
                    }                    

                    rtBuildInfo(
                        buildName: "${env.PACKAGE_NAME}",
                        buildNumber: "${ARTIFACT_VERSION}"
                    )

                    rtNpmDeployer (id: "NPM_DEPLOYER", serverId: "${jfrogCloudId}", repo: "${TARGET_REPO}")

                    rtNpmPublish (buildName: "${env.PACKAGE_NAME}", buildNumber: "${ARTIFACT_VERSION}", deployerId: "NPM_DEPLOYER")

                    rtPublishBuildInfo (serverId: "${jfrogCloudId}", buildName: "${env.PACKAGE_NAME}", buildNumber: "${ARTIFACT_VERSION}")
                }
            }
        
            stage ('xray scan artifact'){
                if ("${env.GIT_BRANCH}" ==~ GlobalVars.qcStagesRegex) {
                    timeout(time: "${jfrogXrayTimeout}", unit: "SECONDS"){
                        xrayScan (
                            serverId: "${jfrogCloudId}",
                            buildName: "${env.PACKAGE_NAME}",
                            buildNumber: "${ARTIFACT_VERSION}",
                            failBuild: "${abortOnXrayFailure}",
                            printTable: true
                        )
                    }
                }
            }

            stage('git tag'){
                if ("${env.GIT_BRANCH}" ==~ GlobalVars.gitTagRegex) {
                    withCredentials([sshUserPrivateKey(credentialsId: 'masterbitbucket', keyFileVariable: 'SSH_KEY', usernameVariable: "SSH_KEY_USER")]) {
                        sh"""
                            git tag ${env.ARTIFACT_VERSION} ${env.GIT_COMMIT}
                            cd ~/.ssh
                            cp ${SSH_KEY} id_rsa
                            eval `ssh-agent`
                            ssh-add ~/.ssh/id_rsa
                            cd ${WORKSPACE}
                            git push origin ${env.ARTIFACT_VERSION}
                        """
                    }
                }
            }    
            stage ("slack message") {
                build(job: 'podium-slack-notification', parameters: [
                    string(name: 'jobname', value: "${env.JOB_NAME}"),
                    string(name: 'buildnumber', value: "${env.BUILD_NUMBER}"),
                    string(name: 'buildstatus', value: "${currentBuild.result}"),
                    string(name: 'branchname', value: "${env.GIT_BRANCH}"),
                    string(name: 'consoleUrl', value: "${env.BUILD_URL}")
                ]) 

            }
       
        }

    }

}
}

