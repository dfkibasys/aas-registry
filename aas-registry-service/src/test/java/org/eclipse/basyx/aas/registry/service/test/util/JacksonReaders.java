package org.eclipse.basyx.aas.registry.service.test.util;

import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptorEnvelop;
import org.eclipse.basyx.aas.registry.model.SubmodelDescriptor;
import org.eclipse.basyx.aas.registry.model.event.RegistryEvent;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import lombok.Getter;
import lombok.Setter;

public class JacksonReaders {
	
	@Setter
	private ObjectMapper mapper;	 
	
	@Getter(lazy = true)
	private final ObjectReader shellDescriptorListReader = mapper
			.readerForListOf(AssetAdministrationShellDescriptor.class);

	@Getter(lazy = true)
	private final ObjectReader subModelListReader = mapper.readerForListOf(SubmodelDescriptor.class);

	@Getter(lazy = true)
	private final ObjectReader subModelReader = mapper.readerFor(SubmodelDescriptor.class);

	@Getter(lazy = true)
	private final ObjectReader shellDescriptorReader = mapper.readerFor(AssetAdministrationShellDescriptor.class);

	@Getter(lazy = true)
	private final ObjectReader repoReader = mapper.readerForMapOf(AssetAdministrationShellDescriptorEnvelop.class);

	@Getter(lazy = true)
	private final ObjectReader registryEventReader = mapper.readerFor(RegistryEvent.class);

	@Autowired
	public JacksonReaders(ObjectMapper mapper) {
		this.mapper = mapper;
	}

}
