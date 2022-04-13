package org.eclipse.basyx.aas.registry.service.storage;

import java.util.List;
import java.util.Set;

import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.basyx.aas.registry.model.ShellDescriptorSearchRequest;
import org.eclipse.basyx.aas.registry.model.ShellDescriptorSearchResponse;
import org.eclipse.basyx.aas.registry.model.SubmodelDescriptor;

import lombok.NonNull;

public interface AasRegistryStorage {

	boolean containsSubmodel(@NonNull String aasDescriptorId, String submodelId);

	List<AssetAdministrationShellDescriptor> getAllAasDesriptors();

	AssetAdministrationShellDescriptor getAasDescriptor(@NonNull String aasDescriptorId) throws AasDescriptorNotFoundException;

	void addOrReplaceAasDescriptor(@NonNull AssetAdministrationShellDescriptor descriptor);

	boolean removeAasDescriptor(@NonNull String aasDescriptorId);

	List<SubmodelDescriptor> getAllSubmodels(@NonNull String aasDescriptorId) throws AasDescriptorNotFoundException;

	SubmodelDescriptor getSubmodel(@NonNull String aasDescriptorId, @NonNull String submodelId) throws AasDescriptorNotFoundException, SubmodelNotFoundException;

	void appendOrReplaceSubmodel(@NonNull String aasDescriptorId, @NonNull SubmodelDescriptor submodel) throws AasDescriptorNotFoundException;

	boolean removeSubmodel(@NonNull String aasDescriptorId, @NonNull String submodelId);

	Set<String> clear();

	ShellDescriptorSearchResponse searchAasDescriptors(@NonNull ShellDescriptorSearchRequest request);

}
