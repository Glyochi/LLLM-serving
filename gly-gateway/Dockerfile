FROM openjdk:22-jdk-slim
ARG JAR_FILE=target/*.jar
ARG AGENT_JAR_FILE=/target/otel/*.jar
COPY ${JAR_FILE} app.jar
COPY ${AGENT_JAR_FILE} agent.jar

#ENV JAVA_TOOL_OPTIONS="-javaagent:agent.jar"
#ENV OTEL_SERVICE_NAME="gly-gateway-otel"
#ENV OTEL_EXPORTER_OTLP_PROTOCOL="grpc"
#ENV OTEL_EXPORTER_OTLP_ENDPOINT="http://localhost:4317"
#ENV OTEL_TRACES_EXPORTER="otlp"
#ENV OTEL_METRICS_EXPORTER="otlp"
#ENV OTEL_LOGS_EXPORTER="otlp"

#ENTRYPOINT ["java","-jar","/app.jar"]
