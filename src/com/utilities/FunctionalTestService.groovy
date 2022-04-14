package com.utilities

import com.utilities.ServiceConfig
import groovy.json.JsonBuilder

class FunctionalTestService{

    static def testPipeline(String environment, String serviceName, Map upstreamData, script) {
        def artifactName = ServiceConfig.getArtifactName(serviceName)
        environment = environment.split('-')[0]

        if(
            !(ServiceConfig.serviceParams[artifactName].containsKey("testEnv")) ||
            !(ServiceConfig.serviceParams[artifactName].containsKey("testType")) ||
            !(ServiceConfig.serviceParams[artifactName].testEnv.containsKey(environment))
        ){
            println("No test cases for ${serviceName} in ${environment}")
            return
        }

        def testType = ServiceConfig.serviceParams[artifactName].testType
        def builder = new JsonBuilder()
        builder(upstreamData)
        def upstreamDataText = builder.toString()
        builder = null
        script.build(job: "CT/${testType}", parameters: [ 
            script.string(name: 'ClusterEnv', value: environment),
            script.string(name: 'ArtifactName', value: artifactName),
            script.string(name: 'UpstreamData', value: upstreamDataText)
        ])     
    }
}