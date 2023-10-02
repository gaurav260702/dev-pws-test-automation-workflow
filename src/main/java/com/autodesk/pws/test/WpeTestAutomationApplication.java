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

@SpringBootApplication
public class WpeTestAutomationApplication {
    static Logger logger = LoggerFactory.getLogger(WpeTestAutomationApplication.class);

    public static void main(String[] args) {
    try {
		  SpringApplication.run(WpeTestAutomationApplication.class, args);
     } catch (Exception e) {
        e.printStackTrace(); 
        System.exit(0);
        
      }
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
