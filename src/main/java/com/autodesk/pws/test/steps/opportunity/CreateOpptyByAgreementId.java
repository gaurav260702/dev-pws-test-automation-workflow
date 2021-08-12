package com.autodesk.pws.test.steps.opportunity;

import okhttp3.*;
import com.autodesk.pws.test.steps.base.*;
import io.restassured.path.json.JsonPath;

public class CreateOpptyByAgreementId extends PwsServiceBase
{
	@Override
	public void preparation()
  	{
	    initVariables();
  	}
	
	private void initVariables()
	{
		super.preparation();
	    initBaseVariables();
	    this.ClassName = this.getClass().getSimpleName();
	    pullDataPoolVariables();
	    setJsonRequestBody();
	}
	
	private void setJsonRequestBody() 
	{
		//  This reques thas a very simple structure, which (in general) can't be filled out
		//  prior to runtime, so it's easier to attach the JSON request body inline than
		//  to create the structures needed to support reading it from a test data file...
		String requestBody = "{\"agreementNumber\":\"$AGREEMENT_NUMBER$\",\"type\":\"Renewal Opportunity\"}";
		super.setJsonRequestBody(requestBody);
	}

	private void pullDataPoolVariables()
	{
		setResourcePath();
		BaseUrl =  DataPool.get("opptyServiceBaseUrl").toString();
	}

	private void setResourcePath()
	{
		super.setResourcePath("/opportunity-service/v1/opportunity/create-opportunity-by-agreement-number");
	}

	@Override	
	public void action()
	{
		this.log("Forced sleep to ensure SalesForce is synced and ready to accept new opportunity request...");
		sleep(60000);
		
		//  Call the method that does the meat of the work...
		Response actionResult = getInfo();

		try
		{
			JsonResponseBody = actionResult.body().string();
		}
		catch (Exception e)
		{
			this.logErr(e, this.ClassName, "action");
		}
	}

	public Response getInfo()
	{
		//  Get the appropriate headers for a token request...
		this.RequestHeaders = generateAccessTokenHeadersWithCurrentToken();
		addHeaderFromDataPool("x-api-key", "opptyXApiKey");
		
		Response response = null;

		try
		{
			String targetUrl = BaseUrl + this.ResourcePath;
			response = getRestResponse("POST", targetUrl, JsonRequestBody);
		}
		catch (Exception e)
		{
			logErr(e, this.ClassName, "getInfo");
	    }

		return response;
	}
		  
	@Override
	public void validation()
	{    	
		super.validation();
	
		//  Here we would extract any data that needs to be promoted to 
		//  the DataPool and may be needed by other steps later on...
		JsonPath pathFinder = JsonPath.with(JsonResponseBody);
	
		//  Extact data that 	
		extractDataFromJsonAndAddToDataPool("$OPPORTUNITY_TRANSACTION_ID$", "transactionId", pathFinder); 
	}	
}
//	  public static void main(String []args) throws IOException
//	  
//	  {
//	    OkHttpClient client = new OkHttpClient().newBuilder()
//	      .build();
//	    Request request = new Request.Builder()
//	      .url("https://api.sfdc-stg.autodesk.com/opportunity-service/v1/opportunity/create-opportunity-by-agreement-number/status/aKP2C00000005BeWAI")
//	      .method("GET", null)
//	      .addHeader("authorization", "Bearer eyJraWQiOiJlSHlESzRxR3BpcHJrTVBKYVZOMTJtR09heUE5cjExKzlYTDg4NGprOFpZPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiIyODUzcXBybTFkN3F0NW0xajVtZTJzNnFhcSIsInRva2VuX3VzZSI6ImFjY2VzcyIsInNjb3BlIjoiaHR0cHM6XC9cL2FwaS1zZmRjLXN0Zy1hdXRvZGVzay5jb21cL29wcG9ydHVuaXR5LmNyZWF0ZSIsImF1dGhfdGltZSI6MTYyNzU5MTYxMSwiaXNzIjoiaHR0cHM6XC9cL2NvZ25pdG8taWRwLnVzLWVhc3QtMS5hbWF6b25hd3MuY29tXC91cy1lYXN0LTFfSzJidkhodkZkIiwiZXhwIjoxNjI3NTk1MjExLCJpYXQiOjE2Mjc1OTE2MTEsInZlcnNpb24iOjIsImp0aSI6ImQwM2E1NDQ5LWFiZDctNGM2MS1hNzI5LTQwNmZjYTNkN2JjOCIsImNsaWVudF9pZCI6IjI4NTNxcHJtMWQ3cXQ1bTFqNW1lMnM2cWFxIn0.VJthnJiYCcB-mxBjl88C956WD8sCvCRnolXPib6oy-MaUt6K0gXthBRYIo_xp9Ryp3qBbptluAntplVOG3Or7eyfSdBxzFEnUld_g63_LYgutwdanMc8139kjLMfFwWqN28RgwGVPmPpsy5SMpyCRt2WXUJAFW-RWJ-2SSadNgwMefXJPsGJDKuPEYq-wR36i2y82cauwHOPZ5scGQiewQp5Zv0o50FXaFxEZK7hPdC6Joe8h57P7Ykv1Ts2PaOe-_tkbKBmgZT7GWZvz163KbiYBraN1b81ChahHnPP5jJvI7zaEalTlncl2GnaECKe-N135qPNmezeFDIBL2DlsA")
//	      .addHeader("x-api-key", "opgvBuOZYQ1iDMjWdJ9SN6qdVyYq9n6Rs6753SJ6")
//	      .build();
//	    Response response = client.newCall(request).execute();
//	    System.out.println(response.body().string());
//	  }
//}
