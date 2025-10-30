<!--- Deploy -->

# Deploy helm chart

## Introduction

This chart bootstraps a deployment on a [Kubernetes](https://kubernetes.io) cluster using [Helm](https://helm.sh) package manager.

## Prerequisites

| The code was tested on **Kubernetes cluster** (v1.21.11) with **Istio** (1.12.6) |

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
| **global.domain** | your domain for the external endpoint, ex `example.com` | string | - | yes |
| **global.limitsEnabled** | whether CPU and memory limits are enabled | boolean | true | yes |
| **global.dataPartitionId** | ID of data partition | string | - | yes |
| **global.tier**            | tier defines the number of replicas for the service to ensure the service HA           | string  | ""      | no       |
| **global.autoscalingMode**     | enables horizontal pod autoscaling on cluster spot nodes; values are `none`, `cpu`, `requests`      | boolean | true    | yes      |

### Configmap variables

| Name                     | Description           | Type   | Default               | Required |
| ------------------------ | --------------------- | ------ | --------------------- | -------- |
| **data.logLevel**             | logging level         | string | ERROR                  | yes      |
| **data.partitionHost**        | partition host        | string | "<http://partition>"    | yes      |
| **data.entitlementsHost**     | entitlements host     | string | "<http://entitlements>" | yes      |
| **data.legalHost** | legal host for creating a tag in bootstrap | string | "<http://legal>" | yes |
| **data.schemaHost** | schema host for checking schema status in bootstrap | string | "<http://schema>" | yes |
| **data.osduAirflowUrl**       | airflow url           | string | "<http://airflow:8080>" | yes      |
| **data.worflowHost**     | Workflow host URL    | string | "<http://workflow>" | yes      |

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
| **data.affinityLabelsSpot** | labels with possible values, used to correctly setup the node affinities for spot deployment | object | cloud.google.com/gke-provisioning: [spot] | only if global.autoscaling is true
| **data.affinityLabelsStandard** | labels with possible values, used to correctly setup the node affinities for standard deployment | object | cloud.google.com/gke-provisioning: [standard] | only if global.autoscaling is true

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
| **istio.proxyCPU** | CPU request for Envoy sidecars | string | `10m` | yes |
| **istio.proxyCPULimit** | CPU limit for Envoy sidecars | string | `200m` | yes |
| **istio.proxyMemory** | memory request for Envoy sidecars | string | `64Mi` | yes |
| **istio.proxyMemoryLimit** | memory limit for Envoy sidecars | string | `256Mi` | yes |
| **istio.bootstrapProxyCPU** | CPU request for Envoy sidecars | string | `10m` | yes |
| **istio.bootstrapProxyCPULimit** | CPU limit for Envoy sidecars | string | `100m` | yes |

### Horizontal Pod Autoscaling (HPA) variables (works only if tier=PROD and autoscaling=true)

| Name                                                 | Description                                                                   | Type    | Default          | Required                                                                          |
|------------------------------------------------------|-------------------------------------------------------------------------------|---------|------------------|-----------------------------------------------------------------------------------|
| **hpa.minReplicas**                                  | minimum number of replicas                                                    | integer | `1`              | used only if `global.autoscalingMode` is not `none` and `global.tier` is "" (nil) |
| **hpa.maxReplicas**                                  | maximum number of replicas                                                    | integer | `6`              | used only if `global.autoscalingMode` is not `none` and `global.tier` is "" (nil) |
| **CPU based scaling**                                | **Enabled when `global.autoscalingMode` is cpu**                              |         |                  |     |
| **hpa.cpu.utilization**                              | the maximum number of new replicas to create (in percents from current state) | integer | `200`            | yes |
| **hpa.cpu.scaleUpStabilizationWindowSeconds**        | time to start implementing the scale up when it is triggered                  | integer | `30`             | yes |
| **hpa.cpu.scaleUpValue**                             | the maximum number of new replicas to create (in percents from current state) | integer | `200`            | yes |
| **hpa.cpu.scaleUpPeriod**                            | pause for every new scale up decision                                         | integer | `15`             | yes |
| **hpa.cpu.scaleDownStabilizationWindowSeconds**      | time to start implementing the scale down when it is triggered                | integer | `150`            | yes |
| **hpa.cpu.scaleDownValue**                           | the maximum number of replicas to destroy (in percents from current state)    | integer | `100`            | yes |
| **hpa.cpu.scaleDownPeriod**                          | pause for every new scale down decision                                       | integer | `15`             | yes |
| **REQUESTS based scaling**                           |  **Enabled when `global.autoscalingMode` is requests**                        |         |                  | **Requests based autoscaling uses Prometheus metrics. Prometheus should be installed in your cluster!**    |
| **hpa.requests.targetType**                          | type of measurements: AverageValue or Value                                   | string  | `"AverageValue"` | yes |
| **hpa.requests.targetValue**                         | threshold value to trigger the scaling up                                     | integer | `40`             | yes |
| **hpa.requests.scaleUpStabilizationWindowSeconds**   | time to start implementing the scale up when it is triggered                  | integer | `10`             | yes |
| **hpa.requests.scaleUpValue**                        | the maximum number of new replicas to create (in percents from current state) | integer | `50`             | yes |
| **hpa.requests.scaleUpPeriod**                       | pause for every new scale up decision                                         | integer | `15`             | yes |
| **hpa.requests.scaleDownStabilizationWindowSeconds** | time to start implementing the scale down when it is triggered                | integer | `60`             | yes |
| **hpa.requests.scaleDownValue**                      | the maximum number of replicas to destroy (in percents from current state)    | integer | `25`             | yes |
| **hpa.requests.scaleDownPeriod**                     | pause for every new scale down decision                                       | integer | `60`             | yes |

### Limits variables

| Name                     | Description                                     | Type    | Default | Required                                       |
|--------------------------|-------------------------------------------------|---------|---------|------------------------------------------------|
| **limits.maxTokens**     | maximum number of requests per fillInterval     | integer | `30`    | only if `global.autoscalingMode` is `requests` |
| **limits.tokensPerFill** | number of new tokens allowed every fillInterval | integer | `30`    | only if `global.autoscalingMode` is `requests` |
| **limits.fillInterval**  | time interval                                   | string  | `"1s"`  | only if `global.autoscalingMode` is `requests` |

### Autoscaling

By default, autoscaling configured for deployments targeting spot nodes. Pods will attempt to schedule on nodes with specific labels indicating they are spot instances. To adjust how pods are scheduled, you can update the data.affinityLabelsSpot for your spot deployments and data.affinityLabelsStandard for your standard deployments in your values.yaml file
Example:

```yml
data:
  affinityLabelsSpot:
    mylabel:
      - value1
      - test
    newLabel:
      - newValue
  affinityLabelsStandard:
    standardLabel:
      - labelValue
```

Each label, along with its values, will be translated into a separate `- matchExpressions` block within the `nodeAffinity` section of your deployment. This configuration operates with OR logic, meaning pods will be scheduled on any node that possesses at least one of the specified labels with one of its defined values.

The chart uses the global.autoscaling parameter in your `values.yaml` to control how autoscaling behaves. This parameter accepts three possible string values:

- **cpu** (default): Autoscaling is enabled and is based on CPU utilization. This is the default setting.
- **requests**: Autoscaling is enabled and is based on resource requests (custom metrics). To enable this, you must also set your global.tier to PROD. **NOTE**: Prometheus should be installed in your cluster, custom metrics used for this type of autoscaling.
- **none**: Autoscaling is entirely disabled for the application. Setting `global.autoscaling` to **none** also prevents the creation of the spot deployment.

### Methodology for Parameter Calculation variables: **hpa.requests.targetValue**, **limits.maxTokens** and **limits.tokensPerFill**

The parameters **hpa.requests.targetValue**, **limits.maxTokens** and **limits.tokensPerFill** were determined through empirical testing during load testing. These tests were conducted using the N2D machine series, which can run on either AMD EPYC Milan or AMD EPYC Rome processors. The values were fine-tuned to ensure optimal performance under typical workloads.

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
