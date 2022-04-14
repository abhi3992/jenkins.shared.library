package com.utilities

class ServiceConfig{

    static def serviceParams = [
        "podium-env-cnt-svc-tenancy-scenario":[
            serviceName: "tenancy-scenario-service",
            helmEnvironment: "podium-in-io-dev",
            namespace: "csc",
            testEnv: [
                dev: ['@ScenarioServiceSmoke'],
                qa:  ['@ScenarioServiceSmoke', '@ScenarioServiceRegression'],
                uat: ['@ScenarioServiceSmoke']
            ],
            serviceType: "containerDeploy",
            testType: "apiTest",
            testRepoUrl: "git@bitbucket.org:lendlease_corp/podium.envision.api.automation.git",
            tenancyType: PodiumVars.sharedTenancy
        ],

        "podium-lib-cnt-svc-mapping":[
            serviceName: "podium-mapping-service",
            helmEnvironment: "podium-in-io-dev",
            namespace: "csc",
            tenancyType: PodiumVars.sharedTenancy,
            serviceType: "containerDeploy"
        ],

        "podium-csc-cnt-int-storaenso":[
            serviceName: "podium-csc-cnt-int-storaenso",
            helmEnvironment: "podium-in-io-dev",
            namespace: "adaptors",
            tenancyType: PodiumVars.adaptorsTenancy,
            serviceType: "containerDeploy"
        ],

        "podium-csc-cnt-ent-project":[
            serviceName: "csc-project-service",
            helmEnvironment: "podium-in-io-dev",
            namespace: "csc",
            testEnv: [
                dev: ['@prjServSmoke'],
                qa:  ['@prjServSmoke', '@prjServRegression'],
                uat: ['@prjServSmoke']
            ],
            serviceType: "containerDeploy",
            testType: "apiTest",
            testType: "apiTest",
            testRepoUrl: "git@bitbucket.org:lendlease_corp/podium.api.automation.karate.git",
            tenancyType: PodiumVars.sharedTenancy
        ],

        "lib-cnt-router":[
            serviceName: "csc-router-service",
            helmEnvironment: "podium-io-dev",
            namespace: "lendleaseau",
            tenancyType: PodiumVars.customerTenancy,
            serviceType: "containerDeploy"
        ],

        "podium-csc-cnt-ent-project-site":[
            serviceName: "csc-site-service",
            helmEnvironment: "podium-in-io-dev",
            namespace: "csc",
            testEnv: [
                dev: ['@siteSmoke'],
                qa:  ['@siteSmoke','@siteRegression'],
                uat: ['@siteSmoke']
            ],
            serviceType: "containerDeploy",
            testType: "apiTest",
            testRepoUrl: "git@bitbucket.org:lendlease_corp/podium.api.automation.karate.git",
            tenancyType: PodiumVars.sharedTenancy
        ],

        "csc-cnt-ent-vendor":[
            serviceName: "csc-vendor-service",
            helmEnvironment: "podium-in-io-dev",
            namespace: "csc",
            testEnv: [
                dev: ['@vendorServSmoke'],
                qa:  ['@vendorServSmoke', '@vendorServRegression'],
                uat: ['@vendorServSmoke']
            ],
            serviceType: "containerDeploy",
            testType: "apiTest",
            testRepoUrl: "git@bitbucket.org:lendlease_corp/podium.api.automation.karate.git",
            tenancyType: PodiumVars.sharedTenancy
        ],

        "podium-csc-cnt-svc-workflow":[
            serviceName: "csc-workflow-service",
            helmEnvironment: "podium-in-io-dev",
            namespace: "csc",
            tenancyType: PodiumVars.sharedTenancy,
            serviceType: "containerDeploy"
        ],
        
        "podium-ppi-cnt-ent-insights-engine":[
            serviceName: "ppi-insights-engine",
            helmEnvironment: "podium-io-dev",
            namespace: "lendleaseau",
            tenancyType: PodiumVars.customerTenancy,
            serviceType: "containerDeploy"
        ],

        "podium-cubs-cnt-svc-modelruntime":[
            serviceName: "podium-cubs-cnt-svc-modelruntime",
            helmEnvironment: "podium-in-io-dev",
            namespace: "csc",
            testEnv: [
                dev: ['@Regression']
            ],
            serviceType: "containerDeploy",
            testType: "apiTest",
            testRepoUrl: "git@bitbucket.org:lendlease_corp/podium.cubs.api.automation.git",
            tenancyType: PodiumVars.sharedTenancy,
            defaultCDEnvironment: "dev-spacex"
        ],

        "podium-lib-cnt-svc-platform-mailservice":[
            serviceName: "mail-service",
            helmEnvironment: "podium-in-io-dev",
            namespace: "shared-service",
            tenancyType: PodiumVars.sharedTenancy,
            serviceType: "containerDeploy"
        ],

        "podium-tenancy-security-service":[
            serviceName: "podium-tenancy-security-service",
            helmEnvironment: "podium-io-dev",
            namespace: "lendleaseau",
            tenancyType: PodiumVars.customerTenancy,
            serviceType: "containerDeploy"
        ],

        "csc-cnt-int-schindler":[
            serviceName: "csc-schindler-service",
            helmEnvironment: "podium-in-io-dev",
            namespace: "adaptors",
            tenancyType: PodiumVars.adaptorsTenancy,
            serviceType: "containerDeploy"
        ],

        "podium-csc-cnt-int-primaverap6":[
            serviceName: "podium-csc-cnt-int-primaverap6",
            helmEnvironment: "podium-in-io-dev",
            namespace: "adaptors",
            tenancyType: PodiumVars.adaptorsTenancy,
            serviceType: "containerDeploy"
        ],

        "csc-cnt-ent-project.delivery":[
            serviceName: "csc-delivery-service",
            helmEnvironment: "podium-in-io-dev",
            namespace: "csc",
            testEnv: [
                dev: ['@deliveryServSmoke'],
                qa:  ['@deliveryServSmoke', '@deliveryServRegression'],
                uat: ['@deliveryServSmoke']
            ],
            serviceType: "containerDeploy",
            testType: "apiTest",
            testRepoUrl: "git@bitbucket.org:lendlease_corp/podium.api.automation.karate.git",
            tenancyType: PodiumVars.sharedTenancy
        ],

        "podium-env-cnt-svc-tenancy-finance":[
            serviceName: "tenancy-finance-service",
            serviceType: "containerDeploy",
            helmEnvironment: "podium-in-io-dev",
            namespace: "csc",
            testEnv: [
                dev : ["@FinanceServiceSmoke"],
                qa: ["@FinanceServiceSmoke", "@FinanceServiceRegression"],
                uat: ["@FinanceServiceSmoke"]
            ],
            testType: "apiTest",
            testRepoUrl: "git@bitbucket.org:lendlease_corp/podium.envision.api.automation.git",
            tenancyType: PodiumVars.sharedTenancy
        ],

        "podium-lib-cnt-ent-documentcontrol":[
            serviceName: "podium-lib-cnt-ent-documentcontrol",
            helmEnvironment: "podium-in-io-dev",
            namespace: "csc",
            testEnv: [
                dev: ['@docCntrlSmoke'],
                qa:  ['@docCntrlSmoke', '@docCntrlRegression'],
                uat: ['@docCntrlSmoke']
            ],
            serviceType: "containerDeploy",
            testType: "apiTest",
            testRepoUrl: "git@bitbucket.org:lendlease_corp/podium.api.automation.karate.git",
            tenancyType: PodiumVars.sharedTenancy
        ],

        "csc-cnt-ent-project-schedule":[
            serviceName: "csc-schedule-service",
            helmEnvironment: "podium-in-io-dev",
            namespace: "csc",
            testEnv: [
                dev: ['@prjActvitySmoke'],
                qa:  ['@prjActvitySmoke', '@prjActivityRegression'],
                uat: ['@prjActvitySmoke']
            ],
            serviceType: "containerDeploy",
            testType: "apiTest",
            testRepoUrl: "git@bitbucket.org:lendlease_corp/podium.api.automation.karate.git",
            tenancyType: PodiumVars.sharedTenancy
        ],

        "podium-test-cnt-ent-example-entityservice":[
            serviceName: "test-demoentity-service",
            helmEnvironment: "podium-in-io-dev",
            namespace: "csc",
            tenancyType: PodiumVars.sharedTenancy,
            serviceType: "containerDeploy"
        ],

        "podium-lib-cnt-svc-reference-standards":[
            serviceName: "reference-standards-service",
            helmEnvironment: "podium-in-io-dev",
            namespace: "csc",
            tenancyType: PodiumVars.sharedTenancy,
            testEnv: [
                dev : ["@RefrenceDataServiceSmoke"],
                qa: ["@RefrenceDataServiceSmoke", "@RefrenceDataServiceRegression"],
                uat: ["@RefrenceDataServiceSmoke"]
            ],
            testType: "apiTest",
            testRepoUrl: "git@bitbucket.org:lendlease_corp/podium.envision.api.automation.git",
            serviceType: "containerDeploy"
        ],

        "podium.ppi-cnt-ent-asset.commissioning":[
            serviceName: "ppi-asset-service",
            helmEnvironment: "podium-in-io-dev",
            namespace: "csc",
            tenancyType: PodiumVars.sharedTenancy,
            serviceType: "containerDeploy"
        ],

        "podium-env-web-exp-envision":[
            serviceName: "podium-env-web-exp-envision",
            helmEnvironment: "podium-io-dev",
            namespace: "lendleaseau",
            tenancyType: PodiumVars.customerTenancy,
            serviceType: "containerDeploy"
        ],

        "podium-lib-npm-ui":[
            serviceName: "podium-lib-npm-ui",
            helmEnvironment: "podium-io-dev",
            namespace: "lendleaseau",
            tenancyType: PodiumVars.customerTenancy,
            serviceType: "containerDeploy"
        ],

        "podium-lib-npm-ui-storybook":[
            serviceName: "podium-lib-npm-ui-storybook",
            helmEnvironment: "podium-io-dev",
            namespace: "lendleaseau",
            tenancyType: PodiumVars.customerTenancy,
            serviceType: "containerDeploy"
        ],

        "podium.lib-cnt-app-podium":[
            serviceName: "podium-ui-framework",
            helmEnvironment: "podium-io-dev",
            namespace: "lendleaseau",
            serviceType: "containerDeploy",
            testRepoUrl: "git@bitbucket.org:lendlease_corp/podium-ppi-ui-automation.git",
            tenancyType: PodiumVars.customerTenancy
        ],

        "podium-lib-cnt-svc-tenancy-projectapppref":[
            serviceName: "csc-projectapppref",
            helmEnvironment: "podium-io-dev",
            namespace: "csc",
            testEnv: [
                dev: ['@projPrefSmoke'],
                qa:  ['@projPrefSmoke'],
                uat: ['@projPrefSmoke']
            ],
            serviceType: "containerDeploy",
            testType: "apiTest",
            testRepoUrl: "git@bitbucket.org:lendlease_corp/podium.api.automation.karate.git",
            tenancyType: PodiumVars.customerTenancy
        ],

        "podium.ppi-cnt-ent-insights":[
            serviceName: "podium-property-insights",
            helmEnvironment: "podium-in-io-dev",
            namespace: "csc",
            tenancyType: PodiumVars.sharedTenancy,
            serviceType: "containerDeploy",
        ],

        "podium-cc-cnt-svc-app-deployment":[
            serviceName: "podium-deployment-service",
            helmEnvironment: "podium-io-dev",
            namespace: "csc",
            tenancyType: PodiumVars.cloudControllerTenancy,
            serviceType: "containerDeploy"
        ],

        "podium-lib-npm-uiframework":[
            serviceName: "podium-lib-npm-uiframework",
            helmEnvironment: "podium-io-dev",
            namespace: "lendleaseau",
            tenancyType: PodiumVars.customerTenancy,
            serviceType: "containerDeploy"
        ],

        "podium-csc-npm-exp-order-tracking":[
            serviceName: "podium-csc-npm-exp-order-tracking",
            helmEnvironment: "podium-io-dev",
            namespace: "lendleaseau",
            tenancyType: PodiumVars.customerTenancy,
            serviceType: "containerDeploy"
        ],

        "podium-ppi-npm-exp-insights":[
            serviceName: "podium-ppi-npm-exp-insights",
            helmEnvironment: "podium-io-dev",
            namespace: "lendleaseau",
            tenancyType: PodiumVars.customerTenancy,
            testRepoUrl: "git@bitbucket.org:lendlease_corp/podium-ppi-ui-automation.git",
            testEnv: [
                dev: ['@PPIUISmoke'],
                qa:  ['@PPIUISmoke']
            ],
            testType: "UiTest",
            serviceType: "containerDeploy"
        ],

        "podium-hma-npm-exp-home":[
            serviceName: "podium-hma-npm-exp-home",
            helmEnvironment: "podium-io-dev",
            namespace: "lendleaseau",
            tenancyType: PodiumVars.customerTenancy,
            serviceType: "containerDeploy"
        ],

        "podium-wkp-npm-exp-workplace":[
            serviceName: "podium-wkp-npm-exp-workplace",
            helmEnvironment: "podium-io-dev",
            namespace: "lendleaseau",
            tenancyType: PodiumVars.customerTenancy,
            serviceType: "containerDeploy"
        ],

        "podium-ppi-cnt-ent-workspace-service":[
            serviceName: "workspace-service",
            helmEnvironment: "podium-io-dev",
            namespace: "lendleaseau",
            tenancyType: PodiumVars.customerTenancy,
            serviceType: "containerDeploy"
        ],

        "podium.ppi-cnt-ent-metrics":[
            serviceName: "ppi-metrics-service",
            helmEnvironment: "podium-in-io-dev",
            namespace: "csc",
            tenancyType: PodiumVars.sharedTenancy,
            serviceType: "containerDeploy",
            testEnv: [
                dev: ['@ppiSmoke'],
                qa:  ['@ppiSmoke', '@ppiRegression']
            ],
            testType: "apiTest",
            testRepoUrl: "git@bitbucket.org:lendlease_corp/podium-ppi-api-automation.git"
        ],
        
        "podium-cubs-cnt-svc-modelruntime-async":[
            serviceName: "podium-cubs-cnt-svc-modelruntime-async",
            helmEnvironment: "podium-in-io-dev",
            namespace: "csc",
            tenancyType: PodiumVars.sharedTenancy,
            serviceType: "containerDeploy"
        ],

        "podium.lib-jar-shell.connectors":[
            serviceType: "airflow",
            serviceName: "podium.lib-jar-shell.connectors",
            buildImage: "jenkins-spring-boot-gradle:1.0.0-java8-gcloud"
        ],

        "podium.lib-jar-weather.connector":[
            serviceType: "airflow",
            serviceName: "podium.lib-jar-weather.connector",
            buildImage: "jenkins-spring-boot-gradle:1.0.0-java8-gcloud"
        ],
        
        "podium.ppi-lib-jar-workplace.acnsentiment.metrics":[
            serviceType: "airflow",
            serviceName: "podium.ppi-lib-jar-workplace.acnsentiment.metrics",
            buildImage: "jenkins-spring-boot-gradle:1.0.0-java8-gcloud"
        ],
        
        "podium.lib-jar-metrics.uploader":[
            serviceType: "airflow",
            serviceName: "podium.lib-jar-metrics.uploader",
            buildImage: "jenkins-spring-boot-gradle:1.0.0-java8-gcloud"
        ],

        "podium.ppi-cim-connectors":[
            serviceType: "airflow",
            serviceName: "podium.ppi-cim-connectors",
            buildImage: "jenkins-spring-boot-gradle:1.0.0-java8-gcloud"
        ],

        "podium.ppi-lib-jar-cardaccess.connector":[
            serviceType: "airflow",
            serviceName: "podium.ppi-lib-jar-cardaccess.connector",
            buildImage: "jenkins-spring-boot-gradle:1.0.0-java8-gcloud"
        ],

        "podium.ppi-lenel-adaptor":[
            serviceType: "airflow",
            serviceName: "podium.ppi-lenel-adaptor",
            buildImage: "jenkins-spring-boot-gradle:1.0.0-java8-gcloud"
        ],

        "podium.ppi-lib-jar-meetingfiles.connector":[
            serviceType: "airflow",
            serviceName: "podium.ppi-lib-jar-meetingfiles.connector",
            buildImage: "jenkins-spring-boot-gradle:1.0.0-java8-gcloud"
        ],

        "podium.ppi-lib-jar-space.utilisation":[
            serviceType: "airflow",
            serviceName: "podium.ppi-lib-jar-space.utilisation",
            buildImage: "jenkins-spring-boot-gradle:1.0.0-java8-gcloud"
        ],

        "podium.ppi-lib-jar-o365.connector":[
            serviceType: "airflow",
            serviceName: "podium.ppi-lib-jar-o365.connector",
            buildImage: "jenkins-spring-boot-gradle:1.0.0-java8-gcloud"
        ],

        "podium.ppi-lib-jar-occupancy.avg.attendance.metrics":[
            serviceType: "airflow",
            serviceName: "podium.ppi-lib-jar-occupancy.avg.attendance.metrics",
            buildImage: "jenkins-spring-boot-gradle:1.0.0-java8-gcloud"
        ],

        "podium.ppi-lib-jar-occupancy.attendance.metrics":[
            serviceType: "airflow",
            serviceName: "podium.ppi-lib-jar-occupancy.attendance.metrics",
            buildImage: "jenkins-spring-boot-gradle:1.0.0-java8-gcloud"
        ],

        "podium.ppi-lib-jar-metrics.processor":[
            serviceType: "airflow",
            serviceName: "podium.ppi-lib-jar-metrics.processor"
        ],

        "podium.lib-jar-gis.utils":[
            serviceType: "airflow",
            serviceName: "podium.lib-jar-gis.utils"
        ],

        "podium.ppi-lib-jar-workplace.sentiment.metrics":[
            serviceType: "airflow",
            serviceName: "podium.ppi-lib-jar-workplace.sentiment.metrics"
        ],

        "podium.ppi-lib-jar-simplespace.team":[
            serviceType: "airflow",
            serviceName: "podium.ppi-lib-jar-simplespace.team"
        ],

        "podium.mapping-lib-jar-geojson.processor":[
            serviceType: "airflow",
            serviceName: "podium.mapping-lib-jar-geojson.processor"
        ],

        "podium.mapping-lib-jar-gml.processor":[
            serviceType: "airflow",
            serviceName: "podium.mapping-lib-jar-gml.processor"
        ],

        "podium.mapping-lib-jar-sla.connector":[
            serviceType: "airflow",
            serviceName: "podium.mapping-lib-jar-sla.connector"
        ],

        "podium.mapping-lib-jar-uk.connector":[
            serviceType: "airflow",
            serviceName: "podium.mapping-lib-jar-uk.connector"
        ],

        "podium.mapping-lib-jar-nsw.connector":[
            serviceType: "airflow",
            serviceName: "podium.mapping-lib-jar-nsw.connector"
        ],
                    
        "podium-cc-web-exp-cloud":[
            serviceName: "podium-cc-web-exp-cloud",
            helmEnvironment: "podium-io-dev",
            namespace: "cc-service",
            serviceType: "containerDeploy",
            tenancyType: PodiumVars.cloudControllerTenancy
        ],

        "podium.csc-zip-svc.sftp": [
            serviceType: "cloudFunction",
            serviceName: "podium.csc-zip-svc.sftp"
        ],

        "javacontainer-test-service-repository":[
            serviceName: "javacontainer-test-service-repository",
            helmEnvironment: "podium-in-io-dev",
            namespace: "csc",
            serviceType: "containerDeploy",
            tenancyType: PodiumVars.sharedTenancy
        ],

        "npmcontainer-test-service-repository":[
                serviceName: "npmcontainer-test-service-repository",
                helmEnvironment: "podium-io-dev",
                namespace: "lendleaseau",
                tenancyType: PodiumVars.customerTenancy,
                serviceType: "containerDeploy"
        ]
    ]

