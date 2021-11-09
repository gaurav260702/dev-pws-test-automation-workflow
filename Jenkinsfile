#!/usr/bin/env groovy
@Library("PSL@master") _

def dockerReg = "autodesk-docker.art-bobcat.autodesk.com/team-pws"
def imageName = "test-automation" 
def regUser = "local-svc_p_ors_art" 
def buildInfo = env.JOB_NAME + '-' + env.BUILD_NUMBER + "\n" + env.BUILD_URL
def workspace = env.WORKSPACE
def slackChannel = "#dpe-dbp-pws-devops"
def SUCCESS = "SUCCESS"
def FAILURE = "FAILURE"
def isMasterBranch = false

properties([

    parameters([
        choice(name: 'ForcePublish',
            choices: 'No\nYes',
            description: 'Force publish to Artifactory (No/Yes). Default only publishes Master branches'
        )
    ])
])

pipeline {
  options {buildDiscarder(logRotator(daysToKeepStr: '7', numToKeepStr: '1'))}
  stages {
    agent {node 'aws-centos'}

    stage("checkout") {
      checkout scm
      sh "git clean -fxd"
    }

    stage("create docker image") {
        sh "docker build --pull --no-cache -t '${dockerReg}/${imageName}' ."
    }
    stage('Test Execution Stages') {
         steps {
                sh 'echo ${workspace}'
                sh 'cd ${workspace}/target/classes; find . -name "*.json" > /tmp/flist'
                sh 'cd ${workspace}'
                sh 'cat /tmp/flist'
                script {
                    def mylist = readFile("/tmp/flist").readLines()
                    for(int i=0; i < mylist.size(); i++) {
                        stage(mylist[i]){
                            sh '''
                               echo "Executing Test ==> $i"
                               docker run -- team-pws/test-automation:latest mvn spring-boot:run -Dspring-boot.run.arguments='${i}'
                            '''
                        }
                    }
                }
         }
         post {
                cleanup {
                   cleanWs()
                }
         }
   }
    stage("push images to artifactory") {
      if (env.BRANCH_NAME != 'master' && params.ForcePublish == 'No') {
        echo "Skipping 'docker push' because branch is not master"
        return
      }

      docker.withRegistry( "https://${dockerReg}/", regUser ) {
         sh "docker tag '${dockerReg}/${imageName}' '${dockerReg}/${imageName}:latest'"
         sh "docker push '${dockerReg}/${imageName}:latest'"
      }
    }
}
}
