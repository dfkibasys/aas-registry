package org.eclipse.basyx.aas.registry.service.test.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.eclipse.basyx.aas.registry.configuration.ElasticConfiguration;
import org.eclipse.basyx.aas.registry.configuration.ElasticConfiguration.PainlessElasticSearchScripts;
import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.basyx.aas.registry.repository.AssetAdministrationShellDescriptorRepository;
import org.eclipse.basyx.aas.registry.repository.AtomicElasticSearchRepoAccess;
import org.eclipse.basyx.aas.registry.service.test.util.RegistryTestObjects;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.query.UpdateResponse.Result;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = { PainlessElasticSearchScripts.class, ObjectMapper.class, ElasticConfiguration.class })
@RunWith(SpringRunner.class)
public class ElasticSearchTest {

	private static final String ID_2_2 = "2.2";

	private static final String ID_2 = "2";

	private static final String ID_SHORT1B = "short1b";

	private static final String ID_SHORT2 = "short2";

	private static final String ID_1_2 = "1.2";

	private static final String IDSHORT_1 = "short1";

	private static final String ID_1_1 = "1.1";

	private static final String ID_1 = "1";

	private static final DockerImageName ELASTICSEARCH_TEST_IMAGE = DockerImageName
			.parse("docker.elastic.co/elasticsearch/elasticsearch-oss:7.10.2");

	@ClassRule
	public static ElasticsearchContainer ELASTIC_SEARCH = new ElasticsearchContainer(ELASTICSEARCH_TEST_IMAGE);

	@DynamicPropertySource
	static void assignAdditionalProperties(DynamicPropertyRegistry registry) {
		registry.add("elasticsearch.url", ELASTIC_SEARCH::getHttpHostAddress);
	}

	@Autowired
	private AtomicElasticSearchRepoAccess access;

	@Autowired
	private AssetAdministrationShellDescriptorRepository repo;
	

	@Test
	public void whenSubmodelAddedOrOverridden_thenModelIsApplied() throws IOException {
		AssetAdministrationShellDescriptor input = RegistryTestObjects.newDescriptor(ID_1);
		AssetAdministrationShellDescriptor result = repo.save(input);
		assertThat(result).isEqualTo(input);

		access.storeAssetAdministrationSubmodel(ID_1, RegistryTestObjects.newSubmodelDescriptorWithIdShort(ID_1_1, IDSHORT_1));
		access.storeAssetAdministrationSubmodel(ID_1, RegistryTestObjects.newSubmodelDescriptorWithIdShort(ID_1_2, ID_SHORT2));
		access.storeAssetAdministrationSubmodel(ID_1, RegistryTestObjects.newSubmodelDescriptorWithIdShort(ID_1_1, ID_SHORT1B));

		AssetAdministrationShellDescriptor descriptor = repo.findById(ID_1).get();

		AssetAdministrationShellDescriptor expected = RegistryTestObjects.newDescriptor(ID_1);
		expected.addSubmodelDescriptorsItem(RegistryTestObjects.newSubmodelDescriptorWithIdShort(ID_1_1, ID_SHORT1B));
		expected.addSubmodelDescriptorsItem(RegistryTestObjects.newSubmodelDescriptorWithIdShort(ID_1_2, ID_SHORT2));

		com.fasterxml.jackson.databind.ObjectWriter writer = new ObjectMapper()
				.setSerializationInclusion(Include.NON_NULL).writerWithDefaultPrettyPrinter();
		assertThat(writer.writeValueAsString(descriptor)).isEqualTo(writer.writeValueAsString(expected));
	}

	@Test
	public void whenSubmodelRemoved_thenUnavailable() throws IOException {
		AssetAdministrationShellDescriptor input = RegistryTestObjects.newDescriptor(ID_1);
		input.addSubmodelDescriptorsItem(RegistryTestObjects.newSubmodelDescriptorWithIdShort(ID_1_1, IDSHORT_1));
		input.addSubmodelDescriptorsItem(RegistryTestObjects.newSubmodelDescriptorWithIdShort(ID_1_2, ID_SHORT2));
		repo.save(input);

		Result result = access.removeAssetAdministrationSubmodel(ID_1, ID_1_2);
		assertThat(result).isEqualTo(Result.UPDATED);

		result = access.removeAssetAdministrationSubmodel(ID_1, ID_1_2);
		assertThat(result).isEqualTo(Result.NOOP);

		AssetAdministrationShellDescriptor descriptor = repo.findById(ID_1).get();
		AssetAdministrationShellDescriptor expected = RegistryTestObjects.newDescriptor(ID_1);
		expected.addSubmodelDescriptorsItem(RegistryTestObjects.newSubmodelDescriptorWithIdShort(ID_1_1, IDSHORT_1));

		com.fasterxml.jackson.databind.ObjectWriter writer = new ObjectMapper()
				.setSerializationInclusion(Include.NON_NULL).writerWithDefaultPrettyPrinter();
		assertThat(writer.writeValueAsString(descriptor)).isEqualTo(writer.writeValueAsString(expected));

		result = access.removeAssetAdministrationSubmodel(ID_2, ID_2_2);
		assertThat(result).isEqualTo(Result.NOT_FOUND);

		assertThat(writer.writeValueAsString(descriptor)).isEqualTo(writer.writeValueAsString(expected));
	}
}