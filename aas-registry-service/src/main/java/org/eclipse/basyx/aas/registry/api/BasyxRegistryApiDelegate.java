package org.eclipse.basyx.aas.registry.api;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.validation.Valid;

import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.basyx.aas.registry.model.ShellDescriptorSearchQuery;
import org.eclipse.basyx.aas.registry.model.ShellDescriptorSearchResponse;
import org.eclipse.basyx.aas.registry.model.SubmodelDescriptor;
import org.eclipse.basyx.aas.registry.service.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class BasyxRegistryApiDelegate implements RegistryApiDelegate {

	private final RegistryService service;

	@Autowired
	public BasyxRegistryApiDelegate(RegistryService service) {
		this.service = service;
	}

	@Override
	public ResponseEntity<Void> deleteAssetAdministrationShellDescriptorById(String aasIdentifier) {
		if (aasIdentifier != null) {
			service.unregisterAssetAdministrationShellDescriptorById(aasIdentifier);
		}
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Void> deleteSubmodelDescriptorById(String aasIdentifier, String submodelIdentifier) {
		if (aasIdentifier != null && submodelIdentifier != null) {
			service.unregisterSubmodelDescriptorById(aasIdentifier, submodelIdentifier);
		}
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<List<AssetAdministrationShellDescriptor>> getAllAssetAdministrationShellDescriptors() {
		List<AssetAdministrationShellDescriptor> result = service.getAllAssetAdministrationShellDescriptors();
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<SubmodelDescriptor>> getAllSubmodelDescriptors(String aasIdentifier) {
		Optional<List<SubmodelDescriptor>> resultOpt = service.getAllSubmodelDescriptors(aasIdentifier);
		if (resultOpt.isPresent()) {
			List<SubmodelDescriptor> result = resultOpt.get();
			return new ResponseEntity<>(result, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Override
	public ResponseEntity<AssetAdministrationShellDescriptor> getAssetAdministrationShellDescriptorById(
			String aasIdentifier) {
		Optional<AssetAdministrationShellDescriptor> result = service
				.getAssetAdministrationShellDescriptorById(aasIdentifier);
		if (result.isPresent()) {
			return new ResponseEntity<>(result.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Override
	public ResponseEntity<SubmodelDescriptor> getSubmodelDescriptorById(String aasIdentifier,
			String submodelIdentifier) {
		Optional<SubmodelDescriptor> resultOpt = service.getSubmodelDescriptorById(aasIdentifier, submodelIdentifier);
		if (resultOpt.isPresent()) {
			SubmodelDescriptor result = resultOpt.get();
			return new ResponseEntity<>(result, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Override
	public ResponseEntity<SubmodelDescriptor> postSubmodelDescriptor(String aasIdentifier,
			@Valid SubmodelDescriptor body) {
		boolean success = service.registerSubmodelDescriptor(aasIdentifier, body);
		if (success) {
			return new ResponseEntity<>(body, HttpStatus.CREATED);
		} else { // aas not found
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Override
	public ResponseEntity<Void> putAssetAdministrationShellDescriptorById(String aasIdentifier,
			@Valid AssetAdministrationShellDescriptor body) {
		if (!Objects.equals(aasIdentifier, body.getIdentification())) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		// override if existing
		service.registerAssetAdministrationShellDescriptor(body);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<AssetAdministrationShellDescriptor> postAssetAdministrationShellDescriptor(
			@Valid AssetAdministrationShellDescriptor body) {
		AssetAdministrationShellDescriptor descriptor = service.registerAssetAdministrationShellDescriptor(body);
		return new ResponseEntity<>(descriptor, HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<Void> putSubmodelDescriptorById(String aasIdentifier, String submodelIdentifier,
			@Valid SubmodelDescriptor body) {
		if (!Objects.equals(submodelIdentifier, body.getIdentification())) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		boolean success = service.registerSubmodelDescriptor(aasIdentifier, body);
		if (success) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} else { // aas not found
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Override
	public ResponseEntity<ShellDescriptorSearchResponse> searchShellDescriptors(ShellDescriptorSearchQuery query) {
		ShellDescriptorSearchResponse result = service.searchAssetAdministrationShellDescriptors(query);
		return ResponseEntity.ok(result);
	}
}