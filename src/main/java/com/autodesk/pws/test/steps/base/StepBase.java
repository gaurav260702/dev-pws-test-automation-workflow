package com.autodesk.pws.test.steps.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.autodesk.pws.test.engine.DataPool;

public class StepBase
{
  protected final Logger logger = LoggerFactory.getLogger(StepBase.class);

  // TODO: <Kurt> make public DataPool dataPool; as protected earlier StepBase.java was in
  // package com.autodesk.pws.test.engine;
  public DataPool dataPool;
  private static Object actionManager;
  protected boolean bypassValidationChainLogging;
  protected String lineMark = System.getProperty("line.separator");
    protected String className;

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
		String errMsg = "Error in " + className + "." + methodName + "():" + this.lineMark + ex.toString();
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

        dataPool.forEach(
		        			(k, v) ->
					        {
				                retVal.append(k + " : " + v + lineMark);
				                retVal.append("-------------------------------------" + lineMark);
					        }
				        );

        return retVal.toString();
    }
}
