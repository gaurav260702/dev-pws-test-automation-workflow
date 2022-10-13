package com.autodesk.pws.test.steps.order;

import com.autodesk.pws.test.steps.base.*;

public class GetOrderDetails extends PwsServiceBase
{
   @Override
    public void preparation()
    {
		//  Do some basic variable preparation...

    	//  Need to set the ClassName here as this will be
        // used by the super/base classes ".preparation()" method.
		this.ClassName = this.getClass().getSimpleName();
    	
		//  Initialize locally relevant variables...
    	//initVariables();
    	
    	//  Set the Resource path BEFORE the base/super class
		//  sets the targetUrl..
		setResourcePath();
    	//  Do stuff that the Action depends on to execute...
    	super.preparation();
    }

    private void setResourcePath()
    {
		super.setResourcePath("/v1/orders?partner_po=$PO_NUMBER$&customer_number=$CSN_RESELLER$", true);
    }

	@Override
    public void action()
    {
		this.sleep(10000);
		super.action();
    }
	
	@Override
	public void validation()
	{
		this.extractDataFromJsonAndAddToDataPool("$CONTRACT_NUMBER$", "message.elements[0].order_header_array[0].contract_number");
		this.extractDataFromJsonAndAddToDataPool("$AGREEMENT_NUMBER$", "message.elements[0].order_header_array[0].contract_number");
		super.validation();
	}
}
