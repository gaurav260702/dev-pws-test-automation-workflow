package com.autodesk.pws.test.processor;

import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.lang.reflect.Method;  
import java.util.*;
import java.util.AbstractMap.*;


public class SimpleScripter  
{  
	//
	//  Keeping the "main()" method below for future test purposes...
	//
//    public static void main(String []args)
//    {
//        String scriptLine = "STRING START___${MakeSpace(5)|ReduceAThing(ReduceAThing('First Half', MakeSpace(10)), 'Second Half')}$___MORE STRING___${MakeSpace(5)|ReduceAThing(ReduceAThing('First Half', MakeSpace(10)), 'Second Half')}$___END STRING___";
//        
//        debugLog(extractAndResolveSimpleScripts(scriptLine, "${", "}$"));
//    }
    
    public static String extractAndResolveSimpleScripts(String simpleScriptLine, String scriptMarkerStart, String scriptMarkerEnd)
	{
    	//  Settting the return value to the original input value
    	//  in case there *are* no SimpleScript lines embedded 
    	//  in the string.  That way, we'll return whatever the 
    	//  original value was that we sent in.  This is appropriate
    	//  since there will be no script items to resolve and the
    	//  string is "pre-resolved"...
		String retVal = simpleScriptLine;
		
		boolean keepResolving = true;
		
		while(keepResolving)
		{
			//  Grab the indicies of the marker start and marker end...
			int startPoint = simpleScriptLine.indexOf(scriptMarkerStart) + scriptMarkerStart.length();
		    int endPoint = simpleScriptLine.indexOf(scriptMarkerEnd);
		    
		    //  Check to see if the stand/end points exist correctly...
			if(startPoint > 0 && endPoint > startPoint)
			{
				//  If the above evaluations are true, then there is an embeded SimpleScript line somewhere
				//  in the text fragment.   We will resolve the SimpleScript chunks so long as they are
				//  detected in the text fragment...
	            String simpleScriptExtract = simpleScriptLine.substring(startPoint, endPoint);
	            
	            debugLog(simpleScriptExtract);
	            debugLog("=====================");
	            
			    String extractValue = resolve(simpleScriptExtract);
			    
			    debugLog(">>>> RESOLVED EXTRACTION VALUE <<<<");
			    debugLog(extractValue);
			    debugLog(">>>> --- <<<<");
			    
			    String targetToReplace = scriptMarkerStart + simpleScriptExtract + scriptMarkerEnd;
			    
			    debugLog(">>>> TARGET TO REPLACE <<<<");
			    debugLog(targetToReplace);
			    debugLog(">>>> --- <<<<");
			   
			    simpleScriptLine = simpleScriptLine.replace(targetToReplace , extractValue);
			    
			    debugLog(">>>> POST REPLACE SOURCE <<<<");
			    debugLog(simpleScriptLine);
			    debugLog(">>>> --- <<<<");
			    
			    //  Do I need to rewrite this to look more like the "if.startpoint/endpoint" check above?
			    //  Will I run into scenarios where an endpoint occurs before a start point?
			    if(simpleScriptLine.contains(scriptMarkerStart) && simpleScriptLine.contains(scriptMarkerEnd))
			    {
			        // Do nothing.  We need to keep resolving...
			        //  keepResolving = false;
			    }
			    else
			    {
			        keepResolving = false;
			    }
			}
			else
			{
				keepResolving = false;
			}
		}
	
		return retVal;
	}
     
    public static String resolve(String simpleScriptLine) 
    {
    	//  Split the incoming line into bite-sized segments...
    	String[] scriptSegments = simpleScriptLine.split("[|]");
    	
    	String retVal = "";
    	
    	//  Loop through each segement...
    	for(String segment: scriptSegments)
    	{
    		//  Reduce the segment expressions to their final value...
    		String reduction = resolveScriptSegment(segment);
    		reduction = reduction.substring(reduction.indexOf(":") + 1).trim();
    		retVal = retVal + reduction;
    	}
    	
    	return retVal;
    }
    
