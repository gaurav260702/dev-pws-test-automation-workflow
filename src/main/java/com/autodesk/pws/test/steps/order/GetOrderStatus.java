package com.autodesk.pws.test.steps.order;

import com.autodesk.pws.test.steps.base.*;

public class GetOrderStatus extends PwsServiceBase
{
   @Override
    public void preparation()
    {
    	//  Need to set the ClassName here as this will be
        // used by the super/base classes ".preparation()" method.
		this.ClassName = this.getClass().getSimpleName();
    	
    	//  Set the Resource path BEFORE the base/super class
		//  sets the targetUrl..
		setResourcePath();
    	//  Do stuff that the Action depends on to execute...
    	super.preparation();
    }

    private void setResourcePath()
    {
    	super.setResourcePath("/v2/orders/status/$TRANSACTION_ID$?detailed=true");
    	
    	//  Keeping the line below because it *used* to work at one time
    	//  but now doesn't.  Wondering if we somehow slipped from using the "internal"
    	//  service to the "external" service?  Not sure what's going on...
		//super.setResourcePath("/v2/orderstatus/$TRANSACTION_ID$?detailed=true");
    }

	@Override
    public void action()
    {
		super.action();
    }
	
	@Override
	public void validation()
	{
		extractDataFromJsonAndAddToDataPool("$CONTRACT_NUMBER$", "contractNumber");
		extractDataFromJsonAndAddToDataPool("$SALES_ORDER_NUMBER$", "salesOrderNumber");
	}
}
