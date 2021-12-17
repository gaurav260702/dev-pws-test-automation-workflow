package com.autodesk.pws.test.workflow;


import java.lang.reflect.*;
import java.util.*;

import com.autodesk.pws.test.steps.base.*;

public class Workflows {
  @SuppressWarnings("unchecked")
  public List<StepBase> getWorkflowByName(String workflowName)
      throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    List<StepBase> retVal = new ArrayList<StepBase>();

    Method[] methods = WorkflowLibrary.class.getMethods();
    WorkflowLibrary workflowLibraryObject = new WorkflowLibrary();

    for (int i = 0; i < methods.length; i++) {
      if (methods[i].getName().toLowerCase().compareTo(workflowName.toLowerCase()) == 0) {
        retVal = (List<StepBase>) methods[i].invoke(workflowLibraryObject);
        break;
      }
    }

    return retVal;
  }
}
