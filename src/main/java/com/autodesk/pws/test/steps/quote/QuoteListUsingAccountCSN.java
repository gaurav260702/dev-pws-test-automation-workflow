package com.autodesk.pws.test.steps.quote;

import com.autodesk.pws.test.steps.base.PwsServiceBase;
import io.restassured.path.json.JsonPath;

public class QuoteListUsingAccountCSN extends PwsServiceBase
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
    	
    	//  Grab the JsonRequestBody...
    	//Gson gson = new Gson();
    	//String jsonBody = DataPool.get(DataPoolSourceInfoLabel).toString();
    	//String jsonBody = gson.toJson(rawJson);
    	//jsonBody = DynamicData.detokenizeRuntimeValues(jsonBody);
    	//this.setJsonRequestBody(jsonBody);
    	
    	//   https://quote.ddwsdev.autodesk.com
    	this.BaseUrl = "$CREATE_QUOTE_BASE_URL$";
    	setTargetUrl();
    }

    private void setResourcePath()
    {
    	// https://quote.ddwsdev.autodesk.com/v1/details?quoteNumber=7265267
	//	super.setResourcePath("v2/quotes?quoteNumber=$QUOTE_NUMBER$");
	//	super.setResourcePath("v2/quotes?filter[quoteNumber]=$QUOTE_NUMBER$");
		super.setResourcePath("$VERSION_PATH$/quotes?filter[$ACCOUNT_TYPE$]=$END_CUSTOMER_CSN$");
    }

	@Override
    public void action()
    {
		//attachHeaderFromDataPool("CSN", "$CSN_ACCOUNT_CONTACT$");
		
		super.action();
    }
	
	@Override
	public void validation()
	{    	
		super.validation();
	
		//  Here we would extract any data that needs to be promoted to 
		//  the DataPool and may be needed by other steps later on...
    	JsonPath pathFinder = JsonPath.with(JsonResponseBody);
		extractDataFromJsonAndAddToDataPool("$QUOTE_LINE_NUMBER$", "lineItems[0].quoteLineNumber[0]", pathFinder);
	//	extractDataFromJsonAndAddToDataPool("$QUOTE_LINE_NUMBER1$", "lineItems[1].quoteLineNumber[1]", pathFinder);

    	//  Extact data that 	
    	//extractDataFromJsonAndAddToDataPool("$TRANSACTION_ID$", "transactionId", pathFinder); 
	}	
}
