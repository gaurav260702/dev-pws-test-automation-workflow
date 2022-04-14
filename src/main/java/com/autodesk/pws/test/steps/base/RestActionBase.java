package com.autodesk.pws.test.steps.base;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.autodesk.pws.test.processor.DynamicData;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.restassured.path.json.JsonPath;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RestActionBase extends StepBase {
	public String BaseUrl;
	public String TargetUrl;
	public String ResourcePath;
	public String JsonRequestBody = "";
	public String JsonResponseBody = "";
	public String ServiceVerb = "GET";

	protected String clientId;
	protected String clientSecret;
	protected String callBackUrl;

	public HashMap<String, String> RequestHeaders = new HashMap<String, String>();

	private HashMap<String, String> attachedRequestHeaders = new HashMap<String, String>();

	public void initBaseVariables() {
		clientId = DataPool.getDetokenized("clientId").toString();
		clientSecret = DataPool.getDetokenized("clientSecret").toString();
		callBackUrl = DataPool.getDetokenized("callBackUrl").toString();
		BaseUrl = DataPool.getDetokenized("oAuthBaseUrl").toString();
	}

	public void addHeaderFromDataPool(String headerAndDataPoolLabel) {
		// Check to make sure the
		if (!RequestHeaders.containsKey(headerAndDataPoolLabel)) {
			RequestHeaders.put(headerAndDataPoolLabel, DataPool.getDetokenized(headerAndDataPoolLabel).toString());
		}
	}

	public void addHeaderFromDataPool(String headerLabel, String DataPoolLabel) {
		RequestHeaders.put(headerLabel, DataPool.getDetokenized(DataPoolLabel).toString());
	}

	public void addValidationChainLink(String validationLabel, Object dataToValidate) {
		if (!BypassValidationChainLogging) {
			log("Adding '" + validationLabel + "' to validation chain...");
			DataPool.addToValidationChain(validationLabel, dataToValidate);
		}
	}

	public void extractIntoDataPool(String targetDataLabel, Object targetDataValue) {
		String targetAsString = targetDataValue.toString();
		String displayValue = targetAsString.substring(0, Math.min(targetAsString.length(), 50));
		log("Extracting '" + targetDataLabel + "' with value of '" + displayValue + "' ...");
		DataPool.add(targetDataLabel, targetDataValue);
	}

	public void extractDataFromJsonIntoDataPool(String rawJson, String... pathsAndLabels) {
		JsonPath jsonPathObj = JsonPath.from(rawJson);

		for (int i = 0; i < pathsAndLabels.length; i++) {
			String[] pathAndLabel = pathsAndLabels[i].split(":");

			String val = jsonPathObj.getString(pathAndLabel[0]).toString();

			DataPool.add(pathAndLabel[1], val);
		}
	}

	public Response getRestResponse(String restMethod, String restResourcePath) throws IOException {
		return getRestResponse(restMethod, restResourcePath, "", "");
	}

	public Response getRestResponse(String restMethod, String restResourcePath, String jsonPayload) throws IOException {
		return getRestResponse(restMethod, restResourcePath, jsonPayload, "application/json");
	}

	public Response getRestResponse(String restMethod, String restResourcePath, String payload,
			String mediaTypeOverride) throws IOException {
		//////////////////////////////////////////////////
		/// **********************************************
		/// THIS CODE NEEDS TO BE CLEANED UP!
		/// It's functional for now, but there's
		/// some serious potential for things to
		/// get overly complicated fast. I think
		/// this could all be simplified to handle
		/// the various media types in few lines
		/// of code, and with less if/then branching...
		/// **********************************************
		//////////////////////////////////////////////////

		// Ensure the resource path is fully detokenized...
		restResourcePath = DataPool.detokenizeDataPoolValues(restResourcePath);

		// Build the first portions of the REST request...
		Builder requestBuilder = new Request.Builder().url(restResourcePath);

		// Set the default mediaTypeValue...
		String jsonDefaultMediaType = "application/json";
		String mediaTypeValue = jsonDefaultMediaType;

		// Check for a mediaType override...
		if (mediaTypeOverride.length() > 0) {
			mediaTypeValue = mediaTypeOverride;
		}

		// Prepare a mediaType container in case it's needed...
		MediaType mediaType = null;

		// Prepare a body container in case it's needed...
		RequestBody body = null;

		log("Target URL: " + restResourcePath);

		if (restMethod.toUpperCase() == "POST") {
			mediaType = MediaType.parse(mediaTypeValue);
		}

		if (mediaType == null) {
			requestBuilder.method(restMethod, null);
		} else {
			// If a JSON payload is included,
			// append it to the Request Builder...
			if (mediaTypeValue == jsonDefaultMediaType) {
				if (payload != "{}") {
					// Nasty bit of hackery to ensure that the "quanity" value is set to
					// an integer instead of a float. There's an issue with this when the
					// file is loaded from disk and fiddled about with by the Jackson
					// JSON library...
					payload = hack_CleanQuantityFloatType(payload);

					// Ensure that the payload is completely detokenized
					// and all SimpleScript evals have been executed...
					payload = DynamicData.detokenizeRuntimeValues(payload);
					payload = DataPool.detokenizeDataPoolValues(payload);
					payload = DynamicData.simpleScriptEval(payload);

					// All this floofery is so we can convert the raw JSON payload into a
					// single line version so it's easier to read in the log, but still
					// useful if we need to pop it into PostMan or something...
					ObjectMapper objectMapper = new ObjectMapper();
					JsonNode jsonNode = objectMapper.readValue(payload, JsonNode.class);

					log("Payload: " + jsonNode.toString());
				}

				body = RequestBody.create(mediaType, payload);
				requestBuilder.method(restMethod, body);
			} else {
				body = RequestBody.create(mediaType, payload);
				requestBuilder.method(restMethod, body);
				requestBuilder.addHeader("Content-Type", mediaTypeValue);
			}
		}

		// Add in any required customer headers...
		for (String key : RequestHeaders.keySet()) {
			String headerVal = RequestHeaders.get(key);
			// log(key + ": " + headerVal);
			requestBuilder.addHeader(key, headerVal);
		}

		// Build the final Request object...
		Request request = requestBuilder.build();

		Headers headers = request.headers();

		// Log the headers for debugging purposes...
		log("-- REQUEST HEADERS --");
		for (int i = 0, count = headers.size(); i < count; i++) {
			log(headers.name(i) + " : " + headers.value(i), 4);
		}

		// Ready the REST client...
		OkHttpClient client = new OkHttpClient().newBuilder().build();

		// Declare the response container...
		Response response = null;

		// Call the REST service...
		response = client.newCall(request).execute();

		this.log("Service response: " + response.code() + " -- " + response.message());

		// Hand back to the caller whatever we received from the REST service...
		return response;
	}

	public void addResponseToValidationChain() {
		// Stick that response body in the ValidationChain,
		// but let's go ahead and make it puuuurrrdy first.
		//
		// Also, we're doing this here on the off chance that
		// the class is part of a "WaitFor*Change" style loop.
		//
		// If we were to include it as part of the "action()"
		// method, it would be called 'n' number of times,
		// which is of course a bit excessive...
		JsonPath jsonPath = JsonPath.from(JsonResponseBody);
		String prettyJson = jsonPath.prettify();
		addValidationChainLink(ClassName, prettyJson);
	}

	private String hack_CleanQuantityFloatType(String rawJson) {
		JsonPath jsonPath = JsonPath.from(rawJson);

		String prettyJson = jsonPath.prettify();

		String lines[] = prettyJson.split(this.LineMark);

		for (String line : lines) {
			String tmp = line.trim();

			if (tmp.toLowerCase().startsWith("\"quantity\"")) {
				String keyVal[] = tmp.split(":");
				String val = keyVal[1].replace(',', ' ').trim();
				float f = Float.parseFloat(val);
				Integer i = Math.round(f);
				String newQuantity = "\"quantity\": " + i.toString() + ",";
				prettyJson = prettyJson.replaceFirst("(.*)" + tmp + "(.*)", newQuantity);
			}
		}

		jsonPath = JsonPath.from(prettyJson);
		prettyJson = jsonPath.prettify();

		return prettyJson;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> mergeJsonFromStrings(String rawJsonOriginal, String rawJsonOverride) {
		// https://stackoverflow.com/questions/35784713/merge-json-objects-with-java
		Gson gson = new Gson();
		// read both jsons
		Map<String, Object> json1 = gson.fromJson(rawJsonOriginal, Map.class);
		Map<String, Object> json2 = gson.fromJson(rawJsonOverride, Map.class);
		// create combined json with contents of first json
		Map<String, Object> combined = new HashMap<>(json1);
		// Add the contents of first json. It will overwrite the values if keys are
		// same. e.g. "foo" of json2 will take precedence if both json1 and json2 have
		// "foo"
		combined.putAll(json2);

		return combined;
	}

	public HashMap<String, Object> jsonStringToHashMap(String json) {
		// https://stackoverflow.com/questions/37019059/remove-null-values-from-json-using-jackson
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);

		try {
			json = mapper.writeValueAsString(json);
		} catch (JsonProcessingException e) {
			logErr(e, this.ClassName, "removeAllNullValuesFromJson");
		}

		// https://stackoverflow.com/questions/2525042/how-to-convert-a-json-string-to-a-mapstring-string-with-jackson-json
		TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
		};

		HashMap<String, Object> jsonMap = new HashMap<String, Object>();

		try {
			jsonMap = mapper.readValue(json, typeRef);
		} catch (JsonMappingException e) {
			logErr(e, this.ClassName, "removeAllNullValuesFromJson");
		} catch (JsonProcessingException e) {
			logErr(e, this.ClassName, "removeAllNullValuesFromJson");
		}

		return jsonMap;
	}

	public void attachHeaderFromDataPool(String headerLabel, String dataPoolLabel) {
		attachedRequestHeaders.put(headerLabel, dataPoolLabel);
	}

	public void attachHeaderFromDataPool(String headerLabel) {
		attachedRequestHeaders.put(headerLabel, headerLabel);
	}

	public HashMap<String, Object> removeAllNullValuesFromJson(Map<String, Object> orderInfo)// throws
																								// JsonProcessingException
	{
		// https://stackoverflow.com/questions/37019059/remove-null-values-from-json-using-jackson
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);

		String json = "{}";

		try {
			json = mapper.writeValueAsString(orderInfo);
		} catch (JsonProcessingException e) {
			logErr(e, this.ClassName, "removeAllNullValuesFromJson");
		}

		// https://stackoverflow.com/questions/2525042/how-to-convert-a-json-string-to-a-mapstring-string-with-jackson-json
		TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
		};

		HashMap<String, Object> jsonMap = new HashMap<String, Object>();

		try {
			jsonMap = mapper.readValue(json, typeRef);
		} catch (JsonMappingException e) {
			logErr(e, this.ClassName, "removeAllNullValuesFromJson");
		} catch (JsonProcessingException e) {
			logErr(e, this.ClassName, "removeAllNullValuesFromJson");
		}

		return jsonMap;
	}

	public void generateTokenHeaders() {
		HashMap<String, String> authHeaders = this.generateAccessTokenHeadersWithCurrentToken();

		RequestHeaders.putAll(authHeaders);
	}

	public HashMap<String, String> generateAccessTokenHeadersWithCurrentToken() {
		HashMap<String, String> headers = new HashMap<String, String>();

		String accessToken = DataPool.get("access_token").toString();
		String timeStamp = getTimeStamp();
		String signature = getSignature(timeStamp, accessToken);

		headers.put("Authorization", "Bearer " + accessToken);
		headers.put("timestamp", timeStamp);
		headers.put("signature", signature);

		return headers;
	}

	public HashMap<String, String> generateAccessTokenHeaders() {
		String timeStamp = getTimeStamp();
		String signature = getSignature(timeStamp);
		String baseAuth = getBaseAuth();

		HashMap<String, String> headers = new HashMap<String, String>();

		headers.put("Authorization", "Basic " + baseAuth);
		headers.put("timestamp", timeStamp);
		headers.put("signature", signature);

		return headers;
	}

	public String getTimeStamp() {
		// return EPOCH Timestamp
		return (Long.toString(new Date().getTime() / 1000));
	}

	public String getSignature(String timeStamp) {
		return getSignature(timeStamp, "");
	}

	public String getSignature(String timeStamp, String token) {
		String mixcode = "";

		if (token.length() == 0) {
			mixcode = callBackUrl + clientId + timeStamp;
		} else {
			mixcode = callBackUrl + token + timeStamp;
		}

		return getSha256Hash(mixcode);
	}

	public String getSha256Hash(String mixcode) {
		String hashedStr = "";

		try {
			Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
			SecretKeySpec secret_key = new SecretKeySpec(clientSecret.getBytes(), "HmacSHA256");
			sha256_HMAC.init(secret_key);
			hashedStr = org.apache.commons.codec.binary.Base64
					.encodeBase64String(sha256_HMAC.doFinal(mixcode.getBytes()));
		} catch (Exception e) {
			logger.error("Error in " + this.ClassName + ".getSha256Hash():", e);
		}

		return hashedStr;
	}

	public String getBaseAuth() {
		String str = clientId + ":" + clientSecret;
		byte[] bytesEncoded = org.apache.commons.codec.binary.Base64.encodeBase64(str.getBytes());
		return new String(bytesEncoded);
	}

	/*
	 * public String set_session_id() { // Generate timestamp header var timestamp =
	 * getTimeStamp(); // Math.floor(Date.now() / 1000);
	 * 
	 * // Generate Authorization header var cred_str = clientId + ":" +
	 * clientSecret; var base64_header =
	 * btoa(unescape(encodeURIComponent(cred_str)));
	 * 
	 * // Generate signature header var base_str = callBackUrl + clientId +
	 * timestamp; var hmacsha256 = getSha256Hash(base_str); //
	 * CryptoJS.HmacSHA256(base_str, client_secret); var signature =
	 * org.apache.commons.codec.binary.Base64. //
	 * CryptoJS.enc.Base64.stringify(hmacsha256);
	 * 
	 * // postman.setEnvironmentVariable("oauth_authorization", base64_header); //
	 * postman.setEnvironmentVariable("oauth_timestamp", timestamp); //
	 * postman.setEnvironmentVariable("oauth_signature", signature);
	 * 
	 * pm.sendRequest({ url:
	 * `https://${hostname}/v2/oauth/generateaccesstoken?grant_type=
	 * client_credentials`, method: 'POST', header: { "Authorization": "Basic " +
	 * base64_header, "signature": signature, "timestamp": timestamp.toString() } },
	 * function(err, res){ if(res.json().access_token){
	 * postman.setEnvironmentVariable("session_id", res.json().access_token);
	 * set_request_headers(); } });
	 * 
	 * //}
	 */
	
	public void generateAttachedRequestHeaders() {
		attachedRequestHeaders.forEach((headerLabel, dataPoolLabel) -> {
			addHeaderFromDataPool(headerLabel, dataPoolLabel);
		});
	}
}
