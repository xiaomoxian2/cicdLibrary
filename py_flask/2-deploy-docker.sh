#!/bin/bash

codeDir=$1
cicdHome=$2
projectName=$3
branchName=$4
nodeIp=$5
# DATE="`date +%Y%m%d%H%M`"
cd ${codeDir}/${projectName}
imgTag=`/usr/bin/git rev-parse --short HEAD`
imgName="local/flask:${imgTag}"

cp -f ${cicdHome}/${projectName}/* ${codeDir}/${projectName}/deploy/
/usr/bin/docker build -t ${imgName} -f deploy/Dockerfile_2 .
/usr/bin/docker tag ${imgName} 10.50.2.92:8086/${imgName}
/usr/bin/docker push 10.50.2.92:8086/${imgName}

#rsync -avz --delete $codeDir/ ${nodeIp}:/home/admin/${projectName}/
#cd $workHome/tmp && /usr/bin/git clone -b ${branchName} $gitUrl
ssh $nodeIp "docker ps -a |grep "local/flask" |awk '{print $1}' |xargs docker stop"
ssh $nodeIp "docker pull 10.50.2.92:8086/${imgName} && docker run -p 8088:80 -d 10.50.2.92:8086/${imgName}"

[ $? -eq 0 ] && echo "构建成功！"