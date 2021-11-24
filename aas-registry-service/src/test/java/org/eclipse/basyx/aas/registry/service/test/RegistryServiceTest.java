package org.eclipse.basyx.aas.registry.service.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.eclipse.basyx.aas.registry.event.RegistryEvent;
import org.eclipse.basyx.aas.registry.event.RegistryEventListener;
import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.basyx.aas.registry.model.Identifier;
import org.eclipse.basyx.aas.registry.model.SubmodelDescriptor;
import org.eclipse.basyx.aas.registry.repository.AssetAdministrationShellDescriptorRepository;
import org.eclipse.basyx.aas.registry.service.RegistryService;
import org.eclipse.basyx.aas.registry.service.RegistryServiceImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import ch.qos.logback.core.recovery.ResilientSyslogOutputStream;


@RunWith(SpringRunner.class)
@ContextConfiguration(classes = RegistryServiceImpl.class)
public class RegistryServiceTest {

	@MockBean
	private AssetAdministrationShellDescriptorRepository repo;

	@MockBean
	private RegistryEventListener listener;

	@Autowired
	private RegistryService registry;

	@Rule
	public final TestResourcesLoader testResourcesLoader = new TestResourcesLoader();

	@Before
	public void init() throws IOException {
		RepositoryMockInitializer initializer = new RepositoryMockInitializer(repo);
		List<AssetAdministrationShellDescriptor> repoContent = testResourcesLoader.getRepositoryDefinition();
		initializer.initialize(repoContent);
	}

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
		Optional<List<SubmodelDescriptor>> found = registry.getAllSubmodelDescriptors("1");
		assertThat(found).isPresent().get().asList().isEmpty();
		verifyNoEventSend();
	}

	@Test
	public void whenGetAllSubmodelDescriptorsAndNotPresent_thenEmptyOptional() throws IOException {
		Optional<List<SubmodelDescriptor>> result = registry.getAllSubmodelDescriptors("unknown");
		assertThat(result).isEmpty();
		verifyNoEventSend();
	}

	@Test
	public void whenGetAllSubmodelDescriptors_thenGot2Elements() throws IOException {
		Optional<List<SubmodelDescriptor>> found = registry.getAllSubmodelDescriptors("2");
		List<SubmodelDescriptor> expected = testResourcesLoader.loadSubmodelList();
		assertThat(found).isPresent().get().asList().containsExactlyInAnyOrderElementsOf(expected);
		verifyNoEventSend();
	}

	@Test
	public void whenExistsAssetAdministratationShellDescriptorAndUnavailable_thenFalse() {
		boolean result = registry.existsAssetAdministrationShellDescriptorById("unknown");
		assertThat(result).isFalse();
		verifyNoEventSend();
	}

	@Test
	public void whenExistsAssetAdministratationShellDescriptorAndAvailable_thenTrue() {
		boolean result = registry.existsAssetAdministrationShellDescriptorById("1");
		assertThat(result).isTrue();
		verifyNoEventSend();
	}

	@Test
	public void whenExistsAssetAdministratationShellDescriptorAndNullArg_thenFalse() {
		boolean result = registry.existsAssetAdministrationShellDescriptorById(null);
		assertThat(result).isFalse();
		verifyNoEventSend();
	}

	@Test
	public void whenExistsSubmodelDescriptorByIdAndBothArgsNull_thenFalse() {
		whenExistsSubmodelDescriptorById_ThenFalse(null, null);
	}

	@Test
	public void whenExistsSubmodelDescriptorByIdAndSubmodelIdIsNull_thenFalse() {
		whenExistsSubmodelDescriptorById_ThenFalse("2", null);
	}

	@Test
	public void whenExistsSubmodelDescriptorByIdAndDescriptorIdIsNotAvailable_thenFalse() {
		boolean result = registry.existsSubmodelDescriptorById("unknown", "unknown.1");
		assertThat(result).isFalse();
		verifyNoEventSend();
	}

	@Test
	public void whenExistsSubmodelDescriptorByIdAndSubmodelIdIsNotAvailable_thenFalse() {
		boolean result = registry.existsSubmodelDescriptorById("2", "2.unknown");
		assertThat(result).isFalse();
		verifyNoEventSend();
	}

	@Test
	public void whenExistsSubmodelDescriptorByIdAndAvailable_thenTrue() {
		boolean result = registry.existsSubmodelDescriptorById("2", "2.1");
		assertThat(result).isTrue();
		verifyNoEventSend();
	}

	@Test
	public void whenGetSubmodelDescriptorByIdAndBothArgsNull_thenEmpty() {
		whenGetSubmodelDescriptorById_thenEmpty(null, null);
	}

	@Test
	public void whenGetSubmodelDescriptorByIdAndSubmodelIdIsNull_thenEmpty() {
		whenGetSubmodelDescriptorById_thenEmpty("2", null);
	}

	@Test
	public void whenGetSubmodelDescriptorByIdAndDescriptorIdIsNotAvailable_thenEmpty() {
		whenGetSubmodelDescriptorById_thenEmpty("unknown", "unknown.1");
	}

	@Test
	public void whenGetSubmodelDescriptorByIdAndSubmodelIdIsNotAvailable_thenEmpty() {
		whenGetSubmodelDescriptorById_thenEmpty("2", "2.unknown");
	}

	@Test
	public void whenGetSubmodelDescriptorByIdAndSubmodelIdIsAvailable_thenGotResult() throws IOException {
		Optional<SubmodelDescriptor> result = registry.getSubmodelDescriptorById("2", "2.1");
		SubmodelDescriptor expected = testResourcesLoader.loadSubmodel();
		assertThat(result).isPresent().get().isEqualTo(expected);
		verifyNoEventSend();
	}

	@Test
	public void whenGetAssetAdminstrationShellDescritorByIdAndIdIsNull_thenEmptyOptional() {
		Optional<AssetAdministrationShellDescriptor> result = registry.getAssetAdministrationShellDescriptorById(null);
		assertThat(result).isEmpty();
		verifyNoEventSend();
	}

	@Test
	public void whenGetAssetAdminstrationShellDescritorByIdAndUnknown_thenEmptyOptional() {
		Optional<AssetAdministrationShellDescriptor> result = registry
				.getAssetAdministrationShellDescriptorById("unknown");
		assertThat(result).isEmpty();
		verifyNoEventSend();
	}

	@Test
	public void whenGetAssetAdminstrationShellDescritorByIdAndAvailable_thenGotResult() throws IOException {
		Optional<AssetAdministrationShellDescriptor> result = registry.getAssetAdministrationShellDescriptorById("1");
		AssetAdministrationShellDescriptor expected = testResourcesLoader.loadAssetAdminShellDescriptor();
		assertThat(result).isPresent().get().isEqualTo(expected);
		verifyNoEventSend();
	}

	@Test
	public void whenRegisterAssetAdministrationShellDescriptorNullArg_thenThrowException() {
		whenRegisterAssetAdminstrationShellDescriptor_thenIllegalArg(null);
	}

	@Test
	public void whenRegisterAssetAdministrationShellDescriptorNoId_thenIdAssignedAndStored() throws IOException {
		AssetAdministrationShellDescriptor descr = new AssetAdministrationShellDescriptor();
		descr.setIdentification(new Identifier());
		whenRegisterAssetAdminstrationShellDescriptor_thenIllegalArg(descr);
	}

	@Test
	public void whenRegisterAssetAdministrationShellDescriptorNoIdentifier_thenIdAssignedAndStored() throws IOException {
		AssetAdministrationShellDescriptor descr = new AssetAdministrationShellDescriptor();
		whenRegisterAssetAdminstrationShellDescriptor_thenIllegalArg(descr);
	}

	@Test
	public void whenRegisterAssetAdministrationShellDescriptor_thenStored() throws IOException {
		List<AssetAdministrationShellDescriptor> initialState = registry.getAllAssetAdministrationShellDescriptors();
		List<AssetAdministrationShellDescriptor> expected = testResourcesLoader.loadShellDescriptorList();
		assertThat(initialState).isNotEqualTo(expected);
		AssetAdministrationShellDescriptor testResource = RegistryTestObjects
				.newAssetAdministrationShellDescriptor("new");
		SubmodelDescriptor subModel = RegistryTestObjects.newSubmodelDescriptor("new.1");
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
		boolean success = registry.unregisterAssetAdministrationShellDescriptorById(null);
		assertThat(success).isFalse();
		List<AssetAdministrationShellDescriptor> currentState = registry.getAllAssetAdministrationShellDescriptors();
		assertThat(currentState).asList().containsExactlyInAnyOrderElementsOf(initialState);
		verifyNoEventSend();
	}

	@Test
	public void whenUnregisterAssetAdministrationShellDescriptorById_thenReturnTrueAndEntryRemoved()
			throws IOException {
		List<AssetAdministrationShellDescriptor> initialState = registry.getAllAssetAdministrationShellDescriptors();
		List<AssetAdministrationShellDescriptor> expected = testResourcesLoader.loadShellDescriptorList();
		boolean success = registry.unregisterAssetAdministrationShellDescriptorById("2");
		assertThat(success).isTrue();
		List<AssetAdministrationShellDescriptor> currentState = registry.getAllAssetAdministrationShellDescriptors();
		assertThat(currentState).asList().isNotEqualTo(initialState).containsExactlyInAnyOrderElementsOf(expected);
		verifyEventSend();
	}

	@Test
	public void whenUnregisterAssetAdministrationShellDescriptorByIdAndIdUnknon_thenReturnFalsAndNoChanges()
			throws IOException {
		List<AssetAdministrationShellDescriptor> initialState = registry.getAllAssetAdministrationShellDescriptors();
		boolean success = registry.unregisterAssetAdministrationShellDescriptorById("unknown");
		assertThat(success).isFalse();
		List<AssetAdministrationShellDescriptor> currentState = registry.getAllAssetAdministrationShellDescriptors();
		assertThat(currentState).asList().containsExactlyInAnyOrderElementsOf(initialState);
		verifyNoEventSend();
	}

	@Test
	public void whenRegisterSubmodelDescriptorNullAasId_thenThrowsException() {
		whenRegisterSubmodelDescritorNull_thenThrowsException(null, null);
	}
	
	@Test
	public void whenRegisterSubmodelDescriptorNullModel_thenThrowsException() {
		whenRegisterSubmodelDescritorNull_thenThrowsException("1", null);
	}

	@Test
	public void whenRegisterSubmodelDescriptorNullId_thenThrowsException() {
		SubmodelDescriptor descriptor = RegistryTestObjects.newSubmodelDescriptor(null);
		whenRegisterSubmodelDescritorNull_thenThrowsException("1", descriptor);
	}
	
	@Test
	public void whenRegisterSubmodelDescriptorUnknownId_thenDoNothingAndReturnEmpty() {
		List<AssetAdministrationShellDescriptor> initialState = registry.getAllAssetAdministrationShellDescriptors();
		SubmodelDescriptor ignored = RegistryTestObjects.newSubmodelDescriptor("ignored");
		Optional<SubmodelDescriptor> returnOpt = registry.registerSubmodelDescriptor("unknown", ignored);
		assertThat(returnOpt).isEmpty();
		List<AssetAdministrationShellDescriptor> currentState = registry.getAllAssetAdministrationShellDescriptors();
		assertThat(currentState).isEqualTo(initialState);
		verifyNoEventSend();
	}

	@Test
	public void whenRegisterSubmodelDescriptorAndWasAlreadyPresent_thenElementIsOverridden() throws IOException {
		List<AssetAdministrationShellDescriptor> initialState = registry.getAllAssetAdministrationShellDescriptors();
		List<AssetAdministrationShellDescriptor> expected = testResourcesLoader.loadShellDescriptorList();
		assertThat(initialState).isNotEqualTo(expected);
		SubmodelDescriptor toAdd = RegistryTestObjects.newSubmodelDescriptor("2.2", "Overridden");
		Optional<SubmodelDescriptor> resultOpt = registry.registerSubmodelDescriptor("2", toAdd);
		assertThat(resultOpt).isPresent().get().isEqualTo(toAdd);
		List<AssetAdministrationShellDescriptor> newState = registry.getAllAssetAdministrationShellDescriptors();
		assertThat(newState).asList().isNotEqualTo(initialState).containsExactlyInAnyOrderElementsOf(expected);
		verifyEventSend();
	}

	@Test
	public void whenRegisterSubmodelDescriptorAndWasNotAlreadyPresent_thenElementIsAdded() throws IOException {
		List<AssetAdministrationShellDescriptor> initialState = registry.getAllAssetAdministrationShellDescriptors();
		List<AssetAdministrationShellDescriptor> expected = testResourcesLoader.loadShellDescriptorList();
		assertThat(initialState).isNotEqualTo(expected);
		SubmodelDescriptor toAdd = RegistryTestObjects.newSubmodelDescriptor("2.3");
		Optional<SubmodelDescriptor> resultOpt = registry.registerSubmodelDescriptor("2", toAdd);
		assertThat(resultOpt).isPresent().get().isEqualTo(toAdd);
		List<AssetAdministrationShellDescriptor> newState = registry.getAllAssetAdministrationShellDescriptors();
		assertThat(newState).asList().isNotEqualTo(initialState).containsExactlyInAnyOrderElementsOf(expected);
		verifyEventSend();
	}

	@Test
	public void whenUnregisterSubmodelDescriptorNullAdminShell_thenReturnFalse() {
		boolean success = registry.unregisterSubmodelDescriptorById(null, "2.1");
		assertThat(success).isFalse();
		verifyNoEventSend();
	}

	@Test
	public void whenUnregisterSubmodelDescriptorNullSubmodelId_thenReturnFalse() {
		boolean success = registry.unregisterSubmodelDescriptorById("2", null);
		assertThat(success).isFalse();
		verifyNoEventSend();
	}

	@Test
	public void whenUnregisterSubmodelDescriptorAndWasPresent_thenElementIsRemoved() throws IOException {
		List<AssetAdministrationShellDescriptor> initialState = registry.getAllAssetAdministrationShellDescriptors();
		List<AssetAdministrationShellDescriptor> expected = testResourcesLoader.loadShellDescriptorList();
		assertThat(initialState).isNotEqualTo(expected);
		boolean success = registry.unregisterSubmodelDescriptorById("2", "2.1");
		assertThat(success).isTrue();
		List<AssetAdministrationShellDescriptor> newState = registry.getAllAssetAdministrationShellDescriptors();
		assertThat(newState).asList().isNotEqualTo(initialState).containsExactlyInAnyOrderElementsOf(expected);
		verifyEventSend();
	}

	@Test
	public void whenUnregisterSubmodelDescriptorAndShellWasNotPresent_thenReturnFalse() throws IOException {
		List<AssetAdministrationShellDescriptor> initialState = registry.getAllAssetAdministrationShellDescriptors();
		boolean success = registry.unregisterSubmodelDescriptorById("unknown", "unknown.1");
		assertThat(success).isFalse();
		List<AssetAdministrationShellDescriptor> newState = registry.getAllAssetAdministrationShellDescriptors();
		assertThat(newState).asList().containsExactlyInAnyOrderElementsOf(initialState);
		verifyNoEventSend();
	}

	@Test
	public void whenUnregisterSubmodelDescriptorAndSubmodelWasNotPresent_thenReturnFalse() throws IOException {
		List<AssetAdministrationShellDescriptor> initialState = registry.getAllAssetAdministrationShellDescriptors();
		boolean success = registry.unregisterSubmodelDescriptorById("2", "2.unknown");
		assertThat(success).isFalse();
		List<AssetAdministrationShellDescriptor> newState = registry.getAllAssetAdministrationShellDescriptors();
		assertThat(newState).asList().containsExactlyInAnyOrderElementsOf(initialState);
		verifyNoEventSend();
	}

	private void whenRegisterSubmodelDescritorNull_thenThrowsException(String aasId, SubmodelDescriptor descriptor) {
		Throwable th = Assertions.catchThrowable(() -> registry.registerSubmodelDescriptor(aasId, descriptor));
		assertThat(th).isInstanceOf(IllegalArgumentException.class);
		verifyNoEventSend();	
	}

	private void whenRegisterAssetAdminstrationShellDescriptor_thenIllegalArg(AssetAdministrationShellDescriptor descr) {
		Throwable th = Assertions.catchThrowable(() -> registry.registerAssetAdministrationShellDescriptor(descr));
		assertThat(th).isInstanceOf(IllegalArgumentException.class);
		verifyNoEventSend();
	}

	private void whenGetSubmodelDescriptorById_thenEmpty(String aasId, String submodelId) {
		Optional<SubmodelDescriptor> result = registry.getSubmodelDescriptorById(aasId, submodelId);
		assertThat(result).isEmpty();
		verifyNoEventSend();
	}

	private void whenExistsSubmodelDescriptorById_ThenFalse(String aasId, String submodelId) {
		boolean result = registry.existsSubmodelDescriptorById(aasId, submodelId);
		assertThat(result).isFalse();
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