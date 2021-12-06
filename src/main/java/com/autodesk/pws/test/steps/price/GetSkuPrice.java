package com.autodesk.pws.test.steps.price;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.autodesk.pws.test.steps.base.*;

public class GetSkuPrice extends PwsServiceBase
{
    @Override
    public void preparation()
    {
		//  Do some basic variable preparation...

    	//  Need to set the ClassName here as this will be
        // used by the super/base classes ".preparation()" method.
		this.ClassName = this.getClass().getSimpleName();
    	
		//  Initialize locally relevant variables...
    	initVariables();
    	
    	//  Set the Resource path BEFORE the base/super class
		//  sets the targetUrl..
		setResourcePath();
		
    	//  Do stuff that the Action depends on to execute...
    	super.preparation();
    }

    private void setResourcePath()
    {
		super.setResourcePath("/v1/sku/prices?customer_number=$CUSTOMER_NUMBER$&part_number=$SKU_OR_PART_NUMBER$&price_date=$PRICE_DATE$&quantity=$QUANTITY$", true);
    }
    
    public void initVariables()
    {
    	String pattern = "yyyy-MM-dd";
    	SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
    	String date = simpleDateFormat.format(new Date());
    	
    	DataPool.add("$PRICE_DATE$", date);
    }

	@Override
    public void action()
    {
		super.action();
    }
	
	@Override
	public void validation()
	{
		//  Run the parent class validation first because that's when
		//  the JSON response is extract into this.JsonresponseBody
		//  and any operations which deoend on it must naturally occur
		//  after it has been filled out...
    	super.validation();
    	
    	super.setExecutionAbortFlagOnError();
    	
    	//  Extact data that may be needed by other steps later on...	
    	extractDataFromJsonAndAddToDataPool("$NET_PRICE$", "response.net_price"); 
    	
    	//  This is a $NET_PRICE$ format hack...
    	String netPrice = DataPool.get("$NET_PRICE$").toString();
    	float value = Float.parseFloat(netPrice);
    	netPrice = String.format("%.2f", value);
    	DataPool.add("$NET_PRICE$", netPrice);
	}
}
