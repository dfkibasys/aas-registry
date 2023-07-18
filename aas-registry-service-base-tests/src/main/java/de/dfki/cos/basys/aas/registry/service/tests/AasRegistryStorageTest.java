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
package de.dfki.cos.basys.aas.registry.service.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import de.dfki.cos.basys.aas.registry.model.AssetAdministrationShellDescriptor;
import de.dfki.cos.basys.aas.registry.model.AssetKind;
import de.dfki.cos.basys.aas.registry.model.SubmodelDescriptor;
import de.dfki.cos.basys.aas.registry.service.errors.AasDescriptorAlreadyExistsException;
import de.dfki.cos.basys.aas.registry.service.errors.AasDescriptorNotFoundException;
import de.dfki.cos.basys.aas.registry.service.errors.DescriptorNotFoundException;
import de.dfki.cos.basys.aas.registry.service.errors.SubmodelAlreadyExistsException;
import de.dfki.cos.basys.aas.registry.service.errors.SubmodelNotFoundException;
import de.dfki.cos.basys.aas.registry.service.storage.CursorResult;
import de.dfki.cos.basys.aas.registry.service.storage.DescriptorCopies;


public abstract class AasRegistryStorageTest extends ExtensionsTest {


	@Test
	public void whenRegisterSubmodelDescriptorNullAasId_thenNullPointer() {
		assertNullPointerThrown(() -> storage.insertSubmodel(null, null));
	}

	@Test
	public void whenRegisterSubmodelDescriptorNullModel_thenNullPointer() {
		assertNullPointerThrown(() -> storage.insertSubmodel(IDENTIFICATION_1, null));
	}

	@Test
	public void whenRegisterSubmodelDescriptorNullId_thenNullPointer() {
		SubmodelDescriptor descriptor = RegistryTestObjects.newSubmodelDescriptor(null);
		assertNullPointerThrown(() -> storage.insertSubmodel(IDENTIFICATION_1, descriptor));
	}

	@Test
	public void whenRegisterSubmodelDescriptorUnknownId_thenThrowNotFound() {
		List<AssetAdministrationShellDescriptor> initialState = getAllAasDescriptors();
		SubmodelDescriptor ignored = RegistryTestObjects.newSubmodelDescriptor("ignored");
		assertThrows(AasDescriptorNotFoundException.class, () -> storage.insertSubmodel(UNKNOWN, ignored));
		List<AssetAdministrationShellDescriptor> currentState = getAllAasDescriptors();
		assertThat(currentState).isEqualTo(initialState);
		verifyNoEventSent();
	}

	@Test
	public void whenRegisterSubmodelDescriptorAndWasAlreadyPresent_thenElementIsOverridden() throws IOException {
		List<AssetAdministrationShellDescriptor> initialState = getAllAasDescriptors();
		List<AssetAdministrationShellDescriptor> expected = testResourcesLoader.loadShellDescriptorList();
		assertThat(initialState).isNotEqualTo(expected);
		SubmodelDescriptor toAdd = RegistryTestObjects.newSubmodelDescriptorWithDescription(IDENTIFICATION_2_2, "Overridden");
		storage.replaceSubmodel(IDENTIFICATION_2, toAdd.getId(), toAdd);
		List<AssetAdministrationShellDescriptor> newState = getAllAasDescriptors();
		assertThat(newState).asList().isNotEqualTo(initialState).containsExactlyInAnyOrderElementsOf(expected);
		verifyEventsSent();
	}


