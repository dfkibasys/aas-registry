package org.eclipse.basyx.aas.registry.repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.eclipse.basyx.aas.registry.configuration.ElasticConfiguration.ElasticSearchScripts;
import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.basyx.aas.registry.model.SubmodelDescriptor;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.springframework.dao.UncategorizedDataAccessException;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.RefreshPolicy;
import org.springframework.data.elasticsearch.core.ScriptType;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.elasticsearch.core.query.UpdateResponse;
import org.springframework.data.elasticsearch.core.query.UpdateResponse.Result;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PainlessAtomicElasticSearchRepoAccess implements AtomicElasticSearchRepoAccess {

	private static final String FAILED_TO_INITIALIZE_SCRIPTS = "Failed to initialize scripts";

	// just if we have a write conflict with another client and need retry
	private static final Integer MAX_RETRIES = Integer.MAX_VALUE;

	private final ElasticsearchOperations ops;

	private final ElasticSearchScripts scripts;

	private final RestHighLevelClient client;

	private final ElasticsearchConverter elasticsearchConverter;

	@Override
	public Result storeAssetAdministrationSubmodel(@NonNull String aasId, @NonNull SubmodelDescriptor descriptor) {
		Document doc = elasticsearchConverter.mapObject(descriptor);
		Map<String, Object> params = Collections.singletonMap("obj", doc);
		return update(aasId, ElasticSearchScripts.STORE_ASSET_ADMIN_SUBMODULE, params);
	}

	@Override
	public Result removeAssetAdministrationSubmodel(@NotNull String aasId, @NotNull String subModelId) {
		Map<String, Object> params = Collections.singletonMap("id", subModelId);
		return update(aasId, ElasticSearchScripts.REMOVE_ASSET_ADMIN_SUBMODULE, params);
	}

	private Result update(String aasId, String scriptId, Map<String, Object> params) {
		UpdateQuery query = UpdateQuery.builder(aasId).withScriptType(ScriptType.INLINE)
				.withLang(scripts.getLanguage())
				.withScript(scripts.loadResourceAsString(scriptId))
				.withRefreshPolicy(RefreshPolicy.IMMEDIATE).withParams(params)
				.withRetryOnConflict(MAX_RETRIES).withAbortOnVersionConflict(true).build();

		IndexCoordinates coordinates = ops.getIndexCoordinatesFor(AssetAdministrationShellDescriptor.class);
		try {
			UpdateResponse response = ops.update(query, coordinates);
			return response.getResult();
		} catch (UncategorizedDataAccessException ex) {
			return assertNotFound(ex);
		}
	}

	@Override
	public List<String> getAllIds(int maxValues) {
		MatchAllQueryBuilder matchAllBuilder = QueryBuilders.matchAllQuery();
		NativeSearchQuery query = new NativeSearchQuery(matchAllBuilder);
		query.setMaxResults(maxValues);
		query.setFields(List.of("identification"));
		SearchHits<AssetAdministrationShellDescriptor> hits = ops.search(query,
				AssetAdministrationShellDescriptor.class);
		return hits.get().map(SearchHit::getId).collect(Collectors.toList());
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