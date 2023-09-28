package com.autodesk.pws.test.engine;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.autodesk.pws.test.processor.DynamicData;

import io.restassured.path.json.JsonPath;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Validator 
{
	private Kicker kicker;
	
	public Validator(Kicker kickerSource)
	{
		kicker = kickerSource;
	}

	private void logIt(String msg)
	{
		kicker.LogIt(msg);
	}
	
	private void logErr(Exception e, String methodName)
	{
		kicker.LogErr(e, methodName);
	}
	
	public class ValidationContainer
	{
		public int ExitCode = 0;
		public HashMap<String, Object> ValidationResults;
		public boolean ValidationsCompleted = false;
	}

	public ValidationContainer Validate() 
	{
		ValidationContainer retVal = new ValidationContainer();
		
		logIt("Beginning validations...");
		
		try
		{
	        //  Get the validation relative file path from the data pool...
	        String validationFile = kicker.DataPool.getRaw("validationFile").toString();
	        
	        logIt("  -- Validation file: " + validationFile);
	        
	        //  Check to see if the validation file exists, and...
	        if(fileExists(validationFile))
	        {
	            //  If it exists, then do the validations...
	        	try 
	        	{
					retVal.ValidationResults = this.doValidations(validationFile);	        	
		        	retVal.ValidationsCompleted = true;
				} 
	        	catch (Exception e) 
	        	{
	        		logIt("Unable to execute Validation!");
	        		logErr(e, "Validate");
		        	
		            retVal.ExitCode = -1;
		        	retVal.ValidationsCompleted = true;
				}
	        }
	        else
	        {
	        	// If it doesn't, return a exitCode indicasting a failure...
	            logIt("Validation file cannot be found.  Skipping validations.");
	            retVal.ValidationsCompleted = false;
	            retVal.ExitCode = -1;
	        }
		}
		catch(Exception e)
    	{
    		logIt("Unable to execute Validation!");
    		logErr(e, "Validate");
        	
            retVal.ExitCode = -1;
        	retVal.ValidationsCompleted = true;
		}
		
        return retVal;
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
        logIt("      --> Grabbing ValidationChain from DataPool...");
        HashMap<String, Object> validationChain = (HashMap<String, Object>) kicker.DataPool.get("ValidationChain");
        logIt("      --> (" + validationChain.size() + ") entries in ValidationChain...");

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
            String validatorRawJson = kicker.DataPool.loadJsonFile(validationFilePath);
        	JsonPath validator = JsonPath.from(validatorRawJson);
			JsonObject jsonObject = JsonParser.parseString(validatorRawJson).getAsJsonObject();
			kicker.DataPool.add("expValidationChain",jsonObject);
        	
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
	            	logIt("  -- " + sectionKey.toString());
	            	
	            	//  Check to see if the Validator section exists in the 
	            	//  validation chain...
	            	if(validatorKeyVals.containsKey(sectionKey))
	            	{
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
	            	else
	            	{
	            		//  Log the fact that the section is missing in the ValidationChain...
		                validationResultCollection.put(sectionKey.toString(), "Missing validation chain section!");
		                totalFailCount += 1;
	            	}
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

            //  Check to see if the validatio chain section exists...
            if(kicker.DataPool.getValidationChainItem(sectionName) == null)
            {
                //  Fill out the test results container with sadness...
                validationResults.put("GeneralError", "No '" + sectionName + "' data found in the validation chain!");
                validationResults.put("ValidationCount", 1);
                validationResults.put("FailCount", 1);
                validationResults.put("PassCount", 0);
                validationResults.put("ValidationList", null);
            }
            else
            {
	            //  Grab the validation chain section and token path to be tested...
	            String chainSecionRawJson = kicker.DataPool.getValidationChainItem(sectionName).toString();
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

					if(expectedValue.startsWith("[") && expectedValue.endsWith("]"))   //<Shailesh> 28 April 23 - to Handle error array logic
					{
						expectedValue = expectedValue.substring(1, expectedValue.length() - 1);
					}

	                //  Here we'll detokenize and resolve any Runtime and DataPool
	                //  values that exist in the validation data...
	                expectedValue = DynamicData.detokenizeRuntimeValues(expectedValue);
	                expectedValue = kicker.DataPool.detokenizeDataPoolValues(expectedValue);
	                
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
        }
        else
        {
            //  Fill out the test results container with sadness...
            validationResults.put("GeneralError", "No '" + sectionName + "' data found in the validation file!  Check for configuration errors...");
            validationResults.put("ValidationCount", 1);
            validationResults.put("FailCount", 1);
            validationResults.put("PassCount", 0);
            validationResults.put("ValidationList", null);
        }

        return validationResults;
    }
	
	private boolean fileExists(String relativeFilePath) 
    {
    	relativeFilePath = DynamicData.convertRelativePathToFullPath(relativeFilePath);
        File fileCheck = new File(relativeFilePath);
        return fileCheck.exists();
	}
}