	@Test
	public void whenRegisterSubmodelDescriptorAndWasNotAlreadyPresent_thenElementIsAdded() throws IOException {
		List<AssetAdministrationShellDescriptor> initialState = getAllAasDescriptors();
		List<AssetAdministrationShellDescriptor> expected = testResourcesLoader.loadShellDescriptorList();
		assertThat(initialState).isNotEqualTo(expected);
		SubmodelDescriptor toAdd = RegistryTestObjects.newSubmodelDescriptor(IDENTIFICATION_2_3);

		//RegistryTestObjects.addDefaultEndpoint(toAdd);
		storage.insertSubmodel(IDENTIFICATION_2, toAdd);
		List<AssetAdministrationShellDescriptor> newState = getAllAasDescriptors();
		assertThat(newState).asList().isNotEqualTo(initialState).containsExactlyInAnyOrderElementsOf(expected);
		verifyEventsSent();
	}

	@Test
	public void whenUnregisterSubmodelDescriptorNullAdminShell_thenNullPointer() {
		assertNullPointerThrown(() -> storage.removeSubmodel(null, IDENTIFICATION_2_1));
	}

	@Test
	public void whenUnregisterSubmodelDescriptorNullSubmodelId_thenNullPointer() {
		assertNullPointerThrown(() -> storage.removeSubmodel(IDENTIFICATION_2, null));
	}

	@Test
	public void whenUnregisterSubmodelDescriptorAndWasPresent_thenElementIsRemoved() throws IOException {
		List<AssetAdministrationShellDescriptor> initialState = getAllAasDescriptors();
		List<AssetAdministrationShellDescriptor> expected = testResourcesLoader.loadShellDescriptorList();
		assertThat(initialState).isNotEqualTo(expected);
		storage.removeSubmodel(IDENTIFICATION_2, IDENTIFICATION_2_2);

		List<AssetAdministrationShellDescriptor> newState = getAllAasDescriptors();
		assertThat(newState).asList().isNotEqualTo(initialState).containsExactlyInAnyOrderElementsOf(expected);
		verifyEventsSent();

		storage.removeSubmodel(IDENTIFICATION_2, IDENTIFICATION_2_1);

		assertThat(storage.getAasDescriptor(IDENTIFICATION_2).getSubmodelDescriptors()).isNullOrEmpty();

		assertThrows(SubmodelNotFoundException.class, ()-> storage.removeSubmodel(IDENTIFICATION_2, IDENTIFICATION_2_1));
	}

	@Test
	public void whenUnregisterSubmodelDescriptorAndShellWasNotPresent_thenThrowNotFound() {
		List<AssetAdministrationShellDescriptor> initialState = getAllAasDescriptors();

		assertThrows(AasDescriptorNotFoundException.class, ()-> storage.removeSubmodel(UNKNOWN, UNKNOWN_1));
		
		List<AssetAdministrationShellDescriptor> newState = getAllAasDescriptors();
		assertThat(newState).asList().containsExactlyInAnyOrderElementsOf(initialState);
		verifyNoEventSent();
	}

	@Test
	public void whenUnregisterSubmodelDescriptorAndSubmodelWasNotPresent_thenReturnFalse() {
		List<AssetAdministrationShellDescriptor> initialState = getAllAasDescriptors();
		assertThrows(SubmodelNotFoundException.class, ()-> storage.removeSubmodel(IDENTIFICATION_2, _2_UNKNOWN));
		List<AssetAdministrationShellDescriptor> newState = getAllAasDescriptors();
		assertThat(newState).asList().containsExactlyInAnyOrderElementsOf(initialState);
		verifyNoEventSent();
	}


	@Test
	public void whenGetAllAssetAdministrationShellDescriptors_thenAll() throws IOException {
		Collection<AssetAdministrationShellDescriptor> found = getAllAasDescriptors();
		List<AssetAdministrationShellDescriptor> expected = testResourcesLoader.loadShellDescriptorList();
		assertThat(found).containsExactlyInAnyOrderElementsOf(expected);
		verifyNoEventSent();
	}
	
	@Test
	public void whenGetAllAssetAdministrationShellDescriptorsFilteredByType_thenOnlyType() throws IOException {
		Collection<AssetAdministrationShellDescriptor> found = getAllAasDescriptorsFiltered(AssetKind.TYPE, null);
		List<AssetAdministrationShellDescriptor> expected = testResourcesLoader.loadShellDescriptorList();
		assertThat(found).containsExactlyInAnyOrderElementsOf(expected);
		verifyNoEventSent();
	}
	
