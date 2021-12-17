package com.autodesk.pws.test.steps.informative;

import com.autodesk.pws.test.steps.base.*;

public class WaitForGetAgreementInfo extends RestActionBase {
  private GetAgreementInfo getAgreementInfo = new GetAgreementInfo();

  @Override
  public void preparation() {
    getAgreementInfo.DataPool = this.DataPool;
    getAgreementInfo.preparation();
    // We want to turn auto retry off because we'll
    // be managing the retries ourselves...
    getAgreementInfo.EnableRetryOnNullResponse = false;
  }

  @Override
  public void action() {
    boolean continueTrying = true;
    boolean retryTimeout = false;
    Integer maxRetries = 60;
    Integer msSleepBeforeStatus = 120000;
    Integer retryCounter = 0;
    Integer flagForDelaysAt = 25;
    String status = "Waiting for service syncing and a non-zero length reply...";

    log("Current status: " + status);

    while (continueTrying) {
      sleep(msSleepBeforeStatus);

      retryCounter += 1;

      if (retryCounter >= maxRetries) {
        continueTrying = false;
        retryTimeout = true;
        status = "Timed out waiting for a non-zero length reply!";
      } else {
        log("Attempt (" + retryCounter + ") of (" + maxRetries + ")...");

        getAgreementInfo.action();

        String json = getAgreementInfo.JsonResponseBody.trim();

        if (json.length() > 8) {
          continueTrying = false;
          status = json.length() + " character reply...";
        }

        log("WaitForGetAgreementInfo status: " + json.length() + " char reply...");
      }

      getAgreementInfo.SuppressLogging = true;

      if (retryCounter >= flagForDelaysAt) {
        // TODO: Create some way of reporting when waiting for the
        // OrderStatusToChange exceeds a reasonable amount of time...
      }
    }

    if (retryTimeout) {
      this.ExceptionAbortStatus = true;
      this.ExceptionMessage = status;
      this.logErr(status, ClassName, "action");
    }

    log("Final status: " + status);
  }

  @Override
  public void validation() {
    // We call this here to be sure that the final JsonResponseBody
    // is added to the ValidationChain...
    getAgreementInfo.validation();

    // Depending on what we're doing, we may need the serialnumber...
    getAgreementInfo.extractDataFromJsonAndAddToDataPool("$SUBSCRIPTION_REFERENCE_NUMBER$",
        "[0].ServiceContract[0].ServiceLevels[0].ContractLineItems[0].SerialNumber");
    getAgreementInfo.extractDataFromJsonAndAddToDataPool("$SERIAL_NUMBER$",
        "[0].ServiceContract[0].ServiceLevels[0].ContractLineItems[0].SerialNumber");
  }
}
