package com.autodesk.pws.test.engine;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.autodesk.pws.test.processor.DynamicData;
import com.autodesk.pws.test.steps.base.StepBase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.restassured.path.json.JsonPath;
import okhttp3.Response;

public class DataPool extends HashMap<String, Object>
{
	private static final long serialVersionUID = -7447329407872389743L;

    protected final Logger logger = LoggerFactory.getLogger(DataPool.class);

	//  Declare JsonPath container to use during validation..
    private JsonPath jsonPath;
    private StepBase stepLogger;
    private String newLine = System.getProperty("line.separator");

	@SuppressWarnings("unchecked")
	public void addToValidationChain(String validationLabel, Object dataToValidate)
	{
		HashMap<String, Object> validationChain = null;

        if(!containsKey("ValidationChain"))
        {
        	HashMap<String, Object> validationChainDictionary = new HashMap<String, Object>();
            this.add("ValidationChain", validationChainDictionary);
        }

        validationChain = (HashMap<String, Object>) this.get("ValidationChain");

        validationChain.put(validationLabel, dataToValidate);
	}

	@SuppressWarnings("unchecked")
	public Object getValidationChainItem(String validationLabel)
	{
		HashMap<String, Object> validationChain = (HashMap<String, Object>) this.get("ValidationChain");

        return validationChain.get(validationLabel);
	}

	public void loadJsonDataAsDataPoolData(String rawJasonData)
	{
    	//  Grab a dictionary of all the first-level elements...
    	Map<String, Object> keyVals =
    			new Gson().
    				fromJson
    					(
							rawJasonData,
							new TypeToken<HashMap<String, Object>>(){}.getType()
						);

    	//  Loop through all the items in the 'testKicker' object...
    	keyVals.
    		forEach(
    				(keyName, value) ->
    				{
		                //  Add each item to the DataPool as key-val...
		                this.put(keyName, value.toString());
	                }
				   );
	}

    public JsonPath loadJsonFileAsDataPoolData(String jsonFilePath)
    {
    	//  Prep a JsonPath object to hold the kickerFile contents...
		jsonFilePath = DynamicData.convertRelativePathToFullPath(jsonFilePath);
		JsonPath jsonObj = JsonPath.from(jsonFilePath);

    	//  Grab the raw JSON data from the file...
    	String rawJson = loadJsonFile(jsonFilePath);

    	loadJsonDataAsDataPoolData(rawJson);

    	return jsonObj;
    }

	public void loadJsonPath(Response responseObject) throws IOException
	{
		//  Extract the content from the response and display it
		//  in the log...
		String content = responseObject.body().string();

		//  Parse the response body content...
		jsonPath = JsonPath.from(content);
	}

	public String loadJsonFile(String jsonFilePath)
	{
		String jsonRequestBody = "";

		try
		{
			jsonFilePath = DynamicData.convertRelativePathToFullPath(jsonFilePath);
			File file = new File(jsonFilePath);

			jsonRequestBody = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
		}
		catch (IOException e)
		{
			String errMsg = "Failure while attempting to read '" + jsonFilePath + "'!";
			logger.error(errMsg);
			logger.error(e.getMessage());
		}

		return jsonRequestBody;
	}

    public void add(String key, Object value)
    {
        String actionType = "Adding";

        if(this.containsKey(key))
        {
            actionType = "Setting";
        }

        this.put(key, value);

        if (stepLogger != null)
        {
        	String msg = actionType + " [" + key + "]: " + padRight(value.toString(), 30, ' ').substring(0, 30).trim() + "...";
            stepLogger.log(msg);
        }
    }

	public static String padRight(String original, int padToLength)
	{
	    return padRight(original, padToLength, ' ');
	}

	public static String padRight(String original, int padToLength, char padWith)
	{
	    if (original.length() >= padToLength)
	    {
	        return original;
	    }

	    StringBuilder sb = new StringBuilder(padToLength);
	    sb.append(original);

	    for (int i = original.length(); i < padToLength; ++i)
	    {
	        sb.append(padWith);
	    }

	    return sb.toString();
	}

    public String dumpDataPool()
    {
        StringBuilder retVal = new StringBuilder();

        this.forEach(
		    			(key, value) ->
				        {
              retVal.append("    " + key + " : " + value.toString() + newLine);
              retVal.append("    -------------------------------------" + newLine);
				        }
				     );

        return retVal.toString();
    }

    public String detokenizeDataPoolValues(String tokenizedString)
    {
    	List<String> list = new ArrayList<String>();
    	list.add(tokenizedString);

        this.forEach(
		    			(k, v) ->
				        {
				            if (tokenizedString.contains(k))
				            {
				            	int listIndex = list.size();
				            	String tmp = list.get(listIndex).replace("$" + k + "$", v.toString());
				            	list.add(tmp);
				        	}
				        }
				     );

        int listIndex = list.size();
        String deTokenizedString = list.get(listIndex);

        if (deTokenizedString.matches(wildcardToRegex("*$*$*")))
        {
            logger.info("**** WARNING!  Detokenzied string appears to still contain tokenized values!");
        }

        return deTokenizedString;
    }

    public static String wildcardToRegex(String wildcard){
        StringBuffer s = new StringBuffer(wildcard.length());
        s.append('^');
        for (int i = 0, is = wildcard.length(); i < is; i++) {
            char c = wildcard.charAt(i);
            switch(c) {
                case '*':
                    s.append(".*");
                    break;
                case '?':
                    s.append(".");
                    break;
                    // escape special regexp-characters
                case '(': case ')': case '[': case ']': case '$':
                case '^': case '.': case '{': case '}': case '|':
                case '\\':
                    s.append("\\");
                    s.append(c);
                    break;
                default:
                    s.append(c);
                    break;
            }
        }
        s.append('$');
        return(s.toString());
    }
}