	@Test
	public void whenGetAllAssetAdministrationShellDescriptorsFilteredByInstance_thenOnlyInstance() throws IOException {
		Collection<AssetAdministrationShellDescriptor> found = getAllAasDescriptorsFiltered(AssetKind.INSTANCE, null);
		List<AssetAdministrationShellDescriptor> expected = testResourcesLoader.loadShellDescriptorList();
		assertThat(found).containsExactlyInAnyOrderElementsOf(expected);
		verifyNoEventSent();
	}
	

	@Test
	public void whenGetAllAssetAdministrationShellDescriptorsFilteredByNotApplicable_thenOnlyNotApplicable() throws IOException {
		Collection<AssetAdministrationShellDescriptor> found = getAllAasDescriptorsFiltered(AssetKind.NOTAPPLICABLE, null);
		List<AssetAdministrationShellDescriptor> expected = testResourcesLoader.loadShellDescriptorList();
		assertThat(found).containsExactlyInAnyOrderElementsOf(expected);
		verifyNoEventSent();
	}
	
	
	@Test
	public void whenGetAllAssetAdministrationShellDescriptorsFilteredByTypeName_thenOnlyMatching() throws IOException {
		Collection<AssetAdministrationShellDescriptor> found = getAllAasDescriptorsFiltered(AssetKind.TYPE, "A");
		List<AssetAdministrationShellDescriptor> expected = testResourcesLoader.loadShellDescriptorList();
		assertThat(found).containsExactlyInAnyOrderElementsOf(expected);
		verifyNoEventSent();
	}
	
	@Test 
	public void whenGetAllAssetAdministrationShellDescriptorsOverTwoPages_thenReturnPageStepByStep() throws IOException {
		CursorResult<List<AssetAdministrationShellDescriptor>> firstResult = getAllAasDescriptorsWithPagination(2, null);
		List<AssetAdministrationShellDescriptor> expected = testResourcesLoader.loadShellDescriptorList();
				
		CursorResult<List<AssetAdministrationShellDescriptor>> secondResult = getAllAasDescriptorsWithPagination(2, firstResult.getCursor());
		assertThat(firstResult.getCursor()).isNotNull();
		assertThat(secondResult.getCursor()).isNull();
		assertThat(firstResult.getResult()).containsExactlyInAnyOrderElementsOf(expected.subList(0, 2));
		assertThat(secondResult.getResult()).containsExactlyInAnyOrderElementsOf(expected.subList(2, 4));
		verifyNoEventSent();
	}
	
	@Test
	public void whenGetAllAssetAdministrationShellDescriptorsAndEmptyRepo_thenEmptyList() {
		clearBaseStorage();
		Collection<AssetAdministrationShellDescriptor> found = getAllAasDescriptors();
		assertThat(found).isEmpty();
		verifyNoEventSent();
	}

	@Test
	public void whenGetAllSubmodelDescriptorsAndNotSet_thenEmptyList() {
		List<SubmodelDescriptor> submodels = getAllSubmodels(IDENTIFICATION_1);
		assertThat(submodels).isEmpty();
		verifyNoEventSent();
	}

	@Test
	public void whenGetAllSubmodelDescriptorsAndNotPresent_throwNotFound() {
		assertThrows(AasDescriptorNotFoundException.class, () -> getAllSubmodels(UNKNOWN));
		verifyNoEventSent();
	}

	@Test
	public void whenGetAllSubmodelDescriptors_thenGot2Elements() throws IOException {
		List<SubmodelDescriptor> found = getAllSubmodels(IDENTIFICATION_2);
		List<SubmodelDescriptor> expected = testResourcesLoader.loadSubmodelList();
		assertThat(found).containsExactlyInAnyOrderElementsOf(expected);
		verifyNoEventSent();
	}
	
