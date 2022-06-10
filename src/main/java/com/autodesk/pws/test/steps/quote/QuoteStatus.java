package com.autodesk.pws.test.steps.quote;

import com.autodesk.pws.test.steps.base.*;
import io.restassured.path.json.JsonPath;

public class QuoteStatus extends PwsServiceBase 
{
	public String DataPoolSourceInfoLabel = "";
	protected boolean LoopTillExpectedStatus = false;
	public String ExpectedEndStateStatus = "DRAFT-CREATED";
	
	QuoteStatus()
	{
		this.LoopTillExpectedStatus = false;
	}
	
	public QuoteStatus(boolean useLoopTillExpectedStatus)
	{
		this.LoopTillExpectedStatus = useLoopTillExpectedStatus;
	}
	
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
		//attachHeaderFromDataPool("CSN", "$CSN_SECONDARY$");
		
		if(this.LoopTillExpectedStatus)
		{
			doActionLoop();
		}
		else
		{
			super.action();
		}
    }
	
	private void doActionLoop()
	{
		boolean continueTrying = true;
		Integer maxRetries = 600;
		Integer flagForDelaysAt = 25;
		Integer msSleepBeforeStatus = 10000;
		Integer retryCounter = 0;
		String finalStatus = "none";

		log("Executing [" + this.ClassName + "]");
		log("Expected end state value: " + ExpectedEndStateStatus);

		while (continueTrying) 
		{
			sleep(msSleepBeforeStatus);

			retryCounter += 1;
			
/*          //  
			if(retryCounter % OAuthTokenRefreshModulus == 0)
			{
				getOrderStatus.refreshOauthToken();
			}
*/
			if(retryCounter >= maxRetries)
			{
				continueTrying = false;
				finalStatus = "timeout";
			}
			else 
			{
				log("Attempt (" + retryCounter + ") of (" + maxRetries + ")...");

				super.action();

				String json = this.JsonResponseBody;

				JsonPath pathFinder = JsonPath.from(json);

				String status = pathFinder.get("status");
				String faultString = pathFinder.get("fault.faultstring");
				String statusMsg = pathFinder.getString("message");
				
				if (status == null && faultString != null) {
					status = "fault";
				}
				
				if (statusMsg != null && statusMsg.length() > 0) {
					statusMsg = " - " + statusMsg;
				} else {
					statusMsg = "";
				}

				log("Current status: " + status + statusMsg);

				if (status.matches(ExpectedEndStateStatus) ||
					status.toLowerCase().matches("error") || 
					status.toLowerCase().matches("failed") || 
					status.toLowerCase().matches("fault"))
				{
					continueTrying = false;
					finalStatus = status;
				}

				if (retryCounter >= flagForDelaysAt) 
				{
					// TODO: Create some way of reporting when waiting for the
					// OrderStatusToChange exceeds a reasonable amount of time...
				}
			}

			//this.SuppressLogging = true;
		}

		log("Final status: " + finalStatus);

		// This check should probably be migrated into the
		// "validation()" routine as the intention is to
		// cause an alteration of the default workflow...
		if (!finalStatus.matches(ExpectedEndStateStatus)) 
		{
			this.addResponseToValidationChain();
			ExceptionAbortStatus = true;
			ExceptionMessage = "Expected to reach '" + ExpectedEndStateStatus + "' state, but ended in '" + finalStatus + "' state!";
		}
	
	}
	
	@Override
	public void validation()
	{    	
		super.validation();
		
		//  Here we would extract any data that needs to be promoted to 
		//  the DataPool and may be needed by other steps later on...
    	JsonPath pathFinder = JsonPath.with(JsonResponseBody);

    	//  Extact data that 	
    	extractDataFromJsonAndAddToDataPool("$TRANSACTION_ID$", "transactionId", pathFinder); 
    	extractDataFromJsonAndAddToDataPool("$QUOTE_NUMBER$", "quoteNumber", pathFinder); 
	}	
}
