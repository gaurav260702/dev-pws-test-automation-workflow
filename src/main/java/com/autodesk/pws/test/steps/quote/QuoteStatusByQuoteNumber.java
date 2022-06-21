package com.autodesk.pws.test.steps.quote;

public class QuoteStatusByQuoteNumber extends QuoteStatus
{
  public QuoteStatusByQuoteNumber(boolean useLoopTillExpectedStatus) 
  {
	  this.LoopTillExpectedStatus = useLoopTillExpectedStatus;
  }

@Override
  public void preparation() 
  {
    super.preparation();
    this.ClassName = this.getClass().getSimpleName();
    
    setResourcePath();
    
    setTargetUrl();

    if(LoopTillExpectedStatus)
    {
    	this.ExpectedEndStateStatus = "QUOTED";
    }
    
    setExpectedEndState(this.ClassName);
  }
  
  private void setResourcePath()
  {
	  super.setResourcePath("/v1/quotes/status?quoteNumber=$QUOTE_NUMBER$");
  }
}
