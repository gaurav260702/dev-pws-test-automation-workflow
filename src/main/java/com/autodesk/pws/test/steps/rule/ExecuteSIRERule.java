package com.autodesk.pws.test.steps.rule;

import java.io.IOException;
import okhttp3.Response;

import java.util.HashMap;

import com.autodesk.pws.test.steps.base.*;

public class ExecuteSIRERule extends RestActionBase
{
  String sireRule;
  String requestPayload;
  
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
	  this.sireRule = DataPool.get("sireRule").toString();
	  this.requestPayload = DataPool.get("rawOverrideFile").toString();
	  BaseUrl =  DataPool.get("SIREbaseUrl").toString();
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
      rawJson = actionResult.body().string();
    }
    catch (IOException e)
    {
      this.logErr(e, this.ClassName, "action");
    }

    //  Stick that response body in the ValidationChain...
    this.addValidationChainLink(this.ClassName, rawJson);
  }

  public Response getInfo()
  {
    //  Get the appropriate headers for a token request...
    this.RequestHeaders = generateAccessTokenHeaders();

    Response executeRuleResponse = null;

    try
    {
      //  Make the call to the SIRE Rule service...
      executeRuleResponse = getRestResponse("POST", BaseUrl + "/sire/v1/execute/" + this.sireRule, this.requestPayload);
    }
    catch (IOException e)
    {
      logErr(e, this.ClassName, "getInfo");
    }

    return executeRuleResponse;
  }

  @Override
  public HashMap<String, String> generateAccessTokenHeaders()
	{
		String accessToken = DataPool.get("sire_access_token").toString();
		String xApiKey = DataPool.get("SIREx-api-key").toString();

		HashMap<String, String> headers = new HashMap<String, String>();

		headers.put("Authorization", "Bearer " + accessToken);
		headers.put("x-api-key", xApiKey);
		
		return headers;
	}
}
