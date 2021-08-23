package com.autodesk.pws.test.steps.opportunity;

import okhttp3.*;
import com.autodesk.pws.test.steps.base.*;
import io.restassured.path.json.JsonPath;

public class GetOpportunityInfoByOpptyId extends PwsServiceBase
{
	public int MillisecondsBetweenGetOpptyInfoRetries = 10000;
	public int MaxGetOpptyInfoRetries = 30;
	
	private String finalStatus = "";
	
	@Override
	public void preparation()
  	{
	    initVariables();
  	}
	
	private void initVariables()
	{
	    this.ServiceVerb = "GET";

	    pullDataPoolVariables();
		setResourcePath();
	    initBaseVariables(); 
	    setTargetUrl();
	    
	    this.ClassName = this.getClass().getSimpleName();
	}
	
	private void pullDataPoolVariables()
	{
		BaseUrl =  DataPool.get("baseUrl").toString();
	}

	private void setResourcePath()
	{
		//  https://enterprise-api-stg.autodesk.com/v1/opportunities?partner_csn=0070176510&opportunity_id=A-15203372
		String targetPath = "/v1/opportunities?partner_csn=$CUSTOMER_NUMBER$&opportunity_id=$OPPORTUNITY_NUMBER$";
		super.setResourcePath(targetPath, true);
	}

	@Override	
	public void action()
	{
		//  Prep a response containers for evaulation and reporting...
		Response response = null;
		JsonPath pathFinder = null;

		//  Prep looping flags and vars...
		boolean keepTrying = true;
		boolean fullResponseFound = false;
		boolean setError = false;
		String status =  "";
		String statusMsg = "";
		int retryCount = 0;
		
		try
		{	
			while(keepTrying)
			{	
				response = getInfo();
				
				JsonResponseBody = response.body().string();

				pathFinder = JsonPath.from(JsonResponseBody);

				statusMsg = "[NONE]";
				
				//{
				//    "status": "Error",
				//    "error": 
				//    {
				//        "code": "22001",
				//        "message": "Invalid response from Server"
				//    }
				//}
				if(JsonResponseBody.contains("error"))
				{
					status = pathFinder.get("error");
					statusMsg = pathFinder.get("error.message");
				}
				else
				{
					status = pathFinder.get("status");
					
					//"status": "OK",
					//"message": [
					//    {
					//        "opportunity_number": "A-15202879",
					//        "opportunity_type": "Renewal",
					//        "status": "Open",
					//      ...
					if(JsonResponseBody.contains("opportunity_type"))
					{
						statusMsg = pathFinder.getString("message.status");
						fullResponseFound = true;
					}
					else
					{
						statusMsg = pathFinder.getString("message");
					}
				}
		
				log("GetOpptyInfo status: " + status + " -- " + statusMsg);
				
				switch(status.toUpperCase())
				{					
					case "ERROR":
						keepTrying = false;
						setError = true;
						break;
					
					case "OK":
						if(fullResponseFound)
						{
							keepTrying = false;
							retryCount+=1;
						}
						break;
						
					default:
						keepTrying = true;
						retryCount+=1;
						break;
				}
			
				
				if(retryCount >= this.MaxGetOpptyInfoRetries)
				{
					status = "Timeout";
					keepTrying = false;
				}
				
				if(keepTrying) 
				{
					this.sleep(MillisecondsBetweenGetOpptyInfoRetries);
				}
			}
			
			finalStatus = status;
		}
		catch (Exception e)
		{
			setError = true;
			finalStatus = e.toString();
	    }
		
		if(setError)
		{
			logErr(status, this.ClassName, "action");
			this.ExceptionAbortStatus = true;
			this.ExceptionMessage = this.ClassName + ".action() -- " + statusMsg;
		}
	}

	@Override
	public void validation()
	{    	
		super.validation();
	
		//  Here we would extract any data that needs to be promoted to 
		//  the DataPool and may be needed by other steps later on...
		JsonPath pathFinder = JsonPath.with(JsonResponseBody);
	
		addResponseToValidationChain();
	}	

}
