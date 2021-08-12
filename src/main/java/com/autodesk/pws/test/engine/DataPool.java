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
import com.autodesk.pws.test.processor.*;
import com.autodesk.pws.test.steps.base.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.restassured.path.json.JsonPath;
import okhttp3.Response;

public class DataPool extends HashMap<String, Object>
{
	private static final long serialVersionUID = -7447329407872389743L;

    protected final Logger logger = LoggerFactory.getLogger(DataPool.class);

	//  Declare JsonPath container to use during validation..
	public JsonPath jsonPath;
	//public Logger logger;
    public StepBase StepLogger;
    public final String NewLine = System.getProperty("line.separator");

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
        String previousValue = "";
        
        if(this.containsKey(key))
        {
            actionType = "Setting";
            previousValue = this.get(key).toString();
        }

        this.put(key, value);

        if (StepLogger != null)
        {
        	String msg = "       " + actionType + " [" + key + "]: " + padRight(value.toString(), 80, ' ').substring(0, 80).trim();
            StepLogger.log(msg);
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
              retVal.append("    " + key + " : " + value.toString() + NewLine);
              retVal.append("    -------------------------------------" + NewLine);
				        }
				     );

        return retVal.toString();
    }

    public String detokenizeDataPoolValues(String tokenizedString)
    {
    	//  ------ NOTE ----
    	//  Even though this is memory intensive and not exactly optimal,
    	//  we use a "swapping container" approach here since in JAVA we're
    	//  not allowed to operate directly on strings while looping.
    	//  We'll simply add in the "Latest Version" of our detokenized string
    	//  to the last entry of the swapping container.  When we're all said
    	//  and done, the very last entry should and will be a fully detokenzied
    	//  version of the original string and we will have covered all possible
    	//  tokens that might have existed in the string...
    	//  ------ NOTE ----
    	
    	//  Setup a swapping container for the tokens in the tokenized string...
    	List<String> list = new ArrayList<String>();
    	
    	//  Add the current version of the tokenzied string to the swapping container...
    	list.add(tokenizedString);
    	
    	//  Loop through the list of tokens & values in the DataPool...
        this.forEach(
		    			(key, value) ->
				        {
				        	//  If we actually **find** a key that exists in the
				        	//  tokenized string...
				            if (tokenizedString.contains(key))
				            {
				            	//  Then we get the most recent entry index of the 
				            	//  swapping container...
				            	int listIndex = list.size() - 1;
				            	
				            	//  We create a **>>NEW<<** string that contains the
				            	//  detokenized version of the string by swapping out "Key"
				            	//  for value...
				            	String tmp = list.get(listIndex).replace(key, value.toString());
				            	
				            	//  And we add this new string to the swapping container...
				            	list.add(tmp);
				        	}
				        }
				     );

        //  Grab the last entry's index...
        int listIndex = list.size() - 1;
        
        //  This should be the fully detokenized string...
        String deTokenizedString = list.get(listIndex);

        //  If we're still seeing what appears to be tokens in the detokenized string...
        if (deTokenizedString.matches(wildcardToRegex("*$*$*")))
        {
        	//  We need to make a note of it in the log, as it's possible we have an
        	//  unresolved token.
            logger.info("**** WARNING!  Detokenzied string appears to still contain tokenized values!");
            
            //  Recusively call this method to finish detokenizing the string...
            //  ---- NOTE ----
            //  Should we be doing this recursive call here to continue attempting
            //  to detokenize the string?  Could that somehow possibly result in an
            //  infinite loop?
            deTokenizedString = detokenizeDataPoolValues(deTokenizedString);
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
