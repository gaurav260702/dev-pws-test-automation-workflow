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
def jobs = [:]
def BUILD_COMPLETE = false

pipeline {
  agent {
       label "aws-centos"
  }
  triggers {
    //run security scans everyday at 5 AM PST
    cron( env.BRANCH_NAME.equals('master') ? '0 12 * * *' : '')
  }
  stages {
    stage('Build Image') {
     steps {
       script {
          sh "docker build --tag wpe ."
          sh "docker image ls"
        }
      }
    }
    stage('Find Test Cases') {
      agent {
        label "aws-centos"
      }
      steps {
        script {
                  testfiles = findFiles(glob: '**/Kicker.*.json')
             //   testfiles = findFiles(glob: '**/KickerSuite*QuoteServices*json')
          echo ""
          echo "${testfiles[0].name} ${testfiles[0].path} ${testfiles[0].directory} ${testfiles[0].length} ${testfiles[0].lastModified}"
          echo ""
        }
      }
    }
    stage('Running Cases') {
      steps {
        script {
          def dir_offset_to_trim = 'src/main/resources/'
          def testcase_run_dir
          def full_dir

          testfiles.each { file ->
            full_dir = "${file.path}"
            testcase_run_dir = full_dir.replaceAll(/^${dir_offset_to_trim}/, "")
            jobs["job-${file.path}"] = {
              stage(file.name) {
                  echo "\n\n\n Test case full directory ${full_dir}"
                  echo "Test case relative directory to run: ${testcase_run_dir}"
                  sh "docker run wpe mvn spring-boot:run -Dspring-boot.run.arguments='${testcase_run_dir}' | tee output.log"
                  sh '! grep \'"TestStatus":"FAIL"\' output.log'
                  BUILD_COMPLETE = true
              }
            }
          }
          jobs["monitor-status"] = {
            stage("monitoring-logs") {
              echo "Monitoring Logs .... "
              while(BUILD_COMPLETE != true) {
                sh '! grep \'"TestStatus":"FAIL"\' output.log'
              }
            }
          };
          jobs.failFast = true
          parallel jobs
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
