#!/bin/bash

cd ..
mvn -s .m2/settings.xml --batch-mode clean install -Dmaven.test.skip=true

sleep 1000
