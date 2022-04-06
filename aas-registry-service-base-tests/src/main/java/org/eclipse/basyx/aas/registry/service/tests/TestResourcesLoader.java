package org.eclipse.basyx.aas.registry.service.tests;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.eclipse.basyx.aas.registry.events.RegistryEvent;
import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.basyx.aas.registry.model.SubmodelDescriptor;
import org.junit.rules.TestName;
import org.junit.runner.Description;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

public class TestResourcesLoader extends TestName {

	private static final String JSON_FILE_ENDING = ".json";

	private final JacksonReaders readerSupport = new JacksonReaders(new ObjectMapper());

	private String packageName;

	public TestResourcesLoader(String packageName) {
		this.packageName = packageName;
	}
	
	public TestResourcesLoader() {
	}

	@Override
	protected void starting(Description d) {
		super.starting(d);
		if (packageName == null) {
			packageName = d.getTestClass().getPackageName();
		}
	}

	public List<AssetAdministrationShellDescriptor> loadRepositoryDefinition() throws IOException {
		String path = getTestRepositoryPath();
		return load(path, readerSupport.getShellDescriptorListReader());
	}

	public List<AssetAdministrationShellDescriptor> loadShellDescriptorList() throws IOException {
		return loadShellDescriptorList(null);
	}

	public List<AssetAdministrationShellDescriptor> loadShellDescriptorList(String suffix) throws IOException {
		return load(readerSupport.getShellDescriptorListReader(), suffix != null ? "_" + suffix : "");
	}

	public List<SubmodelDescriptor> loadSubmodelList() throws IOException {
		return load(readerSupport.getSubModelListReader(), "");
	}

	public SubmodelDescriptor loadSubmodel(String suffix) throws IOException {
		return load(readerSupport.getSubModelReader(), suffix != null ? "_" + suffix : "");
	}

	public SubmodelDescriptor loadSubmodel() throws IOException {
		return loadSubmodel(null);
	}

	public AssetAdministrationShellDescriptor loadAssetAdminShellDescriptor() throws IOException {
		return load(readerSupport.getShellDescriptorReader(), "");
	}

	public RegistryEvent loadEvent() throws IOException {
		String eventPath = getMethodName() + "_event" + JSON_FILE_ENDING;
		return load(eventPath, readerSupport.getRegistryEventReader());
	}

	private String getTestRepositoryPath() {
		String repoPath = getMethodName() + "_repo" + JSON_FILE_ENDING;
		String basedOnTestClass = basedOnClassInTest(repoPath);
		if (getClass().getResource(basedOnTestClass) != null) {
			return repoPath;
		}
		return "default_repository.json";
	}

	private String basedOnClassInTest(String relativePath) {
		return "/" + packageName.replace('.', '/') + "/" + relativePath;
	}

	private <T> T load(ObjectReader reader, String suffix) throws IOException {
		String expectedPath = getMethodName() + suffix + JSON_FILE_ENDING;
		return load(expectedPath, reader);
	}

	private <T> T load(String path, ObjectReader reader) throws IOException {
		path = basedOnClassInTest(path);
		try (InputStream in = getClass().getResourceAsStream(path)) {
			try (BufferedInputStream bIn = new BufferedInputStream(in)) {
				return reader.readValue(bIn);
			}
		}
	}

}
