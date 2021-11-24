package org.eclipse.basyx.aas.registry.service.test;

import org.eclipse.basyx.aas.registry.event.RegistryEvent;
import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptorEnvelop;
import org.eclipse.basyx.aas.registry.model.SubmodelDescriptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JacksonParsers {

	@Getter(lazy = true)
	private final ObjectMapper mapper = new ObjectMapper();

	@Getter(lazy = true)
	private final ObjectReader shellDescriptorListReader = getMapper()
			.readerForListOf(AssetAdministrationShellDescriptor.class);

	@Getter(lazy = true)
	private final ObjectReader subModelListReader = getMapper().readerForListOf(SubmodelDescriptor.class);

	@Getter(lazy = true)
	private final ObjectReader subModelReader = getMapper().readerFor(SubmodelDescriptor.class);

	@Getter(lazy = true)
	private final ObjectReader shellDescriptorReader = getMapper().readerFor(AssetAdministrationShellDescriptor.class);

	@Getter(lazy = true)
	private final ObjectReader repoReader = getMapper().readerForMapOf(AssetAdministrationShellDescriptorEnvelop.class);

	@Getter(lazy = true)
	private static final ObjectReader registryEventReader = getMapper().readerFor(RegistryEvent.class);


	public <V, C extends V> C clonePojo(V value, Class<C> cls) {
		return JacksonParsers.getMapper().convertValue(value, cls);
	}

}
