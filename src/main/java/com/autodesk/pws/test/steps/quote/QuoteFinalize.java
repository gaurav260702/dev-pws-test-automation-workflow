package com.autodesk.pws.test.steps.quote;

import com.autodesk.pws.test.processor.DynamicData;
import com.autodesk.pws.test.steps.base.*;
import io.restassured.path.json.JsonPath;

public class QuoteFinalize extends PwsServiceBase 
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
		
		//  Set the ServiceVerb to a "PATCH" style service.
		//  We're doing this first in case there are any 
		//  init methods that may have a dependency on it.
		//  Naturally, we can't allow any 'setAs***Service()'
		//  methods to have any dependencies if it's being
		//  called before anything else...
		this.setServiceVerb("PATCH");
		
		//  Set the Resource path BEFORE the base/super class
		//  sets the targetUrl during the super class's
		//  "preparation()" method..
		setResourcePath();

    	//  Do stuff that the Action depends on to execute...
    	super.preparation();
    	
    	//  Having a "PATCH" method normally requires a body, but 
    	//  for some reason this service was implemented without
    	//  actually needing a real JSON body.  To get around that
    	//  we're essentially sticking in a zero-data JSON body...
    	String jsonBody = "{ \"foobar\": \"foo\" }";
    	//String jsonBody = gson.toJson(rawJson);
    	jsonBody = DynamicData.detokenizeRuntimeValues(jsonBody);
    	this.setJsonRequestBody(jsonBody);
    	
    	setTargetUrl();
    }

    private void setResourcePath()
    {
		super.setResourcePath("/v1/quotes/finalize/$QUOTE_NUMBER$");
    }

	@Override
    public void action()
    {
		attachHeaderFromDataPool("CSN", "$CSN_SECONDARY$");
		
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
    	//extractDataFromJsonAndAddToDataPool("$TRANSACTION_ID$", "transactionId", pathFinder); 
    	//extractDataFromJsonAndAddToDataPool("$QUOTE_NUMBER$", "quoteNumber", pathFinder); 
	}	
}
