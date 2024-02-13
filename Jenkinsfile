import groovy.json.JsonSlurper;
import groovy.json.JsonOutput;
import com.influxdb:influxdb-k6-java;

@Library(["PSL@master","TestAutomationUtils@master"]) _

def dockerReg = "autodesk-docker.art-bobcat.autodesk.com/team-pws"

def dockerTestImage = "autodesk-docker.art-bobcat.autodesk.com/team-pws/test-automation:latest"

def imageName = "test-automation-" + env.BUILD_NUMBER
def regUser = "local-svc_p_ors_art"

def buildInfo = env.JOB_NAME + '-' + env.BUILD_NUMBER + "\n" + env.BUILD_URL
def slackChannel = "#dpe-dbp-pws-devops"

def isMasterBranch = false
def paramsSelected = false
def testfiles
def vaultPath = null
def allTests = [
  Webhook_Notify_INT: [path: "testdata/WorkflowProcessing/KickerSuites/KickerSuite.NotificationWebhook.INT.json"],
  Webhook_Notify_STG: [path: "testdata/WorkflowProcessing/KickerSuites/KickerSuite.NotificationWebhook.STG.json"],
  QuoteServices_V2_INT: [path: "testdata/WorkflowProcessing/KickerSuites/KickerSuite.QuoteServices.Create.Update.Quote.V2.INT.json"],
  QuoteServices_V2_NZ_INT: [path: "testdata/WorkflowProcessing/KickerSuites/KickerSuite.QuoteServices.Create.Update.Quote.V2.NZ.INT.json"],
  QuoteServices_V2_NZ_STG: [path: "testdata/WorkflowProcessing/KickerSuites/KickerSuite.QuoteServices.Create.Update.Quote.V2.NZ.STG.json"],
  QuoteServices_V2_STG: [path: "testdata/WorkflowProcessing/KickerSuites/KickerSuite.QuoteServices.Create.Update.Quote.V2.STG.json"],
  QuoteNotifyWebhook_INT: [path: "testdata/WorkflowProcessing/KickerSuites/KickerSuite.QuoteNotificationWebhook.INT.json"],
  QuoteServices_STG: [path: "testdata/WorkflowProcessing/KickerSuites/KickerSuite.QuoteServices.STG.json"],
  QuoteServices_INT: [path: "testdata/WorkflowProcessing/KickerSuites/KickerSuite.QuoteServices.INT.json"],
  GetQuoteDetailsInternalv2_INT: [path: "testdata/WorkflowProcessing/KickerSuites/KickerSuite.ServicesQuote.GetQuoteDetailsInternalv2.INT.json"],
  CatalogExport_INT: [path: "testdata/WorkflowProcessing/KickerSuites/KickerSuite.CatalogExport.INT.json"],
  PromotionsExport_INT: [path: "testdata/WorkflowProcessing/KickerSuites/KickerSuite.PromotionsExport.INT.json"]
]

