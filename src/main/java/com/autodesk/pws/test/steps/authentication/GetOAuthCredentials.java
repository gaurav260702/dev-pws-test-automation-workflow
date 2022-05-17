package com.autodesk.pws.test.steps.authentication;

import com.autodesk.pws.test.steps.base.*;

import okhttp3.Response;


public class GetOAuthCredentials extends RestActionBase
{
	protected String accessTokenToExtract = "access_token:access_token";
    @Override
    public void preparation()
    {
    	initVariables();
    }

    private void initVariables()
    {
    	this.ClassName = this.getClass().getSimpleName();
    	
		clientId = DataPool.getDetokenized("clientId").toString();
		clientSecret = DataPool.getDetokenized("clientSecret").toString();
		callBackUrl = DataPool.getDetokenized("callBackUrl").toString();
		
    	pullDataPoolVariables();
	}

	private void pullDataPoolVariables()
    {
		initBaseVariables();
	}

    @Override
    public void action()
    {
		//  Prep a container for the response body (if any)...
		String rawJson = "";

		try
		{
			//  Call the method that does the meat of the work...
			Response actionResult = getInfo();

			rawJson = actionResult.body().string();
		}
		catch (Exception e)
		{
			this.logErr(e, this.ClassName, "action");
		}

		//  Stick that response body in the ValidationChain...
		this.addValidationChainLink(this.ClassName, rawJson);

		//  Here we would extract any data that needs
		//  to be promoted in the DataPool...
		extractDataFromJsonIntoDataPool(rawJson, accessTokenToExtract);
    }

    public Response getInfo()
    {
    	//  String rawJsonBody = "";

    	//  Get the appropriate headers for a token request...
    	this.RequestHeaders = generateAccessTokenHeaders();

    	//  Ready a reponse container...
    	Response oAuthResponse = null;

    	try
    	{
			log("creating request: ");
    		//  Make the call to the oAuth service...
			oAuthResponse = getRestResponse("POST", BaseUrl + "/v2/oauth/generateaccesstoken?grant_type=client_credentials", "{}");
    	}
    	catch (Exception e)
    	{
    		//  Uh-oh.  Now what happened?
    		logErr(e, this.ClassName, "getInfo");
		}

		log("oAuth Response: "+oAuthResponse);
    	return oAuthResponse;
    }
}
