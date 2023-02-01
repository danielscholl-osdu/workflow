FROM google/cloud-sdk:alpine

WORKDIR /opt

COPY ./provider/workflow-gc/bootstrap/ /opt/

RUN apk add jq
RUN chmod +x bootstrap.sh validate-env.sh

CMD ["/bin/bash", "-c", "./bootstrap.sh && sleep 365d"]
