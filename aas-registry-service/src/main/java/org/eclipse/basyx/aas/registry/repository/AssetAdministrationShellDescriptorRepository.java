package org.eclipse.basyx.aas.registry.repository;

import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetAdministrationShellDescriptorRepository extends ElasticsearchRepository<AssetAdministrationShellDescriptor, String> {

	
	
}
