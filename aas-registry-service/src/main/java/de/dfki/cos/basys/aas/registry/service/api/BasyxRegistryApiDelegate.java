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

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import de.dfki.cos.basys.aas.registry.events.RegistryEventSink;
import de.dfki.cos.basys.aas.registry.model.AssetAdministrationShellDescriptor;
import de.dfki.cos.basys.aas.registry.model.AssetKind;
import de.dfki.cos.basys.aas.registry.model.GetAssetAdministrationShellDescriptorsResult;
import de.dfki.cos.basys.aas.registry.model.GetSubmodelDescriptorsResult;
import de.dfki.cos.basys.aas.registry.model.PagedResultPagingMetadata;
import de.dfki.cos.basys.aas.registry.model.SubmodelDescriptor;
import de.dfki.cos.basys.aas.registry.service.storage.AasRegistryStorage;
import de.dfki.cos.basys.aas.registry.service.storage.CursorResult;
import de.dfki.cos.basys.aas.registry.service.storage.DescriptorFilter;
import de.dfki.cos.basys.aas.registry.service.storage.PaginationInfo;
import de.dfki.cos.basys.aas.registry.service.storage.RegistrationEventSendingAasRegistryStorage;

@Component
public class BasyxRegistryApiDelegate implements ShellDescriptorsApiDelegate {

	private final AasRegistryStorage storage;

	public BasyxRegistryApiDelegate(AasRegistryStorage storage, RegistryEventSink eventSink) {
		this.storage = new RegistrationEventSendingAasRegistryStorage(storage, eventSink);
	}

	@Override
	public ResponseEntity<Void> deleteAssetAdministrationShellDescriptorById(String aasIdentifier) {
		storage.removeAasDescriptor(aasIdentifier);		
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		
	}

	@Override
	public ResponseEntity<Void> deleteSubmodelDescriptorByIdThroughSuperpath(String aasIdentifier, String submodelIdentifier) {
		storage.removeSubmodel(aasIdentifier, submodelIdentifier);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<GetAssetAdministrationShellDescriptorsResult> getAllAssetAdministrationShellDescriptors(Integer limit, String cursor, AssetKind assetKind, String assetType) {
		
		PaginationInfo pInfo = new PaginationInfo(limit, decodeCursor(cursor));
		DescriptorFilter filter = new DescriptorFilter(assetKind, assetType);
		CursorResult<List<AssetAdministrationShellDescriptor>> allDescriptors = storage.getAllAasDescriptors(pInfo, filter);

		GetAssetAdministrationShellDescriptorsResult result = new GetAssetAdministrationShellDescriptorsResult();		
		result.setPagingMetadata(resolvePagingMeta(allDescriptors));
		result.setResult(allDescriptors.getResult());

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	

	@Override
	public ResponseEntity<GetSubmodelDescriptorsResult> getAllSubmodelDescriptorsThroughSuperpath(String aasIdentifier, Integer limit, String cursor) {
		PaginationInfo pInfo = new PaginationInfo(limit, decodeCursor(cursor));
		CursorResult<List<SubmodelDescriptor>> allSubmodels = storage.getAllSubmodels(aasIdentifier, pInfo);
		
		GetSubmodelDescriptorsResult result = new GetSubmodelDescriptorsResult();
		result.setPagingMetadata(resolvePagingMeta(allSubmodels));
		result.setResult(allSubmodels.getResult());
		
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<AssetAdministrationShellDescriptor> getAssetAdministrationShellDescriptorById(String aasIdentifier) {
		AssetAdministrationShellDescriptor result = storage.getAasDescriptor(aasIdentifier);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<SubmodelDescriptor> getSubmodelDescriptorByIdThroughSuperpath(String aasIdentifier, String submodelIdentifier) {
		SubmodelDescriptor result = storage.getSubmodel(aasIdentifier, submodelIdentifier);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<SubmodelDescriptor> postSubmodelDescriptorThroughSuperpath(SubmodelDescriptor body, String aasIdentifier) {
		storage.insertSubmodel(aasIdentifier, body);
		return new ResponseEntity<>(body, HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<Void> putAssetAdministrationShellDescriptorById(AssetAdministrationShellDescriptor body, String aasIdentifier) {
		storage.replaceAasDescriptor(aasIdentifier, body);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<AssetAdministrationShellDescriptor> postAssetAdministrationShellDescriptor(@Valid AssetAdministrationShellDescriptor body) {
		storage.insertAasDescriptor(body);
		return new ResponseEntity<>(body, HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<Void> putSubmodelDescriptorByIdThroughSuperpath(SubmodelDescriptor descriptor, String aasIdentifier, String submodelIdentifier) {
		storage.replaceSubmodel(aasIdentifier, submodelIdentifier, descriptor);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Void> deleteAllShellDescriptors() {
		storage.clear();
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	

	private <T> PagedResultPagingMetadata resolvePagingMeta(CursorResult<T> result) {
		PagedResultPagingMetadata meta = new PagedResultPagingMetadata();
		String encodedCursor = encodeCursor(result.getCursor());
		meta.setCursor(encodedCursor);
		return meta;
	}
	
	
	// we encode and decode the cursor as it is passed as url param and could hava invalid chars
	private String encodeCursor(String cursor) {
		if (cursor == null) {
			return null;
		}
		return URLEncoder.encode(cursor, StandardCharsets.UTF_8);
	}
	
	private String decodeCursor(String cursor) {
		if (cursor == null) {
			return null;
		}
		return URLDecoder.decode(cursor, StandardCharsets.UTF_8);
	}

}