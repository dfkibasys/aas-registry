#!/bin/sh
cd ../aas-registry-service-release-kafka-es 
sh build-image.sh
cd ../../aas-registry-service-release-kafka-mem
sh build-image.sh
cd ../../aas-registry-service-release-log-es
sh build-image.sh
cd ../../aas-registry-service-release-log-mem
sh build-image.sh
cd ../docker-compose