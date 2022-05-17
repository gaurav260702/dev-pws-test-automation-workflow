package com.autodesk.pws.test.steps.quote;

public class QuoteStatusByQuoteNumber extends QuoteStatus
{
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
