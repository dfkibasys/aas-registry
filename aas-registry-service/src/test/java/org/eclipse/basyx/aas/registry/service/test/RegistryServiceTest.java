package org.eclipse.basyx.aas.registry.service.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.eclipse.basyx.aas.registry.client.api.AasRegistryPaths;
import org.eclipse.basyx.aas.registry.events.RegistryEvent;
import org.eclipse.basyx.aas.registry.events.RegistryEventListener;
import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.basyx.aas.registry.model.ShellDescriptorQuery;
import org.eclipse.basyx.aas.registry.model.ShellDescriptorQuery.QueryTypeEnum;
import org.eclipse.basyx.aas.registry.model.ShellDescriptorSearchRequest;
import org.eclipse.basyx.aas.registry.model.ShellDescriptorSearchResponse;
import org.eclipse.basyx.aas.registry.model.SubmodelDescriptor;
import org.eclipse.basyx.aas.registry.repository.AssetAdministrationShellDescriptorRepository;
import org.eclipse.basyx.aas.registry.repository.AtomicElasticSearchRepoAccess;
import org.eclipse.basyx.aas.registry.service.RegistryService;
import org.eclipse.basyx.aas.registry.service.RegistryServiceImpl;
import org.eclipse.basyx.aas.registry.service.test.util.RegistryServiceTestConfiguration;
import org.eclipse.basyx.aas.registry.service.test.util.RegistryTestObjects;
import org.eclipse.basyx.aas.registry.service.test.util.RepositoryMockInitializer;
import org.eclipse.basyx.aas.registry.service.test.util.TestResourcesLoader;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { RegistryServiceImpl.class, RegistryServiceTestConfiguration.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class RegistryServiceTest {

	private static final String IDENTIFICATION_2_3 = "identification_2.3";

	private static final String IDENTIFICATION_2_2 = "identification_2.2";

	private static final String IDENTIFICATION_NEW = "identification_new";

	private static final String IDENTIFICATION_NEW_1 = "identification_new.1";

	private static final String IDENTIFICATION_2_1 = "identification_2.1";

	private static final String _2_UNKNOWN = "2.unknown";

	private static final String UNKNOWN_1 = "unknown.1";

	private static final String UNKNOWN = "unknown";

	private static final String IDENTIFICATION_1 = "identification_1";

	private static final String IDENTIFICATION_2 = "identification_2";

	@MockBean
	private AssetAdministrationShellDescriptorRepository repo;

	@MockBean
	private AtomicElasticSearchRepoAccess repoAccess;

	@MockBean
	private ElasticsearchOperations operations;

	@MockBean
	private RegistryEventListener listener;

	@Autowired
	private RegistryService registry;

	@Rule
	@Autowired
	public TestResourcesLoader testResourcesLoader;

	@Rule
	@Autowired
	public RepositoryMockInitializer initializer;

	@Test
	public void whenGetAllAssetAdministrationShellDescriptors_thenAll() throws IOException {
		List<AssetAdministrationShellDescriptor> found = registry.getAllAssetAdministrationShellDescriptors();
		List<AssetAdministrationShellDescriptor> expected = testResourcesLoader.loadShellDescriptorList();
		assertThat(found).containsExactlyInAnyOrderElementsOf(expected);
		verifyNoEventSend();
	}

	@Test
	public void whenGetAllAssetAdministrationShellDescriptorsAndEmptyRepo_thenEmptyList() throws IOException {
		List<AssetAdministrationShellDescriptor> found = registry.getAllAssetAdministrationShellDescriptors();
		assertThat(found).isEmpty();
		verifyNoEventSend();
	}

	@Test
	public void whenGetAllSubmodelDescriptorsAndNotSet_thenEmptyList() throws IOException {
		Optional<List<SubmodelDescriptor>> found = registry.getAllSubmodelDescriptors(IDENTIFICATION_1);
		assertThat(found).isPresent().get().asList().isEmpty();
		verifyNoEventSend();
	}

	@Test
	public void whenGetAllSubmodelDescriptorsAndNotPresent_thenEmptyOptional() throws IOException {
		Optional<List<SubmodelDescriptor>> result = registry.getAllSubmodelDescriptors(UNKNOWN);
		assertThat(result).isEmpty();
		verifyNoEventSend();
	}

	@Test
	public void whenGetAllSubmodelDescriptors_thenGot2Elements() throws IOException {
		Optional<List<SubmodelDescriptor>> found = registry.getAllSubmodelDescriptors(IDENTIFICATION_2);
		List<SubmodelDescriptor> expected = testResourcesLoader.loadSubmodelList();
		assertThat(found).isPresent().get().asList().containsExactlyInAnyOrderElementsOf(expected);
		verifyNoEventSend();
	}

	@Test
	public void whenExistsSubmodelDescriptorByIdAndBothArgsNull_thenNullPointer() {
		assertNullPointerThrown(() -> registry.existsSubmodelDescriptorById(null, null));
	}

	@Test
	public void whenExistsSubmodelDescriptorByIdAndSubmodelIdIsNull_thenNullPointer() {
		assertNullPointerThrown(() -> registry.existsSubmodelDescriptorById(IDENTIFICATION_2, null));
	}

	@Test
	public void whenExistsSubmodelDescriptorByIdAndDescriptorIdIsNotAvailable_thenFalse() {
		boolean result = registry.existsSubmodelDescriptorById(UNKNOWN, UNKNOWN_1);
		assertThat(result).isFalse();
		verifyNoEventSend();
	}

	@Test
	public void whenExistsSubmodelDescriptorByIdAndSubmodelIdIsNotAvailable_thenFalse() {
		boolean result = registry.existsSubmodelDescriptorById(IDENTIFICATION_2, _2_UNKNOWN);
		assertThat(result).isFalse();
		verifyNoEventSend();
	}

	@Test
	public void whenExistsSubmodelDescriptorByIdAndAvailable_thenTrue() {
		boolean result = registry.existsSubmodelDescriptorById(IDENTIFICATION_2, IDENTIFICATION_2_1);
		assertThat(result).isTrue();
		verifyNoEventSend();
	}

	@Test
	public void whenGetSubmodelDescriptorByIdAndBothArgsNull_thenNullPointer() {
		assertNullPointerThrown(() -> registry.getSubmodelDescriptorById(null, null));
	}

	@Test
	public void whenGetSubmodelDescriptorByIdAndSubmodelIdIsNull_thenNullPointer() {
		assertNullPointerThrown(() -> registry.getSubmodelDescriptorById(IDENTIFICATION_2, null));
	}

	@Test
	public void whenGetSubmodelDescriptorByIdAndDescriptorIdIsNotAvailable_thenEmpty() {
		whenGetSubmodelDescriptorById_thenEmpty(UNKNOWN, UNKNOWN_1);
	}

	@Test
	public void whenGetSubmodelDescriptorByIdAndSubmodelIdIsNotAvailable_thenEmpty() {
		whenGetSubmodelDescriptorById_thenEmpty(IDENTIFICATION_2, _2_UNKNOWN);
	}

	@Test
	public void whenGetSubmodelDescriptorByIdAndSubmodelIdIsAvailable_thenGotResult() throws IOException {
		Optional<SubmodelDescriptor> result = registry.getSubmodelDescriptorById(IDENTIFICATION_2, IDENTIFICATION_2_1);
		SubmodelDescriptor expected = testResourcesLoader.loadSubmodel();
		assertThat(result).isPresent().get().isEqualTo(expected);
		verifyNoEventSend();
	}

	@Test
	public void whenGetAssetAdminstrationShellDescritorByIdAndIdIsNull_thenNullPointer() {
		assertNullPointerThrown(() -> registry.getAssetAdministrationShellDescriptorById(null));
	}

	@Test
	public void whenGetAssetAdminstrationShellDescritorByIdAndUnknown_thenEmptyOptional() {
		Optional<AssetAdministrationShellDescriptor> result = registry
				.getAssetAdministrationShellDescriptorById(UNKNOWN);
		assertThat(result).isEmpty();
		verifyNoEventSend();
	}

	@Test
	public void whenGetAssetAdminstrationShellDescritorByIdAndAvailable_thenGotResult() throws IOException {
		Optional<AssetAdministrationShellDescriptor> result = registry.getAssetAdministrationShellDescriptorById(IDENTIFICATION_1);
		AssetAdministrationShellDescriptor expected = testResourcesLoader.loadAssetAdminShellDescriptor();
		assertThat(result).isPresent().get().isEqualTo(expected);
		verifyNoEventSend();
	}

	@Test
	public void whenRegisterAssetAdministrationShellDescriptorNullArg_thenNullPointer() {
		assertNullPointerThrown(() -> registry.registerAssetAdministrationShellDescriptor(null));
	}

	@Test
	public void whenRegisterAssetAdministrationShellDescriptorNoIdentifier_thenNullPointer() throws IOException {
		AssetAdministrationShellDescriptor descr = new AssetAdministrationShellDescriptor();
		assertNullPointerThrown(() -> registry.registerAssetAdministrationShellDescriptor(descr));
	}

	@Test
	public void whenRegisterAssetAdministrationShellDescriptor_thenStored() throws IOException {
		List<AssetAdministrationShellDescriptor> initialState = registry.getAllAssetAdministrationShellDescriptors();
		List<AssetAdministrationShellDescriptor> expected = testResourcesLoader.loadShellDescriptorList();
		assertThat(initialState).isNotEqualTo(expected);
		AssetAdministrationShellDescriptor testResource = RegistryTestObjects
				.newAssetAdministrationShellDescriptor(IDENTIFICATION_NEW);
		SubmodelDescriptor subModel = RegistryTestObjects.newSubmodelDescriptor(IDENTIFICATION_NEW_1);
		testResource.setSubmodelDescriptors(Collections.singletonList(subModel));
		AssetAdministrationShellDescriptor stored = registry.registerAssetAdministrationShellDescriptor(testResource);
		assertThat(stored).isEqualTo(testResource);
		List<AssetAdministrationShellDescriptor> newState = registry.getAllAssetAdministrationShellDescriptors();
		assertThat(newState).asList().isNotEqualTo(initialState).containsExactlyInAnyOrderElementsOf(expected);
		verifyEventSend();
	}

	@Test
	public void whenUnregisterAssetAdministrationShellDescriptorByIdAndNullId_thenReturnFalseAndNoChanges() {
		List<AssetAdministrationShellDescriptor> initialState = registry.getAllAssetAdministrationShellDescriptors();
		assertThrows(NullPointerException.class, () -> registry.unregisterAssetAdministrationShellDescriptorById(null));
		List<AssetAdministrationShellDescriptor> currentState = registry.getAllAssetAdministrationShellDescriptors();
		assertThat(currentState).asList().containsExactlyInAnyOrderElementsOf(initialState);
		verifyNoEventSend();
	}

	@Test
	public void whenUnregisterAssetAdministrationShellDescriptorById_thenReturnTrueAndEntryRemoved()
			throws IOException {
		List<AssetAdministrationShellDescriptor> initialState = registry.getAllAssetAdministrationShellDescriptors();
		List<AssetAdministrationShellDescriptor> expected = testResourcesLoader.loadShellDescriptorList();
		boolean success = registry.unregisterAssetAdministrationShellDescriptorById(IDENTIFICATION_2);
		assertThat(success).isTrue();
		List<AssetAdministrationShellDescriptor> currentState = registry.getAllAssetAdministrationShellDescriptors();
		assertThat(currentState).asList().isNotEqualTo(initialState).containsExactlyInAnyOrderElementsOf(expected);
		verifyEventSend();
	}

	@Test
	public void whenUnregisterAssetAdministrationShellDescriptorByIdAndIdUnknon_thenReturnFalsAndNoChanges()
			throws IOException {
		List<AssetAdministrationShellDescriptor> initialState = registry.getAllAssetAdministrationShellDescriptors();
		boolean success = registry.unregisterAssetAdministrationShellDescriptorById(UNKNOWN);
		assertThat(success).isFalse();
		List<AssetAdministrationShellDescriptor> currentState = registry.getAllAssetAdministrationShellDescriptors();
		assertThat(currentState).asList().containsExactlyInAnyOrderElementsOf(initialState);
		verifyNoEventSend();
	}

	@Test
	public void whenRegisterSubmodelDescriptorNullAasId_thenNullPointer() {
		assertNullPointerThrown(() -> registry.registerSubmodelDescriptor(null, null));
	}

	@Test
	public void whenRegisterSubmodelDescriptorNullModel_thenNullPointer() {
		assertNullPointerThrown(() -> registry.registerSubmodelDescriptor(IDENTIFICATION_1, null));
	}

	@Test
	public void whenRegisterSubmodelDescriptorNullId_thenNullPointer() {
		SubmodelDescriptor descriptor = RegistryTestObjects.newSubmodelDescriptor(null);
		assertNullPointerThrown(() -> registry.registerSubmodelDescriptor(IDENTIFICATION_1, descriptor));
	}

	@Test
	public void whenRegisterSubmodelDescriptorUnknownId_thenDoNothingAndReturnEmpty() {
		List<AssetAdministrationShellDescriptor> initialState = registry.getAllAssetAdministrationShellDescriptors();
		SubmodelDescriptor ignored = RegistryTestObjects.newSubmodelDescriptor("ignored");
		boolean success = registry.registerSubmodelDescriptor(UNKNOWN, ignored);
		assertThat(success).isFalse();
		List<AssetAdministrationShellDescriptor> currentState = registry.getAllAssetAdministrationShellDescriptors();
		assertThat(currentState).isEqualTo(initialState);
		verifyNoEventSend();
	}

	@Test
	public void whenRegisterSubmodelDescriptorAndWasAlreadyPresent_thenElementIsOverridden() throws IOException {
		List<AssetAdministrationShellDescriptor> initialState = registry.getAllAssetAdministrationShellDescriptors();
		List<AssetAdministrationShellDescriptor> expected = testResourcesLoader.loadShellDescriptorList();
		assertThat(initialState).isNotEqualTo(expected);
		SubmodelDescriptor toAdd = RegistryTestObjects.newSubmodelDescriptorWithDescription(IDENTIFICATION_2_2, "Overridden");
		boolean success = registry.registerSubmodelDescriptor(IDENTIFICATION_2, toAdd);
		assertThat(success).isTrue();
		List<AssetAdministrationShellDescriptor> newState = registry.getAllAssetAdministrationShellDescriptors();
		assertThat(newState).asList().isNotEqualTo(initialState).containsExactlyInAnyOrderElementsOf(expected);
		verifyEventSend();
	}

	@Test
	public void whenRegisterSubmodelDescriptorAndWasNotAlreadyPresent_thenElementIsAdded() throws IOException {
		List<AssetAdministrationShellDescriptor> initialState = registry.getAllAssetAdministrationShellDescriptors();
		List<AssetAdministrationShellDescriptor> expected = testResourcesLoader.loadShellDescriptorList();
		assertThat(initialState).isNotEqualTo(expected);
		SubmodelDescriptor toAdd = RegistryTestObjects.newSubmodelDescriptor(IDENTIFICATION_2_3);
		boolean success = registry.registerSubmodelDescriptor(IDENTIFICATION_2, toAdd);
		assertThat(success).isTrue();
		List<AssetAdministrationShellDescriptor> newState = registry.getAllAssetAdministrationShellDescriptors();
		assertThat(newState).asList().isNotEqualTo(initialState).containsExactlyInAnyOrderElementsOf(expected);
		verifyEventSend();
	}

	@Test
	public void whenUnregisterSubmodelDescriptorNullAdminShell_thenNullPointer() {
		assertNullPointerThrown(() -> registry.unregisterSubmodelDescriptorById(null, IDENTIFICATION_2_1));
	}

	@Test
	public void whenUnregisterSubmodelDescriptorNullSubmodelId_thenNullPointer() {
		assertNullPointerThrown(() -> registry.unregisterSubmodelDescriptorById(IDENTIFICATION_2, null));
	}

	@Test
	public void whenUnregisterSubmodelDescriptorAndWasPresent_thenElementIsRemoved() throws IOException {
		List<AssetAdministrationShellDescriptor> initialState = registry.getAllAssetAdministrationShellDescriptors();
		List<AssetAdministrationShellDescriptor> expected = testResourcesLoader.loadShellDescriptorList();
		assertThat(initialState).isNotEqualTo(expected);
		boolean success = registry.unregisterSubmodelDescriptorById(IDENTIFICATION_2, IDENTIFICATION_2_1);
		assertThat(success).isTrue();
		List<AssetAdministrationShellDescriptor> newState = registry.getAllAssetAdministrationShellDescriptors();
		assertThat(newState).asList().isNotEqualTo(initialState).containsExactlyInAnyOrderElementsOf(expected);
		verifyEventSend();
	}

	@Test
	public void whenUnregisterSubmodelDescriptorAndShellWasNotPresent_thenReturnFalse() throws IOException {
		List<AssetAdministrationShellDescriptor> initialState = registry.getAllAssetAdministrationShellDescriptors();
		boolean success = registry.unregisterSubmodelDescriptorById(UNKNOWN, UNKNOWN_1);
		assertThat(success).isFalse();
		List<AssetAdministrationShellDescriptor> newState = registry.getAllAssetAdministrationShellDescriptors();
		assertThat(newState).asList().containsExactlyInAnyOrderElementsOf(initialState);
		verifyNoEventSend();
	}

	@Test
	public void whenUnregisterSubmodelDescriptorAndSubmodelWasNotPresent_thenReturnTrue() throws IOException {
		List<AssetAdministrationShellDescriptor> initialState = registry.getAllAssetAdministrationShellDescriptors();
		boolean success = registry.unregisterSubmodelDescriptorById(IDENTIFICATION_2, _2_UNKNOWN);
		// returning true makes this method idempotent
		assertThat(success).isFalse();
		List<AssetAdministrationShellDescriptor> newState = registry.getAllAssetAdministrationShellDescriptors();
		assertThat(newState).asList().containsExactlyInAnyOrderElementsOf(initialState);
		verifyNoEventSend();
	}

	@Test
	public void whenMatchSearchBySubModel_thenReturnDescriptorList() throws IOException {
		ShellDescriptorSearchRequest request = new ShellDescriptorSearchRequest()
				.query(new ShellDescriptorQuery().queryType(QueryTypeEnum.MATCH).path(AasRegistryPaths.submodelDescriptors().identification()).value(IDENTIFICATION_2_1));
		ShellDescriptorSearchResponse result = registry.searchAssetAdministrationShellDescriptors(request);
		AssetAdministrationShellDescriptor descriptor = testResourcesLoader.loadAssetAdminShellDescriptor();
		assertThat(result.getTotal()).isEqualTo(1);
		
		assertThat(result.getHits().get(0)).isEqualTo(descriptor);
	}

	@Test
	public void whenMatchSearchBySubModelAndNotFound_thenReturnEmptyList() {
		ShellDescriptorSearchRequest query = new ShellDescriptorSearchRequest()
				.query(new ShellDescriptorQuery().queryType(QueryTypeEnum.MATCH).path(AasRegistryPaths.submodelDescriptors().identification()).value(UNKNOWN));
		ShellDescriptorSearchResponse result = registry.searchAssetAdministrationShellDescriptors(query);
		assertThat(result.getTotal()).isZero();
		assertThat(result.getHits().size()).isZero();
	}
	
	@Test
	public void whenRegexSearchBySubModel_thenReturnDescriptorList() throws IOException {
		ShellDescriptorSearchRequest request = new ShellDescriptorSearchRequest()
				.query(new ShellDescriptorQuery().queryType(QueryTypeEnum.REGEX).path(AasRegistryPaths.submodelDescriptors().idShort()).value("ROBOT_.*_24"));
		ShellDescriptorSearchResponse result = registry.searchAssetAdministrationShellDescriptors(request);
		AssetAdministrationShellDescriptor descriptor = testResourcesLoader.loadAssetAdminShellDescriptor();
		assertThat(result.getTotal()).isEqualTo(1);
		assertThat(result.getHits().get(0)).isEqualTo(descriptor);
	}

	@Test
	public void whenRegexSearchBySubModelAndNotFound_thenReturnEmptyList() {
		ShellDescriptorSearchRequest query = new ShellDescriptorSearchRequest()
				.query(new ShellDescriptorQuery().queryType(QueryTypeEnum.REGEX).path(AasRegistryPaths.submodelDescriptors().idShort()).value(".*_333_*"));
		ShellDescriptorSearchResponse result = registry.searchAssetAdministrationShellDescriptors(query);
		assertThat(result.getTotal()).isZero();
		assertThat(result.getHits().size()).isZero();
	}

	@Test
	public void whenDeleteAllShellDescritors_thenEventsAreSendAndDescriptorsRemoved() {
		List<AssetAdministrationShellDescriptor> oldState = registry.getAllAssetAdministrationShellDescriptors();
		assertThat(oldState).isNotEmpty();		
		registry.unregisterAllAssetAdministrationShellDescriptors();
		// we have just 2 elements thus it is just invoked once
		Mockito.verify(repo, Mockito.times(1)).deleteAllById(Mockito.anyIterable());
		List<AssetAdministrationShellDescriptor> newState = registry.getAllAssetAdministrationShellDescriptors();
		assertThat(newState).isEmpty();		
	}
	
	
	private void assertNullPointerThrown(ThrowingCallable callable) {
		Throwable th = Assertions.catchThrowable(callable);
		assertThat(th).isInstanceOf(NullPointerException.class);
		verifyNoEventSend();
	}

	private void whenGetSubmodelDescriptorById_thenEmpty(String aasId, String submodelId) {
		Optional<SubmodelDescriptor> result = registry.getSubmodelDescriptorById(aasId, submodelId);
		assertThat(result).isEmpty();
		verifyNoEventSend();
	}

	private void verifyEventSend() throws IOException {
		RegistryEvent evt = testResourcesLoader.loadEvent();
		Mockito.verify(listener, Mockito.times(1)).onEvent(Mockito.any(RegistryEvent.class));
		Mockito.verify(listener, Mockito.only()).onEvent(evt);
	}

	private void verifyNoEventSend() {
		Mockito.verify(listener, Mockito.never()).onEvent(Mockito.any(RegistryEvent.class));
	}
}