	@Test
	public void whenGetAssetAdminstrationShellDescritorByIdAndIdIsNull_thenNullPointer() {
		assertNullPointerThrown(() -> storage.getAasDescriptor(null));
	}

	@Test
	public void whenGetAssetAdminstrationShellDescritorByIdAndUnknown_thenThrowNotFound() {
		assertThrows(AasDescriptorNotFoundException.class, () -> storage.getAasDescriptor(UNKNOWN));
		verifyNoEventSent();
	}

	@Test
	public void whenGetAssetAdminstrationShellDescritorByIdAndAvailable_thenGotResult() throws IOException {
		AssetAdministrationShellDescriptor result = storage.getAasDescriptor(IDENTIFICATION_1);
		AssetAdministrationShellDescriptor expected = testResourcesLoader.loadAssetAdminShellDescriptor();
		assertThat(result).isEqualTo(expected);
		verifyNoEventSent();
	}


	@Test
	public void whenRegisterAssetAdministrationShellDescriptorNullArg_thenNullPointer() {
		assertNullPointerThrown(() -> storage.replaceAasDescriptor(null, null));
	}

	@Test
	public void whenRegisterAssetAdministrationShellDescriptor_thenStored() throws IOException {
		List<AssetAdministrationShellDescriptor> initialState = getAllAasDescriptors();
		List<AssetAdministrationShellDescriptor> expected = testResourcesLoader.loadShellDescriptorList();
		assertThat(initialState).isNotEqualTo(expected);
		AssetAdministrationShellDescriptor testResource = RegistryTestObjects.newAssetAdministrationShellDescriptor(IDENTIFICATION_NEW);
		SubmodelDescriptor subModel = RegistryTestObjects.newSubmodelDescriptor(IDENTIFICATION_NEW_1);
		testResource.setSubmodelDescriptors(Collections.singletonList(subModel));
		storage.insertAasDescriptor(testResource);
		List<AssetAdministrationShellDescriptor> newState = getAllAasDescriptors();
		assertThat(newState).asList().isNotEqualTo(initialState).containsExactlyInAnyOrderElementsOf(expected);
		verifyEventsSent();
	}

	@Test
	public void whenUnregisterAssetAdministrationShellDescriptorByIdAndNullId_thenThrowNotFoundAndNoChanges() {
		List<AssetAdministrationShellDescriptor> initialState = getAllAasDescriptors();
		assertThrows(NullPointerException.class, () -> storage.removeAasDescriptor(null));
		List<AssetAdministrationShellDescriptor> currentState = getAllAasDescriptors();
		assertThat(currentState).asList().containsExactlyInAnyOrderElementsOf(initialState);
		verifyNoEventSent();
	}

	@Test
	public void whenUnregisterAssetAdministrationShellDescriptorById_thenReturnTrueAndEntryRemoved() throws IOException {
		List<AssetAdministrationShellDescriptor> initialState = getAllAasDescriptors();
		List<AssetAdministrationShellDescriptor> expected = testResourcesLoader.loadShellDescriptorList();
		storage.removeAasDescriptor(IDENTIFICATION_2);
		List<AssetAdministrationShellDescriptor> currentState = getAllAasDescriptors();
		assertThat(currentState).asList().isNotEqualTo(initialState).containsExactlyInAnyOrderElementsOf(expected);
		verifyEventsSent();
	}

	@Test
	public void whenUnregisterAssetAdministrationShellDescriptorByIdAndIdUnknon_thenReturnFalsAndNoChanges() throws IOException {
		List<AssetAdministrationShellDescriptor> initialState = getAllAasDescriptors();
		assertThrows(AasDescriptorNotFoundException.class, ()-> storage.removeAasDescriptor(UNKNOWN));
		
		List<AssetAdministrationShellDescriptor> currentState = getAllAasDescriptors();
		assertThat(currentState).asList().containsExactlyInAnyOrderElementsOf(initialState);
		verifyNoEventSent();
	}
	
