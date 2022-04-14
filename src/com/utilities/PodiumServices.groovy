package com.utilities

import com.utilities.PodiumEnvironments

class PodiumServices{

    static def artifactTypes = [
        dockerImage: "dockerImage",
        jar: "jar",
        zip: "zip"
    ]
    
    static def services = [
        // container services

        [
            serviceName: "tenancy-scenario-service",
            team: PodiumEnvironments.teams.envision,
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.shared,            
            artifacts: [
                [ artifactName: "podium-env-cnt-svc-tenancy-scenario", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: [
                [
                    testType: "apiTest",
                    testRepoUrl: "git@bitbucket.org:lendlease_corp/podium.envision.api.automation.git",
                    environments: [
                        dev: ['@ScenarioServiceSmoke'], qa: ['@ScenarioServiceSmoke', '@ScenarioServiceRegression'], uat: ['@ScenarioServiceSmoke']
                    ]
                ]
            ]
        ],

        [
            serviceName: "podium-mapping-service",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.shared,            
            artifacts: [
                [ artifactName: "podium-lib-cnt-svc-mapping", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: []
        ],
        
        [
            serviceName: "podium-csc-cnt-int-storaenso",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.adaptors,            
            artifacts: [
                [ artifactName: "podium-csc-cnt-int-storaenso", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: []
        ],

        [
            serviceName: "csc-project-service",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.shared,            
            artifacts: [
                [ artifactName: "podium-csc-cnt-ent-project", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: [
                [
                    testType: "apiTest",
                    testRepoUrl: "git@bitbucket.org:lendlease_corp/podium.api.automation.karate.git",
                    environments: [
                        dev: ['@prjServSmoke'], qa: ['@prjServSmoke', '@prjServRegression'], uat: ['@prjServSmoke']
                    ]
                ]
            ]
        ],

        [
            serviceName: "csc-router-service",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.customer,            
            artifacts: [
                [ artifactName: "lib-cnt-router", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: []
        ],

        [
            serviceName: "csc-site-service",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.shared,            
            artifacts: [
                [ artifactName: "podium-csc-cnt-ent-project-site", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: [
                [
                    testType: "apiTest",
                    testRepoUrl: "git@bitbucket.org:lendlease_corp/podium.api.automation.karate.git",
                    environments: [
                        dev: ['@prjServSmoke'], qa: ['@prjServSmoke', '@prjServRegression'], uat: ['@prjServSmoke']
                    ]
                ]
            ]
        ],

        [
            serviceName: "csc-vendor-service",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.shared,            
            artifacts: [
                [ artifactName: "csc-cnt-ent-vendor", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: [
                [
                    testType: "apiTest",
                    testRepoUrl: "git@bitbucket.org:lendlease_corp/podium.api.automation.karate.git",
                    environments: [
                        dev: ['@vendorServSmoke'], qa: ['@vendorServSmoke', '@vendorServRegression'], uat: ['@vendorServSmoke']
                    ]
                ]
            ]
        ],

        [
            serviceName: "csc-workflow-service",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.shared,            
            artifacts: [
                [ artifactName: "podium-csc-cnt-svc-workflow", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: []
        ],

        [
            serviceName: "ppi-insights-engine",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.customer,            
            artifacts: [
                [ artifactName: "podium-ppi-cnt-ent-insights-engine", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: []
        ],

        [
            serviceName: "podium-cubs-cnt-svc-modelruntime",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.shared,            
            artifacts: [
                [ artifactName: "podium-cubs-cnt-svc-modelruntime", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: [
                [
                    testType: "apiTest",
                    testRepoUrl: "git@bitbucket.org:lendlease_corp/podium.cubs.api.automation.git",
                    environments: [
                        dev: ['@Regression']
                    ]
                ]
            ]
        ],

        [
            serviceName: "mail-service",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.sharedService,            
            artifacts: [
                [ artifactName: "podium-lib-cnt-svc-platform-mailservice", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: []
        ],

        [
            serviceName: "podium-tenancy-security-service",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.customer,            
            artifacts: [
                [ artifactName: "podium-tenancy-security-service", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: []
        ],

        [
            serviceName: "csc-schindler-service",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.adaptors,            
            artifacts: [
                [ artifactName: "csc-cnt-int-schindler", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: []
        ],

        [
            serviceName: "podium-csc-cnt-int-primaverap6",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.adaptors,            
            artifacts: [
                [ artifactName: "podium-csc-cnt-int-primaverap6", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: []
        ],

        [
            serviceName: "csc-delivery-service",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.shared,            
            artifacts: [
                [ artifactName: "csc-cnt-ent-project.delivery", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: [
                [
                    testType: "apiTest",
                    testRepoUrl: "git@bitbucket.org:lendlease_corp/podium.api.automation.karate.git",
                    environments: [
                        dev: ['@deliveryServSmoke'], qa: ['@deliveryServSmoke', '@deliveryServRegression'], uat: ['@deliveryServSmoke']
                    ]
                ]
            ]
        ],

        [
            serviceName: "tenancy-finance-service",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.shared,            
            artifacts: [
                [ artifactName: "podium-env-cnt-svc-tenancy-finance", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: [
                [
                    testType: "apiTest",
                    testRepoUrl: "git@bitbucket.org:lendlease_corp/podium.envision.api.automation.git",
                    environments: [
                        dev: ['@FinanceServiceSmoke'], qa: ['@FinanceServiceSmoke', '@FinanceServiceRegression'], uat: ['@FinanceServiceSmoke']
                    ]
                ]
            ]
        ],

        [
            serviceName: "podium-lib-cnt-ent-documentcontrol",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.shared,            
            artifacts: [
                [ artifactName: "podium-lib-cnt-ent-documentcontrol", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: [
                [
                    testType: "apiTest",
                    testRepoUrl: "git@bitbucket.org:lendlease_corp/podium.api.automation.karate.git",
                    environments: [
                        dev: ['@docCntrlSmoke'], qa: ['@docCntrlSmoke', '@docCntrlRegression'], uat: ['@docCntrlSmoke']
                    ]
                ]
            ]
        ],

        [
            serviceName: "csc-schedule-service",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.shared,            
            artifacts: [
                [ artifactName: "csc-cnt-ent-project-schedule", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: [
                [
                    testType: "apiTest",
                    testRepoUrl: "git@bitbucket.org:lendlease_corp/podium.api.automation.karate.git",
                    environments: [
                        dev: ['@prjActvitySmoke'], qa: ['@prjActvitySmoke', '@prjActivityRegression'], uat: ['@prjActvitySmoke']
                    ]
                ]
            ]
        ],

        [
            serviceName: "test-demoentity-service",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.shared,            
            artifacts: [
                [ artifactName: "podium-test-cnt-ent-example-entityservice", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: []
        ],

        [
            serviceName: "reference-standards-service",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.shared,            
            artifacts: [
                [ artifactName: "podium-lib-cnt-svc-reference-standards", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: [
                [
                    testType: "apiTest",
                    testRepoUrl: "git@bitbucket.org:lendlease_corp/podium.envision.api.automation.git",
                    environments: [
                        dev: ['@RefrenceDataServiceSmoke'], qa: ['@RefrenceDataServiceSmoke', '@RefrenceDataServiceRegression'], uat: ['@RefrenceDataServiceSmoke']
                    ]
                ]
            ]
        ],

        
        [
            serviceName: "ppi-asset-service",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.shared,            
            artifacts: [
                [ artifactName: "podium.ppi-cnt-ent-asset.commissioning", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: []
        ],

        [
            serviceName: "podium-env-web-exp-envision",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.customer,            
            artifacts: [
                [ artifactName: "podium-env-web-exp-envision", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: []
        ],

        [
            serviceName: "podium-lib-npm-ui",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.customer,            
            artifacts: [
                [ artifactName: "podium-lib-npm-ui", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: []
        ],

        [
            serviceName: "podium-lib-npm-ui-storybook",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.customer,            
            artifacts: [
                [ artifactName: "podium-lib-npm-ui-storybook", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: []
        ],

        [
            serviceName: "podium-ui-framework",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.customer,            
            artifacts: [
                [ artifactName: "podium.lib-cnt-app-podium", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: []
        ],

        [
            serviceName: "csc-projectapppref",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.customer,            
            artifacts: [
                [ artifactName: "podium-lib-cnt-svc-tenancy-projectapppref", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: [
                [
                    testType: "apiTest",
                    testRepoUrl: "git@bitbucket.org:lendlease_corp/podium.api.automation.karate.git",
                    environments: [
                        dev: ['@projPrefSmoke'], qa: ['@projPrefSmoke'], uat: ['@projPrefSmoke']
                    ]
                ]
            ]
        ],

        [
            serviceName: "podium-ui-framework",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.customer,            
            artifacts: [
                [ artifactName: "podium.ppi-cnt-ent-insights", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: []
        ],

        [
            serviceName: "podium-lib-npm-uiframework",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.customer,            
            artifacts: [
                [ artifactName: "podium-lib-npm-uiframework", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: []
        ],

        [
            serviceName: "podium-csc-npm-exp-order-tracking",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.customer,            
            artifacts: [
                [ artifactName: "podium-csc-npm-exp-order-tracking", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: []
        ],

        [
            serviceName: "podium-ppi-npm-exp-insights",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.customer,            
            artifacts: [
                [ artifactName: "podium-ppi-npm-exp-insights", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: [
                [
                    testType: "UiTest",
                    testRepoUrl: "git@bitbucket.org:lendlease_corp/podium-ppi-ui-automation.git",
                    environments: [
                        dev: ['@PPIUISmoke'], qa: ['@PPIUISmoke']
                    ]
                ]
            ]
        ],

        [
            serviceName: "podium-hma-npm-exp-home",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.customer,            
            artifacts: [
                [ artifactName: "podium-hma-npm-exp-home", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: []
        ],

        [
            serviceName: "podium-wkp-npm-exp-workplace",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.customer,            
            artifacts: [
                [ artifactName: "podium-wkp-npm-exp-workplace", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: []
        ],

        [
            serviceName: "workspace-service",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.customer,            
            artifacts: [
                [ artifactName: "podium-ppi-cnt-ent-workspace-service", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: []
        ],

        [
            serviceName: "ppi-metrics-service",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.shared,            
            artifacts: [
                [ artifactName: "podium.ppi-cnt-ent-metrics", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: [
                [
                    testType: "apiTest",
                    testRepoUrl: "git@bitbucket.org:lendlease_corp/podium-ppi-api-automation.git",
                    environments: [
                        dev: ['@ppiSmoke'], qa: ['@ppiSmoke', '@ppiRegression']
                    ]
                ]
            ]
        ],

        [
            serviceName: "podium-cubs-cnt-svc-modelruntime-async",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.customer,            
            artifacts: [
                [ artifactName: "podium-cubs-cnt-svc-modelruntime-async", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: []
        ],

        [
            serviceName: "javacontainer-test-service-repository",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.customer,            
            artifacts: [
                [ artifactName: "javacontainer-test-service-repository", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: []
        ],

        [
            serviceName: "npmcontainer-test-service-repository",
            deploymentTypes: [[deploymentType: PodiumEnvironments.deploymentTypes.containerDeploy]],
            serviceTenancy: PodiumEnvironments.serviceTenancyTypes.customer,            
            artifacts: [
                [ artifactName: "npmcontainer-test-service-repository", artifactType: this.artifactTypes.dockerImage ]
            ],
            tests: []
        ],

        // airflow services

        [
            serviceName: "podium.lib-jar-shell.connectors",
            deploymentTypes: [
                [deploymentType: PodiumEnvironments.deploymentTypes.runArtifact, deploymentVars: [runEnvironmentImage: "jenkins-spring-boot-gradle:1.0.0-java8-gcloud"]],
                [deploymentType: PodiumEnvironments.deploymentTypes.airflowDeploy]
            ],       
            artifacts: [
                [ artifactName: "podium.lib-jar-shell.connectors", artifactType: this.artifactTypes.jar ]
            ],
            tests: []
        ],

        [
            serviceName: "podium.lib-jar-weather.connector",
            deploymentTypes: [
                [deploymentType: PodiumEnvironments.deploymentTypes.runArtifact, deploymentVars: [runEnvironmentImage: "jenkins-spring-boot-gradle:1.0.0-java8-gcloud"]],
                [deploymentType: PodiumEnvironments.deploymentTypes.airflowDeploy]
            ],       
            artifacts: [
                [ artifactName: "podium.lib-jar-weather.connector", artifactType: this.artifactTypes.jar ]
            ],
            tests: []
        ],

        [
            serviceName: "podium.ppi-lib-jar-workplace.acnsentiment.metrics",
            deploymentTypes: [
                [deploymentType: PodiumEnvironments.deploymentTypes.runArtifact, deploymentVars: [runEnvironmentImage: "jenkins-spring-boot-gradle:1.0.0-java8-gcloud"]],
                [deploymentType: PodiumEnvironments.deploymentTypes.airflowDeploy]
            ],       
            artifacts: [
                [ artifactName: "podium.ppi-lib-jar-workplace.acnsentiment.metrics", artifactType: this.artifactTypes.jar ]
            ],
            tests: []
        ],

        [
            serviceName: "podium.lib-jar-metrics.uploader",
            deploymentTypes: [
                [deploymentType: PodiumEnvironments.deploymentTypes.runArtifact, deploymentVars: [runEnvironmentImage: "jenkins-spring-boot-gradle:1.0.0-java8-gcloud"]],
                [deploymentType: PodiumEnvironments.deploymentTypes.airflowDeploy]
            ],       
            artifacts: [
                [ artifactName: "podium.lib-jar-metrics.uploader", artifactType: this.artifactTypes.jar ]
            ],
            tests: []
        ],

        [
            serviceName: "podium.ppi-cim-connectors",
            deploymentTypes: [
                [deploymentType: PodiumEnvironments.deploymentTypes.runArtifact, deploymentVars: [runEnvironmentImage: "jenkins-spring-boot-gradle:1.0.0-java8-gcloud"]],
                [deploymentType: PodiumEnvironments.deploymentTypes.airflowDeploy]
            ],       
            artifacts: [
                [ artifactName: "podium.ppi-cim-connectors", artifactType: this.artifactTypes.jar ]
            ],
            tests: []
        ],

        [
            serviceName: "podium.ppi-lib-jar-cardaccess.connector",
            deploymentTypes: [
                [deploymentType: PodiumEnvironments.deploymentTypes.runArtifact, deploymentVars: [runEnvironmentImage: "jenkins-spring-boot-gradle:1.0.0-java8-gcloud"]],
                [deploymentType: PodiumEnvironments.deploymentTypes.airflowDeploy]
            ],       
            artifacts: [
                [ artifactName: "podium.ppi-lib-jar-cardaccess.connector", artifactType: this.artifactTypes.jar ]
            ],
            tests: []
        ],

        [
            serviceName: "podium.ppi-lenel-adaptor",
            deploymentTypes: [
                [deploymentType: PodiumEnvironments.deploymentTypes.runArtifact, deploymentVars: [runEnvironmentImage: "jenkins-spring-boot-gradle:1.0.0-java8-gcloud"]],
                [deploymentType: PodiumEnvironments.deploymentTypes.airflowDeploy]
            ],       
            artifacts: [
                [ artifactName: "podium.ppi-lenel-adaptor", artifactType: this.artifactTypes.jar ]
            ],
            tests: []
        ],

        [
            serviceName: "podium.ppi-lib-jar-meetingfiles.connector",
            deploymentTypes: [
                [deploymentType: PodiumEnvironments.deploymentTypes.runArtifact, deploymentVars: [runEnvironmentImage: "jenkins-spring-boot-gradle:1.0.0-java8-gcloud"]],
                [deploymentType: PodiumEnvironments.deploymentTypes.airflowDeploy]
            ],       
            artifacts: [
                [ artifactName: "podium.ppi-lib-jar-meetingfiles.connector", artifactType: this.artifactTypes.jar ]
            ],
            tests: []
        ],

        [
            serviceName: "podium.ppi-lib-jar-meetingfiles.connector",
            deploymentTypes: [
                [deploymentType: PodiumEnvironments.deploymentTypes.runArtifact, deploymentVars: [runEnvironmentImage: "jenkins-spring-boot-gradle:1.0.0-java8-gcloud"]],
                [deploymentType: PodiumEnvironments.deploymentTypes.airflowDeploy]
            ],       
            artifacts: [
                [ artifactName: "podium.ppi-lib-jar-meetingfiles.connector", artifactType: this.artifactTypes.jar ]
            ],
            tests: []
        ],

        [
            serviceName: "podium.ppi-lib-jar-space.utilisation",
            deploymentTypes: [
                [deploymentType: PodiumEnvironments.deploymentTypes.runArtifact, deploymentVars: [runEnvironmentImage: "jenkins-spring-boot-gradle:1.0.0-java8-gcloud"]],
                [deploymentType: PodiumEnvironments.deploymentTypes.airflowDeploy]
            ],       
            artifacts: [
                [ artifactName: "podium.ppi-lib-jar-space.utilisation", artifactType: this.artifactTypes.jar ]
            ],
            tests: []
        ],

        [
            serviceName: "podium.ppi-lib-jar-o365.connector",
            deploymentTypes: [
                [deploymentType: PodiumEnvironments.deploymentTypes.runArtifact, deploymentVars: [runEnvironmentImage: "jenkins-spring-boot-gradle:1.0.0-java8-gcloud"]],
                [deploymentType: PodiumEnvironments.deploymentTypes.airflowDeploy]
            ],       
            artifacts: [
                [ artifactName: "podium.ppi-lib-jar-o365.connector", artifactType: this.artifactTypes.jar ]
            ],
            tests: []
        ],

        [
            serviceName: "podium.ppi-lib-jar-occupancy.avg.attendance.metrics",
            deploymentTypes: [
                [deploymentType: PodiumEnvironments.deploymentTypes.runArtifact, deploymentVars: [runEnvironmentImage: "jenkins-spring-boot-gradle:1.0.0-java8-gcloud"]],
                [deploymentType: PodiumEnvironments.deploymentTypes.airflowDeploy]
            ],       
            artifacts: [
                [ artifactName: "podium.ppi-lib-jar-occupancy.avg.attendance.metrics", artifactType: this.artifactTypes.jar ]
            ],
            tests: []
        ],

        [
            serviceName: "podium.ppi-lib-jar-occupancy.attendance.metrics",
            deploymentTypes: [
                [deploymentType: PodiumEnvironments.deploymentTypes.runArtifact, deploymentVars: [runEnvironmentImage: "jenkins-spring-boot-gradle:1.0.0-java8-gcloud"]],
                [deploymentType: PodiumEnvironments.deploymentTypes.airflowDeploy]
            ],       
            artifacts: [
                [ artifactName: "podium.ppi-lib-jar-occupancy.attendance.metrics", artifactType: this.artifactTypes.jar ]
            ],
            tests: []
        ],

        [
            serviceName: "podium.ppi-lib-jar-metrics.processor",
            deploymentTypes: [
                [deploymentType: PodiumEnvironments.deploymentTypes.airflowDeploy], 
            ],       
            artifacts: [
                [ artifactName: "podium.ppi-lib-jar-metrics.processor", artifactType: this.artifactTypes.jar ]
            ],
            tests: []
        ],

        [
            serviceName: "podium.lib-jar-gis.utils",
            deploymentTypes: [
                [deploymentType: PodiumEnvironments.deploymentTypes.airflowDeploy], 
            ],       
            artifacts: [
                [ artifactName: "podium.lib-jar-gis.utils", artifactType: this.artifactTypes.jar ]
            ],
            tests: []
        ],

        [
            serviceName: "podium.ppi-lib-jar-workplace.sentiment.metrics",
            deploymentTypes: [
                [deploymentType: PodiumEnvironments.deploymentTypes.airflowDeploy], 
            ],       
            artifacts: [
                [ artifactName: "podium.ppi-lib-jar-workplace.sentiment.metrics", artifactType: this.artifactTypes.jar ]
            ],
            tests: []
        ],

        [
            serviceName: "podium.ppi-lib-jar-simplespace.team",
            deploymentTypes: [
                [deploymentType: PodiumEnvironments.deploymentTypes.airflowDeploy], 
            ],       
            artifacts: [
                [ artifactName: "podium.ppi-lib-jar-simplespace.team", artifactType: this.artifactTypes.jar ]
            ],
            tests: []
        ],

        [
            serviceName: "podium.mapping-lib-jar-geojson.processor",
            deploymentTypes: [
                [deploymentType: PodiumEnvironments.deploymentTypes.airflowDeploy], 
            ],       
            artifacts: [
                [ artifactName: "podium.mapping-lib-jar-geojson.processor", artifactType: this.artifactTypes.jar ]
            ],
            tests: []
        ],

        [
            serviceName: "podium.mapping-lib-jar-gml.processor",
            deploymentTypes: [
                [deploymentType: PodiumEnvironments.deploymentTypes.airflowDeploy], 
            ],       
            artifacts: [
                [ artifactName: "podium.mapping-lib-jar-gml.processor", artifactType: this.artifactTypes.jar ]
            ],
            tests: []
        ],

        [
            serviceName: "podium.mapping-lib-jar-sla.connector",
            deploymentTypes: [
                [deploymentType: PodiumEnvironments.deploymentTypes.airflowDeploy], 
            ],       
            artifacts: [
                [ artifactName: "podium.mapping-lib-jar-sla.connector", artifactType: this.artifactTypes.jar ]
            ],
            tests: []
        ],

        [
            serviceName: "podium.mapping-lib-jar-uk.connector",
            deploymentTypes: [
                [deploymentType: PodiumEnvironments.deploymentTypes.airflowDeploy], 
            ],       
            artifacts: [
                [ artifactName: "podium.mapping-lib-jar-uk.connector", artifactType: this.artifactTypes.jar ]
            ],
            tests: []
        ],

        [
            serviceName: "podium.mapping-lib-jar-nsw.connector",
            deploymentTypes: [
                [deploymentType: PodiumEnvironments.deploymentTypes.airflowDeploy], 
            ],       
            artifacts: [
                [ artifactName: "podium.mapping-lib-jar-nsw.connector", artifactType: this.artifactTypes.jar ]
            ],
            tests: []
        ],

        [
            serviceName: "podium.mapping-lib-jar-nsw.connector",
            deploymentTypes: [
                [deploymentType: PodiumEnvironments.deploymentTypes.airflowDeploy], 
            ],       
            artifacts: [
                [ artifactName: "podium.mapping-lib-jar-nsw.connector", artifactType: this.artifactTypes.jar ]
            ],
            tests: []
        ],

        // cloud function services

        [
            serviceName: "podium.csc-zip-svc.sftp",         
            gcpProjectType: PodiumEnvironments.environmentGCPProjectTypes.podium,   
            deploymentTypes: [
                [deploymentType: PodiumEnvironments.deploymentTypes.cloudFunctionDeploy], 
            ],       
            artifacts: [
                [ artifactName: "podium.csc-zip-svc.sftp", artifactType: this.artifactTypes.zip ]
            ],
            tests: []
        ],

        [
            serviceName: "podium-vendor-gsc-handler",
            gcpProjectType: PodiumEnvironments.environmentGCPProjectTypes.datalake,
            deploymentTypes: [
                [deploymentType: PodiumEnvironments.deploymentTypes.cloudFunctionDeploy], 
            ],       
            artifacts: [
                [ artifactName: "podium.lib-jar-gcs.handlers", artifactType: this.artifactTypes.jar ]
            ],
            tests: []
        ],

        [
            serviceName: "acn-data-gcs-handler",
            gcpProjectType: PodiumEnvironments.environmentGCPProjectTypes.datalake,
            deploymentTypes: [
                [deploymentType: PodiumEnvironments.deploymentTypes.cloudFunctionDeploy], 
            ],       
            artifacts: [
                [ artifactName: "podium.lib-jar-gcs.handlers", artifactType: this.artifactTypes.jar ]
            ],
            tests: []
        ],

        [
            serviceName: "podium-acn-ems-gsc-handler",
            gcpProjectType: PodiumEnvironments.environmentGCPProjectTypes.datalake,
            deploymentTypes: [
                [deploymentType: PodiumEnvironments.deploymentTypes.cloudFunctionDeploy], 
            ],       
            artifacts: [
                [ artifactName: "podium.lib-jar-gcs.handlers", artifactType: this.artifactTypes.jar ]
            ],
            tests: []
        ]
    ]
}