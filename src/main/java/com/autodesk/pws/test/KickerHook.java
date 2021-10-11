package com.autodesk.pws.test;

import com.autodesk.pws.test.engine.Kicker;

public class KickerHook 
{
	public static void main(String[] args)
	{
        // args[0] = "./testdata/WorkflowProcessing/TestKickers/Kicker.GetInvoiceDetails.PreCannedData.INT.json" ;
		Kicker kicker = new Kicker();
		kicker.kickIt(args);
	}
}

