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

def GetCode(srcUrl, branchName){
    checkout([$class: 'GitSCM', 
    branches: [[name: branchName]], 
    extensions: [], 
    userRemoteConfigs: [[credentialsId: '54df701e-d369-4f69-85c6-7209d45c283b', 
                                url: srcUrl]]])
}

def MavenBuild(){
    sh "/usr/local/apache-maven-3.9.2/bin/mvn clean package"
}

def GradleBuild(){
    sh "/usr/local/gradle-7.4.2/bin/gradle build"
}

def NpmBuild(){
    sh "/usr/bin/npm install && /usr/bin/npm run build"
}

def YarnBuild(){
    sh "yarn"
}

def CodeBuild(type){
    switch(type){
        case "maven":
            MavenBuild()
            break;
        case "gradle":
            GradleBuild()
            break;
        case "npm":
            NpmBuild()
            break;
        case "yarn":
            YarnBuild()
            break;
        default:
            error "No such tools ... [maven/ant/gradle/npm/yarn/go]"
            break
    }
}

