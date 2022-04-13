package org.eclipse.basyx.aas.registry.service.tests;

import java.util.List;

import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.basyx.aas.registry.model.SubmodelDescriptor;
import org.eclipse.basyx.aas.registry.service.storage.AasRegistryStorage;
import org.eclipse.basyx.aas.registry.service.storage.DescriptorCopies;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

//performs additional cloning for in memory tests
//altering the objects during tests will then not affect the storage
@RequiredArgsConstructor
public class CloningAasRegistryStorageDecorator implements AasRegistryStorage {

	@Delegate
	private final AasRegistryStorage storage;

	@Override
	public List<AssetAdministrationShellDescriptor> getAllAasDesriptors() {
		return DescriptorCopies.deepCloneCollection(storage.getAllAasDesriptors());
	}

	@Override
	public AssetAdministrationShellDescriptor getAasDescriptor(String aasId) {
		return DescriptorCopies.deepClone(storage.getAasDescriptor(aasId));
	}

	@Override
	public void addOrReplaceAasDescriptor(AssetAdministrationShellDescriptor descriptor) {
		storage.addOrReplaceAasDescriptor(DescriptorCopies.deepClone(descriptor));
	}

	@Override
	public List<SubmodelDescriptor> getAllSubmodels(String aasDescriptorId) {
		return DescriptorCopies.deepCloneCollection(storage.getAllSubmodels(aasDescriptorId));
	}

	@Override
	public SubmodelDescriptor getSubmodel(String aasDescriptorId, String submodelId) {
		return DescriptorCopies.deepClone(storage.getSubmodel(aasDescriptorId, submodelId));
	}

	@Override
	public void appendOrReplaceSubmodel(String aasDescriptorId, SubmodelDescriptor submodel) {
		storage.appendOrReplaceSubmodel(aasDescriptorId, DescriptorCopies.deepClone(submodel));
	}
}
