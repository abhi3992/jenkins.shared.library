package com.utilities


class GcpService {

    static def authenticateGCP(script, env, String gcsProject){
        script.withCredentials([script.file(credentialsId: 'lendlease-jenkins-seed-service-account', variable: 'GC_KEY')]) {
            script.sh"""
                gcloud auth activate-service-account --key-file=${env.GC_KEY}
                gcloud config set project ${gcsProject}
            """
        }
    }

    static def uploadArtifactsToGcs(String gcsProject, String destinationFolder, String destinationBucket, script) {
        
        script.sh("""
            gcloud config set project ${gcsProject}
            gsutil cp -r ${destinationFolder}/** gs://${destinationBucket}/ 
        """)
    }

    static def findAllArtifactsWithArtifactPrefixInBucket(String gcsProject, String searchPattern, String destinationBucket, script) {
        def artifacts
        try{
            artifacts=script.sh(script:"gcloud config set project ${gcsProject} | gsutil ls gs://${destinationBucket}/${searchPattern}", returnStdout: true).trim()
        }
        catch(Exception ex){
            ex.printStackTrace()
            return []
        }

        def list=artifacts.split("\\s+")
        return list
    }

    static def deleteFromGcs(String gcsProject, def listToDelete, script) {
        def list=listToDelete
        script.sh("""
            gcloud config set project ${gcsProject}
        """)
        for(def i=0;i<list.size(); i++){
            script.sh(script:" gsutil rm ${list[i]}")
        }
    }

    static def getSecretFromSecretManager(String gcpProjectId, String secretName, String version, script, env){
        def secret = ""
        try{
            authenticateGCP(script, env, gcpProjectId)
            secret = script.sh(script: "gcloud secrets versions access ${version} --secret='${secretName}'", returnStdout: true).trim()
        }
        catch(Exception ex){
            ex.printStackTrace()
            throw ex
        }
        return secret
    }
}