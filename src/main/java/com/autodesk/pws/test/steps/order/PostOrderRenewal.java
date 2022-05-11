package com.autodesk.pws.test.steps.order;

public class PostOrderRenewal extends PostOrder 
{
	public String ExpectedResponse = "OK";
	
	public PostOrderRenewal()
	{
		this.OrderInfoDataPoolLabel = "OrderInfoRenewal";
	}
	
	@Override
    public void preparation()
    {
		super.preparation();
		this.ClassName = this.getClass().getSimpleName();
		
		DataPool.add("$ORDER_ACTION$", "Renewal");
    }

	@Override
	public void validation()
	{
		super.validation();
		
		if(ActionResult.message() != ExpectedResponse)
		{
			this.ExceptionAbortStatus = true;
			this.ExceptionMessage = "Unexpected response from " + this.ClassName + "! -- Exepected '" + ExpectedResponse + "' vs. Actual '" + ActionResult.message() + "'...";   
		}
	}
}
