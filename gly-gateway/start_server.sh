#!/bin/bash

# Default value for the environment variable
ENVIRONMENT="dev"
JAVA_AGENT=false

# Loop through all command-line arguments
while [ "$1" != "" ]; do
    case $1 in
        -a | --agent )
            JAVA_AGENT=true
            ;;
        -p | --prod )
            ENVIRONMENT="prod"
            ;;
        -d | --dev )
            ENVIRONMENT="dev"
            ;;
        -h | --help )
            echo "Usage: myscript.sh [-p | --prod] [-d | --dev]"
            exit 0
            ;;
        * )
            echo "Invalid argument: $1"
            echo "Use --help for usage."
            exit 1
            ;;
    esac
    # Shift to the next argument
    shift
done

# --- Script logic starts here ---

echo "Running script in $ENVIRONMENT environment."
# mvn spring-boot:run -Dspring-boot.run.profiles=$ENVIRONMENT
# mvn clean install
mvn compile

if $JAVA_AGENT; then
  #export JAVA_TOOL_OPTIONS="-javaagent:otel/opentelemetry-javaagent.jar"
  export OTEL_SERVICE_NAME="gly-gateway-otel"
  export OTEL_EXPORTER_OTLP_PROTOCOL="grpc"
  export OTEL_EXPORTER_OTLP_ENDPOINT="http://localhost:4317"
  export OTEL_TRACES_EXPORTER="otlp"
  export OTEL_METRICS_EXPORTER="otlp"
  export OTEL_LOGS_EXPORTER="otlp"
fi

java -jar target/gly-gateway-*.jar

