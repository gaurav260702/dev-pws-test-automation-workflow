package com.autodesk.pws.test.engine;

import org.slf4j.Logger;


public class StepBase
{
    public com.autodesk.pws.test.engine.DataPool DataPool;
    public static Object ActionManager;
    public boolean BypassValidationChainLogging;
	public final String LineMark =  System.getProperty("line.separator");
	public Logger logger;
	public String ClassName;

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

    public void logErr(Exception ex, String className, String methodName) //throws Throwable
    {
		String errMsg = "Error in " + className + "." + methodName + "():" + this.LineMark + ex.toString();
		log(errMsg);
		//throw ex;
    }

    public void log(String msgToLog)
    {
    	logger.info(msgToLog);
    	//  System.out.println(msgToLog);
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
}
