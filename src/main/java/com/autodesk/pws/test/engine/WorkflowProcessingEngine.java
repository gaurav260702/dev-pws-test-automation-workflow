package com.autodesk.pws.test.engine;

import java.io.File;
import java.util.List;
import org.apache.commons.lang3.time.StopWatch;

import com.autodesk.pws.test.steps.base.*;

public class WorkflowProcessingEngine 
{
    // Psuedo-globally available data container for all test steps and validations...
    public DataPool DataPool; 
    
    // Prep a flag to mark ExceptionAborts...
    private Boolean isForcedExceptionAbort = false;
    
	//  Prep a "testCompleted" flag to return.
	//  We're going to assume it did unless
	//  something baaaaad happens...
    private Boolean workflowCompleted = true;
    
    //  Flag to determine if logging is duplicated to a file...
    public Boolean LogToFile = false;
    
    //  Container for the LogFilePath...
    public String LogFileName = "";
    
    //////////////////////////////////////////////////////////////////////////////
    // TODO:  Implement routines to handle "ExceptionAbortStatus"!!
    //        This should come between each substep (preparation, action, 
    //        validation, cleanup) and should end execution immediately upon
    //        detection (with options to report out states and messages)...
    //////////////////////////////////////////////////////////////////////////////
    public boolean execute(List<StepBase> workflowToExecute) throws Exception 
    {    	
	    // Prep a container for the current step name...
	    String currentStep = "";
	    
	    // Ready a stopwatch...
	    StopWatch totalTestTime = new StopWatch();
	    totalTestTime.start();
	
	    // Prep a container for a lastStep reference...
	    StepBase lastStep = null;
	    
	    // Prep a container for looping through..
	    StepBase step = null;
	
	    // Ready a step counter...
	    int stepCount = 0;
	
	    // We need this flag so we can properly utilize
	    // the step.logger() method in the loop below...
	    Boolean firstReportMade = false;
	    
	    // Report out intended step execution...
	    for (int i = 0; i < workflowToExecute.size(); i++) 
		{
	    	step = workflowToExecute.get(i);
	    	DataPool.StepLogger = step;
	      
	    	stepCount += 1;
	
			if (!firstReportMade) 
			{
				step.logNoPad("  ");
				step.logNoPad("Step execution outline:");
				firstReportMade = true;
			}
	
			step.logNoPad("   (" + stepCount + ") -- " + step.getClass().getSimpleName());
	    }
	
	    // Reset the step counter...
	    stepCount = 0;
	
		// Loop through and execute each step in order...
		for (int i = 0; i < workflowToExecute.size(); i++) 
		{
			// Grab the next step...
			step = workflowToExecute.get(i);
		 	
		 	// Push up the counter...
		   	stepCount += 1;
		
		   	// Prep a stopwatch for timing the step...
		   	StopWatch totalStepTime = new StopWatch();
		   	totalStepTime.start();
	
			try 
			{
				// Set the DataPool reference for the next step...
				step.DataPool = DataPool;
				
				// Set the logToFile flag and file path...
				step.LogToFile = LogToFile;
				step.LogFile = LogFileName;
				
				// Grab the step name...
				currentStep = step.getClass().getSimpleName();
				
				// Log each substep in order...
				step.logNoPad("");
				step.logNoPad("");
				step.logNoPad("Step #" + stepCount + " of " + workflowToExecute.size() + "...");
				step.logNoPad("-------------------------------------------------------------");
				step.logNoPad("EXECUTING STEP: '" + currentStep + "'");
				
				step.logNoPad("  -->  Substep: " + currentStep + ".Preparation()");
				step.preparation();
			    //step.logNoPad(DataPool.dumpDataPool());
			    
				if(checkForExceptionAbort(step))
				{
					break;
				}
				
				step.logNoPad("  -->  Substep: " + currentStep + ".Action()");
				step.action();
			    //step.logNoPad(DataPool.dumpDataPool());
			    
				if(checkForExceptionAbort(step))
				{
					break;
				}
				
				step.logNoPad("  -->  Substep: " + currentStep + ".Validation()");
				step.validation();
			    //step.logNoPad(DataPool.dumpDataPool());
			    
				if(checkForExceptionAbort(step))
				{
					break;
				}

				step.logNoPad("  -->  Substep: " + currentStep + ".Cleanup()");
				step.cleanup();
			    //step.logNoPad(DataPool.dumpDataPool());
			    
				if(checkForExceptionAbort(step))
				{
					break;
				}

				step.logNoPad("'" + currentStep + "' execution time: " + (totalStepTime.getTime() / 1000) + " seconds.");
				step.logNoPad("-------------------------------------------------------------");				
			    
				lastStep = step;
			  } 
			  catch (Exception ex) 
			  {
				  // Bad things have happened....
				  step.logNoPad("'" + currentStep + "' execution time: " + (totalStepTime.getTime() / 1000) + " seconds.");
				  String errMsg = "FAILURE DURING '" + currentStep + "'!";
				  step.logNoPad(errMsg);
					
				  String[] exceptionLines = ex.toString().split("\\r?\\n");
					
				  for(String line: exceptionLines)
				  {
					  step.logNoPad(line);					
				  }
					
				  if(isForcedExceptionAbort == true)
				  {
					  //  Exit "gracefullly"... ;-)
					  break;
				  }
				  else
				  {
					  throw new Exception(errMsg, ex);
				  }
			  } 
			  finally 
			  {
				  String subStepTestTime = (totalStepTime.getTime() / 1000) + "";
				  DataPool.add(currentStep + "TestTime", subStepTestTime);
			  }
		}

		// Log the total test time...
		lastStep.logNoPad("Total workflow execution time: " + (totalTestTime.getTime() / 1000) + " seconds.");
		
		return workflowCompleted;
	}

    private Boolean checkForExceptionAbort(StepBase step) throws Exception 
    {
    	Boolean retVal = false;
    	
    	if(step.ExceptionAbortStatus)
    	{
    		retVal = true;
    		isForcedExceptionAbort = true;
    		workflowCompleted = false;
    		step.logNoPad("CONTROLLED WORKFLOW EXCEPTION ABORT! " + step.LineMark + step.ExceptionMessage);
    	}
    	
    	return retVal;
    }

	public void setLogToFile(Boolean logToFileFlag, String logFileName, String testFileName) 
	{
		this.LogToFile = logToFileFlag;
		
		if(LogToFile)
		{
			LogFileName = logFileName; // DataPool.detokenizeDataPoolValues(LogFileNameTemplate);
		}
	}
}

