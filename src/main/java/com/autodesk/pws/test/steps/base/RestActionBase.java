package com.autodesk.pws.test.steps.base;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.restassured.path.json.JsonPath;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RestActionBase extends StepBase
{
  // Note: Regarding Instance fields always keep it private, if a class extends and if they are
  // required in other class(s) keep them as protected
  private String clientId;
  private String clientSecret;
  private String callBackUrl;
  protected String baseUrl;

    public HashMap<String, String> requestHeaders = new HashMap<String, String>();

    public void initBaseVariables()
    {
		clientId = dataPool.get("clientId").toString();
		clientSecret = dataPool.get("clientSecret").toString();
		callBackUrl = dataPool.get("callBackUrl").toString();
		baseUrl =  dataPool.get("oAuthBaseUrl").toString();
    }

    public void addCsnHeader()
    {
    	if(dataPool.containsKey("$CSN_HEADER$"))
    	{
    		addHeaderFromDataPool("CSN", "$CSN_HEADER$");
    	}
    	else
    	{
    		addHeaderFromDataPool("CSN", "$CUSTOMER_NUMBER$");
    	}
    }

    public void addHeaderFromDataPool(String headerAndDataPoolLabel)
    {
    	requestHeaders.put(headerAndDataPoolLabel, dataPool.get(headerAndDataPoolLabel).toString());
    }

    public void addHeaderFromDataPool(String headerLabel, String dataPoolLabel)
    {
    	requestHeaders.put(headerLabel, dataPool.get(dataPoolLabel).toString());
    }

    public void addValidationChainLink(String validationLabel, Object dataToValidate)
    {
        if (!bypassValidationChainLogging)
        {
            log("       Adding '" + validationLabel + "' to validation chain...");
            dataPool.addToValidationChain(validationLabel, dataToValidate);
        }
    }

    public void extractIntoDataPool(String targetDataLabel, Object targetDataValue)
    {
    	String targetAsString = targetDataValue.toString();
        String displayValue = targetAsString.substring(0, Math.min(targetAsString.length(), 50));
        log("Extracting '" + targetDataLabel + "' with value of '" + displayValue + "' ...");
        dataPool.add(targetDataLabel, targetDataValue);
    }

	public void extractDataFromJsonIntoDataPool(String rawJson, String... pathsAndLabels)
    {
		JsonPath jsonPathObj = JsonPath.from(rawJson);

		for(int i=0; i < pathsAndLabels.length; i++)
		{
			String[] pathAndLabel = pathsAndLabels[i].split(":");

			String val = jsonPathObj.getString(pathAndLabel[0]).toString();

			dataPool.add(pathAndLabel[1], val);
		}
    }

	public Response getRestResponse(String restMethod, String restResourcePath) throws IOException
	{
		return getRestResponse(restMethod, restResourcePath, "");
	}

	public Response getRestResponse(String restMethod, String restResourcePath, String jsonPayload) throws IOException
	{
		//  Build the first portions of the REST request...
		Builder requestBuilder = new Request.Builder().url(restResourcePath);

		logger.info("       Target URL: " + restResourcePath);

		//  If a JSON payload is included,
		//  append it to the Request Builder...
		if(jsonPayload != "")
		{
		    MediaType mediaType = MediaType.parse("application/json");
		    RequestBody body = RequestBody.create(mediaType, jsonPayload);
			requestBuilder.method(restMethod, body);
			logger.info("     PayloadL: " + jsonPayload);
		}
		else
		{
			requestBuilder.method(restMethod, null);;
		}

		//  Add in any required customer headers...
		for (String key : requestHeaders.keySet())
        {
			String headerVal = requestHeaders.get(key);
			log(key + ": " + headerVal);
        	requestBuilder.addHeader(key, headerVal);
        }

        //  Build the final Request object...
		Request request = requestBuilder.build();

		//  Ready the REST client...
		OkHttpClient client = new OkHttpClient().newBuilder().build();

		//  Declare the response container...
		Response response = null;

		//  Call the REST service...
		response = client.newCall(request).execute();

		//  Hand back to the caller whatever we received from the REST service...
		return response;
	}

    @SuppressWarnings("unchecked")
	public Map<String, Object> mergeJsonFromStrings(String rawJsonOriginal, String rawJsonOverride)
    {
    	//  https://stackoverflow.com/questions/35784713/merge-json-objects-with-java
    	Gson gson = new Gson();
    	//read both jsons
    	Map<String, Object> json1 = gson.fromJson(rawJsonOriginal, Map.class);
    	Map<String, Object> json2 = gson.fromJson(rawJsonOverride, Map.class);
    	//create combined json with contents of first json
    	Map<String, Object> combined = new HashMap<>(json1);
    	//Add the contents of first json. It will overwrite the values if keys are
    	//same. e.g. "foo" of json2 will take precedence if both json1 and json2 have "foo"
    	combined.putAll(json2);

    	return combined;
    }

    public HashMap<String,Object> removeAllNullValuesFromJson(Map<String, Object> orderInfo)// throws JsonProcessingException
    {
    	//  https://stackoverflow.com/questions/37019059/remove-null-values-from-json-using-jackson
    	ObjectMapper mapper = new ObjectMapper();
    	mapper.setSerializationInclusion(Include.NON_NULL);

    	String json = "{}";

		try
		{
			json = mapper.writeValueAsString(orderInfo);
		}
		catch (JsonProcessingException e)
		{
          logErr(e, this.className, "removeAllNullValuesFromJson");
		}

		//  https://stackoverflow.com/questions/2525042/how-to-convert-a-json-string-to-a-mapstring-string-with-jackson-json
	    TypeReference<HashMap<String,Object>> typeRef = new TypeReference<HashMap<String,Object>>() {};

	    HashMap<String, Object> jsonMap = new HashMap<String, Object>();

		try
		{
			jsonMap = mapper.readValue(json, typeRef);
		}
		catch (JsonMappingException e)
		{
			logErr(e, this.className, "removeAllNullValuesFromJson");
		}
		catch (JsonProcessingException e)
		{
			logErr(e, this.className, "removeAllNullValuesFromJson");
		}

    	return jsonMap;
    }

    public void generateAndAppendCurrentTokenHeaders()
    {
        HashMap<String, String> authHeaders = this.generateAccessTokenHeadersWithCurrentToken();

        requestHeaders.putAll(authHeaders);
    }

    public HashMap<String, String> generateAccessTokenHeadersWithCurrentToken()
    {
		HashMap<String, String> headers = new HashMap<String, String>();

		String accessToken = dataPool.get("access_token").toString();
		String timeStamp = getTimeStamp();
		String signature = getSignature(timeStamp, accessToken);

		headers.put("Authorization", "Bearer " + accessToken);
		headers.put("timestamp", timeStamp);
		headers.put("signature", signature);

		return headers;
    }

	public HashMap<String, String> generateAccessTokenHeaders()
	{
		String timeStamp = getTimeStamp();
		String signature = getSignature(timeStamp);
		String baseAuth = getBaseAuth();

		HashMap<String, String> headers = new HashMap<String, String>();

		headers.put("Authorization", "Basic " + baseAuth);
		headers.put("timestamp", timeStamp);
		headers.put("signature", signature);

		return headers;
	}

	public String getTimeStamp()
	{
	  // return EPOCH Timestamp
		return (Long.toString(new Date().getTime() / 1000));
	}

	public String getSignature(String timeStamp)
	{
		return getSignature(timeStamp, "");
	}

	public String getSignature(String timeStamp, String token)
	{
		String mixcode = "";

		if(token.length() == 0)
		{
			mixcode = callBackUrl + clientId + timeStamp;
		}
		else
		{
			mixcode = callBackUrl + token + timeStamp;
		}

		return getSha256Hash(mixcode);
	}

	public String getSha256Hash(String mixcode)
	{
		String hashedStr = "";

		try
		{
			Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
			SecretKeySpec secret_key = new SecretKeySpec(clientSecret.getBytes(), "HmacSHA256");
			sha256_HMAC.init(secret_key);
			hashedStr = org.apache.commons.codec.binary.Base64.encodeBase64String(sha256_HMAC.doFinal(mixcode.getBytes()));
		}
		catch (Exception e)
		{
			logger.error("Error in " + this.className + ".getSha256Hash():", e);
		}

		return hashedStr;
	}

	public String getBaseAuth()
	{
		String str = clientId + ":" + clientSecret;
		byte[] bytesEncoded = org.apache.commons.codec.binary.Base64.encodeBase64(str.getBytes());
		return new String(bytesEncoded);
	}
}
