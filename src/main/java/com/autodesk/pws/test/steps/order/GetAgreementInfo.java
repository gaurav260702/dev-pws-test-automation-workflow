package com.autodesk.pws.test.steps.order;

import com.autodesk.pws.test.steps.base.*;

public class GetAgreementInfo extends PwsServiceBase
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
    	super.setResourcePath("/v1/agreement/$AGREEMENT_NUMBER$");
    }

	@Override
    public void action()
    {
		super.action();
    }
}
