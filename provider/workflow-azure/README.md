## Ingestion Workflow Service
The Workflow service provides a wrapper functionality around the Apache Airflow functions and is
designed to carry out preliminary work with files before running the Airflow Directed Acyclic Graphs
(DAGs) that will perform actual ingestion of OSDU data.
In OSDU R2, depending on the types of data, workflow, and user, the Workflow service starts the
necessary workflow such as well log ingestion or opaque ingestion.
The Workflow Service is a [Spring Boot](https://spring.io/projects/spring-boot) service.



## Build
All references on repositories settings are external to `pom.xml` and should be configured through Maven `settings.xml` file.
To build against Community GitLab repositories, use `.mvn/community-maven.settings.xml` settings:
`mvn clean compile test --settings .mvn/community-maven.settings.xml`



## Running Locally

### Requirements

In order to run this service locally, you will need the following:

- [Maven 3.6.0+](https://maven.apache.org/download.cgi)
- [AdoptOpenJDK8](https://adoptopenjdk.net/)
- Infrastructure dependencies, deployable through the relevant [infrastructure template](https://dev.azure.com/slb-des-ext-collaboration/open-data-ecosystem/_git/infrastructure-templates?path=%2Finfra&version=GBmaster&_a=contents)
- While not a strict dependency, example commands in this document use [bash](https://www.gnu.org/software/bash/)

### General Tips

**Environment Variable Management**
The following tools make environment variable configuration simpler
- [direnv](https://direnv.net/) - for a shell/terminal environment
- [EnvFile](https://plugins.jetbrains.com/plugin/7861-envfile) - for [Intellij IDEA](https://www.jetbrains.com/idea/)

**Lombok**
This project uses [Lombok](https://projectlombok.org/) for code generation. You may need to configure your IDE to take advantage of this tool.
- [Intellij configuration](https://projectlombok.org/setup/intellij)
- [VSCode configuration](https://projectlombok.org/setup/vscode)

### Understanding Environment Variables

In order to run the service locally, you will need to have the following environment variables defined.

**Note** The following command can be useful to pull secrets from keyvault:
```bash
az keyvault secret show --vault-name $KEY_VAULT_NAME --name $KEY_VAULT_SECRET_NAME --query value -otsv
```

| name | value | description | sensitive? |
| ---  | ---   | ---         | ---        |
| `AZURE_CLIENT_ID` | `********` | Identity to run the service locally. This enables access to Azure resources. You only need this if running locally | yes |
| `AZURE_TENANT_ID` | `********` | AD tenant to authenticate users from | yes |
| `AZURE_CLIENT_SECRET` | `********` | Secret for `$AZURE_CLIENT_ID` | yes |
| `appinsights_key` | ******** | API Key for App Insights | yes |
| `KEYVAULT_URI` | ex https://foo-keyvault.vault.azure.net/ | URI of KeyVault that holds application secrets | no |
| `cosmosdb_database` | ex `dev-osdu-r2-db` | Cosmos database for storage documents | no | output of infrastructure deployment |
| `OSDU_ENTITLEMENTS_URL` | ex `https://foo-entitlements.azurewebsites.net` | Entitlements API endpoint | no | output of infrastructure deployment |
| `OSDU_ENTITLEMENTS_APPKEY` | `********` | The API key clients will need to use when calling the entitlements | yes | -- |
| `OSDU_AIRFLOW_URL` | ex `http://foo.org/test/airflow` | Airflow API endpoint | no |
| `OSDU_AIRFLOW_USERNAME` | ******** | User Name | yes |
| `OSDU_AIRFLOW_PASSWORD` | ******** | Airflow API password | yes |
| `adf_url` | ***** | ADF API endpoint | yes |
| `argo_url` | ex `http://foo.org/test/workflows/argo` | Argo API endpoint | no |
| `argo_token` | ***** | Argo token | yes |
| `LOG_PREFIX` | `workflow` | Logging prefix | no | - |
| `server_port` | `8082` | Port of application. | no | -- |

In Order to run service with AAD authentication add below environment variables, which will enable Authentication in workflow service using AAD filter.

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `azure_istioauth_enabled` | `false` | Flag to Disable AAD auth | no | -- |
| `azure.activedirectory.session-stateless` | `true` | Flag run in stateless mode (needed by AAD dependency) | no | -- |
| `azure.activedirectory.client-id` | `********` | AAD client application ID | yes | output of infrastructure deployment | output of infrastructure deployment |
| `azure.activedirectory.AppIdUri` | `api://${azure.activedirectory.client-id}` | URI for AAD Application | no | -- |

In Order to run service without authentication add below environment variables, which will disable authentication in workflow service.

name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `azure_istioauth_enabled` | `true` | Flag to Disable AAD auth | no | -- |


**Required to run integration tests**

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `AZURE_AD_APP_RESOURCE_ID` | `********` | AAD client application ID | yes | output of infrastructure deployment |
| `AZURE_AD_TENANT_ID` | `********` | AD tenant to authenticate users from | yes | -- |
| `DEPLOY_ENV` | `empty` | Required but not used | no | - |
| `DOMAIN` | `contoso.com` | OSDU R2 to run tests under | no | - |
| `INTEGRATION_TESTER` | `********` | System identity to assume for API calls. Note: this user must have entitlements configured already | no | -- |
| `NO_DATA_ACCESS_TESTER` | `********` | Service principal ID of a service principal without entitlements | yes | `aad-no-data-access-tester-client-id` secret from keyvault |
| `NO_DATA_ACCESS_TESTER_SERVICEPRINCIPAL_SECRET` | `********` | Secret for `$NO_DATA_ACCESS_TESTER` | yes | `aad-no-data-access-tester-secret` secret from keyvault |
| `WORKFLOW_HOST` | ex. `http://localhost:8082` | The URL where the workflow service is running | yes |
| `DEFAULT_DATA_PARTITION_ID_TENANT1` | ex `opendes` | OSDU tenant used for testing | no | -- |
| `TESTER_SERVICEPRINCIPAL_SECRET` | `********` | Secret for `$INTEGRATION_TESTER` | yes | -- |
| `FINISHED_WORKFLOW_ID` | ex c80a2419-8527-4804-b96a-6b6444f0d361 | Finished WorkflowID | no | -- |




### Configure Maven

Check that maven is installed:
```bash
$ mvn --version
Apache Maven 3.6.0
Maven home: /usr/share/maven
Java version: 1.8.0_212, vendor: AdoptOpenJDK, runtime: /usr/lib/jvm/jdk8u212-b04/jre
...
```

You may need to configure access to the remote maven repository that holds the OSDU dependencies. A default file should live within `~/.m2/settings.xml`:
```bash
$ cat ~/.m2/settings.xml
<settings>
	<profiles>
		<profile>
			<!-- This profile uses the CI-Token to authenticate with the server, and is the default case -->
			<id>Personal Maven Profile</id>
			<activation>
				<activeByDefault>true</activeByDefault>
			</activation>
			<properties>
				<repo.releases.id>community-maven-repo</repo.releases.id>
				<publish.snapshots.id>community-maven-via-private-token</publish.snapshots.id>
				<publish.releases.id>community-maven-via-private-token</publish.releases.id>
				<repo.releases.url>https://community.opengroup.org/api/v4/groups/17/-/packages/maven</repo.releases.url>
				<publish.snapshots.url>https://community.opengroup.org/api/v4/projects/118/packages/maven</publish.snapshots.url>
				<publish.releases.url>https://community.opengroup.org/api/v4/projects/118/packages/maven</publish.releases.url>
			</properties>
		</profile>
	</profiles>
	<servers>
		<server>
			<id>community-maven-via-private-token</id>
			<configuration>
				<httpHeaders>
					<property>
						<name>Private-Token</name>
						<value>${env.COMMUNITY_MAVEN_TOKEN}</value>
					</property>
				</httpHeaders>
			</configuration>
		</server>
		<server>
			<id>azure-auth</id>
			<configuration>
				<tenant>${env.AZURE_TENANT_ID}</tenant>
				<client>${env.AZURE_CLIENT_ID}</client>
				<key>${env.AZURE_CLIENT_SECRET}</key>
				<environment>AZURE</environment>
			</configuration>
		</server>
	</servers>
</settings>
```

### Build, Run and Test the application Locally

After configuring your environment as specified above, you can follow these steps to build and run the application

```bash
# execute build + unit tests
$ mvn clean package --settings .mvn/community-maven.settings.xml
...
[INFO] BUILD SUCCESS

# run service locally **REQUIRES SPECIFIC ENVIRONMENT VARIABLES SET**
$ java -jar $(find ./target/ -name '*.jar')

# Test the application  **REQUIRES SPECIFIC ENVIRONMENT VARIABLES SET**
$ mvn clean test --settings .mvn/community-maven.settings.xml -f integration-tests/pom.xml
```


### Test the application

After the service has started it should be accessible via a web browser by visiting [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html). If the request does not fail, you can then run the integration tests.

```bash
# build + install integration test core
$ (cd testing/storage-test-core/ && mvn clean install)

# build + run Azure integration tests.
#
# Note: this assumes that the environment variables for integration tests as outlined
#       above are already exported in your environment.
$ (cd testing/storage-test-azure/ && mvn clean test)
```
## License
Copyright © Microsoft Corporation

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
