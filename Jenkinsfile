import groovy.json.JsonSlurper;
import groovy.json.JsonOutput;

@Library("PSL@master") _


def dockerReg = "autodesk-docker.art-bobcat.autodesk.com/team-pws"

def dockerTestImage = "autodesk-docker.art-bobcat.autodesk.com/team-pws/test-automation:latest"

def imageName = "test-automation-"+ env.BUILD_NUMBER
def regUser = "local-svc_p_ors_art"

def buildInfo = env.JOB_NAME + '-' + env.BUILD_NUMBER + "\n" + env.BUILD_URL
def slackChannel = "#dpe-dbp-pws-devops"

def isMasterBranch = false

def testfiles

def allTests = [
  QuoteServices_STG:[ path:  "testdata/WorkflowProcessing/KickerSuites/KickerSuite.QuoteServices.STG.json"],
  QuoteServices_INT: [ path: "testdata/WorkflowProcessing/KickerSuites/KickerSuite.QuoteServices.INT.json"],
  CatalogExport_INT: [ path: "testdata/WorkflowProcessing/KickerSuites/KickerSuite.CatalogExport.INT.json"],
  PromotionsExport_INT: [ path: "testdata/WorkflowProcessing/KickerSuites/KickerSuite.PromotionsExport.INT.json"]
]

pipeline {
  agent {
       label "aws-centos"
  }
  parameters {
    booleanParam(name: 'QuoteServices_STG',   description: 'Run QuoteServices Tests in STG', defaultValue: false)
    booleanParam(name: 'QuoteServices_INT',   description: 'Run QuoteServices Tests in INT', defaultValue: false)
    booleanParam(name: 'CatalogExport_INT',   description: 'RUN CatalogExport Tests in INT', defaultValue: true)
    booleanParam(name: 'PromotionsExport_INT',   description: 'RUN PromotionsExport Tests in INT', defaultValue: true)
  }
  
  triggers {
    parameterizedCron(env.BRANCH_NAME == 'send-test-reports' ? '''
        # run tests everyday at 5 AM PST
        0 5 * * * % QuoteServices_STG=true;QuoteServices_INT=true;
    ''' : '')
  }
  options {
    disableConcurrentBuilds()
  }
  
  stages {
    stage('Build Image') {
     steps {
       script {
          isMasterBranch = "${env.BRANCH_NAME}" == 'master'
          // Uncomment to allow your branch to act as master ONLY FOR TESTING
          // isMasterBranch = true
          sh "docker build --tag ${imageName} ."
        }
      }
    }
    stage('Set Up Automation Test') {
      agent {
        docker {
          image "${imageName}"
          reuseNode true
          args '-v /tmp:/tmp'
        }
      }
      environment {
        LDAP = credentials('6215a3b2-fb0e-4beb-b2ab-cf3b3fb52bc0')
      }
      // environment {
      //       LDAP = credentials('d88e9614-fb62-4a2a-a4ca-380277fdb498')
      //       VAULT_ADDR = 'https://vault.aws.autodesk.com'
      //       VAULT_PATH = 'spg/pws-integration/aws/adsk-eis-ddws-int/sts/admin'
      //     }
      steps {
        withCredentials([
          usernamePassword(credentialsId: 'pws-k6-influx-db-write-user',
            usernameVariable: 'INFLUX_DB_USERNAME',
            passwordVariable: 'INFLUX_DB_PASSWORD',
          )
        ]) {
        script {
          try {
            // sh """
            // chmod -R u+rwX,go+rX,go-w . || true
            // rm -f ~/.vault-token
            // bash aws_auth
            // cat ~/.aws/credentials
            // """
            allTests.each { test ->
                echo "TEST-START"
                if (params[test.key]) {
                  echo "Key: ${test.key}"
                  echo "value: ${test.value.path}"
                    stage("${test.key}") {
                    sh "mvn spring-boot:run -Dspring-boot.run.arguments='${test.value.path}'"
                    }
                }
            }
            stage('Send Test Report'){
              sendReports(isMasterBranch)
            }
          } catch (err) {
            echo "${err}"
          }
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
        sh "docker image ls"
        sh "docker image rm -f ${imageName}"
        sh "cd /tmp ls"
      }
    }
  }
}

def generateStage(key, valuePath) {
  return {
    node('aws-centos') {
      stage("stage: ${key}") {
      sh "mvn spring-boot:run -Dspring-boot.run.arguments='${valuePath}'"
        sleep 30
      }
    }
  }
}

def sendReports(isMasterBranch) {
  script {
    echo "${isMasterBranch}"
    echo("Send Reports")
    dir('/tmp/reports') {
      def files = findFiles() 
  
      files.each { f -> 
          echo "This is a directory: ${f.name}"
          def configJson = readJSON file: "/tmp/reports/${f.name}"
          def ENV_NAME = configJson.$ENV$
          def TEST_STATUS = configJson.$TEST_STATUS$
          def TEST_NAME = (configJson.$TEST_NAME$).replace( 'Kicker.', '').replace( '.INT.json', '').replace( '.STG.json', '')
          def statusName = "pass"
          if(TEST_STATUS == "FAIL"){
            statusName = "fail"
          }
          def API_CALLS = configJson.apiCalls
          def Validator_path = (configJson.validationFile).replaceAll( '/testdata/WorkflowProcessing/TestData/Validators', '')
          def jsonData = [
            "GIT_BRANCH":env.GIT_BRANCH,
            "BUILD_NUMBER":env.BUILD_NUMBER,
            "ENV_NAME": ENV_NAME,
            "TEST_STATUS": TEST_STATUS,
            "TEST_NAME": TEST_NAME,
            "API_CALLS": API_CALLS,
            "Validator_path": Validator_path
          ]
          echo "${JsonOutput.toJson(jsonData)}"
          def valiDatorJson = readJSON file: "src/main/resources/${validationFile}"
          echo "${valiDatorJson}"
          if(isMasterBranch) {
          sh """
            curl -i -XPOST "https://calvinklein-7de56744.influxcloud.net:8086/write?db=k6&u=$INFLUX_DB_USERNAME&p=$INFLUX_DB_PASSWORD" --data-binary 'automation_test_report,TEST_NAME=${TEST_NAME},ENV_NAME=${ENV_NAME},TEST_STATUS=${TEST_STATUS},BUILD=${env.GIT_BRANCH}-${env.BUILD_NUMBER} value=1,${statusName}=1'
          """
          } 
          else {
            echo "Skipping send reports"
          }
      }
    }
  }
}