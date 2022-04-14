package com.utilities
import jenkins.model.Jenkins


class GitServices {
    static def getCommitterEmail(script, String commitHash, String gitfolderpath) {
        script.sh(script: "cd ${gitfolderpath}")
        String commiter_email = script.sh(script: "git --no-pager show -s --format='%ae' ${commitHash}", returnStdout: true).trim()
        
        return commiter_email
    }

    static def getLatestCommitHash(script, gitfolderpath){
        script.sh(script: "cd ${gitfolderpath}")
        String latest_commit_hash = script.sh(script: "git log --pretty=format:'%h' -n 1", returnStdout: true).trim()

        return latest_commit_hash
    }

}

