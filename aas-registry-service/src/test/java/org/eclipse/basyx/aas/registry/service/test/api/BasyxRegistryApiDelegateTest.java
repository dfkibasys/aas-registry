package org.eclipse.basyx.aas.registry.service.test.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.util.List;

import org.eclipse.basyx.aas.registry.api.BasyxRegistryApiDelegate;
import org.eclipse.basyx.aas.registry.api.RegistryApiController;
import org.eclipse.basyx.aas.registry.client.api.ShellDescriptorPaths;
import org.eclipse.basyx.aas.registry.events.RegistryEventListener;
import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.basyx.aas.registry.model.Match;
import org.eclipse.basyx.aas.registry.model.ShellDescriptorSearchQuery;
import org.eclipse.basyx.aas.registry.model.ShellDescriptorSearchResponse;
import org.eclipse.basyx.aas.registry.model.SubmodelDescriptor;
import org.eclipse.basyx.aas.registry.repository.AssetAdministrationShellDescriptorRepository;
import org.eclipse.basyx.aas.registry.repository.AtomicElasticSearchRepoAccess;
import org.eclipse.basyx.aas.registry.service.RegistryServiceImpl;
import org.eclipse.basyx.aas.registry.service.test.util.RegistryServiceTestConfiguration;
import org.eclipse.basyx.aas.registry.service.test.util.RepositoryMockInitializer;
import org.eclipse.basyx.aas.registry.service.test.util.TestResourcesLoader;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { BasyxRegistryApiDelegate.class, RegistryApiController.class,
		RegistryServiceImpl.class, RegistryServiceTestConfiguration.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class BasyxRegistryApiDelegateTest {

	private static final String ID_3 = "3";

	private static final String ID_2_1 = "2.1";

	private static final String ID_1 = "1";

	private static final String ID_2_4 = "2.4";

	private static final String ID_UNKNOWN = "unknown";

	private static final String ID_2 = "2";

	private static final String ID_2_3 = "2.3";

	@MockBean
	private AssetAdministrationShellDescriptorRepository repo;

	@MockBean
	private AtomicElasticSearchRepoAccess atomicRepoAccess;

	@MockBean
	private ElasticsearchOperations operations;

	@MockBean
	private RegistryEventListener listener;

	@Autowired
	private RegistryApiController controller;

	@Rule
	@Autowired
	public TestResourcesLoader testResourcesLoader;

	@Rule
	@Autowired
	public RepositoryMockInitializer initializer;

	@Test
	public void whenDeleteAssetAdministrationShellDescriptorByIdNullArg_thenNoContent() {
		ResponseEntity<Void> response = controller.deleteAssetAdministrationShellDescriptorById(null);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
	}

	@Test
	public void whenDeleteAssetAdministrationShellDescriptorById_thenNoContent() {
		ResponseEntity<Void> response = controller.deleteAssetAdministrationShellDescriptorById(ID_1);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
	}

	@Test
	public void whenDeleteSubmodelDescriptorById_thenNoContent() {
		ResponseEntity<Void> response = controller.deleteSubmodelDescriptorById(ID_2, ID_2_1);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
	}

	@Test
	public void whenDeleteSubmodelDescriptorByIdUnknownAasId_thenNoContent() {
		ResponseEntity<Void> response = controller.deleteSubmodelDescriptorById(ID_UNKNOWN, ID_2_1);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
	}

	@Test
	public void whenDeleteSubmodelDescriptorByIdNullArgs_thenNoContent() {
		ResponseEntity<Void> response = controller.deleteSubmodelDescriptorById(null, null);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
	}

	@Test
	public void whenGetAllAssetAdministrationShellDescriptors_thenRepoContent() throws IOException {
		List<AssetAdministrationShellDescriptor> repoContent = testResourcesLoader.loadRepositoryDefinition();
		ResponseEntity<List<AssetAdministrationShellDescriptor>> response = controller
				.getAllAssetAdministrationShellDescriptors();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).asList().containsExactlyInAnyOrderElementsOf(repoContent);
	}

	@Test
	public void whenGetAllSubmodelDescriptorsNullArgs_thenThrowNullPointer() {
		assertThrows(NullPointerException.class, () -> controller.getAllSubmodelDescriptors(null));
	}

	@Test
	public void whenGetAllSubmodelDescriptorsUnknownDescriptor_thenNotFound() {
		ResponseEntity<List<SubmodelDescriptor>> response = controller.getAllSubmodelDescriptors(ID_UNKNOWN);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	public void whenGetAllSubmodelDescriptorsKnownDescriptor_thenOk() throws IOException {
		List<SubmodelDescriptor> expected = testResourcesLoader.loadSubmodelList();
		ResponseEntity<List<SubmodelDescriptor>> response = controller.getAllSubmodelDescriptors(ID_2);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).containsExactlyInAnyOrderElementsOf(expected);
	}

	@Test
	public void whenGetAssetAdministrationShellDescriptorByIdNullArg_thenNullPointer() {
		assertThrows(NullPointerException.class, () -> controller.getAssetAdministrationShellDescriptorById(null));
	}

	@Test
	public void whenGetAssetAdministrationShellDescriptorByIdUnknown_thenNotFound() {
		ResponseEntity<AssetAdministrationShellDescriptor> response = controller
				.getAssetAdministrationShellDescriptorById(ID_UNKNOWN);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	public void whenGetAssetAdministrationShellDescriptorById_thenOk() throws IOException {
		AssetAdministrationShellDescriptor expected = testResourcesLoader.loadAssetAdminShellDescriptor();
		ResponseEntity<AssetAdministrationShellDescriptor> response = controller
				.getAssetAdministrationShellDescriptorById(ID_2);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualTo(expected);
	}

	@Test
	public void whenGetSubmodelDescriptorByIdNullArgs_thenNullPointer() {
		assertThrows(NullPointerException.class, () -> controller.getSubmodelDescriptorById(null, null));
	}

	@Test
	public void whenGetSubmodelDescriptorById_thenOk() throws IOException {
		SubmodelDescriptor expected = testResourcesLoader.loadSubmodel();
		ResponseEntity<SubmodelDescriptor> response = controller.getSubmodelDescriptorById(ID_2, ID_2_1);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualTo(expected);
	}

	@Test
	public void whenGetSubmodelDescriptorByIdUnknown_thenNotFound() {
		ResponseEntity<SubmodelDescriptor> response = controller.getSubmodelDescriptorById(ID_UNKNOWN, ID_UNKNOWN);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	public void whenPostSubmodelDescriptorNullArgs_thenNullPointer() {
		assertThrows(NullPointerException.class, () -> controller.postSubmodelDescriptor(null, null));
	}

	@Test
	public void whenPostSubmodelDescriptor_thenCreated() throws IOException {
		SubmodelDescriptor input = testResourcesLoader.loadSubmodel("input");
		ResponseEntity<SubmodelDescriptor> response = controller.postSubmodelDescriptor(ID_2, input);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isEqualTo(input);
		ResponseEntity<List<SubmodelDescriptor>> all = controller.getAllSubmodelDescriptors(ID_2);
		List<SubmodelDescriptor> expected = testResourcesLoader.loadSubmodelList();
		assertThat(all.getBody()).isEqualTo(expected);
	}

	@Test
	public void whenPostSubmodelDescriptorUnknownAasId_thenNotFound() throws IOException {
		SubmodelDescriptor input = new SubmodelDescriptor();
		input.setIdentification("4.3");
		ResponseEntity<SubmodelDescriptor> response = controller.postSubmodelDescriptor(ID_UNKNOWN, input);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isNull();
	}

	@Test
	public void whenPutAssetAdministrationShellDescriptorById_thenNoContent() throws IOException {
		AssetAdministrationShellDescriptor descriptor = testResourcesLoader.loadAssetAdminShellDescriptor();
		ResponseEntity<Void> response = controller.putAssetAdministrationShellDescriptorById(ID_3, descriptor);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ResponseEntity<AssetAdministrationShellDescriptor> stored = controller
				.getAssetAdministrationShellDescriptorById(ID_3);
		assertThat(descriptor).isEqualTo(stored.getBody());
	}

	@Test
	public void whenPutSubmodelDescriptorDescriptorById_thenNoContent() throws IOException {
		SubmodelDescriptor input = new SubmodelDescriptor();
		input.setIdentification(ID_2_3);
		ResponseEntity<Void> response = controller.putSubmodelDescriptorById(ID_2, ID_2_3, input);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ResponseEntity<SubmodelDescriptor> stored = controller.getSubmodelDescriptorById(ID_2, ID_2_3);
		assertThat(input).isEqualTo(stored.getBody());
	}

	@Test
	public void whenPutSubmodelDescriptorDescriptorByIdUnknownParent_thenNotFound() throws IOException {
		SubmodelDescriptor input = new SubmodelDescriptor();
		input.setIdentification(ID_2_3);
		ResponseEntity<Void> response = controller.putSubmodelDescriptorById(ID_UNKNOWN, ID_2_3, input);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	public void whenPutSubmodelDescriptorDescriptorByIdDifferentIds_thenBadRequest() throws IOException {
		SubmodelDescriptor input = new SubmodelDescriptor();
		input.setIdentification(ID_2_3);
		ResponseEntity<Void> response = controller.putSubmodelDescriptorById(ID_2, ID_2_4, input);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void whenPutAssetAdministrationShellDescriptorByIdDifferentIds_thenBadRequest() throws IOException {
		AssetAdministrationShellDescriptor input = new AssetAdministrationShellDescriptor();
		input.setIdentification(ID_2);
		ResponseEntity<Void> response = controller.putAssetAdministrationShellDescriptorById(ID_3, input);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void whenPostAssetAdministrationShellDescriptor_thenApplied() throws IOException {
		AssetAdministrationShellDescriptor input = new AssetAdministrationShellDescriptor();
		input.setIdentification(ID_3);
		ResponseEntity<AssetAdministrationShellDescriptor> response = controller
				.postAssetAdministrationShellDescriptor(input);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isEqualTo(input);

		ResponseEntity<List<AssetAdministrationShellDescriptor>> all = controller
				.getAllAssetAdministrationShellDescriptors();
		List<AssetAdministrationShellDescriptor> expected = testResourcesLoader.loadShellDescriptorList();
		assertThat(all.getBody()).asList().containsExactlyInAnyOrderElementsOf(expected);
	}

	@Test
	public void whenPostAssetAdministrationShellDescriptor_thenOverridden() throws IOException {
		AssetAdministrationShellDescriptor input = new AssetAdministrationShellDescriptor();
		input.setIdentification(ID_2);
		ResponseEntity<AssetAdministrationShellDescriptor> response = controller
				.postAssetAdministrationShellDescriptor(input);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isEqualTo(input);

		ResponseEntity<List<AssetAdministrationShellDescriptor>> all = controller
				.getAllAssetAdministrationShellDescriptors();
		List<AssetAdministrationShellDescriptor> expected = testResourcesLoader.loadShellDescriptorList();
		assertThat(all.getBody()).asList().containsExactlyInAnyOrderElementsOf(expected);
	}

	@Test
	public void whenSearchForUnknownAasDescriptor_thenReturnEmptyList() {
		ShellDescriptorSearchQuery query = new ShellDescriptorSearchQuery()
				.match(new Match().path(ShellDescriptorPaths.submodelDescriptors().identification()).value("unknown"));
		ResponseEntity<ShellDescriptorSearchResponse> entry = controller.searchShellDescriptors(query);
		assertThat(entry.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(entry.getBody().getHits()).isEmpty();
	}

	@Test
	public void whenSearchForAasDescriptor_thenReturnResult() {
		AssetAdministrationShellDescriptor input = new AssetAdministrationShellDescriptor();
		input.setIdentification(ID_2);
		input.submodelDescriptors(List.of(new SubmodelDescriptor().identification(ID_2_1)));
		ResponseEntity<AssetAdministrationShellDescriptor> response = controller
				.postAssetAdministrationShellDescriptor(input);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isEqualTo(input);

		ShellDescriptorSearchQuery query = new ShellDescriptorSearchQuery()
				.match(new Match().path(ShellDescriptorPaths.submodelDescriptors().identification()).value(ID_2_1));
		ResponseEntity<ShellDescriptorSearchResponse> entry = controller.searchShellDescriptors(query);
		assertThat(entry.getStatusCode()).isEqualTo(HttpStatus.OK);
		List<AssetAdministrationShellDescriptor> result = entry.getBody().getHits();
		assertThat(result.size()).isEqualTo(1);
		assertThat(result.get(0)).isEqualTo(input);
	}
}