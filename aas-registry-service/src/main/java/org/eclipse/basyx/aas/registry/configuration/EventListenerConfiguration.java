package org.eclipse.basyx.aas.registry.configuration;

import org.eclipse.basyx.aas.registry.event.RegistryEventListener;
import org.eclipse.basyx.aas.registry.event.StreamBridgeRegistryEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventListenerConfiguration {

	
	@Bean
	public RegistryEventListener registryEventListener(StreamBridge bridge) {
		return new StreamBridgeRegistryEventListener(bridge);
	}
}
