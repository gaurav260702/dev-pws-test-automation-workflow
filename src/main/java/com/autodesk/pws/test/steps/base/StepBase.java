package com.autodesk.pws.test.steps.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.autodesk.pws.test.engine.*;

import io.restassured.path.json.JsonPath;

public class StepBase
{
    protected final Logger logger = LoggerFactory.getLogger(StepBase.class);

    public static final int DEFAULT_LEFT_SPACE_PADDING = 7;
    
    public DataPool DataPool;
    public static Object ActionManager;
    public boolean BypassValidationChainLogging;
    //  For now, leave LineMark as a public property.  It will eventually be turned into
    //  a global value that is set once at runtime and then either directly referenced when
    //  required or the value will be passed into the class at instantiation...
	public final String LineMark =  System.getProperty("line.separator");
	//public Logger logger;
	public String ClassName;
	public Boolean SuppressLogging = false;
	public String ExceptionMessage = "";
	public Boolean ExceptionAbortStatus = false;
	
    public void preparation()
    {

    }

    public void action()
    {

    }

    public void validation()
    {

    }

    public void cleanup()
    {

    }
    
    public void sleep(int millisecondsToSleep)
    {
		try
		{
			log("Sleeping for " + (millisecondsToSleep / 1000) + " seconds...");
			Thread.sleep(millisecondsToSleep);
		} 
		catch (InterruptedException e) 
		{
			//  This should fail like...never.
		}
    }

    public void logErr(String exceptionMsg, String className, String methodName)
    {
		String errMsg = "Error in " + className + "." + methodName + "():" + LineMark + exceptionMsg;
    	log(errMsg);
    }
    
    public void logErr(Exception ex, String className, String methodName) //throws Throwable
    {
		logErr(ex.toString(), className, methodName);
	}

    public void logNoPad(String msgToLog)
    {
    	log(msgToLog, 0);
    }
    
    public void log(String msgToLog, int indentSpace)
    {
    	if(!SuppressLogging)
    	{
    		String leftPad = "";
    		
    		if(indentSpace > 0)
    		{
    			leftPad = padLeft(" ", indentSpace);
    		}
    		
    		String lines[] = msgToLog.split("\\r?\\n");
    		
    		for(String line : lines)
    		{
    			logger.info(leftPad + line);
    		}
    	}
    }
    
    private String padLeft(String s, int n) 
    {
        return String.format("%" + n + "s", s);  
    }
    
    public void log(String msgToLog)
    {
    	log(msgToLog, DEFAULT_LEFT_SPACE_PADDING);
    }

    public String dumpDataPool()
    {
        StringBuilder retVal = new StringBuilder();

        DataPool.forEach(
		        			(k, v) ->
					        {
				                retVal.append(k + " : " + v + LineMark);
				                retVal.append("-------------------------------------" + LineMark);
					        }
				        );

        return retVal.toString();
    }
    
	public void extractDataFromJsonAndAddToDataPool(String dataPoolLabel, String targetPath, JsonPath pathFinder)
	{
		try
		{
			//  Some weird monkeyshines here.  Raw numbers seem to be coming back as floats
			//  from JsonPath even if they're clearly Integers (ie, digits with no trailing
			//  decimal places).  This crazy little test would have been a lot easier in C#,
			//  but this was the only way I knew how to do it in Java, so, this is a stupid
			//  bit of code that shouldn't be removed...
			Object rawTargetValue = pathFinder.get(targetPath);
			String targetValue = rawTargetValue.toString();
			
			//  We're going to do something special if this is a float, so hold onto you hats...
			if(rawTargetValue instanceof Float)
			{
				//  Ok, forcefully turn the target value into a float...
		        Float floatTest= Float.parseFloat(rawTargetValue.toString());
		        
		        //  Now convert it to an integer, which should force drop
		        //  any trailing decimal places...
		        Integer intTest = Math.round(floatTest);
		        
		        //  Now we reconvert the "intergerized" float back to a float
		        //  and check to see if they're equal.  If the *are* equal, it's
		        //  likely the number should have been an integer all along.
		        //  If they' not equal, when they then they were clearly intended
		        //  to be a float in the first place...
		        if(Float.parseFloat(intTest.toString()) == floatTest)
		        {
		        	//  Looks like the float/int values were equal, so we're going
		        	//  to store the integer version of the number as a string
		        	//  instead of the float version...
		        	targetValue = intTest.toString();
		        }
			}

			//  At long last, we're finally at the point where we can store the 
			//  value in the DataPool!  Whoopee!!
			DataPool.add(dataPoolLabel, targetValue); 
		}
		catch (Exception ex)
		{
			this.log("Unable to locate path in JSON: '" + targetPath + "'...");
		}
	}
}
