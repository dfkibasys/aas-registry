package org.eclipse.basyx.aas.registry.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.eclipse.basyx.aas.registry.util.path.GenerationTarget;
import org.eclipse.basyx.aas.registry.util.path.PathInfo;
import org.eclipse.basyx.aas.registry.util.path.PathInfoGenerator;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import lombok.Getter;
import lombok.Setter;

@Mojo(name = "simple-path-generator", requiresDependencyResolution = ResolutionScope.COMPILE, defaultPhase = LifecyclePhase.INITIALIZE)
@Getter
@Setter
public class SimplePathGenerator extends AbstractMojo {

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	private MavenProject project;

	@Parameter(property = "className")
	private String className;

	@Parameter(property = "pathsTargetClassName")
	private String pathsTargetClassName;

	@Parameter(property = "processorTargetClassName")
	private String processorTargetClassName;

	@Parameter(property = "targetPackageName")
	private String targetPackageName;

	@Parameter(property = "targetSourceFolder")
	private File targetSourceFolder;

	@Parameter(property = "charset", defaultValue = "UTF-8")
	private String charSet;

	public String getTargetPackageName(Class<?> cls) {
		if (targetPackageName == null) {
			return cls.getPackageName();
		}
		return targetPackageName;
	}

	public String getPathsTargetClassName(Class<?> cls) {
		if (pathsTargetClassName == null) {
			return cls.getSimpleName() + "JacksonPaths";
		}
		return pathsTargetClassName;
	}

	public String getProcessorTargetClassName(Class<?> cls) {
		if (processorTargetClassName == null) {
			return cls.getSimpleName() + "PathsProcessor";
		}
		return processorTargetClassName;
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			URL[] urlsForClassLoader = getClassLoaderUrls();
			try (URLClassLoader loader = new URLClassLoader(urlsForClassLoader, SimplePathGenerator.class.getClassLoader())) {
				generateClasses(loader);
			}
		} catch (ClassNotFoundException | IOException | DependencyResolutionRequiredException ex) {
			throw new MojoExecutionException("Failed to load mojo.", ex);
		}
	}

	void generateClasses(ClassLoader loader) throws IOException, ClassNotFoundException {
		Class<?> inputCls = loader.loadClass(className);

		PathInfoGenerator generator = new PathInfoGenerator(inputCls);
		PathInfo info = generator.generate();
		info.setPathsTarget(new GenerationTarget(targetPackageName, pathsTargetClassName));
		info.setProcessorTarget(new GenerationTarget(targetPackageName, processorTargetClassName));

		generateClass(info, "simple-path.mustache", getPathsTargetClassName());
		generateClass(info, "simple-path-processor.mustache", getProcessorTargetClassName());
	}

	private void generateClass(PathInfo info, String path, String outputFileName) throws IOException {
		MustacheFactory mf = new DefaultMustacheFactory();
		Mustache mustache = mf.compile(path);
		File outputFile = prepareOutputFile(outputFileName);
		String charSetOrDefault = charSet == null ? "UTF-8" : charSet;
		try (FileWriter fOut = new FileWriter(outputFile, Charset.forName(charSetOrDefault)); BufferedWriter writer = new BufferedWriter(fOut)) {
			Map<String, Object> context = Map.of("info", info);
			mustache.execute(writer, context);
		}
	}

	private File prepareOutputFile(String name) throws IOException {
		File targetFolder = new File(targetSourceFolder, targetPackageName.replace('.', File.separatorChar));
		if (targetFolder.mkdirs()) {
			getLog().info("Target folder created");
		}
		File targetFile = new File(targetFolder, name + ".java");
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
}