pipeline {
  agent {
    label "aws-centos"
  }
  parameters {
    choice(name: 'Environment',
        choices: 'INT\nSTG',
        description: 'Select Environment(INT/STG)'
    )
    booleanParam(name: 'QuoteServices_STG', description: 'Run QuoteServices Tests in STG', defaultValue: false)
    booleanParam(name: 'QuoteServices_INT', description: 'Run QuoteServices Tests in INT', defaultValue: false)
    booleanParam(name: 'QuoteServices_V2_STG', description: 'Run QuoteServices V2 Tests in STG', defaultValue: false)
    booleanParam(name: 'QuoteServices_V2_INT', description: 'Run QuoteServices V2 Tests in INT', defaultValue: false)
    booleanParam(name: 'QuoteServices_V2_NZ_INT', description: 'Run QuoteServices V2 Tests for NZ in INT', defaultValue: false)
    booleanParam(name: 'QuoteServices_V2_NZ_STG', description: 'Run QuoteServices V2 Tests for NZ in STG', defaultValue: false)
    booleanParam(name: 'GetQuoteDetailsInternalv2_INT', description: 'RUN GetQuoteDetailsInternalv2 Tests in INT', defaultValue: false)
    booleanParam(name: 'CatalogExport_INT', description: 'RUN CatalogExport Tests in INT', defaultValue: false)
    booleanParam(name: 'PromotionsExport_INT', description: 'RUN PromotionsExport Tests in INT', defaultValue: false)
    booleanParam(name: 'Webhook_Notify_INT', description: 'Run Webhook Notification in INT', defaultValue: false)
    booleanParam(name: 'Webhook_Notify_STG', description: 'Run Webhook Notification in STG', defaultValue: false)
  }

  triggers {
    parameterizedCron(env.BRANCH_NAME == 'master' ? '''
        # run INT tests everyday at 5 AM PST
        0 5 * * * % Environment=INT;QuoteServices_INT=true;QuoteServices_V2_INT=true;QuoteServices_V2_NZ_INT=true;GetQuoteDetailsInternalv2_INT=true;
        
        # run STG tests everyday at 5 AM PST
        0 5 * * * % Environment=STG;QuoteServices_STG=true;QuoteServices_V2_STG=true;QuoteServices_V2_NZ_STG=true;
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
            isMasterBranch = true
            sh "docker build --tag ${imageName} ."
            vaultPath = "${params.Environment}" == 'STG' ? "aws-sts/des/ddws-stg/sts/Vault-Deployer" : "aws-sts/des/DDWS-UAT/sts/Vault-Deployer"
          }
        }
      }
      stage('Set Up Automation Tests') {
        agent {
          docker {
            image "${imageName}"
            reuseNode true
            args '-u root -v /tmp:/tmp'
          }
        }
        environment {
          LDAP = credentials('d88e9614-fb62-4a2a-a4ca-380277fdb498')
          VAULT_ADDR = 'https://vault.aws.autodesk.com'
          VAULT_PATH = "${vaultPath}"
        }
        steps {
          withCredentials([
            usernamePassword(credentialsId: 'pws-k6-influx-db-write-user',
              usernameVariable: 'INFLUX_DB_USERNAME',
              passwordVariable: 'INFLUX_DB_PASSWORD',
            )
          ]) {
            script {
              try {
                sh """
                echo "vaultPath-${vaultPath}"
                chmod 777 aws_auth 
                bash aws_auth
                echo ReadingFileInDocker
                cat /root/.aws/credentials
                chmod -R u+rwX,go+rX,go-w /root/.aws || true
                cat /root/.aws/credentials
                rm -rf /tmp/reports/
                """
                echo "TEST-START"
                def group = [:]
                allTests.eachWithIndex {
                  test, index ->
                  if (params[test.key]) {
                    paramsSelected = true
                    echo "Key: ${test.key}"
                    echo "value: ${test.value.path}"
                    def count = index +1
                    group["${test.key}"]= {
                      stage("${test.key}") {
                        sleep(count*20)
                        sh "mvn spring-boot:run -Dspring-boot.run.arguments='${test.value.path}'"
                      }
                    }
                  }
                }
                parallel group
                echo "TEST-END"
                if (paramsSelected) {
                stage('Send Test Report') {
                  sendReports(isMasterBranch) 
                }
                } else {
                    echo "No params selected"
                }
              } catch (err) {
                throw new Exception("Error: ${err}")
              }
            }
          }
        }
      }
    }
    post {
      always {
        script {
          echo ""
          sh "ls /tmp/"
          sh "docker image ls"
          sh "docker image rm -f ${imageName}"
        }
      }
    }
  }

  def sendReports(isMasterBranch) {
    script {
      echo "${isMasterBranch}"
      echo("Send Reports")
      def passCount = 0
      def failCount = 0

      dir('/tmp/reports') {
        def files = findFiles()

        if (!fileExists('test-reports')) {
         sh 'mkdir -p test-reports'
         }
         sh """
         pwd
         cp -f *.json test-reports
         chmod -R 777 /tmp/reports/
         """

        // zip zipFile: "PWSQuoteServices-reports.zip", dir: "test-reports"
        // UploadTestResults("PWSQuoteServices-reports.zip","JSON_LOG","PJPWS-42548", "${env.GIT_URL}".tokenize('/.')[-2], env.BRANCH_NAME, env.BUILD_NUMBER,"PJPWS")

        files.each {
          f ->
            echo "This is a directory: ${f.name}"
          def configJson = readJSON file: "/tmp/reports/${f.name}"
          def ENV_NAME = configJson.$ENV$ ? (configJson.$ENV$).toUpperCase() : null
          def TEST_STATUS = configJson.$TEST_STATUS$
          def TEST_NAME = (configJson.$TEST_NAME$).replaceAll(/(\s|\)|\(|%|Kicker.|.INT.json|.STG.json)/,"")
          def statusName = "pass"
          if (TEST_STATUS == "FAIL") {
            statusName = "fail"
            failCount = failCount + 1
          }else{
            passCount = passCount + 1
          }
          
          def SERVICE_NAME = configJson.$SERVICE_NAME$ ? configJson.$SERVICE_NAME$ : null
          def RESTAPI_CALL = JsonOutput.toJson(configJson.apiCalls)
          def API_RESPONSE = JsonOutput.toJson(configJson.responseChain)
          def API_EXP_RESPONSE = JsonOutput.toJson(configJson.expValidationChain)

          def TOTAL_VALIDATIONS = configJson.totalValidations ? configJson.totalValidations : 0
          def FAIL_VALIDATIONS = configJson.failValidations ? configJson.failValidations : 0
          def PASS_VALIDATIONS = configJson.passValidations ? configJson.passValidations : 0
          def TRANSACTION_ID = configJson.$TRANSACTION_ID$ ? configJson.$TRANSACTION_ID$ : "NA"
          def VALIDATION_ERROR = configJson.validationError ? JsonOutput.toJson(configJson.validationError) : null
          def VALIDATION_ERRORS = configJson.validationErrorsList ? JsonOutput.toJson(configJson.validationErrorsList) : null
          def COUNTRY = configJson.$COUNTRY$ ? configJson.$COUNTRY$ : null
          def TEST_DISPLAY_NAME = configJson.$TEST_DISPLAY_NAME$ ? configJson.$TEST_DISPLAY_NAME$ : null
          def TEST_STEPS = configJson.$TEST_STEPS$ ? configJson.$TEST_STEPS$ : null

          def jsonData = [
            "GIT_BRANCH": env.GIT_BRANCH,
            "BUILD_NUMBER": env.BUILD_NUMBER,
            "ENV_NAME": ENV_NAME,
            "TEST_STATUS": TEST_STATUS,
            "TEST_NAME": TEST_NAME,
            "SERVICE_NAME": SERVICE_NAME,
            "COUNTRY": COUNTRY,
            "TOTAL_VALIDATIONS": TOTAL_VALIDATIONS,
            "FAIL_VALIDATIONS": FAIL_VALIDATIONS,
            "PASS_VALIDATIONS": PASS_VALIDATIONS,
            "TRANSACTION_ID": TRANSACTION_ID,
            "VALIDATION_ERROR": VALIDATION_ERROR,
            "VALIDATION_ERRORS": VALIDATION_ERRORS,
            "TEST_DISPLAY_NAME": TEST_DISPLAY_NAME,
            "TEST_STEPS": TEST_STEPS,
          ]
          echo "${JsonOutput.toJson(jsonData)}"

          if(isMasterBranch) {
          String url = "https://calvinklein-7de56744.influxcloud.net:8086";
          String username = "$INFLUX_DB_USERNAME";
          String password = "$INFLUX_DB_PASSWORD";
          InfluxDB influxDB = InfluxDBFactory.connect(url, username, password);
          String databaseName = "dev-automation-test-results";
          String pointName = "dev_automation_test_report";

          Map<String, String> filters = new HashMap<>();
          tags.put("TEST_NAME", TEST_NAME);
          tags.put("ENV_NAME", ENV_NAME);
          tags.put("TEST_STATUS", TEST_STATUS);
          tags.put("BUILD", env.GIT_BRANCH + "-" + env.BUILD_NUMBER);
          tags.put("SERVICE_NAME", SERVICE_NAME);
          tags.put("COUNTRY", COUNTRY);
          tags.put("TEST_DISPLAY_NAME", TEST_DISPLAY_NAME);

          Map<String, Object> fields = new HashMap<>();
          fields.put("TEST_STEPS", TEST_STEPS);
          fields.put(statusName, 1); // Assuming statusName is a variable
          fields.put("TRANSACTION_ID", TRANSACTION_ID);
          fields.put("TOTAL_VALIDATIONS", TOTAL_VALIDATIONS);
          fields.put("PASS_VALIDATIONS", PASS_VALIDATIONS);
          fields.put("FAIL_VALIDATIONS", FAIL_VALIDATIONS);
          fields.put("API_RESPONSE", API_RESPONSE);
          fields.put("API_EXP_RESPONSE", API_EXP_RESPONSE);
          fields.put("RESTAPI_CALL", RESTAPI_CALL);
          fields.put("VALIDATION_ERROR", VALIDATION_ERROR);
          fields.put("VALIDATION_ERRORS", VALIDATION_ERRORS);

          Point point = Point.measurement(pointName)
              .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
              .tag(filters)
              .field(fields)
              .build();

          influxDB.write(point, databaseName);
          influxDB.close();
//             sh('curl -i -XPOST "https://calvinklein-7de56744.influxcloud.net:8086/write?db=k6&u=$INFLUX_DB_USERNAME&p=$INFLUX_DB_PASSWORD" --data-binary '+"""'dev_automation_test_report,TEST_NAME=$TEST_NAME,ENV_NAME=$ENV_NAME,TEST_STATUS=$TEST_STATUS,BUILD=$env.GIT_BRANCH-$env.BUILD_NUMBER,SERVICE_NAME=$SERVICE_NAME,COUNTRY=$COUNTRY,TEST_DISPLAY_NAME=$TEST_DISPLAY_NAME value=1,TEST_STEPS=$TEST_STEPS,$statusName=1,TRANSACTION_ID="${TRANSACTION_ID}",TOTAL_VALIDATIONS=$TOTAL_VALIDATIONS,PASS_VALIDATIONS=$PASS_VALIDATIONS,FAIL_VALIDATIONS=$FAIL_VALIDATIONS,API_RESPONSE="'''${API_RESPONSE}'''",API_EXP_RESPONSE="'''${API_EXP_RESPONSE}'''",RESTAPI_CALL="'''${RESTAPI_CALL}'''",VALIDATION_ERROR="'''${VALIDATION_ERROR}'''",VALIDATION_ERRORS="'''${VALIDATION_ERRORS}'''"'""")
          } else {
            echo "Skipping send test reports due to isMasterBranch=${isMasterBranch} "
          }
        }
      }
      echo "Total Tests: ${passCount + failCount}"
      echo "Pass Tests: ${passCount}"
      echo "Fail Tests: ${failCount}"
    }
  }
