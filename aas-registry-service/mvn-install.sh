#!/bin/bash

cd ..
mvn -s .m2/settings.xml --batch-mode clean install -Dmaven.test.skip=true

cd aas-registry-service
read -p "Done. Press any key to continue... " -n1 -s

