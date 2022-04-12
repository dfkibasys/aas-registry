package org.eclipse.basyx.aas.registry.service.storage.elasticsearch;

import org.eclipse.basyx.aas.registry.service.tests.integration.BaseEventListener;
import org.eclipse.basyx.aas.registry.service.tests.integration.BaseIntegrationTest;
import org.junit.ClassRule;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;


@TestPropertySource(properties = { "registry.type=inMemory", "events.sink=kafka" })
public class KafkaEventsInMemoryStorageIntegrationTest extends BaseIntegrationTest {	

	private static final DockerImageName KAFKA_TEST_IMAGE = DockerImageName.parse("confluentinc/cp-kafka:6.2.1");

	@ClassRule
	public static KafkaContainer KAFKA = new KafkaContainer(KAFKA_TEST_IMAGE);

	
	@DynamicPropertySource
	static void assignAdditionalProperties(DynamicPropertyRegistry registry) {

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