#!/bin/bash

codeDir=$1
projectName=$2
branchName=$3
nodeIp=$4
# DATE="`date +%Y%m%d%H%M`"

rsync -avz --delete $codeDir/ ${nodeIp}:/home/admin/${projectName}/
#cd $workHome/tmp && /usr/bin/git clone -b ${branchName} $gitUrl
ssh $nodeIp "bash supervisorctl restart gunicorn "

[ $? -eq 0 ] && echo "构建成功！"