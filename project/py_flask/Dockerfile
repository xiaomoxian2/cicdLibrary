FROM python:3.7
WORKDIR ./docker_demo

ADD . .
RUN pip install -i https://mirrors.aliyun.com/pypi/simple/ pip -U && pip config set global.index-url https://mirrors.aliyun.com/pypi/simple && \
   pip install --upgrade pip && \
  pip install -r requirements.txt
CMD ["python", "./app.py"]
