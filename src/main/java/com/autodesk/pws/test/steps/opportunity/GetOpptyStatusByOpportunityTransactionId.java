package com.autodesk.pws.test.steps.opportunity;

import okhttp3.*;
import com.autodesk.pws.test.steps.base.*;
import io.restassured.path.json.JsonPath;

public class GetOpptyStatusByOpportunityTransactionId extends PwsServiceBase
{
	public int MillisecondsBetweenGetOpptyRetries = 10000;
	public int MaxGetOpptyRetries = 10;
	
	private String finalStatus = "";
	
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
		BaseUrl =  DataPool.get("opptyServiceBaseUrl").toString();
		setResourcePath();
	}

	private void setResourcePath()
	{
		String targetPath = "/opportunity-service/v1/opportunity/create-opportunity-by-agreement-number/status/$OPPORTUNITY_TRANSACTION_ID$";
		super.setResourcePath(targetPath, true);
	}

	@Override	
	public void action()
	{
		//  Call the method that does the meat of the work...
		//  We're going to be looping in this version of it
		//  and all the parsing and extraction will be handled
		//  by the loop itself, so we only need to call the
		//  method and not worry about any response...
		getInfo();
	}

	public Response getInfo()
	{
		//  Get the appropriate headers for a token request...
		this.RequestHeaders = generateAccessTokenHeadersWithCurrentToken();
		addHeaderFromDataPool("x-api-key", "opptyXApiKey");

		Response response = null;

		boolean keepTrying = true;
		
		String targetUrl = BaseUrl + this.ResourcePath;
		JsonPath pathFinder = null;
		String status =  "";
		int retryCount = 0;
		
		try
		{	
			while(keepTrying)
			{
				response = getRestResponse("GET", targetUrl);
				
				JsonResponseBody = response.body().string();

				pathFinder = JsonPath.from(JsonResponseBody);

				//  [{"errorCode": "SYSTEM_ERROR", "message": "There was system error - Request failed. Response: [{'message': 'ConcurrentRequests (Concurrent API Requests) Limit exceeded.', 'errorCode': 'REQUEST_LIMIT_EXCEEDED'}]"}]
				if(JsonResponseBody.contains("errorCode"))
				{
					status = pathFinder.get("errorCode") + " -- " + pathFinder.get("message");
				}
				else
				{
					status = pathFinder.get("status");
				}
		
				log("GetOppty status -- " + status);
				
				switch(status)
				{
					case "Success":						
					case "Failed":
						keepTrying = false;
						break;
						
					default:
						keepTrying = true;
						retryCount+=1;
						break;
				}
			
				
				if(retryCount >= this.MaxGetOpptyRetries)
				{
					status = "Timeout";
					keepTrying = false;
				}
				
				if(keepTrying) 
				{
					this.sleep(MillisecondsBetweenGetOpptyRetries);
				}
			}
			
			finalStatus = status;
		}
		catch (Exception e)
		{
			logErr(e, this.ClassName, "getInfo");
			this.ExceptionAbortStatus = true;
			this.ExceptionMessage = this.ClassName + ".getInfo() -- " + e.getMessage();
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
	
		String finalMessage = "";
		
		switch(finalStatus)
		{
			case "Success":		
				//finalMessage = pathFinder.get("opportunities"); 		
				break;
				
			case "Failed":
				finalMessage = pathFinder.get("errors"); 
				this.ExceptionAbortStatus = true;
				this.ExceptionMessage = this.ClassName + ".getInfo() -- " + finalMessage;
				break;
				
			case "Timeout":
				finalMessage = "Timeout after (" + this.MaxGetOpptyRetries + ") GetOpptyStatus retries!";
				break;
		}
		
		//  Extact relevant data from JSON response body...
		extractDataFromJsonAndAddToDataPool("$OPPORTUNITY_SERVICE_IDS$", "opportunities", pathFinder); 
		
		//  Properly format the opportunity (assuming only a single oppty here)
		//  and add it to the DataPool...
		String opptyId = pathFinder.getString("opportunities");
		opptyId = opptyId.substring(opptyId.indexOf("[") + 1, opptyId.indexOf("]"));
		DataPool.add("$OPPORTUNITY_NUMBER$", opptyId);
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
