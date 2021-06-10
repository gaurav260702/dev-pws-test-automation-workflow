package com.autodesk.pws.test.steps.informative;

import com.autodesk.pws.test.steps.base.*;

public class WaitForGetAgreementInfo extends StepBase
{
	private GetAgreementInfo getAgreementInfo = new GetAgreementInfo();
	
	@Override
    public void preparation()
    {
		getAgreementInfo.DataPool = this.DataPool;
		getAgreementInfo.preparation();
    }

	@Override
    public void action()
    {
		boolean continueTrying = true;
		Integer maxRetries = 30;
		Integer msSleepBeforeStatus = 10000;
		Integer retryCounter = 0;
		String status = "Waiting for service syncing and a non-zero length reply...";
		log("Current status: " + status);
		
		while(continueTrying)
		{
			sleep(msSleepBeforeStatus);
			
			retryCounter += 1;
			
			if(retryCounter >= maxRetries)
			{
				continueTrying = false;
				status =  "Timed out waiting for a non-zero length reply!";
			}
			else
			{
				log("Attempt (" + retryCounter + ") of (" + maxRetries + ")...");
				
				getAgreementInfo.action();
				
				String json = getAgreementInfo.JsonResponseBody.trim();
								
				if(json.length() > 8)
				{
					continueTrying = false;
					status = json.length() + " character reply...";
				}
			}
			
			getAgreementInfo.SuppressLogging = true;
		}
		
		log("Final status: " + status);
    }

	@Override
    public void validation()
    {
		//  We call this here to be sure that the final JsonResponseBody
		//  is added to the ValidationChain...
		getAgreementInfo.validation();
		
		//  Depending on what we're doing, we may need the serialnumber...
		getAgreementInfo.extractDataFromJsonAndAddToDataPool("$SUBSCRIPTION_REFERENCE_NUMBER$", "[0].ServiceContract[0].ServiceLevels[0].ContractLineItems[0].SerialNumber");
		getAgreementInfo.extractDataFromJsonAndAddToDataPool("$SERIAL_NUMBER$", "[0].ServiceContract[0].ServiceLevels[0].ContractLineItems[0].SerialNumber");
    }
}