package org.eclipse.basyx.aas.registry.service.storage.elasticsearch;

import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "registry", name = "type", havingValue = "elasticsearch")
public interface AasDescriptorRepository extends ElasticsearchRepository<AssetAdministrationShellDescriptor, String> {

}
