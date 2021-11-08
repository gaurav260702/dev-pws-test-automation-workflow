@Library("PSL@master") _
def BEDROCK_BUILD_IMAGE = 'autodesk-docker.art-bobcat.autodesk.com/team-pws/bedrock-build-terraform:latest'
def TEST_AUTOMATION_IMAGE = 'autodesk-docker.art-bobcat.autodesk.com/team-pws/bedrock-build-terraform:latest'

def testfiles

  def TEST_AUTOMATION_LOCAL_IMAGE="team-pws/wpe-test-automation:latest"
  def dockerReg = "autodesk-docker.art-bobcat.autodesk.com/team-pws"
  def imageName = "test-automation" 
  def regUser = "local-svc_p_ors_art" 

  def buildInfo = env.JOB_NAME + '-' + env.BUILD_NUMBER + "\n" + env.BUILD_URL
  def slackChannel = "#dpe-dbp-pws-devops"

  def SUCCESS = "SUCCESS"
  def FAILURE = "FAILURE"
  def isMasterBranch = false

  if (env.BRANCH_NAME != 'master'){
    imageName = imageName + "-" + env.BRANCH_NAME.toLowerCase()
    isMasterBranch = true
  }
    currentBuild.result = SUCCESS

pipeline {
    parameters {
      string(name: 'AgentLabel',
        defaultValue: "aws-centos",
        description: 'Slave Label for the node to run the build on'
      )
      choice(name: 'ForcePublish',
            choices: 'No\nYes',
            description: 'Force publish to Artifactory (No/Yes). Default only publishes Master branches'
      )
    }
    
    agent {
      label "${params.AgentLabel}"
    }
    stages {
       stage('Prepare') {
           steps {
                script {
                    testfiles = findFiles(glob: '**/*Tests.java')
                }
            }
            post {
                cleanup {
                    echo 'completed the anylisis of test cases'
                }
            }
        }


    stage("checkout") {
      checkout scm
      sh "git clean -fxd"
    }
    stage('find cases') {
       steps {
          script {
            // testfiles = findFiles(glob: '**/*INT.json')
            echo "finding files ..."
          }
       }
       post {
          cleanup {
             echo 'completed listing of test cases'
          }
       }
    }

    stage("create docker image") {
        sh "docker build --pull --no-cache -t '${dockerReg}/${imageName}' ."
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

    stage("cleanup docker image") {
        sh "docker rmi --force '${imageName}' >/dev/null 2>&1 || true"
    }
  }
}
