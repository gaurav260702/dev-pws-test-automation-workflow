def TEST_AUTOMATION_LOCAL_IMAGE="team-pws/wpe-test-automation:latest"
@Library('PSL@master') _

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

  def SUCCESS = "SUCCESS"
  def FAILURE = "FAILURE"
  def isMasterBranch = false


  if (env.BRANCH_NAME != 'master'){
    imageName = imageName + "-" + env.BRANCH_NAME.toLowerCase()
    isMasterBranch = true
  }

  try {
    currentBuild.result = SUCCESS

    stage("cleanup") {
      sh 'docker ps -a -q | xargs -r docker stop'
      sh 'docker ps -a -q | xargs -r docker rm'
      sh 'docker network ls --filter type=custom -q | xargs -r docker network rm'
      sh 'rm -f ~/.dockercfg || true'
      sh 'rm -f ~/.docker/config.json || true'
    }

    stage("checkout") {
      checkout scm
      sh "git clean -fxd"
    }


    stage("create docker image") {
        sh "docker build --pull --no-cache -t '${dockerReg}/${imageName}' ."
    }
    stage('find cases') {
       steps {
          script {
             testfiles = findFiles(glob: '**/*INT.json')

          }
       }
       post {
          cleanup {
             echo 'completed the anylisis of test cases'
          }
       }
    }
    stage('run test') {
       steps {
          script {
              def dir_offset_to_trim = 'src/main/resources/'
              def testcase_run_dir
              def full_dir
              for(int i=0; i < testfiles.size(); i++) {
                 full_dir = "${testfiles[i].path}"
                 testcase_run_dir = full_dir.replaceAll(/^${dir_offset_to_trim}/, "")
                 stage(testfiles[i].name){
                    echo "Test case full directory ${full_dir}"
                    echo "Test case relative directory to run: ${testcase_run_dir}"
                    echo "docker run -it  ${TEST_AUTOMATION_LOCAL_IMAGE}  mvn spring-boot:run -Dspring-boot.run.arguments=${testcase_run_dir}"
                 }
              }
          }
      }
      post {
         cleanup {
            echo 'done'
         }
      }
   }
    stage ('run test') {
        script {
            docker run -it ${TEST_AUTOMATION_LOCAL_IMAGE}  mvn spring-boot:run -Dspring-boot.run.arguments='testdata/WorkflowProcessing/KickerSuites/KickerSuite.GetInvoiceServices.INT.json'
        }
    }

    stage("executeHarmonyScan") {
      steps {
         executeHarmonyScan()
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

def executeHarmonyScan() {
  echo "ExecuteHarmonyScan ..."
  new ors.security.common_harmony(steps, env, Artifactory, scm).run_scan([
    'repository':"pws/pws-test-automation-workflow",
    'product_output':"${workspace}/node_modules",
  ])
}
