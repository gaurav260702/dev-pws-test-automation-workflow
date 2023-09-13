
@Library("PSL@master") _
import groovy.json.JsonSlurper;
import groovy.json.JsonOutput;

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
  
  triggers {
    cron(env.BRANCH_NAME.equals('master') ? '00 01 * * 1-5' : '')
  }
  
  stages {
    stage('Run Test Cases') {
      // environment {
      //       LDAP = credentials('d88e9614-fb62-4a2a-a4ca-380277fdb498')
      //       VAULT_ADDR = 'https://vault.aws.autodesk.com'
      //       VAULT_PATH = 'spg/pws-integration/aws/adsk-eis-ddws-int/sts/admin'
      //     }
      agent {
        dockerfile {
            reuseNode true
            args '-v /tmp/reports:/reports'
          }
      }
      steps {
        script {
          try {
            // sh """
            // chmod -R u+rwX,go+rX,go-w . || true
            // rm -f ~/.vault-token
            // bash aws_auth
            // cat ~/.aws/credentials
            // """
            sh "mvn spring-boot:run -Dspring-boot.run.arguments='testdata/WorkflowProcessing/KickerSuites/KickerSuite.QuoteServices.INT_STG.json'"
            sendReports()
          } catch (err) {
            echo "${err}"
          }
        }
      }
    }

    // stage('Running Cases') {
    // environment {
    //         LDAP = credentials('d88e9614-fb62-4a2a-a4ca-380277fdb498')
    //         VAULT_ADDR = 'https://vault.aws.autodesk.com'
    //         VAULT_PATH = 'spg/pws-integration/aws/adsk-eis-ddws-int/sts/admin'
    //       }
    //   steps {
    //     script {
    //       sh """
    //         chmod -R u+rwX,go+rX,go-w . || true
    //         rm -f ~/.vault-token
    //         bash aws_auth
    //         cat ~/.aws/credentials
    //       """
    //       echo ""
    //       echo "${testfiles[0].name} ${testfiles[0].path} ${testfiles[0].directory} ${testfiles[0].length} ${testfiles[0].lastModified}"
    //       echo ""
    //       def dir_offset_to_trim = 'src/main/resources/'
    //       def testcase_run_dir
    //       def full_dir
    //       for (int i = 0; i < testfiles.size(); i++) {
    //         full_dir = "${testfiles[i].path}"
    //         testcase_run_dir = full_dir.replaceAll(/^${dir_offset_to_trim}/, "")
    //         stage(testfiles[i].name) {
    //           echo "Test case full directory ${full_dir}"
    //           echo "Test case relative directory to run: ${testcase_run_dir}"
    //           sh "docker run -v /home/jenkins/.aws/credentials:/root/.aws/credentials:ro wpe mvn spring-boot:run -Dspring-boot.run.arguments='${testcase_run_dir}'"
    //         }
    //       }
    //     }
    //   }
    // }
  }
  post {
    always {
      script {
        echo ""
        sh 'ls /tmp/reports'
        sh 'docker image rm -f wpetest'
        sh 'docker image ls'
      }
    }
  }
}

def sendReports() {
  script {
    echo("Send Reports")
    dir('/tmp/reports') {
      def files = findFiles() 
  
      files.each { f -> 
          echo "This is a directory: ${f.name}"
          def configJson = readJSON file: "/tmp/reports/${f.name}"
          def ENV_NAME = configJson.$ENV$
          def TEST_STATUS = configJson.$TEST_STATUS$
          def TEST_NAME = configJson.$TEST_NAME$
          def jsonReport = [
            "GIT_BRANCH":env.GIT_BRANCH,
            "BUILD_NUMBER":env.BUILD_NUMBER,
            "ENV_NAME": ENV_NAME,
            "TEST_STATUS": TEST_STATUS,
            "TEST_NAME": TEST_NAME,
          ]
          echo "${JsonOutput.toJson(jsonReport)}"
          // sh """
          //   curl -i -XPOST "https://calvinklein-7de56744.influxcloud.net:8086/write?db=k6&u=k6writeuser&p=Autodesk@123" --data-binary 'automotion_test_report,TEST_NAME=${TEST_NAME},ENV_NAME=${ENV_NAME},TEST_STATUS=${TEST_STATUS},BUILD=${env.GIT_BRANCH}-${env.BUILD_NUMBER} BUILD_NUMBER=${env.BUILD_NUMBER}'
          //    """
      }
    }
  }
}