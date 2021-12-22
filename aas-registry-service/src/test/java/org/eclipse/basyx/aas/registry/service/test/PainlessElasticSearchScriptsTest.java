package org.eclipse.basyx.aas.registry.service.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.eclipse.basyx.aas.registry.configuration.ResourceLoadingException;
import org.eclipse.basyx.aas.registry.configuration.ElasticConfiguration.ElasticSearchScripts;
import org.eclipse.basyx.aas.registry.configuration.ElasticConfiguration.PainlessElasticSearchScripts;
import org.junit.Test;

public class PainlessElasticSearchScriptsTest {

	@Test
	public void whenUnknownResourceRequested_thenThrowException() {
		PainlessElasticSearchScripts scripts = new PainlessElasticSearchScripts();
		assertThatExceptionOfType(ResourceLoadingException.class).isThrownBy(()->scripts.loadResourceAsString("unknown"));
	}
	
	@Test
	public void whenStoreResourcesRequested_thenSuccess() {
		PainlessElasticSearchScripts scripts = new PainlessElasticSearchScripts();
		assertThat(scripts.loadResourceAsString(ElasticSearchScripts.STORE_ASSET_ADMIN_SUBMODULE)).isNotEmpty();
	}
	
	@Test
	public void whenRemoveResourcesRequested_thenSuccess() {
		PainlessElasticSearchScripts scripts = new PainlessElasticSearchScripts();
		assertThat(scripts.loadResourceAsString(ElasticSearchScripts.REMOVE_ASSET_ADMIN_SUBMODULE)).isNotEmpty();
	}
}
