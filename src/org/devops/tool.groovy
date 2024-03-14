package org.devops

// 格式化输出
def PrintMes(value,color){
    colors = ['red'   : "\033[94;31m ${value} \033[1m",
              'blue'  : "\033[94;34m ${value} \033[1m",
              'green' : "\033[94;32m ${value} \033[1m" ]
    ansiColor('xterm') {
        println(colors[color])
    }
}

// 参数化构建
def Build(buildType,buildShell){
    def buildTools = ["mvn": "maven-3.6", "ant": "ANT", "gradle": "GRADLE", "npm": "node-v16", "java": "jdk-11", "go": "go-1.17", "pnpm": "node-v16"]
    
    PrintMes("当前选择的构建类型为 ${buildType}","blue")
    buildHome= tool buildTools[buildType]
    
    if ("${buildType}" =~ "npm|pnpm"){
        sh  """ 
            export NODE_HOME=${buildHome} 
            export PATH=\$NODE_HOME/bin:\$PATH 
            ${buildHome}/bin/${buildType} ${buildShell}
        """
    } else if ("${buildType}" == "go") {
        sh  """ 
            export GO_HOME=${buildHome} 
            export PATH=\$GO_HOME/bin:\$PATH 
            ${buildHome}/bin/${buildType} ${buildShell}
        """
    } else {
        sh "${buildHome}/bin/${buildType}  ${buildShell}"
    }
}

// 构建上传docker镜像

def BuildDocker(imageRepoUrl,branchName,commitId,dockerfilePath){
    switch(dockerfilePath) {
        case "saas-base":
            if  ( "${branchName}" == "jdk-ubuntu" ) {
                imageUrl = "${imageRepoUrl}:soc-jdk11-ubuntu"
                sh """
                    DOCKER_BUILDKIT=1 docker build --load  -t ${imageUrl} ${dockerfilePath}
                    docker tag ${imageUrl} eclipse-temurin:soc-jdk11-ubuntu
                    docker push ${imageUrl}
                """
            } else {
                imageUrl = "${imageRepoUrl}:soc-jdk11"
                sh """
                    DOCKER_BUILDKIT=1 docker build --load  -t ${imageUrl} ${dockerfilePath}
                    docker tag ${imageUrl} eclipse-temurin:soc-jdk11
                    docker push ${imageUrl}
                """
            }
            PrintMes("Push ${imageUrl}镜像","blue")
            return imageUrl
        
        case "soc-upgrade-cfg/soc-tool/Dockerfile":
        case "soc-upgrade-cfg/soc-event":
            imageUrl = "${imageRepoUrl}:${branchName}"
            sh """
                DOCKER_BUILDKIT=1 docker build --load  -t ${imageUrl} ${dockerfilePath}
                docker push ${imageUrl}
            """
            PrintMes("Push ${imageUrl}镜像","blue")
            return imageUrl
        default:
            imageUrl = "${imageRepoUrl}:${branchName}-${commitId}"
            sh """
                DOCKER_BUILDKIT=1 docker build --load  -t ${imageUrl} ${dockerfilePath}
                docker push ${imageUrl}
            """
            PrintMes("Push ${imageUrl}镜像","blue")
            return imageUrl
    }
}

