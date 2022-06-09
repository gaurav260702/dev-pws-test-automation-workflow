package com.autodesk.pws.test.steps.quote;

public class QuoteStatusByQuoteNumber extends QuoteStatus
{
  public QuoteStatusByQuoteNumber(boolean useLoopTillExpectedStatus) 
  {
	  this.LoopTillExpectedStatus = useLoopTillExpectedStatus;

	  if(LoopTillExpectedStatus)
	  {
		  this.ExpectedEndStateStatus = "QUOTED";
	  }
  }

@Override
  public void preparation() 
  {
    super.preparation();
    this.ClassName = this.getClass().getSimpleName();
    
    setResourcePath();
    
    setTargetUrl();
  }
  
  private void setResourcePath()
  {
	  super.setResourcePath("/v1/quotes/status?quoteNumber=$QUOTE_NUMBER$");
  }
}
