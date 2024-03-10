#!/bin/bash
projectName=$1
branchName=$2
deployMode=$3
k3sIp=$4

echo -e "项目：${projectName}\n分支：${branchName}\n部署模式：${deployMode}\n部署节点：${k3sIp}"

workHome=/home/admin
cicdHome=/home/admin/cicd
tmpDir=/home/admin/${projectName}_tmp
gitUrl="https://github.com/xiaomoxian2/${projectName}.git"

if [ -d $tmpDir ];then
    rm -fr $tmpDir/*
else
    mkdir $tmpDir
fi

# 每个项目自实现部署方式
function deployMachine(){
    echo "==================物理机方式-部署开始====================="
    cd $tmpDir && /usr/bin/git clone -b ${branchName} $gitUrl
    bash $cicdHome/${projectName}/2-deploy-m.sh $tmpDir ${projectName} ${branchName} $k3sIp
    echo "==================物理机方式-部署结束====================="
}

function deployDocker(){
    echo "==================Docker方式-部署开始====================="
    scp $workHome/${projectName}/2-deploy-docker.sh $k3sIp:/home/admin/2-deploy-docker.sh
    ssh $k3sIp "bash /home/admin/2-deploy-docker.sh ${projectName} ${branchName}"
    echo "==================Docker方式-部署结束====================="
}

function deployK3s(){
    echo "==================K3s方式-部署开始====================="
    bash $workHome/${projectName}/deploy/2-deploy-k3s.sh ${projectName} ${branchName} $k3sIp
    echo "==================K3s方式-部署结束====================="
}

case $deployMode in
  machine)
    deployMachine
    ;;
  docker) 
    deployDocker
    ;;
  k3s)
    deployK3s
    ;;
  *)
    echo "未知部署方式"
    ;;
esac
