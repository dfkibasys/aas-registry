package org.eclipse.basyx.aas.registry.service;

import java.util.List;
import java.util.Optional;

import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.basyx.aas.registry.model.ShellDescriptorSearchRequest;
import org.eclipse.basyx.aas.registry.model.ShellDescriptorSearchResponse;
import org.eclipse.basyx.aas.registry.model.SubmodelDescriptor;

public interface RegistryService {
    
    boolean existsSubmodelDescriptorById(String aasIdentifier, String submodelIdentifier);

    List<AssetAdministrationShellDescriptor> getAllAssetAdministrationShellDescriptors();
    
    Optional<AssetAdministrationShellDescriptor> getAssetAdministrationShellDescriptorById(String aasIdentifier);
    
    AssetAdministrationShellDescriptor registerAssetAdministrationShellDescriptor(AssetAdministrationShellDescriptor descriptor);
    
    boolean unregisterAssetAdministrationShellDescriptorById(String aasIdentifier);

    Optional<List<SubmodelDescriptor>> getAllSubmodelDescriptors(String aasIdentifier);
    
    Optional<SubmodelDescriptor> getSubmodelDescriptorById(String aasIdentifier, String submodelIdentifier);
    // returns false if aas could not be resolved,
    boolean registerSubmodelDescriptor(String aasIdentifier, SubmodelDescriptor submodel);
    
    // returns false if aas could not be resolved
    boolean unregisterSubmodelDescriptorById(String aasIdentifier, String submodelIdentifier);

    ShellDescriptorSearchResponse searchAssetAdministrationShellDescriptors(ShellDescriptorSearchRequest request);

	void unregisterAllAssetAdministrationShellDescriptors();
	
}
