package de.dfki.cos.basys.aas.registry.service.tests;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;

import de.dfki.cos.basys.aas.registry.events.RegistryEvent;
import de.dfki.cos.basys.aas.registry.events.RegistryEventSink;
import de.dfki.cos.basys.aas.registry.model.AssetAdministrationShellDescriptor;
import de.dfki.cos.basys.aas.registry.model.AssetKind;
import de.dfki.cos.basys.aas.registry.model.SubmodelDescriptor;
import de.dfki.cos.basys.aas.registry.service.storage.AasRegistryStorage;
import de.dfki.cos.basys.aas.registry.service.storage.CursorResult;
import de.dfki.cos.basys.aas.registry.service.storage.DescriptorFilter;
import de.dfki.cos.basys.aas.registry.service.storage.PaginationInfo;
import de.dfki.cos.basys.aas.registry.service.storage.RegistrationEventSendingAasRegistryStorage;

@RunWith(SpringRunner.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class BaseInterfaceTest {

	protected static final String IDENTIFICATION_2_3 = "identification_2.3";

	protected static final String IDENTIFICATION_2_2 = "identification_2.2";

	protected static final String IDENTIFICATION_NEW = "identification_new";

	protected static final String IDENTIFICATION_NEW_1 = "identification_new.1";

	protected static final String IDENTIFICATION_2_1 = "identification_2.1";

	protected static final String _2_UNKNOWN = "2.unknown";

	protected static final String UNKNOWN_1 = "unknown.1";

	protected static final String UNKNOWN = "unknown";

	protected static final String IDENTIFICATION_1 = "identification_1";

	protected static final String IDENTIFICATION_2 = "identification_2";
	
	protected static final String IDENTIFICATION_3 = "identification_3";

	private RegistryEventSink eventSink = Mockito.mock(RegistryEventSink.class);

	@Autowired
	public AasRegistryStorage baseStorage;

	protected RegistrationEventSendingAasRegistryStorage storage;

	@Rule
	public TestResourcesLoader testResourcesLoader = new TestResourcesLoader();
	
	
	protected RegistryEventSink getEventSink() {
		return eventSink;
	}

	protected void clearBaseStorage() {
		baseStorage.clear();
	}
	
	@Before
	public void setUp() throws IOException {
		storage = new RegistrationEventSendingAasRegistryStorage(baseStorage, eventSink);
		List<AssetAdministrationShellDescriptor> descriptors = testResourcesLoader.loadRepositoryDefinition();
		descriptors.forEach(baseStorage::insertAasDescriptor);
	}

	@After
	public void tearDown() {
		baseStorage.clear();
	}


	protected void assertNullPointerThrown(ThrowingCallable callable) {
		Throwable th = Assertions.catchThrowable(callable);
		assertThat(th).isInstanceOf(NullPointerException.class);
		verifyNoEventSent();
	}

	
	protected void verifyEventsSent() throws IOException {
 		List<RegistryEvent> events = testResourcesLoader.loadEvents();
		
		Mockito.verify(eventSink, Mockito.times(events.size())).consumeEvent(ArgumentMatchers.any(RegistryEvent.class));
		
		InOrder inOrder = Mockito.inOrder(eventSink);
		for (RegistryEvent eachEvent : events) {
			inOrder.verify(eventSink).consumeEvent(eachEvent);
		}
	}

	protected void verifyNoEventSent() {
		Mockito.verify(eventSink, Mockito.never()).consumeEvent(ArgumentMatchers.any(RegistryEvent.class));
	}

	protected List<AssetAdministrationShellDescriptor> getAllAasDescriptors() {
		return storage.getAllAasDescriptors(new PaginationInfo(null, null), new DescriptorFilter(null, null)).getResult();
	}
	

	protected List<AssetAdministrationShellDescriptor> getAllAasDescriptorsFiltered(AssetKind kind, String type) {
		return storage.getAllAasDescriptors(new PaginationInfo(null, null), new DescriptorFilter(kind, type)).getResult();
	}
	
	protected CursorResult<List<AssetAdministrationShellDescriptor>> getAllAasDescriptorsWithPagination(int limit, String cursor) {
		return storage.getAllAasDescriptors(new PaginationInfo(limit, cursor), new DescriptorFilter(null, null));
	}
	
	protected List<SubmodelDescriptor> getAllSubmodels(String id) {
		return storage.getAllSubmodels(id, new PaginationInfo(null, null)).getResult();
	}
	
	protected CursorResult<List<SubmodelDescriptor>> getAllSubmodelsWithPagination(String aasId, int limit, String cursor) {
		return storage.getAllSubmodels(aasId, new PaginationInfo(limit, cursor));
	}
}