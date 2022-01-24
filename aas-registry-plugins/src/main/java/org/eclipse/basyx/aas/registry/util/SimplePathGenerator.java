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

	@Parameter(property = "targetClassName")
	private String targetClassName;

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
					SimplePathGenerator.class.getClassLoader())) {
				generateClass(loader);
			}
		} catch (ClassNotFoundException | IOException | DependencyResolutionRequiredException ex) {
			throw new MojoExecutionException("Failed to load mojo.", ex);
		}
	}

	private void generateClass(ClassLoader loader) throws IOException, ClassNotFoundException {
		MustacheFactory mf = new DefaultMustacheFactory();
		Mustache mustache = mf.compile("simple-path.mustache");
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
			getLog().info("Target folder created");
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
		PathInfoGenerator generator = new PathInfoGenerator(inputCls);
		GenerationTarget target = new GenerationTarget(targetPackageName, targetClassName);
		PathInfo info = generator.generate(target);
		return Map.of("info", info);
	}

}