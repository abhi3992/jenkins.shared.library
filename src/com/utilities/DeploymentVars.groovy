package com.utilities

class DeploymentVars{

    static def deploymentConfig = [
        GCP_PROJECT_ID: "podium-app-dev-2",
        GCP_LOCATION: "australia-southeast1",
        GKE_CLUSTER_NAME: "podium-cluster-dev",
        BLOCK_DEPLOYMENT: false
    ]

    static def airflowConfig = [

        airflowFilesBucketPrefix: "airflow-files-",
        dev: [
            datalakeProjectId: "podium-datalake-dev-38a8",
            gcpProjectId: "podium-app-dev-2",
            gcpComposerName: "podium-aus-southeast1-datalake-dev-airflow-cluster-2"
        ],

        qa: [
            datalakeProjectId: "podium-datalake-qa-ae97",
            gcpProjectId: "podium-app-qa-9683",
            gcpComposerName: "pdp-australia-southeast1-qa-airflow-cluster"
        ],

        uat: [
            datalakeProjectId: "podium-datalake-uat-8872",
            gcpProjectId: "podium-app-uat-fe4d",
            gcpComposerName: "pdp-australia-southeast2-uat-airflow-cluster"
        ],

        prod: [
            datalakeProjectId: "podium-datalake-prod-9c5a",
            gcpProjectId: "podium-app-prod",
            gcpComposerName: "pdp-australia-southeast2-prod-airflow-cluster"
        ]
    ]

    static def clusterConfigs = [

        dev: [
            gcpProjectId: "podium-app-dev-2",
            gcpLocation: "australia-southeast1",
            gkeClusterName: "podium-cluster-dev",
            blockDeployment: false,
            dockerRepo: "docker",
            defaultNamespaces:[
                shared: "csc",
                customer: "lendleaseau",
                adaptors: "adaptors",
                "cc-service": "cc-service"
            ]
        ],

        "dev-spacex": [
            gcpProjectId: "podium-app-dev-2",
            gcpLocation: "australia-southeast1",
            gkeClusterName: "podium-cluster-dev",
            blockDeployment: false,
            dockerRepo: "docker",
            defaultNamespaces:[
                shared: "spacex-multii",
                customer: "spacex",
                adaptors: "adaptors",
                "cc-service": "cc-service"
            ]
        ],

        qa: [
            gcpProjectId: "podium-app-qa-9683",
            gcpLocation: "australia-southeast1",
            gkeClusterName: "podium-cluster-qa",
            blockDeployment: false,
            dockerRepo: "docker-qa",
            defaultNamespaces:[
                shared: "csc",
                customer: "lendleaseau",
                adaptors: "adaptors",
                "cc-service": "cc-service"  
            ]
        ],

        "qa-sandbox": [
            gcpProjectId: "podium-app-qa-9683",
            gcpLocation: "australia-southeast1",
            gkeClusterName: "podium-cluster-qa",
            blockDeployment: false,
            dockerRepo: "docker-qa",
            defaultNamespaces:[
                shared: "sandbox-glb",
                customer: "sandbox-env",
                adaptors: "adaptors",
                "cc-service": "cc-service"
            ]
        ],

        uat: [
            gcpProjectId: "podium-app-uat-fe4d",
            gcpLocation: "australia-southeast1",
            gkeClusterName: "podium-cluster-uat",
            blockDeployment: false,
            dockerRepo: "docker-uat",
            defaultNamespaces:[
                shared: "csc",
                customer: "lendleaseau",
                adaptors: "adaptors",
                "cc-service": "cc-service"
            ]
        ],

        prod: [
            gcpProjectId: "podium-app-prod",
            gcpLocation: "australia-southeast1",
            gkeClusterName: "podium-cluster-prod",
            blockDeployment: false,
            dockerRepo: "docker-prod",
            defaultNamespaces:[
                shared: "csc",
                customer: "lendleaseau",
                adaptors: "adaptors",
                "cc-service": "cc-service"
            ]
        ],

        sharedServices: [
            gcpProjectId: "shared-services-d042",
            gcpLocation: "australia-southeast1-a",
            gkeClusterName: "pdp-shared-svc-gke",
            blockDeployment: false,
            dockerRepo: "docker"
        ]
    ]

    static def helmUpdate(script, service, chartPath, namespace, helmValuesPath, helmSecretPath, imageTag, isDryRun){
        def prerunFlags = ""
        if(isDryRun){
            prerunFlags = "--dry-run --debug"
        }

        script.sh(script: """
            helm upgrade --install ${service}-app ${chartPath} --namespace ${namespace} -f ${helmValuesPath} -f ${helmSecretPath} --set image.tag=${imageTag} --set namespace=${namespace} --timeout 30m0s ${prerunFlags}
        """)
    }

}
