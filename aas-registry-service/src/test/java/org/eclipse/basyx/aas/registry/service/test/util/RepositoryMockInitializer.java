package org.eclipse.basyx.aas.registry.service.test.util;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.SerializationUtils;
import org.eclipse.basyx.aas.registry.client.api.ShellDescriptorPaths;
import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.basyx.aas.registry.model.SubmodelDescriptor;
import org.eclipse.basyx.aas.registry.repository.AssetAdministrationShellDescriptorRepository;
import org.eclipse.basyx.aas.registry.repository.AtomicElasticSearchRepoAccess;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.UpdateResponse.Result;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RepositoryMockInitializer extends TestWatcher {

	private final AssetAdministrationShellDescriptorRepository repo;

	private Map<String, AssetAdministrationShellDescriptor> repoContent;

	private final TestResourcesLoader loader;

	private final AtomicElasticSearchRepoAccess repoAccess;

	private final ElasticsearchOperations ops;

	@Override
	protected void starting(Description description) {
		try {
			List<AssetAdministrationShellDescriptor> descriptors = loader.loadRepositoryDefinition();
			initialize(descriptors);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void initialize(List<AssetAdministrationShellDescriptor> content) throws IOException {
		repoContent = content.stream()
				.collect(Collectors.toMap(AssetAdministrationShellDescriptor::getIdentification, Function.identity()));

		prepareFindAll();
		prepareFindById();
		prepareExistsById();
		prepareSave();
		prepareDeleteById();
		prepareAtomicRepoAccess();
		prepareSearchBySubmodelId();
	}

	private void prepareAtomicRepoAccess() {
		Mockito.when(repoAccess.removeAssetAdministrationSubmodel(Mockito.anyString(), Mockito.anyString()))
				.thenAnswer(this::answerRemoveAssetAdminstrationSubmodel);
		Mockito.when(
				repoAccess.storeAssetAdministrationSubmodel(Mockito.anyString(), Mockito.any(SubmodelDescriptor.class)))
				.thenAnswer(this::answerStoreAssetAdminstrationSubmodel);
	}

	private Result answerStoreAssetAdminstrationSubmodel(InvocationOnMock invocation) {
		String aasId = invocation.getArgument(0);
		SubmodelDescriptor toAdd = invocation.getArgument(1);
		AssetAdministrationShellDescriptor descriptor = repoContent.get(aasId);
		if (descriptor == null) {
			return Result.NOT_FOUND; // aasid not found
		}
		List<SubmodelDescriptor> submodels = descriptor.getSubmodelDescriptors();
		if (submodels != null) {
			ListIterator<SubmodelDescriptor> submodelIter = submodels.listIterator();
			while (submodelIter.hasNext()) {
				SubmodelDescriptor current = submodelIter.next();
				if (Objects.equals(current.getIdentification(), toAdd.getIdentification())) {
					submodelIter.set(toAdd);
					return Result.UPDATED;
				}
			}
		}
		descriptor.addSubmodelDescriptorsItem(toAdd);
		return Result.UPDATED;
	}

	private Result answerRemoveAssetAdminstrationSubmodel(InvocationOnMock invocation) {
		String aasId = invocation.getArgument(0);
		String submodelId = invocation.getArgument(1);
		AssetAdministrationShellDescriptor descriptor = repoContent.get(aasId);
		if (descriptor == null) {
			return Result.NOT_FOUND;
		}
		List<SubmodelDescriptor> descrList = descriptor.getSubmodelDescriptors();
		if (descrList != null) {
			if (descrList.removeIf(d -> Objects.equals(d.getIdentification(), submodelId))) {
				return Result.UPDATED;
			}
		}
		return Result.NOT_FOUND;
	}

	private void prepareSave() {
		Mockito.when(repo.save(Mockito.any(AssetAdministrationShellDescriptor.class))).then(this::answerSave);
	}

	private AssetAdministrationShellDescriptor answerSave(InvocationOnMock invocation) {
		AssetAdministrationShellDescriptor envelop = invocation.getArgument(0);
		AssetAdministrationShellDescriptor toStore = SerializationUtils.clone(envelop);
		repoContent.put(toStore.getIdentification(), toStore);
		return SerializationUtils.clone(toStore);
	}

	private Map<String, AssetAdministrationShellDescriptor> cloneRepo()
			throws JsonMappingException, JsonProcessingException {
		Map<String, AssetAdministrationShellDescriptor> toReturn = new HashMap<>();
		for (Entry<String, AssetAdministrationShellDescriptor> eachEntry : repoContent.entrySet()) {
			AssetAdministrationShellDescriptor clone = SerializationUtils.clone(eachEntry.getValue());
			toReturn.put(clone.getIdentification(), clone);
		}
		return toReturn;
	}

	private void prepareDeleteById() {
		Mockito.doAnswer(this::answerDeleteById).when(repo).deleteById(Mockito.anyString());
	}

	private Void answerDeleteById(InvocationOnMock invocation) {
		String id = invocation.getArgument(0);
		repoContent.remove(id);
		return null;
	}

	private void prepareExistsById() {
		Mockito.when(repo.existsById(Mockito.anyString())).thenAnswer(this::answerExistsById);
	}

	private boolean answerExistsById(InvocationOnMock invocation) {
		String id = invocation.getArgument(0);
		return repoContent.containsKey(id);
	}

	private void prepareSearchBySubmodelId() {
		Mockito.when(ops.search(Mockito.any(Query.class), Mockito.eq(AssetAdministrationShellDescriptor.class)))
				.then(this::answerSearchBySubmodelId);
	}

	@SuppressWarnings("unchecked")
	private SearchHits<AssetAdministrationShellDescriptor> answerSearchBySubmodelId(InvocationOnMock invocation) {
		Object value = getValueAndAssertCorrectPath(invocation.getArgument(0));
	    
		SearchHits<AssetAdministrationShellDescriptor> hits = Mockito.mock(SearchHits.class);
		for (AssetAdministrationShellDescriptor descr : repoContent.values()) {
			for (SubmodelDescriptor sDescr : Optional.ofNullable(descr.getSubmodelDescriptors())
					.orElseGet(Collections::emptyList)) {
				if (Objects.equals(sDescr.getIdentification(), value)) {
					SearchHit<AssetAdministrationShellDescriptor> hit = Mockito.mock(SearchHit.class);
					mockHitList(hits, List.of(hit));
					Mockito.when(hit.getContent()).thenReturn(descr);
					return hits;
				}
			}
		}
		mockHitList(hits, List.of());
		return hits;
	}

	private void mockHitList(SearchHits<AssetAdministrationShellDescriptor> hits,
			List<SearchHit<AssetAdministrationShellDescriptor>> toReturn) {
		Mockito.when(hits.get()).thenAnswer(i -> toReturn.stream());
		Mockito.when(hits.stream()).thenAnswer(i -> toReturn.stream());
		Mockito.when(hits.getSearchHits()).thenAnswer(i -> toReturn);
		
	}

	private Object getValueAndAssertCorrectPath(NativeSearchQuery nsQuery) {
		QueryBuilder builder = nsQuery.getQuery();
		BoolQueryBuilder bBuilder;
		if (builder instanceof NestedQueryBuilder) {
			NestedQueryBuilder nQueryBuilder = (NestedQueryBuilder) builder;
			bBuilder = (BoolQueryBuilder) nQueryBuilder.query();
		} else {
			bBuilder = (BoolQueryBuilder) builder;
		}
		MatchQueryBuilder mBuilder = (MatchQueryBuilder) bBuilder.must().get(0);
		// for the test we expect that it is a submodel id request because we do not
		// want complex logic in our mock
		String path = mBuilder.fieldName();
		assert ShellDescriptorPaths.submodelDescriptors().identification().equals(path);
		return mBuilder.value();
	}

	private void prepareFindById() {
		Mockito.when(repo.findById(Mockito.anyString())).thenAnswer(this::answerFindById);
	}

	private Optional<AssetAdministrationShellDescriptor> answerFindById(InvocationOnMock invocation)
			throws JsonMappingException, JsonProcessingException {
		String id = invocation.getArgument(0);
		return Optional.ofNullable(cloneRepo().get(id));
	}

	private void prepareFindAll() {
		Mockito.when(repo.findAll()).thenAnswer(i -> cloneRepo().values());
	}

}
