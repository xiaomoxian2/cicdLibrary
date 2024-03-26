package org.devops

//checkout 
def CheckOut(){
    println("CheckOut")

    checkout([$class: 'GitSCM', 
        branches: [[name: "${env.branchName}"]], 
        extensions: [], 
        userRemoteConfigs: [[credentialsId: '', url: "${env.srcUrl}"]]])

    sh "ls -l" //验证
}

//run build
def Build(){
    println("Build")

    sh "${env.buildShell}"
}
