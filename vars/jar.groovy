import com.utilities.DeploymentVars
import com.utilities.ServiceConfig
import com.utilities.ArtifactoryService
import com.utilities.UserAuthorization
import com.utilities.ArtifactRepoHelper
import com.utilities.GlobalVars
import com.utilities.FileReader
import com.utilities.SlackNotification
import com.utilities.GitServices
import com.utilities.PreBuildValidationService
import com.utilities.GcpService
import com.utilities.JenkinsCredentials

import groovy.json.JsonSlurper

def call(Map config){
    def repoVersion = ""
    def artifactVersion = ""
    def targetRepo = (config.containsKey("targetRepo")) ? config.targetRepo.toString() : 'gradle-local'
    def buildInfo
    def server = Artifactory.server(ArtifactoryService.artifactoryServerId)
    def buildImage = (config.containsKey('buildImage')) ? config.buildImage : 'jenkins-spring-boot-gradle:3.14-0.0'

    def buildStatus = GlobalVars.successStatus
    def build_message = ""
    def commitHash
    def notificationsTo
    def upstreamData

podTemplate(yaml: """
apiVersion: v1
kind: Pod
metadata:
    labels:
      jenkins/label: jenkins-slave
spec:
    containers:
    - name: jenkins-cd
      image: lldigital-docker.jfrog.io/${buildImage}
      resources:
        requests:
          memory: "2Gi"
          cpu: "500m"
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
        container('jenkins-cd'){

            try{
                
                checkout scm
                sh(script:'chmod +x gradlew')
                repoVersion = sh(script:'./gradlew -q version', returnStdout: true ).trim()
                artifactVersion = ArtifactRepoHelper.getFullTagWithPreRelease("${env.BRANCH_NAME}", repoVersion, "gradle", config.artifactName, this, env)

                commitHash = GitServices.getLatestCommitHash(this, env.WORKSPACE)
                notificationsTo = GitServices.getCommitterEmail(this, commitHash, env.WORKSPACE)
                echo"${notificationsTo}"
                upstreamData = [notificationsTo: notificationsTo]

                stage('pre-build validation'){
                    PreBuildValidationService.preBuildValidation("${env.WORKSPACE}", artifactVersion.toString(), config.artifactName, repoVersion, this, env)
                }

                def USERNAME = GcpService.getSecretFromSecretManager(JenkinsCredentials.gcpProjectId, JenkinsCredentials.credentials['jfrog-dev-team-user'].usernameSecretName, JenkinsCredentials.credentials['jfrog-dev-team-user'].version, this, env)
                def PASSWORD = GcpService.getSecretFromSecretManager(JenkinsCredentials.gcpProjectId, JenkinsCredentials.credentials['jfrog-dev-team-user'].passwordSecretName, JenkinsCredentials.credentials['jfrog-dev-team-user'].version, this, env)

                env.JFROG_COMMON_CREDS_USR = "${USERNAME}"
                env.JFROG_COMMON_CREDS_PSW = "${PASSWORD}"

                stage('build & test'){
                    sh"""
                        chmod +x gradlew
                        ./gradlew -PbuildVersion=${artifactVersion} clean build
                    """
                }
                if(buildImage != "jenkins-spring-boot-gradle:1.0.0-java8"){
                    stage('send sonar scan'){
                        def sonarscanner = GcpService.getSecretFromSecretManager(JenkinsCredentials.gcpProjectId, JenkinsCredentials.credentials['sonartoken'].secretName, JenkinsCredentials.credentials['sonartoken'].version, this, env)
                        withSonarQubeEnv("sonarscanner"){
                            sh """ 
                                #!/bin/bash -xe
                                ./gradlew -PbuildVersion=${artifactVersion} sonarqube \
                                -Dsonar.organization=lendlease-digital \
                                -Dsonar.projectKey=${config.sonarProjectKey} \
                                -Dsonar.login="${sonarscanner}" \
                                -Dsonar.host.url=https://sonarcloud.io \
                                -Dsonar.branch.autoconfig.disabled=true
                            """
                        }

                        timeout(time: 10, unit: 'MINUTES') {
                            waitForQualityGate(abortPipeline: GlobalVars.abortPipeline)
                        }  
                    }
                }

                stage ('push & publish artifact') {
                    buildInfo = Artifactory.newBuildInfo("${config.artifactName}", artifactVersion)

                    def rtGradle = Artifactory.newGradleBuild()
                    rtGradle.resolver(repo: 'gradle', server: server)
                    rtGradle.deployer(repo: targetRepo, server: server)
                    rtGradle.usesPlugin = true
                    rtGradle.useWrapper = true

                    rtGradle.run(buildFile: 'build.gradle', tasks: """clean artifactoryPublish -PbuildVersion=${artifactVersion}""".toString(), buildInfo: buildInfo)
                    
                    server.publishBuildInfo(buildInfo)                
                }

                if(FileReader.isServiceDependenciesFolderPresent(this, "${env.WORKSPACE}")){
                    stage('upload service-dependencies'){
                        def artifactName = sh(script: "./gradlew -q projectName", returnStdout: true).trim()
                        def fileSpecs = []
                        sh"""
                            cp -R service-dependencies ${artifactName}-${artifactVersion}
                            zip -r ${artifactName}-${artifactVersion}.zip ${artifactName}-${artifactVersion} 
                        """
                        fileSpec = [
                            pattern: "${artifactName}-${artifactVersion}.zip", 
                            target: "service-dependencies/${artifactName}/"
                        ]
                        fileSpecs.add(fileSpec)
                        def uploadSpec = ArtifactoryService.uploadFilesToJfrog(fileSpecs, Artifactory, buildInfo)
                        echo"${uploadSpec}"
                    }   
                }

                stage('xray scan artifact'){
                    def scanConfig = [
                        'buildName': buildInfo.name,
                        'buildNumber': buildInfo.number,
                        'failBuild': ArtifactoryService.failBuildOnXrayFailure
                    ]

                    timeout(time: ArtifactoryService.jfrogXrayTimeoutInSecs, unit: "SECONDS"){
                        def scanResult = server.xrayScan(scanConfig)
                        echo scanResult as String
                    }
                }

                if("${env.BRANCH_NAME}" ==~ GlobalVars.gitTagRegex){
                    stage('tag main'){
                        def SSH_KEY_USER = GcpService.getSecretFromSecretManager(JenkinsCredentials.gcpProjectId, JenkinsCredentials.credentials['masterbitbucket'].usernameSecretName, JenkinsCredentials.credentials['masterbitbucket'].version, this, env)
                        def SSH_KEY = GcpService.getSecretFromSecretManager(JenkinsCredentials.gcpProjectId, JenkinsCredentials.credentials['masterbitbucket'].passwordSecretName, JenkinsCredentials.credentials['masterbitbucket'].version, this, env)
                        sh"""
                            cd ${WORKSPACE}
                            git tag ${repoVersion} ${env.GIT_COMMIT}
                            cd ~/.ssh
                            cp ${SSH_KEY} id_rsa
                            eval `ssh-agent`
                            ssh-add ~/.ssh/id_rsa
                            cd ${WORKSPACE}
                            git push origin ${repoVersion}
                        """
                    }
                }
            }
            catch (Exception ex){
                buildStatus = GlobalVars.failureStatus
                ex.printStackTrace()
                throw ex
            }
            finally {
                build_message = "*Job name*: ${env.JOB_NAME} \n *Build number*: ${env.BUILD_NUMBER} \n *Console URL:* <${env.BUILD_URL}console|Click Here> \n *Branch Name:* ${env.BRANCH_NAME} \n *Status:* ${buildStatus}"
                SlackNotification.notify(this, buildStatus, notificationsTo, null, build_message)
            }


        }
    }
}

}