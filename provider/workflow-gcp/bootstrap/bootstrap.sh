#!/usr/bin/env bash

set -ex

source ./validate-env.sh "WORKFLOW_HOST"
source ./validate-env.sh "DATA_PARTITION_ID"

bootstrap_workflow_onprem() {

    # Getting ID_TOKEN from OpenID provider
    ID_TOKEN="$(curl --location --silent --globoff --request POST "${OPENID_PROVIDER_URL}/protocol/openid-connect/token" \
        --header "data-partition-id: ${DATA_PARTITION_ID}" \
        --header "Content-Type: application/x-www-form-urlencoded" \
        --data-urlencode "grant_type=client_credentials" \
        --data-urlencode "scope=openid" \
        --data-urlencode "client_id=${OPENID_PROVIDER_CLIENT_ID}" \
        --data-urlencode "client_secret=${OPENID_PROVIDER_CLIENT_SECRET}" | jq -r ".id_token")"
    export ID_TOKEN

    # Iterating over dag names
    IFS=","
    read -ra DAG_LIST <<<"$DAG_NAMES"

    # Create workflow for each dag
    for DAG_NAME in "${DAG_LIST[@]}"; do

        status_code=$(curl --location --globoff --request POST "${WORKFLOW_HOST}/api/workflow/v1/workflow" \
            --write-out "%{http_code}" --silent --output "output.txt" \
            --header 'Content-Type: application/json' \
            --header "Authorization: Bearer ${ID_TOKEN}" \
            --header "data-partition-id: ${DATA_PARTITION_ID}" \
            --data-binary '{ "workflowName": "'"${DAG_NAME}"'", "registrationInstructions": { "dagName": "'"${DAG_NAME}"'", "etc": "string" }, "description": "'"${DAG_NAME}"'" }')

        # Checking result code
        # 200 - Created, 409 - Already exists, Other - error
        if [ "$status_code" == 200 ]; then
            echo "Successfully registered workflow ${DAG_NAME}"
        elif [ "$status_code" == 409 ]; then
            cat /opt/output.txt | jq -r '.message'
        else
            cat /opt/output.txt | jq -r '.message'
            exit 1
        fi
        rm /opt/output.txt
    done
}

bootstrap_workflow_gcp() {

    # Getting ACCESS_TOKEN from context
    ACCESS_TOKEN="$(gcloud auth print-access-token)"
    export ACCESS_TOKEN

    # Iterating over dag names
    IFS=","
    read -ra DAG_LIST <<<"$DAG_NAMES"

    # Create workflow for each dag
    for DAG_NAME in "${DAG_LIST[@]}"; do

        status_code=$(curl --location --globoff --request POST "${WORKFLOW_HOST}/api/workflow/v1/workflow" \
            --write-out "%{http_code}" --silent --output "output.txt" \
            --header 'Content-Type: application/json' \
            --header "Authorization: Bearer ${ACCESS_TOKEN}" \
            --header "data-partition-id: ${DATA_PARTITION_ID}" \
            --data-binary '{ "workflowName": "'"${DAG_NAME}"'", "registrationInstructions": { "dagName": "'"${DAG_NAME}"'", "etc": "string" }, "description": "'"${DAG_NAME}"'" }')

        # Checking result code
        # 200 - Created, 409 - Already exists, Other - error
        if [ "$status_code" == 200 ]; then
            echo "Successfully registered workflow ${DAG_NAME}"
        elif [ "$status_code" == 409 ]; then
            cat /opt/output.txt | jq -r '.message'
        else
            cat /opt/output.txt | jq -r '.message'
            exit 1
        fi
        rm /opt/output.txt
    done

}

if [ "${ONPREM_ENABLED}" == "true" ]; then
    source ./validate-env.sh "OPENID_PROVIDER_URL"
    source ./validate-env.sh "OPENID_PROVIDER_CLIENT_ID"
    source ./validate-env.sh "OPENID_PROVIDER_CLIENT_SECRET"
    bootstrap_workflow_onprem
else
    bootstrap_workflow_gcp
fi

touch /tmp/bootstrap_ready
