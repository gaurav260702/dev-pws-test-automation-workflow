package com.autodesk.pws.test.steps.order;

import com.autodesk.pws.test.steps.base.*;

import io.restassured.path.json.JsonPath;

public class WaitForOrderStatusChange extends StepBase
{
	private GetOrderStatus getOrderStatus = new GetOrderStatus();
	
	@Override
    public void preparation()
    {
		getOrderStatus.DataPool = this.DataPool;
		getOrderStatus.preparation();
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
			try 
			{
				log("Sleeping " + (msSleepBeforeStatus / 1000) + " seconds...");
				Thread.sleep(msSleepBeforeStatus);
			} 
			catch (InterruptedException e) 
			{
				this.logErr(e, this.ClassName, "action");
			}
			
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
				
				log("Current status: " + status);
				
				if(status.matches("accepted") || status.matches("error"))
				{
					continueTrying = false;
					finalStatus = status;
				}
			}
			
			getOrderStatus.SuppressLogging = true;
		}
		
		log("Final status: " + finalStatus);
		
		if(!finalStatus.matches("accepted"))
		{
			ExceptionAbortStatus = true;
			ExceptionMessage = "Expected to reach 'accepted' state, but ended in '" + finalStatus + "' state!";
		}
    }

	@Override
    public void validation()
    {
		//  We call this here to be sure that the final JsonResponseBody
		//  is added to the ValidationChain...
		getOrderStatus.validation();
    }
}
