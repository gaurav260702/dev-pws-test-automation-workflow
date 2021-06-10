package com.autodesk.pws.test.engine;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.apache.commons.lang3.time.StopWatch;
import com.autodesk.pws.test.steps.base.*;

public class WorkflowProcessingEngine 
{
    // Psuedo-globally available data container for all test steps and validations...
    private DataPool dataPool; // { get; private set; }

    //////////////////////////////////////////////////////////////////////////////
    // TODO:  Implement routines to handle "ExceptionAbortStatus"!!
    //        This should come between each substep (preparation, action, 
    //        validation, cleanup) and should end execution immediately upon
    //        detection (with options to report out states and messages)...
    //////////////////////////////////////////////////////////////////////////////
    
    public void execute(List<StepBase> workflowToExecute, DataPool localDataPool) throws Exception 
    {
	    // Prep a container for the current step name...
	    String currentStep = "";
	
	    // Set the local DataPool to the passed reference...
	    dataPool = localDataPool;
	    
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
	      dataPool.StepLogger = step;
	      
	      stepCount += 1;
	
	      if (!firstReportMade) 
		  {
	        step.log("  ");
	        step.log("Step execution outline:");
	        firstReportMade = true;
	      }
	
	      step.log("   (" + stepCount + ") -- " + step.getClass().getSimpleName());
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
				step.DataPool = dataPool;
				// step.DataPool.StepLogger = step;
				
				// Grab the step name...
				currentStep = step.getClass().getSimpleName();
				
				// Log each substep in order...
				step.log("");
				step.log("");
				step.log("Step #" + stepCount + " of " + workflowToExecute.size() + "...");
				step.log("-------------------------------------------------------------");
				step.log("EXECUTING STEP: '" + currentStep + "'");
				
				step.log("  -->  Substep: " + currentStep + ".Preparation()");
				step.preparation();
				
				checkForExceptionAbort(step);
				
				step.log("  -->  Substep: " + currentStep + ".Action()");
				step.action();

				checkForExceptionAbort(step);
				
				step.log("  -->  Substep: " + currentStep + ".Validation()");
				step.validation();
				
				checkForExceptionAbort(step);

				step.log("  -->  Substep: " + currentStep + ".Cleanup()");
				step.cleanup();

				checkForExceptionAbort(step);

				step.log("'" + currentStep + "' execution time: " + (totalStepTime.getTime() / 1000) + " seconds.");
				step.log("-------------------------------------------------------------");
				lastStep = step;
			  } 
			  catch (Exception ex) 
			  {
				// Bad things have happened....
				step.log("'" + currentStep + "' execution time: " + (totalStepTime.getTime() / 1000) + " seconds.");
				String errMsg = "FAILURE DURING '" + currentStep + "'!";
				step.log(errMsg);
				
				String[] exceptionLines = ex.toString().split("\\r?\\n");
				
				for(String line: exceptionLines)
				{
					step.log(line);					
				}

				throw new Exception(errMsg, ex);
			  } 
			  finally 
			  {
				String subStepTestTime = (totalTestTime.getTime() / 1000) + "";
				dataPool.add(currentStep + "TestTime", subStepTestTime);
			  }
		}

		// Log the total test time...
		lastStep.log("Total workflow execution time: " + (totalTestTime.getTime() / 1000) + " seconds.");
	}

    private void checkForExceptionAbort(StepBase step) throws Exception 
    {
    	if(step.ExceptionAbortStatus)
    	{
			throw new Exception("CONTROLLED WORKFLOW EXECUTION ERROR! " + step.LineMark + step.ExceptionMessage);
    	}
    }

// When this is fixed, it will solve the issue above of
  // converting the total test execution time to seconds...
  @SuppressWarnings("unused")
  private String convertLongToTimeString(Long timeToConvert) 
  {
    Date date = new Date(timeToConvert);
    DateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");
    formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    String dateFormatted = formatter.format(date);

    return dateFormatted;
  }
}

