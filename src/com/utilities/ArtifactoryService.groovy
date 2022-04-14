package com.utilities

import org.jfrog.hudson.pipeline.scripted.dsl.ArtifactoryPipelineGlobal;
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

class ArtifactoryService{

    static def artifactoryServerId = "jfrog-artifactory"
    static def jfrogXrayTimeoutInSecs = "300"
    static def failBuildOnXrayFailure = false

    static def downloadFileFromArtifactory(String filePathInArtifactory, String destinationFolder, ArtifactoryPipelineGlobal artifactoryGlobal){
        def server = artifactoryGlobal.server(this.artifactoryServerId)
        def downloadSpec = """{
            "files": [
            {
                "pattern": "${filePathInArtifactory}",
                "target": "${destinationFolder}/",
                "flat": "true"
                }
            ]
        }"""
        server.download spec: downloadSpec
    }

    //return true if successfully promoted,
    static def promoteArtifact(String artifactName, String imageTag, String promoteFrom, String promoteTo, ArtifactoryPipelineGlobal artifactoryGlobal, script){

        def validationRes = this.validatePromote(artifactName, imageTag, promoteFrom, promoteTo, script)
        if(!validationRes.isValid){
            throw new Exception(validationRes.message)
        }

        //pull docker image
        def server = artifactoryGlobal.server(this.artifactoryServerId)
        def rtDocker = artifactoryGlobal.docker(server: server)
        def promoteFromDNS = ArtifactRepoHelper.repoConfigs[promoteFrom].virtualRepoDNS
        def completeImageName = promoteFromDNS + "/" + artifactName + ":" + imageTag
        def promoteFromRepo = ArtifactRepoHelper.repoConfigs[promoteFrom].repo
        rtDocker.pull(completeImageName, promoteFromRepo)

        //create buildInfo
        def newBuildInfo = artifactoryGlobal.newBuildInfo()
        newBuildInfo.setNumber(imageTag + '-' + promoteTo)
        newBuildInfo.setName(artifactName)

        //if promoteTo==prod, remove any pre-release tag
        if(promoteTo == "prod"){
            def preReleaseRegex = /(-dev|-rel)/
            imageTag = imageTag.split(preReleaseRegex)[0]
        }

        //retag image
        def promoteToDNS = ArtifactRepoHelper.repoConfigs[promoteTo].uploadRepoDNS
        def completeImageNamePromoteTo = promoteToDNS + "/" + artifactName + ":" + imageTag
        script.sh("""
            docker tag ${completeImageName} ${completeImageNamePromoteTo}
        """)

        //push to promoteTo
        def promoteToRepo = ArtifactRepoHelper.repoConfigs[promoteTo].repo 
        rtDocker.push(completeImageNamePromoteTo, promoteToRepo, newBuildInfo)

        return true
    }

    static def validatePromote(String artifactName, String imageTag, String promoteFrom, String promoteTo, script){

        def response = [
            isValid: false, 
            message: ""
        ]
        def errorMessagePrefix = "Error:ArtifactoryService.validatePromote(${artifactName},${imageTag},${promoteFrom},${promoteTo},${script.env},${script})"

        //if promoteFrom==promoteTo, return false promoteFrom==promoteTo
        if(promoteFrom == promoteTo){
            response.message = "${errorMessagePrefix}:cannot be same environment"
            return response
        }

        //if artifact not in promoteFrom, return false imageTag not in promoteFrom
        def promoteFromDNS = ArtifactRepoHelper.repoConfigs[promoteFrom].virtualRepoDNS
        def completeImageName = promoteFromDNS + "/" + artifactName + ":" + imageTag        
        if(!isImageInRepo(completeImageName,script.env,promoteFrom,script)){
            response.message = "${errorMessagePrefix}:artifact ${completeImageName} doest not exist in promoteFrom"
            return response
        }
        
        //if artifact already in promoteTo, return false imageTag already in promoteTo
        def promoteToDNS = ArtifactRepoHelper.repoConfigs[promoteTo].virtualRepoDNS
        def completeImageNamePromoteTo = promoteToDNS + "/" + artifactName + ":" + imageTag
        def isInPromoteTo = true
        if(isImageInRepo(completeImageNamePromoteTo,script.env,promoteTo,script)){
            response.isValid = true
            response.message = "${errorMessagePrefix}:artifact already in promoteTo"
            return response
        }

        response.isValid = true
        return response
    }

