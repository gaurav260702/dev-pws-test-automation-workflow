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
  def imageName = "test-automation" 
  def regUser = "local-svc_p_ors_art" 

  def buildInfo = env.JOB_NAME + '-' + env.BUILD_NUMBER + "\n" + env.BUILD_URL
  def slackChannel = "#dpe-dbp-pws-devops"

  def workspace = env.WORKSPACE
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

    stage("create docker image") {
        sh "docker build --pull --no-cache -t '${dockerReg}/${imageName}' ."
    }
    stage('Test Execution Stages') {
            agent {node 'master'}
            steps {
                sh 'echo ${workspace}'
                sh 'cd ${workspace}/target/classes; find . -name "*.json" > /tmp/flist'
                sh 'cd ${workspace}
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
