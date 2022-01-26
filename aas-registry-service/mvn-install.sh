#!/bin/bash

cd ..
mvn -s .m2/settings.xml --batch-mode clean install -Dmaven.test.skip=true

read -p "Done. Press any key to continue... " -n1 -s
