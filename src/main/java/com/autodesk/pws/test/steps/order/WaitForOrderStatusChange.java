package com.autodesk.pws.test.steps.order;

import com.autodesk.pws.test.steps.base.*;

import io.restassured.path.json.JsonPath;

public class WaitForOrderStatusChange extends StepBase
{
	private GetOrderStatus getOrderStatus = new GetOrderStatus();
	//  This state will be used to allow negative tests to be
	//  successfully executed.  The default expected state is 
	//  "accepted", however this state can be overriddent by
	//  providing a new value in the Kicker file called
	//  "WaitForOrderStatusChange.expectedEndStateStatus"...
	protected String expectedEndStateStatus = "accepted";
	
	@Override
    public void preparation()
    {
		getOrderStatus.DataPool = this.DataPool;
		getOrderStatus.preparation();
		expectedEndStateStatus = DataPool.getOrDefault("WaitForOrderStatusChange.expectedEndStateStatus", expectedEndStateStatus).toString(); 
		log("Expected end state value: " + expectedEndStateStatus);
    }

	@Override
    public void action()
    {
		boolean continueTrying = true;
		Integer maxRetries = 30;
		Integer msSleepBeforeStatus = 10000;
		Integer retryCounter = 0;
		String finalStatus = "none";
		
		while(continueTrying)
		{
			sleep(msSleepBeforeStatus);
			
			retryCounter += 1;
			
			if(retryCounter >= maxRetries)
			{
				continueTrying = false;
				finalStatus = "timeout";
			}
			else
			{
				log("Attempt (" + retryCounter + ") of (" + maxRetries + ")...");
				
				getOrderStatus.action();
				
				String json = getOrderStatus.JsonResponseBody;
				
				JsonPath pathFinder = JsonPath.from(json);
				
				String status = pathFinder.get("status");
				String faultString = pathFinder.get("fault.faultstring");
				String statusMsg = pathFinder.getString("message");
				
				if(status == null && faultString != null)
				{
					status = "fault";
				}
				
				if(statusMsg != null && statusMsg.length() > 0)
				{
					statusMsg = " - " + statusMsg;
				}
				else
				{
					statusMsg = "";
				}
				
				log("Current status: " + status + statusMsg);
				
				if(status.matches("accepted") || status.matches("error") || status.matches("failed") || status.matches("fault"))
				{
					continueTrying = false;
					finalStatus = status;
				}
			}
			
			getOrderStatus.SuppressLogging = true;
		}
		
		log("Final status: " + finalStatus);
		
		//  This check should probably be migrated into the 
		//  "validation()" routine as the intention is to 
		//  cause an alteration of the default workflow...
		if(!finalStatus.matches(expectedEndStateStatus))
		{
			getOrderStatus.addResponseToValidationChain();
			ExceptionAbortStatus = true;
			ExceptionMessage = "Expected to reach '" + expectedEndStateStatus + "' state, but ended in '" + finalStatus + "' state!";
		}
    }

	@Override
    public void validation()
    {
		super.validation();
		
		//  We call this here to be sure that the final JsonResponseBody
		//  is added to the ValidationChain...
		getOrderStatus.validation();
    }
}
