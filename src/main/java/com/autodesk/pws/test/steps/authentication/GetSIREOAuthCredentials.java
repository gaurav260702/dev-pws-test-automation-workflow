package com.autodesk.pws.test.steps.authentication;

import java.util.HashMap;

import java.io.IOException;
import okhttp3.Response;

import com.autodesk.pws.test.steps.base.*;

public class GetSIREOAuthCredentials extends GetOAuthCredentials
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
		accessTokenToExtract = "access_token:sire_access_token";
	}

	private void pullDataPoolVariables()
	{
		clientId = DataPool.get("sireClientId").toString();
		clientSecret = DataPool.get("sireClientSecret").toString();
		callBackUrl = DataPool.get("sireCallBackUrl").toString();
		BaseUrl = DataPool.get("sireOAuthBaseUrl").toString();
	}

	public Response getInfo()
	{
		//  Get the appropriate headers for a token request...
		this.RequestHeaders = generateAccessTokenHeaders();

		//  Ready a reponse container...
		Response oAuthResponse = null;

		try
		{
			log("      creating request: ");
			//  Make the call to the oAuth service...
			oAuthResponse = getRestResponse("POST", BaseUrl + "/oauth2/token", "grant_type=client_credentials", "application/x-www-form-urlencoded");
		}
		catch (IOException e)
		{
			//  Uh-oh.  Now what happened?
			logErr(e, this.ClassName, "getInfo");
		}

		log("      oAuth Response: " + oAuthResponse);

		return oAuthResponse;
  }

	@Override
	public HashMap<String, String> generateAccessTokenHeaders()
	{
		String baseAuth = getBaseAuth();

		HashMap<String, String> headers = new HashMap<String, String>();

		headers.put("Authorization", "Basic " + baseAuth);
		
		return headers;
	}
}
