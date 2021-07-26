package com.autodesk.pws.test.steps.browser;

import com.autodesk.pws.test.steps.base.SeleniumActionBase;
import com.autodesk.pws.test.utilities.StringUtils;

public class CreateRenewalOpportunityFromSalesForceId extends SeleniumActionBase
{
    public String SalesForceRedirectedUrl() { return DataPool.get("salesForceRedirectedUrl").toString(); }
    public String SalesForceId() { return DataPool.get("SalesForceId").toString(); }
    public String SalesForceCreateRenewalPath() { return "apex/ROM_ManualCreateRenewalButton?agreementId=" + SalesForceId(); }

	@Override
    public void preparation()
    {
        super.preparation();
    	this.ClassName = this.getClass().getSimpleName();
    }
	
    @Override
    public  void action()
    {
        String targetUrl = SalesForceRedirectedUrl() + "/" + SalesForceCreateRenewalPath();
        
        NavigateToPage(targetUrl);

        String opportunityId = BrowserManager.getPageSource();
        
        opportunityId = StringUtils.getLeft(opportunityId, "<td class=\"labelCol\">Opportunity Name</td>");
		opportunityId = StringUtils.getRight(opportunityId, "<td class=\"labelCol\">Opportunity Number</td>");
		opportunityId = StringUtils.getRight(opportunityId, "<div id=\"");
		opportunityId = StringUtils.getBetween(opportunityId, "\">", "<");
		
		opportunityId = opportunityId.trim();

        ExtraIntoDataPool("RenewalOpportunityId", opportunityId);
    }
}
