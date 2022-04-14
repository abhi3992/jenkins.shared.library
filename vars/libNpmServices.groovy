import com.utilities.GlobalVars
import com.utilities.PodiumVars
import com.utilities.ArtifactRepoHelper

//required keys in config: {sonarProjectKey}
def call(Map config){
pipeline {
    agent {
        kubernetes {
            yaml """
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
"""
        }
    }

    stages {

        stage("set build parameters"){
            steps{
                container('react-container') {
                    withCredentials([ file(credentialsId:"npmrc", variable: "npmrc_file") ]){
                        sh"""
                            cp ${npmrc_file} .npmrc
                            npm install -g json
                        """                        
                    }

                    script{
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
                }
            }
        }

        stage("install & test") {
            when{
                expression{ return "${env.GIT_BRANCH}" ==~ GlobalVars.buildDepsRegex }
            }
            steps {
                container('react-container') {
                    sh """
                         #!/bin/bash
                        npm install
                        npm install -g sonarqube-scanner
                        npm install jest-sonar-reporter
                        npm install --save-dev cross-env
                    """                    
                }
            }
        }

        stage("send sonar scan") {
            when{
                expression{ return "${env.GIT_BRANCH}" ==~ GlobalVars.qcStagesRegex }
            }
            steps {
                container('react-container') {
                    withCredentials([string(credentialsId: 'sonartoken', variable: 'sonarscanner')]) {
                        withSonarQubeEnv("sonarscanner"){
                            sh """ 
                                sonar-scanner \
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
        }     
      
        stage("sonar scan results") {
            when{
                expression{ return "${env.GIT_BRANCH}" ==~ GlobalVars.qcStagesRegex }
            }
            steps {
                container('react-container'){
                    timeout(time: 10, unit: 'MINUTES') {
                        waitForQualityGate abortPipeline: GlobalVars.abortPipeline
                    }                  
                }   
            }
        }

        stage("publish build version package") {
            when{
                expression{ return "${env.GIT_BRANCH}" ==~ GlobalVars.buildDepsRegex }
            }
            steps {
                container('react-container') {
                    script{
                        if(!("${env.GIT_BRANCH}" ==~ GlobalVars.gitTagRegex)){
                            sh" npm version --no-commit-hooks --no-git-tag-version ${ARTIFACT_VERSION}"
                        }
                    }                    

                    rtBuildInfo(
                        buildName: "${env.PACKAGE_NAME}",
                        buildNumber: "${ARTIFACT_VERSION}",
                        maxDays: "${env.ARTIFACT_DELETE_DAYS}",
                        deleteBuildArtifacts: "${env.IS_DELETE_ARTIFACTS}"
                    )

                    rtNpmDeployer (id: "NPM_DEPLOYER", serverId: "${jfrogCloudId}", repo: "${TARGET_REPO}")

                    rtNpmPublish (buildName: "${env.PACKAGE_NAME}", buildNumber: "${ARTIFACT_VERSION}", deployerId: "NPM_DEPLOYER")

                    rtPublishBuildInfo (serverId: "${jfrogCloudId}", buildName: "${env.PACKAGE_NAME}", buildNumber: "${ARTIFACT_VERSION}")
                }
            }
        }

        stage ('xray scan artifact') {
           when{
                expression{ return "${env.GIT_BRANCH}" ==~ GlobalVars.qcStagesRegex }
            }
            steps {
                container('react-container'){
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
        }

        stage('git tag'){
            when{
                expression{ return "${env.GIT_BRANCH}" ==~ GlobalVars.gitTagRegex }
            }
            steps{
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
    }   
}

}