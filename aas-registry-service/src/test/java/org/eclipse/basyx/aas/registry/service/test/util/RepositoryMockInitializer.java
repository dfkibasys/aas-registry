package org.eclipse.basyx.aas.registry.service.test.util;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.SerializationUtils;
import org.eclipse.basyx.aas.registry.client.api.AasRegistryPaths;
import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.basyx.aas.registry.model.SubmodelDescriptor;
import org.eclipse.basyx.aas.registry.repository.AssetAdministrationShellDescriptorRepository;
import org.eclipse.basyx.aas.registry.repository.AtomicElasticSearchRepoAccess;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.RegexpQueryBuilder;
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
		repoContent = content.stream().collect(Collectors.toMap(AssetAdministrationShellDescriptor::getIdentification,
				Function.identity()));

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
		Mockito.when(repoAccess.getAllIds(Mockito.anyInt())).thenAnswer(this::answerWithAllIds);
		Mockito.doAnswer(this::deleteAllById).when(repo).deleteAllById(Mockito.anyIterable());
	}
	
	private Void deleteAllById(InvocationOnMock invocation) {
		List<String> itemIds = invocation.getArgument(0);
		itemIds.forEach(repoContent::remove);
		return null;
	}

	private List<String> answerWithAllIds(InvocationOnMock invocation) {
		int count = invocation.getArgument(0);
		// the list order is not relevant for us
		List<String> content = new LinkedList<>(repoContent.keySet());
		int listSize = content.size();
		int size =  listSize > count ? count : listSize;
		return content.subList(0, size);
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
		QueryBuilder builder = getQueryBuilder(invocation.getArgument(0));
		SearchHitMatcher<?> matcher = newSearchHitMatcher(builder);
		return matcher.search();
	}
	
	private SearchHitMatcher<?> newSearchHitMatcher(QueryBuilder builder) {
		if (builder instanceof MatchQueryBuilder) {
			MatchQueryBuilder mqBuilder = (MatchQueryBuilder) builder; 
			assert AasRegistryPaths.submodelDescriptors().identification().equals(mqBuilder.fieldName());
			return new MatchSearchHitMatcher(repoContent, (String) mqBuilder.value());
		} else {
			RegexpQueryBuilder regexBuilder = (RegexpQueryBuilder) builder;
			assert AasRegistryPaths.submodelDescriptors().idShort().equals(regexBuilder.fieldName());
			return new RegexSearchHitMatcher(repoContent, regexBuilder.value());
		}
	}

	private static abstract class SearchHitMatcher<T extends Object> {
		
		private final Map<String, AssetAdministrationShellDescriptor> content;
		private final Function<SubmodelDescriptor, String> valueResolver;
		private final T query;
		
		protected SearchHitMatcher(Map<String, AssetAdministrationShellDescriptor> content, Function<SubmodelDescriptor, String> valueResolver, T query) {
			this.content = content;
			this.valueResolver = valueResolver;
			this.query = query;
		}
		
		@SuppressWarnings("unchecked")
		public SearchHits<AssetAdministrationShellDescriptor> search() {
			SearchHits<AssetAdministrationShellDescriptor> hits = Mockito.mock(SearchHits.class);
			for (AssetAdministrationShellDescriptor descr : content.values()) {
				for (SubmodelDescriptor sDescr : Optional.ofNullable(descr.getSubmodelDescriptors())
						.orElseGet(Collections::emptyList)) {
					String value = valueResolver.apply(sDescr);
					if (matches(query, value)) {
						// we keep it quite simple here. 
						// assume that we will have just one hit per descriptor submodels
						// and also just one in all submodels
						SearchHit<AssetAdministrationShellDescriptor> hit = Mockito.mock(SearchHit.class);					
						mockHitList(hits, List.of(hit));
						Mockito.when(hit.getContent()).thenReturn(descr);
						Mockito.when(hits.getTotalHits()).thenReturn(1L);
						return hits;
					}
				}
			}
			Mockito.when(hits.getTotalHits()).thenReturn(0L);
			mockHitList(hits, List.of());
			return hits;		
		}
		
		private void mockHitList(SearchHits<AssetAdministrationShellDescriptor> hits,
				List<SearchHit<AssetAdministrationShellDescriptor>> toReturn) {
			Mockito.when(hits.get()).thenAnswer(i -> toReturn.stream());
			Mockito.when(hits.stream()).thenAnswer(i -> toReturn.stream());
			Mockito.when(hits.getSearchHits()).thenAnswer(i -> toReturn);

		}
		
		protected abstract boolean matches(T query, String value);
		
	}
	
	private final static class RegexSearchHitMatcher extends SearchHitMatcher<String> {

		private static final Map<String, Pattern> PATTERN_CACHE = new HashMap<>();		
		
		protected RegexSearchHitMatcher(Map<String, AssetAdministrationShellDescriptor> content, String query) {
			super(content, SubmodelDescriptor::getIdShort, query);
		}

		@Override
		protected boolean matches(String query, String value) {
			Pattern pattern = PATTERN_CACHE.computeIfAbsent(query, Pattern::compile);
			return pattern.matcher(value).matches();
		} 
		
	}
	
	private final static class MatchSearchHitMatcher extends SearchHitMatcher<Object> {

		public MatchSearchHitMatcher(Map<String, AssetAdministrationShellDescriptor> content, Object value) {
			super(content, SubmodelDescriptor::getIdentification, value);
		}

		@Override
		protected boolean matches(Object query, String value) {
			return query.equals(value);
		}
	}

	private QueryBuilder getQueryBuilder(NativeSearchQuery nsQuery) {
		QueryBuilder builder = nsQuery.getQuery();
		BoolQueryBuilder bBuilder;
		if (builder instanceof NestedQueryBuilder) {
			NestedQueryBuilder nQueryBuilder = (NestedQueryBuilder) builder;
			bBuilder = (BoolQueryBuilder) nQueryBuilder.query();
		} else {
			bBuilder = (BoolQueryBuilder) builder;
		}
		
		return bBuilder.must().get(0);
	}
	

	private Object assertCorrectPathAndGetValue(QueryBuilder builder) {
		// match always to identification and regexp to idshort
		// this is just the assumption based on our testcases
		if (builder instanceof MatchQueryBuilder) {
			MatchQueryBuilder mqBuilder = (MatchQueryBuilder) builder;
			
			return mqBuilder.value();
		} else {
			RegexpQueryBuilder rqBuilder = (RegexpQueryBuilder) builder;
			assert AasRegistryPaths.submodelDescriptors().idShort().equals(rqBuilder.fieldName());
			return rqBuilder.value();
		}
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
