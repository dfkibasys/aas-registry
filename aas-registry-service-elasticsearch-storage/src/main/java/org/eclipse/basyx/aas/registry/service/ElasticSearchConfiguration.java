package org.eclipse.basyx.aas.registry.service;

import java.time.Duration;

import org.eclipse.basyx.aas.registry.service.storage.AasRegistryStorage;
import org.eclipse.basyx.aas.registry.service.storage.elasticsearch.AtomicElasticSearchRepoAccess;
import org.eclipse.basyx.aas.registry.service.storage.elasticsearch.ElasticSearchAasRegistryStorage;
import org.eclipse.basyx.aas.registry.service.storage.elasticsearch.PainlessAtomicElasticSearchRepoAccess;
import org.eclipse.basyx.aas.registry.service.storage.elasticsearch.PainlessElasticSearchScripts;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import lombok.extern.log4j.Log4j2;

@Configuration
@EnableElasticsearchRepositories(basePackages = "org.eclipse.basyx.aas.registry.service.storage.elasticsearch")
@Log4j2
@ConditionalOnProperty(prefix = "registry", name = "type", havingValue = "elasticsearch")
public class ElasticSearchConfiguration extends AbstractElasticsearchConfiguration {

	@Value("${elasticsearch.url}")
	private String elasticsearchUrl;

	@Override
	public RestHighLevelClient elasticsearchClient() {
		log.info("Connecting to elasticsearch server '" + elasticsearchUrl + "' ...");
		ClientConfiguration clientConfiguration = ClientConfiguration.builder().connectedTo(elasticsearchUrl).withSocketTimeout(Duration.ofSeconds(300)).build();
		return RestClients.create(clientConfiguration).rest();
	}

	@Bean
	public AtomicElasticSearchRepoAccess extension(ApplicationContext context, ElasticsearchOperations ops, ElasticsearchConverter converter) {
		return new PainlessAtomicElasticSearchRepoAccess(ops, new PainlessElasticSearchScripts(), converter);
	}

	@Bean
	public AasRegistryStorage storage() {
		return new ElasticSearchAasRegistryStorage();
	}
}
