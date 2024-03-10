#!/bin/bash

projectName=$1
branchName=$2
# DATE="`date +%Y%m%d%H%M`"
workHome=/home/admin
gitUrl="https://github.com/xiaomoxian2/${projectName}.git"

if [ -d $workHome/tmp ];then
    rm -fr $workHome/tmp/*
else
    mkdir $workHome/tmp
fi

cd $workHome/tmp && /usr/bin/git clone -b ${branchName} $gitUrl
supervisorctl stop gunicorn

rm -fr /home/admin/py_flask/*
mv /home/admin/tmp/${projectName}/* /home/admin/py_flask/

supervisorctl start gunicorn
[ $? -eq 0 ] && echo "构建成功！"