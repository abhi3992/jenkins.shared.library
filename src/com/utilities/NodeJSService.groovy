package com.utilities

class NodeJSService{

    static def build(script,String packageManager){
        if(packageManager=='npm'){
            script.sh(script: ''' 
                npm install
                ls -la
                npm run build:prod
            ''')
        }
        else if(packageManager=='yarn'){
            script.sh(script: ''' 
                yarn install
                ls -la
                yarn build:prod
            ''')
        }
    }
}