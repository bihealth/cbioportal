#!/bin/bash -ex

export PORTAL_HOME=${PWD}/src/main/resources

export CBIOPORTAL_DB_USER="cbio_user"
export CBIOPORTAL_DB_PASSWORD="XXXXX"
export CBIOPORTAL_DB_HOST="XXXX"
export CBIOPORTAL_CONNECTION_STRING="jdbc:mysql://XXXX/"

export PORT=8088
export JAVA_OPTS="-Xmx4096m -Xms4096m -XX:+UseCompressedOops"

mvn -Pheroku -DskipTests package

java $JAVA_OPTS \
    -Dspring.profiles.active=dbcp,false \
    -Dspring.config.name=portal.properties \
    -Ddbconnector=dbcp \
    -jar portal/target/dependency/webapp-runner.jar \
        --expand-war --port $PORT portal/target/cbioportal-*.war
