## Service Configuration for GCP

## Environment variables:

Define the following environment variables.

Must have:

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `GOOGLE_AUDIENCES` | ex `*****.apps.googleusercontent.com` | Client ID for getting access to cloud resources | yes | https://console.cloud.google.com/apis/credentials |
| `SPRING_PROFILES_ACTIVE` | ex `gcp` | Spring profile that activate default configuration for GCP environment | false | - |
| `SHARED_TENANT_NAME` | ex `osdu` | Shared account id | no | - |
| `GCP_AIRFLOW_URL` | ex `https://********-tp.appspot.com` | Airflow endpoint | yes | - |
| `SHARED_TENANT_NAME` | ex `osdu` | Shared account id | no | - |

Defined in default application property file but possible to override:

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `LOG_PREFIX` | `workflow` | Logging prefix | no | - |
| `AUTHORIZE_API` | ex `https://entitlements.com/entitlements/v1` | Entitlements API endpoint | no | output of infrastructure deployment |
| `PARTITION_API` | ex `http://localhost:8081/api/partition/v1` | Partition service endpoint | no | - |
| `GOOGLE_APPLICATION_CREDENTIALS` | ex `/path/to/directory/service-key.json` | Service account credentials, you only need this if running locally | yes | https://console.cloud.google.com/iam-admin/serviceaccounts |
| `STATUS_CHANGED_MESSAGING_ENABLED` | `true` OR `false` | Allows to configure message publishing about schemas changes to Pub/Sub | no | - |
| `STATUS_CHANGED_TOPIC_NAME` | ex `status-changed` | Allows to subscribe a specific Pub/Sub topic | no | - |

These variables define service behavior, and are used to switch between `anthos` or `gcp` environments, their overriding
and usage in mixed mode was not tested. Usage of spring profiles is preferred.

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `PARTITION_AUTH_ENABLED` | ex `true` or `false` | Disable or enable auth token provisioning for requests to Partition service | no | - |
| `OQMDRIVER` | `rabbitmq` or `pubsub` | Oqm driver mode that defines which message broker will be used | no | - |
| `OSMDRIVER` | `postgres` OR `datastore` | Osm driver mode that defines which storage will be used | no | - |
| `OSDU_AIRFLOW_VERSION2` | `true` OR `false` | Allows to configure Airflow API used by Workflow service, choose `true` to use `stable` API, by default used `true`  | no | - |
| `AIRFLOW_IAAP_MODE` | `true` OR `false` | Allows to configure authentication method used by Workflow to authenticate it requests to Airflow, by default `true` and IAAP used | no | - |

## Datastore configuration:

There must be a namespace for each tenant, which is the same as the tenant name.

Example:

![Screenshot](./pics/namespace.PNG)

Kinds `workflow_osm` and `workflow_run_osm` will be created by service if it does not exist.

## Pubsub configuration:

At Pubsub should be created topic with name:

**name:** `status-changed`

It can be overridden by:

- through the Spring Boot property `gcp.status-changed.topicName`
- environment variable `STATUS_CHANGED_TOPIC_NAME`

## Google cloud service account configuration :
TBD

| Required roles |
| ---    |
| - |
