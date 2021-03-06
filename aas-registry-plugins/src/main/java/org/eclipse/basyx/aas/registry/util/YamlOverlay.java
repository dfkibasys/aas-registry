package org.eclipse.basyx.aas.registry.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.yaml.snakeyaml.Yaml;

@Mojo(name = "yaml-overlay", defaultPhase = LifecyclePhase.INITIALIZE)
public class YamlOverlay extends AbstractMojo {

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	private MavenProject project;

	@Parameter(property = "base")
	private File base;

	@Parameter(property = "overlay")
	private File overlay;

	@Parameter(property = "out")
	private File out;

	@Parameter(property = "charset", defaultValue = "UTF-8")
	private String charSet;


	public void execute() throws MojoExecutionException, MojoFailureException {
		Yaml yaml = new Yaml();
		try (BufferedReader bReaderBase = new BufferedReader(new FileReader(base, Charset.forName(charSet)));
				BufferedReader bReaderToInsert = new BufferedReader(new FileReader(overlay, Charset.forName(charSet)));
				BufferedWriter bWriter = new BufferedWriter(new FileWriter(out, Charset.forName(charSet)))) {
			Map<String, Object> baseContent = yaml.load(bReaderBase);
			Map<String, Object> overlayContent = yaml.load(bReaderToInsert);
			Map<String, Object> target = new LinkedHashMap<>();
			doOverlay(baseContent, overlayContent, target);
			yaml.dump(target, bWriter);
		} catch (IOException e) {
			throw new MojoExecutionException("Failed to combine files", e);
		}
	}

	@SuppressWarnings("unchecked")
	private void doOverlay(Map<String, Object> baseContent, Map<String, Object> overlayContent,
			Map<String, Object> target) throws MojoExecutionException {
		for (Entry<String, Object> eachBaseEntry : baseContent.entrySet()) {
			String key = eachBaseEntry.getKey();
			Object baseValue = eachBaseEntry.getValue();
			Object overlayValue = overlayContent.remove(key);
			if (overlayValue != null) {
				Class<?> baseClass = baseValue.getClass();
				Class<?> overlayClass = overlayValue.getClass();
				if (baseClass != overlayClass) {
					throw new MojoExecutionException(
							"Value conflict: Base: " + baseClass + " Overlay: " + overlayClass);
				}
				if (baseValue instanceof Map) {
					Map<String, Object> targetMap = new LinkedHashMap<>();
					target.put(key, targetMap);
					doOverlay((Map<String, Object>) baseValue, (Map<String, Object>) overlayValue, targetMap);
				} else {
					target.put(key, overlayValue);
				}
			} else {
				target.put(key, baseValue);
			}
			
		}
		// for now we just append the remaining entries
		overlayContent.forEach(target::put);
	}

	public void setProject(MavenProject project) {
		this.project = project;
	}

	public void setBase(File base) {
		this.base = base;
	}

	public void setOverlay(File overlay) {
		this.overlay = overlay;
	}

	public void setOut(File out) {
		this.out = out;
	}

	public void setCharSet(String charSet) {
		this.charSet = charSet;
	}
}