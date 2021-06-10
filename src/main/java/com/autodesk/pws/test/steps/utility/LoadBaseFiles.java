package com.autodesk.pws.test.steps.utility;

import java.util.Map;
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

    @Override
    public void action()
    {
        //  Load in default test data, relevant overrides,
        //  and merge into order info object...
    	String baseFilePath = (String)DataPool.get(RequestFileLabel.toString());
        String baseFileRaw = DynamicData.loadJsonFile(baseFilePath);
        DataPool.add(DataPoolLabelOrderInfoRawJson, baseFileRaw);

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

