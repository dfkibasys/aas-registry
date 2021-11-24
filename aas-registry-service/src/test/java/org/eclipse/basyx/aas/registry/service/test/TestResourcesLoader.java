package org.eclipse.basyx.aas.registry.service.test;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.eclipse.basyx.aas.registry.event.RegistryEvent;
import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.basyx.aas.registry.model.SubmodelDescriptor;
import org.junit.rules.TestName;

import com.fasterxml.jackson.databind.ObjectReader;

public class TestResourcesLoader extends TestName {

	private static final String JSON_FILE_ENDING = ".json";

	public List<AssetAdministrationShellDescriptor> getRepositoryDefinition() throws IOException {
		String path = getTestRepositoryPath();
		return load(path, JacksonParsers.getShellDescriptorListReader());
	}

	public List<AssetAdministrationShellDescriptor> loadShellDescriptorList() throws IOException {
		return loadExpected(JacksonParsers.getShellDescriptorListReader());
	}

	public List<SubmodelDescriptor> loadSubmodelList() throws IOException {
		return loadExpected(JacksonParsers.getSubModelListReader());
	}

	public SubmodelDescriptor loadSubmodel() throws IOException {
		return loadExpected(JacksonParsers.getSubModelReader());
	}

	public AssetAdministrationShellDescriptor loadAssetAdminShellDescriptor() throws IOException {
		return loadExpected(JacksonParsers.getShellDescriptorReader());
	}
	
	public RegistryEvent loadEvent() throws IOException {
		String eventPath = getMethodName() + "_event" + JSON_FILE_ENDING;
		return load(eventPath, JacksonParsers.getRegistryEventReader());
	}

	private String getTestRepositoryPath() {
		String repoPath = getMethodName() + "_repo" + JSON_FILE_ENDING;
		if (getClass().getResource(repoPath) != null) {
			return repoPath;
		}
		return "default_repository.json";
	}
	
	private <T> T loadExpected(ObjectReader reader) throws IOException {
		String expectedPath = getMethodName() + JSON_FILE_ENDING;
		return load(expectedPath, reader);
	}

	private <T> T load(String path, ObjectReader reader) throws IOException {
		try (InputStream in = getClass().getResourceAsStream(path);
				BufferedInputStream bIn = new BufferedInputStream(in)) {
			return reader.readValue(bIn);
		}
	}

}
