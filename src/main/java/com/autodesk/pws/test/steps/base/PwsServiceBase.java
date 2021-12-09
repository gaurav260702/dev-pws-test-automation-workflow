package com.autodesk.pws.test.steps.base;

import java.io.IOException;

import com.autodesk.pws.test.processor.*;
import com.autodesk.pws.test.steps.authentication.*;

import io.restassured.path.json.JsonPath;
import okhttp3.Response;

public class PwsServiceBase extends RestActionBase
{
    public boolean EnableRetryOnNullResponse = true;
    public int MaximumNullRetryCountBeforeError = 10;
    public int MillisecondsBetweenNullResponseRetry = 1000;
	//  Call the method that does the meat of the work...
    public Response ActionResult = null;
	
    @Override
    public void preparation()
    {
    	rootPreparation();
    }

    public void rootPreparation()
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
		//String baseFileData = DataPool.get("rawBaseFile").toString();
		//DataPool.loadJsonDataAsDataPoolData(baseFileData);
		this.BaseUrl = DataPool.get("baseUrl").toString();
	}

	public void setResourcePath(String defaultPath)
	{
		setResourcePath(defaultPath, false);
	}
	
    public void setResourcePath(String defaultPath, boolean includeDetokenization)
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
		
		if(includeDetokenization)
		{
			ResourcePath = DataPool.detokenizeDataPoolValues(ResourcePath);
			ResourcePath = DynamicData.detokenizeRuntimeValues(ResourcePath);
			ResourcePath = DynamicData.simpleScriptEval(ResourcePath);
		}
    }
	
	public void setTargetUrl()
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
	
	public void refreshOauthToken()
	{
		log("Refreshing OAuth Token...");
		
		GetOAuthCredentials newOAuth = new GetOAuthCredentials();
		
		newOAuth.DataPool = this.DataPool;
		
		newOAuth.preparation();
		newOAuth.action();
		newOAuth.validation();
		newOAuth.cleanup();
	}
	
	@Override
    public void action()
    {
		int retryCount = 0;
		
		boolean keepTrying = true;
		boolean retryExceeded = false;
		
		//  We're going to keep retrying the
		//  call until this flag is set to false...
		while(keepTrying)
		{
			//  Grab the info...
			ActionResult = getInfo();
			
			//  If actionResult contains something, 
			//  then set the flag to break the loop...
			if(ActionResult != null)
			{
				keepTrying = false;
			}
			
			//  If the Retry flag is set to TRUE and keepTrying is set to TRUE...
			if(EnableRetryOnNullResponse && keepTrying == true)
			{
				//  Initiate a retry...
				retryCount+=1;
				this.log("Received (" + retryCount + ") of (" + this.MaximumNullRetryCountBeforeError + ") 'null' values for actionResponse.  Retry in '" + this.MillisecondsBetweenNullResponseRetry + "' milliseconds...");
				this.sleep(this.MillisecondsBetweenNullResponseRetry);
			}
			else
			{
				//  Otherwise, this is just a single-shot attempt and we
				//  need to exit the loop and deal with the consequences...
				keepTrying = false;
			}
			
			//  If the retryCount exceeds the Maximum, set the retryExceeded
			//  flag to true and we'll deal with it down below...
			if(EnableRetryOnNullResponse && retryCount >= this.MaximumNullRetryCountBeforeError)
			{
				retryExceeded = true;
			}
		}	
		
		//  Grab the body of the response (if any)...
		String rawJson = "";
		
		try 
		{
			//  If there was no retry exception, try and grab
			//  the JSON body and get ready to return it...
			if(!retryExceeded)
			{
				rawJson = ActionResult.body().string();
			}
			else
			{
				//  Otherwise, throw an exception with a custom message...
				Exception retryExceededError = new Exception("Exceeded maximum retry count of (" + this.MaximumNullRetryCountBeforeError + ")!");
				throw retryExceededError;
			}
		} 
		catch (Exception e) 
		{
			//  GET READY FOR THE BIG-BAD BEAUTIFUL BARF-O-RAMA, KIDDIES!
			logErr(e, this.ClassName, "action");
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
        
		//  Do SimpleScript resolution...
		JsonRequestBody = DynamicData.simpleScriptEval(JsonRequestBody);	
		
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
		addResponseToValidationChain();
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
	public boolean setExecutionAbortFlagOnError() 
	{
		JsonPath pathFinder = JsonPath.from(JsonResponseBody);
		
		if(pathFinder.get("status").toString().toLowerCase().matches("error"))
		{
			ExceptionAbortStatus = true;
			ExceptionMessage = ClassName + ": " + this.LineMark + pathFinder.prettify();
		}
		
		return ExceptionAbortStatus;
	}
}
