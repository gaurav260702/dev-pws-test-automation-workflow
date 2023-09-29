package com.autodesk.pws.test.steps.quote;

import com.autodesk.pws.test.steps.base.PwsServiceBase;
import io.restassured.path.json.JsonPath;

public class QuoteStatusERROR extends PwsServiceBase
{
	public String DataPoolSourceInfoLabel = "";
	protected boolean LoopTillExpectedStatus = false;
	protected Integer MaxRetryLoopCount = 12;
	protected Integer MilliSecondsSleepBeforeStatusAttempt = 10000;

	QuoteStatusERROR()
	{
		LoopTillExpectedStatus = false;
	}

	public QuoteStatusERROR(boolean useLoopTillExpectedStatus)
	{
		LoopTillExpectedStatus = useLoopTillExpectedStatus;
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
    	
		ExpectedEndStateStatus = "FAILED";
    	super.setExpectedEndState(this.ClassName);
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
		Integer flagForDelaysAt = 25;
		Integer retryCounter = 0;
		String finalStatus = "none";

		log("Executing [" + this.ClassName + "]");
		log("Expected end state value: " + ExpectedEndStateStatus);

		while (continueTrying) 
		{
			sleep(MilliSecondsSleepBeforeStatusAttempt);

			retryCounter += 1;
			
/*          //  
			if(retryCounter % OAuthTokenRefreshModulus == 0)
			{
				getOrderStatus.refreshOauthToken();
			}
*/
			if(retryCounter >= MaxRetryLoopCount)
			{
				continueTrying = false;
				finalStatus = "timeout";
			}
			else 
			{
				log("Attempt (" + retryCounter + ") of (" + MaxRetryLoopCount + ")...");

				super.action();

				String json = this.JsonResponseBody;

				JsonPath pathFinder = JsonPath.from(json);

				String status = pathFinder.get("quoteStatus");
				String faultString = pathFinder.get("fault.faultstring");
				String errMsg = pathFinder.get("error.message");
			//	String errMsg = pathFinder.get("errors[0].message");
				log("Quote Status Message Error: " + errMsg);
			//	String errCode = pathFinder.get("errors[0].code");
			//	log("Quote Status Code Error: " + errCode);
				String errStatus = pathFinder.get("status");
				
				if (status == null && faultString != null)
				{
					status = "fault";
				}
				
				if(errStatus != null)
				{
					status = errStatus;
				}
				
				if (errMsg != null && errMsg.length() > 0) 
				{
					errMsg = " - " + errMsg;
				} else {
					errMsg = "";
				}

				log("Current status: " + status + errMsg);
			//	log("Current status: " + status);

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
