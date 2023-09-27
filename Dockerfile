# Build stage
FROM maven:3.6.2-jdk-11-slim AS build
RUN apt-get update && apt-get install -y jq && rm -rf /var/lib/apt/lists/*
CMD  mvn --version
COPY src /home/app/src
COPY pom.xml /home/app
COPY aws_auth /home/app/aws_auth
RUN  mkdir -p /root/.aws
RUN  mkdir -p /home/app/src/test/resources/contracts
RUN  mvn -f /home/app/pom.xml clean package
WORKDIR /home/app
ENTRYPOINT []
CMD ["bash"]
