<!--- Deploy -->

# Deploy helm chart

## Introduction

This chart bootstraps a deployment on a [Kubernetes](https://kubernetes.io) cluster using [Helm](https://helm.sh) package manager.

## Prerequisites

The code was tested on **Kubernetes cluster** (v1.21.11) with **Istio** (1.12.6)

> It is possible to use other versions, but it hasn't been tested

### Operation system

The code works in Debian-based Linux (Debian 10 and Ubuntu 20.04) and Windows WSL 2. Also, it works but is not guaranteed in Google Cloud Shell. All other operating systems, including macOS, are not verified and supported.

### Packages

Packages are only needed for installation from a local computer.

- **HELM** (version: v3.7.1 or higher) [helm](https://helm.sh/docs/intro/install/)
- **Kubectl** (version: v1.21.0 or higher) [kubectl](https://kubernetes.io/docs/tasks/tools/#kubectl)

## Installation

First you need to set variables in **values.yaml** file using any code editor. Some of the values are prefilled, but you need to specify some values as well. You can find more information about them below.

### Configmap variables

| Name                     | Description           | Type   | Default               | Required |
| ------------------------ | --------------------- | ------ | --------------------- | -------- |
| **logLevel**             | logging level         | string | ERROR                  | yes      |
| **springProfilesActive** | active spring profile | string | gcp                   | yes      |
| **partitionHost**        | partition host        | string | "http://partition"    | yes      |
| **entitlementsHost**     | entitlements host     | string | "http://entitlements" | yes      |
| **osduAirflowUrl**       | airflow url           | string | "http://airflow:8080" | yes      |
| **sharedTenantName**     | tenant name           | string | -                     | yes      |
| **dataPartitionId** | ID of data partition | string | -                | yes      |
| **worflowHost**     | Workflow host URL    | string | "http://workflow" | yes      |
| **googleAudiences** | your Google Cloud client ID | string | -       | yes      |
| **composerClient**  | authentication method used by Workflow to authenticate its requests to Airflow | string | IAAP | no |

### Deployment variables

| Name                   | Description                  | Type   | Default      | Required |
| ---------------------- | ---------------------------- | ------ | ------------ | -------- |
| **image**              | your image name              | string | -            | yes      |
| **requestsCpu**        | amount of requests CPU       | string | 0.1          | yes      |
| **requestsMemory**     | amount of requests memory    | string | 2816M        | yes      |
| **limitsCpu**          | CPU limit                    | string | 1            | yes      |
| **limitsMemory**       | memory limit                 | string | 3G           | yes      |
| **serviceAccountName** | name of your service account | string | workflow     | yes      |
| **imagePullPolicy**    | when to pull image           | string | IfNotPresent | yes      |
| **bootstrapImage**              | name of the bootstrap image | string | -       | yes      |
| **bootstrapServiceAccountName** | name of the bootstrap SA    | string | -       | yes      |

### Config variables

| Name                           | Description                | Type    | Default                  | Required |
| ------------------------------ | -------------------------- | ------- | ------------------------ | -------- |
| **appName**                    | name of the app            | string  | workflow                 | yes      |
| **configmap**                  | configmap to be used       | string  | workflow-config          | yes      |
| **workflowPostgresSecretName** | secret for postgres        | string  | workflow-postgres-secret | yes      |
| **workflowAirflowSecretName**  | secret for airflow         | string  | workflow-airflow-secret  | yes      |
| **rabbitmqSecretName**         | secret for rabbitmq        | string  | rabbitmq-secret          | yes      |
| **bootstrapSecretName**        | Secret name for bootstrap  | string  | datafier-secret          | yes      |
| **onPremEnabled**              | whether on-prem is enabled | boolean | false                    | yes      |
| **domain**                     | your domain                | string  | -                        | yes      |

### Install the helm chart

Run this command from within this directory:

```console
helm install gcp-workflow-deploy .
```

## Uninstalling the Chart

To uninstall the helm deployment:

```console
helm uninstall gcp-workflow-deploy
```

[Move-to-Top](#deploy-helm-chart)
