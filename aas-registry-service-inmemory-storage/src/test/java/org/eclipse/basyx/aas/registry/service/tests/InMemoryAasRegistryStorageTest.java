package org.eclipse.basyx.aas.registry.service.tests;

import org.eclipse.basyx.aas.registry.service.configuration.InMemoryAasStorageConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = { "registry.type=inMemory", , "logging.level.root=INFO" })
@ContextConfiguration(classes = { InMemoryAasStorageConfiguration.class })
public class InMemoryAasRegistryStorageTest extends AasRegistryStorageTest {

}