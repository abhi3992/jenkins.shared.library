package com.utilities

class GlobalVars{

    //build and artifact deletion specs
    static String defaultMaxDays = "730"
    static String isDeleteArtifacts = "false"
    static String jenkinsRegexDeleteDays = "1"
    static String featureRegexDeleteDays = "14"
    static String devRegexDeleteDays = "60"
    static String packageReleaseRegexDeleteDays = "90"

    //comman branch regex
    static String jenkinsRegex = /(jenkinsfile.*|sharedlib.*)/
    static String featureRegex = "(feature/.*|bugfix/.*)"
    static String devRegex = /(development|PSAX-4240.*|enhancement-ct-rollback.*|.*PDSO-630.*|jenkinsfile.*|.*PDSO-626.*)/
    static String qcStagesRegex = /(release*|development|PR.*)/
    static String gitTagRegex = /(master|main)/

    //variables for service containers
    static String buildPackageRegex = /(release.*|development.*|jenkinsfile.*||PR.*|.*PDSO-626.*)/
    static String uatRegex = /(release.*|hotfix.*)/
    static String deploymentBranchRegex = /(release.*|development.*)/

    //variables for dependencies/libraries/packages
    static String buildDepsRegex = /(release.*|development.*|jenkinsfile.*|master|main)/
    static String packageReleaseRegex = /(release.*|hotfix.*)/

    //variable for quality gate
    static Boolean abortPipeline = false
    static String failureStatus = "FAILURE"
    static String successStatus = "SUCCESS"
    static String failureStatusColor = "#d64223"
    static String successStatusColor = "#00ff00"

    static Boolean rollback = false
}