# Asset Administration Registry Server Base Tests

This project offers test utilities that you can use in your storage or integration tests.

We use an extra project here instead of test-jar generation as it is the [prefered way](https://maven.apache.org/plugins/maven-jar-plugin/examples/create-test-jar.html) of providing test artifacts

Have a look at the elastic-search-storage project or the release project to see how the abstract test classes defined here can be used. The provide a good test coverage and you can extend these classes without writing additional test cases for your storage.

Use [testcontainers](https://www.testcontainers.org/) for integration or storage tests. 

The integration test of the release project starts an Apache Kafka and an ElasticSearch instance using testcontainers and you need to start up a docker daemon before running the tests.



