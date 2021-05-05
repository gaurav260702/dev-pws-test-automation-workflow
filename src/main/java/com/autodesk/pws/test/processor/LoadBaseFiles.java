package com.autodesk.pws.test.processor;

import java.util.Map;

public class LoadBaseFiles extends RestActionBase
{
    public String RequestFileLabel;
    public String OverridesFileLabel;
    public String DataPoolLabelOrderInfoRaw;
    public String DataPoolLabelOrderInfoOverrides;
    public String DataPoolLabelOrderInfoFinal;

    public LoadBaseFiles()
    {
    	setConstructorDefaults();
    }

    public void setConstructorDefaults()
    {
        RequestFileLabel = "baseFile";
        OverridesFileLabel = "overridesFile";
        DataPoolLabelOrderInfoRaw = "rawBaseFile";
        DataPoolLabelOrderInfoOverrides = "rawOverrideFile";
        DataPoolLabelOrderInfoFinal = "OrderInfo";
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
    	String baseFilePath = (String)this.DataPool.get(RequestFileLabel.toString());
        String baseFileRaw = DynamicData.loadJsonFile(baseFilePath);
        DataPool.add(DataPoolLabelOrderInfoRaw, baseFileRaw);

        String overrideInfoRaw = DynamicData.loadJsonFile((String)DataPool.get(OverridesFileLabel));
        DataPool.add(DataPoolLabelOrderInfoOverrides, overrideInfoRaw);

        //  Copy the raw order info to prep for merging...
        //  Union array values together to avoid duplicates...
        Map<String, Object> orderInfoFinal = mergeJsonFromStrings(baseFileRaw, overrideInfoRaw);

        //  Strip out any elements with "null" values...
    	orderInfoFinal = removeAllNullValuesFromJson(orderInfoFinal);

        //  Pop the final result into the DataPool...
        DataPool.add(DataPoolLabelOrderInfoFinal, orderInfoFinal);
    }
}

