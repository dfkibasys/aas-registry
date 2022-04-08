package org.eclipse.basyx.aas.registry.service.storage;

import java.util.List;

import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.basyx.aas.registry.model.SubmodelDescriptor;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@RequiredArgsConstructor
public class CloningAasRegistryStorageDecorator implements AasRegistryStorage {

	@Delegate
	private final AasRegistryStorage storage;

	@Override
	public List<AssetAdministrationShellDescriptor> getAllAasDesriptors() {
		return DescriptorCopies.deepCloneCollection(storage.getAllAasDesriptors());
	}

	@Override
	public AssetAdministrationShellDescriptor getAasDescriptor(@NonNull String aasId) {
		return DescriptorCopies.deepClone(storage.getAasDescriptor(aasId));
	}

	@Override
	public void addOrReplaceAasDescriptor(@NonNull AssetAdministrationShellDescriptor descriptor) {
		storage.addOrReplaceAasDescriptor(DescriptorCopies.deepClone(descriptor));
	}

	@Override
	public List<SubmodelDescriptor> getAllSubmodels(@NonNull String aasDescriptorId) {
		return DescriptorCopies.deepCloneCollection(storage.getAllSubmodels(aasDescriptorId));
	}

	@Override
	public SubmodelDescriptor getSubmodel(@NonNull String aasDescriptorId, @NonNull String submodelId) {
		return DescriptorCopies.deepClone(storage.getSubmodel(aasDescriptorId, submodelId));
	}

	@Override
	public void appendOrReplaceSubmodel(@NonNull String aasDescriptorId, @NonNull SubmodelDescriptor submodel) {
		storage.appendOrReplaceSubmodel(aasDescriptorId, DescriptorCopies.deepClone(submodel));
	}
}
