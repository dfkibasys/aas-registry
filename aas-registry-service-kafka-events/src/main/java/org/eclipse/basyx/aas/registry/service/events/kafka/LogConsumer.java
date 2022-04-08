package org.eclipse.basyx.aas.registry.service.events.kafka;

import java.io.IOException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
@ConditionalOnProperty(prefix = "events", name = "sink", havingValue = "kafka")
public class LogConsumer {

	
	@KafkaListener(topics = "aas-registry", groupId = "log", autoStartup = "true")
	public void consume(String message) throws IOException {
		log.info("Kafka Event received -> " + message);
	}
	
}
