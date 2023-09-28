package com.autodesk.pws.test.engine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.autodesk.pws.test.engine.Validator.ValidationContainer;
import com.autodesk.pws.test.processor.*;
import com.autodesk.pws.test.steps.base.*;
import com.autodesk.pws.test.utilities.StringUtils;
import com.autodesk.pws.test.workflow.*;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import io.restassured.path.json.JsonPath;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

////////////////////////////////////////////////////////
////////////////////////////////////////////////////////
////  TODO TODO TODO TODO TODO TODO TODO TODO TODO  ////
////   Kicker is WAAAY to friggin' big.  We really  ////
////   need to break it into smaller functional     ////
////   chunks.  Suggest that we start with the      ////
////   validation portion, especially considering   ////
////   that we'll be pulling in Malla's JsonAssert  ////
////   stuff.  We should preserve the current code  ////
////   as a separate class, but maybe make both     ////
////   validation approaches inherit from a common  ////
////   ancestor and let the ancestor determine how  ////
////   to proceed with validation based on a flag   ////
////   internal to the data or something....        ////
////////////////////////////////////////////////////////
////////////////////////////////////////////////////////

public class Kicker
{
    //  Prep DataPool...
    public DataPool DataPool;
    //  Prep total test count...
    private int testCountTotal = 0;
    //  Prep test result reporting container...
    private HashMap<String, String> allTests = new HashMap<String, String>();
    //  Get the system NewLine value...
    private final String newLine = System.getProperty("line.separator");
    //  Create a logger container...
	protected final Logger logger = LoggerFactory.getLogger(Kicker.class);
	//  Flag determining if logging should be duplicated to a file...
	protected Boolean logToFile = false;
	//  Container for the LogFileName template...
	protected String logFileNameTemplate = "";
	//  Container for the resolved LogFileName...
	protected String logFileName = "";
	//  Container for command line override values...
	private HashMap<String, String> cmdLineOverrides = new HashMap<String, String>();
	
