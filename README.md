# Asset Administration Shell Registry

This in a Java-based implementation of the Asset Adminstration Shell Registry server and client based on the corresponding [Open-API specification](https://api.swaggerhub.com/apis/Plattform_i40/Registry-and-Discovery/Final-Draft/swagger.yaml?resolved=true) of the German Plattform Industrie 4.0 and its specification document [Details of the Asset Administration Shell, Part 2, v1.0RC02](https://www.plattform-i40.de/IP/Redaktion/EN/Downloads/Publikation/Details_of_the_Asset_Administration_Shell_Part2_V1.html)

[aas-registry-service](aas-registry-service/README.md) provides the application server to access the AAS descriptor storage and offers an API for REST-based communication.

[aas-registry-client](aas-registry-client/README.md) can be used to interact with the backend and register or unregister descriptors and submodels.

[aas-registry-compatibility](aas-registry-compatibility/README.md) provides a IAASRegistry implementation using the aas-registry-client as compatibility layer for existing BaSyx components, e.g. the AASServer.

[aas-registry-paths](aas-registry-paths/README.md) generates a builder class that can be used by the registry client to create search requests. 

[aas-registry-events](aas-registry-events/README.md) provides classes to deserialize aas-registry events.

[aas-registry-plugins](aas-registry-plugins/README.md) contains maven plugins used by the other projects

The server uses [Elasticsearch](https://www.elastic.co/de/elasticsearch/) as persistence layer and [Apache Kafka](https://kafka.apache.org/) as event broadcasting mechanism. Both are encapsulated via [Spring Data Elasticsearch](https://spring.io/projects/spring-data-elasticsearch) and [Spring Cloud Stream](https://spring.io/projects/spring-cloud-stream) and can easily be replaced.

A docker image can be retrieved via `docker pull dfkibasys/aas-registry:dotaas-latest` or found [here](https://hub.docker.com/r/dfkibasys/aas-registry/).

A docker compose file for a fast setup is provided inside the aas-registry-service folder.


