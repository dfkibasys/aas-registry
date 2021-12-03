#!/bin/bash

cp target/aas-registry-service-0.0.1-SNAPSHOT.jar docker/aas-registry-service.jar
cd docker

docker build -t dfkibasys/aas-registry:dotaas .
echo "Done"