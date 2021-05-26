# Build stage
FROM maven:3.6.2-jdk-11-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package
RUN find /home/app -name "*.jar" 
RUN jar tvf /home/app/target/wpe-test-automation-0.0.1-SNAPSHOT.jar
RUN jar tvf /home/app/target/wpe-test-automation-0.0.1-SNAPSHOT-stubs.jar

# Package stage
FROM openjdk:11-jre-slim
COPY --from=build /home/app/target/wpe-test-automation-0.0.1-SNAPSHOT.jar /usr/local/lib/wpe-test-automation-0.0.1-SNAPSHOT.jar
COPY --from=build /home/app/target/wpe-test-automation-0.0.1-SNAPSHOT-stubs.jar /usr/local/lib/wpe-test-automation-0.0.1-SNAPSHOT-stubs.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/local/lib/wpe-test-automation-0.0.1-SNAPSHOT.jar"]
