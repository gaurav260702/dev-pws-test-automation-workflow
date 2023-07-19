package com.autodesk.pws.test.steps.quote;

import com.autodesk.pws.test.processor.DynamicData;
import com.autodesk.pws.test.steps.base.*;
import io.restassured.path.json.JsonPath;

public class QuoteCreate extends PwsServiceBase 
{
	public String DataPoolSourceInfoLabel = "";
	
    @Override
    public void preparation()
    {
		//  Do some basic variable preparation...
    	this.UseAlternateAuthHeaderGenerationMethod = true;
    	
    	//  Need to set the ClassName here as this will be
    	//  used by the super/base classes ".preparation()" 
    	//  method.
		this.ClassName = this.getClass().getSimpleName();
		this.DataPoolSourceInfoLabel = "raw" + this.ClassName;
		
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
    	String jsonBody = DataPool.get(DataPoolSourceInfoLabel).toString();
    	jsonBody = DynamicData.detokenizeRuntimeValues(jsonBody);
    	this.setJsonRequestBody(jsonBody);
    	
    	//   https://quote.ddwsdev.autodesk.com
    	this.BaseUrl = "$CREATE_QUOTE_BASE_URL$";
    	setTargetUrl();
    	
    	//this.ExpectedResponseMessage = "Accepted";
    	this.ExpectedEndStateStatus = "Accepted";
    	
    	this.setExpectedEndState(this.ClassName);
    }

    private void setResourcePath()
    {
		super.setResourcePath("v2/quotes");
		// quote.ddwsdev.autodesk.com/v1/status/ 
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
    	
    	if(ExpectedResponseMessage.compareTo(ActualResponseMessage) != 0)
    	{
    		this.ExceptionMessage = "'" + this.ClassName + "' was expecting a response message of '" + this.ExpectedResponseMessage + "' but ended in a '" + this.ActualResponseMessage + "' response message!";
    		this.ExceptionAbortStatus = true;
    	}
	}	
}