def CloneCode(codeDir,codeUrl,codeBranch,mode){
    sh "rm -rf ${codeDir} && git clone ${codeUrl} && cd ${codeDir} && git checkout ${codeBranch} && git pull origin ${codeBranch}"

    switch(mode) {
        case "soc-tool":
            branchNameTools = sh(returnStdout: true, script: "echo ${codeBranch} | tr '/' '_'").trim()
            return branchNameTools

        case "upgrade-daemon":
            commitIdUdaemon = sh(returnStdout: true, script: "cd ${codeDir} && git rev-parse --short HEAD").trim()
            sh "cd ${codeDir} && mvn -Dmaven.test.skip=true clean package -Prelease -U -T6"
            return commitIdUdaemon

        case "upgrade-webui":
            commitIdUwebui = sh(returnStdout: true, script: "cd ${codeDir} && git rev-parse --short HEAD").trim()
            npmParameter = "--unsafe-perm=true --registry=https://ci.das-security.cn/repository/ah_npm "
            sh "cd ${codeDir} && pnpm ${npmParameter} install && pnpm run build:normal"
            return commitIdUwebui

        case "upgrade-saas":
            commitIdUsaas = sh(returnStdout: true, script: "cd ${codeDir} && git rev-parse --short HEAD").trim()
            return commitIdUsaas

        case "upgrade-k8syaml":
            commitIdUk8syaml = sh(returnStdout: true, script: "cd ${codeDir} && git rev-parse --short HEAD").trim()
            return commitIdUk8syaml

        case "install-daemon":
            sh "cd ${codeDir} && mvn -Dmaven.test.skip=true clean package -Prelease -U -T6"
            break

        case "install-webui":
            branchNameIwebui = sh(returnStdout: true, script: "echo ${codeBranch} | tr '/' '_'").trim()
            commitIdIwebui = sh(returnStdout: true, script: "cd ${codeDir} && git rev-parse --short HEAD").trim()
            webuiImage = "saas-web-ui:${branchNameIwebui}-${commitIdIwebui}"
            npmParameter = "--unsafe-perm=true --registry=https://ci.das-security.cn/repository/ah_npm "
            sh """
                cd ${codeDir} && pnpm ${npmParameter} install && pnpm run build:normal
            """
            sh "DOCKER_BUILDKIT=1 docker build --load  --build-arg 'commitId=${commitIdIwebui}' -t '${webuiImage}' ${codeDir}"
            return webuiImage

        case "install-kafkamanager":
            branchNameIkafkamanager = sh(returnStdout: true, script: "echo ${codeBranch} | tr '/' '_'").trim()
            commitIdIkafkamanager = sh(returnStdout: true, script: "cd ${codeDir} && git rev-parse --short HEAD").trim()
            kafkaManagerImage = "monitor-kafka-manager:${branchNameIkafkamanager}-${commitIdIkafkamanager}"
            sh "DOCKER_BUILDKIT=1 docker build --load  -t ${kafkaManagerImage} ${codeDir}"
            return kafkaManagerImage

        case "install-sentinel":
            branchNameIsentinel = sh(returnStdout: true, script: "echo ${codeBranch} | tr '/' '_'").trim()
            commitIdIsentinel = sh(returnStdout: true, script: "cd ${codeDir} && git rev-parse --short HEAD").trim()
            sentinelImage = "sentinel-dashboard:${branchNameIsentinel}-${commitIdIsentinel}"
            sh "DOCKER_BUILDKIT=1 docker build --load  -t ${sentinelImage} ${codeDir}"
            return sentinelImage

        case "install-ckftp":
            branchNameIckftp = sh(returnStdout: true, script: "echo ${codeBranch} | tr '/' '_'").trim()
            commitIdIckftp = sh(returnStdout: true, script: "cd ${codeDir} && git rev-parse --short HEAD").trim()
            ckftpImage = "ck-ftp:${branchNameIckftp}-${commitIdIckftp}"
            sh "DOCKER_BUILDKIT=1 docker build --load  -t ${ckftpImage} ${codeDir}"
            return ckftpImage

        case "install-saas":
            branchNameIsaas = sh(returnStdout: true, script: "echo ${codeBranch} | tr '/' '_'").trim()
            commitIdIsaas = sh(returnStdout: true, script: "cd ${codeDir} && git rev-parse --short HEAD").trim()
            saasImage = "saas:${branchNameIsaas}-${commitIdIsaas}"
            flinkImage = "soc-saas-flink:${branchNameIsaas}-${commitIdIsaas}"
            nexusUrl = "10.50.2.250:8081/repository"
            sh """
                cd ${codeDir}
                sed -ri 's|10.50.2.224:8081/nexus/content/groups|'${nexusUrl}'|g' pom.xml 
                sed -ri 's|10.50.2.224:8081/nexus/content/repositories|'${nexusUrl}'|g' pom.xml
                mvn -Dmaven.test.skip=true  clean package -Prelease -U -T6
            """
            sh """
                DOCKER_BUILDKIT=1 docker build --load  --build-arg 'commitId=${commitIdIsaas}' -t ${saasImage} ${codeDir}
                DOCKER_BUILDKIT=1 docker build --load  --build-arg 'IMAGE_NAME_ARG=${flinkImage}' -t ${flinkImage} ${codeDir}/saas-infrastructure-flink/target/docker
            """
            return "${branchNameIsaas}-${commitIdIsaas}"

        case "install-lgentsvr":
            lgentsvrImage = "lgentsvr:develop"
            LgentsvrCICD("fullinstall","develop",codeDir)
            return lgentsvrImage
        
        case "install-lgentsvr-test":
            lgentsvrImage = "lgentsvr:develop"
            LgentsvrCICD("fullinstall-test","develop",codeDir)
            return lgentsvrImage

        case "install-k8syaml":
            branchNameIk8syaml = sh(returnStdout: true, script: "echo ${codeBranch} | tr '/' '_'").trim()
            commitIdIk8syaml = sh(returnStdout: true, script: "cd ${codeDir} && git rev-parse --short HEAD").trim()
            return "${branchNameIk8syaml}-${commitIdIk8syaml}"

        case "upgrade-lgentsvr":
            branchNamelgentsvr = sh(returnStdout: true, script: "echo ${codeBranch} | tr '/' '_'").trim()
            commitIdlgentsvr = sh(returnStdout: true, script: "cd ${codeDir} && git rev-parse --short HEAD").trim()
            lgentsvrDockerTag = "${branchNamelgentsvr}-${commitIdlgentsvr}"
            LgentsvrCICD("fullinstall",lgentsvrDockerTag,codeDir)
            return "lgentsvr:${lgentsvrDockerTag}"

        case "upgrade-lgentsvr-test":
            branchNamelgentsvr = sh(returnStdout: true, script: "echo ${codeBranch} | tr '/' '_'").trim()
            commitIdlgentsvr = sh(returnStdout: true, script: "cd ${codeDir} && git rev-parse --short HEAD").trim()
            lgentsvrDockerTag = "${branchNamelgentsvr}-${commitIdlgentsvr}"
            LgentsvrCICD("fullinstall-test",lgentsvrDockerTag,codeDir)
            return "lgentsvr:${lgentsvrDockerTag}"
    
        case "install-saas-v5.0":
            branchNameIsaas = sh(returnStdout: true, script: "echo ${codeBranch} | tr '/' '_'").trim()
            commitIdIsaas = sh(returnStdout: true, script: "cd ${codeDir} && git rev-parse --short HEAD").trim()
            saasImage = "saas:${branchNameIsaas}-${commitIdIsaas}"
            nexusUrl = "10.50.2.250:8081/repository"
            sh """
                cd ${codeDir}
                sed -ri 's|10.50.2.224:8081/nexus/content/groups|'${nexusUrl}'|g' pom.xml 
                sed -ri 's|10.50.2.224:8081/nexus/content/repositories|'${nexusUrl}'|g' pom.xml
                mvn -Dmaven.test.skip=true  clean package -Prelease -U -T6
            """
            sh """
                DOCKER_BUILDKIT=1 docker build --load  --build-arg 'commitId=${commitIdIsaas}' -t ${saasImage} ${codeDir}
            """
            return "${branchNameIsaas}-${commitIdIsaas}"

        case "install-saas-flink-v5.0":
            sh """
                bash ${codeDir}/package.sh
            """
            flinkImage = sh(returnStdout: true, script: "cat ${codeDir}/target/image_name").trim()
            sh """
                docker pull ${flinkImage}
            """
            return flinkImage

        case "upgrade-configmap":
            branchNameconfigmap = sh(returnStdout: true, script: "echo ${codeBranch} | tr '/' '_'").trim()
            commitIdconfigmap = sh(returnStdout: true, script: "cd ${codeDir} && git rev-parse --short HEAD").trim()
            return "${branchNameIk8syaml}-${commitIdIk8syaml}"
        
    }

}

def MapperRulePackage(mode,packageName,codeBranch){
    switch(mode) {
        case "mapper-rule":
            if ("${codeBranch}" == "main"){
            sh  """ 
                [ ! -d rule_packages ] && mkdir -p rule_packages
                rm -rf ./rule_packages/*
                mv rule*/system/mapper*.xml rule_packages
                zip -r -j -e -P '{JvV)fjayBnR0Uc7' ${packageName} rule_packages/*
                mv ${packageName} rule_packages/
            """
        }
    }
}