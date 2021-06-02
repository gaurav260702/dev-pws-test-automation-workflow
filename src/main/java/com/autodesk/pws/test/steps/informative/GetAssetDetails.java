package com.autodesk.pws.test.steps.informative;

import com.autodesk.pws.test.steps.base.*;

public class GetAssetDetails extends PwsServiceBase
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
    	super.setResourcePath("/license/v2/assets?serialNumbers=$SERIAL_NUMBER$");
    }

	@Override
    public void action()
    {
		super.action();
    }
}
