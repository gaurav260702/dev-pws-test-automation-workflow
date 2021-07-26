package com.autodesk.pws.test.steps.browser;

import com.autodesk.pws.test.steps.base.SeleniumActionBase;

public class OpenBrowser extends SeleniumActionBase
{
    public String StartupPage; // { get; set; }
    public String StartupPageTitle; // { get; set; }

    @Override
    public void preparation()
    {
    	super.preparation();
    	this.ClassName = this.getClass().getSimpleName();
    }
    
    @Override
    public  void action()
    {
        StartChrome(StartupPage);
    }

    @Override
    public void validation()
    {
        //  TODO: Create better validation criteria for page landing...
        if(StartupPageTitle != null && StartupPageTitle.length() > 0)
        {
            if (BrowserManager.getTitle() != StartupPageTitle)
            {
            	String errMsg = "Browser page title does not match target page title! '" + BrowserManager.getTitle() + "' vs. '" + StartupPageTitle + "}'...";
                setExceptionAbort(errMsg, this.ClassName, "validation");
            }
        }
    }
}
