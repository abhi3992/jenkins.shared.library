import com.utilities.DeploymentVars
import com.utilities.ServiceConfig
import com.utilities.ArtifactoryService
import com.utilities.UserAuthorization
import com.utilities.ArtifactRepoHelper
import com.utilities.GlobalVars
import com.utilities.FileReader
import com.utilities.DeployService
import com.utilities.SlackNotification
import com.utilities.GitServices
import com.utilities.PreBuildValidationService
import com.utilities.GcpService
import com.utilities.JenkinsCredentials

import groovy.json.JsonSlurper

def call(Map config){
    def repoVersion = ""
    def artifactVersion = ""
    def fullImageName = ""
    def storybookFullImageName = ""
    def buildInfo
    def server = Artifactory.server(ArtifactoryService.artifactoryServerId)
    def buildImage = (config.containsKey('buildImage')) ? config.buildImage : 'jenkins-react-ui:16.13.3'

    def buildStatus = GlobalVars.successStatus
    def build_message = ""
    def commitHash
    def commiter_Email
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
        limits:
          memory: "4Gi"
          cpu: "1500m"
        requests:
          memory: "3Gi"
          cpu: "1000m"
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
                if("${env.BRANCH_NAME}" ==~ GlobalVars.buildPackageRegex){
                    
                    checkout scm
                    def package_js_string = sh(script: "cat ${WORKSPACE}/package.json", returnStdout: true).trim()
                    def package_js = readJSON(text: package_js_string)
                    repoVersion = package_js.version
                    //clear top
                    artifactVersion = ArtifactRepoHelper.getFullTagWithPreRelease("${env.BRANCH_NAME}", repoVersion, "docker", config.artifactName, this, env)
                    fullImageName = ArtifactRepoHelper.getfullDockerImageName(config.artifactName, "${env.BRANCH_NAME}", repoVersion, "docker", true, "dev", this, env)
                    def tag = ArtifactRepoHelper.getFullTagWithPreRelease("${env.BRANCH_NAME}", repoVersion, "docker", config.artifactName, this, env)
                    storybookFullImageName = config.repoDNS + "/" + config.artifactName + "-storybook:" + tag

                    commitHash = GitServices.getLatestCommitHash(this, env.WORKSPACE)
                    commiter_Email = GitServices.getCommitterEmail(this, commitHash, env.WORKSPACE)
                    upstreamData = [commiter_Email: commiter_Email]

                    stage('pre-build validation'){
                        PreBuildValidationService.preBuildValidation("${env.WORKSPACE}", fullImageName.toString(), config.artifactName, repoVersion, this, env)
                        PreBuildValidationService.preBuildValidation("${env.WORKSPACE}", storybookFullImageName.toString(), config.artifactName, repoVersion, this, env)
                    }

                    stage('build'){
                     
                        def npmrc_file = new File("npmrc.txt")
                        def npmrcSecrets = GcpService.getSecretFromSecretManager(JenkinsCredentials.gcpProjectId, JenkinsCredentials.credentials['npmrc'].secretName, JenkinsCredentials.credentials['npmrc'].version, this, env)
                        writeFile(file: "npmrc.txt", text:"${npmrcSecrets}" , encoding: "UTF-8")
                        sh(script:"""
                            cp ${npmrc_file} .npmrc
                        """)

                        sh(script: ''' 
                            yarn install
                            ls -la
                            yarn build:prod
                        ''')
                    }
                    
                    stage('build docker image'){
                        sh"""
                            cd ${WORKSPACE}
                            docker build -f Dockerfile -t ${fullImageName} .
                            docker build -f Dockerfile.Storybook -t ${storybookFullImageName} .
                        """
                    }
                    
                    stage ('push & publish artifact') {

                        buildInfo = Artifactory.newBuildInfo("${config.artifactName}", artifactVersion)
                        def rtDocker = Artifactory.docker(server: server)
                        def targetRepo = ArtifactRepoHelper.repoConfigs["dev"].repo

                        rtDocker.push(fullImageName, targetRepo, buildInfo)
                        rtDocker.push(storybookFullImageName, targetRepo, buildInfo)
                        server.publishBuildInfo(buildInfo)                
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

                    stage('deploy to dev cluster'){
                        if(
                            ("${env.BRANCH_NAME}" ==~ GlobalVars.devRegex) && 
                            (ServiceConfig.serviceParams[config.artifactName].containsKey("serviceName"))
                        ){
                            def environment = (ServiceConfig.serviceParams[config.artifactName].containsKey("defaultCDEnvironment")) ? ServiceConfig.serviceParams[config.artifactName].defaultCDEnvironment : "dev"
                            DeployService.deployPipeline(environment, artifactVersion, "${config.artifactName}", upstreamData, this)
                            DeployService.deployPipeline(environment, artifactVersion, "${config.artifactName}-storybook", upstreamData, this)

                        }
                    }
                }

                if("${env.BRANCH_NAME}" ==~ GlobalVars.gitTagRegex){
                    stage('tag main'){
                        withCredentials([sshUserPrivateKey(credentialsId: 'masterbitbucket', keyFileVariable: 'SSH_KEY', usernameVariable: "SSH_KEY_USER")]) {
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
            }
            catch(Exception ex){
                buildStatus = GlobalVars.failureStatus
                ex.printStackTrace()
                throw ex
            }
            finally{
                build_message = "*Job name*: ${env.JOB_NAME} \n *Build number*: ${env.BUILD_NUMBER} \n *Console URL:* <${env.BUILD_URL}console|Click Here> \n *Branch Name:* ${env.BRANCH_NAME} \n *Status:* ${buildStatus}"
                SlackNotification.notify(this, buildStatus, commiter_Email, null, build_message)
            }

        }
    }
}
}
