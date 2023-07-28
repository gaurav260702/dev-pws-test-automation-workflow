package com.autodesk.pws.test.steps.authentication;

 import okhttp3.*;
 import com.autodesk.pws.test.steps.base.*;

 public class GetQuoteDetailsInternalv2ForgeCredentials extends RestActionBase {
     protected String accessTokenToExtract = "access_token:access_token";

     @Override
     public void preparation()
     {      
         this.initVariables();
     }

     private void initVariables()
     {
         this.ClassName = this.getClass().getSimpleName();
         pullDataPoolVariables();
     }

     private void pullDataPoolVariables()
     {
         clientId = DataPool.get("clientId").toString();
         clientSecret = DataPool.get("clientSecret").toString();
         BaseUrl = DataPool.get("oAuthBaseUrl").toString();

         DataPool.add("$X-API-KEY$", DataPool.get("x-api-key").toString());
     }

     @Override
     public void action()
     {
         //  Prep a container for the response body (if any)...
         String rawJson = "";

         try
         {
             //  Call the method that does the meat of the work...
             Response actionResult = getInfo();

             rawJson = actionResult.body().string();
         }
         catch (Exception e)
         {
             this.logErr(e, this.ClassName, "action");
         }

         //  Stick that response body in the ValidationChain...
         this.addValidationChainLink(this.ClassName, rawJson);

         //  Here we would extract any data that needs
         //  to be promoted in the DataPool...
         extractDataFromJsonIntoDataPool(rawJson, accessTokenToExtract);
     }

     public Response getInfo()
     {
         //  String rawJsonBody = "";

         //  Get the appropriate headers for a token request...
         this.RequestHeaders = generateAccessTokenHeaders();

         //  Ready a reponse container...
         Response response = null;

         String grant_type = "client_credentials";
         String requestBody = "grant_type=" + grant_type + "&client_id=" + clientId + "&client_secret=" + clientSecret;
         try
         {
             log("Creating request: ");
             //  Make the call to the oAuth service...
             //oAuthResponse = getRestResponse("POST", BaseUrl + "/authentication/v1/authenticate", requestBody, "application/x-www-form-urlencoded");
             OkHttpClient client = new OkHttpClient().newBuilder()
                     .build();
             MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
             RequestBody body = RequestBody.create(mediaType, requestBody);
             Request request = new Request.Builder()
                     .url("https://developer-stg.api.autodesk.com/authentication/v1/authenticate")
                     .method("POST", body)
                     .addHeader("Content-Type", "application/x-www-form-urlencoded")
                     .addHeader("Cookie", "PF=mps66M0oG87MgPzyr9QqOT")
                     .build();
              response = client.newCall(request).execute();
         }
         catch (Exception e)
         {
             //  Uh-oh.  Now what happened?
             logErr(e, this.ClassName, "getInfo");
         }

         log("oAuth Response: "+response);
         return response;
     }
}