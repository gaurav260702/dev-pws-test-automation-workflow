package com.autodesk.pws.test.steps.webhook;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.PropertiesFileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.autodesk.pws.test.steps.base.PwsServiceBase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilterLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilterLogEventsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilteredLogEvent;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResultEntry;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class InvokeCatalogWebhook extends PwsServiceBase
{
    @Override
    public void preparation()
    {
        //  Do some basic variable preparation...

        //  Need to set the ClassName here as this will be
        //  used by the super/base classes ".preparation()"
        //  method.
        this.ClassName = this.getClass().getSimpleName();
        //  Set the Resource path BEFORE the base/super class
        //  sets the targetUrl..
        setResourcePath();
        //  Do stuff that the Action depends on to execute...
        super.preparation();
    }

    private void setResourcePath()
    {

    }

    @Override
    public void action()
    {


        String env = (DataPool.get("$ENV$").toString()).toLowerCase();
        System.out.println("env is "+ env);

        //Step 1: Invoking catalog update change event
        AWSCredentialsProvider credentialsProvider = new ProfileCredentialsProvider("default");

        // Generate current epoch timestamp
        long currentTimestamp = Instant.now().getEpochSecond();


        //Step 2: Invoking catalog update change event
        String eventId = UUID.randomUUID().toString();
        System.out.println("Invoking event with id:" + eventId);

        String json = "{\n  \"specversion\": \"1.0\",\n  \"id\": \""+eventId+"\",\n  \"source\": \"urn:adsk.dpe.dbp:moniker:CPRDCTLG-S-UE1\",\n  \"type\": \"adsk.o2pcoop:productcatalog.update.PRODUCTCATALOG_CHANGED\",\n  \"datacontenttype\": \"application/json\",\n  \"dataschema\": \" http://forge.autodesk.com/schemas/data-event-schema-v1.0.0.json\",\n  \"subject\": \"urn:o2pcoop:int-stg:productcatalog\",\n  \"time\": \"2023-07-06T21:20:55.185Z\",\n  \"data\": {\n      \"date\": \"2023-08-06\",\n      \"priceRegion\": {\n          \"code\": \"AH\",\n          \"description\": \"Australia\"\n      },\n      \"countries\": [\n          \"AU\",\n          \"CX\",\n          \"CC\",\n          \"FJ\",\n          \"MH\",\n          \"NR\",\n          \"PG\",\n          \"NU\",\n          \"NF\",\n          \"PW\",\n          \"PN\",\n          \"WS\",\n          \"SB\",\n          \"TO\",\n          \"TV\",\n          \"VU\",\n          \"NZ\"\n      ],\n      \"salesChannels\": [\n          {\n              \"salesChannelType\": \"Agency\",\n              \"salesPlatformCodes\": [\n                  \"PWS\"\n              ]\n          }\n      ]\n  },\n  \"traceparent\": \"00-59bd75a3549778672ae2a182de09a0a3-dc38e64bf685b1c8-01\"\n}\n";

        Region region = Region.US_WEST_2;
        EventBridgeClient eventBridgeClient = EventBridgeClient
                .builder()
                .region(region)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        // Create an EventBridge event with a payload
        PutEventsRequest putEventsRequest = PutEventsRequest.builder()
                .entries(
                        PutEventsRequestEntry.builder()
                                .source("urn:adsk.dpe.dbp:moniker:CPRDCTLG-S-UE1")
                                .detailType("adsk.o2pcoop:productcatalog.update.PRODUCTCATALOG_CHANGED")
                                .detail(json) // Your payload here
                                .eventBusName("pws-catalog-upd-subscriber")
                                .build()
                )
                .build();
        // Publish the event to EventBridge
        PutEventsResponse result = eventBridgeClient.putEvents(putEventsRequest);
        System.out.println(result);
        for (PutEventsResultEntry resultEntry: result.entries()) {
            if (resultEntry.eventId() != null) {
                System.out.println("Event Id: " + resultEntry.eventId());
            } else {
                System.out.println("PutEvents failed with Error Code: " + resultEntry.errorCode());
            }
        }
        System.out.println("Catalog Event published successfully");

        //Step 3: Reading cloudwatch logs
        System.out.println("Starting to reading catalog Cloudwatch logs every 1min ");
        String logGroupName = "/aws/lambda/pws-catalog-upd-notify-async-"+env;
        CloudWatchLogsClient cloudWatchLogsClient = CloudWatchLogsClient.builder()
                .region(Region.US_WEST_2)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        // Query CloudWatch Logs for recent log events
        //long startTimeMillis = System.currentTimeMillis() - 2*3600000; // 1 hour ago

        // 5min ago
        long timeToSleepFor = 300000;
        //Thread.sleep(timeToSleepFor);
        long startTimeMillis = System.currentTimeMillis() - timeToSleepFor; // 1 hour ago
        long endTimeMillis = System.currentTimeMillis();

        int count =1;
        boolean isMatchFound = false;
        while (count<=5) {
            try {
                //Read lambda logs every 1 min
                Thread.sleep(60000);
                System.out.println("Reading Cloudwatch logs for last: "+ count+ " min");

                //For 1st cycle,read logs for last 2min
                //For 2nt cycle,read logs for last 3min

                FilterLogEventsRequest logEventsRequest = FilterLogEventsRequest.builder()
                        .logGroupName(logGroupName)
                        .startTime(System.currentTimeMillis() - ((count+1)*60*1000))
                        .endTime(System.currentTimeMillis())
                        .build();

                FilterLogEventsResponse logEventsResponse = cloudWatchLogsClient.filterLogEvents(logEventsRequest);

                // Print log events
                List<FilteredLogEvent> logEvents = logEventsResponse.events();
                for (FilteredLogEvent logEvent : logEvents) {
                    //System.out.println("Log Event Timestamp: " + logEvent.timestamp());
                    //logger.info("Log group length " + logEvents.size());
                    //logger.info("Executing Kicker with args: " + Arrays.toString(args));


                    String logMessage = logEvent.message();
                    //System.out.println("Log Event Message: " + logEvent.message());
                    if(logMessage.contains("Response from Notification service\"")) {
                        System.out.println("Found notification response");
                        //System.out.println(logMessage);

                        String startWord = "\"response\":";
                        String endWord = ",\"exception\"";


                        int startIndex = logMessage.indexOf(startWord);
                        int endIndex = logMessage.indexOf(endWord);
                        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                            // Get the string after the search word
                            String notificationResponse = logMessage.substring(startIndex + startWord.length(), endIndex).trim();
                            // System.out.println("notification Response::" + notificationResponse);
                            ObjectMapper notificationResponseMapper = new ObjectMapper();

                            // Parse the JSON string
                            //System.out.println("Current notification Api response"+notificationResponse);
                            try{
                                JsonNode jsonNode = notificationResponseMapper.readTree(notificationResponse);
                                // Get the value of a specific property
                                String status = jsonNode.get("statusCode").asText();
                                String id = jsonNode.get("data").get("id").asText();
                                // System.out.println("Id Found: " + id);
                                // System.out.println("Status Found: " + status);

                                if(Objects.equals(id, eventId) && Integer.valueOf(status) == 202 ){
                                    //System.out.println("Test case passed in "+count+ "iteration");
                                    System.out.println("Log for mapped event");
                                    System.out.println(notificationResponse);
                                    this.JsonResponseBody = "{ \"status\":\"202\"}";
                                    isMatchFound = true;
                                    break;
                                }

                            }
                            catch (Exception e){
                                e.printStackTrace();
                            }

                        }
                    }
                    //System.out.println(logMessage);
                    //System.out.println();
                }
                count++;
                if(isMatchFound){
                    break;
                }
                if(count == 6){
                    System.out.println("Test case failed");
                    break;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("Test case failed");
                break;
            }
        }


        // Close the clients
        //lambdaClient.close();
        cloudWatchLogsClient.close();

        this.log("-- RESPONSE BODY --", DEFAULT_LEFT_SPACE_PADDING + 4);
        this.log(this.JsonResponseBody, DEFAULT_LEFT_SPACE_PADDING + 8);
    }

    @Override
    public void validation()
    {
        super.validation();
    }
}