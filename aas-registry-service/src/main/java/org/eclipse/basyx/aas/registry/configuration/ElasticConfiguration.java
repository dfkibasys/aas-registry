package org.eclipse.basyx.aas.registry.configuration;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.basyx.aas.registry.repository.AtomicElasticSearchRepoAccess;
import org.eclipse.basyx.aas.registry.repository.PainlessAtomicElasticSearchRepoAccess;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import com.google.common.io.CharStreams;

import lombok.extern.log4j.Log4j2;

@Configuration
@EnableElasticsearchRepositories(basePackages = "org.eclipse.basyx.aas.registry.repository")
@Log4j2
public class ElasticConfiguration extends AbstractElasticsearchConfiguration {

	@Value("${elasticsearch.url}")
	private String elasticsearchUrl;

	@Override
	public RestHighLevelClient elasticsearchClient() {
		log.info("Connecting to elasticsearch server '" + elasticsearchUrl + "' ...");
		ClientConfiguration clientConfiguration = ClientConfiguration.builder().connectedTo(elasticsearchUrl).withSocketTimeout(Duration.ofSeconds(300)).build();
		return RestClients.create(clientConfiguration).rest();
	}

	@Bean
	public ElasticSearchScripts scripts() {
		return new PainlessElasticSearchScripts();
	}
	
	@Bean
	public AtomicElasticSearchRepoAccess extension(ApplicationContext context, ElasticsearchOperations ops, ElasticSearchScripts scripts,  RestHighLevelClient client, ElasticsearchConverter converter) {
		return new PainlessAtomicElasticSearchRepoAccess(ops, scripts, client, converter);
	}

	public static interface ElasticSearchScripts {

		String STORE_ASSET_ADMIN_SUBMODULE = "storeAssetAdministrationSubModel.painless";
		String REMOVE_ASSET_ADMIN_SUBMODULE = "removeAssetAdministrationSubModel.painless"; 
			
		String loadResourceAsString(String path);
		
		String getLanguage();
	}
	
	public static class PainlessElasticSearchScripts implements ElasticSearchScripts {
		
		private final Map<String, String> loadedScripts = new ConcurrentHashMap<>();
		
		
		@Override
		public String loadResourceAsString(String path) {
			return loadedScripts.computeIfAbsent(path, this::loadResourceFromJar);
		}
		
		private String loadResourceFromJar(String path) {
			try (InputStream in = ElasticSearchScripts.class.getResourceAsStream(path);
					BufferedInputStream bIn = new BufferedInputStream(in);
					InputStreamReader reader = new InputStreamReader(bIn, StandardCharsets.UTF_8)) {
				return CharStreams.toString(reader);
			} catch (IOException ex) {
				throw new ResourceLoadingException(path, ex);
			}	
		}

		@Override
		public String getLanguage() {
			return "painless";
		}
	}
}
