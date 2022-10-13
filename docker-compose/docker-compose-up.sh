#!/bin/bash

OLD_WORK_DIR=$(pwd)
trap 'cd $OLD_WORK_DIR' EXIT

cd $(dirname "${BASH_SOURCE[0]}")

docker-compose up -d --build --force-recreate

echo Done!
echo ""
echo Portainer: http://localhost:9090
echo Registry - kafka,es: http://localhost:8020
echo Registry - kafka,mem: http://localhost:8030
echo Registry - log,es: http://localhost:8050
echo Registry - log,mem: http://localhost:8040
echo ""
read -p "Press any key to continue... " -n1 -s
cd $OLD_WORK_DIR
