package com.autodesk.pws.test.steps.utility;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.autodesk.pws.test.processor.*;
import com.autodesk.pws.test.steps.base.*;

public class LoadBaseFiles extends RestActionBase
{
    public String RequestFileLabel;
    public String OverridesFileLabel;
    public String DataPoolLabelOrderInfoRawJson;
    public String DataPoolLabelOrderInfoOverridesJson;
    public String DataPoolLabelOrderInfoFinalJson;

    public LoadBaseFiles()
    {
    	setConstructorDefaults();
    }

    public void setConstructorDefaults()
    {
        RequestFileLabel = "baseFile";
        OverridesFileLabel = "overridesFile";
        DataPoolLabelOrderInfoRawJson = "rawBaseFile";
        DataPoolLabelOrderInfoOverridesJson = "rawOverrideFile";
        DataPoolLabelOrderInfoFinalJson = "OrderInfo";
    }

    @Override
    public void preparation()
    {
    	log("Initializiing variables...");
    	initVariables();
    }

    private void initVariables()
    {
    	this.ClassName = this.getClass().getName();
    	pullDataPoolVariables();
	}

	private void pullDataPoolVariables()
    {
    	//  Set variables that are extracted
		//  from the DataPool Here...
	}

	
	private List<String>getFileList(String dirOrFilePath) 
	{
	    Set<String> fileSet = new HashSet<>();
	    
	    String dir = "";
	    
	    dirOrFilePath = DynamicData.convertRelativePathToFullPath(dirOrFilePath);
	    
	    File file = new File(dirOrFilePath);
	    
	    if(file.isDirectory())
	    {
	    	dir = dirOrFilePath;
	    }
	    else
	    {
	    	dir  = file.getParent();
	    }
	    
	    try
	    {
		    try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dir)))
		    {
		        for (Path path : stream) 
		        {
		            if (!Files.isDirectory(path)) 
		            {
		                fileSet.add(path.getFileName().toString());
		            }
		            else
		            {
		            	fileSet.add("[" + path.toString() + "]");
		            }
		        }
		    }
	    }
	    catch (Exception ex)
	    {
	    	log("Error during 'getFileList'!");
	    	log(ex);
	    }
	    
	    List<String> sortedList = new ArrayList<>(fileSet);
	    Collections.sort(sortedList);
	    
	    return sortedList;
	}
	
    @Override
    public void action()
    {
        //  Load in default test data, relevant overrides,
        //  and merge into order info object...
    	log("Detokenizing '" + RequestFileLabel.toString());
    	String baseFilePath = DynamicData.detokenizeRuntimeValues((String)DataPool.get(RequestFileLabel.toString()));
    	
    	log("");
    	log("Loading base data file: " + baseFilePath);
    	
    	var fileList = getFileList(baseFilePath);
    	
    	log("Directory contents:", DEFAULT_LEFT_SPACE_PADDING + 4);
    	for (String file : fileList) 
    	{
    		log(file, DEFAULT_LEFT_SPACE_PADDING + 8);
    	}
    	
    	String baseFileRaw = DynamicData.loadJsonFile(baseFilePath);
        DataPool.add(DataPoolLabelOrderInfoRawJson, baseFileRaw);

    	log("");
    	log("Loading override data file: " + baseFilePath);
        String overrideInfoRaw = DynamicData.loadJsonFile((String)DataPool.get(OverridesFileLabel));
        DataPool.add(DataPoolLabelOrderInfoOverridesJson, overrideInfoRaw);

        //  Copy the raw order info to prep for merging...
        //  Union array values together to avoid duplicates...
        Map<String, Object> orderInfoFinal = mergeJsonFromStrings(baseFileRaw, overrideInfoRaw);

        //  Strip out any elements with "null" values...
    	orderInfoFinal = removeAllNullValuesFromJson(orderInfoFinal);
    	
        //  Pop the final result into the DataPool...
        DataPool.add(DataPoolLabelOrderInfoFinalJson, orderInfoFinal);
    }
}

