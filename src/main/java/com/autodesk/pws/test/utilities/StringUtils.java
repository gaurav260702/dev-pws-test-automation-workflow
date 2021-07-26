package com.autodesk.pws.test.utilities;

public class StringUtils 
{
    public static String getLeft(String source, String match)
	{
		return getLeft(source, match, true);
	}
	
    public static String getLeft(String source, String match, Boolean returnZeroLengthOnNomatch)
    {
        String strLeft = "";

        try
        {
        	
            if (source.indexOf(match) > 0)
            {
                strLeft = source.substring(0, source.indexOf(match) - 1);
            }
            else if (returnZeroLengthOnNomatch != true)
            {
                strLeft = source;
            }

            return strLeft;
        }
        catch (Exception ex) 
        {
        	//  We're going to treat all exceptions as if there
        	//  was no match in the String at all...
            return "";
        }
    }
    
    public static String getRight(String source, String match)
    {
    	return getRight(source, match, true);
    }
    
    public static String getRight(String source, String match, Boolean returnZeroLengthOnNomatch)
    {
        String strRight = "";

        if (source.indexOf(match) > 0)
        {
            strRight = source.substring(source.indexOf(match) + match.length());
        }
        else if (returnZeroLengthOnNomatch != true)
        {
            strRight = source;
        }

        return strRight;
    }
    
    public static String getBetween(String source, String startPattern, String endPattern)
    {
    	return getBetween(source, startPattern, endPattern, 0);
    }
    
    public static String getBetween(String source, String startPattern, String endPattern, int startingIndex)
    {
        String tmp = source.substring(startingIndex);
        String strRight = getRight(tmp, startPattern, true);
        String strLeft = getLeft(strRight, endPattern, true);

        return strLeft;
    }
}

