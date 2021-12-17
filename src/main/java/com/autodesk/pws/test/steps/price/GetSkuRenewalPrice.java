package com.autodesk.pws.test.steps.price;

public class GetSkuRenewalPrice extends GetSkuPrice 
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
    	this.rootPreparation();
    }

    private void setResourcePath()
    {
		super.setResourcePath("/v1/sku/prices?customer_number=$CUSTOMER_NUMBER$&part_number=$PRODUCT_SKU_SECONDARY$&price_date=$PRICE_DATE$&quantity=$QUANTITY$&agreement_end_date=$EXTENSION_DATE$&agreement_number=$AGREEMENT_NUMBER$", true);
    }
}
