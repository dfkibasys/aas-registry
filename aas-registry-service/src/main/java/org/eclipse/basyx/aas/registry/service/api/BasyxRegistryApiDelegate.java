package org.eclipse.basyx.aas.registry.service.api;

import java.util.List;
import java.util.Objects;

import javax.validation.Valid;

import org.eclipse.basyx.aas.registry.events.RegistryEventSink;
import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.basyx.aas.registry.model.ShellDescriptorSearchRequest;
import org.eclipse.basyx.aas.registry.model.ShellDescriptorSearchResponse;
import org.eclipse.basyx.aas.registry.model.SubmodelDescriptor;
import org.eclipse.basyx.aas.registry.service.storage.AasRegistryStorage;
import org.eclipse.basyx.aas.registry.service.storage.RegistrationEventSendingAasRegistryStorage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class BasyxRegistryApiDelegate implements RegistryApiDelegate {

	private final AasRegistryStorage storage;

	public BasyxRegistryApiDelegate(AasRegistryStorage storage, RegistryEventSink eventSink) {
		this.storage = new RegistrationEventSendingAasRegistryStorage(storage, eventSink);
	}

	@Override
	public ResponseEntity<Void> deleteAssetAdministrationShellDescriptorById(String aasIdentifier) {
		if (aasIdentifier != null) {
			storage.removeAasDescriptor(aasIdentifier);
		}
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Void> deleteSubmodelDescriptorById(String aasIdentifier, String submodelIdentifier) {
		storage.removeSubmodel(aasIdentifier, submodelIdentifier);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<List<AssetAdministrationShellDescriptor>> getAllAssetAdministrationShellDescriptors() {
		List<AssetAdministrationShellDescriptor> result = storage.getAllAasDesriptors();
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<SubmodelDescriptor>> getAllSubmodelDescriptors(String aasIdentifier) {
		List<SubmodelDescriptor> result = storage.getAllSubmodels(aasIdentifier);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<AssetAdministrationShellDescriptor> getAssetAdministrationShellDescriptorById(String aasIdentifier) {
		AssetAdministrationShellDescriptor result = storage.getAasDescriptor(aasIdentifier);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<SubmodelDescriptor> getSubmodelDescriptorById(String aasIdentifier, String submodelIdentifier) {
		SubmodelDescriptor result = storage.getSubmodel(aasIdentifier, submodelIdentifier);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<SubmodelDescriptor> postSubmodelDescriptor(String aasIdentifier, @Valid SubmodelDescriptor body) {
		storage.appendOrReplaceSubmodel(aasIdentifier, body);
		return new ResponseEntity<>(body, HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<Void> putAssetAdministrationShellDescriptorById(String aasIdentifier, @Valid AssetAdministrationShellDescriptor body) {
		if (!Objects.equals(aasIdentifier, body.getIdentification())) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		// override if existing
		storage.addOrReplaceAasDescriptor(body);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<AssetAdministrationShellDescriptor> postAssetAdministrationShellDescriptor(@Valid AssetAdministrationShellDescriptor body) {
		storage.addOrReplaceAasDescriptor(body);
		// for now we just return the input object as there is no specific operation
		// like merge (would perhaps be a patch operation then)
		// or something similar, just an insertion or replacement
		return new ResponseEntity<>(body, HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<Void> putSubmodelDescriptorById(String aasIdentifier, String submodelIdentifier, @Valid SubmodelDescriptor body) {
		if (!Objects.equals(submodelIdentifier, body.getIdentification())) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		storage.appendOrReplaceSubmodel(aasIdentifier, body);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<ShellDescriptorSearchResponse> searchShellDescriptors(ShellDescriptorSearchRequest request) {
		ShellDescriptorSearchResponse result = storage.searchAasDescriptors(request);
		return ResponseEntity.ok(result);
	}

	@Override
	public ResponseEntity<Void> deleteAllShellDescriptors() {
		storage.clear();
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}