    static def getArtifactName(serviceName){
        for(def artifactName : this.serviceParams.keySet()){
            if(this.serviceParams[artifactName].serviceName == serviceName){
                return artifactName
            }
        }

        throw new Exception("Error:ServiceConfig.getArtifactName(${serviceName}):No serviceName tied to artifactName")
    }

    static def getArtifactDefaultNamespace(artifactName, environment){
        def tenancyType = this.serviceParams[artifactName].tenancyType
        def defaultNamespace = DeploymentVars.clusterConfigs[environment].defaultNamespaces[tenancyType]

        return defaultNamespace
    }

    static def getHelmFolder(artifactName, environment){
        def tenancyType = this.serviceParams[artifactName].tenancyType
        def helmFolderPrefix = ""

        if(tenancyType == PodiumVars.customerTenancy || tenancyType == PodiumVars.cloudControllerTenancy){
            helmFolderPrefix = "podium-io"
        }
        else if (tenancyType == PodiumVars.sharedTenancy || tenancyType == PodiumVars.adaptorsTenancy){
            helmFolderPrefix = "podium-in-io"
        }
        else{
            throw new Exception("Error:ServiceConfig.getHelmFolder(${artifactName}):No tenancy type assigned matches")
        }

        return (helmFolderPrefix + "-" + environment)
    }

}

