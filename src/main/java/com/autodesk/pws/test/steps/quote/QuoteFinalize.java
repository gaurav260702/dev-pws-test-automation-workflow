package com.autodesk.pws.test.steps.quote;

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
    	
    	//  Having a "PATCH" method requires a body.
    	//  This should be stored in an external file, but
    	//  for the moment we're going to embed it in the 
    	//  class as I don't want to deal with creating a
    	//  loader/extracter at this time...
    	String jsonBody = "{\"quoteNumber\":\"$QUOTE_NUMBER$\",\"agentAccount\":{\"accountCsn\":\"$CSN_HEADER$\"},\"agentContact\":{\"email\":\"partneruser_da_int_0070000339_2@letscheck.email\"}}";
    	jsonBody = this.fullyDetokenize(jsonBody);
    	
    	this.setJsonRequestBody(jsonBody);
    	
    	setTargetUrl();
    }

    private void setResourcePath()
    {
		super.setResourcePath("/v1/quotes/finalize");
    }

	@Override
    public void action()
    {
		// attachHeaderFromDataPool("CSN", "$CSN_SECONDARY$");
		
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
