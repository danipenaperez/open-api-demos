#!/bin/bash

#Execution vars
CURRENT_USER=$(whoami)

# Open API Generator Tools configuration
GROUPID=com.dppware
ARTIFACT_ID=spring-pdf-api-client
ARTIFACT_VERSION=1.0.0
ARTIFACT_PACKAGE=com.dppware.demo
OPENAPI_MAIN_FILE=openapi3.yaml

# Remove if exist older folders generated
if [ -d "./api-contracts/generated/$ARTIFACT_ID" ] 
then
    echo "Removing old generated code "
    sudo rm -R ./api-contracts/generated/$ARTIFACT_ID 
fi

# Start Generation 
docker run --net host --rm \
-v "${PWD}/api-contracts:/local" openapitools/openapi-generator-cli generate \
-i /local/$OPENAPI_MAIN_FILE \
-g java \
--library resttemplate \
--additional-properties interfaceOnly=true \
--additional-properties generateSupportingFiles=false \
--additional-properties dateLibrary=java8 \
--enable-post-process-file \
--api-package $ARTIFACT_PACKAGE.client.api \
--model-package $ARTIFACT_PACKAGE.client.model \
--model-name-suffix DTO \
--invoker-package $ARTIFACT_PACKAGE.client.invoker \
--group-id $GROUPID \
--artifact-id $ARTIFACT_ID \
--artifact-version $ARTIFACT_VERSION \
-o /local/generated/$ARTIFACT_ID

# change folder
echo "Change owner generated folders"
sudo chown -R $CURRENT_USER:$CURRENT_USER ./api-contracts/generated/*

echo "Compile and Install artifact"
cd ./api-contracts/generated/$ARTIFACT_ID
mvn clean install


echo "Compile and Install artifact"
cd ./api-contracts/generated/$ARTIFACT_ID
mvn clean install

echo "Succesfully Created Classes, compiled and installation local (.m2)"
echo "	<dependency> "
echo "		<groupId>$GROUPID</groupId> "
echo "		<artifactId>$ARTIFACT_ID</artifactId> "
echo "		<version>$ARTIFACT_VERSION</version> "
echo "	</dependency>"

echo "Source Code generated at ./api-contracts/generated/$ARTIFACT_ID"