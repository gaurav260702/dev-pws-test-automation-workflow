package com.autodesk.pws.test.engine;

import com.autodesk.pws.test.service.invoice.GetInvoiceDetails;
import com.autodesk.pws.test.service.invoice.GetInvoiceList;
import com.autodesk.pws.test.processor.LoadBaseFiles;
import com.autodesk.pws.test.service.GetOAuthCredentials;
import java.util.*;

public class WorkflowLibrary
{
    public static List<StepBase> GetInvoiceDetailsPreCannedNoAuth()
    {
        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadBaseFiles());
        workflow.add(new GetInvoiceDetails());

        return workflow;
    }

    public static List<StepBase> GetInvoiceDetailsPreCannedWithAuth()
    {
        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadBaseFiles());
        workflow.add(new GetOAuthCredentials());
        workflow.add(new GetInvoiceDetails());

        return workflow;
    }

    public static List<StepBase> GetInvoiceListPreCannedWithAuth()
    {
        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadBaseFiles());
        workflow.add(new GetOAuthCredentials());
        workflow.add(new GetInvoiceList());

        return workflow;
    }
}
