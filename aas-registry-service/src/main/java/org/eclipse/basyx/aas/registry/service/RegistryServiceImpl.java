package org.eclipse.basyx.aas.registry.service;

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptorEnvelop;
import org.eclipse.basyx.aas.registry.model.SubmodelDescriptor;
import org.eclipse.basyx.aas.registry.model.event.RegistryEvent;
import org.eclipse.basyx.aas.registry.model.event.RegistryEvent.EventType;
import org.eclipse.basyx.aas.registry.repository.AssetAdministrationShellDescriptorRepository;
import org.eclipse.basyx.aas.registry.model.event.RegistryEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.NonNull;

@Service
public class RegistryServiceImpl implements RegistryService {

	private static final String SUBMODEL_ID_IS_IS_NULL = "Submodel id is is null!";

	private static final String AAS_ID_IS_NULL = "Aas id is null!";

	@Autowired
	private AssetAdministrationShellDescriptorRepository aasDescriptorRepository;

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
				.assetAdministrationShellDescriptor(result.getAssetAdministrationShellDescriptor()).build();
		listener.onEvent(evt);

		return result.getAssetAdministrationShellDescriptor();
	}

	@Override
	public boolean unregisterAssetAdministrationShellDescriptorById(@Nullable String aasIdentifier) {
		if (aasIdentifier != null && aasDescriptorRepository.existsById(aasIdentifier)) {
			aasDescriptorRepository.deleteById(aasIdentifier);
			RegistryEvent evt = RegistryEvent.builder().id(aasIdentifier).type(RegistryEvent.EventType.AAS_UNREGISTERED)
					.build();
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

	@Override // TODO should run in a transaction
	public Optional<SubmodelDescriptor> registerSubmodelDescriptor(@NotNull @NonNull String aasIdentifier,
			@NotNull @NonNull SubmodelDescriptor submodel) {
		String subModelId = Objects.requireNonNull(submodel.getIdentification(), SUBMODEL_ID_IS_IS_NULL);
		// we do not have a real transactional behaviour here, because we could have
		// multiple spring apps
		// but just one elasticsearch repo
		// FIXME check if we can use update scripts or if we should abort on conflict
		// and retry on client-side
		// ->
		// https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-document-update.html#_updates_with_a_script
		synchronized (RegistryServiceImpl.class) {
			Optional<AssetAdministrationShellDescriptor> descriptorOpt = aasDescriptorRepository.findById(aasIdentifier)
					.map(AssetAdministrationShellDescriptorEnvelop::getAssetAdministrationShellDescriptor);
			if (descriptorOpt.isPresent()) {
				AssetAdministrationShellDescriptor descriptor = descriptorOpt.get();

				List<SubmodelDescriptor> subModels = descriptor.getSubmodelDescriptors();
				replaceOrAppendModel(subModels, subModelId, submodel);

				AssetAdministrationShellDescriptorEnvelop result = aasDescriptorRepository
						.save(new AssetAdministrationShellDescriptorEnvelop(descriptor));

				RegistryEvent evt = RegistryEvent.builder().id(subModelId).type(EventType.SUBMODEL_REGISTERED)
						.assetAdministrationShellDescriptor(result.getAssetAdministrationShellDescriptor()).build();
				listener.onEvent(evt);
				return Optional.of(submodel);
			} else {
				return Optional.empty();
			}
		}
	}

	@Override
	public boolean unregisterSubmodelDescriptorById(@NotNull @NonNull String aasIdentifier,
			@NotNull @NonNull String subModelId) {
		synchronized (RegistryServiceImpl.class) {
			// we do not have a real transactional behaviour here, because we could have
			// multiple spring apps
			// but just one elasticsearch repo
			// FIXME check if we can use update scripts or if we should abort on conflict
			// and retry on client-side
			// ->
			// https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-document-update.html#_updates_with_a_script
			Optional<AssetAdministrationShellDescriptor> descriptorOpt = aasDescriptorRepository.findById(aasIdentifier)
					.map(AssetAdministrationShellDescriptorEnvelop::getAssetAdministrationShellDescriptor);
			if (descriptorOpt.isPresent()) {
				AssetAdministrationShellDescriptor descriptor = descriptorOpt.get();
				boolean success = descriptor.getSubmodelDescriptors()
						.removeIf(submodelDescriptor -> submodelDescriptor.getIdentification().equals(subModelId));
				if (success) {
					AssetAdministrationShellDescriptorEnvelop result = aasDescriptorRepository
							.save(new AssetAdministrationShellDescriptorEnvelop(descriptor));
					RegistryEvent evt = RegistryEvent.builder().id(subModelId)
							.type(RegistryEvent.EventType.SUBMODEL_UNREGISTERED)
							.assetAdministrationShellDescriptor(result.getAssetAdministrationShellDescriptor()).build();
					listener.onEvent(evt);
				}
				return success;
			}
			return false;
		}
	}

	private void replaceOrAppendModel(List<SubmodelDescriptor> subModels, String subModelId,
			SubmodelDescriptor subModel) {
		boolean overridden = replaceModelIfExisting(subModels, subModelId, subModel);
		if (!overridden) {
			subModels.add(subModel);
		}
	}

	private boolean replaceModelIfExisting(List<SubmodelDescriptor> subModels, String subModelId,
			SubmodelDescriptor subModel) {
		ListIterator<SubmodelDescriptor> modelIter = subModels.listIterator();
		while (modelIter.hasNext()) {
			SubmodelDescriptor model = modelIter.next();
			String modelId = model.getIdentification();
			if (Objects.equals(subModelId, modelId)) {
				modelIter.remove();
				modelIter.add(subModel);
				return true;
			}
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
