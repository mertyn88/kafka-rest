#!/usr/bin/env groovy

def getCommitMsg() {
  return sh(script: """git log --format="medium" -1 ${env.GIT_COMMIT}""", returnStdout: true)
}

def sendMessage(msg) {
  def commitMsg = getCommitMsg()
  slackSend(channel: '#noti-tech-jenkins', color: "good",
    message: """
        $BUILD_TIMESTAMP
        - ${env.BUILD_URL}
        - ${params.MODULE}:${env.BRANCH_NAME} -> ${env.BUILD_ID}
        - ${msg}

        ${commitMsg}
    """
    )
}

def sendFailMessage(msg) {
    def commitMsg = getCommitMsg()
    slackSend(channel: '#noti-tech-jenkins', color: "danger",
        message: """
        $BUILD_TIMESTAMP
        ${env.BUILD_URL}
        - ${msg}
    """
    )
}

def getEnvName(branch) {
    if(branch.equals("develop")) {
        return "dev";
    } else if (branch.equals("release")) {
        return "qa";
    } else if (branch.equals("master")) {
        return "prod";
    } else {
        return "dev";
    }
}

pipeline {
    agent {
        node {
            label 'slave-1'
        }
    }
    parameters {
        string(name : 'PROJECT', defaultValue : 'kafka-rest', description : '프로젝트명')
        choice(name : 'MODULE', choices: ['kafka-rest'], description: '멀티모듈명')
        choice(name : 'DEPLOY', choices: ['true'], description: 'EKS 배포 여부')
    }
    environment {
        ECR = "697736665449.dkr.ecr.ap-northeast-2.amazonaws.com"
        REGION = "ap-northeast-2"
        ENV = getEnvName(env.BRANCH_NAME)
    }
    options {
        disableConcurrentBuilds()
        buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '30', daysToKeepStr: '30', numToKeepStr: '')
    }
    tools {
        maven 'apache-maven-3.8.5'
        jdk 'JAVA 11'
    }

    stages {
        stage('Project prepared') {
            steps {
                checkout scm
                sendMessage("${params.PROJECT}.${params.MODULE} build start")
            }
        }

        stage('Project build') {
            steps {
                sh """
                    mvn clean install -DskipTests -Dcheckstyle.skip -Dspotless.check.skip=true
                """
            }
            post {
                failure {
                    sendFailMessage('STEP 1. Failure Maven build')
                }
            }
        }

        stage('Project docker build') {
            steps {
                sh """
                    docker build --tag ${params.PROJECT}/${params.MODULE}:${ENV}.${env.BUILD_ID} ./${params.MODULE}
                """
            }
            post {
                failure {
                    sendFailMessage('STEP 2. Failure docker build')
                }
            }
        }

        stage('Project before docker images') {
            steps {
                sh """
                    docker images
                """
            }
        }

        stage('AWS Login') {
            steps {
                sh """
                    aws ecr get-login-password --region ${REGION} | docker login --username AWS --password-stdin ${ECR}
                """
            }
            post {
                failure {
                    sendFailMessage('STEP 3. Failure ECR Login')
                }
            }
        }

        stage('Project docker images ecr upload') {
            steps {
                sh """
                    docker tag ${params.PROJECT}/${params.MODULE}:${ENV}.${env.BUILD_ID} ${ECR}/${params.PROJECT}/${params.MODULE}:${ENV}.${env.BUILD_ID}
                    docker tag ${params.PROJECT}/${params.MODULE}:${ENV}.${env.BUILD_ID} ${ECR}/${params.PROJECT}/${params.MODULE}:${ENV}.latest
                    docker push ${ECR}/${params.PROJECT}/${params.MODULE}:${ENV}.${env.BUILD_ID}
                    docker push ${ECR}/${params.PROJECT}/${params.MODULE}:${ENV}.latest
                    docker rmi ${params.PROJECT}/${params.MODULE}:${ENV}.${env.BUILD_ID} ${ECR}/${params.PROJECT}/${params.MODULE}:${ENV}.${env.BUILD_ID} ${ECR}/${params.PROJECT}/${params.MODULE}:${ENV}.latest
                """
            }
            post {
                failure {
                    sendFailMessage('STEP 4. Failure ECR ECR Upload')
                }
            }
        }

        stage('Project after docker images') {
            steps {
                sh """
                    docker images
                """
            }
        }

        stage('Project deploy EKS') {
            when {
                expression { params.DEPLOY ==~ /^true$/ }
            }
            steps {
                sh """
                    aws eks --region ${REGION} update-kubeconfig --name ${ENV}-lific-eks
                    /usr/local/bin/kubectl -n ${ENV} rollout restart deployment ${params.PROJECT}-${params.MODULE}
                """
            }
            post {
                failure {
                    sendFailMessage('STEP 5. Failure ECR EKS Deploy')
                }
            }
        }
    }
}