package com.autodesk.pws.test.steps.order;

import com.autodesk.pws.test.steps.base.*;

public class GetOrderStatus extends PwsServiceBase
{

//	@Override
//    public void Preparation()
//    {
//        // ExceptionOnRetriesExceededError = bool.Parse((string)DataPool.GetValue("GetOrderStatusV2.ExceptionOnRetriesExceededError", caseSensitive: false, defaultValue: true).ToString());
//        
//        SetExceptionOnRetriesExceededErrorFlag(GetType().Name);
//
//        super.preparation();
//    }
//
//	@@Override
//    public void Action()
//    {
//        var getOrderStatusV2Response = GetStatus();
//
//        AddValidationChainLink("GetOrderStatusV2", getOrderStatusV2Response);
//    }
//
//    public dynamic GetStatus()
//    {
//        String targetUrl = $"{DataPool["getOrderStatusV2Url"]}/v2/status/{DataPool["TransactionId"]}?detailed=true";
//        var retVal = DoTheRequest(targetUrl, Method.GET);
//
//        return retVal;
//    }
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
		super.setResourcePath("/v2/orderstatus/$TRANSACTION_ID$?detailed=true");
    }

	@Override
    public void action()
    {
		super.action();
    }
}
