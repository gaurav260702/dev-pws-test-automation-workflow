package com.autodesk.pws.test.workflow;

import com.autodesk.pws.test.steps.authentication.*;
import com.autodesk.pws.test.steps.base.*;
import com.autodesk.pws.test.steps.informative.*;
import com.autodesk.pws.test.steps.invoice.*;
import com.autodesk.pws.test.steps.opportunity.*;
import com.autodesk.pws.test.steps.order.*;
import com.autodesk.pws.test.steps.price.*;
import com.autodesk.pws.test.steps.quote.*;
import com.autodesk.pws.test.steps.rule.*;
import com.autodesk.pws.test.steps.utility.*;
import com.autodesk.pws.test.steps.catalog.*;

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
        List<StepBase> workflow = GenericPlaceOrder();
        
        workflow.add(new GetOrderDetails());
        workflow.add(new WaitForGetAgreementInfo());

        return workflow;
    }
    
    public static List<StepBase> GenericPlaceOrder()
    {
        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadBaseFilesAndExtractOrderInfoData());
        workflow.add(new GetOAuthCredentials());
        workflow.add(new GetSkuPrice());
        workflow.add(new PostOrder());
        workflow.add(new GetOAuthCredentials());
        workflow.add(new WaitForOrderStatusChange());

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
         workflow.add(new GetOrderDetails());
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
         workflow.add(new GetSkuRenewalPrice());
	   	 
         //    	 Place a V2 Renewal Order
         workflow.add(new PostOrderRenewal());

         //    	 Wait for the Renewal OrderStatus to move to "order is under review"
         workflow.add(new GetOAuthCredentials());
         workflow.add(new WaitForOrderStatusChange2ndPass());
	   	 workflow.add(new GetOrderDetails2ndPass());
	   	 
    	 return workflow;
    }
    
    public static List<StepBase> CreateSalesForceRenewalOpportunity()
    {
        List<StepBase> workflow = new ArrayList<StepBase>();
    
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
    
    public static List<StepBase> PlaceS2SOrder()
    {
        // Initial PlaceOrder...
        List < StepBase > workflow = PlaceOrder();

        // Place S2S order...
        workflow.add(new LoadAddOnFilesAndExtractAddOnInfoData());
        workflow.add(new GetOAuthCredentials());
        workflow.add(new GetSkuPrice());
        workflow.add(new PostOrder());
        workflow.add(new GetOAuthCredentials());
        workflow.add(new WaitForOrderStatusChange2ndPass());
        workflow.add(new GetOrderDetails());

        return workflow;
    }
     
    public static List<StepBase> CreateQuote()
    {
    	boolean waitForExpectedStatus = true;
    	
        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetails());
        workflow.add(new QuoteFinalize());
        workflow.add(new QuoteStatusByQuoteNumber(waitForExpectedStatus));
        
        return workflow;
    }


    public static List<StepBase> CreateQuoteResendEmail()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetails());
        workflow.add(new QuoteFinalize());
        workflow.add(new QuoteStatusByQuoteNumber(waitForExpectedStatus));
        workflow.add(new QuoteResendEmail());

        return workflow;
    }

    public static List<StepBase> CancelQuoteNegative()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetails());
        workflow.add(new QuoteFinalize());
        workflow.add(new QuoteStatusByQuoteNumber(waitForExpectedStatus));
        workflow.add(new QuoteCancel());


        return workflow;
    }

    public static List<StepBase> QuoteCancelForDeleteQuote()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetails());
        workflow.add(new QuoteFinalize());
        workflow.add(new QuoteStatusByQuoteNumber(waitForExpectedStatus));
        workflow.add(new QuoteCancelForDeleteQuote());


        return workflow;
    }

    public static List<StepBase> QuoteCancelPositive()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
     //   workflow.add(new QuoteDetails());
     //   workflow.add(new QuoteFinalize());
     //   workflow.add(new QuoteStatusByQuoteNumber(waitForExpectedStatus));
        workflow.add(new QuoteCancelPositive());

        return workflow;
    }

    public static List<StepBase> CatalogExport()
    {
        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadBaseFiles());
        // workflow.add(new GetOAuthCredentials());
        workflow.add(new GetCatalogExportOAuthCredentials());
        workflow.add(new GetCatalogDetails());

        return workflow;
    }

    public static List<StepBase> QuoteUpdateNegative()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetails());
        workflow.add(new QuoteFinalize());
        workflow.add(new QuoteStatusByQuoteNumber(waitForExpectedStatus));
        workflow.add(new QuoteUpdateNegative());


        return workflow;
    }

    public static List<StepBase> QuoteUpdateLineNumberMismatchedNeg()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetails());
        workflow.add(new QuoteFinalize());
        workflow.add(new QuoteStatusByQuoteNumber(waitForExpectedStatus));
        workflow.add(new QuoteUpdateLineNumberMismatchedNeg());


        return workflow;
    }

    public static List<StepBase> QuoteUpdatePositive()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
    //  workflow.add(new QuoteDetails());
    //  workflow.add(new QuoteFinalize());
    //  workflow.add(new QuoteStatusByQuoteNumber(waitForExpectedStatus));
        workflow.add(new QuoteUpdatePositive());


        return workflow;
    }
}
