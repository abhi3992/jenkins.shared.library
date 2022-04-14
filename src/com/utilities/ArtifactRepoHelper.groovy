package com.utilities
import com.utilities.ArtifactoryService
class ArtifactRepoHelper{

    static def jfrogInstance = [
        id: "jfrog-artifactory",
        url: "https://lldigital.jfrog.io/artifactory",
        jenkinsCredentialID: "jfrog-jenkins" // value configured on jenkins
    ]

    static def repoConfigs = [
        dev: [
            uploadRepoDNS: "lldigital-docker-local.jfrog.io",
            virtualRepoDNS: "lldigital-docker.jfrog.io",
            repo: "docker-local",
            virtualRepo: "docker"
        ],
        qa: [
            uploadRepoDNS: "lldigital-docker-local-qa.jfrog.io",
            virtualRepoDNS: "lldigital-docker-qa.jfrog.io",
            repo: "docker-local-qa",
            virtualRepo: "docker-qa"
        ],
        uat: [
            uploadRepoDNS: "lldigital-docker-local-uat.jfrog.io",
            virtualRepoDNS: "lldigital-docker-uat.jfrog.io",
            repo: "docker-local-uat",
            virtualRepo: "docker-uat"
        ],
        prod: [
            uploadRepoDNS: "lldigital-docker-local-prod.jfrog.io",
            virtualRepoDNS: "lldigital-docker-prod.jfrog.io",
            repo: "docker-local-prod",
            virtualRepo: "docker-prod"
        ],
    ]

    static def getfullDockerImageName(String artifact_name, String branch, String repo_version, String repoName, boolean isUpload, String environment, script, env){
        def repoDNS = this.repoConfigs[environment].virtualRepoDNS
        if(isUpload){
            repoDNS = this.repoConfigs[environment].uploadRepoDNS
        }

        def tag = this.getFullTagWithPreRelease(branch, repo_version, repoName, artifact_name, script, env)
        def fullImageName = repoDNS + "/" + artifact_name + ":" + tag

        return fullImageName
    }

    static def getFullTagWithPreRelease(String branch, String repo_version, String repoName, String artifactName, script, env){

        def tagWithoutBuildNumber = ""
        def build_number = ""
        def artifactVersion = ""

        if(branch ==~ GlobalVars.jenkinsRegex){
            tagWithoutBuildNumber = repo_version + "-" + branch.replaceAll('-','').replaceAll("\\.",'').replaceAll('/','') + "."
        }
        else if(branch ==~ GlobalVars.featureRegex){
            tagWithoutBuildNumber = repo_version + "-" + branch.replaceAll('-','').replaceAll("\\.",'').replaceAll('/','') + "."
        }
        else if (branch ==~ GlobalVars.devRegex){
            tagWithoutBuildNumber = repo_version + "-dev."
        }

        else if (branch ==~ GlobalVars.packageReleaseRegex){
            tagWithoutBuildNumber = repo_version + "-rel."
        }
        else if (!(branch ==~ GlobalVars.gitTagRegex)){
            tagWithoutBuildNumber = repo_version + "-" + branch.replaceAll('-','').replaceAll("\\.",'').replaceAll('/','') + "."
        }
        build_number = getLatestBuildNumber(repoName, artifactName, script, env, tagWithoutBuildNumber)
        artifactVersion = tagWithoutBuildNumber + build_number
        return artifactVersion 
    }

    public static def getLatestBuildNumber(String repoName, String artifactName, script, env, String tagWithoutBuildNumber){
        def tagList = []
        List buildNumber = []
        try{
            if (repoName == "docker"){
                try {
                    tagList = ArtifactoryService.listDockerTagsInDockerRepo(script, env, repoName, artifactName)
                    for (artifacts in tagList){
                        if(artifacts.contains(tagWithoutBuildNumber)){
                            buildNumber.add(artifacts.split('\\.')[-1]as int)
                        }
                    }
                }
                catch(Exception ex){
                    ex.printStackTrace()
                    return 1
                }
            }
            else{
                try {
                    tagList = ArtifactoryService.listArtifacts(script, env, repoName, artifactName)
                    for (artifacts in tagList){
                        if(!artifacts.contains(".pom") && artifacts.contains(tagWithoutBuildNumber) && !artifacts.contains("-all")){
                            buildNumber.add(artifacts.split('\\.')[-2]as int)
                        }
                    }
                }
                catch(Exception ex){
                    ex.printStackTrace()
                    return 1
                }
            }
            return buildNumber.sort()[-1] + 1
        }
        catch(Exception ex){
            ex.printStackTrace()
            return 1
        }
    } 
}