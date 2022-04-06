package org.eclipse.basyx.aas.registry.events;

public interface RegistryEventSink {

	void consumeEvent(RegistryEvent evt);

}