    private static String resolveScriptSegment(String scriptSegment)
    {
    	//  Prepare a return value...
        String retVal = "";
        
        //  Break the segment into parsable tokens...
        List<String> fullTokenStream = getTokenizedScriptLine(scriptSegment);
        
        debugLog("-------------------------------------------");
    
        boolean keepReducing = true;
        
        //  Set a loop to keep processing the expression tokens  
        //  until there's nothing more to reduce...
        while(keepReducing)
        {
        	//  Find the first complete, innermost expression...
            List<SimpleEntry<String,Integer>> innerMost = findInnermostExpression(fullTokenStream);
            
            debugLog("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ INNERMOST ~~~~~~");

            //  The resulting tokens come back in reverse order, so flip them around...
            Collections.reverse(innerMost);
            
            //  This is just for debugging purposes, if needed...
            for (int i = 0; i < innerMost.size(); i++) 
            {
                debugLog(innerMost.get(i).toString());
            }
            
            //  Grab the first token index from the "innerMost" token 
            //  stream.  This index corresponds to the token's position
            //  in the full token stream.  
            int tokenIndexMarker = innerMost.get(0).getValue();
            
            //  Reduce the innerMost's expression to it's final value...
            String reductionValue = reduceExpression(innerMost);
            
            //  Change the "text" value at the previously grabbed token
            //  index to the reduction value we just obtained..  This
            //  will now be the value of the expression at that point in
            //  the full token stream...
            fullTokenStream.set(tokenIndexMarker, reductionValue);
            
            debugLog("--->>>  REDUCING...");
            
            //  Remove all the remaining tokens that were part of the 
            //  previous "innerMost" expression as they're no longer needed...
            for (int i = 1; i < innerMost.size(); i++) 
            {
                fullTokenStream.remove(tokenIndexMarker + 1);
            }
            
            debugLog("###################################");
            
            //  This is just for debugging purposes, if needed...
            for(int i = 0; i < fullTokenStream.size(); i++)
            {
                debugLog(fullTokenStream.get(i));
            }
            
            debugLog("++++++++++++++++++++++++++++++++++"); 
            
            //  Check and see if we've reduced the full token stream
            //  to a single remaining token...
            if(fullTokenStream.size() == 1)
            {
            	//  If we have, we're done and we can break out of
            	//  the loop and return the final reduction value...
                retVal = fullTokenStream.get(0);
                keepReducing = false;
            }
        }
        
        //  Return the final reduction value...
        return retVal;
    }
    
    //  Fake method for educational purposes...
    public static String ReduceAThing(String a, String b)
    {
        debugLog("Inside 'ReduceAThing'...");
        
        String retVal = a + b;
        return retVal;
    }
    
    //  Fake method for educational purposes...
    public static String MakeSpace(String numberOfSpaces)
    {
        debugLog("Inside 'MakeSpace': '" + numberOfSpaces + "'...");
                
        double d = Double.parseDouble(numberOfSpaces);
        int spaceCount = (int) d;
        
        String retVal = "";
        
        for(int i = 0; i < spaceCount; i++)
        {
            retVal = retVal + "-";
        }
        
        debugLog("Return value: '" + retVal + "'...");
        
        return retVal;
    }
    
    //  Grabs a reference to the given method name...
    private static Method getMethodByNameOnly(String methodName)
    {
        Method retVal = null;
        
        //  Get a full list of the public methods available for this class...
        for (Method m : SimpleScripter.class.getMethods()) 
        {
        	//  Check to see if the current name matches the target...
            if (methodName.equals(m.getName())) 
            {
            	//  If so, set the return name and break out of the loop...
                retVal = m;
                break;
            }
        }
        
        return retVal;
    }
    
