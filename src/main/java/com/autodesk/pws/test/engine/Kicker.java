package com.autodesk.pws.test.engine;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.autodesk.pws.test.processor.*;
import com.autodesk.pws.test.steps.base.*;
import com.autodesk.pws.test.workflow.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.restassured.path.json.JsonPath;

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
    private DataPool dataPool;
    //  Prep total test count...
    private int testCountTotal = 0;
    //  Prep test result reporting container...
    private HashMap<String, String> allTests = new HashMap<String, String>();
    //  Get the system NewLine value...
    private final String newLine = System.getProperty("line.separator");
    //  Create a logger container...
	protected final Logger logger = LoggerFactory.getLogger(Kicker.class);

	//  TODO: Figure out if we're actually going to need this kind of functionality
	//        or not.  Pretty sure we won't, but leaving it for now until a final
	//        confirmation has been made...
//    private Map<String, String> getCommandLineArgsFromEnvironment()
//    {
//        Map<String, String> env = System.getenv();
//
//        for (String envName : env.keySet())
//        {
//            System.out.format("%s=%s%n", envName, env.get(envName));
//        }
//
//        return env;
//    }

    public int kickIt(String[] args)
    {
    	//  Because TestNg, that's why!
        //args = (String[]) getCommandLineArgsFromEnvironment().keySet().toArray();

    	//  Ready the failure count, me maties!
        int failureCount = 0;

        //  Grab each filename passed in and
        //  process it accordingly...
        // foreach (String fileArg in args)
        for (int i = 0; i < args.length; i++)
        {
        	String fileArg = args[i];

        	//  Check to make sure this isn't some wacky "springboot" 
        	//  argument being passed just to mess with your head...
        	if(!fileArg.startsWith("--"))
        	{
	            //  Grab the test execution result...
	            int testExitCode = executeFileArguments(fileArg);
	
	            //  Add it to the failure count...
	            failureCount += Math.abs(testExitCode);
        	}
        }

        //  Print out the name of the failed tests to make
        //  it easier to determine what the issues were
        //  if there was a long String of tests run...
        if (failureCount > 0)
        {
            logIt(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
            logIt("------------------------------------->    FAILED TESTS    <------------------------------------");
            allTests.forEach(
				    			(testName, status) ->
						        {
									if (status == "FAIL")
									{
									    logIt("      " + testName);
									}
						        }
			        		);
            logIt(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
        }

        //  Loop through all tests and report out results...
        allTests.forEach(
			    			(testName, status) ->
					        {
					        	logIt("  " + testName + " -- " + status);
					        }
		        		);

        //  Log the total pass/fail stats...
        logIt("(" + (testCountTotal - failureCount) + ") of (" + testCountTotal + ") PASSED.");

        //  Set the exit code and get the heck outta Dodge...
        return Math.abs(failureCount) * -1;
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

            default:
                logIt("==================================================");
                logIt("Unknown file type '" + execType + "' in '" + filePathArgument + "'!");
                logIt("==================================================");
                failureCount = 1;
                break;
        }

        return failureCount;
    }

//    private void ExecuteKickerPackFile(String kickerPackFilePath)
//    {
//        logIt("KickPack file processing has not yet been implemented...");
//    }

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
	    	logIt("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
	    	logIt("Exception during '" + testName + "'!");
	    	logIt(ex.toString());
	    	logIt("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
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

    private int executeKickerFile(String kickerFilePath)
    {
        //  Log the test start time...
        logIt("====================================================");
        logIt("Test start time: " + LocalDateTime.now().toString());
        logIt("====================================================");
        logIt("  ");

        //  Setup the default return exit code value...
        int exitCode = 0;

        //  Increment the testCountTotal tracker...
        testCountTotal += 1;

        //  Reset the DataPool...
        dataPool = new DataPool();

        //  Initialize the DynamicData runtime values...
        DynamicData.initRuntimeValues();

        //  Shove the execution path into the global DataPool
        //  cuz it's likely we'll be needing for some reason later...
        setExecutionPathVariable();

        //  Introduce yourself!
        File testFile = new File(kickerFilePath);
        logIt("Test file name: '" + testFile.getName() + "':");
        logIt("Full file path: " + kickerFilePath);

        //  Load in test params as DataPool data...
        //JsonPath testKicker = loadTestKickerAsDataPoolData(kickerFilePath);
        loadTestKickerAsDataPoolData(kickerFilePath);

        //  Load the WorkflowProcessingEngine...
        WorkflowProcessingEngine workflowProcEngine = new WorkflowProcessingEngine();

        //  Load the workflow steps...
        //List<StepBase> workflow = loadWorkflow(testKicker.getString("workflow").toString());
        List<StepBase> workflow = loadWorkflow(dataPool.get("workflow").toString());

        //  Prepare a 'validationResults' container...
        HashMap<String, Object> validationResults = new HashMap<String, Object>();

        try
        {
            //  Execute the workflow steps...
            workflowProcEngine.execute(workflow, dataPool);

            logIt("  ");
            logIt("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            logIt("Beginning validations...");

            //  Do the validations...
            String validationFile = dataPool.get("validationFile").toString();
            validationResults = doValidations(validationFile);
        }
        catch (Exception ex)
        {
        	//  Dump all the DataPool data...
        	doDataDump(ex);

        	//  TODO:  Look into "throw ex" and reconfiguring what the method throws...
            //  Set the Exit Code..
            exitCode = -1;
        }

        int validationCount = (int) validationResults.get("ValidationCount");
        int failCount = (int) validationResults.get("FailCount");
        int passCount =(int)  validationResults.get("PassCount");

        //  Removing this for now as it creates some problematic logging...
//        if(failCount > 0)
//        {
//        	Exception ex = new Exception("Validation failure count of (" + failCount + ")!");
//        	doDataDump(ex);
//        }

        logIt("  ");
        logIt("-----------------------------------------------------------------------------------------------");
        logIt("-----------------------------------------------------------------------------------------------");
        logIt("-----------------------------------   VALIDATION RESULTS   ------------------------------------");
        logIt("Validation Total: " + validationCount);
        logIt("Failure Count:    " + failCount);
        logIt("Pass Count:       " + passCount);

        if(failCount > 0)
        {
        	dumpValidationList(validationResults.get("ValidationList"));
        }

        logIt("-----------------------------------------------------------------------------------------------");
        logIt("-----------------------------------------------------------------------------------------------");

        if ((validationResults.size() == 0 || ((int) validationResults.get("FailCount")) > 0))
        {
            exitCode = -1;
        }

        //  Log the test end time...
        logIt("====================================================");
        logIt("Test end time: " + LocalDateTime.now().toString());
        logIt("===================================================="); // + newLine);
        logIt("  ");

        if(failCount > 0)
        {
        	doDataDump();
        }
        
        return exitCode;
    }

    private void doDataDump()
    {
    	doDataDump(null);
    }

    private void doDataDump(Exception ex)
    {
    	if(ex != null)
    	{
    		logIt(ex.toString());
    	}
        logIt("===  DATA POOL DUMP  ===");
        
        String[] dataPoolDump = dataPool.dumpDataPool().split("\\r?\\n");
        
        for(String line: dataPoolDump)
        {
            logIt(line);        	
        }

//        logIt("=====  VALIDATION CHAIN DUMP  =====");
//        logIt(dumpValidationChain());
        logIt("Test Failed!");
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
					    	logIt("=================================================");
					    	logIt("  ");
					    	logIt("Validation section: " + k);
					    	HashMap<String, Object> validationItemsList = (HashMap<String, Object>) v;
					    	dumpValidationItem(validationItemsList.get("ValidationList"));
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
						    	logIt("-----------");
						    	logIt("  ");
						    	logIt("Test Path:      " + k);
						    	logIt("Expected value: " + validationItemDetails.get("ExpectedValue"));
						    	logIt("Actual value:   " + validationItemDetails.get("ActualValue"));
						    	logIt("Test Result:    " + validationItemDetails.get("TestResult"));
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

    	dataPool.add("ExecutionPath", absolute);

    	logIt("ExecutionPath set to: " + absolute);
    }

    @SuppressWarnings("unchecked")
	private String dumpValidationChain()
    {
    	//  Ready a container to hold the return value...
        StringBuilder sb = new StringBuilder();

        //  Convert the DataPool's "ValidationChain" object into a first class object...
        HashMap<String, Object> validationChain = (HashMap<String, Object>) dataPool.get("ValidationChain");

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
            logIt("Found workflow for '" + workflowName + "'.");
        }

        return workflowSteps;
    }

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
        HashMap<String, Object> validationChain = (HashMap<String, Object>) dataPool.get("ValidationChain");

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
            String validatorRawJson = dataPool.loadJsonFile(validationFilePath);
            int validatorRawLength = validatorRawJson.length();
        	JsonPath validator = JsonPath.from(validatorRawJson);

            if(validatorRawLength > 8)
            {
	            Map<String, Object> validatorKeyVals = validator.getMap(".");

	            //  Start looping through the various validation sections...
	            Object[] validatorKeys = validatorKeyVals.keySet().toArray();

	            for(int i = 0; i < validatorKeys.length; i++)
	            {
	            	Object sectionKey = validatorKeys[i];
	            	logIt("  -- " +sectionKey.toString());
	            	Object section = validatorKeyVals.get(sectionKey);

	                //  Grab a validation section and execute the
	                //  validations against the appropriate part of the
	                //  execution results...
	            	HashMap<String, Object> newValidationResult = executeValidator(sectionKey.toString(), section);

	                validationResultCollection.put(sectionKey.toString(), newValidationResult);

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
            String chainSecionRawJson = dataPool.getValidationChainItem(sectionName).toString();
        	JsonPath chainSection = JsonPath.with(chainSecionRawJson);

            //  Start looping through the validation items...
            for(int i = 0; i < validatorListKeys.length; i++)
            {
                validationCount += 1;

                //  Get a test results container ready...
                HashMap<String, String> testResults = new HashMap<String, String>();

                //  Grab the Json path that needs to be tested...
                String pathToTest = validatorListKeys[i].toString();
                
                //  Grab the last *expected* value we want to compare it to...
                String expectedValue = validationList.get(pathToTest).toString();
                               
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
                }

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
//                logIt("ValidationPath: " + pathToTest);
//                logIt("ExpectedValue:  " + expectedValue);
//                logIt("ActualValue:    " + actualValue);
                
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

    private void loadTestKickerAsDataPoolData(String testKickerFilePath)
    {
    	loadTestKickerAsDataPoolData(testKickerFilePath, true);
    }

    private void loadTestKickerAsDataPoolData(String testKickerFilePath, Boolean logValues)
    {
    	//  Grab the raw JSON data from the file...
    	String rawJson = DynamicData.loadJsonFile(testKickerFilePath);

    	//  Grab a dictionary of all the first-level elements...
    	Map<String, Object> testKickerKeyVals =
    			new Gson().fromJson(rawJson, new TypeToken<HashMap<String, Object>>() {}.getType());

    	//  Loop through all the items in the 'testKicker' object...
    	testKickerKeyVals.
    		forEach(
    				(keyName, value) ->
    				{
		                //  Add each item to the DataPool as key-val...
		                dataPool.put(keyName, value.toString());

		                if(logValues)
		                {
		                	logIt("   --> " + keyName + ": " + value.toString());
		                }
		                else
		                {
		                	logIt("   --> " + keyName + ": *****************");
		                }
		                //  In the special case that the item key happens
		                //  to be a "secretsFile" path, load it up and
		                //  recursively enter this method again, treating it
		                //  like it was any other "testKicker" file...
		                if (keyName.compareTo("secretsFile") == 0)
		                {
		                    //  JsonPath secretsData = DynamicData.LoadJsonFile(paramItem.Value.toString());
		                    loadTestKickerAsDataPoolData(value.toString(), false);
		                }
	                }
				   );
    }

    private void logIt(String msg)
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
    }
}

