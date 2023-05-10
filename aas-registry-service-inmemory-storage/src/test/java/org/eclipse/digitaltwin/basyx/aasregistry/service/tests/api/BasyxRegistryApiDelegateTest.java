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
package org.eclipse.digitaltwin.basyx.aasregistry.service.tests.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import org.eclipse.digitaltwin.basyx.aasregistry.events.RegistryEventSink;
import org.eclipse.digitaltwin.basyx.aasregistry.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.basyx.aasregistry.model.GetAssetAdministrationShellDescriptorsResult;
import org.eclipse.digitaltwin.basyx.aasregistry.model.GetSubmodelDescriptorsResult;
import org.eclipse.digitaltwin.basyx.aasregistry.model.ShellDescriptorQuery;
import org.eclipse.digitaltwin.basyx.aasregistry.model.ShellDescriptorQuery.QueryTypeEnum;
import org.eclipse.digitaltwin.basyx.aasregistry.model.ShellDescriptorSearchRequest;
import org.eclipse.digitaltwin.basyx.aasregistry.model.ShellDescriptorSearchResponse;
import org.eclipse.digitaltwin.basyx.aasregistry.model.SubmodelDescriptor;
import org.eclipse.digitaltwin.basyx.aasregistry.service.api.BasyxRegistryApiDelegate;
import org.eclipse.digitaltwin.basyx.aasregistry.service.api.BasyxSearchApiDelegate;
import org.eclipse.digitaltwin.basyx.aasregistry.service.api.SearchApiController;
import org.eclipse.digitaltwin.basyx.aasregistry.service.api.ShellDescriptorsApiController;
import org.eclipse.digitaltwin.basyx.aasregistry.service.configuration.InMemoryAasStorageConfiguration;
import org.eclipse.digitaltwin.basyx.aasregistry.service.errors.AasDescriptorNotFoundException;
import org.eclipse.digitaltwin.basyx.aasregistry.service.storage.AasRegistryStorage;
import org.eclipse.digitaltwin.basyx.aasregistry.service.tests.TestResourcesLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import dorg.eclipse.digitaltwin.basyx.aasregistry.client.api.AasRegistryPaths;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { BasyxSearchApiDelegate.class, BasyxRegistryApiDelegate.class, SearchApiController.class, ShellDescriptorsApiController.class, InMemoryAasStorageConfiguration.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = { "registry.type=inMemory" })
public class BasyxRegistryApiDelegateTest {

	private static final String ID_3 = "identification_3";

	private static final String ID_2_1 = "identification_2.1";

	private static final String ID_1 = "identification_1";

	private static final String ID_2_4 = "2.4";

	private static final String ID_UNKNOWN = "unknown";

	private static final String ID_2 = "identification_2";

	private static final String ID_2_3 = "identification_2.3";

	@MockBean
	private RegistryEventSink listener;

	@Autowired
	private AasRegistryStorage storage;

	@Autowired
	private ShellDescriptorsApiController aasController;

	@Autowired
	private SearchApiController searchController;

	@Rule
	public TestResourcesLoader testResourcesLoader = new TestResourcesLoader();

	@Before
	public void initStorage() throws IOException {
		testResourcesLoader.loadRepositoryDefinition().forEach(storage::insertAasDescriptor);
	}

	@After
	public void clearStorage() {
		storage.clear();
	}

	@Test
	public void whenDeleteAssetAdministrationShellDescriptorById_thenNoContent() {
		ResponseEntity<Void> response = aasController.deleteAssetAdministrationShellDescriptorById(encode(ID_1));
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
	}

	@Test
	public void whenDeleteSubmodelDescriptorById_thenNoContent() {
		ResponseEntity<Void> response = aasController.deleteSubmodelDescriptorByIdThroughSuperpath(encode(ID_2), encode(ID_2_1));
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
	}

	@Test
	public void whenDeleteSubmodelDescriptorByIdUnknownAasId_thenNotFound() {
		assertThrows(AasDescriptorNotFoundException.class, () -> aasController.deleteSubmodelDescriptorByIdThroughSuperpath(encode(ID_UNKNOWN), encode(ID_2_1)));
	}

