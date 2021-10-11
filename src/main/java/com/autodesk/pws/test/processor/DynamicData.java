package com.autodesk.pws.test.processor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.restassured.path.json.JsonPath;

public class DynamicData
{
    protected static final Logger logger = LoggerFactory.getLogger(DynamicData.class);
    private static HashMap<String, Object> runtimeValues;

    public static void initRuntimeValues()
    {
        runtimeValues = new HashMap<String, Object>();

        generateNewRuntimeValues();
    }

    public static void generateNewRuntimeValues()
    {
    	if(runtimeValues == null)
    	{
    		runtimeValues = new HashMap<String, Object>();
    	}

        runtimeValues.put("{{po_number}}", getTicksAsString());
        runtimeValues.put("{{cust_po_number}}", getTicksAsString());
        runtimeValues.put("{{random_first_name}}", "fName-" + generateRandomLengthAlphaString(5));
        runtimeValues.put("{{random_last_name}}", "lName-" + generateRandomLengthAlphaString(5));
        runtimeValues.put("{{random_email_domain}}", "email-" + generateRandomLengthAlphaString(5));
        runtimeValues.put("{{net_price}}", "$NET_PRICE$");
        runtimeValues.put("{{uuid1}}", UUID.randomUUID().toString());
        runtimeValues.put("{{uuid2}}", UUID.randomUUID().toString());
        runtimeValues.put("{{uuid3}}", UUID.randomUUID().toString());
        runtimeValues.put("{{uuid4}}", UUID.randomUUID().toString());
    }

    public static String generateRandomLengthAlphaString(int length)
    {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;

        Random random = new Random();

        StringBuilder buffer = new StringBuilder(targetStringLength);

        for (int i = 0; i < targetStringLength; i++)
        {
            int randomLimitedInt = leftLimit + (int)(random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }

        return buffer.toString();
    }

    public static long getTicks()
    {
    	long TICKS_AT_EPOCH = 621355968000000000L;
    	long ticks = System.currentTimeMillis()*10000 + TICKS_AT_EPOCH;

    	return ticks;
    }

    public static String getTicksAsString()
    {
    	long ticks = getTicks();

    	return String.valueOf(ticks);
    }

    public static String runtimeLookup(String key)
    {
        String retVal = ">> UNKNOWN RUNTIME VALUE! <<";

        if(runtimeValues.containsKey(key))
        {
            retVal = runtimeValues.get(key).toString();
        }

        return retVal;
    }

	public static JsonPath loadJsonFileToJsonPath(String jsonFilePath)
	{
		jsonFilePath = convertRelativePathToFullPath(jsonFilePath);
		JsonPath jsonPathObj = JsonPath.from(jsonFilePath);

		return jsonPathObj;
	}

	public static String convertRelativePathToFullPath(String relativePath)
	{
		DynamicData dd = new DynamicData();
		String fullFilePath = dd.getClass().getClassLoader().getResource(relativePath).getFile();
		return fullFilePath;
	}

	public static String loadJsonFile(String jsonFilePath)
	{
		return loadJsonFile(jsonFilePath, true);
	}

	public static String loadJsonFile(String jsonFilePath, boolean includeDetokenization)
    {
		String jsonRequestBody = "";

		try
		{
			String fullFilePath = convertRelativePathToFullPath(jsonFilePath);
			File file = new File(fullFilePath);

			jsonRequestBody = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

			if(includeDetokenization)
			{
				jsonRequestBody = detokenizeRuntimeValues(jsonRequestBody);
			}

		}
		catch (IOException e)
		{
			String errMsg = "Failure while attempting to read '" + jsonFilePath + "'!";
			logger.error(errMsg);
			logger.error(e.getMessage());
		}

		return jsonRequestBody;
    }

    public static String detokenizeRuntimeValues(String tokenizedString)
    {
    	tokenizedString = detokenizeString(tokenizedString, "{{", "}}", runtimeValues);
    	tokenizedString = detokenizeString(tokenizedString, "$", "$", runtimeValues);

    	return tokenizedString;
    }

    public static String detokenizeRuntimeValuesAndCustomDictionary(String tokenizedString, HashMap<String, Object> tokenDictionary)
    {
    	tokenizedString = detokenizeString(tokenizedString, "{{", "}}", runtimeValues);
    	tokenizedString = detokenizeString(tokenizedString, "$", "$", runtimeValues);
    	tokenizedString = detokenizeString(tokenizedString, "{{", "}}", tokenDictionary);
    	tokenizedString = detokenizeString(tokenizedString, "$", "$", tokenDictionary);

    	return tokenizedString;
    }

    public static String detokenizeString(String tokenizedString, String tokenStart,
    									  String tokenEnd, HashMap<String, Object> tokenDictionary)
    {
    	//  We're going to create an array list to hold each of the
    	//  new instances of the detokenized string.  I'm sure there's
    	//  a much better way to do this, but in C# you're allowed to
    	//  modify a string while in-loop.  Java seems to barf on that.
    	//  Sooooo...long story short, this is my wacky workaround...
    	List<String> list = new ArrayList<String>();

    	//  Push the raw "tokenized" string into the container array...
    	list.add(tokenizedString);

    	//  Start looping through each of the tokens...
    	tokenDictionary.forEach(
				    			(k, v) ->
						        {
						        	//  Check to see if the value is genuinely a token value...
						        	if(k.startsWith(tokenStart) && k.endsWith(tokenEnd))
						        	{
							        	//  If the tokenized string has the value in question...
							            if (tokenizedString.contains(k))
							            {
							            	//  Grab the last version of the detokenzied string...
							            	int listIndex = list.size() - 1;
							            	//  Create a newly detokenized version of the string
							            	//  with the latest token we've pulled up...
							            	String tmp = list.get(listIndex).replace(k, v.toString());
							            	//  Add the newly detokenized stirng to the array...
							            	list.add(tmp);
							        	}
						        	}
						        }
							   );

    	//  Grab the index for the last detokenized string in the array...
        int listIndex = list.size() - 1;
        //  Grab the string from the array...
        String deTokenizedString = list.get(listIndex);

        //  Return the fully deotokenized string...
        return deTokenizedString;
    }

    public static String wildcardToRegex(String wildcard)
    {
        StringBuffer s = new StringBuffer(wildcard.length());
        s.append('^');
        for (int i = 0, is = wildcard.length(); i < is; i++)
        {
            char c = wildcard.charAt(i);
            switch(c)
            {
	            case '+':
	            	s.append("[+]");
	            	break;
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

	public static String prettyJson(String unprettyJson)
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement je = JsonParser.parseString(unprettyJson);
        String prettyJsonString = gson.toJson(je);
        return prettyJsonString;
    }

	public static String getSimpleDatTimeFormat() 
	{
		Date date = Calendar.getInstance().getTime();  
        DateFormat dateFormat =  new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss.SSS");
        String strDate = dateFormat.format(date);  
        
        return strDate;
	}
}

