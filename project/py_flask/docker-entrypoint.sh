#!/bin/bash

/usr/sbin/nginx

cd /home/admin/py_flask
/usr/bin/gunicorn -w 2 -b :8000 app:app

while :
do
    echo "process 1 sleep 60.."
    sleep 60
done
