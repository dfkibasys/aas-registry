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
package de.dfki.cos.basys.aas.registry.service.storage.memory;

import java.util.List;
import java.util.Set;

import javax.validation.Valid;

import de.dfki.cos.basys.aas.registry.model.AssetAdministrationShellDescriptor;
import de.dfki.cos.basys.aas.registry.model.ShellDescriptorSearchRequest;
import de.dfki.cos.basys.aas.registry.model.ShellDescriptorSearchResponse;
import de.dfki.cos.basys.aas.registry.model.SubmodelDescriptor;
import de.dfki.cos.basys.aas.registry.service.errors.AasDescriptorAlreadyExistsException;
import de.dfki.cos.basys.aas.registry.service.errors.AasDescriptorNotFoundException;
import de.dfki.cos.basys.aas.registry.service.errors.SubmodelNotFoundException;
import de.dfki.cos.basys.aas.registry.service.storage.AasRegistryStorage;
import de.dfki.cos.basys.aas.registry.service.storage.CursorResult;
import de.dfki.cos.basys.aas.registry.service.storage.DescriptorFilter;
import de.dfki.cos.basys.aas.registry.service.storage.PaginationInfo;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ThreadSafeAasRegistryStorageDecorator implements AasRegistryStorage {

	private final AasRegistryStorage storage;

	private final ThreadSafeAccess access = new ThreadSafeAccess();

	@Override
	public boolean containsSubmodel(@NonNull String aasDescriptorId, @NonNull String submodelId) {
		return access.read(storage::containsSubmodel, aasDescriptorId, submodelId);
	}

	@Override
	public CursorResult<List<AssetAdministrationShellDescriptor>> getAllAasDescriptors(@NonNull PaginationInfo pRequest, @NonNull DescriptorFilter filter) {	
		return access.read(storage::getAllAasDescriptors, pRequest, filter);
	}

	@Override
	public void removeAasDescriptor(@NonNull String aasDescriptorId) {
		access.write(storage::removeAasDescriptor, aasDescriptorId);
	}

	@Override
	public AssetAdministrationShellDescriptor getAasDescriptor(@NonNull String aasDescriptorId) throws AasDescriptorNotFoundException {
		return access.read(storage::getAasDescriptor, aasDescriptorId);
	}
	
	@Override
	public CursorResult<List<SubmodelDescriptor>> getAllSubmodels(@NonNull String aasDescriptorId, @NonNull PaginationInfo pRequest) throws AasDescriptorNotFoundException {	
		return access.read(storage::getAllSubmodels, aasDescriptorId, pRequest);
	}

	@Override
	public SubmodelDescriptor getSubmodel(@NonNull String aasDescriptorId, @NonNull String submodelId) {
		return access.read(storage::getSubmodel, aasDescriptorId, submodelId);
	}

	@Override
	public void insertSubmodel(@NonNull String aasDescriptorId, @NonNull SubmodelDescriptor submodel) {
		access.write(storage::insertSubmodel, aasDescriptorId, submodel);
	}

	@Override
	public void removeSubmodel(@NonNull String aasDescrId, @NonNull String submodelId) {
		 access.write(storage::removeSubmodel, aasDescrId, submodelId);
	}

	@Override
	public Set<String> clear() {
		return access.write(storage::clear);
	}

	@Override
	public ShellDescriptorSearchResponse searchAasDescriptors(ShellDescriptorSearchRequest request) {
		return access.read(storage::searchAasDescriptors, request);
	}

	@Override
	public void insertAasDescriptor(@Valid AssetAdministrationShellDescriptor descr) throws AasDescriptorAlreadyExistsException {
		access.write(storage::insertAasDescriptor, descr);
	}

	@Override
	public void replaceAasDescriptor(@NonNull String aasDescritorId, @NonNull AssetAdministrationShellDescriptor descriptor) throws AasDescriptorNotFoundException {
		access.write(storage::replaceAasDescriptor, aasDescritorId, descriptor);
	}

	@Override
	public void replaceSubmodel(@NonNull String aasDescriptorId, @NonNull String submodelId, @NonNull SubmodelDescriptor submodel) throws AasDescriptorNotFoundException, SubmodelNotFoundException {
		access.write(storage::replaceSubmodel, aasDescriptorId, submodelId, submodel);
	}
}