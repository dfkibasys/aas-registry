package org.eclipse.basyx.aas.registry.service.tests;

import org.eclipse.basyx.aas.registry.service.ElasticSearchConfiguration;
import org.junit.ClassRule;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

@TestPropertySource(properties = { "registry.type=elasticsearch", "logging.level.org=INFO"})
@ContextConfiguration(classes = { ElasticSearchConfiguration.class })
public class ElasticSearchAasRegistryStorageTest extends AasRegistryStorageTest {

	private static final DockerImageName ELASTICSEARCH_TEST_IMAGE = DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch-oss:7.10.2");

	@ClassRule
	public static ElasticsearchContainer ELASTICSEARCH_CONTAINER = new ElasticsearchContainer(ELASTICSEARCH_TEST_IMAGE);

	@DynamicPropertySource
	static void assignAdditionalProperties(DynamicPropertyRegistry registry) {
		registry.add("elasticsearch.url", ELASTICSEARCH_CONTAINER::getHttpHostAddress);
	}
}