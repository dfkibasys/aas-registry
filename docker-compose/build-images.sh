#!/bin/sh
OLD_WORK_DIR=$(pwd)
trap 'cd $OLD_WORK_DIR' EXIT

cd $(dirname "${BASH_SOURCE[0]}")/..

mvn clean install -f aas-registry-plugins
mvn clean install -DskipTests -Ddocker.username=dfkibasys -Ddocker.password=""

cd $OLD_WORK_DIR


