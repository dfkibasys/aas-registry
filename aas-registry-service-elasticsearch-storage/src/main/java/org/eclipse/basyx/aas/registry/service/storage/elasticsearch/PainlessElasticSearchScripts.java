package org.eclipse.basyx.aas.registry.service.storage.elasticsearch;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.io.CharStreams;

public class PainlessElasticSearchScripts {

	public static final String STORE_ASSET_ADMIN_SUBMODULE = "storeAssetAdministrationSubModel.painless";
	public static final String REMOVE_ASSET_ADMIN_SUBMODULE = "removeAssetAdministrationSubModel.painless";

	private final Map<String, String> loadedScripts = new ConcurrentHashMap<>();

	public String loadResourceAsString(String path) {
		return loadedScripts.computeIfAbsent(path, this::loadResourceFromJar);
	}

	private String loadResourceFromJar(String path) {
		try (InputStream in = PainlessElasticSearchScripts.class.getResourceAsStream(path); BufferedInputStream bIn = new BufferedInputStream(in); InputStreamReader reader = new InputStreamReader(bIn, StandardCharsets.UTF_8)) {
			return CharStreams.toString(reader);
		} catch (IOException ex) {
			throw new ResourceLoadingException(path, ex);
		}
	}

	public String getLanguage() {
		return "painless";
	}
}
