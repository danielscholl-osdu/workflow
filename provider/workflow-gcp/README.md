# workflow-gcp

The OSDU R3 Workflow service is designed to start business processes in the system. In the OSDU R3
prototype phase, the service allows you to work with workflow metadata, supporting CRUD operations
and also trigger workflow in airflow, get, delete and change the status of process startup records.

The Workflow service provides a wrapper functionality around the Apache Airflow functions and is
designed to carry out preliminary work with files before running the Airflow Directed Acyclic Graphs
(DAGs) that will perform actual ingestion of OSDU data.

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
| `WORKFLOW_ADMIN_ACCOUNT` | ex `admin@domain.iam.gserviceaccount.com` | Admin account for using root endpoints | yes | - |
| `gcp.airflow.url` | ex `https://********-tp.appspot.com` | Airflow endpoint | yes | - |
| `GOOGLE_AUDIENCES` | ex `*****.apps.googleusercontent.com` | Client ID for getting access to cloud resources | yes | https://console.cloud.google.com/apis/credentials |
| `GOOGLE_APPLICATION_CREDENTIALS` | ex `/path/to/directory/service-key.json` | Service account credentials, you only need this if running locally | yes | https://console.cloud.google.com/iam-admin/serviceaccounts |
| `OSDU_AIRFLOW_URL` | ex `https://********-tp.appspot.com` | Airflow endpoint | yes | - |
| `OSDU_AIRFLOW_USERNAME` | ex `******` | Username for access Apache Airflow | yes | - |
| `OSDU_AIRFLOW_PASSWORD` | ex `******` | Password for access Apache Airflow | yes | - |
| `PARTITION_API` | ex `http://localhost:8081/api/partition/v1` | Partition service endpoint | no | - |
| `SHARED_TENANT_NAME` | ex `common` or `opendes` | the name of the shared tenant | yes | - |
| `STATUS_CHANGED_MESSAGING_ENABLED` | `true` OR `false` | Allows to configure message publishing about schemas changes to Pub/Sub | no | - |
| `STATUS_CHANGED_TOPIC_NAME` | ex `status-changed` | Allows to subscribe a specific Pub/Sub topic | no | - |
| `OSMDRIVER` | `postgres` OR `datastore` | Osm driver mode that defines which storage will be used | no | - |
| `DATASTORE_LEGACY_DATA_STRUCTURE` | `false` OR `true` | Serves to use already existing data structure, by default true | no | - |
| `SPRING_DATASTOURCE_URL` | `jdbc:postgresql://127.0.0.1:5432/postgres` | Postgres connection URL | no | - |
| `SPRING_DATASTOURCE_USERNAME` | `postgres` | Postgres username | yes | - |
| `SPRING_DATASTOURCE_PASSWORD` | `postgres` | Postgres password | yes | - |
| `OSDU_AIRFLOW_VERSION2` | `true` OR `false` | Allows to configure Airflow API used by Workflow service, choose `true` to use `stable` API, by default used `false` and `experimental` API | no | - |
| `AIRFLOW_IAAP_MODE` | `true` OR `false` | Allows to configure authentication method used by Workflow to authenticate it requests to Airflow, by default `true` and IAAP used | no | - |
| `OSDU_AIRFLOW_USERNAME` | `******` | Airflow username, need to be defined if `AIRFLOW_IAAP_MODE`=`false`| yes | - |
| `OSDU_AIRFLOW_PASSWORD` | `******` | Airflow password, need to be defined if `AIRFLOW_IAAP_MODE`=`false` | yes | - |

