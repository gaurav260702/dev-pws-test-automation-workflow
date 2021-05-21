package com.autodesk.pws.test.steps.utility;

import com.google.gson.*;

import io.restassured.path.json.JsonPath;

public class LoadAddOnFilesAndExtractAddOnInfoData extends LoadBaseFiles
{
    @Override
    public void preparation()
    {
    	//  Note that we're *>NOT<* calling the 
    	//  super.initBaseVariables() method here! 
    	initBaseVariables();
    	initVariables();
    }

    public void initBaseVariables()
    {
        OverridesFileLabel = "addOnOverridesFile";
        DataPoolLabelOrderInfoRawJson = "rawAddOnFile";
        DataPoolLabelOrderInfoOverridesJson = "rawAddOnOverrideFile";
        DataPoolLabelOrderInfoFinalJson = "AddOnInfo";
    }
    
    private void initVariables()
    {
    	this.ClassName = this.getClass().getName();
    }

	private void pullDataPoolVariables()
    {
    	//  Set variables that are extracted
		//  from the DataPool Here...
		
        //  Grab the final OrderInfo json and pop it into a JsonPath object...
		Gson gson = new Gson(); 
		String json = gson.toJson(DataPool.get(this.DataPoolLabelOrderInfoFinalJson)); 
    	JsonPath pathFinder = JsonPath.with(json);
    	
    	//  Extact data that may be needed by other steps later on...	
		//    	extractDataFromJsonAndAddToDataPool("$CUSTOMER_NUMBER$", "soldTo.csn", pathFinder); 
		//    	extractDataFromJsonAndAddToDataPool("$RESELLER_NUMBER$", "reseller.csn", pathFinder); 
		//    	extractDataFromJsonAndAddToDataPool("$PO_NUMBER$", "poNumber", pathFinder); 
		//    	extractDataFromJsonAndAddToDataPool("$CUSTOMER_PO_NUMBER$", "customerPoNumber", pathFinder); 
    	extractDataFromJsonAndAddToDataPool("$ADD_ON_QUANTITY$", "lineItems[0].quantity", pathFinder); 
    	extractDataFromJsonAndAddToDataPool("$ADD_ON_NET_PRICE$", "lineItems[0].netPrice", pathFinder); 
    	extractDataFromJsonAndAddToDataPool("$ADD_ON_SKU_OR_PART_NUMBER$", "lineItems[0].partNumber", pathFinder); 
    	
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

