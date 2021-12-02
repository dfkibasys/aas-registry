package org.eclipse.basyx.aas.registry.service.test.integration;

import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

public class TestSetup {

	private static final DockerImageName KAFKA_TEST_IMAGE = DockerImageName.parse("confluentinc/cp-kafka:6.2.1");

	private static final DockerImageName ELASTICSEARCH_TEST_IMAGE = DockerImageName
			.parse("docker.elastic.co/elasticsearch/elasticsearch-oss:7.10.2");

	public static KafkaContainer KAFKA = new KafkaContainer(KAFKA_TEST_IMAGE);

	public static ElasticsearchContainer ELASTICSEARCH = new ElasticsearchContainer(ELASTICSEARCH_TEST_IMAGE);

	public static void main(String[] args) throws InterruptedException {
		Runtime.getRuntime().addShutdownHook(new Thread(KAFKA::stop));
		Runtime.getRuntime().addShutdownHook(new Thread(ELASTICSEARCH::stop));
		KAFKA.start();
		ELASTICSEARCH.start();
	
		System.out.println("Kafka bootstrap: " + KAFKA.getBootstrapServers());
		System.out.println("Elastic search: " + ELASTICSEARCH.getHttpHostAddress());
		
		
		
		
		
		Thread.sleep(Long.MAX_VALUE);
	}
}
