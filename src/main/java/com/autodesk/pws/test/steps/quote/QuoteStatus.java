package com.autodesk.pws.test.steps.quote;

import com.autodesk.pws.test.steps.base.*;
import io.restassured.path.json.JsonPath;

public class QuoteStatus extends PwsServiceBase 
{
	public String DataPoolSourceInfoLabel = "";
	
    @Override
    public void preparation()
    {
		//  Do some basic variable preparation...
    	
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
		this.setAsGetService();
		
		//  Set the Resource path BEFORE the base/super class
		//  sets the targetUrl during the super class's
		//  "preparation()" method..
		setResourcePath();

    	//  Do stuff that the Action depends on to execute...
    	super.preparation();
    	
    	setTargetUrl();
    }

    private void setResourcePath()
    {
		super.setResourcePath("/v1/status/?transactionId=$TRANSACTION_ID$");
    }

	@Override
    public void action()
    {
		//attachHeaderFromDataPool("x-api-key", "createQuoteXApiKey");
		//attachHeaderFromDataPool("CSN", "$CSN_QUOTE_STATUS$");
		attachHeaderFromDataPool("CSN", "$CSN_PRIMARY$");
		
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
    	extractDataFromJsonAndAddToDataPool("$QUOTE_NUMBER$", "quoteNumber", pathFinder); 
	}	
}
