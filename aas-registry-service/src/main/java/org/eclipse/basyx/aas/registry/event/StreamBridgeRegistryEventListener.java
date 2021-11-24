package org.eclipse.basyx.aas.registry.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StreamBridgeRegistryEventListener implements RegistryEventListener {


	private static final String AAS_REGISTRY_BINDING_NAME = "aasRegistryBinding";
	
	private final StreamBridge streamBridge;

	@Override
	public void onEvent(RegistryEvent evt) {
		streamBridge.send(AAS_REGISTRY_BINDING_NAME, evt);
	}
	

}