    public int kickIt(String[] args)
    {
    	//  Ready the failure count, me maties!
        int failureCount = 0;
        
        //  Grab each argument passed in and
        //  process it accordingly...
        for (int i = 0; i < args.length; i++)
        {
        	String cmdArg = args[i];

        	//  Check to make sure this isn't some wacky "springboot" 
        	//  argument being passed just to mess with your head or
        	//  if it's a CommandLineOverride value that needs to be
        	//  stored for processing later...
        	if(!cmdArg.startsWith("--"))
        	{
	            //  Grab the test execution result...
	            int testExitCode = executeFileArguments(cmdArg);
	
	            //  Add it to the failure count...
	            failureCount += Math.abs(testExitCode);
        	}
        	else
        	{
        		cmdArg = cmdArg.substring(2);
        		
        		if(cmdArg.startsWith("{") && cmdArg.endsWith("}"))
				{
        			processsCmdLineOverrideArgs(cmdArg);
				}
        	}
        }

        //  Print out the name of the failed tests to make
        //  it easier to determine what the issues were
        //  if there was a long String of tests run...
        if (failureCount > 0)
        {
            LogIt(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
            LogIt("------------------------------------->    FAILED TESTS    <------------------------------------");
            allTests.forEach(
				    			(testName, status) ->
						        {
									if (status == "FAIL")
									{
									    LogIt("      " + testName);
									}
						        }
			        		);
            LogIt(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
        }

        //  Loop through all tests and report out results...
        allTests.forEach(
			    			(testName, status) ->
					        {
					        	LogIt("  " + testName + " -- " + status);
					        }
		        		);

        //  Log the total pass/fail stats...
        LogIt("(" + (testCountTotal - failureCount) + ") of (" + testCountTotal + ") PASSED.");

        //  Set the exit code and get the heck outta Dodge...
        return Math.abs(failureCount) * -1;
    }

    private void processsCmdLineOverrideArgs(String cmdArg) 
    {	
    	cmdArg = StringUtils.getBetween(cmdArg, "{", "}");
		String[] chunk = cmdArg.split(":");
		cmdLineOverrides.put(chunk[0], chunk[1]);
	}

	private void loadLocalConfig() 
    {
    	String configFilePath = "./testdata/WorkflowProcessing/TestData/Configurations/LocalConfig.json";
    	
    	String fullConfigPath = DynamicData.convertRelativePathToFullPath(configFilePath);
    	
    	File configFile = new File(fullConfigPath);
    	
    	if(configFile.exists())
    	{
    		loadFlatJsonAsDataPool(configFilePath);
    		
    		logToFile = Boolean.valueOf(DataPool.get("LogToFile").toString());
    		
    		logFileNameTemplate = DataPool.get("LogFileNameTemplate").toString();
    		logFileNameTemplate = logFileNameTemplate.replace("%", "$");
    		
			logFileName = DataPool.detokenizeDataPoolValues(logFileNameTemplate);

			SimpleScripter.DebugLoggingEnabled = Boolean.valueOf(DataPool.get("SimpleScriptDebugLoggingEnabled").toString());
			
			String fullLogFilePath = reportOutLogFileInfo();
			
			DataPool.add("$LOG_FILE_PATH$", fullLogFilePath);
			
			LogIt("LOG FILE PATH: " + fullLogFilePath);
			LogIt("SIMPLESCRIPT DEBUG: " + SimpleScripter.DebugLoggingEnabled);
    	}
    }

	private String reportOutLogFileInfo()
	{
		// Create a file object
        File f = new File(logFileName);

        // Get the absolute path of file f
        String absolutePath = f.getAbsolutePath();
        
        return absolutePath;
	}
    
	private int executeFileArguments(String filePathArgument)
    {
        int failureCount;

        //  File types are determined based on the PREFIX of the file, which
        //  I know is somewhat unusual, but utlimately it works here...
        String execType = getFilePrefix(filePathArgument);

        //  Determine how to process the file based on the file type...
        switch (execType.toUpperCase())
        {
            case "KICKER":
            case "KICKPACK":
                failureCount = executeKickerFile(filePathArgument);
                break;

            case "KICKERSUITE":
                failureCount = executeKickerSuite(filePathArgument);
                break;

            case "DIR":
            case "D":
            case "DIRECTORY":
            	failureCount = executeKickersInDirectory(filePathArgument);
            
            	
            default:
                LogIt("==================================================");
                LogIt("Unknown file type '" + execType + "' in '" + filePathArgument + "'!");
                LogIt("==================================================");
                failureCount = 1;
                break;
        }

        return failureCount;
    }
    
    private int executeKickersInDirectory(String kickerDirectory)
    {
    	//  Setup a failure count container...
    	int failureCount = 0;
    	
        // Creates an array in which we will store the names of files and directories
        String[] filePathNames;

        // Creates a new File instance by converting the given pathname string
        // into an abstract pathname
        File f = new File(kickerDirectory);

        // Populates the array with names of files and directories
        filePathNames = f.list();

        // Create an uppercase comparision container...
        String filePathUpper = "";
        
        // For each pathname in the pathnames array
        for (String filePath : filePathNames) 
        {
        	filePathUpper = filePath.toUpperCase();
          
        	//  If the file names STARTS WITH Kicker or KickerSuite AND ENDS WITH .json, then execute the kicker file...
        	if(filePathUpper.startsWith("KICKER.") && filePathUpper.endsWith(".JSON"))
        	{
        		failureCount += executeKickerFile(filePath);
        	}
        	else if(filePathUpper.startsWith("KICKERSUITE.") && filePathUpper.endsWith(".JSON"))
        	{
        		failureCount += executeKickerSuite(filePath);
        	}
        }
    	
    	return failureCount;
    }

    @SuppressWarnings("unchecked")
	private int executeKickerSuite(String kickerSuiteFilePath)
    {
        int failureCount = 0;

        //  Load up the kicker suite JSON file...
        String rawJson = DynamicData.loadJsonFile(kickerSuiteFilePath, false);
        //JsonPath kickerSuite = DynamicData.loadJsonFileToJsonPath(kickerSuiteFilePath);
        JsonPath kickerSuite = JsonPath.from(rawJson);

        //  Start looping through all the kicker suite items...
        Map<String, Object>  kickerKeyVals = kickerSuite.getMap("");
        ArrayList<String> kickerFiles = new ArrayList<String>();

        kickerKeyVals.
			forEach(
						(k, v) ->
						{
							if(k.toString().contentEquals("KickerFiles"))
							{
								ArrayList<String> fileList = (ArrayList<String>) v;
								fileList.forEach( (filePath) -> kickerFiles.add(filePath));
							}
						}
					);

        for (int i = 0; i < kickerFiles.size(); i++)
        {
        	String kickerFilePath = kickerFiles.get(i);
        	failureCount += processKickerFileFromKickerSuite(kickerFilePath);
        }

        return failureCount;
    }

    private int processKickerFileFromKickerSuite(String kickerFilePath)
    {
	    //  If this ais a multi-file run, we'll need new runtime data
	    //  for each pass.  We'll force new data generation here...
	    DynamicData.generateNewRuntimeValues();

	    //  Strip out just the test name from the full file path and name...
	    Path pathObj = Paths.get(kickerFilePath);
	    String testName = pathObj.getFileName().toString();
	    testName = FilenameUtils.removeExtension(testName);

	    //  Assume the test is going to fail...
	    int testExitCode = -1;

	    try
	    {
	    	//  Grab the exit code of the execution...
	    	testExitCode = executeFileArguments(kickerFilePath);
	    }
	    catch (Exception ex)
	    {
	    	//  Deal with the "uh-oh"...
	    	LogIt("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
	    	LogIt("Exception during '" + testName + "'!");
	    	LogIt(ex.toString());
	    	LogIt("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
	    }

	    //  Add the result to the test results list based on the exit code...
	    if (testExitCode != 0)
	    {
	    	allTests.put(testName, "FAIL");
	    }
	    else
	    {
	    	allTests.put(testName, "PASS");
	    }

	    return testExitCode;
    }

    private String getFilePrefix(String filePath)
    {
    	//  https://www.baeldung.com/java-filename-without-extension
        Path pathObj = Paths.get(filePath);
        String prefix = pathObj.getFileName().toString();
		int dotIndex = prefix.indexOf('.');
		prefix =  (dotIndex == -1) ? prefix : prefix.substring(0, dotIndex);
		return prefix;
    }

    private String getCurrentTime()
    {
        //Get current date time
        LocalDateTime now = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String formatDateTime = now.format(formatter);

        return formatDateTime;
    }
    
    private int executeKickerFile(String kickerFilePath)
    {
        //  Setup the default return exit code value...
        int exitCode = 0;

        //  Increment the testCountTotal tracker...
        testCountTotal += 1;

        //  Force a reset to the DataPool in case it contains
        //  data from a previous execution...
        DataPool = new DataPool();

        //  Initialize the DynamicData runtime values...
        DynamicData.initRuntimeValues();

        //  Initialize the DataPool runtime values...
        initDataPoolRuntimeValues();

        //  Introduce yourself!
        File testFile = new File(kickerFilePath);
        
        String testName = testFile.getName();
        DataPool.add("$TEST_NAME$", testName);
        
        //  Load in LocalConfig info, if it exists.
        //  Because this method ALSO determines if log entries
        //  are sent to a file, we need to run it before we 
        //  do any extensive logging or we'll miss some of our
        //  logging stuff...
        loadLocalConfig();
        
        //  Shove the execution path into the global DataPool
        //  cuz it's likely we'll be needing for some reason later...
        setExecutionPathVariable();
        
        //  Log the test start time...
        LogIt("====================================================");
        LogIt("Test start time: " + getCurrentTime());
        LogIt("====================================================");
        LogIt("  ");
        
        LogIt("Test file name: " + testName);
        DataPool.add("TestFileName", testName);
        
        LogIt("Full file path: " + kickerFilePath);
        DataPool.add("FullKickerFilePath", kickerFilePath);
               
        //  Load in test params as DataPool data...
        loadTestKickerAsDataPoolData(kickerFilePath);
        
        //  Set and/or add any command line override arguments into
        //  the datapool that were passed in at the start of all this...
        mergeCmdLineOverridesIntoDataPool();
        
        //  Now detokenize any token references in the values of the DataPool...
        detokenizeDataPool();
        
        //  Load any "secretsFile" references in the DataPool...
        loadSecretsFilesFromDataPool();
        
        //  Now detokenize any token references that came in with the SecretsFile...
        detokenizeDataPool();
        
        //  Load the WorkflowProcessingEngine...
        WorkflowProcessingEngine workflowProcEngine = new WorkflowProcessingEngine();

        //  Set WPE DataPool reference...
        
        workflowProcEngine.DataPool = DataPool;
        
        //  Load the workflow steps...
        //List<StepBase> workflow = loadWorkflow(testKicker.getString("workflow").toString());
        List<StepBase> workflow = loadWorkflow(DataPool.get("workflow").toString());

        //  Prepare a 'validationResults' container...
        HashMap<String, Object> validationResults = new HashMap<String, Object>();

        //  Prep a flag to mark if the workflow succesfully completed...
        Boolean workflowCompleted = false;
        
        //  Prep a flag to mark if the validations succesfully completed...
        Boolean validationsCompleted = false;
        
        //  Prep a flag to check for forced validations...
        Boolean forceValidationsIfWorkflowIncomplete = false;
        
        try
        {
        	//  Set the logToFile flag on the WPE...
        	workflowProcEngine.setLogToFile(logToFile, logFileName, testName);
        	
        	//  Assume the test fails unless it's specifically
        	//  marked with "PASS" result...
        	DataPool.addAsDefault("$TEST_STATUS$", "FAIL");
        	
        	//  Record the JuiceBox log...
        	logJuiceBoxTestInfo(testName, workflow);
        	
            //  Execute the workflow steps...
            workflowCompleted = workflowProcEngine.execute(workflow, testName);

            LogIt("  ");
            LogIt("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

            //Add Expected Validations in DataPool
			String validationFilePath = DataPool.getRaw("validationFile").toString();
			String validatorRawJson = DataPool.loadJsonFile(validationFilePath);
			JsonObject jsonObject = JsonParser.parseString(validatorRawJson).getAsJsonObject();
			DataPool.add("expValidationChain",jsonObject);

            String forceValidations = (String)DataPool.get("forceValidationsIfWorkflowIncomplete");
            
            if(forceValidations != null && forceValidations.equalsIgnoreCase("true"))
            {
            	forceValidationsIfWorkflowIncomplete = true;
            }
            
            if(workflowCompleted || forceValidationsIfWorkflowIncomplete)
            {
            	//  Create a Validator...
            	Validator validator = new Validator(this);
            	
            	ValidationContainer validationContainer = validator.Validate();
            	
            	if(workflowCompleted)
            	{
            		exitCode = validationContainer.ExitCode;
            	}
            	else
            	{
            		exitCode = -1;
            	}
            	
            	validationsCompleted = validationContainer.ValidationsCompleted;
            	validationResults = validationContainer.ValidationResults;
            }
            else
            {
	            LogIt("Workflow failed to complete.  Skipping validations.");
	            exitCode = -1;
            }
        }
        catch (Exception ex)
        {
        	//  Dump all the DataPool data...
        	dumpDataPool(ex);

            //  Set the Exit Code..
            exitCode = -1;
        }

        
        //  Check to see if the workflow actually completed.
        if((workflowCompleted && validationsCompleted) || forceValidationsIfWorkflowIncomplete)
        {
            //  If the workflow did complete, we're going 
        	//  to report out all the validation stuff...
        	int validationCount = (int) validationResults.get("ValidationCount");
	        int failCount = (int) validationResults.get("FailCount");
	        int passCount =(int)  validationResults.get("PassCount");
	
	        LogIt("  ");
	        LogIt("-----------------------------------------------------------------------------------------------");
	        LogIt("-----------------------------------------------------------------------------------------------");
	        LogIt("-----------------------------------   VALIDATION RESULTS   ------------------------------------");
	        LogIt("Validation Total: " + validationCount);
	        LogIt("Failure Count:    " + failCount);
	        LogIt("Pass Count:       " + passCount);
	
	        if(failCount > 0)
	        {
	        	dumpValidationList(validationResults.get("ValidationList"));
	        }
	
	        LogIt("-----------------------------------------------------------------------------------------------");
	        LogIt("-----------------------------------------------------------------------------------------------");
	
	        if ((validationResults.size() == 0 || ((int) validationResults.get("FailCount")) > 0))
	        {
	            exitCode = -1;
	        }
	
	        logTestEndTime();
	
	        if(failCount > 0)
	        {
	        	dumpDataPool();
	        }
        }
        else
        {
        	logTestEndTime();
        	//  Looks like the workflow didn't complete succesfully.
        	//  Better do a data pool dump...
        	dumpDataPool();
        }
        
        if(exitCode == 0)
        {
        	DataPool.add("$TEST_STATUS$", "PASS");
        }
        
		exportDataPoolToJson(logToFile, logFileName);
        //  Log the JuiceBox test results...
        logJuiceBoxResultInfo();
        
        return exitCode;
    }
    
    private void logJuiceBoxTestInfo(String currentTestName, List<StepBase> currentWorkflow) 
    {
    	//  Set the DataPool value for the $TEST_NAME$...
	    DataPool.addAsDefault("$TEST_NAME$", currentTestName);
    	
	    //  Prepare a quoted, comma delimited list of workflow steps...
	    ArrayList<String> workflowList = new ArrayList<String>();
	    
	    // Prep a step container for use during looping...
	    StepBase step = null;
	    
	    String stepName = "";
	    
		// Loop through each step in order...
		for (int i = 0; i < currentWorkflow.size(); i++) 
		{
			// Grab the next step...
			step = currentWorkflow.get(i);
			
			// Grab the next step...
			stepName = step.getClass().getSimpleName();
			workflowList.add("\"" + stepName + "\"");
		}
	    
	    String csvWorkflowList = String.join(",", workflowList);
	    
	    DataPool.add("$QUOTED_CSV_WORKFLOW$", csvWorkflowList);
	    
	    //  Prepare the JuiceBox raw JSON "container"...
	    String rawJuiceBox = "$juiceBox->{\"juiceBox\":{\"juiceBoxType\":\"testInfo\",\"TestName\":\"$TEST_NAME$\",\"Workflow\":[$QUOTED_CSV_WORKFLOW$],\"Description\":\"$TEST_DESCRIPTION$\",\"testId\":\"$TEST_ID$\",\"runtimeId\": \"$RUNTIME_ID$\"}}<-juiceBox$";

	    DataPool.addAsDefault("$TEST_ID$", "???");
	    DataPool.addAsDefault("$TEST_DESCRIPTION$", "");
	    
	    //  Swap in the appropriate token values...
	    rawJuiceBox = DataPool.detokenizeDataPoolValues(rawJuiceBox);
		
	    //  LOG IT!!
	    LogIt(rawJuiceBox);
	}

    private void logJuiceBoxResultInfo()
    {
    //  Prepare the JuiceBox raw JSON "container"...
	    String rawJuiceBox = "$juiceBox->{\"juiceBox\":{\"juiceBoxType\":\"testResults\",\"TestName\":\"$TEST_NAME$\",\"TestLogPath\":\"$LOG_FILE_PATH$\",\"TestStatus\":\"$TEST_STATUS$\",\"testId\":\"$TEST_ID$\",\"runtimeId\": \"$RUNTIME_ID$\"}}<-juiceBox$";
	    
	    //  Swap in the appropriate token values...
	    rawJuiceBox = DataPool.detokenizeDataPoolValues(rawJuiceBox);
		
	    //  LOG IT!!
	    LogIt(rawJuiceBox);
    }

	private void mergeCmdLineOverridesIntoDataPool() 
	{
		//  Merges command line override  
		//  values into DataPool...
    	LogIt("Merging command line override values...");
    	
		cmdLineOverrides.
			forEach(
						(keyName, value) ->
						{							
							DataPool.add(keyName, value);
						}
					);
	}

	private void exportDataPoolToJson(Boolean logToFile, String logFileName) 
    {
		var dataPoolDump = DataPool.toRawJson();
		var validationChainDump = DataPool.validationChainToRawJson();
		
		if(logToFile)
		{	
			try 
			{
				FileUtils.writeStringToFile(new File(logFileName + ".DataPool.json"), dataPoolDump, Charset.defaultCharset());
				FileUtils.writeStringToFile(new File("/tmp/reports/"+logFileName + ".json"), dataPoolDump, Charset.defaultCharset());
				FileUtils.writeStringToFile(new File(logFileName + ".ValidationChain.json"), validationChainDump, Charset.defaultCharset());
			} 
			catch (Exception e) 
			{
				LogErr(e, "exportDataPoolToJson");
			}
		}
	}

	public void LogErr(Exception e, String methodName)	
	{
		// TODO Auto-generated catch block
		var x = e.getStackTrace();
		
		LogIt("Error in method! -- " + methodName);
		
		for (int i=0; i<x.length; i++) 
		{ 
		    Object y = x[i];
		    this.LogIt(y.toString());
		}
	}
		
	private void initDataPoolRuntimeValues() 
    {
		String strDate = DynamicData.getSimpleDatTimeFormat();
        DataPool.add("$FULL_DATE_TIME$", strDate);
        DataPool.add("$RUNTIME_ID$", SimpleScripter.CreateUniqueHexTimestamp());
	}

	private void logTestEndTime() 
    {
        //  Log the test end time...
        LogIt("====================================================");
        LogIt("Test end time: " + getCurrentTime());
        LogIt("===================================================="); // + newLine);
        LogIt("  ");
    }

	private void loadSecretsFilesFromDataPool() 
    {
    	//  Prep a container for the list of secretsFiles we may discover...
    	ArrayList<String> secretsFilesToLoad = new ArrayList<String>();
    	
    	//  We'll have to add any secretsFiles we discover in the array
    	//  above because Java will throw a concurrency error if we attempt
    	//  to operate on the DataPool while it's being iterated over...
        DataPool.
			forEach(
						(keyName, value) ->
						{							
							//  If we find a "secretsFile", add it to the list...
					        if (keyName.contains("secretsFile"))
					        {
					        	secretsFilesToLoad.add(value.toString());
					        }
						}
					);
        
        //  Iterate over the secrets files list...
        secretsFilesToLoad
        	.forEach(
				        //  If we find a "secretsFile" key, load up the path
				        //  like it was any other "testKicker" file.  This
						//  will result in the contents of the secretsFile
						//  simply being loaded into the DataPool as keyVals...
        				secretsFile -> loadTestKickerAsDataPoolData(secretsFile, false)
        			);
	}

	private void detokenizeDataPool() 
    {
        DataPool.
			forEach(
						(key, value) ->
						{
					        //  Grab the value in the pair...
							String detokenizedValue = value.toString();
							
							//  Detokenize the value (this is forward only)...
							detokenizedValue = DataPool.detokenizeDataPoolValues(detokenizedValue);
							
							//  Resolve any embedded simple script items...
							detokenizedValue = DynamicData.simpleScriptEval(detokenizedValue);
							
							//  If the value has changed after the above
							//  treatments, reset the value for the key... 							
							if(detokenizedValue != value.toString())
							{
								DataPool.add(key, detokenizedValue);
							}
						}
					);
	}

	private void dumpDataPool()
    {
    	dumpDataPool(null);
    }

    private void dumpDataPool(Exception ex)
    {
    	if(ex != null)
    	{
    		LogIt(ex.toString());
    	}
        LogIt("===  DATA POOL DUMP  ===");
        
        String[] dataPoolDump = DataPool.dumpDataPool().split("\\r?\\n");
        
        for(String line: dataPoolDump)
        {
            LogIt(line);        	
        }

//        logIt("=====  VALIDATION CHAIN DUMP  =====");
//        logIt(dumpValidationChain());
        LogIt("Test Failed!");
    }

    @SuppressWarnings("unchecked")
	private void dumpValidationList(Object validationListObj)
    {
    	HashMap<String, Object> validationlist = (HashMap<String, Object>) validationListObj;

    	//  Loop through all the items in the Validation List...
    	validationlist.
    		forEach(
					(k, v) ->
					{
					    {
					    	LogIt("=================================================");
					    	LogIt("  ");
					    	LogIt("Validation section: " + k);
					    	HashMap<String, Object> validationItemsList = (HashMap<String, Object>) v;
					    	if(validationItemsList != null)
					    	{
					    		Object validationList = validationItemsList.get("ValidationList");
					    		
					    		if(validationList != null)
				    			{
					    			dumpValidationItem(validationList);
				    			}
					    		
					    		Object generalError = validationItemsList.get("GeneralError");
					    		
					    		if(generalError != null)
				    			{
					    			LogIt("  -> " + (String)generalError);
				    			}
					    	}
					    }
					}
    			   );
	}

    @SuppressWarnings("unchecked")
	private void dumpValidationItem(Object validationItemsListObj)
    {
    	HashMap<String, Object> validationItemslist = (HashMap<String, Object>) validationItemsListObj;

    	//  Loop through all the items in the Validation List...
    	validationItemslist.
    		forEach(
					(k, v) ->
						{
						    {
						    	HashMap<String, Object> validationItemDetails = (HashMap<String, Object>) v;
	
						    	String testResult = validationItemDetails.get("TestResult").toString();
	
						    	if(testResult != "PASS")
						    	{
							    	LogIt("-----------");
							    	LogIt("  ");
							    	LogIt("Test Path:      " + k);
							    	LogIt("Expected value: " + validationItemDetails.get("ExpectedValue"));
							    	LogIt("Actual value:   " + validationItemDetails.get("ActualValue"));
							    	LogIt("Test Result:    " + validationItemDetails.get("TestResult"));
						    	}
						    }
						}
    			   );
    }

	private void setExecutionPathVariable()
    {
    	// FROM:
    	//      https://stackoverflow.com/questions/17939556/how-to-get-the-execution-directory-path-in-java
    	String absolute = getClass().getProtectionDomain().getCodeSource().getLocation().toExternalForm();
    	String os = System.getProperty("os.name");
    	if (os.indexOf("Windows") != -1)
    	{
    		absolute = absolute.replace("/", "\\\\");
    	    if (absolute.indexOf("file:\\\\") != -1)
    	    {
    	    	absolute = absolute.replace("file:\\\\", "");
    	    }
    	}
    	else if (absolute.indexOf("file:") != -1)
    	{
    		absolute = absolute.replace("file:", "");
    	}

    	DataPool.add("ExecutionPath", absolute);

    	LogIt("ExecutionPath set to: " + absolute);
    }

    @SuppressWarnings({ "unchecked", "unused" })
	private String dumpValidationChain()
    {
    	//  Ready a container to hold the return value...
        StringBuilder sb = new StringBuilder();

        //  Convert the DataPool's "ValidationChain" object into a first class object...
        HashMap<String, Object> validationChain = (HashMap<String, Object>) DataPool.get("ValidationChain");

        //  If the ValidationChain object isn't null, then...
        if(validationChain != null)
        {
	    	//  Loop through all the items in the 'testKicker' object...
	        validationChain.
	    		forEach(
						(chainName, value) ->
						{
						    //foreach (var validationItem in (dataPool["ValidationChain"] as Dictionary<string, object>))
						    {
						        sb.append("=========================================" + newLine);
						        sb.append("  -- " + chainName + " --  " + newLine);
						        //  sb.AppendLine(JsonConvert.SerializeObject(validationItem.Value, Formatting.Indented));
						    }
						}
	    			   );

        }

        return sb.toString();
    }

    private List<StepBase> loadWorkflow(String workflowName)
    {
    	//  Make an instance of the "Workflows" class...
		Workflows workflows = new Workflows();

		//  Create a return container for the WorkflowSteps...
		List<StepBase> workflowSteps = null;

		try
		{
			//  Try and grab the steps based on the name passed in...
			workflowSteps = workflows.getWorkflowByName(workflowName);
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//  Handle it when there's no workflow steps found...
        if (workflowSteps == null)
        {
            //throw new Exception($"Unable to locate named workflow -- '{workflowName}'!");
        }
        else
        {
            LogIt("Found workflow for '" + workflowName + "'.");
        }

        return workflowSteps;
    }
/*
    @SuppressWarnings("unchecked")
	private HashMap<String, Object> doValidations(String validationFilePath) throws Exception
    {
        //  Prepare the full results ValidationResults container...
    	HashMap<String, Object> validationResults = new HashMap<String, Object>();

        //  Group counting indexes...
        int totalPassCount = 0;
        int totalFailCount = 0;
        int totalTestCount = 0;

        //  Grab the ValidationChain...
        LogIt("      --> Grabbing ValidationChain from DataPool...");
        HashMap<String, Object> validationChain = (HashMap<String, Object>) DataPool.get("ValidationChain");
        LogIt("      --> (" + validationChain.size() + ") entries in ValidationChain...");

        //  Check to see if there's a validation chain to do validation against...
        if (validationChain != null)
        {
            // Prep a locally scoped loop contextualized ValidationResults container...
        	HashMap<String, Object> validationResultCollection = new HashMap<String, Object>();

        	//  Check to make sure that sucker ain't empty...
            if (validationChain.size() == 0)
            {
                throw new Exception("Validation Chain is empty!  Is there a configuration issue?");
            }

            //  Grab all the validator commands from the validator file...
            String validatorRawJson = DataPool.loadJsonFile(validationFilePath);
        	JsonPath validator = JsonPath.from(validatorRawJson);
        	
            //  Get the length, which we'll test to make sure it's long
            //  enough to actually contain a validator... 
            int validatorRawLength = validatorRawJson.length();

            //  If the validator is long enough, then...
            if(validatorRawLength > 8)
            {
            	//  Get the root of the json in the validator (it'll be an array)...
	            Map<String, Object> validatorKeyVals = validator.getMap(".");

	            //  Grab the list of all the validator sections/items...
	            Object[] validatorKeys = validatorKeyVals.keySet().toArray();

	            //  Start looping through the various validation sections...
	            for(int i = 0; i < validatorKeys.length; i++)
	            {
	            	//  Grab the sectionKey/name we need to reference...
	            	Object sectionKey = validatorKeys[i];
	            	
	            	//  Log it for manual post-analysis...
	            	LogIt("  -- " + sectionKey.toString());
	            	
	            	//  Grab the validatorKeyVal for the section...
	            	Object section = validatorKeyVals.get(sectionKey);

	                //  Execute the validations against the appropriate 
	            	//  part of the execution results...
	            	HashMap<String, Object> newValidationResult = executeValidator(sectionKey.toString(), section);

	            	//  Stuff the results into a validationResults collection...
	                validationResultCollection.put(sectionKey.toString(), newValidationResult);

	                //  Increment the test total counters as necessary...
	                totalTestCount += Integer.parseInt(newValidationResult.get("ValidationCount").toString());
	                totalFailCount += Integer.parseInt(newValidationResult.get("FailCount").toString());
	            }
            }
            //  Calculate the total passing tests...
            totalPassCount = totalTestCount - totalFailCount;

            //  Fill out the loop scope validation results container...
            validationResults.put("ValidationList", validationResultCollection);
            validationResults.put("ValidationCount", totalTestCount);
            validationResults.put("PassCount", totalPassCount);
            validationResults.put("FailCount", totalFailCount);
        }

        return validationResults;
    }

    @SuppressWarnings("unchecked")
	private HashMap<String, Object> executeValidator(String sectionName, Object validatorSection)
    {
        //  Setup our validation results container...
        HashMap<String, Object> validationResults = new HashMap<String, Object>();

        //  Check to make sure the validator section isn't empty...
        if (validatorSection != null)
        {
            //  Convert the validation commands to a loopable list
        	HashMap<String, Object> validationList = (HashMap<String, Object>) validatorSection;

            int validationCount = 0;
            int failCount = 0;

            //  Convert the validation list to an array of Keys...
            Object[] validatorListKeys = validationList.keySet().toArray();

            //  Grab the validation chain section and token path to be tested...
            String chainSecionRawJson = DataPool.getValidationChainItem(sectionName).toString();
        	JsonPath chainSection = JsonPath.with(chainSecionRawJson);

            //  Start looping through the validation items...
            for(int i = 0; i < validatorListKeys.length; i++)
            {
                validationCount += 1;

                //  Get a test results container ready...
                HashMap<String, String> testResults = new HashMap<String, String>();

                //  Grab the Json path that needs to be tested...
                String pathToTest = validatorListKeys[i].toString();
                //logIt("ValidationPath: " + pathToTest);                
                
                //  Grab the last *expected* value we want to compare it to...
                String expectedValue = validationList.get(pathToTest).toString();
                //logIt("ExpectedValue:  " + expectedValue);
                
                if(expectedValue == "null")
                {
                	expectedValue = null;
                }
                
                //  Set the default value of 'valueToTest' in case the;
                //  path doesn't exist...
                String actualValue = "--> The target path doesn't exist! <--";

                //  Grab the chain section version of the path as an object...
                //
                //  NOTE: I think a library must have been updated, because
                //        if you try to "chainSection.get()" section that doesn't
                //        exist, Java throws a "null exception" or some such
                //        craziness.  So we set the value null by default and then 
                //        try to grab the value, but catch it if it errs out.
                //        At some point this should be wrapped into an external
                //        function rather than testing for that condition inline...
                Object actualValueObj = null;
                
                try
                {
                	actualValueObj = chainSection.get(pathToTest);
                }
                catch (Exception e)
                {
                	//  do nothing as this means there was a null value referenced
                	//  and the actualValue is therefore "missing"...
                }
                
                //  Check to see if the path token actually exists...
                if (actualValueObj != null)
                {
                    //  Grab the value from the path token...
                	actualValue = actualValueObj.toString();
                	if(actualValue.startsWith("[") && actualValue.endsWith("]")) 
                	{
                		actualValue = actualValue.substring(1, actualValue.length() - 1);
                	}
                }

                //  Here we'll detokenize and resolve any Runtime and DataPool
                //  values that exist in the validation data...
                expectedValue = DynamicData.detokenizeRuntimeValues(expectedValue);
                expectedValue = DataPool.detokenizeDataPoolValues(expectedValue);
                
                //  Here we'll resolve any SimpleScript fragments that exist in 
                //  the validation data...
                //expectedValue = SimpleScripter.extractAndResolveSimpleScripts(expectedValue, "[[", "]]");
                expectedValue = DynamicData.simpleScriptEval(expectedValue);
                
                //  Convert the wildcard/plainstring expected value
                //  to a regular expression...
                expectedValue = DynamicData.wildcardToRegex(expectedValue);

                //  Do the actual validation and record the results...
                if (actualValue.matches(expectedValue) && !actualValue.matches("--> The target path doesn't exist! <--"))
                {
                    testResults.put("TestResult", "PASS");
                }
                else
                {
                    testResults.put("TestResult", "FAIL");
                    failCount += 1;
                }

                //  Fill out the test results container with the data we
                //  have collected during validation...
                testResults.put("ValidationPath", pathToTest);
                testResults.put("ExpectedValue", expectedValue);
                testResults.put("ActualValue", actualValue);
                
                //  Keep these lines, but uncomment when debugging validation items...
                //logIt("ValidationPath: " + pathToTest);
                //logIt("ExpectedValue:  " + expectedValue);
                //logIt("ActualValue:    " + actualValue);
                
                validationList.put(validatorListKeys[i].toString(), testResults);
            }

            //  Group all test results for summmary...
            validationResults.put("ValidationList", validationList);
            validationResults.put("ValidationCount", validationCount);
            validationResults.put("PassCount", validationCount - failCount);
            validationResults.put("FailCount", failCount);
        }
        else
        {
            //  Fill out the test results container with sadness...
            validationResults.put("GeneralError", "No '{validatorSection.Key}' validator data found!  Check for configuration errors...");
            validationResults.put("ValidationCount", 0);
            validationResults.put("FailCount", 0);
            validationResults.put("PassCount", 0);
            validationResults.put("ValidationList", null);
        }

        return validationResults;
    }
*/

    private void loadTestKickerAsDataPoolData(String testKickerFilePath)
    {
    	loadTestKickerAsDataPoolData(testKickerFilePath, true);
    }

    private void loadTestKickerAsDataPoolData(String testKickerFilePath, Boolean logValues)
    {
    	loadFlatJsonAsDataPool(testKickerFilePath, logValues);
    }
    
    private void loadFlatJsonAsDataPool(String jsonFilePath)
    {
    	loadFlatJsonAsDataPool(jsonFilePath, true);
    }
    
    private void loadFlatJsonAsDataPool(String jsonFilePath, Boolean logValues)
    {
    	//  Grab the raw JSON data from the file...
    	String rawJson = DynamicData.loadJsonFile(jsonFilePath);

    	//  Grab a dictionary of all the first-level elements...
    	Map<String, Object> testKickerKeyVals = new Gson().fromJson(rawJson, new TypeToken<HashMap<String, Object>>() {}.getType());

    	//  Loop through all the items in the 'testKicker' object...
    	testKickerKeyVals.
    		forEach(
	    				(keyName, value) ->
		    				{
		    					if(value instanceof LinkedTreeMap<?,?>)
		    					{
					                //  Add item to the DataPool as a keyname/rawJson string...
		    						LinkedTreeMap<?,?> linkTree = (LinkedTreeMap<?,?>) value;
									String jsonString = ConvertLinkedTreeMapToRawJson(linkTree);
					                DataPool.put(keyName, jsonString);	
		    					}
		    					else
		    					{
					                //  Add item to the DataPool as key-val...
					                DataPool.put(keyName, value.toString());
		    					}
		    					
				                if(logValues)
				                {
				                	LogIt("   --> " + keyName + ": " + value.toString());
				                }
				                else
				                {
				                	LogIt("   --> " + keyName + ": *****************");
				                }
		    					
			                }
				    );
    }

    public String ConvertLinkedTreeMapToRawJson(LinkedTreeMap<?, ?> linkedTreeMap)
    {
		HashMap<String, Object> hashMappedVals = convertLinkedTreeMapToHashMap(linkedTreeMap);
		
		//Deep clone
		Gson gson = new Gson();
		String jsonString = gson.toJson(hashMappedVals);
		
		return jsonString;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public HashMap<String, Object> convertLinkedTreeMapToHashMap(LinkedTreeMap<?, ?> linkedTreeMap) 
    {
        HashMap<String, Object> retVal = new HashMap<String, Object>();
        
        Object[] objs = linkedTreeMap.entrySet().toArray();
        
        for (int l=0;l<objs.length;l++)
        {
            Map.Entry o= (Map.Entry) objs[l];
            try 
            {
                if (o.getValue() instanceof LinkedTreeMap)
                {
                	LinkedTreeMap<String, Object> linkTreeVal =	(LinkedTreeMap<String, Object>) o.getValue();
                	HashMap<String, Object> nodeVal = convertLinkedTreeMapToHashMap(linkTreeVal);
                	retVal.put(o.getKey().toString(), nodeVal);
                }
                else
                {
                	retVal.put(o.getKey().toString(),o.getValue());
                }
            } 
            catch (Exception e) 
            {
                e.printStackTrace();
            }
        }
        
        return retVal;
    }
    
    public void LogIt(String msg)
    {
        //  Strip out any "overformatting" that
        //  may have been passed in and replace
        //  it with an intelligent and readable
        //  option for the logging message...
        msg = msg.replace("\\r", "\r");
        msg = msg.replace("\\n", "\n");
        msg = msg.replace("\\\"", "\"");
        //System.out.println(msg);
        // Console.WriteLine(msg);
        logger.info(msg);
        
        if(logToFile)
        {
        	logToFile(msg);
        }
    }
    
    //  This method is duplicated in the StepBase.java class and
    //  needs to be put into a separate class (Global so that 
    //  all components can access it as necessary?  Pass an 
    //  instantiated class as an argument?  I dunno...)
    private void logToFile(String line) 
    {
    	try 
    	{
    	    FileWriter fw = new FileWriter(logFileName, true);
    	    BufferedWriter bw = new BufferedWriter(fw);
    	    bw.write(line + newLine);
    	    bw.close();
	    } 
    	catch (Exception e) 
    	{
			System.out.println("An error occurred in 'logToFile'!");
			e.printStackTrace();
		}
	}
}

