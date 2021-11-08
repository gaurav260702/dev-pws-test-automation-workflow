properties([
    parameters([
        choice(name: 'ForcePublish',
            choices: 'No\nYes',
            description: 'Force publish to Artifactory (No/Yes). Default only publishes Master branches'
        )
    ])
])

node('aws-centos') {
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

  try {
    currentBuild.result = SUCCESS

    stage("checkout") {
      checkout scm
      sh "git clean -fxd"
    }
    stage('find cases') {
       steps {
          script {
             testfiles = findFiles(glob: '**/*INT.json')
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
  } catch (err) {
    currentBuild.result = FAILURE
    throw err

  } finally {
    withCredentials([[$class: 'StringBinding', credentialsId: 'pws_slack_token', variable: 'mytoken']]) {
      build_succeeded = currentBuild.result == SUCCESS

      slackSend(message: "Build ${build_succeeded ? "Succeeded" : "Failed"}: ${buildInfo}",
        teamDomain: 'autodesk', token: env.mytoken, channel: "${slackChannel}",
        color: "${build_succeeded ? "good" : "danger"}")
    }

    stage("cleanup docker image") {
        sh "docker rmi --force '${imageName}' >/dev/null 2>&1 || true"
    }
  }
}
