package com.autodesk.pws.test.steps.base;

import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;

public class SeleniumActionBase extends StepBase
{
    public WebDriver BrowserManager;
    
//    {
//        get
//        {
//            return (IWebDriver)ActionManager;
//        }
//        private set
//        {
//            ActionManager = value;
//        }
//    }

    public void SetActionManager(WebDriver actionManager)
    {
        BrowserManager = actionManager;
    }

    public void StartChrome(String url)
    {
    	StartChrome(url, true);
    }
    
    public void StartChrome(String url, boolean minimizeOnStart)
    {
        log("Starting Chrome browser at '" + url +"'...");
        
        BrowserManager = new ChromeDriver();

        if (minimizeOnStart)
        {
        	//  The following doesn't work in Java
        	//  implementation of Selenium...
            //BrowserManager.manage().window().minimize();
        	
            //  Workaround for minimizing browser window...
            BrowserManager.manage().window().setPosition(new Point(-2000, 0));
        }

        NavigateToPage(url);
    }

    public void NavigateToPage(String url)
    {
        log("Navigating to '" + url + "'...");
        
    	//  The following doesn't work in Java
    	//  implementation of Selenium...
        //BrowserManager.Url = url;
        
        BrowserManager.get(url);
    }

    public void Focus(WebElement element) throws Exception
    {
        {
            try
            {
                log("Focusing on '" + element.getTagName() + "'...");
                var focusAction = new Actions(BrowserManager);
                focusAction.moveToElement(element);
            }
            catch (Exception ex)
            {
                String errMsg = "Unable to focus on element!  Error: " + ex.getMessage();
                log(errMsg);
                throw new Exception(errMsg, ex);
            }
        }
    }

    public void Click(WebElement element) throws Exception
    {
        try
        {
            log("Clicking '" + element.getTagName() + "'...");
            element.click();
        }
        catch (Exception ex)
        {
            String errMsg = "Unable to click element!  Error: " + ex.getMessage();
            log(errMsg);
            throw new Exception(errMsg, ex);
        }
    }

    public void SetText(WebElement element, String text) throws Exception
    {
        try
        {
            log("Setting text '" + element.getTagName() + "' to '{text}'...");
            element.click();
            element.sendKeys(text);
        }
        catch (Exception ex)
        {
            String errMsg = "Unable to set text element!  Error: {ex.Message}";
            log(errMsg);
            throw new Exception(errMsg, ex);
        }
    }

    public void ExtraIntoDataPool(String targetDataLabel, String targetDataValue)
    {
        String displayValue = targetDataValue.substring(0, Math.min(targetDataValue.length(), 50));
        log("Extracting '{targetDataLabel}' with value of '" + displayValue + "'...");
        DataPool.add(targetDataLabel, targetDataValue);
    }

	public void setExceptionAbort(String exceptionMsg) 
	{
		this.ExceptionAbortStatus = true;
		this.ExceptionMessage = exceptionMsg;	
	}
	
	public void setExceptionAbort(Exception e, String className, String methodName) 
	{
		logErr(e, className, methodName);
		setExceptionAbort(e.getMessage());
	}
	
	public void setExceptionAbort(String exceptionMsg, String className, String methodName) 
	{
	 	Exception e = new Exception(exceptionMsg);
	 	setExceptionAbort(e, className, methodName);
	}
	
}