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
    
    //  Clamp down on the default retry count...
    this.MaxRetryLoopCount = 12;
    
    setResourcePath();
    
    setTargetUrl();

    if(LoopTillExpectedStatus)
    {
    	this.ExpectedEndStateStatus = "QUOTED";
    }
    
    setExpectedEndState(this.ClassName);
  }
  
@Override
  public void validation()
  {
	  //  We *DO NOT* want to execute the
	  //  code found in the superclass!
	  //  -- Thanks @Shailesh Pathade  for finding this bug!
	
	 addResponseToValidationChain();
  }
	

  private void setResourcePath()
  {
	  super.setResourcePath("/v1/quotes/status?quoteNumber=$QUOTE_NUMBER$");
  }
}
