# PWS Test Automation using Workflow Processing Engine

Documentation and other sundries: 
* https://wiki.autodesk.com/pages/viewpage.action?spaceKey=PWSEng&title=PWS+-+Workflow+Processing+Engine+-+Documentation+and+Discussion

![Workflow Processing Engine Execution Diagram](https://wiki.autodesk.com/download/attachments/867567388/WorkflowProcessingEngine.png?version=2&modificationDate=1607721171186&api=v2&effects=border-simple,shadow-kn)

### Overview
This project creates the pws automation container based on the [workflow processing engine](https://wiki.autodesk.com/display/PWSEng/PWS+Tech+Info+-+Java+based+Workflow+Engine+for+Test+Framework).
It uses spring-boot framework and is a commandline application.

### Test Data and config
It is assumed that the testdata in resources folder will be attached to this container externally 
and managed as another project. 
To test locally you can add files there. 

Please note that the `./src/main/resources/testdata` folder is ignored in git as well as overwritten/erased when deployed.
Please do not change that and it is as designed. 

### Prerequisites
* JDK 8 or higher and available in path
* Maven 2.3 or higher and available in path

### Compile and package
```
mvn clean package
```

### Execute a test
See note above about test data.
```
mvn spring-boot:run -Dspring-boot.run.arguments="./testdata/WorkflowProcessing/TestKickers/Kicker.GetInvoiceList.PreCannedData.WithAuth.DateRangeTooBig.INT.json"
```

### Spring Boot and Maven Reference Documentation
For further reference on some of the modules used on the project:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.4.5/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.4.5/maven-plugin/reference/html/#build-image)
* [Mustache](https://docs.spring.io/spring-boot/docs/2.4.5/reference/htmlsingle/#boot-features-spring-mvc-template-engines)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/docs/2.4.5/reference/htmlsingle/#using-boot-devtools)

### Spring modules in use guides
The following guides illustrate how to use some features concretely:

* [Spring Cloud Contract Verifier Setup](https://cloud.spring.io/spring-cloud-contract/spring-cloud-contract.html#_spring_cloud_contract_verifier_setup)

