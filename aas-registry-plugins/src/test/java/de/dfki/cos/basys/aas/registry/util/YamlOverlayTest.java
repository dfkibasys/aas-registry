package de.dfki.cos.basys.aas.registry.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Assert;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

public class YamlOverlayTest {

	@Test
	public void testSimpleOverlay() throws IOException, MojoExecutionException {
		new TestRunner().testOverlay("/de/dfki/cos/basys/aas/registry/util/simple");
	}

	@Test
	public void testArrayOverlay() throws MojoExecutionException, IOException {
		new TestRunner().testOverlay("/de/dfki/cos/basys/aas/registry/util/listinstructions");
	}

	private static class TestRunner {

		public void testOverlay(String path) throws IOException, MojoExecutionException {
			try (BufferedReader baseIn = buildReader(path, "base");
					BufferedReader overlayIn = buildReader(path, "overlay");
					BufferedReader expectedIn = buildReader(path, "expected")) {
				Yaml yaml = new Yaml();
				Map<String, Object> base = yaml.load(baseIn);
				Map<String, Object> overlay = yaml.load(overlayIn);
				Map<String, Object> expected = yaml.load(expectedIn);
				Map<String, Object> result = new HashMap<>();
				YamlOverlay overlayer = new YamlOverlay();
				overlayer.doOverlay(base, overlay, result);

				Assert.assertEquals(expected, result);
			}
		}
		
		private BufferedReader buildReader(String path, String name) {
			InputStream resourceStream = TestRunner.class.getResourceAsStream(path + "/" + name + ".yaml");
			InputStreamReader resourceReader = new InputStreamReader(resourceStream, StandardCharsets.UTF_8);
			return new BufferedReader(resourceReader);
		}
		
		private BufferedReader buildWriter(String path, String name) {
			InputStream resourceStream = TestRunner.class.getResourceAsStream(path + "/" + name + ".yaml");
			InputStreamReader resourceReader = new InputStreamReader(resourceStream, StandardCharsets.UTF_8);
			return new BufferedReader(resourceReader);
		}
	}
}
