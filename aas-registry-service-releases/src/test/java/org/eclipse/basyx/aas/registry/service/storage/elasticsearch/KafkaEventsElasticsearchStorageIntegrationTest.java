package org.eclipse.basyx.aas.registry.service.storage.elasticsearch;

import java.util.stream.Stream;

import org.eclipse.basyx.aas.registry.service.tests.integration.BaseEventListener;
import org.eclipse.basyx.aas.registry.service.tests.integration.BaseIntegrationTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;


@TestPropertySource(properties = { "registry.type=elasticsearch", "events.sink=kafka" })
@Ignore
public class KafkaEventsElasticsearchStorageIntegrationTest extends BaseIntegrationTest {	

	private static final DockerImageName KAFKA_TEST_IMAGE = DockerImageName.parse("confluentinc/cp-kafka:6.2.1");

	private static final DockerImageName ELASTICSEARCH_TEST_IMAGE = DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch-oss:7.10.2");
	
	public static KafkaContainer KAFKA = new KafkaContainer(KAFKA_TEST_IMAGE);

	public static ElasticsearchContainer ELASTIC_SEARCH = new ElasticsearchContainer(ELASTICSEARCH_TEST_IMAGE);
	
	@BeforeClass
	public static void startContainersInParallel() {
		Stream.of(KAFKA, ELASTIC_SEARCH).parallel().forEach(GenericContainer::start);
	}

	@AfterClass
	public static void stopContainersInParallel() {
		Stream.of(KAFKA, ELASTIC_SEARCH).parallel().forEach(GenericContainer::stop);
	}
	
	@DynamicPropertySource
	static void assignAdditionalProperties(DynamicPropertyRegistry registry) {
		registry.add("elasticsearch.url", ELASTIC_SEARCH::getHttpHostAddress);
		registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
	}

	
	@Component
	public static class KafkaEventListener extends BaseEventListener {

		@KafkaListener(topics = "aas-registry", groupId = "test")
		public void receive(String message) {			
			super.offer(message);
		}
	}
}