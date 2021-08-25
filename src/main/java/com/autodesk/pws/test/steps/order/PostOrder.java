package com.autodesk.pws.test.steps.order;

import com.autodesk.pws.test.processor.DynamicData;
import com.autodesk.pws.test.steps.base.*;
import com.google.gson.Gson;

import io.restassured.path.json.JsonPath;

public class PostOrder extends PwsServiceBase
{    
	public String OrderInfoDataPoolLabel = "OrderInfo";
	
    @Override
    public void preparation()
    {
		//  Do some basic variable preparation...
    	
    	//  Need to set the ClassName here as this will be
    	//  used by the super/base classes ".preparation()" 
    	//  method.
		this.ClassName = this.getClass().getSimpleName();
		
		//  Set the ServiceVerb to a "POST" style service.
		//  We're doing this first in case there are any 
		//  init methods that may have a dependency on it.
		//  Naturally, we can't allow any 'setAs***Service()'
		//  methods to have any dependencies if it's being
		//  called before anything else...
		this.setAsPostService();
		
		//  Set the Resource path BEFORE the base/super class
		//  sets the targetUrl during the super class's
		//  "preparation()" method..
		setResourcePath();
		
    	//  Do stuff that the Action depends on to execute...
    	super.preparation();
    	
    	//  Grab the JsonRequestBody...
    	Gson gson = new Gson();
    	String jsonBody = gson.toJson(DataPool.get(OrderInfoDataPoolLabel));
    	jsonBody = DynamicData.detokenizeRuntimeValues(jsonBody);
    	this.setJsonRequestBody(jsonBody);
    }

    private void setResourcePath()
    {
		super.setResourcePath("/v2/orders/fulfillment");
    }

	@Override
    public void action()
    {
		super.action();
    }
	
	@Override
	public void validation()
	{    	
		super.validation();
	
		//  Here we would extract any data that needs to be promoted to 
		//  the DataPool and may be needed by other steps later on...
    	JsonPath pathFinder = JsonPath.with(JsonResponseBody);

    	//  Extact data that 	
    	extractDataFromJsonAndAddToDataPool("$TRANSACTION_ID$", "transactionId", pathFinder); 
	}	
}