**Required to run integration tests**

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `DOMAIN` | ex `contoso.com` | OSDU R2 to run tests under | no | - |
| `INTEGRATION_TESTER` | `********` | Service account for API calls, as a filename or JSON content, plain or Base64 encoded.  Note: this user must have entitlements configured already | yes | https://console.cloud.google.com/iam-admin/serviceaccounts |
| `NO_DATA_ACCESS_TESTER` | `********` | Service account without data access, as a filename or JSON content, plain or Base64 encoded. | yes | https://console.cloud.google.com/iam-admin/serviceaccounts |
| `LEGAL_TAG` | `********` | Demo legal tag used to pass test| yes | Legal service |
| `WORKFLOW_HOST` | ex `https://os-workflow-dot-opendes.appspot.com/api/workflow` | Endpoint of workflow service | no | - |
| `DEFAULT_DATA_PARTITION_ID_TENANT1`| ex `opendes` | OSDU tenant used for testing | no | - |
| `OTHER_RELEVANT_DATA_COUNTRIES`| `US`| - | no | - |
| `GOOGLE_AUDIENCE` | ex `********.apps.googleusercontent.com`| client application ID | yes | https://console.cloud.google.com/apis/credentials |
| `FINISHED_WORKFLOW_ID` | `********` | Workflow ID with finished status | yes | - |
| `TEST_DAG_NAME` | `********` | Name of test DAG | yes | - |

**Entitlements configuration for integration accounts**

| INTEGRATION_TESTER | NO_DATA_ACCESS_TESTER |
| ---  | ---   |
| users<br/>service.entitlements.user<br/>service.workflow.admin<br/>service.workflow.creator<br/>service.workflow.viewer<br/>service.legal.admin<br/>service.legal.editor<br/>data.test1<br/>data.integration.test | users |


### Persistence layer
### Database structure for OSMDRIVER=postgres
```
DROP TABLE IF EXISTS opendes.workflow;
CREATE TABLE IF NOT EXISTS opendes.workflow
(
	id text COLLATE pg_catalog."default" NOT NULL,
	pk bigint NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	data jsonb NOT NULL,
	CONSTRAINT workflow_id UNIQUE (id)
)
TABLESPACE pg_default;
ALTER TABLE opendes.workflow
    OWNER to postgres;


DROP TABLE IF EXISTS opendes.workflow_run;
CREATE TABLE IF NOT EXISTS opendes.workflow_run
(
	id text COLLATE pg_catalog."default" NOT NULL,
	pk bigint NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	data jsonb NOT NULL,
	CONSTRAINT workflow_run_id UNIQUE (id)
)
TABLESPACE pg_default;
ALTER TABLE opendes.workflow_run
    OWNER to postgres;


DROP TABLE IF EXISTS opendes.workflow_status;
CREATE TABLE IF NOT EXISTS opendes.workflow_status
(
	id text COLLATE pg_catalog."default" NOT NULL,
	pk bigint NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	data jsonb NOT NULL,
	CONSTRAINT workflow_status_id UNIQUE (id)
)
TABLESPACE pg_default;
ALTER TABLE opendes.workflow_status
    OWNER to postgres;

```
### Run Locally
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

* Update the Google cloud SDK to the latest version:

```bash
gcloud components update
```
* Set Google Project Id:

```bash
gcloud config set project <YOUR-PROJECT-ID>
```

* Perform a basic authentication in the selected project:

```bash
gcloud auth application-default login
```

## Testing
* Navigate to workflow service's root folder and run:

```bash
mvn clean install
```

* If you wish to see the coverage report then go to testing/target/site/jacoco-aggregate and open index.html

* If you wish to build the project without running tests

```bash
mvn clean install -DskipTests
```
### Running
After configuring your environment as specified above, you can follow these steps to build and run the application. These steps should be invoked from the *repository root.*

```bash
cd provider/workflow-gcp-datastore/ && mvn spring-boot:run
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

## Deployment
Workflow Service is compatible with App Engine Flexible Environment and Cloud Run.

* To deploy into Cloud run, please, use this documentation:
  https://cloud.google.com/run/docs/quickstarts/build-and-deploy

* To deploy into App Engine, please, use this documentation:
  https://cloud.google.com/appengine/docs/flexible/java/quickstart

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
