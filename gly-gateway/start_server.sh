#!/bin/bash

# Default value for the environment variable
ENVIRONMENT="dev"

# Loop through all command-line arguments
while [ "$1" != "" ]; do
    case $1 in
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
mvn clean install 

cd ./target
matches=(glygateway*.jar)
if (( ${#matches[@]} == 1 )); then
  mv -- "${matches[0]}" "app.jar"
else
  echo "ERROR no built jar file"
  exit
fi
cd -

docker compose down && docker compose up