    //  Reduces a given set of tokens in an expression to its final value...
    private static String reduceExpression(List<SimpleEntry<String,Integer>> innerMostTokens)
    {
        debugLog("Inside 'reduceExpression'...");
        
        //  Prepare a container to hold the invocation result...
        Object invokeResult = null;
        
        //  Determine the method name that will be called.  This always
        //  corresponds to the first element in the token set...
        String targetMethodName = innerMostTokens.get(0).getKey();
        debugLog("Method target: " + targetMethodName);
        
        try
        {
            debugLog("Inside 'reduceExpression.try'...");
            
            //  Grab a basic reference to the target method...
            Method method = getMethodByNameOnly(targetMethodName);
    
            //  Prepare the parameter fingerprint...
            Class<?>[] params = method.getParameterTypes();

            //  Set the method reference to the full fingerprinted method...
            method = SimpleScripter.class.getDeclaredMethod(targetMethodName, params);
        
            debugLog("grabbed full method reference...");
            
            //  Load the parameters from the rest of the tokens in the token set...
            List<String> methodParams = loadMethodParameters(innerMostTokens);
    
            debugLog("Param size: " + methodParams.size());
            
            //  This is stupidly hokey, but because it's Java there's no easy
            //  way around it (viva la C#!).  If you create a method that takes
            //  **more** than 6 parameters, you'll need to create a new entry in
            //  the switch table below to be able to call it...
            switch(methodParams.size())
            {
                case 0:
                    invokeResult = method.invoke(null);
                    break;
                    
                case 1:
                    invokeResult = method.invoke(null, methodParams.get(0));  
                    break;   
                    
                case 2:
                    invokeResult = method.invoke(null, methodParams.get(0), methodParams.get(1));  
                    break;   
                    
                case 3:
                    invokeResult = method.invoke(null, methodParams.get(0), methodParams.get(1), methodParams.get(2));  
                    break; 
                    
                case 4:
                    invokeResult = method.invoke(null, methodParams.get(0), methodParams.get(1), methodParams.get(2), methodParams.get(3));  
                    break;  
                    
                case 5:
                    invokeResult = method.invoke(null, methodParams.get(0), methodParams.get(1), methodParams.get(2), methodParams.get(3), methodParams.get(4));  
                    break; 
                    
                case 6:
                    invokeResult = method.invoke(null, methodParams.get(0), methodParams.get(1), methodParams.get(2), methodParams.get(3), methodParams.get(4), methodParams.get(5));  
                    break;
            }
        }
        catch(Exception e)
        {
            debugLog(e.toString());
        }
        
        debugLog(invokeResult.toString());  
        
        //  Return the value of the method call along with 
        //  the appropirate reduction label...
        return "Reduction : " + invokeResult;
    }
    
    //  Takes a set of tokens and returns only the tokens which have actual values
    //  and ignores any symbolic token markers and delimiters...
    private static List<String> loadMethodParameters(List<SimpleEntry<String,Integer>> methodCallTokens)
    {
    	//  Prepare a return container for the tokens that 
    	//  will make it through the filter below...
        List<String> paramTokens = new ArrayList<String>();
        
        //  Loop through all the tokens in the token set...
        for(int i = 1; i < methodCallTokens.size(); i ++)
        {
        	//  Grab the "key" of the token, which is really just its value...
            String token = methodCallTokens.get(i).getKey();
            
            //  Check the state of the token in question...
            switch(token)
            {
            	//  If it's one of the token makers, it's going to be ignored...
                case "(":
                case ")":
                case ",":
                    //  Ignore these tokens...
                    break;
                    
                //  And if it's not a symbolic token marker, then it must 
                //  be a value, so pop it into the return container...
                default:
                    paramTokens.add(token);        
                    break;
            }
        }
        
        //  Return the value-only tokens...
        return paramTokens;
    }
    
    
    private static void debugLog(String msg)
    {
    	//  Uncomment the line below to see the 
    	//  debug logging during execution...
        //  System.out.println(msg);
    }
    