	@Test
	public void whenGetAllAssetAdministrationShellDescriptors_thenRepoContent() throws IOException {
		List<AssetAdministrationShellDescriptor> repoContent = testResourcesLoader.loadRepositoryDefinition();
		ResponseEntity<GetAssetAdministrationShellDescriptorsResult> response = aasController.getAllAssetAdministrationShellDescriptors(null, null, null, null);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().getResult()).asList().containsExactlyInAnyOrderElementsOf(repoContent);
	}

	@Test
	public void whenGetAllSubmodelDescriptorsNullArgs_thenThrowNullPointer() {
		assertThrows(NullPointerException.class, () -> aasController.getAllSubmodelDescriptorsThroughSuperpath(null, null, null));
	}

	@Test
	public void whenGetAllSubmodelDescriptorsUnknownDescriptor_thenNotFound() {
		assertThrows(AasDescriptorNotFoundException.class, () -> aasController.getAllSubmodelDescriptorsThroughSuperpath(encode(ID_UNKNOWN), null, null));
	}

	@Test
	public void whenGetAllSubmodelDescriptorsKnownDescriptor_thenOk() throws IOException {
		List<SubmodelDescriptor> expected = testResourcesLoader.loadSubmodelList();
		ResponseEntity<GetSubmodelDescriptorsResult> response = aasController.getAllSubmodelDescriptorsThroughSuperpath(encode(ID_2), null, null);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().getResult()).containsExactlyInAnyOrderElementsOf(expected);
	}

	@Test
	public void whenGetAssetAdministrationShellDescriptorByIdNullArg_thenNullPointer() {
		assertThrows(NullPointerException.class, () -> aasController.getAssetAdministrationShellDescriptorById(null));
	}

	@Test
	public void whenGetAssetAdministrationShellDescriptorByIdUnknown_thenNotFound() {
		assertThrows(AasDescriptorNotFoundException.class, () -> aasController.getAssetAdministrationShellDescriptorById(encode(ID_UNKNOWN)));
	}

	@Test
	public void whenGetAssetAdministrationShellDescriptorById_thenOk() throws IOException {
		AssetAdministrationShellDescriptor expected = testResourcesLoader.loadAssetAdminShellDescriptor();
		ResponseEntity<AssetAdministrationShellDescriptor> response = aasController.getAssetAdministrationShellDescriptorById(encode(ID_2));
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualTo(expected);
	}

	@Test
	public void whenGetSubmodelDescriptorByIdNullArgs_thenNullPointer() {
		assertThrows(NullPointerException.class, () -> aasController.getSubmodelDescriptorByIdThroughSuperpath(null, null));
	}

	@Test
	public void whenGetSubmodelDescriptorById_thenOk() throws IOException {
		SubmodelDescriptor expected = testResourcesLoader.loadSubmodel();
		ResponseEntity<SubmodelDescriptor> response = aasController.getSubmodelDescriptorByIdThroughSuperpath(encode(ID_2), encode(ID_2_1));
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualTo(expected);
	}

	@Test
	public void whenGetSubmodelDescriptorByIdUnknown_thenNotFound() {
		assertThrows(AasDescriptorNotFoundException.class, () -> aasController.getSubmodelDescriptorByIdThroughSuperpath(encode(ID_UNKNOWN), encode(ID_UNKNOWN)));
	}

	@Test
	public void whenPostSubmodelDescriptorNullArgs_thenNullPointer() {
		assertThrows(NullPointerException.class, () -> aasController.postSubmodelDescriptorThroughSuperpath(null, null));
	}

	@Test
	public void whenPostSubmodelDescriptor_thenCreated() throws IOException {
		SubmodelDescriptor input = testResourcesLoader.loadSubmodel("input");
		ResponseEntity<SubmodelDescriptor> response = aasController.postSubmodelDescriptorThroughSuperpath(input, encode(ID_2));
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isEqualTo(input);
		ResponseEntity<GetSubmodelDescriptorsResult> all = aasController.getAllSubmodelDescriptorsThroughSuperpath(encode(ID_2), null, null);
		List<SubmodelDescriptor> expected = testResourcesLoader.loadSubmodelList();
		assertThat(all.getBody().getResult()).isEqualTo(expected);
	}

	@Test
	public void whenPostSubmodelDescriptorUnknownAasId_thenNotFound() throws IOException {
		SubmodelDescriptor input = new SubmodelDescriptor();
		input.setId("4.3");
		assertThrows(AasDescriptorNotFoundException.class, () -> aasController.postSubmodelDescriptorThroughSuperpath(input, encode(ID_UNKNOWN)));
	}

	@Test
	public void whenPostAssetAdministrationShellDescriptorById_thenCreated() throws IOException {
		AssetAdministrationShellDescriptor descriptor = testResourcesLoader.loadAssetAdminShellDescriptor();
		ResponseEntity<?> response = aasController.postAssetAdministrationShellDescriptor(descriptor);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		ResponseEntity<AssetAdministrationShellDescriptor> stored = aasController.getAssetAdministrationShellDescriptorById(encode(ID_3));
		assertThat(descriptor).isEqualTo(stored.getBody());
	}

	@Test
	public void whenPostSubmodelDescriptorDescriptorById_thenNoContent() throws IOException {
		AssetAdministrationShellDescriptor descr = new AssetAdministrationShellDescriptor();
		descr.id(ID_2);
		assertThat(aasController.putAssetAdministrationShellDescriptorById(descr, encode(ID_2)).getStatusCode()).isEqualByComparingTo(HttpStatus.NO_CONTENT);
		SubmodelDescriptor input = new SubmodelDescriptor();
		input.setId(ID_2_3);
		ResponseEntity<?> response = aasController.postSubmodelDescriptorThroughSuperpath(input, encode(ID_2));
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		ResponseEntity<SubmodelDescriptor> stored = aasController.getSubmodelDescriptorByIdThroughSuperpath(encode(ID_2), encode(ID_2_3));
		assertThat(input).isEqualTo(stored.getBody());
	}

	@Test
	public void whenPutSubmodelDescriptorDescriptorByIdUnknownParent_thenNotFound() throws IOException {
		SubmodelDescriptor input = new SubmodelDescriptor();
		input.setId(ID_2_3);
		assertThrows(AasDescriptorNotFoundException.class, () -> aasController.putSubmodelDescriptorByIdThroughSuperpath(input, encode(ID_UNKNOWN), encode(ID_2_3)));
	}

	@Test
	public void whenPostAssetAdministrationShellDescriptor_thenApplied() throws IOException {
		AssetAdministrationShellDescriptor input = new AssetAdministrationShellDescriptor();
		input.setId(ID_3);
		ResponseEntity<AssetAdministrationShellDescriptor> response = aasController.postAssetAdministrationShellDescriptor(input);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isEqualTo(input);

		ResponseEntity<GetAssetAdministrationShellDescriptorsResult> all = aasController.getAllAssetAdministrationShellDescriptors(null, null, null, null);
		List<AssetAdministrationShellDescriptor> expected = testResourcesLoader.loadShellDescriptorList();
		assertThat(all.getBody().getResult()).asList().containsExactlyInAnyOrderElementsOf(expected);
	}

	@Test
	public void whenPostAssetAdministrationShellDescriptor_thenOverridden() throws IOException {
		AssetAdministrationShellDescriptor input = new AssetAdministrationShellDescriptor();
		input.setId(ID_2);
		ResponseEntity<Void> response = aasController.putAssetAdministrationShellDescriptorById(input, encode(ID_2));
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ResponseEntity<GetAssetAdministrationShellDescriptorsResult> all = aasController.getAllAssetAdministrationShellDescriptors(null, null, null, null);
		List<AssetAdministrationShellDescriptor> expected = testResourcesLoader.loadShellDescriptorList();
		assertThat(all.getBody().getResult()).asList().containsExactlyInAnyOrderElementsOf(expected);
	}

	@Test
	public void whenSearchForUnknownAasDescriptor_thenReturnEmptyList() {
		ShellDescriptorSearchRequest request = new ShellDescriptorSearchRequest().query(new ShellDescriptorQuery().queryType(QueryTypeEnum.MATCH).path(AasRegistryPaths.submodelDescriptors().id()).value("unknown"));
		ResponseEntity<ShellDescriptorSearchResponse> entry = searchController.searchShellDescriptors(request);
		assertThat(entry.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(entry.getBody().getHits()).isEmpty();
	}

	@Test
	public void whenDeleteAllShellDescritors_thenReturnNoContent() {
		ResponseEntity<Void> entry = aasController.deleteAllShellDescriptors();
		assertThat(entry.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(entry.getBody()).isNull();
	}

	@Test
	public void whenMatchSearchForAasDescriptor_thenReturnResult() {
		AssetAdministrationShellDescriptor input = new AssetAdministrationShellDescriptor();
		input.setId(ID_2);
		input.submodelDescriptors(List.of((SubmodelDescriptor) new SubmodelDescriptor().id(ID_2_1)));
		ResponseEntity<Void> response = aasController.putAssetAdministrationShellDescriptorById(input, encode(ID_2));
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ShellDescriptorSearchRequest request = new ShellDescriptorSearchRequest().query(new ShellDescriptorQuery().queryType(QueryTypeEnum.MATCH).path(AasRegistryPaths.submodelDescriptors().id()).value(ID_2_1));
		ResponseEntity<ShellDescriptorSearchResponse> entry = searchController.searchShellDescriptors(request);
		assertThat(entry.getStatusCode()).isEqualTo(HttpStatus.OK);
		List<AssetAdministrationShellDescriptor> result = entry.getBody().getHits();
		assertThat(result.size()).isEqualTo(1);
		assertThat(result.get(0)).isEqualTo(input);
	}

	@Test
	public void whenRegexSearchForAasDescriptor_thenReturnResult() {
		AssetAdministrationShellDescriptor input = new AssetAdministrationShellDescriptor();
		input.setId(ID_2);
		input.submodelDescriptors(List.of((SubmodelDescriptor) new SubmodelDescriptor().idShort(ID_2_1).id(ID_2_1)));
		ResponseEntity<Void> response = aasController.putAssetAdministrationShellDescriptorById(input, encode(ID_2));
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ShellDescriptorSearchRequest request = new ShellDescriptorSearchRequest().query(new ShellDescriptorQuery().queryType(QueryTypeEnum.REGEX).path(AasRegistryPaths.submodelDescriptors().idShort()).value(".*_2.1"));
		ResponseEntity<ShellDescriptorSearchResponse> entry = searchController.searchShellDescriptors(request);
		assertThat(entry.getStatusCode()).isEqualTo(HttpStatus.OK);
		List<AssetAdministrationShellDescriptor> result = entry.getBody().getHits();
		assertThat(result.size()).isEqualTo(1);
		assertThat(result.get(0)).isEqualTo(input);
	}

	private byte[] encode(String id) {
		return Base64.getUrlEncoder().encode(id.getBytes(StandardCharsets.UTF_8));
	}
}