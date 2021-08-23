package com.autodesk.pws.test.steps.opportunity;

import okhttp3.*;
import com.autodesk.pws.test.steps.base.*;
import io.restassured.path.json.JsonPath;

public class CreateOpptyByAgreementId extends PwsServiceBase
{
	@Override
	public void preparation()
  	{
	    initVariables();
  	}
	
	private void initVariables()
	{
		super.preparation();
	    initBaseVariables();
	    this.ClassName = this.getClass().getSimpleName();
	    pullDataPoolVariables();
	    setJsonRequestBody();
	}
	
	private void setJsonRequestBody() 
	{
		//  This reques thas a very simple structure, which (in general) can't be filled out
		//  prior to runtime, so it's easier to attach the JSON request body inline than
		//  to create the structures needed to support reading it from a test data file...
		String requestBody = "{\"agreementNumber\":\"$AGREEMENT_NUMBER$\",\"type\":\"Renewal Opportunity\"}";
		super.setJsonRequestBody(requestBody);
	}

	private void pullDataPoolVariables()
	{
		setResourcePath();
		BaseUrl =  DataPool.get("opptyServiceBaseUrl").toString();
	}

	private void setResourcePath()
	{
		super.setResourcePath("/opportunity-service/v1/opportunity/create-opportunity-by-agreement-number");
	}

	@Override	
	public void action()
	{
		this.log("Forced sleep to ensure SalesForce is synced and ready to accept new opportunity request...");
		sleep(60000);
		
		//  Call the method that does the meat of the work...
		Response actionResult = getInfo();

		try
		{
			JsonResponseBody = actionResult.body().string();
		}
		catch (Exception e)
		{
			this.logErr(e, this.ClassName, "action");
		}
	}

	public Response getInfo()
	{
		//  Get the appropriate headers for a token request...
		this.RequestHeaders = generateAccessTokenHeadersWithCurrentToken();
		addHeaderFromDataPool("x-api-key", "opptyXApiKey");
		
		Response response = null;

		boolean keepTrying = true;
		int retryCount = 0;
		int maxRetries = 30;
		int millisecondsSleepBetweenRetries = 10000;
		boolean successful = false;
		
		try
		{
			String targetUrl = BaseUrl + this.ResourcePath;
			
			while(keepTrying)
			{
				log("Attempt (" + retryCount + ") of " + maxRetries + "...");

				response = getRestResponse("POST", targetUrl, JsonRequestBody);
				
				if(response.code() == 200)
				{
					successful = true;
					keepTrying = false;
				}
				else
				{
					log("Status: " + response.code() + " -- " + response.message());
					retryCount +=1;
					sleep(millisecondsSleepBetweenRetries);
					
				}
				
				if(retryCount >= maxRetries)
				{
					keepTrying = false;
				}
			}
		}
		catch (Exception e)
		{
			logErr(e, this.ClassName, "getInfo");
	    }

		if(!successful)
		{
			this.ExceptionAbortStatus = true;
			this.ExceptionMessage = "Unable to create new opportunity after (" + retryCount + ") attempts!";
		}
		
		return response;
	}
		  
	@Override
	public void validation()
	{    	
		super.validation();
	
		//  Here we would extract any data that needs to be promoted to 
		//  the DataPool and may be needed by other steps later on...
		JsonPath pathFinder = JsonPath.with(JsonResponseBody);
		
		//  Extact data that 	
		extractDataFromJsonAndAddToDataPool("$OPPORTUNITY_TRANSACTION_ID$", "transactionId", pathFinder); 
	}	
}

