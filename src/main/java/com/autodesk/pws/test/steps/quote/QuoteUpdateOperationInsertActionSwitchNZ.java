package com.autodesk.pws.test.steps.quote;

import com.autodesk.pws.test.steps.base.PwsServiceBase;
import io.restassured.path.json.JsonPath;

public class QuoteUpdateOperationInsertActionSwitchNZ extends PwsServiceBase
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
		
		
		//  Have some crappy fall-through code here, but this
		//  should only be temporary until the STG environment
		//  gets updated to the same code base as INT environment...
		this.setServiceVerb("PUT");
		
		//  Set the Resource path BEFORE the base/super class
		//  sets the targetUrl during the super class's
		//  "preparation()" method..
		setResourcePath();

    	//  Do stuff that the Action depends on to execute...
    	super.preparation();
    	
    //	ExpectedEndStateStatus = "error";
		super.setExpectedEndState(this.ClassName);
    	
    	//  Having a "PATCH" method requires a body.
    	//  This should be stored in an external file, but
    	//  for the moment we're going to embed it in the 
    	//  class as I don't want to deal with creating a
    	//  loader/extracter at this time...
    //	String jsonBody = "{\"quoteNumber\":\"$QUOTE_NUMBER$\"}";  $QUOTE_LINE_NUMBER$
		String jsonBody = "{\n" +
				"  \"quoteNumber\": \"$QUOTE_NUMBER$\",\n" +
				"  \"lineItems\": [\n" +
				"    {\n" +
				"      \"operation\": \"Insert\",\n" +
				"      \"offeringId\": \"OD-000054\",\n" +
				"      \"action\": \"Switch\",\n" +
				"      \"quantity\": 7,\n" +
				"      \"referenceSubscriptionId\": \"70112474840084\",\n" +
				"      \"offer\": {\n" +
				"        \"term\": {\n" +
				"          \"code\": \"A01\",\n" +
				"          \"description\": \"Annual\"\n" +
				"        },\n" +
				"        \"accessModel\": {\n" +
				"          \"code\": \"S\",\n" +
				"          \"description\": \"Single User\"\n" +
				"        },\n" +
				"        \"intendedUsage\": {\n" +
				"          \"code\": \"COM\",\n" +
				"          \"description\": \"Commercial\"\n" +
				"        },\n" +
				"        \"connectivity\": {\n" +
				"          \"code\": \"C100\",\n" +
				"          \"description\": \"Online\"\n" +
				"        },\n" +
				"        \"servicePlan\": {\n" +
				"          \"code\": \"STND\",\n" +
				"          \"description\": \"Standard\"\n" +
				"        }\n" +
				"      }\n" +
				"    }\n" +
				"  ]\n" +
				"}";

    	jsonBody = this.fullyDetokenize(jsonBody);
    	
    	this.setJsonRequestBody(jsonBody);
    	
    	setTargetUrl();
    }

    private void setResourcePath()
    {
		super.setResourcePath("/$VERSION_PATH$/quotes");
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
		
		//  :::TODO:::
		//  Need to abstract this whole mechanism into the "RestActionBase" 
		//  or the "PwsServiceBase" set of methods and properties...
		String json = this.JsonResponseBody;
		JsonPath pathFinder = JsonPath.from(json);
		String finalStatus = pathFinder.get("status");

		/*if (!finalStatus.matches(ExpectedEndStateStatus))
		{
			this.addResponseToValidationChain();
			ExceptionAbortStatus = true;
			ExceptionMessage = "Expected to reach '" + ExpectedEndStateStatus + "' state, but ended in '" + finalStatus + "' state!";
		}*/
		//  :::TODO:::
	}	
}
