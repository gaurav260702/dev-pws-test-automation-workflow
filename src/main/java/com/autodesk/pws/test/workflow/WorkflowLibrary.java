package com.autodesk.pws.test.workflow;

import com.autodesk.pws.test.steps.authentication.*;
import com.autodesk.pws.test.steps.base.*;
import com.autodesk.pws.test.steps.informative.*;
import com.autodesk.pws.test.steps.invoice.*;
import com.autodesk.pws.test.steps.opportunity.*;
import com.autodesk.pws.test.steps.order.*;
import com.autodesk.pws.test.steps.price.*;
import com.autodesk.pws.test.steps.utility.*;
import com.autodesk.pws.test.steps.rule.*;

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
        workflow.add(new GetOAuthCredentials());
        workflow.add(new WaitForOrderStatusChange());
        workflow.add(new GetOrderDetailsV1());
        workflow.add(new WaitForGetAgreementInfo());

        return workflow;
    }
    
    public static List<StepBase> GenericPlaceOrderWithAddOn()
    {
    	 //  Initial PlaceOrder...
    	 List<StepBase> workflow = PlaceOrder();
    	 
    	 //  AddOn order...
         workflow.add(new LoadAddOnFilesAndExtractAddOnInfoData());
         workflow.add(new GetOAuthCredentials());
         workflow.add(new WaitForGetAssetDetails());
         workflow.add(new GetSkuPrice());
         workflow.add(new PostOrder());
         workflow.add(new GetOAuthCredentials());
         workflow.add(new WaitForOrderStatusChange());
         workflow.add(new GetOrderDetailsV1());
         workflow.add(new WaitForGetAgreementInfo());
         
    	 return workflow;
    }
    
    public static List<StepBase> GenericPlaceOrderWithRenewal()
    {
  
	   	 //  Initial PlaceOrder...
	   	 List<StepBase> workflow = PlaceOrder();
	   	 
	   	 //	 Login to SalesForce and create a Renewal Opportunity
	   	 workflow.addAll(CreateSalesForceRenewalOpportunity());
	   	 
    	 //  AddOn order...
         workflow.add(new LoadRenewalFilesAndExtractData());
         workflow.add(new GetOAuthCredentials());
         workflow.add(new GetOpportunityInfoByOpptyId());
         workflow.add(new WaitForGetAssetDetails());
	   	 
         //    	 Get the Price for the Renewal SKU
         workflow.add(new GetOAuthCredentials());
         workflow.add(new GetSkuPrice());
	   	 
         //    	 Place a V2 Renewal Order
         workflow.add(new PostOrderRenewal());

         //    	 Wait for the Renewal OrderStatus to move to "order is under review"
         workflow.add(new GetOAuthCredentials());
         workflow.add(new WaitForOrderStatusChange2ndPass());
	   	 
    	 return workflow;
    }
    
    public static List<StepBase> CreateSalesForceRenewalOpportunity()
    {
        List<StepBase> workflow = new ArrayList<StepBase>();
        
//        workflow.add(new OpenSalesForce());
//        workflow.add(new SalesForceLogin());
//        workflow.add(new CreateRenewalOpportunityFromSalesForceId());
//        workflow.add(new CloseBrowser());
    
        workflow.add(new GetOpptyOAuthCredentials());
        workflow.add(new CreateOpptyByAgreementId());
        workflow.add(new GetOpptyStatusByOpportunityTransactionId());
        
        return workflow;
    }
    

    public static List<StepBase> PlaceFlexOrder()
    {
        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadBaseFilesAndExtractOrderInfoData());
        workflow.add(new GetSireOAuthCredentials());
        workflow.add(new ExecuteSireRule());
        workflow.add(new GetOAuthCredentials());
        workflow.add(new GetSkuPrice());
        workflow.add(new PostOrder());
        workflow.add(new WaitForOrderStatusChange());

        return workflow;
    }
}
