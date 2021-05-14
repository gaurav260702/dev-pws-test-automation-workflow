package com.autodesk.pws.test.steps.order;

import com.autodesk.pws.test.steps.base.*;

public class PostOrder extends PwsServiceBase
{    
    @Override
    public void preparation()
    {
		//  Do some basic variable preparation...
    	
    	//  Need to set the ClassName here as this will be
    	//  used by the super/base classes ".preparation()" 
    	//  method.
		this.ClassName = this.getClass().getSimpleName();
		
		//  Set the ServiceVerb to a "POST" style service.
		//  We're doing this first in case there are any 
		//  init methods that may have a dependency on it.
		//  Naturally, we can't allow any 'setAs***Service()'
		//  methods to have any dependencies if it's being
		//  called before anything else...
		this.setAsPostService();
		
		//  Set the Resource path BEFORE the base/super class
		//  sets the targetUrl..
		setResourcePath();
		
    	//  Do stuff that the Action depends on to execute...
    	super.preparation();
    	
    	//  TODO:  Need to get the 'JsonRequestBody'...
    	this.setJsonRequestBody(DataPool.get("OrderInfo").toString());
    }

    private void setResourcePath()
    {
		//  Setting up a special case here for modified GetInvoiceDetails paths.
		//  This allows negative testing (dropping "invoice_number" or "customer_number")
		//  or modifying the ResourcePath to allow for "sales_order_number"...
		if(DataPool.containsKey(ClassName + ".ResourcePath"))
		{
			ResourcePath = DataPool.get(ClassName + ".ResourcePath").toString();
		}
		else
		{
			ResourcePath = "/v2/orders/fulfillment";
		}
    }

	@Override
    public void action()
    {
		super.action();
    }
}
