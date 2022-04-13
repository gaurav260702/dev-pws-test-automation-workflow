package com.autodesk.pws.test.steps.authentication;

import okhttp3.Response;


public class GetOpptyOAuthCredentials extends GetOAuthCredentials 
{
	@Override
	public void preparation()
	{
		this.initVariables();
	}

	private void initVariables()
	{
		this.ClassName = this.getClass().getSimpleName();
		pullDataPoolVariables();
	}

	private void pullDataPoolVariables()
	{
		clientId = DataPool.get("opptyClientId").toString();
		clientSecret = DataPool.get("opptyClientSecret").toString();
		BaseUrl = DataPool.get("opptyOAuthBaseUrl").toString();  //  https://api-sfdc-stg-autodesk.auth.us-east-1.amazoncognito.com
	}

	public Response getInfo()
	{
		//  Get the appropriate headers for a token request...
		this.RequestHeaders = generateAccessTokenHeaders();

		//  Ready a reponse container...
		Response oAuthResponse = null;

		try
		{
			log("creating request: ");
			//  Make the call to the oAuth service...
			oAuthResponse = getRestResponse("POST", BaseUrl + "/oauth2/token", "grant_type=client_credentials", "application/x-www-form-urlencoded");
		}
		catch (Exception e)
		{
			//  Uh-oh.  Now what happened?
			logErr(e, this.ClassName, "getInfo");
		}

		log("oAuth Response: " + oAuthResponse);

		return oAuthResponse;
  }
}
