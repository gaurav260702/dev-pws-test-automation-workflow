# Build stage
FROM maven:3.6.2-jdk-11-slim AS build

ENV JQ_VERSION=1.5
ENV JQ_SHA2256SUM=c6b3a7d7d3e7b70c6f51b706a3b90bd01833846c54d32ca32f0027f00226ff6d

CMD  mvn --version
COPY src /home/app/src
COPY pom.xml /home/app
COPY aws_auth /home/app/aws_auth

RUN curl -SL https://github.com/stedolan/jq/releases/download/jq-$JQ_VERSION/jq-linux64 > \
	jq_${JQ_VERSION} && \
    echo "${JQ_SHA2256SUM}  jq_${JQ_VERSION}" > jq_${JQ_VERSION}_SHA256SUMS && \
    sha256sum -c --status jq_${JQ_VERSION}_SHA256SUMS && \
    mv jq_${JQ_VERSION} /bin/jq && \
    chmod +x /bin/jq
    
RUN  mkdir -p /home/app/src/test/resources/contracts
RUN  mvn -f /home/app/pom.xml clean package
WORKDIR /home/app
ENTRYPOINT []
CMD ["bash"]