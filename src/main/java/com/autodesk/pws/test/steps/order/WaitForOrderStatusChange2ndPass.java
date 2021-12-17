package com.autodesk.pws.test.steps.order;

public class WaitForOrderStatusChange2ndPass extends WaitForOrderStatusChange
{
	@Override
    public void preparation()
    {
      super.preparation();
		  this.ClassName = this.getClass().getSimpleName();
		  expectedEndStateStatus = DataPool.getOrDefault(this.ClassName + ".expectedEndStateStatus", expectedEndStateStatus).toString();
		  getOrderStatus.ClassName = "GetOrderStatus2ndPass";
    }
}
