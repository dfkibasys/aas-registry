package org.eclipse.basyx.aas.registry.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.validation.constraints.NotNull;

import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptorEnvelop;
import org.eclipse.basyx.aas.registry.model.SubmodelDescriptor;
import org.eclipse.basyx.aas.registry.model.event.RegistryEvent;
import org.eclipse.basyx.aas.registry.model.event.RegistryEvent.EventType;
import org.eclipse.basyx.aas.registry.model.event.RegistryEventListener;
import org.eclipse.basyx.aas.registry.repository.AssetAdministrationShellDescriptorRepository;
import org.eclipse.basyx.aas.registry.repository.AtomicElasticSearchRepoAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.query.UpdateResponse.Result;
import org.springframework.stereotype.Service;

import lombok.NonNull;

@Service
public class RegistryServiceImpl implements RegistryService {

	private static final String SUBMODEL_ID_IS_NULL = "Submodel id is null!";

	private static final String AAS_ID_IS_NULL = "Aas id is null!";

	@Autowired
	private AssetAdministrationShellDescriptorRepository aasDescriptorRepository;

	@Autowired
	private AtomicElasticSearchRepoAccess atomicRepoAccess;

	@Autowired
	private RegistryEventListener listener;

	@Override
	public boolean existsAssetAdministrationShellDescriptorById(@NotNull @NonNull String aasIdentifier) {
		return aasDescriptorRepository.existsById(aasIdentifier);
	}

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
		Iterable<AssetAdministrationShellDescriptorEnvelop> iterable = aasDescriptorRepository.findAll();
		return StreamSupport.stream(iterable.spliterator(), false)
				.map(AssetAdministrationShellDescriptorEnvelop::getAssetAdministrationShellDescriptor)
				.collect(Collectors.toUnmodifiableList());
	}

	@Override
	public Optional<AssetAdministrationShellDescriptor> getAssetAdministrationShellDescriptorById(
			@NotNull @NonNull String aasIdentifier) {
		return aasDescriptorRepository.findById(aasIdentifier)
				.map(AssetAdministrationShellDescriptorEnvelop::getAssetAdministrationShellDescriptor);
	}

	@Override
	public AssetAdministrationShellDescriptor registerAssetAdministrationShellDescriptor(
			@NotNull @NonNull AssetAdministrationShellDescriptor descriptor) {
		Objects.requireNonNull(descriptor.getIdentification(), AAS_ID_IS_NULL);
		AssetAdministrationShellDescriptorEnvelop envelop = new AssetAdministrationShellDescriptorEnvelop(descriptor);
		AssetAdministrationShellDescriptorEnvelop result = aasDescriptorRepository.save(envelop);
		RegistryEvent evt = RegistryEvent.builder().id(result.getId()).type(RegistryEvent.EventType.AAS_REGISTERED)
				.aasDescriptor(result.getAssetAdministrationShellDescriptor()).build();
		listener.onEvent(evt);

		return result.getAssetAdministrationShellDescriptor();
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
		Optional<AssetAdministrationShellDescriptor> descriptorOpt = getAssetAdministrationShellDescriptorById(
				aasIdentifier);
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
			RegistryEvent evt = RegistryEvent.builder().id(aasIdentifier).submodelId(submodel.getIdentification())
					.type(EventType.SUBMODEL_REGISTERED).submodelDescriptor(submodel).build();
			listener.onEvent(evt);
			return true;
		}
		return false;
	}

	@Override
	public boolean unregisterSubmodelDescriptorById(@NotNull @NonNull String aasIdentifier,
			@NotNull @NonNull String subModelId) {
		Result result =  atomicRepoAccess.removeAssetAdministrationSubmodel(aasIdentifier, subModelId);
		if (result == Result.UPDATED) {
			RegistryEvent evt = RegistryEvent.builder().id(aasIdentifier).submodelId(subModelId)
					.type(EventType.SUBMODEL_UNREGISTERED).build();
			listener.onEvent(evt);
			return true;
		}
		return false;
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
