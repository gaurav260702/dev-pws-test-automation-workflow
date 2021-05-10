package com.autodesk.pws.test.steps.utility;

import java.util.Map;
import com.autodesk.pws.test.processor.DynamicData;
import com.autodesk.pws.test.steps.base.RestActionBase;

public class LoadBaseFiles extends RestActionBase
{
    private String requestFileLabel;
    private String overridesFileLabel;
    private String dataPoolLabelOrderInfoRaw;
    private String dataPoolLabelOrderInfoOverrides;
    private String dataPoolLabelOrderInfoFinal;

    public LoadBaseFiles()
    {
    	setConstructorDefaults();
    }

    public void setConstructorDefaults()
    {
        requestFileLabel = "baseFile";
        overridesFileLabel = "overridesFile";
        dataPoolLabelOrderInfoRaw = "rawBaseFile";
        dataPoolLabelOrderInfoOverrides = "rawOverrideFile";
        dataPoolLabelOrderInfoFinal = "OrderInfo"; // TODO: check is this working (O)/(o)rderInfo?
    }

    @Override
    public void preparation()
    {
    	initVariables();
    }

    private void initVariables()
    {
    	this.className = this.getClass().getName();
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
    	String baseFilePath = (String)this.dataPool.get(requestFileLabel.toString());
        String baseFileRaw = DynamicData.loadJsonFile(baseFilePath);
        dataPool.add(dataPoolLabelOrderInfoRaw, baseFileRaw);

        String overrideInfoRaw = DynamicData.loadJsonFile((String)dataPool.get(overridesFileLabel));
        dataPool.add(dataPoolLabelOrderInfoOverrides, overrideInfoRaw);

        //  Copy the raw order info to prep for merging...
        //  Union array values together to avoid duplicates...
        Map<String, Object> orderInfoFinal = mergeJsonFromStrings(baseFileRaw, overrideInfoRaw);

        //  Strip out any elements with "null" values...
    	orderInfoFinal = removeAllNullValuesFromJson(orderInfoFinal);

        //  Pop the final result into the DataPool...
        dataPool.add(dataPoolLabelOrderInfoFinal, orderInfoFinal);
    }
}

