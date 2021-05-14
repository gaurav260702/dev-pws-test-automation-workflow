package com.autodesk.pws.test.steps.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.autodesk.pws.test.engine.*;

public class StepBase
{
  protected final Logger logger = LoggerFactory.getLogger(StepBase.class);

  // TODO: <Kurt> make public DataPool dataPool; as protected earlier StepBase.java was in
  // package com.autodesk.pws.test.engine;
  // ^^^^ NOTE: DataPool is a public property and according my understanding of the Java casing guidelines
  //            should have be a capitalized name.  DataPool is passed around frequently between classes
  //            and is deliberately intended to be interrogated and modified by other classes.  Getters
  //            and Setters *SHOULD* *NOT* be used when working with DataPool as it adds fragility and 
  //            excess code and thereby creates a large potential for introducing needless coding errors
  //		    into the codebase.  It's best to conceptualize DataPool as a global, runtime database.
    public DataPool DataPool;
    public static Object ActionManager;
    public boolean BypassValidationChainLogging;
    //  For now, leave LineMark as a public property.  It will eventually be turned into
    //  a global value that is set once at runtime and then either directly referenced when
    //  required or the value will be passed into the class at instantiation...
	public final String LineMark =  System.getProperty("line.separator");
	//public Logger logger;
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
		String errMsg = "Error in " + className + "." + methodName + "():" + LineMark + ex.toString();
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
