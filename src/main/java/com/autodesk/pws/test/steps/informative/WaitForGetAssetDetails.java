package com.autodesk.pws.test.steps.informative;

import com.autodesk.pws.test.steps.base.*;

import io.restassured.path.json.JsonPath;

public class WaitForGetAssetDetails extends StepBase
{
	private GetAssetDetails getAssetDetails = new GetAssetDetails();
	
	@Override
    public void preparation()
    {
		getAssetDetails.DataPool = this.DataPool;
		getAssetDetails.preparation();
    }

	@Override
    public void action()
    {
		boolean continueTrying = true;
		Integer maxRetries = 60;
		Integer msSleepBeforeStatus = 10000;
		Integer retryCounter = 0;
		boolean retriesExceeded = false;
		String status = "Waiting for service syncing and a non-zero length reply...";
		String statusMsg = "";
		
		log("Current status: " + status);
		
		String searchResult = "";
		String json = "";
		Object messageObject = null;
		JsonPath jsonPath = null;
		
		while(continueTrying)
		{
			sleep(msSleepBeforeStatus);
			
			retryCounter += 1;
			
			if(retryCounter >= maxRetries)
			{
				continueTrying = false;
				status =  "Timed out waiting for a non-zero length reply after (" + maxRetries + ") attempts!";
				retriesExceeded = true;
			}
			else
			{
				statusMsg = "[none]";
				
				log("Attempt (" + retryCounter + ") of (" + maxRetries + ")...");
				
				getAssetDetails.action();
				
				//  Should these declarations be outside the loop to 
				//  save CPU time and memory?  I really don't know if 
				//  Java has under the hood optimizers in these cases...
				json = getAssetDetails.JsonResponseBody.trim();
				jsonPath = new JsonPath(json);
				messageObject = jsonPath.get("endpointStatus.postgres_assets.message");
				
				if(messageObject != null)
				{
					searchResult = messageObject.toString();
					statusMsg = searchResult;
				}

  				if(!searchResult.matches("not found") && searchResult.length() > 0)
				{
					continueTrying = false;
					status = json.length() + " character reply...";
				}
			}
			
			log("WaitForGetAssetDetails status: " + status + " --  " + statusMsg);
			
			getAssetDetails.SuppressLogging = true;
		}
		
		log("Final status: " + status);
		
		if(retriesExceeded)
		{
			this.ExceptionAbortStatus = true;
			this.ExceptionMessage = status;
		}
    }

	@Override
    public void validation()
    {
		//  We call this here to be sure that the final JsonResponseBody
		//  is added to the ValidationChain...
		getAssetDetails.validation();
    }
}
