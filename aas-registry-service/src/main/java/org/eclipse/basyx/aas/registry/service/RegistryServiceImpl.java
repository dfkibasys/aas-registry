package org.eclipse.basyx.aas.registry.service;

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.basyx.aas.registry.event.RegistryEvent;
import org.eclipse.basyx.aas.registry.event.RegistryEvent.EventType;
import org.eclipse.basyx.aas.registry.event.RegistryEventListener;
import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptorEnvelop;
import org.eclipse.basyx.aas.registry.model.Identifier;
import org.eclipse.basyx.aas.registry.model.KeyType;
import org.eclipse.basyx.aas.registry.model.SubmodelDescriptor;
import org.eclipse.basyx.aas.registry.repository.AssetAdministrationShellDescriptorRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegistryServiceImpl implements RegistryService {

	private final AssetAdministrationShellDescriptorRepository aasDescriptorRepository;

	private final RegistryEventListener listener;

	@Override
	public boolean existsAssetAdministrationShellDescriptorById(String aasIdentifier) {
		return aasDescriptorRepository.existsById(aasIdentifier);
	}

	@Override
	public boolean existsSubmodelDescriptorById(String aasIdentifier, String submodelIdentifier) {
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
			String aasIdentifier) {
		return aasDescriptorRepository.findById(aasIdentifier)
				.map(AssetAdministrationShellDescriptorEnvelop::getAssetAdministrationShellDescriptor);
	}

	@Override
	public AssetAdministrationShellDescriptor registerAssetAdministrationShellDescriptor(
			AssetAdministrationShellDescriptor descriptor) {
		if (descriptor == null) {
			throw new IllegalArgumentException("Argument is null");
		}
		Identifier identifier = descriptor.getIdentification();
		if (identifier == null || identifier.getId() == null) {
			throw new IllegalArgumentException("Id was null");
		}
		AssetAdministrationShellDescriptorEnvelop envelop = new AssetAdministrationShellDescriptorEnvelop(descriptor);
		AssetAdministrationShellDescriptorEnvelop result = aasDescriptorRepository.save(envelop);

		RegistryEvent evt = RegistryEvent.builder().id(result.getId()).type(RegistryEvent.EventType.AAS_REGISTERED)
				.assetAdministrationShellDescriptor(result.getAssetAdministrationShellDescriptor()).build();
		listener.onEvent(evt);

		return result.getAssetAdministrationShellDescriptor();
	}

	@Override
	public boolean unregisterAssetAdministrationShellDescriptorById(String aasIdentifier) {
		if (aasIdentifier == null) {
			return false;
		}
		if (aasDescriptorRepository.existsById(aasIdentifier)) {
			aasDescriptorRepository.deleteById(aasIdentifier);
			RegistryEvent evt = RegistryEvent.builder().id(aasIdentifier).type(RegistryEvent.EventType.AAS_UNREGISTERED)
					.build();
			listener.onEvent(evt);
			return true;
		}
		return false;
	}

	@Override
	public Optional<List<SubmodelDescriptor>> getAllSubmodelDescriptors(String aasIdentifier) {
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
	public Optional<SubmodelDescriptor> getSubmodelDescriptorById(String aasIdentifier, String submodelIdentifier) {
		SubmodelDescriptorIdMatcher matcher = new SubmodelDescriptorIdMatcher(submodelIdentifier);
		return getAssetAdministrationShellDescriptorById(aasIdentifier)
				.map(AssetAdministrationShellDescriptor::getSubmodelDescriptors).stream().flatMap(List::stream)
				.filter(matcher::matches).findFirst();
	}

	@Override // TODO should run in a transaction
	public Optional<SubmodelDescriptor> registerSubmodelDescriptor(String aasIdentifier, SubmodelDescriptor submodel) {
		if (aasIdentifier == null || submodel == null) {
			throw new IllegalArgumentException("Argument was null.");
		}
		String subModelId = Optional.ofNullable(submodel.getIdentification()).map(Identifier::getId).orElse(null);
		if (subModelId == null) {
			throw new IllegalArgumentException("Submodel id was not set.");
		}
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
	public boolean unregisterSubmodelDescriptorById(String aasIdentifier, String subModelId) {
		if (aasIdentifier == null || subModelId == null) {
			return false;
		}
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
				boolean success = descriptor.getSubmodelDescriptors().removeIf(
						submodelDescriptor -> submodelDescriptor.getIdentification().getId().equals(subModelId));
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
			String modelId = Optional.ofNullable(model.getIdentification()).map(Identifier::getId).orElse(null);
			if (subModelId.equals(modelId)) {
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
			return descriptor.getIdentification().getId().equals(subModelIdentifier);
		}
	}

}
