package com.utilities
import org.jfrog.hudson.pipeline.scripted.dsl.ArtifactoryPipelineGlobal;
import com.utilities.ArtifactoryService

class PreBuildValidationService {
    
    static def validationResults = []
public static def isJiraTicketPresent(script, "${env.WORKSPACE}"){
def branchList = ["development" , "release", "main","master"]
if (branchList.contains(env.WORKSPACE)){
         def commit= script.sh (script:"git log --merges -n 1", returnStdout: true ).trim()
         script.echo"${commit}"
         def jiraRegex= (/(PDSO|PSAX|PCUBS|PPI|PPP|ENV)-[0-9]/)
         if(commit =~ jiraRegex){
         return ""
         } else {
         return "JIRA Ticket not mentioned in last merge commit"
         }  
    }
else {
        return ""
        }
}    
    public static def isBuildGradlePresent(String folderPath, script){
        def path = folderPath
        def exists = script.fileExists("${path}/build.gradle")
        if(exists){
            return true
        }
        else{
            println("File does not exist")
            return false
        }
    }

    static def isGradlePublicRepoInside(String folderPath, script){

        if (!isBuildGradlePresent(folderPath, script)){
            return ""
        }
        
        def publicRepos = [
            'mavencentral()','google()', 'jcentre()', 'ivy{', 'clojarsrepo()', 'clojuresnapshotsrepo()', 
            'gradlepluginportal()', 'https://oss.sonatype.org', 'https://packages.atlassian.com', 'https://maven.atlassian.com', 
            'https://repository.jboss.org', 'https://repo.hortonworks.com', 'https://repo.spring.io', 'https://nexus.bedatadriven.com', 
            'https://nexus.pentaho.org','https://maven.wso2.org', 'https://maven.xwiki.org', 'https://maven.ibiblio.org', 
            'http://maven-repository.openspaces.org', 'https://dl.bintray.com/kotlin', "https://repository.apache.org/content/repositories/snapshots/",
            "https://repo.maven.apache.org/maven2", "https://repo.spring.io/milestone", "https://repo.spring.io/snapshot", "https://packages.confluent.io/maven"
        ]
        def publicRepoFound = []
        def gradlecontent = script.sh(script: "cat ${folderPath}/build.gradle", returnStdout: true).trim().toLowerCase()
        
        for(repo in publicRepos){
            if(gradlecontent.contains(repo)){
                publicRepoFound.add(repo)
            }
        }
        if(publicRepoFound.size()!=0){
            return "Build has failed as public repositories found in build.gradle: ${publicRepoFound}. Please remove them."
        }
        else{
            return ""
        }
    }
        
    public static def findArtifactType(String folderPath, String keyword, script){
        def path = folderPath
        def key = keyword
        def filecontent = script.readFile("${path}/Jenkinsfile")
        return filecontent.contains(key)
    }

    public static def verifyIfImageAlreadyPresent(String workspacePath, String fullImageName, env, script){

        def buildGradlePresent = isBuildGradlePresent(workspacePath, script)
        def isKeyWordPresent = findArtifactType(workspacePath, 'javaContainer',script)
        if((!buildGradlePresent) || (!isKeyWordPresent)){
            return ""
        }
        def isImagePresent = ArtifactoryService.isImageInRepo(fullImageName,env ,"dev" ,script)
        if(isImagePresent){
            return "${fullImageName} is already in artifactory"
        }
        else {
            return ""
        }
    }

    public static def checkProdMajorVersion(String workspacePath, String artifactName, String repoVersion, script, env){
        def buildGradlePresent = isBuildGradlePresent(workspacePath, script)
        def isKeyWordPresent = findArtifactType(workspacePath, 'javaContainer', script)
        if((!buildGradlePresent) || (!isKeyWordPresent)){
            return ""
        }
        def prodVersions =  ArtifactoryService.listDockerTagsInDockerRepo(script, env, artifactName)
        if(prodVersions.contains(repoVersion)){
            return "${repoVersion} already exists on production please amend the version currently on the repository"
        }
        else{
            return "" 
        }
         
    }
    
    public static def preBuildValidation(String workspacePath, String fullImageName, String artifactName, String repoVersion, script, env) {
        validationResults.add(verifyIfImageAlreadyPresent(workspacePath, fullImageName, env, script))
        validationResults.add(checkProdMajorVersion(workspacePath, artifactName, repoVersion, script, env))
        validationResults.add(isGradlePublicRepoInside(workspacePath, script))
        validationResults.add(isJiraTicketPresent(script ,env.WORKSPACE))
        
        def buildFailed = false
        for(int i = 0; i < validationResults.size(); i++){
            if (validationResults[i] != ""){
                buildFailed = true
            }
        }
        if(buildFailed){
            throw new Exception("build failed due to the following failures: ${validationResults.toString()}")
        }            
    }
}