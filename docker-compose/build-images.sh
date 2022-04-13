#!/bin/sh
cd ../aas-registry-service-release-kafka-es 
. build-image.sh
cd ../../aas-registry-service-release-kafka-mem
. build-image.sh
cd ../../aas-registry-service-release-log-es
. build-image.sh
cd ../../aas-registry-service-release-log-mem
. build-image.sh
cd ../docker-compose