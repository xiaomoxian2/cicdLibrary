/*
首行导入 otherTools
然后最外层是一个 call 函数
接下来全篇就是正常的一个 pipeline 声明式写法（脚本式也如此）
*/
import org.devops.otherTools

def call(Map runMap) {

    def timeStr=new Date().format('yyyyMMddHHmm')
    def mytools = new org.devops.otherTools()

    pipeline {
        agent {
            label runMap.RUN_NODE       // 调用 Map 参数里面的值来确定该 Job 跑在哪
        }
        environment {
            serverName = "${runMap.SERVERNAME}"         // 设置 pipeline 环境变量，方便下面调用
        }

        stages {
            stage('拉取代码') {
                steps {
                        script {
                                mytools.printMsg('hello world')         // 放在 script {} 里调用
                        }
                    sh """
                        echo "start time ：${timeStr}"
                        echo "pull ${serverName} code"
                    """
                }
            }

            stage('构建项目') {
                steps {
                    sh "echo 'build'"
                }
            }

            stage('启动程序') {
                steps {
                    sh "echo 'start'"
                }
            }
        }
    }
}