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
		super.setResourcePath("/v1/quotes/status?transactionId=$TRANSACTION_ID$");
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
	
		/*
		this.log("****************************************************");
		this.log("****************************************************");
		this.log("*****  -----/ !!CRUDE POTATO BASHING!! \\-----  *****");
		
		this.log(" ");

		String transactionId = DataPool.get("$TRANSACTION_ID$").toString();
		this.log("Swapping out 'stubbedTransactionId' for '$TRANSACTION_ID$' in 'JsonResponseBody'...");
		this.JsonResponseBody = StringUtils.replace(JsonResponseBody, "stubbedTransactionId", transactionId);
		
		String fakeQuoteNumber = "Q-00600";
		this.log("Swapping out 'stubbedQuoteNumber' for '7265267' in 'JsonResponseBody'...");
		
		this.JsonResponseBody = StringUtils.replace(JsonResponseBody, "stubbedQuoteNumber", fakeQuoteNumber);

		this.log(" ");
		this.log("*****  -----|    BASHING COMPLETE.       |-----  *****");
		this.log("*****  -----\\       IZ POTATO.         //-----  *****");
		this.log("****************************************************");
		this.log("****************************************************");
		*/
		
		//  Here we would extract any data that needs to be promoted to 
		//  the DataPool and may be needed by other steps later on...
    	JsonPath pathFinder = JsonPath.with(JsonResponseBody);

    	//  Extact data that 	
    	extractDataFromJsonAndAddToDataPool("$TRANSACTION_ID$", "transactionId", pathFinder); 
    	extractDataFromJsonAndAddToDataPool("$QUOTE_NUMBER$", "quoteNumber", pathFinder); 
	}	
}
