package org.eclipse.basyx.aas.registry.service.tests;

import org.assertj.core.api.Assertions;
import org.eclipse.basyx.aas.registry.service.storage.elasticsearch.PainlessElasticSearchScripts;
import org.eclipse.basyx.aas.registry.service.storage.elasticsearch.ResourceLoadingException;
import org.junit.Test;

public class PainlessElasticSearchScriptsTest {

	@Test
	public void whenUnknownResourceRequested_thenThrowException() {
		PainlessElasticSearchScripts scripts = new PainlessElasticSearchScripts();
		Assertions.assertThatExceptionOfType(ResourceLoadingException.class).isThrownBy(() -> scripts.loadResourceAsString("unknown"));
	}

	@Test
	public void whenStoreResourcesRequested_thenSuccess() {
		PainlessElasticSearchScripts scripts = new PainlessElasticSearchScripts();
		Assertions.assertThat(scripts.loadResourceAsString(PainlessElasticSearchScripts.STORE_ASSET_ADMIN_SUBMODULE)).isNotEmpty();
	}

	@Test
	public void whenRemoveResourcesRequested_thenSuccess() {
		PainlessElasticSearchScripts scripts = new PainlessElasticSearchScripts();
		Assertions.assertThat(scripts.loadResourceAsString(PainlessElasticSearchScripts.REMOVE_ASSET_ADMIN_SUBMODULE)).isNotEmpty();
	}
}