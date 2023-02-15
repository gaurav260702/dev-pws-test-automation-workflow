package com.autodesk.pws.test.steps.order;

import com.autodesk.pws.test.steps.base.*;
import io.restassured.path.json.JsonPath;

public class WaitForOrderStatusChange extends RestActionBase 
{
	protected GetOrderStatus getOrderStatus = new GetOrderStatus();
	// This state will be used to allow negative tests to be
	// successfully executed. The default expected state is
	// "accepted", however this state can be overriddent by
	// providing a new value in the Kicker file called
	// "WaitForOrderStatusChange.expectedEndStateStatus"...
	protected String expectedEndStateStatus = "accepted";
	public int OAuthTokenRefreshModulus = 75;

	@Override
	public void preparation() 
	{
		getOrderStatus.DataPool = this.DataPool;
		getOrderStatus.preparation();
		this.ClassName = this.getClass().getSimpleName();
		expectedEndStateStatus =
				DataPool.
					getOrDefault(
									this.ClassName + ".expectedEndStateStatus",
									expectedEndStateStatus
								).toString();
	}

	@Override
	public void action() 
	{
		boolean continueTrying = true;
		Integer maxRetries = 48;
		Integer flagForDelaysAt = 25;
		Integer msSleepBeforeStatus = 10000;
		Integer retryCounter = 0;
		String finalStatus = "none";

		log("Executing [" + getOrderStatus.ClassName + "]");
		log("Expected end state value: " + expectedEndStateStatus);

		while (continueTrying) 
		{
			sleep(msSleepBeforeStatus);

			retryCounter += 1;

			if(retryCounter % OAuthTokenRefreshModulus == 0)
			{
				getOrderStatus.refreshOauthToken();
			}

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

				// If the status message is blank, try another route...
				if (statusMsg == null || statusMsg.length() == 0) {
					statusMsg = pathFinder.getString("error.message") + " ["
							+ pathFinder.getString("error.code") + "]";
				}

				// If the status message is *still* blank, try yet *another* route...
				if (statusMsg == null || statusMsg.length() == 0) {
					statusMsg = pathFinder.getString("messageV1");
				}

				if (status == null && faultString != null) {
					status = "fault";
				}
				
				if (statusMsg != null && statusMsg.length() > 0) {
					statusMsg = " - " + statusMsg;
				} else {
					statusMsg = "";
				}

				if (statusMsg.contains("export control review"))
				{
					status = "exportControl";
				}
				
				if (statusMsg.contains("Order is under review"))
				{
					status = "review";
				}

				log("Current status: " + status + statusMsg);

				if (status.matches("accepted") ||
					status.matches("error") || 
					status.matches("failed") || 
					status.matches("fault") || 
					status.matches("exportControl") || 
					status.matches("review")) 
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

			//getOrderStatus.SuppressLogging = true;
		}

		log("Final status: " + finalStatus);

		// This check should probably be migrated into the
		// "validation()" routine as the intention is to
		// cause an alteration of the default workflow...
		if (!finalStatus.matches(expectedEndStateStatus)) 
		{
			addToValidationChain();

			ExceptionAbortStatus = true;
			ExceptionMessage = "Expected to reach '" + expectedEndStateStatus + "' state, but ended in '" + finalStatus + "' state!";
		}
	}

	private void addToValidationChain() 
	{
		// Call this to be sure that the final JsonResponseBody
		// is added to the ValidationChain...
		getOrderStatus.addResponseToValidationChain();
		
		//  Now overwriting the default GetOrderStatus class name
		//  with the name of this class so that the ValidationChain
		//  has an appropriate entry in it...
		getOrderStatus.ClassName = this.ClassName;
		getOrderStatus.validation();
	}

	@Override
	public void validation() 
	{
		super.validation();

		addToValidationChain();
	}
}
