package com.autodesk.pws.test.steps.invoice;

import com.autodesk.pws.test.steps.base.*;

public class GetInvoiceDetails extends PwsServiceBase
{    
    @Override
    public void preparation()
    {
		//  Do some basic variable preparation...
    	
    	//  Need to set the ClassName here as this will be
    	//  used by the super/base classes ".preparation()" 
    	//  method.
		this.ClassName = this.getClass().getSimpleName();
		//  Set the Resource path BEFORE the base/super class
		//  sets the targetUrl..
		setResourcePath();
    	//  Do stuff that the Action depends on to execute...
    	super.preparation();
    }

    private void setResourcePath()
    {
		super.setResourcePath("/v1/invoices?customer_number=$CUSTOMER_NUMBER$&invoice_number=$INVOICE_NUMBER$");
    }

	@Override
    public void action()
    {
		super.action();
    }
}
