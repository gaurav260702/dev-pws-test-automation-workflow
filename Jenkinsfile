#!/usr/bin/env groovy

@Library("PSL@master") _

def dockerReg = "autodesk-docker.art-bobcat.autodesk.com/team-pws"

def dockerTestImage = "autodesk-docker.art-bobcat.autodesk.com/team-pws/test-automation:latest"

def imageName = "test-automation"
def regUser = "local-svc_p_ors_art"

def buildInfo = env.JOB_NAME + '-' + env.BUILD_NUMBER + "\n" + env.BUILD_URL
def slackChannel = "#dpe-dbp-pws-devops"

def SUCCESS = "SUCCESS"
def FAILURE = "FAILURE"
def isMasterBranch = false

def testfiles

pipeline {
  agent {
       label "aws-centos"
  }
  stages {
    stage('Build Image') {
     script {
        sh "docker build --tag wpe ."
        sh "docker image ls"
      }
    }
    stage('find test cases') {
      agent {
        label "aws-centos"
      }
      steps {
        script {
          testfiles = findFiles(glob: '**/Kicker.*.json')
          echo ""
          echo "${testfiles[0].name} ${testfiles[0].path} ${testfiles[0].directory} ${testfiles[0].length} ${testfiles[0].lastModified}"
          echo ""
        }
      }
    }
    stage('Running Cases') {
      steps {
        script {
          echo ""
          echo "${testfiles[0].name} ${testfiles[0].path} ${testfiles[0].directory} ${testfiles[0].length} ${testfiles[0].lastModified}"
          echo ""
          def dir_offset_to_trim = 'src/main/resources/'
          def testcase_run_dir
          def full_dir
          for (int i = 0; i < testfiles.size(); i++) {
            full_dir = "${testfiles[i].path}"
            testcase_run_dir = full_dir.replaceAll(/^${dir_offset_to_trim}/, "")
            stage(testfiles[i].name) {
              echo "Test case full directory ${full_dir}"
              echo "Test case relative directory to run: ${testcase_run_dir}"
              sh "docker run wpe mvn spring-boot:run -Dspring-boot.run.arguments='${testcase_run_dir}'"
            }
          }
        }
      }
    }
  }
  post {
    always {
      script {
        sh 'docker image rm -f wpe'
        sh 'docker image ls'
      }
    }
  }
}
