package com.autodesk.pws.test.steps.rule;

import java.io.IOException;
import okhttp3.Response;

import java.util.HashMap;

import com.autodesk.pws.test.steps.base.*;

public class ExecuteSireRule extends RestActionBase
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
    initBaseVariables();
    this.ClassName = this.getClass().getSimpleName();
    pullDataPoolVariables();
  }

  private void pullDataPoolVariables()
  {
	  this.sireRule = DataPool.get("sireRule").toString();
    //
    // TODO -- CREATE A SECONDARY DATA FILE METHOD TO KEEP OVERRIDE 
    // FILES DIFFERENT FROM ADDITIONAL PAYLOAD FILES
    //
    this.requestPayload = DataPool.get("rawOverrideFile").toString();

	  BaseUrl =  DataPool.get("sireBaseUrl").toString();
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
    this.RequestHeaders = generateAccessTokenHeadersWithCurrentToken();
    addHeaderFromDataPool("x-api-key", "sireXApiKey");

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
}
