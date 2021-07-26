package com.autodesk.pws.test.steps.browser;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.autodesk.pws.test.steps.base.SeleniumActionBase;

public class SalesForceLogin extends SeleniumActionBase
{
    public WebElement Username() { return BrowserManager.findElement(By.cssSelector("[id='username']")); }
    public WebElement Password() { return BrowserManager.findElement(By.cssSelector("[id='password']")); }
    public WebElement LoginButton() { return BrowserManager.findElement(By.cssSelector("[id='Login']")); }

	@Override
    public void preparation()
    {
        super.preparation();
    	this.ClassName = this.getClass().getSimpleName();
    }

	@Override
	public void action()
    {
        try 
        {
			SetText(Username(), DataPool.get("SalesForceUserName").toString());
	        SetText(Password(), DataPool.get("SalesForcePassword").toString());
	        Click(LoginButton());		
        }
        catch (Exception e) 
        {
			super.setExceptionAbort(e, this.ClassName, "action");
		}
    }
}

