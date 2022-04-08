package org.eclipse.basyx.aas.registry.service.events.kafka;

import org.eclipse.basyx.aas.registry.events.RegistryEvent;
import org.eclipse.basyx.aas.registry.events.RegistryEventSink;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class KafkaRegistryEventSink implements RegistryEventSink {

	private static final String AAS_REGISTRY_BINDING_NAME = "aasRegistryBinding";

	@Autowired
	private StreamBridge streamBridge;

	@Override
	public void consumeEvent(RegistryEvent evt) {
		boolean msgSent = streamBridge.send(AAS_REGISTRY_BINDING_NAME, evt);
		if (msgSent)   {
			log.info("Registration event message sent to stream.");
		} else {
			log.error("Failed to sent registration event info.");
		}
	}

}