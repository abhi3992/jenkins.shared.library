package com.utilities

class SlackNotification{
    static def defaultChannel = "podium-cicd-build-alerts"    

    static def defaultTeamNotifications = [
        ppi: [
            ci: ["someguys@emai.com", "anotherguy@email.com"], 
            test:["sometest"], 
            deploy: ["someone"]
        ]
    ]
    
    static def notify(script, String buildstatus, String notificationsTo, String file_path, String customised_message) {
        def statusColor = (buildstatus == GlobalVars.successStatus) ? GlobalVars.successStatusColor : GlobalVars.failureStatusColor
        def userid = ""

        def attachments = [
            [
              "fallback": "Plain-text summary of the attachment.",
              "color": "${statusColor}",
              "pretext": "Please find the Jenkins job status here\n\n*JENKINS JOB STATUS*",
              "text": customised_message
            ]
        ]

        def channels = this.defaultChannel
        if(notificationsTo != null){
            userid =  script.slackUserIdFromEmail(email: "${notificationsTo}", botUser: true)
            channels = (userid != null) ? (channels + ", @${userid}") : channels            
        }
        
        if (file_path != null){
            script.slackUploadFile(channel: channels, filePath: file_path, initialComment: customised_message)
        }
        else{
            script.slackSend(botUser: true, 
            channel: channels,
            color: "${statusColor}", 
            attachments: attachments)
        }
    }   
}   

