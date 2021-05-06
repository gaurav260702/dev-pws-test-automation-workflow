package com.autodesk.pws.test.steps.base;

import com.autodesk.pws.test.processor.DynamicData;
import com.autodesk.pws.test.steps.base.*;
import java.io.IOException;
import okhttp3.Response;

public class GetServiceBase extends RestActionBase
{
    public String TargetUrl;
    public String ResourcePath;
    public String JsonResponseBody;

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

		//  Stick that response body in the ValidationChain...
		this.addValidationChainLink(this.ClassName, rawJson);

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
			retVal = getRestResponse("GET", TargetUrl);
		}
		//  And vomit if it doesn't work...
		catch (IOException e)
		{
			logErr(e, this.ClassName, "getInfo");
		}

        return retVal;
    }

}
