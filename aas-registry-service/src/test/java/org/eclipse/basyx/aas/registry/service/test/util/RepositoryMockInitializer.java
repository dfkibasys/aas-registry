package org.eclipse.basyx.aas.registry.service.test.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.SerializationUtils;
import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptorEnvelop;
import org.eclipse.basyx.aas.registry.repository.AssetAdministrationShellDescriptorRepository;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RepositoryMockInitializer extends TestWatcher {

	private final AssetAdministrationShellDescriptorRepository repo;

	private Map<String, AssetAdministrationShellDescriptorEnvelop> repoContent;

	private final TestResourcesLoader loader;

	@Override
	protected void starting(Description description) {
		try {
			List<AssetAdministrationShellDescriptor> descriptors = loader.loadRepositoryDefinition();
			initialize(descriptors);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void initialize(List<AssetAdministrationShellDescriptor> content) throws IOException {
		repoContent = content.stream().map(AssetAdministrationShellDescriptorEnvelop::new)
				.collect(Collectors.toMap(AssetAdministrationShellDescriptorEnvelop::getId, Function.identity()));
		prepareFindAll();
		prepareFindById();
		prepareExistsById();
		prepareSave();
		prepareDeleteById();
	}

	private void prepareSave() {
		Answer<AssetAdministrationShellDescriptorEnvelop> answer = invocation -> {
			AssetAdministrationShellDescriptorEnvelop envelop = invocation.getArgument(0);
			AssetAdministrationShellDescriptorEnvelop toStore = SerializationUtils.clone(envelop);
			repoContent.put(toStore.getId(), toStore);
			return SerializationUtils.clone(toStore);
		};
		Mockito.when(repo.save(Mockito.any(AssetAdministrationShellDescriptorEnvelop.class))).then(answer);
	}

	private Map<String, AssetAdministrationShellDescriptorEnvelop> cloneRepo()
			throws JsonMappingException, JsonProcessingException {
		Map<String, AssetAdministrationShellDescriptorEnvelop> toReturn = new HashMap<>();
		for (Entry<String, AssetAdministrationShellDescriptorEnvelop> eachEntry : repoContent.entrySet()) {
			AssetAdministrationShellDescriptorEnvelop clone = SerializationUtils.clone(eachEntry.getValue());
			toReturn.put(clone.getId(), clone);
		}
		return toReturn;
	}

	private void prepareDeleteById() {
		Answer<Void> answer = invocation -> {
			String id = invocation.getArgument(0);
			repoContent.remove(id);
			return null;
		};
		Mockito.doAnswer(answer).when(repo).deleteById(Mockito.anyString());
	}

	private void prepareExistsById() {
		Answer<Boolean> answer = invocation -> {
			String id = invocation.getArgument(0);
			return repoContent.containsKey(id);
		};
		Mockito.when(repo.existsById(Mockito.anyString())).thenAnswer(answer);
	}

	private void prepareFindById() {
		Answer<Optional<AssetAdministrationShellDescriptorEnvelop>> answer = invocation -> {
			String id = invocation.getArgument(0);
			return Optional.ofNullable(cloneRepo().get(id));
		};
		Mockito.when(repo.findById(Mockito.anyString())).thenAnswer(answer);
	}

	private void prepareFindAll() {
		Mockito.when(repo.findAll()).thenAnswer(i -> cloneRepo().values());
	}

}
