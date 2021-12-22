package org.eclipse.basyx.aas.registry.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

@Mojo(name = "simple-json-path-generator", requiresDependencyResolution = ResolutionScope.COMPILE, defaultPhase = LifecyclePhase.INITIALIZE)
public class SimpleJsonPathGenerator extends AbstractMojo {

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	private MavenProject project;

	@Parameter(property = "className")
	private String className;

	@Parameter(property = "targetClassName")
	private String targetClassName;

	@Parameter(property = "targetPackageName")
	private String targetPackageName;

	@Parameter(property = "targetSourceFolder")
	private File targetSourceFolder;

	@Parameter(property = "charset", defaultValue = "UTF-8")
	private String charSet;

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public void setTargetClassName(String targetClassName) {
		this.targetClassName = targetClassName;
	}

	public void setTargetPackageName(String targetPackageName) {
		this.targetPackageName = targetPackageName;
	}

	public void setTargetSourceFolder(File targetSourceFolder) {
		this.targetSourceFolder = targetSourceFolder;
	}

	public void setCharSet(String charSet) {
		this.charSet = charSet;
	}

	public String getTargetPackageName(Class<?> cls) {
		if (targetPackageName == null) {
			return cls.getPackageName();
		}
		return targetPackageName;
	}

	public String getTargetClassName(Class<?> cls) {
		if (targetClassName == null) {
			return cls.getSimpleName() + "JacksonPaths";
		}
		return targetClassName;
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			URL[] urlsForClassLoader = getClassLoaderUrls();
			try (URLClassLoader loader = new URLClassLoader(urlsForClassLoader,
					SimpleJsonPathGenerator.class.getClassLoader())) {
				generateClass(loader);
			}
		} catch (ClassNotFoundException | IOException | DependencyResolutionRequiredException ex) {
			throw new MojoExecutionException("Failed to load mojo.", ex);
		}
	}

	private void generateClass(URLClassLoader loader) throws IOException, ClassNotFoundException {
		MustacheFactory mf = new DefaultMustacheFactory();
		Mustache mustache = mf.compile("jackson-path-util.mustache");
		File outputFile = prepareOutputFile();
		try (FileWriter fOut = new FileWriter(outputFile, Charset.forName(charSet));
				BufferedWriter writer = new BufferedWriter(fOut)) {
			Class<?> cls = loader.loadClass(className);
			Map<String, Object> context = buildContext(cls);
			mustache.execute(writer, context);
		}
	}

	private File prepareOutputFile() throws IOException {
		File targetFolder = new File(targetSourceFolder, targetPackageName.replace('.', File.separatorChar));
		if (targetFolder.mkdirs()) {

		}
		File targetFile = new File(targetFolder, targetClassName + ".java");
		if (targetFile.createNewFile()) {
			getLog().info("File " + targetFile + " created!");
		}
		return targetFile;
	}

	@SuppressWarnings("unchecked")
	private URL[] getClassLoaderUrls() throws MalformedURLException, DependencyResolutionRequiredException {
		List<URL> pathUrls = new ArrayList<>();
		for (String mavenCompilePath : (List<String>) project.getCompileClasspathElements()) {
			pathUrls.add(new File(mavenCompilePath).toURI().toURL());
		}
		return pathUrls.toArray(new URL[pathUrls.size()]);
	}

	private Map<String, Object> buildContext(Class<?> inputCls) {
		return Map.of("target", buildTarget(inputCls), "info", buildInfo(inputCls));
	}

	private Map<String, String> buildTarget(Class<?> inputCls) {
		HashMap<String, String> target = new HashMap<>();
		target.put("packageName", getTargetPackageName(inputCls));
		target.put("className", getTargetClassName(inputCls));
		return target;
	}

	public void setProject(MavenProject project) {
		this.project = project;
	}

	public Set<Entry<String, String>> buildInfo(Class<?> cls) {
		Map<String, String> target = new HashMap<>();
		fillInfoFor(cls, null, target);
		Set<Entry<String, String>> toReturn = new TreeSet<>(Comparator.comparing(Entry::getKey));
		toReturn.addAll(target.entrySet());
		return toReturn;
	}

	private void fillInfoFor(Class<?> cls, String currentPath, Map<String, String> target) {
		for (Field eachField : cls.getDeclaredFields()) {
			fillInfoFor(eachField, currentPath, target);
		}
	}

	private void fillInfoFor(Field eachField, String currentPath, Map<String, String> target) {
		if (Modifier.isStatic(eachField.getModifiers())) {
			return;
		}
		String fieldName = getPathSegment(eachField);
		String newPath = deliverPath(currentPath, fieldName, target);
		Class<?> type = eachField.getType();
		if (type.isPrimitive() || type.equals(String.class)) {
			return;
		} else if (List.class.isAssignableFrom(type)) {
			type = getGenericClass(eachField, 0);
		} else if (Map.class.isAssignableFrom(type)) {
			type = getGenericClass(eachField, 1);
		}
		fillInfoFor(type, newPath, target);
	}

	private Class<?> getGenericClass(Field field, int pos) {
		ParameterizedType genType = (ParameterizedType) field.getGenericType();
		return (Class<?>) genType.getActualTypeArguments()[pos];
	}

	private String getPathSegment(Field eachField) {
		return Optional.ofNullable(eachField.getDeclaredAnnotation(JsonProperty.class)).map(JsonProperty::value)
				.orElse(eachField.getName());
	}

	private String deliverPath(String currentPath, String fieldName, Map<String, String> target) {
		String value = currentPath == null ? fieldName : String.join(".", currentPath, fieldName);
		String key = value.toUpperCase().replace('.', '_');
		target.put(key, value);
		return value;
	}
}