package org.eclipse.basyx.aas.registry.repository;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.eclipse.basyx.aas.registry.model.SubmodelDescriptor;
import org.springframework.data.elasticsearch.core.query.UpdateResponse.Result;

public interface AtomicElasticSearchRepoAccess {

	Result storeAssetAdministrationSubmodel(@NotNull String aasId, @NotNull SubmodelDescriptor descriptor);

	Result removeAssetAdministrationSubmodel(@NotNull String aasIdentifier, @NotNull String subModelId);

	List<String> getAllIds(int maxResults);

}
