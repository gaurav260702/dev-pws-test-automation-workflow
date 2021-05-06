package com.autodesk.pws.test.steps.invoice;

import com.autodesk.pws.test.steps.base.*;

public class GetInvoiceList extends GetServiceBase
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
		//  Setting up a special case here for modified GetInvoiceDetails paths.
		//  This allows negative testing (dropping "invoice_number" or "customer_number")
		//  or modifying the ResourcePath to allow for "sales_order_number"...
		if(DataPool.containsKey(ClassName + ".ResourcePath"))
		{
			ResourcePath = DataPool.get(ClassName + ".ResourcePath").toString();
		}
		else
		{
			ResourcePath = "/v1/invoices?customer_number=$CUSTOMER_NUMBER$&invoice_date_from=$INVOICE_DATE_FROM$&invoice_date_to=$INVOICE_DATE_TO$";
		}
    }

	@Override
    public void action()
    {
		super.action();
    }
}