    public static def isImageInRepo(String fullImageName, env, String environment, script){
		def repo = ArtifactRepoHelper.repoConfigs[environment].virtualRepo
		def inRepo = true
        //splitting the fullImageName into repo, artifactName and Tag name
        def values=fullImageName.split("/|\\:")
        def artifactName= values[1]
        def tag=values[2]
        def listOfTags=listDockerTagsInDockerRepo(script, env, repo, artifactName)
       
        if(!listOfTags.contains(tag))
        {
          inRepo=false
        }
		
		return inRepo
	}
	

    //format of files should follow the jfrog FileSpecs == list<map>
    static def uploadFilesToJfrog(List files, ArtifactoryPipelineGlobal artifactoryGlobal, buildInfo){
        def fileSpec = [files: files]
        def builder = new JsonBuilder()
        builder(fileSpec)
        def uploadSpec = """${builder.toString()}"""
        builder = null
        
        def server = artifactoryGlobal.server(this.artifactoryServerId)
        try{
            server.upload(spec: uploadSpec, buildInfo: buildInfo)
        }
        catch(Exception ex){
            throw new Exception("Failed to upload files to jfrog with fileSpec: ${uploadSpec}")
        }

        return uploadSpec
    }

    public static def listDockerTagsInDockerRepo(script, env, String repoName = "docker-prod", String artifactName){
        script.withCredentials([script.usernamePassword(credentialsId: 'csc-artifactory', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
            def encodedString = "${env.USERNAME}:${env.PASSWORD}"
        
            try{
                def credentialsString = encodedString.bytes.encodeBase64().toString()
                def get = new URL("https://lldigital.jfrog.io/artifactory/api/docker/${repoName}/v2/${artifactName}/tags/list?").openConnection()
                get.setRequestMethod("GET")
                get.setDoOutput(true)
                get.setRequestProperty ("Authorization", "Basic " + credentialsString)

                def response = get.getInputStream().getText()
                def jsonSlurper = new JsonSlurper()
                def map = jsonSlurper.parseText(response)
                return map.tags
            }
            catch(Exception ex){
                ex.printStackTrace()
            }

            return []
        }
    }

    public static def listArtifacts(script, env, String repoName, String artifactName){

        def aqlQuery = 'items.find({"repo": {"$eq":"<<repo>>"},"name": {"$match":"<<artifactName>>*"}}).include("name")'
        aqlQuery = aqlQuery.replaceAll("<<repo>>", "${repoName}").replaceAll("<<artifactName>>", "${artifactName}")
        script.withCredentials([script.usernamePassword(credentialsId: 'csc-artifactory', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
            def encodedString = "${env.USERNAME}:${env.PASSWORD}"
            try{
                def credentialsString = encodedString.bytes.encodeBase64().toString()
                def post = new URL("https://lldigital.jfrog.io/artifactory/api/search/aql").openConnection()
                post.setRequestMethod("POST")
                post.setDoOutput(true)
                post.setRequestProperty ("Authorization", "Basic " + credentialsString)
                post.setRequestProperty("Content-Type", "text/plain")
                post.getOutputStream().write((aqlQuery.toString()).getBytes("UTF-8"))
                def response = post.getInputStream().getText()
                def jsonSlurper = new JsonSlurper()
                def map = jsonSlurper.parseText(response)
                return map.results.name
            }
            catch(Exception ex){
                ex.printStackTrace()
            }
            return []
        }
    }
}
