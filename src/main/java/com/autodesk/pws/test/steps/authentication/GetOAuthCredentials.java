package com.autodesk.pws.test.steps.authentication;

import java.io.IOException;
import okhttp3.Response;

import com.autodesk.pws.test.steps.base.*;

public class GetOAuthCredentials extends RestActionBase
{
    @Override
    public void preparation()
    {
    	initVariables();
    }

    private void initVariables()
    {
    	this.ClassName = this.getClass().getSimpleName();
    	pullDataPoolVariables();
	}

	private void pullDataPoolVariables()
    {
		initBaseVariables();
	}

    @Override
    public void action()
    {
		//  Call the method that does the meat of the work...
		Response actionResult = getInfo();

		//  Grab the body of the response (if any)...
		String rawJson = "";

		try
		{
			//  Can somebody tell me why the ******** this has to be in a try-catch?!?!
			rawJson = actionResult.body().string();
		}
		catch (IOException e)
		{
			this.logErr(e, this.ClassName, "action");
		}

		//  Stick that response body in the ValidationChain...
		this.addValidationChainLink(this.ClassName, rawJson);

		//  Here we would extract any data that needs
		//  to be promoted in the DataPool...
		extractDataFromJsonIntoDataPool(rawJson, "access_token:access_token");
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
    		//  Make the call to the oAuth service...
			oAuthResponse = getRestResponse("POST", BaseUrl + "/v2/oauth/generateaccesstoken?grant_type=client_credentials", "{}");
    	}
    	catch (IOException e)
    	{
    		//  Uh-oh.  Now what happened?
    		logErr(e, this.ClassName, "getInfo");
		}

    	return oAuthResponse;
    }
}
