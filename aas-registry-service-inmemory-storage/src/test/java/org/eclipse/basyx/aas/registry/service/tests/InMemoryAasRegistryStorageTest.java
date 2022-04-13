package org.eclipse.basyx.aas.registry.service.tests;

import org.eclipse.basyx.aas.registry.service.configuration.InMemoryAasStorageConfiguration;
import org.eclipse.basyx.aas.registry.service.storage.AasRegistryStorage;
import org.eclipse.basyx.aas.registry.service.tests.AasRegistryStorageTest;
import org.eclipse.basyx.aas.registry.service.tests.CloningAasRegistryStorageDecorator;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = { "registry.type=inMemory" })
@ContextConfiguration(classes = { InMemoryAasRegistryStorageTest.class })
public class InMemoryAasRegistryStorageTest extends AasRegistryStorageTest {

	@Bean
	public AasRegistryStorage createCloningInMemoryStorage() {
		// we save the initial storage state in some testcases
		// so we do not want to alter the object and thus need a deep copy
		return new CloningAasRegistryStorageDecorator(new InMemoryAasStorageConfiguration().storage());
	}

}