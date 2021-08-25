package com.autodesk.pws.test.steps.utility;

import com.google.gson.Gson;
import io.restassured.path.json.JsonPath;

public class LoadRenewalFilesAndExtractData extends LoadBaseFiles
{
//	  "baseFile": "./testdata/WorkflowProcessing/TestData/BaseData/IntialOrderV2.DDA.json",
//	  "overridesFile": "./testdata/WorkflowProcessing/TestData/Overrides/Override.InitialOrderV2.DDA.json",
//	  "renewalRequestFile": "./testdata/WorkflowProcessing/TestData/BaseData/IntialOrderV2.DDA.json",
//	  "renewalOverridesFile": "./testdata/WorkflowProcessing/TestData/Overrides/Override.ValidDDARenewal.json",
	
    public void setConstructorDefaults()
    {
        RequestFileLabel = "renewalRequestFile";
        OverridesFileLabel = "renewalOverridesFile";
        DataPoolLabelOrderInfoRawJson = "rawRenewalFile";
        DataPoolLabelOrderInfoOverridesJson = "rawRenewalOverrideFile";
        DataPoolLabelOrderInfoFinalJson = "OrderInfoRenewal";
    }
	

    public void preparation()
    {
    	super.initBaseVariables();
    	initVariables();
    }

    private void initVariables()
    {
    	this.ClassName = this.getClass().getName();
    	
	}

	private void pullDataPoolVariables()
    {
    	//  Set variables that are extracted
		//  from the DataPool Here...
		
		//		reseller.csn = "0070176510";
		//		soldTo.csn = "0070176510";
		//		poNumber = "PO_{{po_number}}";
		//		customerPoNumber = "CUST_PO_{{cust_po_number}}";
		//		lineItems[0].quantity = 1;
		//		lineItems[0].netPrice = "{{net_price}}";
		//      lineItems[0].partNumber = "02KI1-WWN760-L231";
		
        //  Grab the final OrderInfo json and pop it into a JsonPath object...
		Gson gson = new Gson(); 
		String json = gson.toJson(DataPool.get(this.DataPoolLabelOrderInfoFinalJson)); 
    	JsonPath pathFinder = JsonPath.with(json);
    	
    	//  Extact data that may be needed by other steps later on...	
    	extractDataFromJsonAndAddToDataPool("$CUSTOMER_NUMBER$", "soldTo.csn", pathFinder); 
    	extractDataFromJsonAndAddToDataPool("$RESELLER_NUMBER$", "reseller.csn", pathFinder); 
    	extractDataFromJsonAndAddToDataPool("$PO_NUMBER$", "poNumber", pathFinder); 
    	extractDataFromJsonAndAddToDataPool("$CUSTOMER_PO_NUMBER$", "customerPoNumber", pathFinder); 
    	extractDataFromJsonAndAddToDataPool("$DDA_OPPORTUNITY_ID$", "orderDiscounts[0].discountId", pathFinder); 
    	
    	//  NOTE: There is a weakness in the WPE internal assumption, which presumes
    	//        that there will only be a single LineItem requested.  This will need
    	//        to be dealt with differently when multiple LineItems are in the request...
    	extractDataFromJsonAndAddToDataPool("$QUANTITY$", "lineItems[0].quantity", pathFinder); 
    	extractDataFromJsonAndAddToDataPool("$NET_PRICE$", "lineItems[0].netPrice", pathFinder); 
    	extractDataFromJsonAndAddToDataPool("$SKU_OR_PART_NUMBER$", "lineItems[0].partNumber", pathFinder); 
	}
	
    @Override
    public void action()
    {
        super.action();
        
        pullDataPoolVariables();
    }
}
