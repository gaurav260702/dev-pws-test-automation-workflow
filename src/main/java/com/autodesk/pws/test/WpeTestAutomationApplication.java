package com.autodesk.pws.test;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import com.autodesk.pws.test.engine.Kicker;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.FunctionConfiguration;
import com.amazonaws.services.lambda.model.ListFunctionsResult;
import com.amazonaws.services.lambda.model.ServiceException;
import java.util.Iterator;
import java.util.List;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;


import java.nio.charset.StandardCharsets;

@SpringBootApplication
public class WpeTestAutomationApplication {
    static Logger logger = LoggerFactory.getLogger(WpeTestAutomationApplication.class);

    public static void main(String[] args) {
		SpringApplication.run(WpeTestAutomationApplication.class, args);
        // logger.info("Info level log message");
        // logger.debug("Debug level log message");
        // logger.error("Error level log message");

        // ApplicationContext ctx = SpringApplication.run(WpeTestAutomationApplication.class, args);
        // String[] beanNames = ctx.getBeanDefinitionNames();
        // Arrays.sort(beanNames);
        // for (String beanName : beanNames) {
        // logger.info(beanName);
        // }
	}

  /**
   * Main runner for the test case.
   * @param ctx
   * @return
   */
  @Bean
  public CommandLineRunner testKicker(ApplicationContext ctx) {
    return args -> {
//      ListFunctionsResult functionResult = null;
//      AWSLambda awsLambda = AWSLambdaClientBuilder.standard()
//              //.withCredentials(new ProfileCredentialsProvider())
//              .withRegion(Regions.US_WEST_2).build();
//
//      functionResult = awsLambda.listFunctions();
//
//      List<FunctionConfiguration> list = functionResult.getFunctions();
//
//      for (Iterator iter = list.iterator(); iter.hasNext(); ) {
//        FunctionConfiguration config = (FunctionConfiguration)iter.next();
//
//        System.out.println("The function name is "+config.getFunctionName());
//        logger.info("The function name is "+config.getFunctionName());
//      }
      InvokeResult invokeResult = null;
      AWSLambda awsLambda = AWSLambdaClientBuilder.standard()
//              //.withCredentials(new ProfileCredentialsProvider())
//              .withRegion(Regions.US_WEST_2).build();

      String event = "{\n" +
              "  \"version\": \"0\",\n" +
              "  \"id\": \"f5b56bf6-dbaf-4072-80f5-64dc236a7500\",\n" +
              "  \"detail-type\": \"adsk.o2pcoop:productcatalog.update.PRODUCTCATALOG_CHANGED\",\n" +
              "  \"source\": \"urn:adsk.dpe.dbp:moniker:CPRDCTLG-C-UW2\",\n" +
              "  \"account\": \"793121732731\",\n" +
              "  \"time\": \"2023-06-07T22:23:30Z\",\n" +
              "  \"region\": \"us-west-2\",\n" +
              "  \"resources\": [],\n" +
              "  \"detail\": {\n" +
              "    \"specversion\": \"1.0\",\n" +
              "    \"id\": \"879f131f-bb19-4ee4-9cf2-18c6c6e2c278\",\n" +
              "    \"source\": \"urn:adsk.dpe.dbp:moniker:CPRDCTLG-C-UW2\",\n" +
              "    \"type\": \"adsk.o2pcoop:productcatalog.update.PRODUCTCATALOG_CHANGED\",\n" +
              "    \"datacontenttype\": \"application/json\",\n" +
              "    \"dataschema\": \"http://forge.autodesk.com/schemas/data-event-schema-v1.0.0.json\",\n" +
              "    \"subject\": \"urn:o2pcoop:dev:productcatalog\",\n" +
              "    \"time\": \"2023-12-08T14:42:04.976Z\",\n" +
              "    \"data\": {\n" +
              "      \"date\": \"2023-12-08\",\n" +
              "      \"priceRegion\": {\n" +
              "        \"code\": \"AO\",\n" +
              "        \"description\": \"US\"\n" +
              "      },\n" +
              "      \"countries\": [\n" +
              "        \"US\",\n" +
              "        \"CA\"\n" +
              "      ],\n" +
              "      \"salesChannels\": [\n" +
              "        {\n" +
              "          \"salesChannelType\": \"Agency\",\n" +
              "          \"salesPlatformCodes\": [\n" +
              "            \"PWS\"\n" +
              "          ]\n" +
              "        }\n" +
              "      ]\n" +
              "    }\n" +
              "  }\n" +
              "}";
      InvokeRequest invokeRequest = new InvokeRequest()
              .withFunctionName("pws-catalog-upd-notify-async-dev")
              .withPayload("{\n" +
                      " \"Hello \": \"Paris\",\n" +
                      " \"countryCode\": \"FR\"\n" +
                      "}");
      invokeResult = awsLambda.invoke(invokeRequest);

      String ans = new String(invokeResult.getPayload().array(), StandardCharsets.UTF_8);

      //write out the return value
      System.out.println(ans);
      logger.info("Response from lambda: " + ans);

      logger.info("Executing Kicker with args: " + Arrays.toString(args));
      Kicker kicker = new Kicker();
      kicker.kickIt(args);
    };
  }

  /* Example command line runner
   * shows how to load a property file and also list out all the beans that are loaded
   *
  @Bean
  public CommandLineRunner commandLineRunnerTest(ApplicationContext ctx) {
    return args -> {
      logger.info("Start printing beans");
      Map<String, Object> beansOfType = ctx.getBeansOfType(Object.class);
      beansOfType.forEach((s, o) -> logger.info("{} - {}", s, o.getClass()));
      logger.info("End printing beans");
      // Load a property file
      Properties properties = new Properties();
      try {
        File file = ResourceUtils.getFile("classpath:application.properties");
        InputStream in = new FileInputStream(file);
        properties.load(in);
      } catch (IOException e) {
        logger.error(e.getMessage());
      }
      logger.info(properties.toString());
    };
  }*/
}
