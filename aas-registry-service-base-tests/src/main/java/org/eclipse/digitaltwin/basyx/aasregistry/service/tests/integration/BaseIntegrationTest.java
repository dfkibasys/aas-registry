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
package org.eclipse.digitaltwin.basyx.aasregistry.service.tests.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.digitaltwin.basyx.aasregistry.client.api.DescriptionApi;
import org.eclipse.digitaltwin.basyx.aasregistry.client.api.RegistryAndDiscoveryInterfaceApi;
import org.eclipse.digitaltwin.basyx.aasregistry.events.RegistryEvent;
import org.eclipse.digitaltwin.basyx.aasregistry.events.RegistryEvent.EventType;
import org.eclipse.digitaltwin.basyx.aasregistry.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.basyx.aasregistry.model.AssetKind;
import org.eclipse.digitaltwin.basyx.aasregistry.model.GetAssetAdministrationShellDescriptorsResult;
import org.eclipse.digitaltwin.basyx.aasregistry.model.GetSubmodelDescriptorsResult;
import org.eclipse.digitaltwin.basyx.aasregistry.model.Key;
import org.eclipse.digitaltwin.basyx.aasregistry.model.KeyTypes;
import org.eclipse.digitaltwin.basyx.aasregistry.model.Page;
import org.eclipse.digitaltwin.basyx.aasregistry.model.Reference;
import org.eclipse.digitaltwin.basyx.aasregistry.model.ReferenceTypes;
import org.eclipse.digitaltwin.basyx.aasregistry.model.ServiceDescription;
import org.eclipse.digitaltwin.basyx.aasregistry.model.ServiceDescription.ProfilesEnum;
import org.eclipse.digitaltwin.basyx.aasregistry.model.ShellDescriptorQuery;
import org.eclipse.digitaltwin.basyx.aasregistry.model.ShellDescriptorQuery.QueryTypeEnum;
import org.eclipse.digitaltwin.basyx.aasregistry.model.ShellDescriptorSearchRequest;
import org.eclipse.digitaltwin.basyx.aasregistry.model.ShellDescriptorSearchResponse;
import org.eclipse.digitaltwin.basyx.aasregistry.model.SortDirection;
import org.eclipse.digitaltwin.basyx.aasregistry.model.Sorting;
import org.eclipse.digitaltwin.basyx.aasregistry.model.SortingPath;
import org.eclipse.digitaltwin.basyx.aasregistry.model.SubmodelDescriptor;
import org.eclipse.digitaltwin.basyx.aasregistry.service.tests.RegistryTestObjects;
import org.eclipse.digitaltwin.basyx.aasregistry.service.tests.TestResourcesLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import dorg.eclipse.digitaltwin.basyx.aasregistry.client.api.AasRegistryPaths;

