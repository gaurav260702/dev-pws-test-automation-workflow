package com.autodesk.pws.test.steps.opportunity;

import okhttp3.*;
import com.autodesk.pws.test.steps.base.*;
import io.restassured.path.json.JsonPath;

public class GetOpptyStatusByOpportunityTransactionId extends PwsServiceBase
{
	public int MillisecondsBetweenGetOpptyRetries = 10000;
	public int MaxGetOpptyRetries = 10;
	
	private String finalStatus = "";
	
	@Override
	public void preparation()
  	{
	    initVariables();
  	}
	
	private void initVariables()
	{
	    initBaseVariables();
	    this.ClassName = this.getClass().getSimpleName();
	    pullDataPoolVariables();
	}
	
	private void pullDataPoolVariables()
	{
		BaseUrl =  DataPool.get("opptyServiceBaseUrl").toString();
		setResourcePath();
	}

	private void setResourcePath()
	{
		String targetPath = "/opportunity-service/v1/opportunity/create-opportunity-by-agreement-number/status/$OPPORTUNITY_TRANSACTION_ID$";
		super.setResourcePath(targetPath, true);
	}

	@Override	
	public void action()
	{
		//  Call the method that does the meat of the work...
		//  We're going to be looping in this version of it
		//  and all the parsing and extraction will be handled
		//  by the loop itself, so we only need to call the
		//  method and not worry about any response...
		getInfo();
	}

	public Response getInfo()
	{
		//  Get the appropriate headers for a token request...
		this.RequestHeaders = generateAccessTokenHeadersWithCurrentToken();
		addHeaderFromDataPool("x-api-key", "opptyXApiKey");

		Response response = null;

		boolean keepTrying = true;
		
		String targetUrl = BaseUrl + this.ResourcePath;
		JsonPath pathFinder = null;
		String status =  "";
		int retryCount = 0;
		
		try
		{	
			while(keepTrying)
			{
				response = getRestResponse("GET", targetUrl);
				
				JsonResponseBody = response.body().string();

				pathFinder = JsonPath.from(JsonResponseBody);

				//  [{"errorCode": "SYSTEM_ERROR", "message": "There was system error - Request failed. Response: [{'message': 'ConcurrentRequests (Concurrent API Requests) Limit exceeded.', 'errorCode': 'REQUEST_LIMIT_EXCEEDED'}]"}]
				if(JsonResponseBody.contains("errorCode"))
				{
					status = pathFinder.get("errorCode") + " -- " + pathFinder.get("message");
				}
				else if(JsonResponseBody.contains("status"))
				{
					status = pathFinder.get("status");
				}
				
				if(response.code() != 200)
				{
					status = response.code() + " -- " + pathFinder.get("message");
				}
				
				log("GetOppty status -- " + status);
				
				switch(status)
				{
					case "Success":						
					case "Failed":
						keepTrying = false;
						break;
						
					default:
						keepTrying = true;
						retryCount+=1;
						break;
				}
			
				
				if(retryCount >= this.MaxGetOpptyRetries)
				{
					status = "Timeout";
					keepTrying = false;
				}
				
				if(keepTrying) 
				{
					this.sleep(MillisecondsBetweenGetOpptyRetries);
				}
			}
			
			finalStatus = status;
		}
		catch (Exception e)
		{
			logErr(e, this.ClassName, "getInfo");
			this.ExceptionAbortStatus = true;
			this.ExceptionMessage = this.ClassName + ".getInfo() -- " + e.getMessage();
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
	
		String finalMessage = "";
		
		switch(finalStatus)
		{
			case "Success":		
				//finalMessage = pathFinder.get("opportunities"); 		
				break;
				
			case "Failed":
				finalMessage = pathFinder.get("errors"); 
				this.ExceptionAbortStatus = true;
				this.ExceptionMessage = this.ClassName + ".getInfo() -- " + finalMessage;
				break;
				
			case "Timeout":
				finalMessage = "Timeout after (" + this.MaxGetOpptyRetries + ") GetOpptyStatus retries!";
				break;
		}
		
		//  Extact relevant data from JSON response body...
		extractDataFromJsonAndAddToDataPool("$OPPORTUNITY_SERVICE_IDS$", "opportunities", pathFinder); 
		
		//  Properly format the opportunity (assuming only a single oppty here)
		//  and add it to the DataPool...
		String opptyId = pathFinder.getString("opportunities");
		opptyId = opptyId.substring(opptyId.indexOf("[") + 1, opptyId.indexOf("]"));
		DataPool.add("$OPPORTUNITY_NUMBER$", opptyId);
	}	
}
