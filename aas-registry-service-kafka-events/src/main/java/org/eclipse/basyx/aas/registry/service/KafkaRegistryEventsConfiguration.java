package org.eclipse.basyx.aas.registry.service;

import org.eclipse.basyx.aas.registry.events.RegistryEventSink;
import org.eclipse.basyx.aas.registry.service.events.kafka.KafkaRegistryEventSink;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "events", name = "sink", havingValue = "kafka")
public class KafkaRegistryEventsConfiguration {

	@Bean
	public RegistryEventSink eventSink() {
		return new KafkaRegistryEventSink();
	}

}
