package org.eclipse.basyx.aas.registry.service.test.util;

import java.io.IOException;

import org.eclipse.basyx.aas.registry.repository.AssetAdministrationShellDescriptorRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class RegistryServiceTestConfiguration {	

	
	@Bean
	public ObjectMapper createObjectMapper() {
		return new ObjectMapper();
	}

	@Bean
	public JacksonReaders createJacksonReaderSupport(ObjectMapper mapper) {
		return new JacksonReaders(mapper);
	}
	
	@Bean
	public TestResourcesLoader createResourceLoader(JacksonReaders readerSupport) {
		return new TestResourcesLoader(readerSupport);
	}
	
	@Bean
	public RepositoryMockInitializer createMockInitializer(AssetAdministrationShellDescriptorRepository repo, TestResourcesLoader loader) throws IOException {
		return new RepositoryMockInitializer(repo, loader);
	}
}
