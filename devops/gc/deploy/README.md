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

### Global variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**global.domain** | your domain for the external endpoint, ex `example.com` | string | - | yes
**global.onPremEnabled** | whether on-prem is enabled | boolean | false | yes
**global.limitsEnabled** | whether CPU and memory limits are enabled | boolean | true | yes

### Configmap variables

| Name                     | Description           | Type   | Default               | Required |
| ------------------------ | --------------------- | ------ | --------------------- | -------- |
| **data.logLevel**             | logging level         | string | ERROR                  | yes      |
| **data.springProfilesActive** | active spring profile | string | gcp                   | yes      |
| **data.partitionHost**        | partition host        | string | "http://partition"    | yes      |
| **data.entitlementsHost**     | entitlements host     | string | "http://entitlements" | yes      |
| **data.osduAirflowUrl**       | airflow url           | string | "http://airflow:8080" | yes      |
| **data.sharedTenantName**     | tenant name           | string | -                     | yes      |
| **data.dataPartitionId** | ID of data partition | string | -                | yes      |
| **data.dataPartitionIdList** | list of partition IDs | array | - | yes |
| **data.worflowHost**     | Workflow host URL    | string | "http://workflow" | yes      |
| **data.composerClient**  | authentication method used by Workflow to authenticate its requests to Airflow | string | IAAP | no |

### Deployment variables

| Name                   | Description                  | Type   | Default      | Required |
| ---------------------- | ---------------------------- | ------ | ------------ | -------- |
| **data.image**              | your image name              | string | -            | yes      |
| **data.requestsCpu**        | amount of requests CPU       | string | 10m          | yes      |
| **data.requestsMemory**     | amount of requests memory    | string | 750Mi        | yes      |
| **data.limitsCpu**          | CPU limit                    | string | 1            | only if `global.limitsEnabled` is true      |
| **data.limitsMemory**       | memory limit                 | string | 3G           | only if `global.limitsEnabled` is true      |
| **data.serviceAccountName** | name of your service account | string | workflow     | yes      |
| **data.imagePullPolicy**    | when to pull image           | string | IfNotPresent | yes      |
| **data.bootstrapImage**              | name of the bootstrap image | string | -       | yes      |
| **data.bootstrapServiceAccountName** | name of the bootstrap SA    | string | -       | yes      |

### Config variables

| Name                           | Description                | Type    | Default                  | Required |
| ------------------------------ | -------------------------- | ------- | ------------------------ | -------- |
| **conf.appName**                    | name of the app            | string  | workflow                 | yes      |
| **conf.configmap**                  | configmap to be used       | string  | workflow-config          | yes      |
| **conf.workflowPostgresSecretName** | secret for postgres        | string  | workflow-postgres-secret | yes      |
| **conf.workflowAirflowSecretName**  | secret for airflow         | string  | workflow-airflow-secret  | yes      |
| **conf.rabbitmqSecretName**         | secret for rabbitmq        | string  | rabbitmq-secret          | yes      |
| **conf.bootstrapSecretName**        | Secret name for bootstrap  | string  | datafier-secret          | yes      |

### Istio variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**istio.proxyCPU** | CPU request for Envoy sidecars | string | `10m` | yes
**istio.proxyCPULimit** | CPU limit for Envoy sidecars | string | `200m` | yes
**istio.proxyMemory** | memory request for Envoy sidecars | string | `64Mi` | yes
**istio.proxyMemoryLimit** | memory limit for Envoy sidecars | string | `256Mi` | yes
**istio.bootstrapProxyCPU** | CPU request for Envoy sidecars | string | `10m` | yes
**istio.bootstrapProxyCPULimit** | CPU limit for Envoy sidecars | string | `100m` | yes

### Install the helm chart

Run this command from within this directory:

```console
helm install gc-workflow-deploy .
```

## Uninstalling the Chart

To uninstall the helm deployment:

```console
helm uninstall gc-workflow-deploy
```

[Move-to-Top](#deploy-helm-chart)
