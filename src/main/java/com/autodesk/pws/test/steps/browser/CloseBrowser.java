package com.autodesk.pws.test.steps.browser;

import com.autodesk.pws.test.steps.base.SeleniumActionBase;

public class CloseBrowser extends SeleniumActionBase
{
	@Override
    public void preparation()
    {
        super.preparation();
    	this.ClassName = this.getClass().getSimpleName();
    }
	
	@Override
    public void action()
    {
        BrowserManager.close();
        BrowserManager.quit();
    }
}
