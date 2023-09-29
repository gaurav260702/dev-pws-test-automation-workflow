
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
  
  triggers {
    cron(env.BRANCH_NAME.equals('master') ? '00 01 * * 1-5' : '')
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
         //  testfiles = findFiles(glob: '**/Kicker.*.json')
             testfiles = findFiles(glob: '**/KickerSuite*QuoteServices*json')
         //  testfiles = findFiles(glob: '**/KickerSuite.Create.Quote.V2.INT.json')
         //  testfiles = findFiles(glob: '**/KickerSuite.Create.Update.Quote.V2.*.json')

          echo ""
          echo "${testfiles[0].name} ${testfiles[0].path} ${testfiles[0].directory} ${testfiles[0].length} ${testfiles[0].lastModified}"
          echo ""
        }
      }
    }

    stage('Running Cases') {
    environment {
            LDAP = credentials('d88e9614-fb62-4a2a-a4ca-380277fdb498')
            VAULT_ADDR = 'https://vault.aws.autodesk.com'
            VAULT_PATH = 'spg/pws-integration/aws/adsk-eis-ddws-int/sts/admin'
          }
      steps {
        script {
          sh """
            chmod -R u+rwX,go+rX,go-w . || true
            rm -f ~/.vault-token
            bash aws_auth
            cat ~/.aws/credentials
          """
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
              sh "docker run -v /home/jenkins/.aws/credentials:/root/.aws/credentials:ro wpe mvn spring-boot:run -Dspring-boot.run.arguments='${testcase_run_dir}'"
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