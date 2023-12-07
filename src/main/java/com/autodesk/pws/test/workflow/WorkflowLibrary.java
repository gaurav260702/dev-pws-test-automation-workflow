package com.autodesk.pws.test.workflow;

import com.autodesk.pws.test.steps.authentication.*;
import com.autodesk.pws.test.steps.base.*;
import com.autodesk.pws.test.steps.informative.*;
import com.autodesk.pws.test.steps.invoice.*;
import com.autodesk.pws.test.steps.opportunity.*;
import com.autodesk.pws.test.steps.order.*;
import com.autodesk.pws.test.steps.price.*;
import com.autodesk.pws.test.steps.promotions.GetPromotionDetails;
import com.autodesk.pws.test.steps.quote.*;
import com.autodesk.pws.test.steps.rule.*;
import com.autodesk.pws.test.steps.utility.*;
import com.autodesk.pws.test.steps.catalog.*;
import com.autodesk.pws.test.steps.webhook.InvokeWebhook;


import java.util.*;
import java.io.UnsupportedEncodingException;

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

    public static List<StepBase> CreateQuoteV2New()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
     // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());
        workflow.add(new QuoteFinalizeV2New());
        workflow.add(new QuoteStatusByQuoteNumber(waitForExpectedStatus));
        return workflow;
    }

    public static List<StepBase> CreateQuoteV2NewEndCustomerExportControl_Review()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteFinalizeV2New());
        workflow.add(new QuoteStatusAfterFinalize(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());

        return workflow;
    }

    public static List<StepBase> QuoteUpdateOperationDelete()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());
        workflow.add(new QuoteUpdateOperationDelete());

        return workflow;
    }

    public static List<StepBase> QuoteUpdateOperationDeleteQuoteNotInDraftStateNeg()
    {
        boolean waitForExpectedStatus = true;
        List<StepBase> workflow = CreateQuoteV2New();

        /*List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());
        workflow.add(new QuoteFinalizeV2New());
        workflow.add(new QuoteStatusByQuoteNumber(waitForExpectedStatus));*/
        workflow.add(new QuoteUpdateOperationDelete());

        return workflow;
    }

    public static List<StepBase> QuoteUpdateOperationDeleteExtraFieldNeg()
    {
        boolean waitForExpectedStatus = true;
     // List<StepBase> workflow = CreateQuoteV2New();

        List<StepBase> workflow = new ArrayList<StepBase>();

      workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
    /*    workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());
        workflow.add(new QuoteFinalizeV2New());
        workflow.add(new QuoteStatusByQuoteNumber(waitForExpectedStatus)); */
        workflow.add(new QuoteUpdateOperationDeleteExtraFieldNeg());

        return workflow;
    }


    public static List<StepBase> QuoteUpdateOperationUpdateActionNew()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());
        workflow.add(new QuoteUpdateOperationUpdateActionNew());

        return workflow;
    }

    public static List<StepBase> QuoteUpdateOperationUpdateActionNewNewFieldNeg()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
       /* workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());*/
        workflow.add(new QuoteUpdateOperationUpdateActionNewNewFieldNeg());

        return workflow;
    }

    public static List<StepBase> QuoteUpdateOperationUpdateActionNewNeg()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());
        workflow.add(new QuoteUpdateOperationUpdateActionNewNeg());

        return workflow;
    }

    public static List<StepBase> QuoteUpdateOperationInsertActionNew()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());
        workflow.add(new QuoteUpdateOperationInsertActionNew());

        return workflow;
    }

    public static List<StepBase> QuoteUpdateOperationInsertActionNewNewFieldNew()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        /*workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());*/
        workflow.add(new QuoteUpdateOperationInsertActionNewNewFieldNew());

        return workflow;
    }

    public static List<StepBase> QuoteUpdateOperationInsertActionCoTerm()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());
        workflow.add(new QuoteUpdateOperationInsertActionCoTerm());

        return workflow;
    }

    public static List<StepBase> QuoteUpdateOperationInsertActionCoTermNZ()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());
        workflow.add(new QuoteUpdateOperationInsertActionCoTermNZ());

        return workflow;
    }

    public static List<StepBase> QuoteUpdateOperationInsertActionCoTermNewFieldNeg()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        /*workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());*/
        workflow.add(new QuoteUpdateOperationInsertActionCoTermNewFieldNeg());

        return workflow;
    }

    public static List<StepBase> QuoteUpdateOperationInsertActionRenewal()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());
        workflow.add(new QuoteUpdateOperationInsertActionRenewal());

        return workflow;
    }

    public static List<StepBase> QuoteUpdateOperationInsertActionRenewalNZ()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());
        workflow.add(new QuoteUpdateOperationInsertActionRenewalNZ());

        return workflow;
    }

    public static List<StepBase> QuoteUpdateOperationInsertActionRenewalNewFieldNeg()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        /*workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());*/
        workflow.add(new QuoteUpdateOperationInsertActionRenewalNewFieldNeg());

        return workflow;
    }

    public static List<StepBase> QuoteUpdateOperationInsertActionSwitch()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());
        workflow.add(new QuoteUpdateOperationInsertActionSwitch());

        return workflow;
    }

    public static List<StepBase> QuoteUpdateOperationInsertActionSwitchNZ()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());
        workflow.add(new QuoteUpdateOperationInsertActionSwitchNZ());

        return workflow;
    }

    public static List<StepBase> QuoteUpdateOperationInsertActionSwitchNewFieldNeg()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        /*workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());*/
        workflow.add(new QuoteUpdateOperationInsertActionSwitchNewFieldNeg());

        return workflow;
    }



    public static List<StepBase> QuoteUpdateOperationInsertActionSwitchNeg()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());
        workflow.add(new QuoteUpdateOperationInsertActionSwitchNeg());

        return workflow;
    }

    public static List<StepBase> QuoteUpdateOperationInsertActionSwitchSubNeg()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteUpdateOperationInsertActionSwitchNeg());

        return workflow;
    }

    public static List<StepBase> QuoteUpdateOperationInsertActionExtension()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());
        workflow.add(new QuoteUpdateOperationInsertActionExtension());

        return workflow;
    }

    public static List<StepBase> QuoteUpdateOperationInsertActionExtensionNewFieldNeg()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        /*workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());*/
        workflow.add(new QuoteUpdateOperationInsertActionExtensionNewFieldNeg());

        return workflow;
    }

    public static List<StepBase> QuoteUpdateOperationInsertActionTrueUp()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());
        workflow.add(new QuoteUpdateOperationInsertActionTrueUp());

        return workflow;
    }

    public static List<StepBase> QuoteUpdateOperationInsertActionTrueUpNewFieldNeg()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        /*workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());*/
        workflow.add(new QuoteUpdateOperationInsertActionTrueUpNewFieldNeg());

        return workflow;
    }

    public static List<StepBase> QuoteUpdateOperationUpdateActionCoTerm()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());
        workflow.add(new QuoteUpdateOperationUpdateActionCoTerm());

        return workflow;
    }

    public static List<StepBase> QuoteUpdateOperationUpdateActionCoTermNewFieldNeg()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        /*workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());*/
        workflow.add(new QuoteUpdateOperationUpdateActionCoTermNewFieldNeg());

        return workflow;
    }

    public static List<StepBase> QuoteUpdateOperationUpdateActionRenewal()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());
        workflow.add(new QuoteUpdateOperationUpdateActionRenewal());

        return workflow;
    }

    public static List<StepBase> QuoteUpdateOperationUpdateActionRenewalNewFieldNeg()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        /*workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());*/
        workflow.add(new QuoteUpdateOperationUpdateActionRenewalNewFieldNeg());

        return workflow;
    }

    public static List<StepBase> QuoteUpdateOperationUpdateActionSwitch()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());
        workflow.add(new QuoteUpdateOperationUpdateActionSwitch());

        return workflow;
    }

    public static List<StepBase> QuoteUpdateOperationUpdateActionSwitchNewFieldNeg()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        /*workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());*/
        workflow.add(new QuoteUpdateOperationUpdateActionSwitchNewFieldNeg());

        return workflow;
    }

    public static List<StepBase> QuoteUpdateOperationUpdateActionExtension()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());
        workflow.add(new QuoteUpdateOperationUpdateActionExtension());

        return workflow;
    }

    public static List<StepBase> QuoteUpdateOperationUpdateActionExtensionNewFieldNeg()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        /*workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());*/
        workflow.add(new QuoteUpdateOperationUpdateActionExtensionNewFieldNeg());

        return workflow;
    }

    public static List<StepBase> CreateQuoteV2Extension()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());


        return workflow;
    }

    public static List<StepBase> RenewalQuoteV2New()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());

        return workflow;
    }

    public static List<StepBase> QuoteListUsingAccountCSN()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
     //   workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
     //   workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteListUsingAccountCSN());

        return workflow;
    }

    public static List<StepBase> UpdateQuoteV2OperationDelete()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());
        workflow.add(new QuoteDeleteV2());
    //  workflow.add(new QuoteFinalizeV2New());
    //  workflow.add(new QuoteStatusByQuoteNumber(waitForExpectedStatus));

        return workflow;
    }

    public static List<StepBase> CreateQuoteV2NewMOAB()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());
        workflow.add(new QuoteFinalizeV2New());
        workflow.add(new QuoteStatusByQuoteNumberMOAB(waitForExpectedStatus));

        return workflow;
    }

    public static List<StepBase> CreateQuoteV2NewFlexQTYLessThan100()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        workflow.add(new QuoteStatusFAILED(waitForExpectedStatus));

        return workflow;
    }

    public static List<StepBase> CreateQuoteV2NewDuplicatePayloadCheck()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        workflow.add(new QuoteStatusFAILED(waitForExpectedStatus));
        workflow.add(new QuoteCreate());

        return workflow;
    }

    public static List<StepBase> CreateQuoteV2NewFlexInvalidCSN()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
        //  workflow.add(new QuoteStatusFAILED());
      //  workflow.add(new QuoteStatusERROR(waitForExpectedStatus));
        //  workflow.add(new QuoteDetailsV2New());
        //  workflow.add(new QuoteFinalizeV2New());
        //  workflow.add(new QuoteStatusByQuoteNumber(waitForExpectedStatus));

        return workflow;
    }

    public static List<StepBase> CreateQuoteV2NewInvalidQuoteNo()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
     //   workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());
     //   workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2NewInvalidQuoteNo());
      //  workflow.add(new QuoteFinalizeV2New());
      //  workflow.add(new QuoteStatusByQuoteNumber(waitForExpectedStatus));

        return workflow;
    }

    public static List<StepBase> CreateQuoteV2NewIsIndividualTrueNegative()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
       // workflow.add(new QuoteCreateV2New());


        return workflow;
    }

    public static List<StepBase> CreateQuoteV2NewQuantityZeroNegative()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        // workflow.add(new QuoteCreateV2New());


        return workflow;
    }

    public static List<StepBase> CreateQuoteV2NewDuplicatePayloadCheckNegative()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
     // workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteCreate());

     //   workflow.add(new QuoteCreateDuplicatePayloadCheck());

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

    public static List<StepBase> QuoteResendEmailTwiceNegative()
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
        workflow.add(new QuoteResendEmailTwiceNegative());

        return workflow;
    }

    public static List<StepBase> QuoteResendEmailInvalidQuoteNumberNegative()
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
        workflow.add(new QuoteResendEmailInvalidQuoteNumberNegative());

        return workflow;
    }

    public static List<StepBase> QuoteResendEmailQuoteNumberDraftStateNegative()
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
        workflow.add(new QuoteResendEmailQuoteNumberDraftStateNegative());


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

    public static List<StepBase> PromotionsExport()
    {
        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadBaseFiles());
        // workflow.add(new GetOAuthCredentials());
        workflow.add(new GetPromotionsExportOAuthCredentials());
        workflow.add(new GetPromotionDetails());

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
     //   workflow.add(new QuoteDetails());
     //   workflow.add(new QuoteFinalize());
     //   workflow.add(new QuoteStatusByQuoteNumber(waitForExpectedStatus));
        workflow.add(new QuoteUpdateLineNumberMismatchedNeg());


        return workflow;
    }

    public static List<StepBase> QuoteUpdateLineNumberMismatchedSTGNeg()
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
        workflow.add(new QuoteUpdateLineNumberMismatchedSTGNeg());


        return workflow;
    }

    public static List<StepBase> QuoteUpdateActionAddPositive()
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
        workflow.add(new QuoteUpdateActionAddPositive());


        return workflow;
    }


    public static List<StepBase> QuoteUpdateActionAddSTGPositive()
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
        workflow.add(new QuoteUpdateActionAddSTGPositive());


        return workflow;
    }

    public static List<StepBase> QuoteUpdateActionAddSTGNeg()
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
        workflow.add(new QuoteUpdateActionAddSTGNeg());


        return workflow;
    }


    public static List<StepBase> QuoteUpdateActionUpdatePositive()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetails());
        //  workflow.add(new QuoteFinalize());
        //  workflow.add(new QuoteStatusByQuoteNumber(waitForExpectedStatus));
        workflow.add(new QuoteUpdateActionUpdatePositive());


        return workflow;
    }

    public static List<StepBase> UpdateQuoteV2OperationUpdateActionNew()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());
        //  workflow.add(new QuoteFinalize());
        //  workflow.add(new QuoteStatusByQuoteNumber(waitForExpectedStatus));
        workflow.add(new QuoteUpdateV2OperationUpdateActionNew());

        return workflow;
    }

    public static List<StepBase> UpdateQuoteV2OperationUpdateActionNewNeg()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetailsV2New());
        //  workflow.add(new QuoteFinalize());
        //  workflow.add(new QuoteStatusByQuoteNumber(waitForExpectedStatus));
        workflow.add(new QuoteUpdateV2OperationUpdateActionNewNeg());


        return workflow;
    }

    public static List<StepBase> QuoteUpdateActionUpdateSTGPositive()
    {
        boolean waitForExpectedStatus = true;

        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        workflow.add(new QuoteStatus(waitForExpectedStatus));
        workflow.add(new QuoteDetails());
        //  workflow.add(new QuoteFinalize());
        //  workflow.add(new QuoteStatusByQuoteNumber(waitForExpectedStatus));
        workflow.add(new QuoteUpdateActionUpdateSTGPositive());


        return workflow;
    }

    public static List<StepBase> QuoteUpdateActionRemoveLineNumberMismatchedNeg()
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
        workflow.add(new QuoteUpdateActionRemoveLineNumberMismatchedNeg());


        return workflow;
    }

    public static List<StepBase> QuoteUpdateActionRemoveLineNumberMismatchedSTGNeg()
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
        workflow.add(new QuoteUpdateActionRemoveLineNumberMismatchedSTGNeg());


        return workflow;
    }

    public static List<StepBase> QuoteUpdateActionRemoveNegative()
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
        workflow.add(new QuoteUpdateActionRemoveNegative());


        return workflow;
    }

    /*public static List<StepBase> QuoteUpdateActionRemovePositive()   // [Shailesh]- commented for Testing purpose
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
        workflow.add(new QuoteUpdateActionRemovePositive());


        return workflow;
    }*/

    public static List<StepBase> QuoteUpdateActionRemovePositive()
    {
        boolean waitForExpectedStatus = true;

       // List<StepBase> workflow = new ArrayList<StepBase>();
        List<StepBase> workflow = QuoteUpdateActionAddPositive();

        /*workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        workflow.add(new QuoteStatus(waitForExpectedStatus));*/
        //  workflow.add(new QuoteDetails());
        //  workflow.add(new QuoteFinalize());
        //  workflow.add(new QuoteStatusByQuoteNumber(waitForExpectedStatus));
        workflow.add(new QuoteUpdateActionRemovePositive());


        return workflow;
    }

    public static List<StepBase> QuoteUpdateActionRemoveSTGPositive()
    {
        boolean waitForExpectedStatus = true;

        // List<StepBase> workflow = new ArrayList<StepBase>();
        List<StepBase> workflow = QuoteUpdateActionAddSTGPositive();

        /*workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        workflow.add(new QuoteStatus(waitForExpectedStatus));*/
        //  workflow.add(new QuoteDetails());
        //  workflow.add(new QuoteFinalize());
        //  workflow.add(new QuoteStatusByQuoteNumber(waitForExpectedStatus));
        workflow.add(new QuoteUpdateActionRemoveSTGPositive());


        return workflow;
    }

    public static List<StepBase> GetQuoteDetailsInternalv2()
    {
        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteDetailsInternalv2ForgeCredentials());
        workflow.add(new QuoteDetailsInternalv2());

        return workflow;
    }

    public static List<StepBase> GetQuoteDetailsInternalv2UsingCreateQuoteV2()
    {
        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadQuoteFilesAndExtractData());
        workflow.add(new GetQuoteOAuthCredentials());
        workflow.add(new QuoteCreate());
        workflow.add(new GetQuoteDetailsInternalv2ForgeCredentials());
        workflow.add(new QuoteDetailsInternalv2());

        return workflow;
    }

    public static List<StepBase> GetQuoteNotification()
    {
        List<StepBase> workflow = new ArrayList<StepBase>();

        workflow.add(new LoadBaseFiles());
        // workflow.add(new GetOAuthCredentials());
        //workflow.add(new GetCatalogExportOAuthCredentials());
        workflow.add(new InvokeWebhook());

        return workflow;

    }



}
