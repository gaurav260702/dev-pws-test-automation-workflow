properties([
    parameters([
        choice(name: 'ForcePublish',
            choices: 'No\nYes',
            description: 'Force publish to Artifactory (No/Yes). Default only publishes Master branches'
        )
    ])
])

node('aws-centos') {
  def dockerReg = "autodesk-docker.art-bobcat.autodesk.com/team-pws"
  def imageName = "wpe-test-automation" // docker image name
  def regUser = "local-svc_p_ors_art" // docker registry user ID

  // Notifications
  def buildInfo = env.JOB_NAME + '-' + env.BUILD_NUMBER + "\n" + env.BUILD_URL
  def slackChannel = "#dpe-dbp-pws-devops"

  def SUCCESS = "SUCCESS"
  def FAILURE = "FAILURE"

  if (env.BRANCH_NAME != 'master'){
    imageName = imageName + "-" + env.BRANCH_NAME.toLowerCase()
  }

  try {
    currentBuild.result = SUCCESS

    stage("checkout") {
      checkout scm
      sh "git clean -fxd"
    }

    stage("create docker image") {
        sh "docker build --pull --no-cache -t team-pws/wpe-test-automation:latest ."
    }

    stage("push images to artifactory") {
      if (env.BRANCH_NAME != 'master' && params.ForcePublish == 'No') {
        echo "Skipping 'docker push' because branch is not master"
        return
      }

      docker.withRegistry( "https://${dockerReg}/", regUser ) {
          sh "docker tag 'team-pws/wpe-test-automation:latest' 'team-pws/wpe-test-automation:latest'"
          sh "docker push 'team-pws/wpe-test-automation:latest'"
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
      versions.each( { v ->
        sh "docker rmi --force 'team-pws/wpe-test-automation:latest' >/dev/null 2>&1 || true"
      })
    }
  }
}