@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public abstract class BaseIntegrationTest {

	private static final int DELETE_ALL_TEST_INSTANCE_COUNT = 50;

	@LocalServerPort
	private Integer port;

	@Rule
	public TestResourcesLoader resourceLoader = new TestResourcesLoader(BaseIntegrationTest.class.getPackageName());

	@Autowired
	private BaseEventListener listener;

	private RegistryAndDiscoveryInterfaceApi api = new RegistryAndDiscoveryInterfaceApi();
	
	@Before
	public void prepareClient() {
		api.setApiClientBasePath("http", "127.0.0.1", port);
	}
	
	@After
	public void cleanup() {
		GetAssetAdministrationShellDescriptorsResult result = api.getAllAssetAdministrationShellDescriptors(null, null, null, null);
		result.getResult().stream().map(AssetAdministrationShellDescriptor::getId).forEach(this::deleteAdminAssetShellDescriptor);
		listener.assertNoAdditionalMessage();
	}

	@Test
	public void whenGetDescription_thenDescriptionIsReturned() {
		DescriptionApi descrApi = new DescriptionApi();
		descrApi.setApiClientBasePath("http", "127.0.0.1", port);
		ResponseEntity<ServiceDescription> entity = descrApi.getDescriptionWithHttpInfo();
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
		List<ProfilesEnum> profiles = entity.getBody().getProfiles();
		assertThat(profiles).asList().hasSize(1);
		assertThat(profiles).asList().containsExactlyInAnyOrder(ProfilesEnum.REGISTRYSERVICESPECIFICATION_V3_0);
	}
	

	@Test
	public void whenWritingParallel_transactionManagementWorks() {
		AssetAdministrationShellDescriptor descriptor = new AssetAdministrationShellDescriptor();
		descriptor.setId("descr");
		api.postAssetAdministrationShellDescriptor(descriptor);
		IntFunction<HttpStatus> op = idx -> writeSubModel(descriptor.getId(), idx);
		assertThat(IntStream.iterate(0, i -> i + 1).limit(300).parallel().mapToObj(op).filter(HttpStatus::isError).findAny()).isEmpty();
		assertThat(api.getAssetAdministrationShellDescriptorById(descriptor.getId()).getSubmodelDescriptors()).hasSize(300);
	}

	private HttpStatus writeSubModel(String descriptorId, int idx) {
		SubmodelDescriptor sm = new SubmodelDescriptor();
		sm.setId(idx + "");
		Reference reference = new Reference();
		sm.setSemanticId(reference);
		if (idx % 2 == 0) {
			reference.setType(ReferenceTypes.EXTERNALREFERENCE);
			reference.addKeysItem(new Key().type(KeyTypes.PROPERTY).value("a"));
		} else {
			reference.setType(ReferenceTypes.MODELREFERENCE);
			reference.addKeysItem(new Key().type(KeyTypes.PROPERTY).value("aaa"));
		}
		RegistryTestObjects.addDefaultEndpoint(sm);
		try {
			return api.postSubmodelDescriptorThroughSuperpathWithHttpInfo(sm, descriptorId).getStatusCode();
		} catch (HttpServerErrorException ex) {
			return ex.getStatusCode();
		}
	}

	@Test
	public void whenDeleteAll_thenAllDescriptorsAreRemoved() {

		for (int i = 0; i < DELETE_ALL_TEST_INSTANCE_COUNT; i++) {
			AssetAdministrationShellDescriptor descr = new AssetAdministrationShellDescriptor();
			String id = "id_" + i;
			descr.setId(id);
			ResponseEntity<AssetAdministrationShellDescriptor> response = api.postAssetAdministrationShellDescriptorWithHttpInfo(descr);
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
			assertThatEventWasSend(RegistryEvent.builder().id(id).aasDescriptor(descr).type(EventType.AAS_REGISTERED).build());
		}

		List<AssetAdministrationShellDescriptor> all = api.getAllAssetAdministrationShellDescriptors(null, null, null, null).getResult();
		assertThat(all.size()).isEqualTo(DELETE_ALL_TEST_INSTANCE_COUNT);

		api.deleteAllShellDescriptors();

		all = api.getAllAssetAdministrationShellDescriptors(null, null, null, null).getResult();
		assertThat(all).isEmpty();

		HashSet<RegistryEvent> events = new HashSet<>();
		// we do not have a specific order, so read all events first
		for (int i = 0; i < DELETE_ALL_TEST_INSTANCE_COUNT; i++) {
			events.add(listener.poll());
		}
		for (int i = 0; i < DELETE_ALL_TEST_INSTANCE_COUNT; i++) {
			assertThat(events.remove(RegistryEvent.builder().id("id_" + i).type(EventType.AAS_UNREGISTERED).build())).isTrue();
		}
		listener.assertNoAdditionalMessage();
	}

	@Test
	public void whenCreateAndDeleteDescriptors_thenAllDescriptorsAreRemoved() throws IOException, InterruptedException, TimeoutException {
		List<AssetAdministrationShellDescriptor> deployed = initialize();
		List<AssetAdministrationShellDescriptor> all = api.getAllAssetAdministrationShellDescriptors(port, null, null, null).getResult();
		assertThat(all).containsExactlyInAnyOrderElementsOf(deployed);

		all.stream().map(AssetAdministrationShellDescriptor::getId).forEach(this::deleteAdminAssetShellDescriptor);

		all = api.getAllAssetAdministrationShellDescriptors(port, null, null, null).getResult();
		assertThat(all).isEmpty();

		listener.assertNoAdditionalMessage();
	}

	@Test
	public void whenRegisterAndUnregisterSubmodel_thenSubmodelIsCreatedAndDeleted() throws IOException, InterruptedException, TimeoutException {
		
		List<AssetAdministrationShellDescriptor> deployed = initialize();
		List<AssetAdministrationShellDescriptor> all = api.getAllAssetAdministrationShellDescriptors(port, null, null, null).getResult();
		assertThat(all).asList().containsExactlyInAnyOrderElementsOf(deployed);

		SubmodelDescriptor toRegister = resourceLoader.loadSubmodel("toregister");
		String aasId = "identification_1";
		ResponseEntity<SubmodelDescriptor> response = api.postSubmodelDescriptorThroughSuperpathWithHttpInfo(toRegister, aasId);
		assertThatEventWasSend(RegistryEvent.builder().id(aasId).submodelId(toRegister.getId()).submodelDescriptor(toRegister).type(EventType.SUBMODEL_REGISTERED).build());
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		SubmodelDescriptor registered = response.getBody();
		assertThat(registered).isEqualTo(toRegister);

		SubmodelDescriptor resolved = api.getSubmodelDescriptorByIdThroughSuperpath(aasId, toRegister.getId());
		assertThat(resolved).isEqualTo(registered);

		AssetAdministrationShellDescriptor aasDescriptor = api.getAssetAdministrationShellDescriptorById(aasId);
		assertThat(aasDescriptor.getSubmodelDescriptors()).contains(toRegister);

		ResponseEntity<Void> deleteResponse = api.deleteSubmodelDescriptorByIdThroughSuperpathWithHttpInfo(aasId, toRegister.getId());
		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		assertThatEventWasSend(RegistryEvent.builder().id(aasId).submodelId(toRegister.getId()).type(EventType.SUBMODEL_UNREGISTERED).build());

		aasDescriptor = api.getAssetAdministrationShellDescriptorById(aasId);
		assertThat(aasDescriptor.getSubmodelDescriptors()).doesNotContain(toRegister);

		listener.assertNoAdditionalMessage();
	}

	@Test
	public void whenInvalidInput_thenSuccessfullyValidated() throws IOException, InterruptedException, TimeoutException {
		initialize();
		assertThrows(HttpClientErrorException.class, () -> api.deleteSubmodelDescriptorByIdThroughSuperpathWithHttpInfo(null, null));
		assertThrows(HttpClientErrorException.class, () -> api.deleteAssetAdministrationShellDescriptorById(null));
		assertThrows(HttpClientErrorException.class, () -> api.getAllSubmodelDescriptorsThroughSuperpath(null, null, null));
		assertThrows(HttpClientErrorException.class, () -> api.getAssetAdministrationShellDescriptorById(null));
		assertThrows(HttpClientErrorException.class, () -> api.putAssetAdministrationShellDescriptorById(null, null));
		assertThrows(HttpClientErrorException.class, () -> api.postAssetAdministrationShellDescriptor(null));
		assertThrows(HttpClientErrorException.class, () -> api.postSubmodelDescriptorThroughSuperpath(null, null));

		AssetAdministrationShellDescriptor descriptor = new AssetAdministrationShellDescriptor();
		descriptor.setIdShort("shortId");
		assertThrows(HttpClientErrorException.class, () -> api.postAssetAdministrationShellDescriptor(descriptor));

		descriptor.setId("identification");
		HttpStatus status = api.postAssetAdministrationShellDescriptorWithHttpInfo(descriptor).getStatusCode();
		assertThat(status).isEqualTo(HttpStatus.CREATED);
		assertThatEventWasSend(RegistryEvent.builder().id(descriptor.getId()).aasDescriptor(descriptor).type(EventType.AAS_REGISTERED).build());
	}

	@Test
	public void whenMatchSearchBySubmodelDescriptorId_thenGotResult() throws Exception {
		initialize();
		AssetAdministrationShellDescriptor expected = resourceLoader.loadAssetAdminShellDescriptor();
		String path = AasRegistryPaths.submodelDescriptors().idShort();
		ShellDescriptorSearchRequest request = new ShellDescriptorSearchRequest().query(new ShellDescriptorQuery().queryType(QueryTypeEnum.MATCH).path(path).value("sm3"));
		ResponseEntity<ShellDescriptorSearchResponse> response = api.searchShellDescriptorsWithHttpInfo(request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		List<AssetAdministrationShellDescriptor> result = response.getBody().getHits();
		assertThat(result.size()).isEqualTo(1);
		assertThat(result.get(0)).isEqualTo(expected);
	}

	@Test
	public void whenRegexSearchBySubmodelDescriptorShortId_thenGotResult() throws Exception {
		initialize();
		AssetAdministrationShellDescriptor expected = resourceLoader.loadAssetAdminShellDescriptor();
		String path = AasRegistryPaths.submodelDescriptors().idShort();
		ShellDescriptorSearchRequest request = new ShellDescriptorSearchRequest().query(new ShellDescriptorQuery().queryType(QueryTypeEnum.REGEX).path(path).value("[st]{1}.*3"));
		ResponseEntity<ShellDescriptorSearchResponse> response = api.searchShellDescriptorsWithHttpInfo(request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		List<AssetAdministrationShellDescriptor> result = response.getBody().getHits();
		assertThat(result.size()).isEqualTo(1);
		assertThat(result.get(0)).isEqualTo(expected);
	}
	
	
	@Test
	public void whenPutShellDescriptorDifferentId_thenMoved() throws IOException, InterruptedException, TimeoutException {
		initialize();

		AssetAdministrationShellDescriptor descr = RegistryTestObjects.newAssetAdministrationShellDescriptor("identification_9");
		ResponseEntity<Void> putResult = api.putAssetAdministrationShellDescriptorByIdWithHttpInfo(descr, "identification_7");
		assertThat(putResult.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThrows(HttpClientErrorException.NotFound.class, () -> api.getAssetAdministrationShellDescriptorByIdWithHttpInfo("identification_7"));
		ResponseEntity<AssetAdministrationShellDescriptor> getResult = api.getAssetAdministrationShellDescriptorByIdWithHttpInfo("identification_9");
		assertThat(getResult.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(descr).isEqualTo(getResult.getBody());
	}
	
	@Test
	public void whenPutSubmodelDifferentId_thenMoved() throws IOException, InterruptedException, TimeoutException {
		initialize();
		SubmodelDescriptor descr = RegistryTestObjects.newSubmodelDescriptor("submodel_9");
		RegistryTestObjects.addDefaultEndpoint(descr);
		ResponseEntity<Void> putResult = api.putSubmodelDescriptorByIdThroughSuperpathWithHttpInfo(descr, "identification_5", "submodel_0");
		assertThat(putResult.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		
		assertThrows(HttpClientErrorException.NotFound.class, () -> api.getSubmodelDescriptorByIdThroughSuperpath("identification_5", "submodel_0"));
		ResponseEntity<SubmodelDescriptor> getResult = api.getSubmodelDescriptorByIdThroughSuperpathWithHttpInfo("identification_5", "submodel_9");
		assertThat(getResult.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(descr).isEqualTo(getResult.getBody());		
	}
	
	@Test
	public void whenPutShellDescriptorSameId_thenUpdated() throws IOException, InterruptedException, TimeoutException {
		initialize();
		AssetAdministrationShellDescriptor descr = RegistryTestObjects.newAssetAdministrationShellDescriptor("identification_5");
		ResponseEntity<Void> putResult = api.putAssetAdministrationShellDescriptorByIdWithHttpInfo(descr, "identification_5");
		assertThat(putResult.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		
		ResponseEntity<AssetAdministrationShellDescriptor> getResult = api.getAssetAdministrationShellDescriptorByIdWithHttpInfo("identification_5");
		assertThat(getResult.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(descr).isEqualTo(getResult.getBody());	
	}
	
	@Test
	public void whenPutSubmodelSameId_thenUpdated() throws IOException, InterruptedException, TimeoutException {
		initialize();
		SubmodelDescriptor descr = RegistryTestObjects.newSubmodelDescriptor("submodel_0");
		RegistryTestObjects.addDefaultEndpoint(descr);
		ResponseEntity<Void> putResult = api.putSubmodelDescriptorByIdThroughSuperpathWithHttpInfo(descr, "identification_5", "submodel_0");
		assertThat(putResult.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		
		ResponseEntity<SubmodelDescriptor> getResult = api.getSubmodelDescriptorByIdThroughSuperpathWithHttpInfo("identification_5", "submodel_0");
		assertThat(getResult.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(descr).isEqualTo(getResult.getBody());			
	}
	
	@Test
	public void whenPutUnknownShellDescriptor_thenNotFound() throws IOException, InterruptedException, TimeoutException {
		initialize();
		AssetAdministrationShellDescriptor descr = RegistryTestObjects.newAssetAdministrationShellDescriptor("unknown");
		assertThrows(HttpClientErrorException.NotFound.class, ()-> api.putAssetAdministrationShellDescriptorByIdWithHttpInfo(descr, "unknown"));

	}
	
	@Test
	public void whenPutUnknownSubmodel_thenNotFound() throws IOException, InterruptedException, TimeoutException {
		initialize();
		SubmodelDescriptor descr = RegistryTestObjects.newSubmodelDescriptorWithDescription("submodel_0", "test");
		RegistryTestObjects.addDefaultEndpoint(descr);
		assertThrows(HttpClientErrorException.NotFound.class, ()->api.putSubmodelDescriptorByIdThroughSuperpathWithHttpInfo(descr, "identification_5", "unknown"));
	}
	
	@Test
	public void whenUseDescriptorPagination_thenUseRefetching() throws IOException, InterruptedException, TimeoutException {
		List<AssetAdministrationShellDescriptor> postedDescriptors = initialize();
		List<AssetAdministrationShellDescriptor> postedDescriptorsSorted = postedDescriptors.stream()
				.sorted(Comparator.comparing(AssetAdministrationShellDescriptor::getId)).collect(Collectors.toList());
		assertThat(postedDescriptors).hasSize(5);
		
		GetAssetAdministrationShellDescriptorsResult  result0 = api.getAllAssetAdministrationShellDescriptors(2, null, null, null);
		List<AssetAdministrationShellDescriptor> body0 = result0.getResult();
		assertThat(body0).hasSize(2);
		assertThat(postedDescriptorsSorted.get(0)).isEqualTo(body0.get(0));
		assertThat(postedDescriptorsSorted.get(1)).isEqualTo(body0.get(1));
		GetAssetAdministrationShellDescriptorsResult  result1 = api.getAllAssetAdministrationShellDescriptors(2, result0.getPagingMetadata().getCursor(), null, null);
		List<AssetAdministrationShellDescriptor> body1 = result1.getResult();
		assertThat(body1).hasSize(2);
		assertThat(postedDescriptorsSorted.get(2)).isEqualTo(body1.get(0));
		assertThat(postedDescriptorsSorted.get(3)).isEqualTo(body1.get(1));
		GetAssetAdministrationShellDescriptorsResult  result2 = api.getAllAssetAdministrationShellDescriptors(2, result1.getPagingMetadata().getCursor(), null, null);
		List<AssetAdministrationShellDescriptor> body2 = result2.getResult();
		assertThat(body2).hasSize(1);
		assertThat(postedDescriptorsSorted.get(4)).isEqualTo(body2.get(0));
		assertThat(result2.getPagingMetadata().getCursor()).isNull();
	}
	
	
	@Test
	public void whenUseDescriptorFilter_thenFiltered() throws IOException, InterruptedException, TimeoutException {
		List<AssetAdministrationShellDescriptor> postedDescriptors = initialize();
			assertThat(postedDescriptors).hasSize(5);
		
		GetAssetAdministrationShellDescriptorsResult  result = api.getAllAssetAdministrationShellDescriptors(null, null, AssetKind.TYPE, "tp");
		List<String> aasIds = result.getResult().stream().map(AssetAdministrationShellDescriptor::getId).collect(Collectors.toList());
		
		assertThat(aasIds).hasSize(2);
		assertThat(aasIds).contains("identification_7", "identification_5");
			
		
	}
	
	@Test
	public void whenUseSubmodelPagination_thenUseRefetching() throws IOException, InterruptedException, TimeoutException {
		List<AssetAdministrationShellDescriptor> postedDescriptors = initialize();
		List<SubmodelDescriptor> postedDescriptorsSorted = postedDescriptors.stream()
				.filter(a -> "identification_5".equals(a.getId()))
				.map(AssetAdministrationShellDescriptor::getSubmodelDescriptors)
				.filter(Objects::nonNull)
				.flatMap(List::stream)
				.sorted(Comparator.comparing(SubmodelDescriptor::getId)).collect(Collectors.toList());
		
		assertThat(postedDescriptorsSorted).hasSize(4);
		
		GetSubmodelDescriptorsResult  result0 = api.getAllSubmodelDescriptorsThroughSuperpath("identification_5", 2, null);
		List<SubmodelDescriptor> body0 = result0.getResult();
		assertThat(body0).hasSize(2);
		assertThat(postedDescriptorsSorted.get(0)).isEqualTo(body0.get(0));
		assertThat(postedDescriptorsSorted.get(1)).isEqualTo(body0.get(1));
		GetSubmodelDescriptorsResult  result1 = api.getAllSubmodelDescriptorsThroughSuperpath("identification_5", 2, result0.getPagingMetadata().getCursor());
		List<SubmodelDescriptor> body1 = result1.getResult();
		assertThat(body1).hasSize(2);
		assertThat(postedDescriptorsSorted.get(2)).isEqualTo(body1.get(0));
		assertThat(postedDescriptorsSorted.get(3)).isEqualTo(body1.get(1));
		assertThat(result1.getPagingMetadata().getCursor()).isNull();
	}

	

	@Test
	public void whenUsePagination_thenUseRefetching() throws IOException, InterruptedException, TimeoutException {
		initialize();

		List<AssetAdministrationShellDescriptor> expected = resourceLoader.loadShellDescriptorList();

		assertResultByPage(0, expected);
		assertResultByPage(1, expected);
		assertResultByPage(2, expected);
		assertResultByPage(3, expected);
	}

	private void assertResultByPage(int from, List<AssetAdministrationShellDescriptor> expected) {
		ShellDescriptorSearchRequest request = new ShellDescriptorSearchRequest().sortBy(new Sorting().addPathItem(SortingPath.IDSHORT).addPathItem(SortingPath.ID).direction(SortDirection.ASC))
				.page(new Page().index(from).size(2));
		ShellDescriptorSearchResponse response = api.searchShellDescriptors(request);
		int total = 5;
		assertThat(response.getTotal()).isEqualTo(total);
		List<AssetAdministrationShellDescriptor> hits = response.getHits();
		int position = from * 2;
		if (position < total) {
			AssetAdministrationShellDescriptor hit0 = hits.get(0);
			AssetAdministrationShellDescriptor expected0 = expected.get(position);
			assertThat(hit0).isEqualTo(expected0);
		} else {
			assertThat(hits).isEmpty();
		}
		position++;
		if (position < total) {
			AssetAdministrationShellDescriptor hit1 = hits.get(1);
			AssetAdministrationShellDescriptor expected1 = expected.get(position);
			assertThat(hit1).isEqualTo(expected1);
		}
	}

	@Test
	public void whenSearchWithSortingByIdShortAsc_thenReturnSortedAsc() throws IOException, InterruptedException, TimeoutException {
		whenSearchWithSortingByIdShort_thenReturnSorted(SortDirection.ASC);
	}

	@Test
	public void whenSearchWithSortingByIdNoSortOrder_thenReturnSortedAsc() throws IOException, InterruptedException, TimeoutException {
		whenSearchWithSortingByIdShort_thenReturnSorted(null);
	}

	@Test
	public void whenSearchWithSortingByIdShortDesc_thenReturnSortedDesc() throws IOException, InterruptedException, TimeoutException {
		whenSearchWithSortingByIdShort_thenReturnSorted(SortDirection.DESC);
	}

	private void whenSearchWithSortingByIdShort_thenReturnSorted(SortDirection direction) throws IOException, InterruptedException, TimeoutException {
		initialize();
		List<AssetAdministrationShellDescriptor> expected = resourceLoader.loadShellDescriptorList();
		String path = AasRegistryPaths.description().language();
		ShellDescriptorSearchRequest request = new ShellDescriptorSearchRequest().query(new ShellDescriptorQuery().queryType(QueryTypeEnum.MATCH).path(path).value("de-DE"))
				.sortBy(new Sorting().addPathItem(SortingPath.IDSHORT).addPathItem(SortingPath.ADMINISTRATION_REVISION).direction(direction));
		ResponseEntity<ShellDescriptorSearchResponse> response = api.searchShellDescriptorsWithHttpInfo(request);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		List<AssetAdministrationShellDescriptor> result = response.getBody().getHits();
		System.out.println(result.toString());
		System.out.println(expected.toString());
		assertThat(result.toString()).isEqualTo(expected.toString());
		assertThat(result).asList().isEqualTo(expected);
	}

	@Test
	public void whenIllegalArguments_thenResult() throws IOException, InterruptedException, TimeoutException {
		initialize();
		api.searchShellDescriptors(new ShellDescriptorSearchRequest());
	}

	private void deleteAdminAssetShellDescriptor(String aasId) {
		listener.reset();

		HttpStatus response = api.deleteAssetAdministrationShellDescriptorByIdWithHttpInfo(URLEncoder.encode(aasId, StandardCharsets.UTF_8)).getStatusCode();
		assertThat(response).isEqualTo(HttpStatus.NO_CONTENT);
		assertThatEventWasSend(RegistryEvent.builder().id(aasId).type(EventType.AAS_UNREGISTERED).build());
	}

	private List<AssetAdministrationShellDescriptor> initialize() throws IOException, InterruptedException, TimeoutException {
		List<AssetAdministrationShellDescriptor> descriptors = resourceLoader.loadRepositoryDefinition();
		for (AssetAdministrationShellDescriptor eachDescriptor : descriptors) {
			ResponseEntity<AssetAdministrationShellDescriptor> response = api.postAssetAdministrationShellDescriptorWithHttpInfo(eachDescriptor);
			assertThat(response.getBody()).isEqualTo(eachDescriptor);
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
			assertThatEventWasSend(RegistryEvent.builder().id(eachDescriptor.getId()).aasDescriptor(eachDescriptor).type(EventType.AAS_REGISTERED).build());
		}
		return descriptors;
	}

	private void assertThatEventWasSend(RegistryEvent build) {
		RegistryEvent evt = listener.poll();
		assertThat(evt).isEqualTo(build);
	}	
}