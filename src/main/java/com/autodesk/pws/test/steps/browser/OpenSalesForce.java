package com.autodesk.pws.test.steps.browser;

public class OpenSalesForce extends OpenBrowser
{
	@Override
    public void preparation()
    {
        StartupPage = DataPool.get("salesForceUrl").toString();
        super.preparation();
    	this.ClassName = this.getClass().getSimpleName();
    }
}