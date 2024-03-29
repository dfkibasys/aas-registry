/*******************************************************************************
 * Copyright (C) 2022 DFKI GmbH
 * Author: Gerhard Sonnenberg (gerhard.sonnenberg@dfki.de)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * SPDX-License-Identifier: MIT
 ******************************************************************************/
package de.dfki.cos.basys.aas.registry.service.api;

import java.util.List;
import java.util.Objects;

import javax.validation.Valid;

import de.dfki.cos.basys.aas.registry.events.RegistryEventSink;
import de.dfki.cos.basys.aas.registry.model.AssetAdministrationShellDescriptor;
import de.dfki.cos.basys.aas.registry.model.Result;
import de.dfki.cos.basys.aas.registry.model.SubmodelDescriptor;
import de.dfki.cos.basys.aas.registry.service.errors.PathParamAndBodyIdentifierDifferException;
import de.dfki.cos.basys.aas.registry.service.storage.AasRegistryStorage;
import de.dfki.cos.basys.aas.registry.service.storage.RegistrationEventSendingAasRegistryStorage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class BasyxRegistryApiDelegate implements ShellDescriptorsApiDelegate {

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
	public ResponseEntity<SubmodelDescriptor> postSubmodelDescriptor(SubmodelDescriptor body, String aasIdentifier) {
		storage.appendOrReplaceSubmodel(aasIdentifier, body);
		return new ResponseEntity<>(body, HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<Void> putAssetAdministrationShellDescriptorById(AssetAdministrationShellDescriptor body, String aasIdentifier) {
		String bodyId = body.getIdentification();
		if (!Objects.equals(aasIdentifier, bodyId)) {
			throw new PathParamAndBodyIdentifierDifferException(aasIdentifier, bodyId);
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
	public ResponseEntity<Void> putSubmodelDescriptorById(@Valid SubmodelDescriptor body, String aasIdentifier, String submodelIdentifier) {
		String bodyId = body.getIdentification();
		if (!Objects.equals(submodelIdentifier, bodyId)) {
			throw new PathParamAndBodyIdentifierDifferException(aasIdentifier, bodyId);
		}
		storage.appendOrReplaceSubmodel(aasIdentifier, body);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	

	@Override
	public ResponseEntity<Void> deleteAllShellDescriptors() {
		storage.clear();
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

}