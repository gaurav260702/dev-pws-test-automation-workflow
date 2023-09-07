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

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class InvokeWebhook extends PwsServiceBase
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
        super.setResourcePath("/v1/catalog/export");
    }

    @Override
    public void action()
    {


        String env = "int";
        //Step 1: Making dynamodb entry
        AWSCredentialsProvider credentialsProvider = new ProfileCredentialsProvider("default");
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                //.withCredentials(credentialsProvider)
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .withRegion("us-east-1")
                .build();
        DynamoDB dynamoDB = new DynamoDB(client);
        Table table = dynamoDB.getTable("pws-quote-status-"+env);

        // Generate current epoch timestamp
        long currentTimestamp = Instant.now().getEpochSecond();

        // Generate a random UUID
        String uuid = UUID.randomUUID().toString();

        // Create a Unix timestamp for 24 hours from now
        long ttl = Instant.now().plusSeconds(24 * 60 * 60).getEpochSecond();
        String quoteNumber = "Q-"+Long.toString(currentTimestamp);

        // Build the item
        Item item = new Item()
                .withPrimaryKey("transactionId", uuid)
                .withString("quoteNumber", quoteNumber)
                .withNumber("ttl", ttl);

        try {
            // Put the item into the DynamoDB table
            System.out.println("ttl is"+ ttl);
            System.out.println("quotenumber is"+ quoteNumber);
            System.out.println("transactionId is"+ uuid);
            //client.putItem(request);
            PutItemOutcome outcome = table.putItem(item);
            System.out.println("Written to status db");
            System.out.println(outcome);
            System.out.println("Item added to DynamoDB table successfully.");
        } catch (Exception e) {
            System.err.println("Error adding item to DynamoDB: " + e.getMessage());
        }




        //Step 2: Invoking quote status change event
        String eventId = UUID.randomUUID().toString();
        System.out.println("Invoking event with id:" + eventId + " for quote number: "+quoteNumber);
        String json = "{\n" +
                "  \"id\": \" "+eventId+"\",\n" +
                "  \"source\": \"urn:adsk.dpe.crm:moniker:749E33AC-C-UE1\",\n" +
                "  \"specversion\": \"1.0\",\n" +
                "  \"type\": \"adsk.cip:quote.created-updated-1.0.0\",\n" +
                "  \"subject\": \"Event triggered for Quote(Quote Number) Q-03094\",\n" +
                "  \"time\": \"2022-04-29T14:01:46.394703+00:00\",\n" +
                "  \"dataschema\": \"https://platform.stoplight.autodesk.com/docs/salesforceintegrations/event-specs/QuoteChangeEvent.json/components/schemas/QuoteEvent\",\n" +
                "  \"tracebaggage\": \"463ac35c9f6413ad48485a3953bb6124\",\n" +
                "  \"traceparent\": \"\",\n" +
                "  \"tracestate\": \"\",\n" +
                "  \"sequence\": \"\",\n" +
                "  \"data\": {\n" +
                "    \"commitUser\": \"00563000004xnhIAAQ\",\n" +
                "    \"changeType\": \"UPDATE\",\n" +
                "    \"targetSystems\": [\n" +
                "      \"QuoteChangeSubscribers\"\n" +
                "    ],\n" +
                "    \"changedFields\": [\n" +
                "      \"SBQQ__LastCalculatedOn__c\"\n" +
                "    ],\n" +
                "    \"quote\": {\n" +
                "      \"agreementTypeCode\": \"Agent\",\n" +
                "      \"salesChannelType\": \"Agency\",\n" +
                "      \"salesPlatformCode\": \"PWS\",\n" +
                "      \"quoteNumber\": \""+quoteNumber+"\",\n" +
                "      \"salesOpportunityNumber\": \"A-12950975\",\n" +
                "      \"quoteCreatedDate\": \"2022-04-29T13:56:34.000+0000\",\n" +
                "      \"quoteExpirationDate\": \"2022-05-20\",\n" +
                "      \"subscriptionStartDate\": \"2022-05-20\",\n" +
                "      \"quoteStatus\": \"Ordered\",\n" +
                "      \"pricing\": {\n" +
                "        \"totalListAmount\": 105000,\n" +
                "        \"totalNetAmount\": 73500,\n" +
                "        \"totalAmount\": 73500,\n" +
                "        \"currency\": \"USD\"\n" +
                "      },\n" +
                "      \"agentAccount\": {\n" +
                "        \"accountCsn\": \"0000000000_sample_csn\",\n" +
                "        \"name\": \"RESn-Tech Data Prod MGT Commercial VAR\",\n" +
                "        \"addressLine1\": \"5350 Tech Data Dr\",\n" +
                "        \"city\": \"Daly City\",\n" +
                "        \"stateProvinceCode\": \"CA\",\n" +
                "        \"postalCode\": \"94014\",\n" +
                "        \"countryCode\": \"US\"\n" +
                "      },\n" +
                "      \"agentContact\": {\n" +
                "        \"contactCsn\": \"168152752\",\n" +
                "        \"email\": \"jamesjackson@gmail.com\",\n" +
                "        \"firstName\": \"James\",\n" +
                "        \"lastName\": \"Jackson\",\n" +
                "        \"preferredLanguage\": \"ENU\"\n" +
                "      },\n" +
                "      \"endCustomer\": {\n" +
                "        \"accountCsn\": \"5500081753\",\n" +
                "        \"name\": \"State Inc\",\n" +
                "        \"addressLine1\": \"1 South State Street\",\n" +
                "        \"city\": \"Chicago\",\n" +
                "        \"stateProvinceCode\": \"IL\",\n" +
                "        \"postalCode\": \"60603\",\n" +
                "        \"countryCode\": \"US\"\n" +
                "      },\n" +
                "      \"purchaser\": {\n" +
                "        \"contactCsn\": \"168152752\",\n" +
                "        \"email\": \"jamesjackson@gmail.com\",\n" +
                "        \"firstName\": \"James\",\n" +
                "        \"lastName\": \"Jackson\",\n" +
                "        \"preferredLanguage\": \"ENU\"\n" +
                "      },\n" +
                "      \"lineItems\": [\n" +
                "        {\n" +
                "          \"lineNumber\": 1,\n" +
                "          \"offeringId\": \"OD-004694\",\n" +
                "          \"offeringCode\": \"FLEXACCESS\",\n" +
                "          \"offeringName\": \"Flex Access\",\n" +
                "          \"quantity\": 100,\n" +
                "          \"orderAction\": \"New\",\n" +
                "          \"subscriptionStartDate\": \"2022-05-20\",\n" +
                "          \"subscriptionEndDate\": \"2023-05-19\",\n" +
                "          \"pricing\": {\n" +
                "            \"unitSRP\": 1050,\n" +
                "            \"extendedSRP\": 105000,\n" +
                "            \"transactionalVolumeDiscount\": 30,\n" +
                "            \"transactionalVolumeDiscountAmount\": 31500,\n" +
                "            \"extendedDiscountedSRP\": 73500,\n" +
                "            \"endUserPrice\": 73500\n" +
                "          },\n" +
                "          \"offer\": {\n" +
                "            \"term\": {\n" +
                "              \"code\": \"A01\",\n" +
                "              \"description\": \"Annual\"\n" +
                "            },\n" +
                "            \"accessModel\": {\n" +
                "              \"code\": \"F\",\n" +
                "              \"description\": \"Flex\"\n" +
                "            },\n" +
                "            \"intendedUsage\": {\n" +
                "              \"code\": \"COM\",\n" +
                "              \"description\": \"Commercial\"\n" +
                "            },\n" +
                "            \"connectivity\": {\n" +
                "              \"code\": \"C100\",\n" +
                "              \"description\": \"Online\"\n" +
                "            },\n" +
                "            \"connectivityInterval\": {\n" +
                "              \"code\": \"C02\",\n" +
                "              \"description\": \"365 Day\"\n" +
                "            },\n" +
                "            \"servicePlanId\": {\n" +
                "              \"code\": \"STND\",\n" +
                "              \"description\": \"Standard\"\n" +
                "            },\n" +
                "            \"billingBehavior\": {\n" +
                "              \"code\": \"A300\",\n" +
                "              \"description\": \"Once\"\n" +
                "            },\n" +
                "            \"billingType\": {\n" +
                "              \"code\": \"B100\",\n" +
                "              \"description\": \"Up-front\"\n" +
                "            },\n" +
                "            \"billingFrequency\": {\n" +
                "              \"code\": \"B01\",\n" +
                "              \"description\": \"One-Time\"\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}";




        Region region = Region.US_EAST_1;
        EventBridgeClient eventBridgeClient = EventBridgeClient
                .builder()
                .region(region)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        // Create an EventBridge event with a payload
        PutEventsRequest putEventsRequest = PutEventsRequest.builder()
                .entries(
                        PutEventsRequestEntry.builder()
                                .source("urn:adsk.dpe.crm:moniker:749E33AC-C-UE1\"")
                                .detailType("adsk.cip:quote.created-updated-1.0.0")
                                .detail(json) // Your payload here
                                .eventBusName("pws-cpq-quote-upd-notify-subscriber")
                                .build()
                )
                .build();
        // Publish the event to EventBridge
        eventBridgeClient.putEvents(putEventsRequest);
        System.out.println("Event published successfully");

        //Step 3: Reading cloudwatch logs
        String logGroupName = "/aws/lambda/pws-cpq-quote-upd-notify-async-"+env;
        CloudWatchLogsClient cloudWatchLogsClient = CloudWatchLogsClient.builder()
                .region(Region.US_EAST_1)
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
        boolean isNotificationAPIInvokedWithSuccessResponse = false;
        while (count<=5) {
            try {
                //Read lambda logs every 1 min
                Thread.sleep(60000);

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
                    if(logMessage.contains("Response from Notification service")) {
                        System.out.println("Found notification response");
                        //System.out.println(logMessage);

                        String startWord = "\"response\":";
                        String endWord = ",\"exception\"";


                        int startIndex = logMessage.indexOf(startWord);
                        int endIndex = logMessage.indexOf(endWord);
                        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                            // Get the string after the search word
                            String notificationResponse = logMessage.substring(startIndex + startWord.length(), endIndex).trim();
                            System.out.println("notificationResponse::" + notificationResponse);
                            ;          ObjectMapper notificationResponseMapper = new ObjectMapper();

                            // Parse the JSON string
                            try{
                                JsonNode jsonNode = notificationResponseMapper.readTree(notificationResponse);
                                // Get the value of a specific property
                                String status = jsonNode.get("status").asText();
                                String id = jsonNode.get("data").get("id").asText();
                                System.out.println("Id Found: " + id);
                                System.out.println("Status Found: " + status);

                                if(Objects.equals(id, eventId) && Integer.valueOf(status) == 202 ){
                                    //System.out.println("Test case passed in "+count+ "iteration");
                                    isNotificationAPIInvokedWithSuccessResponse = true;
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
                if(isNotificationAPIInvokedWithSuccessResponse) {
                    System.out.println("Test case passed in "+count+ "iteration");
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

        if(isNotificationAPIInvokedWithSuccessResponse){
            this.JsonResponseBody = "{}";
        }

        this.log("-- RESPONSE BODY --", DEFAULT_LEFT_SPACE_PADDING + 4);
        this.log(this.JsonResponseBody, DEFAULT_LEFT_SPACE_PADDING + 8);
    }

    @Override
    public void validation()
    {
        super.validation();
    }
}