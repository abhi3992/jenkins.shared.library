package com.utilities

import jenkins.model.Jenkins
import com.michelin.cio.hudson.plugins.rolestrategy.RoleBasedAuthorizationStrategy
import com.michelin.cio.hudson.plugins.rolestrategy.Role

class UserAuthorization{

    static def OTHER_THAN_USER = "Multi_Branch_Scan_or_Timer"
    static def ADMIN_ROLE = "admin"

    static def environmentRolePermissions = [
        developer: ["dev","qa"],
        qa: ["dev","qa","qa-sandbox","uat"]
    ]

    static def isUserAuthorised(environment, currentBuild){
        def userid = this.getBuildUser(currentBuild)

        if(userid == this.OTHER_THAN_USER){
            return true
        }

        def role = getUserRoles(userid)[0] //need to fix this to take least privilege
        if(role == this.ADMIN_ROLE){
            return true
        }
        
        if(this.environmentRolePermissions[role].contains(environment)){
            return true
        }

        throw new Exception("${userid} is unauthorised to deploy on ${environment}")
    }

    static def getUserRoles(userid){
        def roles = []
                       
        def authorization = jenkins.model.Jenkins.instance.getAuthorizationStrategy()
        
        // RoleBasedAuthorizationStrategy.{GLOBAL, PROJECT, SLAVE, MACRO_ROLE, MACRO_USER}
        def grantedRoles = authorization.getGrantedRoles(RoleBasedAuthorizationStrategy.GLOBAL)
        
        for (Role grantedRole : grantedRoles.entrySet()) {
            if (grantedRole.getValue().contains(userid)) {
                roles.add(grantedRole.getKey().getName())
            }
        }        

        return roles
    }

    static def getBuildUser(currentBuild) {
        def build = currentBuild.rawBuild
        def BUILD_USER = ""
        def BUILD_USERID = ""
        
        try {
            def cause = build.getCause(Cause.UserIdCause)
            BUILD_USER = cause.getUserName()
            BUILD_USERID = cause.getUserId()
             
        } catch(Exception ex) {
             println "\n\n-- Build caused by either Multi-Branch Pipeline Scanning -or- Timer i.e. not directly by a logged in user\n";
             BUILD_USER = this.OTHER_THAN_USER
             BUILD_USERID = this.OTHER_THAN_USER
        }

        return BUILD_USERID
    }

    static def getUpstreamCause(currentBuild){
        def causes = currentBuild.rawBuild.getCauses()
        for (cause in causes){
            if (cause.class.toString().contains("UpstreamCause")){
                println "This job was caused by job " + cause.upstreamProject
            }
            else{
                println "Root cause : " + cause.toString()
            }
        }
    }
}