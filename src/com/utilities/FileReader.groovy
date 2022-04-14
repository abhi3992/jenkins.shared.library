package com.utilities
import com.utilities.GcpService
import com.utilities.DeploymentVars
import com.utilities.ServiceConfig
import groovy.json.JsonSlurperClassic
import groovy.json.JsonSlurper
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
// import groovy.yaml.YamlSlurper

class FileReader{

    @NonCPS
    static def getMapFromFile(String fileContentAsString){
        def jsonSlurper = new JsonSlurperClassic()
        def fileMap = jsonSlurper.parseText(fileContentAsString)

        return fileMap
    }

    static def getMapFromPath(String filePath){
        def tokens = filePath.split('.')
        def extension = tokens[tokens.size()-1]
        def file = new File(filePath)
        def map

        if(extension == 'json'){
            def jsonSlurper = new JsonSlurper()
            map = jsonSlurper.parse(file)
        }
        // else if(extension == 'yaml'){
        //     def yamlSlurper = new YamlSlurper()
        //     map = yamlSlurper.parse(file)
        // }
        else{
            throw new Exception('Only [json] file extensions supported')
        }

        return map
    }


    static def replaceElements(String stringContent, String projectId, script, env){      
        try{
            def secretVariables = stringContent.findAll(~/(sm:\/\/){1}([A-Za-z0-9\-_])+(\/(Latest)|\/(latest)|\/[0-9]+)?/)
            secretVariables = sort(secretVariables)
            script.sh(script:"echo ${secretVariables.toString()}")

            for(def secretVariable in secretVariables){
                def tokens = secretVariable.split('/')
                def secretKey = tokens[2]
                def secretVersion = (tokens.size() == 4) ? tokens[3] : 'latest'
                def secretValue = GcpService.getSecretFromSecretManager(projectId, secretKey, secretVersion, script, env)
                stringContent = stringContent.replaceAll(secretVariable, secretValue)
            }
        }
        catch(Exception ex){
            ex.printStackTrace()
            throw ex
        }

        return stringContent
    }

    static def sort(arr){

        def n = arr.size()
        for (def i=1 ;i<n; i++)
        {
            def temp = arr[i];
            def j = i - 1;
            while (j >= 0 && temp.size() > arr[j].size()){
                arr[j+1] = arr[j];
                j--;
            }
            arr[j+1] = temp;
        }

        return arr
    }

    static def getlistOfFilesAndFolders(script,path){
        def listOfFilesAndFolders = script.sh(script:"cd ${path} && ls", returnStdout: true).trim().split("\\r?\\n")

        return listOfFilesAndFolders
    }

    static def isServiceDependenciesFolderPresent(script, path){
        def listOfFilesFolders = this.getlistOfFilesAndFolders(script, path)

        for(String fileName : listOfFilesFolders){
            if(fileName == 'service-dependencies'){
                return true
            }
        }

        return false
    }
}