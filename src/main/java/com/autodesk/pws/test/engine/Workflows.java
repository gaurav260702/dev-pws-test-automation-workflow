package com.autodesk.pws.test.engine;


import java.lang.reflect.*;
import java.util.*;

public class Workflows
{
    @SuppressWarnings("unchecked")
	public List<StepBase> getWorkflowByName(String workflowName) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
    	List<StepBase> retVal = new ArrayList<StepBase>();

        Method[] methods = WorkflowLibrary.class.getMethods();
        WorkflowLibrary workflowLibraryObject = new WorkflowLibrary();

        for (int i = 0; i < methods.length; i++)
        {
//        	System.out.println(methods[i].getName().toLowerCase());
//        	System.out.println(workflowName.toLowerCase());
//        	System.out.println("-----------------------------------");

            if(methods[i].getName().toLowerCase().compareTo(workflowName.toLowerCase()) == 0)
            {
                retVal = (List<StepBase>) methods[i].invoke(workflowLibraryObject);
                break;
            }
        }

        return retVal;
    }
}
