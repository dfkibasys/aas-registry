package org.eclipse.basyx.aas.registry.service.configuration;

import org.eclipse.basyx.aas.registry.service.storage.AasRegistryStorage;
import org.eclipse.basyx.aas.registry.service.storage.memory.InMemoryAasRegistryStorage;
import org.eclipse.basyx.aas.registry.service.storage.memory.ThreadSafeAasRegistryStorageDecorator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InMemoryAasStorageConfiguration {

	@Bean
	@ConditionalOnProperty(prefix = "registry", name = "type", havingValue = "inMemory")
	public AasRegistryStorage storage() {
		return new ThreadSafeAasRegistryStorageDecorator(new InMemoryAasRegistryStorage());
	}

}