	@Test
	public void whenRegistrationUpdateForNewId_AvailableUnderNewIdAndTwoEventsFired() throws IOException {
		List<AssetAdministrationShellDescriptor> initialState = getAllAasDescriptors();
		
		AssetAdministrationShellDescriptor descr = initialState.stream().filter(a->a.getId().equals(IDENTIFICATION_2)).findAny().get();
		descr = DescriptorCopies.deepClone(descr);
		descr.setId(IDENTIFICATION_3);
		storage.replaceAasDescriptor(IDENTIFICATION_2, descr);
		
		List<AssetAdministrationShellDescriptor> currentState = getAllAasDescriptors();
		List<AssetAdministrationShellDescriptor> expected = testResourcesLoader.loadShellDescriptorList();
		assertThat(currentState).asList().isNotEqualTo(initialState).containsExactlyInAnyOrderElementsOf(expected);
		
		verifyEventsSent();
	}


	@Test
	public void whenExistsSubmodelDescriptorByIdAndBothArgsNull_thenNullPointer() {
		assertNullPointerThrown(() -> storage.containsSubmodel(null, null));
	}

	@Test
	public void whenExistsSubmodelDescriptorByIdAndSubmodelIdIsNull_thenNullPointer() {
		assertNullPointerThrown(() -> storage.containsSubmodel(IDENTIFICATION_2, null));
	}

	@Test
	public void whenExistsSubmodelDescriptorByIdAndDescriptorIdIsNotAvailable_thenFalse() {
		boolean result = storage.containsSubmodel(UNKNOWN, UNKNOWN_1);
		assertThat(result).isFalse();
		verifyNoEventSent();
	}

	@Test
	public void whenExistsSubmodelDescriptorByIdAndSubmodelIdIsNotAvailable_thenFalse() {
		boolean result = storage.containsSubmodel(IDENTIFICATION_2, _2_UNKNOWN);
		assertThat(result).isFalse();
		verifyNoEventSent();
	}

	@Test
	public void whenExistsSubmodelDescriptorByIdAndAvailable_thenTrue() {
		boolean result = storage.containsSubmodel(IDENTIFICATION_2, IDENTIFICATION_2_1);
		assertThat(result).isTrue();
		verifyNoEventSent();
	}

	@Test
	public void whenGetSubmodelDescriptorByIdAndBothArgsNull_thenNullPointer() {
		assertNullPointerThrown(() -> storage.getSubmodel(null, null));
	}

	@Test
	public void whenGetSubmodelDescriptorByIdAndSubmodelIdIsNull_thenNullPointer() {
		assertNullPointerThrown(() -> storage.getSubmodel(IDENTIFICATION_2, null));
	}

	@Test
	public void whenGetSubmodelDescriptorByIdAndDescriptorIdIsNotAvailable_thenThrowNotFound() {
		assertThrows(AasDescriptorNotFoundException.class, () -> storage.getSubmodel(UNKNOWN, UNKNOWN_1));
		verifyNoEventSent();
	}

	@Test
	public void whenGetSubmodelDescriptorByIdAndSubmodelIdIsNotAvailable_thenThrowNotFound() {
		assertThrows(SubmodelNotFoundException.class, () -> storage.getSubmodel(IDENTIFICATION_2, _2_UNKNOWN));
		verifyNoEventSent();
	}

	@Test
	public void whenGetSubmodelDescriptorByIdAndSubmodelIdIsAvailable_thenGotResult() throws IOException {
		SubmodelDescriptor result = storage.getSubmodel(IDENTIFICATION_2, IDENTIFICATION_2_1);
		SubmodelDescriptor expected = testResourcesLoader.loadSubmodel();
		assertThat(result).isEqualTo(expected);
		verifyNoEventSent();
	}

