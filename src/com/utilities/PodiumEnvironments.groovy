package com.utilities

class PodiumEnvironments{
    static def teams = [
        ppi: "ppi",
        cubs: "cubs",
        envision: "envision",
        sa: "sa",
        platform: "platform"
    ]

    static def deploymentTypes = [
        containerDeploy: "containerDeploy",
        airflowDeploy: "airflowDeploy",
        cloudFunctionDeploy: "cloudFunctionDeploy",
        runArtifact: "runArtifact"
    ]

    static def cloudfunctionBucketPrefix = "podium-cloudfunction-"

    static def serviceTenancyTypes = [
        shared: "shared",
        sharedService: "sharedService"
        customer: "customer",
        adaptors: "adaptors",
        "cc-service": "cc-service"
    ]

    static def environmentGCPProjectTypes = [
        datalake: "datalake",
        podium: "podium"
    ]

    static def environments = [
        dev:[
            gcpProjectIDs: [datalake: "podium-datalake-dev-38a8", podium: "podium-app-dev-2"],
            serviceClusters: [                
                gcpEnvironment: this.environmentGCPProjectTypes.podium
                name: "podium-cluster-dev",
                gcpProjectId: "podium-app-dev-2",
                location: "australia-southeast1",
                clusterTenancies: [
                    dev: [shared: "csc", customer: "lendleaseau", adaptors: "adaptors", sharedService: "shared-service", "cc-service": "cc-service"],
                    "dev-spacex": [shared: "spacex-multii", customer: "spacex", adaptors: "adaptors", sharedService: "shared-service", "cc-service": "cc-service"]
                ]                
            ],
            composer: [
                gcpEnvironment: this.environmentGCPProjectTypes.datalake
                name: "podium-aus-southeast1-datalake-dev-airflow-cluster-2",
                gcpProjectId: "podium-datalake-dev-38a8",
                airflowFilesBucket: "airflow-files-dev"
            ]
        ],

        qa:[
            gcpProjectIDs: [datalake: "podium-datalake-qa-ae97", podium: "podium-app-qa-9683"],
            servicesCluster: [
                name: "podium-cluster-qa",
                gcpProjectId: "podium-app-qa-9683",
                location: "australia-southeast1",
                clusterTenancies: [
                    qa: [shared: "csc", customer: "lendleaseau", adaptors: "adaptors", sharedService: "shared-service", "cc-service": "cc-service"],
                    "qa-sandbox": [shared: "sandbox-glb", customer: "sandbox-env", adaptors: "adaptors", sharedService: "shared-service", "cc-service": "cc-service"]
                ]
            ],
            composer: [
                name: "pdp-australia-southeast1-qa-airflow-cluster",
                gcpProjectId: "podium-datalake-qa-ae97",
                airflowFilesBucket: "airflow-files-qa"
            ]
        ],

        uat:[
            gcpProjectIDs: [datalake: "podium-datalake-uat-8872", podium: "podium-app-uat-fe4d"],
            serviceCluster: [
                name: "podium-cluster-uat",
                gcpProjectId: "podium-app-uat-fe4d",
                location: "australia-southeast1",
                clusterTenancies: [
                    uat: [shared: "csc", customer: "lendleaseau", adaptors: "adaptors", sharedService: "shared-service", "cc-service": "cc-service"]
                ]
            ],
            composer: [
                name: "pdp-australia-southeast2-uat-airflow-cluster",
                gcpProjectId: "podium-datalake-uat-8872",
                airflowFilesBucket: "airflow-files-uat"
            ]
        ],

        prod:[
            gcpProjectIDs: [datalake: "podium-datalake-prod-9c5a", podium: "podium-app-prod"],
            serviceCluster: [
                name: "podium-cluster-prod",
                gcpProjectId: "podium-app-prod",
                location: "australia-southeast1",
                clusterTenancies: [
                    prod: [shared: "csc", customer: "lendleaseau", adaptors: "adaptors", sharedService: "shared-service", "cc-service": "cc-service"]
                ]
            ],
            composer: [
                name: "pdp-australia-southeast2-prod-airflow-cluster",
                gcpProjectId: "podium-datalake-prod-9c5a",
                airflowFilesBucket: "airflow-files-qa"
            ]
        ]
    ]
}