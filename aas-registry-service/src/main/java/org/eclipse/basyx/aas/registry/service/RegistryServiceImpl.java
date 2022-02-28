package org.eclipse.basyx.aas.registry.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.validation.constraints.NotNull;

import org.eclipse.basyx.aas.registry.events.RegistryEvent;
import org.eclipse.basyx.aas.registry.events.RegistryEvent.EventType;
import org.eclipse.basyx.aas.registry.events.RegistryEventListener;
import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.basyx.aas.registry.model.ShellDescriptorSearchQuery;
import org.eclipse.basyx.aas.registry.model.ShellDescriptorSearchResponse;
import org.eclipse.basyx.aas.registry.model.SubmodelDescriptor;
import org.eclipse.basyx.aas.registry.repository.AssetAdministrationShellDescriptorRepository;
import org.eclipse.basyx.aas.registry.repository.AtomicElasticSearchRepoAccess;
import org.eclipse.basyx.aas.registry.repository.SearchRequestMapper;
import org.eclipse.basyx.aas.registry.repository.SearchResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.UpdateResponse.Result;
import org.springframework.stereotype.Service;

import lombok.NonNull;

@Service
public class RegistryServiceImpl implements RegistryService {

	private static final int MAX_RESULTS = 30;

	private static final String SUBMODEL_ID_IS_NULL = "Submodel id is null!";

	private static final String AAS_ID_IS_NULL = "Aas id is null!";

	@Autowired
	private AssetAdministrationShellDescriptorRepository aasDescriptorRepository;

	@Autowired
	private AtomicElasticSearchRepoAccess atomicRepoAccess;

	@Autowired
	private RegistryEventListener listener;

	@Autowired
	private ElasticsearchOperations ops;

	@Override
	public boolean existsSubmodelDescriptorById(@NotNull @NonNull String aasIdentifier,
			@NotNull @NonNull String submodelIdentifier) {
		SubmodelDescriptorIdMatcher matcher = new SubmodelDescriptorIdMatcher(submodelIdentifier);
		return getAssetAdministrationShellDescriptorById(aasIdentifier)
				.map(AssetAdministrationShellDescriptor::getSubmodelDescriptors).stream().flatMap(List::stream)
				.anyMatch(matcher::matches);
	}