	@Test 
	public void whenGetAllSubmodelsOverTwoPages_ThenReturnPageStepByStep() throws IOException {
		CursorResult<List<SubmodelDescriptor>> firstResult = getAllSubmodelsWithPagination(IDENTIFICATION_2, 2, null);
		List<SubmodelDescriptor> expected = testResourcesLoader.loadSubmodelList();
		CursorResult<List<SubmodelDescriptor>> secondResult = getAllSubmodelsWithPagination(IDENTIFICATION_2, 2, firstResult.getCursor());
		assertThat(firstResult.getCursor()).isNotNull();
		assertThat(secondResult.getCursor()).isNull();
		assertThat(firstResult.getResult()).containsExactlyInAnyOrderElementsOf(expected.subList(0, 2));
		assertThat(secondResult.getResult()).containsExactlyInAnyOrderElementsOf(expected.subList(2, 4));
		verifyNoEventSent();
	}
	
	@Test
	public void whenTryToReplaceUnknownDescriptor_thenThrowException() {
		AssetAdministrationShellDescriptor descr = RegistryTestObjects.newAssetAdministrationShellDescriptor(IDENTIFICATION_1);
		assertThrows(AasDescriptorNotFoundException.class, ()-> storage.replaceAasDescriptor(UNKNOWN, descr));
	}

	@Test
	public void whenInsertSubmodelAndAlreadyExists_thenThrowException() {
		SubmodelDescriptor descr = RegistryTestObjects.newSubmodelDescriptor(IDENTIFICATION_2_1);
		assertThrows(SubmodelAlreadyExistsException.class, ()-> storage.insertSubmodel(IDENTIFICATION_2, descr));
	}
	
	@Test
	public void whenInsertSubmodelAndAasDescriptorNotFound_thenThrowException() {
		SubmodelDescriptor descr = RegistryTestObjects.newSubmodelDescriptor(IDENTIFICATION_2_1);
		assertThrows(AasDescriptorNotFoundException.class, ()-> storage.insertSubmodel(UNKNOWN, descr));
	}

	
	@Test
	public void whenReplaceSubmodelAndNotAvailable_thenThrowException() {
		SubmodelDescriptor descr = RegistryTestObjects.newSubmodelDescriptor(UNKNOWN);
		assertThrows(SubmodelNotFoundException.class, ()-> storage.replaceSubmodel(IDENTIFICATION_1, UNKNOWN, descr));	
	}
	
	@Test
	public void whenReplaceSubmodelAndDescriptorNotAvailable_thenThrowException() {
		SubmodelDescriptor descr = RegistryTestObjects.newSubmodelDescriptor(UNKNOWN);
		assertThrows(DescriptorNotFoundException.class, ()-> storage.replaceSubmodel(UNKNOWN, UNKNOWN, descr));	
	}
	
	@Test
	public void whenInsertAasDescriptorAndDescriptorAlreadyAvailable_thenThrowException() {
		assertThrows(AasDescriptorAlreadyExistsException.class, ()->storage.insertAasDescriptor(RegistryTestObjects.newAssetAdministrationShellDescriptor(IDENTIFICATION_1)));
	}
	
	@Test
	public void whenReplaceSubmodelAndWithDifferentId_thenEventIsSent() throws IOException {
		SubmodelDescriptor descr = RegistryTestObjects.newSubmodelDescriptor(IDENTIFICATION_NEW);
		storage.replaceSubmodel(IDENTIFICATION_2, IDENTIFICATION_2_1, descr);
		verifyEventsSent();
	}

	
	@Test
	public void whenReplaceSubmodelButNotFound_thenThrowNotFound() {
		SubmodelDescriptor sm = RegistryTestObjects.newSubmodelDescriptor(IDENTIFICATION_NEW);
		
		assertThrows(SubmodelNotFoundException.class, ()->storage.replaceSubmodel(IDENTIFICATION_1, IDENTIFICATION_NEW, sm));
		
		verifyNoEventSent();
	}
	
}