package com.autodesk.pws.test.utilities;

import java.util.Arrays;


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
        	int indexMarker = source.indexOf(match);
        	
            if (indexMarker > -1)
            {
                strLeft = source.substring(0, indexMarker);
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

        if (source.indexOf(match) > -1)
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
    
    //
    // FROM: https://www.geeksforgeeks.org/wildcard-pattern-matching
    //
    // Function that matches input str with given wildcard pattern...
    //
    public static boolean patternMatch(String stringToTest, String patternToMatch)
    {
    	int testStringLength = stringToTest.length();
    	int patternLength = patternToMatch.length();
    	
        // Empty pattern can only match with empty string...
        if (patternLength == 0)
        {   
        	return (testStringLength == 0);
        }
 
        // Lookup table for storing results of subproblems...
        boolean[][] lookup = new boolean[testStringLength + 1][patternLength + 1];
 
        // Initialize lookup table to false...
        for (int i = 0; i < testStringLength + 1; i++)
        {
            Arrays.fill(lookup[i], false);
        }
        
        // Empty pattern can match with empty string...
        lookup[0][0] = true;
 
        // Only '*' can match with empty string...
        for (int j = 1; j <= patternLength; j++)
        {
            if (patternToMatch.charAt(j - 1) == '*')
            {
            	lookup[0][j] = lookup[0][j - 1];
            }
        }
        
        // Fill the table in bottom-up fashion..F
        for (int i = 1; i <= testStringLength; i++)
        {
            for (int j = 1; j <= patternLength; j++)
            {
                if (patternToMatch.charAt(j - 1) == '*')
                {
                    // Two cases if we see a '*':
                    //   a) We ignore '*'' character and move to next  character 
                    //      in the pattern, i.e., '*' indicates an empty sequence
                    //   b) '*' character matches with ith character in input
                	lookup[i][j] = lookup[i][j - 1] || lookup[i - 1][j];
                }
                else if (patternToMatch.charAt(j - 1) == '?' || stringToTest.charAt(i - 1) == patternToMatch.charAt(j - 1))
                {
                    // Current characters are considered as matching in two cases:
                    //   (a) current character of pattern is '?'
                    //   (b) characters actually match
                    lookup[i][j] = lookup[i - 1][j - 1];
                }
                else
                {
                    // If characters don't match...
                    lookup[i][j] = false;
                }
            }
        }
 
        return lookup[testStringLength][patternLength];
    }
    
}