    private static List<SimpleEntry<String,Integer>> findInnermostExpression(List<String> tokens)
    {
        //  Prepare a hash table to throw the appropriate tokens onto...
        List<SimpleEntry<String,Integer>> expressionTable = new ArrayList<SimpleEntry<String,Integer>>(); 
        
        //  Prepare a flag for when a balanced expression container has been found...
        boolean expressionBalanceSet = false;
        
        //  Prepare a flag for when the expression name has been found...
        boolean expressionNameFound = false;
        
        //  Start iterating **backward** through the token list...
        for (int i = tokens.size(); i-- > 0;) 
        {
            //  Grab the token at the index...
            String token = tokens.get(i).split(":")[1].trim();
            debugLog(token);
            
            //  Determine the disposition of the token...
            switch (token)
            {
                //  If it's a closing paren, we've found the end of an expression...
                case ")":
                    //  Check to see if there's one or more token already in the table...
                    if(expressionTable.size() > 0)
                    {
                        debugLog("--->>> CLEARING...");
                        debugLog("-------------------------------------------");
                        //  If there is, it means we've found a more deeply buried
                        //  expression and we need to reset the table...
                        expressionTable.clear();
                    }
                    break;
                    
                 case "(":
                    //  We're set to balance the expression with the next token...
                    expressionBalanceSet = true;
                    break;
                        
                 default:
                    //  Check to see if the expression balance flag has been set...
                    if(expressionBalanceSet)
                    {
                        //  Because if it has the next token completes the expression
                        //  and provides the name for the expresion...
                        expressionNameFound = true;
                    }
                    break;
            }
            
            //  Add the current token to the table...
            expressionTable.add(new SimpleEntry<String,Integer>(token, i));
            
            //  Check to see if the expression name has been found...
            if(expressionNameFound)
            {
                //  Because if it has we're all done here...
                break;
            }
        }
        
        return expressionTable;
    }
    
    //  Simply tokenizes the string passed in and returns a string list of tokens...
    private static List<String> getTokenizedScriptLine(String simpleScriptLine)
    {
        // Input
        // String simpleScriptLine = "AddDaysToDate(\"$INVOICE_DATE$\", 30)";
        // String test = "thisFunction( a() , 'b + 3', function2( 'as df jk lp' ) ) ";

    	//  Prep a return container...
        List<String> retVal = new ArrayList<String>();

        //  Init a string reader...
        Reader reader = new StringReader(simpleScriptLine);
        
        //  Init a streamtokenizer from the previously initialized string reader...
        StreamTokenizer tokenizer = new StreamTokenizer(reader);

        try 
        {
        	//  Grab each token and pop it into the return container 
        	//  as long as there's still a token to grab...
            while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) 
            {
                retVal.add(getTokenValue(tokenizer));
                debugLog(getTokenValue(tokenizer));
            }
        } 
        catch (Exception e) 
        {
            debugLog(e.toString());
        }
        
        return retVal;
    }

    //  Grabs the value of a token and determines its general type...
    private static String getTokenValue(StreamTokenizer st) 
    {
    	//  Prep a return container...
        String retVal = "";

        //  Set the return value based on the token type...
        switch(st.ttype)
        {
        	case StreamTokenizer.TT_NUMBER:
        		retVal = "Value   : " + st.nval;
        		break;

        	case StreamTokenizer.TT_WORD:
                retVal = "Function: " + st.sval;
                break;
          
            default:
            	//  If a token isn't a NUMBER or a WORD it 
            	//  requires some specail handling...
            	
                retVal = ((Object)st).toString();
                int start = retVal.indexOf("[") + 1;
                int end = retVal.lastIndexOf("]");
                retVal = retVal.substring(start, end);

                if (retVal.startsWith("'") && retVal.endsWith("'")) 
                {
                    retVal = retVal.substring(1, retVal.length() - 1);
                    retVal = "Marker  : " + retVal;
                } 
                else 
                {
                    retVal = "Literal : " + retVal;
                }            	
        }
        
        return retVal;
    }
}  