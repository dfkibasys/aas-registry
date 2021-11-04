package org.eclipse.basyx.aas.registry.service;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.basyx.aas.registry.model.SubmodelDescriptor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

public interface RegistryService {

    boolean existsAssetAdministrationShellDescriptorById(String aasIdentifier);
    boolean existsSubmodelDescriptorById(String aasIdentifier, String submodelIdentifier);

    List<AssetAdministrationShellDescriptor> getAllAssetAdministrationShellDescriptors();
    Optional<AssetAdministrationShellDescriptor> getAssetAdministrationShellDescriptorById(String aasIdentifier);
    AssetAdministrationShellDescriptor registerAssetAdministrationShellDescriptor(AssetAdministrationShellDescriptor body);
    void unregisterAssetAdministrationShellDescriptorById(String aasIdentifier);

    List<SubmodelDescriptor> getAllSubmodelDescriptors(String aasIdentifier);
    Optional<SubmodelDescriptor> getSubmodelDescriptorById(String aasIdentifier, String submodelIdentifier);
    SubmodelDescriptor registerSubmodelDescriptor(String aasIdentifier, SubmodelDescriptor body);
    void unregisterSubmodelDescriptorById(String aasIdentifier, String submodelIdentifier);

}