	@Override
	public List<AssetAdministrationShellDescriptor> getAllAssetAdministrationShellDescriptors() {
		Iterable<AssetAdministrationShellDescriptor> iterable = aasDescriptorRepository.findAll();
		return StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toUnmodifiableList());
	}

	@Override
	public Optional<AssetAdministrationShellDescriptor> getAssetAdministrationShellDescriptorById(
			@NotNull @NonNull String aasIdentifier) {
		return aasDescriptorRepository.findById(aasIdentifier);
	}

	@Override
	public AssetAdministrationShellDescriptor registerAssetAdministrationShellDescriptor(
			@NotNull @NonNull AssetAdministrationShellDescriptor descriptor) {
		Objects.requireNonNull(descriptor.getIdentification(), AAS_ID_IS_NULL);
		AssetAdministrationShellDescriptor result = aasDescriptorRepository.save(descriptor);
		RegistryEvent evt = RegistryEvent.builder().id(result.getIdentification())
				.type(RegistryEvent.EventType.AAS_REGISTERED).aasDescriptor(result).build();
		listener.onEvent(evt);
		return result;
	}

	@Override
	public boolean unregisterAssetAdministrationShellDescriptorById(@NotNull @NonNull String aasIdentifier) {
		if (aasDescriptorRepository.existsById(aasIdentifier)) {
			aasDescriptorRepository.deleteById(aasIdentifier);
			RegistryEvent evt = RegistryEvent.builder().id(aasIdentifier)
					.type(RegistryEvent.EventType.AAS_UNREGISTERED).build();
			listener.onEvent(evt);
			return true;
		}
		return false;
	}

	@Override
	public Optional<List<SubmodelDescriptor>> getAllSubmodelDescriptors(@NotNull @NonNull String aasIdentifier) {
		// identifier is decoded in this method
		Optional<AssetAdministrationShellDescriptor> descriptorOpt = getAssetAdministrationShellDescriptorById(aasIdentifier);

		if (descriptorOpt.isEmpty()) {
			return Optional.empty();
		}
		List<SubmodelDescriptor> descriptorList = descriptorOpt
				.map(AssetAdministrationShellDescriptor::getSubmodelDescriptors).orElse(Collections.emptyList());
		return Optional.of(Collections.unmodifiableList(descriptorList));
	}

	@Override
	public Optional<SubmodelDescriptor> getSubmodelDescriptorById(@NotNull @NonNull String aasIdentifier,
			@NotNull @NonNull String submodelIdentifier) {
		SubmodelDescriptorIdMatcher matcher = new SubmodelDescriptorIdMatcher(submodelIdentifier);

		// use encoded aasIdentifier here, because it is decoded in
		// getAssetAdministrationShellDescriptorById()!
		return getAssetAdministrationShellDescriptorById(aasIdentifier)
				.map(AssetAdministrationShellDescriptor::getSubmodelDescriptors).stream().flatMap(List::stream)
				.filter(matcher::matches).findFirst();
	}

	@Override
	public boolean registerSubmodelDescriptor(@NotNull @NonNull String aasIdentifier,
			@NotNull @NonNull SubmodelDescriptor submodel) {
		Objects.requireNonNull(submodel.getIdentification(), SUBMODEL_ID_IS_NULL);
		Result result = atomicRepoAccess.storeAssetAdministrationSubmodel(aasIdentifier, submodel);
		if (result == Result.UPDATED) {
			RegistryEvent evt = RegistryEvent.builder().id(aasIdentifier)
					.submodelId(submodel.getIdentification()).type(EventType.SUBMODEL_REGISTERED)
					.submodelDescriptor(submodel).build();
			listener.onEvent(evt);
			return true;
		}
		return false;
	}

	@Override
	public boolean unregisterSubmodelDescriptorById(@NotNull @NonNull String aasIdentifier,
			@NotNull @NonNull String subModelId) {
		Result result = atomicRepoAccess.removeAssetAdministrationSubmodel(aasIdentifier, subModelId);
		if (result == Result.UPDATED) {
			RegistryEvent evt = RegistryEvent.builder().id(aasIdentifier).submodelId(subModelId)
					.type(EventType.SUBMODEL_UNREGISTERED).build();
			listener.onEvent(evt);
			return true;
		}
		return false;
	}

	@Override
	public ShellDescriptorSearchResponse searchAssetAdministrationShellDescriptors(ShellDescriptorSearchQuery query) {
		NativeSearchQuery nQuery = SearchRequestMapper.mapSearchQuery(query);
		SearchHits<AssetAdministrationShellDescriptor> hits = ops.search(nQuery,
				AssetAdministrationShellDescriptor.class);
		SearchResultMapper cutter = new SearchResultMapper();
		List<AssetAdministrationShellDescriptor> transformed = cutter.shrinkHits(hits);
		return new ShellDescriptorSearchResponse().total(hits.getTotalHits()).hits(transformed);
	}

	@Override
	public void unregisterAllAssetAdministrationShellDescriptors() {
		List<String> descriptors;
		do {

			descriptors = atomicRepoAccess.getAllIds(MAX_RESULTS);
			aasDescriptorRepository.deleteAllById(descriptors);
			// It could be that an element was deleted during get and delete operations by
			// another client
			// We ignore this for now and always fire events
			// could be that we end up in multiple delete events, but that's also true for a
			// single delete in the current version
			// we would need to go more low-level and check the response code if we want to
			// have just one event fired then
			for (String eachId : descriptors) {
				RegistryEvent evt = RegistryEvent.builder().id(eachId).type(RegistryEvent.EventType.AAS_UNREGISTERED)
						.build();
				listener.onEvent(evt);
			}
		} while (descriptors.size() == MAX_RESULTS);
	}


	private static final class SubmodelDescriptorIdMatcher {

		private final String subModelIdentifier;

		private SubmodelDescriptorIdMatcher(String subModelIdentifier) {
			this.subModelIdentifier = subModelIdentifier;
		}

		private boolean matches(SubmodelDescriptor descriptor) {
			return Objects.equals(descriptor.getIdentification(), subModelIdentifier);
		}
	}
}