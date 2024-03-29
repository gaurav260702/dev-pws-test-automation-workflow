package com.autodesk.pws.test.steps.utility;

import com.google.gson.*;

import io.restassured.path.json.JsonPath;

public class LoadBaseFilesAndExtractOrderInfoData extends LoadBaseFiles
{
    @Override
    public void preparation()
    {
    	log("Calling super class 'initBaseVariables()'...");
    	super.initBaseVariables();
    	log("Calling local 'initVariables()'...");
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
		String json = gson.toJson(DataPool.getRaw(this.DataPoolLabelOrderInfoFinalJson)); 
    	JsonPath pathFinder = JsonPath.with(json);
    	
    	//  Extact data that may be needed by other steps later on...	
    	extractDataFromJsonAndAddToDataPoolIfNull("$CUSTOMER_NUMBER$", "endCustomer.account.csn", pathFinder); 
    	extractDataFromJsonAndAddToDataPoolIfNull("$RESELLER_NUMBER$", "reseller.csn", pathFinder); 
    	extractDataFromJsonAndAddToDataPoolIfNull("$PO_NUMBER$", "poNumber", pathFinder); 
    	extractDataFromJsonAndAddToDataPoolIfNull("$CUSTOMER_PO_NUMBER$", "customerPoNumber", pathFinder); 
	
    	//  NOTE: There is a weakness in the WPE internal assumption, which presumes
    	//        that there will only be a single LineItem requested.  This will need
    	//        to be dealt with differently when multiple LineItems are in the request...
		extractDataFromJsonAndAddToDataPoolIfNull("$QUANTITY$", "lineItems[0].quantity", pathFinder); 
		extractDataFromJsonAndAddToDataPoolIfNull("$NET_PRICE$", "lineItems[0].netPrice", pathFinder); 
		extractDataFromJsonAndAddToDataPoolIfNull("$SKU_OR_PART_NUMBER$", "lineItems[0].partNumber", pathFinder); 
	}
	
    @Override
    public void action()
    {
    	log("Calling LoadBaseFiles.action()");
        super.action();
        
        log("Pulling DataPool variables");
        pullDataPoolVariables();
    }
}

