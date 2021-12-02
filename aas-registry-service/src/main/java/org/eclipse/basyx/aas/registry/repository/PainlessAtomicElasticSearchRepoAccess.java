package org.eclipse.basyx.aas.registry.repository;

import java.util.Collections;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.eclipse.basyx.aas.registry.configuration.ElasticConfiguration.ElasticSearchScripts;
import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptorEnvelop;
import org.eclipse.basyx.aas.registry.model.SubmodelDescriptor;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.rest.RestStatus;
import org.springframework.dao.UncategorizedDataAccessException;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.RefreshPolicy;
import org.springframework.data.elasticsearch.core.ScriptType;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.elasticsearch.core.query.UpdateResponse;
import org.springframework.data.elasticsearch.core.query.UpdateResponse.Result;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PainlessAtomicElasticSearchRepoAccess implements AtomicElasticSearchRepoAccess {

	private final ElasticsearchOperations ops;

	private final ElasticSearchScripts scripts;

	private final ObjectMapper mapper;

	@Override
	public Result storeAssetAdministrationSubmodel(@NonNull String aasId, @NonNull SubmodelDescriptor descriptor) {
		@SuppressWarnings("rawtypes")
		Map input = mapper.convertValue(descriptor, Map.class);
		Map<String, Object> params = Collections.singletonMap("obj", input);
		String script = scripts.loadResourceAsString(ElasticSearchScripts.STORE_ASSET_ADMIN_SUBMODULE);
		return update(aasId, script, params);
	}

	@Override
	public Result removeAssetAdministrationSubmodel(@NotNull String aasId, @NotNull String subModelId) {
		Map<String, Object> params = Collections.singletonMap("id", subModelId);
		String script = scripts.loadResourceAsString(ElasticSearchScripts.REMOVE_ASSET_ADMIN_SUBMODULE);
		return update(aasId, script, params);
	}

	private Result update(String aasId, String script, Map<String, Object> params) {
		UpdateQuery query = UpdateQuery.builder(aasId).withLang(scripts.getLanguage()).withScriptType(ScriptType.INLINE)
				.withRefreshPolicy(RefreshPolicy.IMMEDIATE).withScript(script).withParams(params).build();
		IndexCoordinates coordinates = ops.getIndexCoordinatesFor(AssetAdministrationShellDescriptorEnvelop.class);
		try {
			UpdateResponse response = ops.update(query, coordinates);
			return response.getResult();
		} catch (UncategorizedDataAccessException ex) {
			return assertNotFound(ex);
		}
	}

	// assert that the api was called wrong -> aas id was not available
	// we do not want to use upserts for now
	private Result assertNotFound(UncategorizedDataAccessException ex) {
		Throwable th = ex.getCause();
		if (th instanceof ElasticsearchException) {
			ElasticsearchException stEx = (ElasticsearchException) th;
			if (stEx.status() == RestStatus.NOT_FOUND) {
				return Result.NOT_FOUND;
			}
		}
		throw ex;
	}

}
