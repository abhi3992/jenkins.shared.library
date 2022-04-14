package com.utilities
import groovy.json.JsonSlurper
import com.utilities.DeploymentVars

class CloudController {

    static def deployService(Map serviceParams, String serviceName, List containerImages, String tenancy) {
        def apiBaseUrl = DeploymentVars.deploymentConfig.apiUrl
        def post = new URL("${apiBaseUrl}/deploy").openConnection()
        def jsonSlurper = new JsonSlurper()
        def defaultParams = serviceParams
        def paramBody = [name : serviceName, 
            serviceContainers : containerImages, 
            tenancy: tenancy]
        def body = (defaultParams + paramBody).toString()
        def requestBody = jsonSlurper.parseText(body)
        post.setRequestMethod("POST")
        post.setDoOutput(true)
        post.setRequestProperty("Content-Type", "application/json")
        post.getOutputStream().write((requestBody.toString()).getBytes("UTF-8"));
        def postRC = post.getResponseCode();
        if (postRC.equals(200)) {
            return post.getInputStream().getText();
        }
    }
}
