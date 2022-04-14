import com.utilities.GlobalVars
import com.utilities.PodiumVars
import com.utilities.DeploymentVars
import com.utilities.ServiceConfig
import com.utilities.GcpService
import com.utilities.JenkinsCredentials

//required keys in config: {sonarProjectKey, repoDNS, targetRepo, artifactName, serviceName, envVarFileId}
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
    - name: jnlp
      image: lldigital-docker.jfrog.io/inbound-agent:windowsservercore-ltsc2019
      env:
      - name: JENKINS_AGENT_WORKDIR
        value: C:/Jenkins/agent
      imagePullPolicy: Always
    - name: dotnet-container
      image: lldigital-docker.jfrog.io/jenkins-dotnet-api:202109.0.1
      imagePullPolicy: Always
      tty: true
      volumeMounts:
        - name: workspace-volume
          mountPath: C:/Jenkins/agent
    volumes:
    - emptyDir: {}
      name: workspace-volume
    imagePullSecrets: [ 'lldigital-docker-jenkins' ]
    nodeSelector:
        poolname: jenkins-windows
    tolerations:
    - key: "node.kubernetes.io/os"
      operator: "Equal"
      value: "windows"
      effect: "NoSchedule"
"""
        }  
    }
    
    stages {

        stage("set build parameters"){
            steps{
                container('dotnet-container') {
                    dir("MX.Systems") {
                        script{
                            def REPO_VERSION = readFile "metadata"
                            env.ARTIFACT_NAME = config.artifactName
                            env.PRE_RELEASE = "-dev"
                            if("${env.GIT_BRANCH}" ==~ GlobalVars.uatRegex){
                                env.PRE_RELEASE = "-rel"
                            } 
                            else if("${env.GIT_BRANCH}" ==~ GlobalVars.jenkinsRegex){
                                env.PRE_RELEASE = "-jens"
                            } 
                            else if("${env.GIT_BRANCH}" ==~ GlobalVars.featureRegex){
                                env.PRE_RELEASE - "-alp"
                            }

                            env.ARTIFACT_VERSION = "${REPO_VERSION}" + "${env.PRE_RELEASE}" + ".${env.BUILD_NUMBER}"
                            echo env.ARTIFACT_VERSION
                        }
                    }
                }
            }
        }
        
        stage("build") {
            when{
                    expression{ return "${env.GIT_BRANCH}" ==~ GlobalVars.buildPackageRegex }
            }
            steps {
                container('dotnet-container') {
                    echo 'building the API...'
                    echo 'building in progress'
                    dir("MX.Systems") {     
                        withCredentials([string(credentialsId: 'sonartoken', variable: 'sonarscanner')]){
                            withSonarQubeEnv("sonarscanner"){
                                bat """
                                    dotnet sonarscanner begin \
                                    /o:lendlease-digital \
                                    /k:${config.sonarProjectKey} \
                                    /d:sonar.cs.opencover.reportsPaths=**/*.xml \
                                    /d:sonar.login=$sonarscanner \
                                    /d:sonar.language="cs" \
                                    /d:sonar.verbose=true \
                                    /d:sonar.host.url=https://sonarcloud.io
                                """
                            }
                        }
                    }
                }
            }
        }
       

        // stage("Test") {
        //     when{
        //             expression{ return "${env.GIT_BRANCH}" ==~ GlobalVars.buildPackageRegex }
        //     }
        //     steps {
        //         container('dotnet-container') {
        //             dir("MX.Systems") {
        //                 bat """
        //                     nuget restore MX.Systems.sln
                        
        //                 """
        //             }
        //             dir("MX.Systems/MX.Systems.Tests") {
        //                 bat """
        //                     MSBuild MX.Systems.Tests.csproj -t:Test -p:PostBuildEventUseInBuild=false
        //                 """
                        
        //             }
        //         }
        //     }
        // }

        stage("Send sonar scan") {
            when{
                    expression{ return "${env.GIT_BRANCH}" ==~ GlobalVars.buildPackageRegex }
            }
            steps {
                container('dotnet-container') {
                    dir("MX.Systems") {
                        withCredentials([string(credentialsId: 'sonartoken', variable: 'sonarscanner')]){
                            withSonarQubeEnv("sonarscanner"){
                                bat """
                                    nuget restore MX.Systems.sln
                                    MSBuild ./MX.Components.GH/MX.Components.GH.csproj -p:Configuration=Release
                                    dotnet sonarscanner end /d:sonar.login=$sonarscanner
                                """
                            }
                        }
                        bat """
                            7z a MX.Systems-${env.ARTIFACT_VERSION}.zip ./MX.Components.GH/bin/*
                            dir
                        """
                    }
                }
            }
        }

        stage("sonar scan results") {
            when{
                    expression{ return "${env.GIT_BRANCH}" ==~ GlobalVars.qcStagesRegex }
            }
            steps {
                dir("MX.Systems") {
                    timeout(time: 10, unit: 'MINUTES') {
                        waitForQualityGate abortPipeline: "${abortOnSonarFailure}"
                    }
                }
            }
        }
      
        stage ('push & publish artifact') {
            when{
                    expression{ return "${env.GIT_BRANCH}" ==~ GlobalVars.buildPackageRegex }
            }
            steps {
                container('dotnet-container'){
                    dir("MX.Systems") {
                        rtUpload (
                        serverId: "${jfrogCloudId}",
                        spec: '''{
                                "files": [
                                {
                                    "pattern": "*.zip",
                                    "target": "POD-MX-Systems/"
                                }
                            ]
                        }''',
                        buildName: "${ARTIFACT_NAME}",
                        buildNumber: "${ARTIFACT_VERSION}"
                    )
                    
                        rtPublishBuildInfo (
                            serverId: "${jfrogCloudId}",
                            buildName: "${ARTIFACT_NAME}",
                            buildNumber: "${ARTIFACT_VERSION}"
                        )
                    }
                }
            }
        }
        
        stage ('xray scan artifact') {
            when{
                    expression{ return "${env.GIT_BRANCH}" ==~ GlobalVars.qcStagesRegex }
            }
            steps {
                container('dotnet-container'){
                    dir("MX.Systems") {
                        timeout(time: "${jfrogXrayTimeout}", unit: "SECONDS"){
                            xrayScan (
                                serverId: "${jfrogCloudId}",
                                buildName: "${env.ARTIFACT_NAME}",
                                buildNumber: "${env.ARTIFACT_VERSION}",
                                failBuild: "${abortOnXrayFailure}",
                                printTable: true
                            )
                        }
                    }
                }
            }
        }
    }
}
}
