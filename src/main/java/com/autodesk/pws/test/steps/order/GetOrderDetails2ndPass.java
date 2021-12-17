package com.autodesk.pws.test.steps.order;

public class GetOrderDetails2ndPass extends GetOrderDetails 
{
	@Override
    public void preparation()
    {
		//  Do some basic variable preparation...
		super.preparation();
		
    	//  Need to set the ClassName here as this will be
        // used by the super/base classes ".preparation()" method.
		this.ClassName = this.getClass().getSimpleName();
    }
}
