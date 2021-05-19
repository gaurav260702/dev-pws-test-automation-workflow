package com.autodesk.pws.test.workflow;

import com.autodesk.pws.test.steps.authentication.*;
import com.autodesk.pws.test.steps.base.*;
import com.autodesk.pws.test.steps.informative.*;
import com.autodesk.pws.test.steps.invoice.*;
import com.autodesk.pws.test.steps.order.*;
import com.autodesk.pws.test.steps.price.*;
import com.autodesk.pws.test.steps.utility.*;

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
    
    public static List<StepBase> PlaceOrder()
    {
        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadBaseFilesAndExtractOrderInfoData());
        workflow.add(new GetOAuthCredentials());
        workflow.add(new GetSkuPrice());
        workflow.add(new PostOrder());
        workflow.add(new WaitForOrderStatusChange());
        workflow.add(new GetOrderDetailsV1());
        workflow.add(new WaitForGetAgreementInfo());

        return workflow;
    }
}
