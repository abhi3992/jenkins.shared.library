package com.utilities

import com.utilities.ServiceConfig
import groovy.json.JsonBuilder

class DeployService {

    static def deployPipeline(String environment, String artifactVersion, String artifactName, Map upstreamData, script) {
        def serviceName = ServiceConfig.serviceParams[artifactName].serviceName
        def builder = new JsonBuilder()
        builder(upstreamData)
        def upstreamDataText = builder.toString()
        builder = null

        if (ServiceConfig.serviceParams[artifactName].serviceType == "airflow") {
            script.build(job: "Deploy/AirflowDeploy", parameters: [
                script.string(name: 'Environment', value: environment),
                script.string(name: 'ServiceName', value: serviceName),
                script.string(name: 'ImageTag', value: artifactVersion),
                script.string(name: 'UpstreamData', value: upstreamDataText)
            ])

        }
        else if (ServiceConfig.serviceParams[artifactName].serviceType == "containerDeploy") {
            def namespace = ServiceConfig.getArtifactDefaultNamespace(artifactName, environment)
            script.build(job: "Deploy/ContainerDeployService", parameters: [
                script.string(name: 'ServiceName', value: serviceName),
                script.string(name: 'Environment', value: environment),
                script.string(name: 'ImageTag', value: artifactVersion),
                script.string(name: 'Namespace', value: namespace),
                script.string(name: 'UpstreamData', value: upstreamDataText)
            ])
        }
    }
}