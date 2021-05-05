package com.autodesk.pws.test;

import com.autodesk.pws.test.engine.Kicker;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WpeTestAutomationApplication {
  Logger logger = LoggerFactory.getLogger(WpeTestAutomationApplication.class);

  public static void main(String[] args) {
		SpringApplication.run(WpeTestAutomationApplication.class, args);
	}

  /**
   * Main runner for the test case.
   * @param ctx
   * @return
   */
  @Bean
  public CommandLineRunner testKicker(ApplicationContext ctx) {
    return args -> {
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
