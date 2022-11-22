# wso2-api-revisioner

### Recommendations for building
- Java 1.8+
- Maven 3.5.4+

### Tested APIM Versions
- 4.1.0

### Validations in-place
- Following variables are parameterized so that they can be defined according to the environment
> - HOST --> API Manager hostname
> - TRANSPORT_PORT --> API Manager port
> - CLIENT_KEY --> Client Key of an application capable of generating password grant type access token
> - CLIENT_SECRET -->  Client Secrete of an application capable of generating password grant type access token
> - SANDBOX_ENDPOINT --> The sandbox endpoint that needs to be changed
> - PRODUCTION_ENDPOINT --> The production endpoint that needs to be changed
> - USERNAME --> Admin username
> - PASSWORD --> Admin password
> - API_LIMIT --> This number should be higher than the number of APIs available in the Publisher
- The above parameters should be defined in a file named "integration.properties" in a directory named "resources". This directory should reside in the same directory in which the tool is.
> <TOOL_HOME>/resources/integration.properties
- A log file named "revisioner.log" will be generated in the "resources" directory once the tool is executed and completed its task.
- When retrieving all APIs, the DEPRECATED and RETIRED APIs will be skipped as they cannot be published again.
- When retrieving all APIs, the WebSocket APIs will be skipped as it is not been implemented yet.
- When changing an endpoint (sandbox/production), the tool will only change the <http|https>://\<HOST\>:\<PORT\> part. The API's context path will remain the same.
- The tool will validate whether the sandbox or production endpoint exists or not. There could be APIs with either sandbox or production endpoint only. This will take care of those APIs.
- Before creating a revision, the tool will check if the maximum revision limit is reached for a given API. If yes, it will delete the oldest inactive revision.
- The following variables will be taken from the latest active revision.
> - GATEWAY_NAME --> Name of the GW that the revision should be deployed
> - VHOST --> Host of the GW access URL
> - DISPLAY_ON_DEV_PORTAL --> Whether to display the API on DevPortal or not

### How to run
- Build the tool
> mvn clean install
- Copy the built wso2-api-revisioner-tool-0.0.1-SNAPSHOT.jar to a separate place and let's call that <TOOL_HOME>
- Copy the "resources" directory available in the project to <TOOL_HOME>
- Change the parameters according to your environment
- Run the tool
> java -jar wso2-api-revisioner-tool-0.0.1-SNAPSHOT.jar
- After the process is complete, the revisioner.log file will be available in the <TOOL_HOME>/resources directory