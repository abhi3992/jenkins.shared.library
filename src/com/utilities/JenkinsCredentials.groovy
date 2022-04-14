package com.utilities

class JenkinsCredentials{

    static String gcpProjectId = 'shared-services-d042'
    static def credentials = [

        "npmrc" : [
            secretName: "jenkins-npmrc",
            version: "1"
        ],

        "sonartoken" : [
            secretName: "jenkins-sonartoken",
            version: "1"
        ],

        "csc-artifactory" : [
            usernameSecretName: "jenkins-csc-artifactory-username",
            passwordSecretName: "jenkins-csc-artifactory-password",
            version: "1"
        ],

        "jfrog-dev-team-user" : [
            usernameSecretName: "jenkins-jfrog-dev-team-user-username",
            passwordSecretName: "jenkins-jfrog-dev-team-user-password",
            version: "1"
        ],

        "masterbitbucket" : [
            usernameSecretName: "jenkins-masterbitbucket-username",
            passwordSecretName: "jenkins-masterbitbucket-password",
            version: "1"
        ],


    ]

}


