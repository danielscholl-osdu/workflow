# workflow-gcp

## Running Locally

### Requirements

In order to run this service locally, you will need the following:

- [Maven 3.6.0+](https://maven.apache.org/download.cgi)
- [AdoptOpenJDK8](https://adoptopenjdk.net/)
- Infrastructure dependencies, deployable through the relevant [infrastructure template](https://community.opengroup.org/osdu/platform/deployment-and-operations/infra-gcp-provisioning)

### Environment Variables

In order to run the service locally, you will need to have the following environment variables defined.

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `LOG_PREFIX` | `workflow` | Logging prefix | no | - |
| `osdu.entitlements.url` | ex `https://entitlements.com/entitlements/v1` | Entitlements API endpoint | no | output of infrastructure deployment |
| `osdu.entitlements.app-key` | ex `test` | Entitlements app key | no | - |
| `gcp.airflow.url` | ex `https://********-tp.appspot.com` | Airflow endpoint | yes | - |
| `GOOGLE_AUDIENCES` | ex `*****.apps.googleusercontent.com` | Client ID for getting access to cloud resources | yes | https://console.cloud.google.com/apis/credentials |
| `GOOGLE_APPLICATION_CREDENTIALS` | ex `/path/to/directory/service-key.json` | Service account credentials, you only need this if running locally | yes | https://console.cloud.google.com/iam-admin/serviceaccounts |

**Required to run integration tests**

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `DOMAIN` | ex `contoso.com` | OSDU R2 to run tests under | no | - |
| `INTEGRATION_TESTER` | `********` | Service account for API calls. Note: this user must have entitlements configured already | yes | https://console.cloud.google.com/iam-admin/serviceaccounts |
| `NO_DATA_ACCESS_TESTER` | `********` | Service account without data access | yes | https://console.cloud.google.com/iam-admin/serviceaccounts |
| `LEGAL_TAG` | `********` | Demo legal tag used to pass test| yes | Legal service |
| `WORKFLOW_HOST` | ex `https://os-workflow-dot-opendes.appspot.com` | Endpoint of workflow service | no | - |
| `DEFAULT_DATA_PARTITION_ID_TENANT1`| ex `opendes` | OSDU tenant used for testing | no | - |
| `OTHER_RELEVANT_DATA_COUNTRIES`| `US`| - | no | - |
| `GOOGLE_AUDIENCE` | ex `********.apps.googleusercontent.com`| client application ID | yes | https://console.cloud.google.com/apis/credentials |
| `FINISHED_WORKFLOW_ID` | `********` | Workflow ID with finished status | yes | - |

**Entitlements configuration for integration accounts**

| INTEGRATION_TESTER | NO_DATA_ACCESS_TESTER | 
| ---  | ---   |
| users<br/>service.entitlements.user<br/>service.workflow.admin<br/>service.workflow.creator<br/>service.workflow.viewer<br/>service.legal.admin<br/>service.legal.editor<br/>data.test1<br/>data.integration.test | users |

### Configure Maven

Check that maven is installed:
```bash
$ mvn --version
Apache Maven 3.6.0
Maven home: /usr/share/maven
Java version: 1.8.0_212, vendor: AdoptOpenJDK, runtime: /usr/lib/jvm/jdk8u212-b04/jre
...
```

You may need to configure access to the remote maven repository that holds the OSDU dependencies. This file should live within `~/.mvn/community-maven.settings.xml`:
```bash
$ cat ~/.m2/settings.xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>community-maven-via-private-token</id>
            <!-- Treat this auth token like a password. Do not share it with anyone, including Microsoft support. -->
            <!-- The generated token expires on or before 11/14/2019 -->
             <configuration>
              <httpHeaders>
                  <property>
                      <name>Private-Token</name>
                      <value>${env.COMMUNITY_MAVEN_TOKEN}</value>
                  </property>
              </httpHeaders>
             </configuration>
        </server>
    </servers>
</settings>
```
### Build and run the application

After configuring your environment as specified above, you can follow these steps to build and run the application. These steps should be invoked from the *repository root.*
```bash
cd provider/workflow-gcp/ && mvn spring-boot:run
```
### Test the application

After the service has started it should be accessible via a web browser by visiting [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html). If the request does not fail, you can then run the integration tests.

```bash
# build + install integration test core
$ (cd testing/workflow-test-core/ && mvn clean install)

# build + run GCP integration tests.
#
# Note: this assumes that the environment variables for integration tests as outlined
#       above are already exported in your environment.
$ (cd testing/workflow-test-gcp/ && mvn clean test)
```

## License
  Copyright 2020 Google LLC
  Copyright 2020 EPAM Systems, Inc

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.