package com.autodesk.pws.test.steps.order;

public class PostOrderRenewal extends PostOrder 
{
	public PostOrderRenewal()
	{
		this.OrderInfoDataPoolLabel = "OrderInfoRenewal";
	}
	
	@Override
    public void preparation()
    {
		super.preparation();
		this.ClassName = this.getClass().getSimpleName();
    }

}
