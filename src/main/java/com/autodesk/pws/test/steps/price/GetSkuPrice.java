package com.autodesk.pws.test.steps.price;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.autodesk.pws.test.steps.base.*;

import io.restassured.path.json.JsonPath;

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
		super.setResourcePath("/v1/sku/prices?customer_number=$CUSTOMER_NUMBER$&part_number=$SKU_OR_PART_NUMBER$&price_date=$PRICE_DATE$&quantity=$QUANTITY$");
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
		//  Here we would extract any data that needs
		//  to be promoted in the DataPool.
		//  We would extract stuff out of:
		//           this.JsonResponseBody
    	JsonPath pathFinder = JsonPath.with(JsonResponseBody);

    	//  Extact data that may be needed by other steps later on...	
    	extractDataFromJsonAndAddToDataPool("$NET_PRICE$", "response.net_price", pathFinder); 
	}
}
