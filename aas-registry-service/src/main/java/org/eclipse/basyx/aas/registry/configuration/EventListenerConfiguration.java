package org.eclipse.basyx.aas.registry.configuration;

import org.eclipse.basyx.aas.registry.events.RegistryEvent;
import org.eclipse.basyx.aas.registry.events.RegistryEventListener;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;

@Configuration
public class EventListenerConfiguration {
	
	@Bean
	public RegistryEventListener registryEventListener(StreamBridge bridge) {
		return new StreamBridgeRegistryEventListener(bridge);
	}
	
	@RequiredArgsConstructor
	private static class StreamBridgeRegistryEventListener implements RegistryEventListener {

		private static final String AAS_REGISTRY_BINDING_NAME = "aasRegistryBinding";
		
		private final StreamBridge streamBridge;

		@Override
		public void onEvent(RegistryEvent evt) {
			streamBridge.send(AAS_REGISTRY_BINDING_NAME, evt);
		}
	}
}
