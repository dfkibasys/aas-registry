package org.eclipse.basyx.aas.registry.service.test.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import org.eclipse.basyx.aas.registry.client.api.RegistryAndDiscoveryInterfaceApi;
import org.eclipse.basyx.aas.registry.client.api.AasRegistryPaths;
import org.eclipse.basyx.aas.registry.events.RegistryEvent;
import org.eclipse.basyx.aas.registry.events.RegistryEvent.EventType;
import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.basyx.aas.registry.model.Match;
import org.eclipse.basyx.aas.registry.model.Page;
import org.eclipse.basyx.aas.registry.model.ShellDescriptorSearchQuery;
import org.eclipse.basyx.aas.registry.model.ShellDescriptorSearchResponse;
import org.eclipse.basyx.aas.registry.model.SortDirection;
import org.eclipse.basyx.aas.registry.model.Sorting;
import org.eclipse.basyx.aas.registry.model.SortingPath;
import org.eclipse.basyx.aas.registry.model.SubmodelDescriptor;
import org.eclipse.basyx.aas.registry.service.test.util.TestResourcesLoader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class IntegrationTest {

	private static final DockerImageName KAFKA_TEST_IMAGE = DockerImageName.parse("confluentinc/cp-kafka:6.2.1");

	private static final DockerImageName ELASTICSEARCH_TEST_IMAGE = DockerImageName
			.parse("docker.elastic.co/elasticsearch/elasticsearch-oss:7.10.2");

	@LocalServerPort
	private Integer port;

	@Autowired
	public KafkaEventListener listener;

	@Rule
	@Autowired
	public TestResourcesLoader resourceLoader;

	private final RegistryAndDiscoveryInterfaceApi api = new RegistryAndDiscoveryInterfaceApi();

	public static KafkaContainer KAFKA = new KafkaContainer(KAFKA_TEST_IMAGE);

	public static ElasticsearchContainer ELASTIC_SEARCH = new ElasticsearchContainer(ELASTICSEARCH_TEST_IMAGE);

	@BeforeClass
	public static void startContainersInParallel() {
		Stream.of(KAFKA, ELASTIC_SEARCH).parallel().forEach(GenericContainer::start);
	}

	@AfterClass
	public static void stopContainersInParallel() {
		Stream.of(KAFKA, ELASTIC_SEARCH).parallel().forEach(GenericContainer::stop);
	}

	@After
	public void cleanup() {
		// kafka and elasticSearch containers need to be static because of the
		// @DynamicPropertySource-Method thus we need to cleanup the containers
		// when we reuse them
		List<AssetAdministrationShellDescriptor> all = api.getAllAssetAdministrationShellDescriptors();
		all.stream().map(AssetAdministrationShellDescriptor::getIdentification)
				.forEach(this::deleteAdminAssetShellDescriptor);
		listener.assertNoAdditionalMessage();
	}

	@DynamicPropertySource
	static void assignAdditionalProperties(DynamicPropertyRegistry registry) {
		registry.add("elasticsearch.url", ELASTIC_SEARCH::getHttpHostAddress);
		registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
	}

	@Before
	public void setup() {
		api.getApiClient().setBasePath("http://localhost:" + port);
	}

	@Test
	public void whenCreateAndDeleteDescriptors_thenAllDescriptorsAreRemoved()
			throws IOException, InterruptedException, TimeoutException {
		List<AssetAdministrationShellDescriptor> deployed = initialize();
		List<AssetAdministrationShellDescriptor> all = api.getAllAssetAdministrationShellDescriptors();
		assertThat(all).containsExactlyInAnyOrderElementsOf(deployed);

		all.stream().map(AssetAdministrationShellDescriptor::getIdentification)
				.forEach(this::deleteAdminAssetShellDescriptor);

		all = api.getAllAssetAdministrationShellDescriptors();
		assertThat(all).isEmpty();

		listener.assertNoAdditionalMessage();
	}

	@Test
	public void whenRegisterAndUnregisterSubmodel_thenSubmodelIsCreatedAndDeleted()
			throws IOException, InterruptedException, TimeoutException {
		initialize();
		List<AssetAdministrationShellDescriptor> deployed = initialize();
		List<AssetAdministrationShellDescriptor> all = api.getAllAssetAdministrationShellDescriptors();
		assertThat(all).asList().containsExactlyInAnyOrderElementsOf(deployed);

		SubmodelDescriptor toRegister = resourceLoader.loadSubmodel("toregister");
		String aasId = "1";
		ResponseEntity<SubmodelDescriptor> response = api.postSubmodelDescriptorWithHttpInfo(toRegister, aasId);
		assertThatEventWasSend(RegistryEvent.builder().id(aasId).submodelId(toRegister.getIdentification())
				.submodelDescriptor(toRegister).type(EventType.SUBMODEL_REGISTERED).build());
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		SubmodelDescriptor registered = response.getBody();
		assertThat(registered).isEqualTo(toRegister);

		SubmodelDescriptor resolved = api.getSubmodelDescriptorById(aasId, toRegister.getIdentification());
		assertThat(resolved).isEqualTo(registered);

		AssetAdministrationShellDescriptor aasDescriptor = api.getAssetAdministrationShellDescriptorById(aasId);
		assertThat(aasDescriptor.getSubmodelDescriptors()).contains(toRegister);

		ResponseEntity<Void> deleteResponse = api.deleteSubmodelDescriptorByIdWithHttpInfo(aasId,
				toRegister.getIdentification());
		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		assertThatEventWasSend(RegistryEvent.builder().id(aasId).submodelId(toRegister.getIdentification())
				.type(EventType.SUBMODEL_UNREGISTERED).build());

		aasDescriptor = api.getAssetAdministrationShellDescriptorById(aasId);
		assertThat(aasDescriptor.getSubmodelDescriptors()).doesNotContain(toRegister);

		listener.assertNoAdditionalMessage();
	}

	@Test
	public void whenInvalidInput_thenSuccessfullyValidated()
			throws IOException, InterruptedException, TimeoutException {
		initialize();
		assertThrows(HttpClientErrorException.class, () -> api.deleteSubmodelDescriptorByIdWithHttpInfo(null, null));
		assertThrows(HttpClientErrorException.class, () -> api.deleteAssetAdministrationShellDescriptorById(null));
		assertThrows(HttpClientErrorException.class, () -> api.getAllSubmodelDescriptors(null));
		assertThrows(HttpClientErrorException.class, () -> api.getAssetAdministrationShellDescriptorById(null));
		assertThrows(HttpClientErrorException.class, () -> api.putAssetAdministrationShellDescriptorById(null, null));
		assertThrows(HttpClientErrorException.class, () -> api.postAssetAdministrationShellDescriptor(null));
		assertThrows(HttpClientErrorException.class, () -> api.postSubmodelDescriptor(null, null));

		AssetAdministrationShellDescriptor descriptor = new AssetAdministrationShellDescriptor();
		descriptor.setIdShort("shortId");
		assertThrows(HttpClientErrorException.class, () -> api.postAssetAdministrationShellDescriptor(descriptor));

		descriptor.setIdentification("identification");
		HttpStatus status = api.postAssetAdministrationShellDescriptorWithHttpInfo(descriptor).getStatusCode();
		assertThat(status).isEqualTo(HttpStatus.CREATED);
		assertThatEventWasSend(RegistryEvent.builder().id(descriptor.getIdentification()).aasDescriptor(descriptor)
				.type(EventType.AAS_REGISTERED).build());
	}

	@Test
	public void whenSearchBySubmodelDescriptorId_thenGotResult() throws Exception {
		initialize();
		AssetAdministrationShellDescriptor expected = resourceLoader.loadAssetAdminShellDescriptor();
		String path = AasRegistryPaths.submodelDescriptors().idShort();
		ShellDescriptorSearchQuery query = new ShellDescriptorSearchQuery().match(new Match().path(path).value("sm4"));
		ResponseEntity<ShellDescriptorSearchResponse> response = api.searchShellDescriptorsWithHttpInfo(query);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		List<AssetAdministrationShellDescriptor> result = response.getBody().getHits();
		assertThat(result.size()).isEqualTo(1);
		assertThat(result.get(0)).isEqualTo(expected);
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
		ShellDescriptorSearchQuery query = new ShellDescriptorSearchQuery().sortBy(new Sorting()
				.addPathItem(SortingPath.IDSHORT).addPathItem(SortingPath.IDENTIFICATION).direction(SortDirection.ASC))
				.page(new Page().index(from).size(2));
		ShellDescriptorSearchResponse response = api.searchShellDescriptors(query);
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
	public void whenSearchWithSortingByIdShortAsc_thenReturnSortedAsc()
			throws IOException, InterruptedException, TimeoutException {
		whenSearchWithSortingByIdShort_thenReturnSorted(SortDirection.ASC);
	}
	
	@Test
	public void whenSearchWithSortingByIdNoSortOrder_thenReturnSortedAsc()
			throws IOException, InterruptedException, TimeoutException {
		whenSearchWithSortingByIdShort_thenReturnSorted(null);
	}

	@Test
	public void whenSearchWithSortingByIdShortDesc_thenReturnSortedDesc()
			throws IOException, InterruptedException, TimeoutException {
		whenSearchWithSortingByIdShort_thenReturnSorted(SortDirection.DESC);
	}

	private void whenSearchWithSortingByIdShort_thenReturnSorted(SortDirection direction)
			throws IOException, InterruptedException, TimeoutException {
		initialize();
		List<AssetAdministrationShellDescriptor> expected = resourceLoader.loadShellDescriptorList();
		String path = AasRegistryPaths.description().language();
		ShellDescriptorSearchQuery query = new ShellDescriptorSearchQuery().match(new Match().path(path).value("de-DE"))
				.sortBy(new Sorting().addPathItem(SortingPath.IDSHORT).addPathItem(SortingPath.ADMINISTRATION_REVISION)
						.direction(direction));
		ResponseEntity<ShellDescriptorSearchResponse> response = api.searchShellDescriptorsWithHttpInfo(query);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		List<AssetAdministrationShellDescriptor> result = response.getBody().getHits();
		Assert.assertEquals(expected.toString(), result.toString());
		assertThat(result).asList().isEqualTo(expected);
	}

	@Test
	public void whenIllegalArguments_thenResult() throws IOException, InterruptedException, TimeoutException {
		initialize();
		api.searchShellDescriptors(new ShellDescriptorSearchQuery());
	}

	
	private void deleteAdminAssetShellDescriptor(String aasId) {
		HttpStatus response = api.deleteAssetAdministrationShellDescriptorByIdWithHttpInfo(aasId).getStatusCode();
		assertThat(response).isEqualTo(HttpStatus.NO_CONTENT);
		assertThatEventWasSend(RegistryEvent.builder().id(aasId).type(EventType.AAS_UNREGISTERED).build());
	}

	private List<AssetAdministrationShellDescriptor> initialize()
			throws IOException, InterruptedException, TimeoutException {
		List<AssetAdministrationShellDescriptor> descriptors = resourceLoader.loadRepositoryDefinition();
		for (AssetAdministrationShellDescriptor eachDescriptor : descriptors) {
			ResponseEntity<AssetAdministrationShellDescriptor> response = api
					.postAssetAdministrationShellDescriptorWithHttpInfo(eachDescriptor);
			assertThat(response.getBody()).isEqualTo(eachDescriptor);
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
			assertThatEventWasSend(RegistryEvent.builder().id(eachDescriptor.getIdentification())
					.aasDescriptor(eachDescriptor).type(EventType.AAS_REGISTERED).build());
		}
		return descriptors;
	}

	private void assertThatEventWasSend(RegistryEvent build) {
		RegistryEvent evt = listener.poll();
		assertThat(evt).isEqualTo(build);
	}

	@Component
	public static class KafkaEventListener {

		private LinkedBlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

		@Autowired
		private ObjectMapper mapper;

		@KafkaListener(topics = "aas-registry", groupId = "test")
		public void receive(String message) {
			messageQueue.offer(message);
		}

		public void assertNoAdditionalMessage() {
			try {
				String message = messageQueue.poll(1, TimeUnit.SECONDS);
				if (message != null) {
					throw new RuntimeException("Got additional message: " + message);
				}
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		public RegistryEvent poll() {
			try {
				String message = messageQueue.poll(5, TimeUnit.SECONDS);
				if (message == null) {
					throw new RuntimeException("timeout");
				}
				return mapper.readValue(message, RegistryEvent.class);
			} catch (InterruptedException | JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}
	}
}