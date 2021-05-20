package com.autodesk.pws.test.steps.base;

import java.io.IOException;

import com.autodesk.pws.test.processor.*;

import io.restassured.path.json.JsonPath;
import okhttp3.Response;

public class PwsServiceBase extends RestActionBase
{
    public String TargetUrl;
    public String ResourcePath;
    public String JsonResponseBody;
    private String ServiceVerb = "GET";
    private String JsonRequestBody = "";
    
    @Override
    public void preparation()
    {
    	//  Do stuff that the Action depends on to execute...
    	initVariables();
    	prepareRequestHeaders();
    }

    private void prepareRequestHeaders()
    {
    	//  Add in the headers required for this request type...
    	//  TODO: Determine method to determine if calling EXTERNAL
    	//  web service and therefore not needed...
    	addHeaderFromDataPool("x-api-key");
	}

	private void initVariables()
    {
		this.initBaseVariables();
		pullDataPoolVariables();
    	setTargetUrl();
	}

	private void pullDataPoolVariables()
    {
    	//  Set variables that are extracted
		//  from the DataPool Here...
		String baseFileData = DataPool.get("rawBaseFile").toString();
		DataPool.loadJsonDataAsDataPoolData(baseFileData);
		this.BaseUrl = DataPool.get("baseUrl").toString();
	}

    public void setResourcePath(String defaultPath)
    {
		//  Setting up a special case here for modified GetInvoiceDetails paths.
		//  This allows negative testing (dropping "invoice_number" or "customer_number")
		//  or modifying the ResourcePath to allow for "sales_order_number"...
		if(DataPool.containsKey(ClassName + ".ResourcePath"))
		{
			ResourcePath = DataPool.get(ClassName + ".ResourcePath").toString();
		}
		else
		{
			ResourcePath = defaultPath;
		}
    }
	
	private void setTargetUrl()
    {
		//  Set the resourceURL for the REST service...
		// https://invoice.ddwsint.autodesk.com
        String targetUrl = this.BaseUrl + ResourcePath;

        //  Detokenize any necessary runtime values...
        targetUrl = DynamicData.detokenizeRuntimeValuesAndCustomDictionary(targetUrl, this.DataPool);

        //  Set the Class.TargetUrl to the now detokenized value...
        TargetUrl = targetUrl;
	}

	public void setServiceVerb(String serviceVerb)
	{
		ServiceVerb = serviceVerb;
	}
	
	public void setAsPostService()
	{
		setServiceVerb("POST");
	}
	
	public void setAsGetService()
	{
		setServiceVerb("GET");
	}
	
	public void setJsonRequestBody(String jsonRequestBody)
	{
		JsonRequestBody =  DynamicData.detokenizeRuntimeValuesAndCustomDictionary(jsonRequestBody, DataPool);
	}
	
	public void loadJsonRequestBody(String jsonRequestBodyFilePath)
	{
		String rawJsonRequest = DynamicData.loadJsonFile(jsonRequestBodyFilePath, true);
		
		setJsonRequestBody(rawJsonRequest);	
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
			logErr(e, this.ClassName, "action");
			//  TODO: Copy this pattern into similar occasions...
			throw new RuntimeException(e);
		}

		//  Make the json response body available for data extraction...
		this.JsonResponseBody = rawJson;
    }


    public Response getInfo()
    {
    	//  Prep a response container...
        Response retVal = null;
        
        generateAndAppendCurrentTokenHeaders();
        
        addCsnHeader();
        
        //  Try and get the request...
		try
		{
			retVal = getRestResponse(ServiceVerb, TargetUrl, JsonRequestBody);
		}
		//  And vomit if it doesn't work...
		catch (IOException e)
		{
			logErr(e, ClassName, "getInfo");
		}

        return retVal;
    }

    @Override
    public void validation()
    {
		//  Stick that response body in the ValidationChain,
		//  but let's go ahead and make it puuuurrrdy first.
    	//
    	//  Also, we're doing this here on the off chance that
    	//  the class is part of a "WaitFor*Change" style loop.
    	//
    	//  If we were to include it as part of the "action()"
    	//  method, it would be called 'n' number of times, 
    	//  which is of course a bit excessive...
		JsonPath jsonPath = JsonPath.from(JsonResponseBody);
		String prettyJson = jsonPath.prettify();
		addValidationChainLink(ClassName, prettyJson);
    }
    
    public void extractDataFromJsonAndAddToDataPool(String dataPoolLabel, String jsonPath)
    {
		JsonPath pathFinder = JsonPath.from(JsonResponseBody);
    	super.extractDataFromJsonAndAddToDataPool(dataPoolLabel, jsonPath, pathFinder);
    }
    
    //  The following method allows the WPE to detect any JSON formatted REST response
    //  that contains an "error" node as a child of the "status" node.
    //  If one is found, it then converts the JSON to a prettified string string and
    //  shoves it into a container and sets an abort status flag, which is checked by
    //  the WPE between each sub-step and acted upon if it is set to 'true'...
	public void setExecutionAbortFlagOnError() 
	{
		JsonPath pathFinder = JsonPath.from(JsonResponseBody);
		
		if(pathFinder.get("status").toString().toLowerCase().matches("error"))
		{
			ExceptionAbortStatus = true;
			ExceptionMessage = ClassName + ": " + this.LineMark + pathFinder.prettify();
		}
	}
}
