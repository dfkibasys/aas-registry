package org.eclipse.basyx.aas.registry.service.test.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.SerializationUtils;
import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptorEnvelop;
import org.eclipse.basyx.aas.registry.model.SubmodelDescriptor;
import org.eclipse.basyx.aas.registry.repository.AssetAdministrationShellDescriptorRepository;
import org.eclipse.basyx.aas.registry.repository.AtomicElasticSearchRepoAccess;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.data.elasticsearch.core.query.UpdateResponse.Result;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RepositoryMockInitializer extends TestWatcher {

	private final AssetAdministrationShellDescriptorRepository repo;

	private Map<String, AssetAdministrationShellDescriptorEnvelop> repoContent;

	private final TestResourcesLoader loader;

	private final AtomicElasticSearchRepoAccess repoAccess;

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
		prepareAtomicRepoAccess();
	}

	private void prepareAtomicRepoAccess() {
		Mockito.when(repoAccess.removeAssetAdministrationSubmodel(Mockito.anyString(), Mockito.anyString()))
				.thenAnswer(this::answerRemoveAssetAdminstrationSubmodel);
		Mockito.when(
				repoAccess.storeAssetAdministrationSubmodel(Mockito.anyString(), Mockito.any(SubmodelDescriptor.class)))
				.thenAnswer(this::answerStoreAssetAdminstrationSubmodel);
	}

	private Result answerStoreAssetAdminstrationSubmodel(InvocationOnMock invocation) {
		String aasId = invocation.getArgument(0);
		SubmodelDescriptor toAdd = invocation.getArgument(1);
		AssetAdministrationShellDescriptorEnvelop envelop = repoContent.get(aasId);
		if (envelop == null) {
			return Result.NOT_FOUND; // aasid not found
		}
		AssetAdministrationShellDescriptor descriptor = envelop.getAssetAdministrationShellDescriptor();
		List<SubmodelDescriptor> submodels = descriptor.getSubmodelDescriptors();
		if (submodels != null) {
			ListIterator<SubmodelDescriptor> submodelIter = submodels.listIterator();
			while (submodelIter.hasNext()) {
				SubmodelDescriptor current = submodelIter.next();
				if (Objects.equals(current.getIdentification(), toAdd.getIdentification())) {
					submodelIter.set(toAdd);
					return Result.UPDATED;
				}
			}
		}
		descriptor.addSubmodelDescriptorsItem(toAdd);
		return Result.UPDATED;
	}

	private Result answerRemoveAssetAdminstrationSubmodel(InvocationOnMock invocation) {
		String aasId = invocation.getArgument(0);
		String submodelId = invocation.getArgument(1);
		AssetAdministrationShellDescriptorEnvelop envelop = repoContent.get(aasId);
		if (envelop == null) {
			return Result.NOT_FOUND;
		}
		List<SubmodelDescriptor> descrList = envelop.getAssetAdministrationShellDescriptor().getSubmodelDescriptors();
		if (descrList != null) {
			if (descrList.removeIf(d -> Objects.equals(d.getIdentification(), submodelId))) {
				return Result.UPDATED;
			}
		}
		return Result.NOT_FOUND;
	}

	private void prepareSave() {
		Mockito.when(repo.save(Mockito.any(AssetAdministrationShellDescriptorEnvelop.class))).then(this::answerSave);
	}

	private AssetAdministrationShellDescriptorEnvelop answerSave(InvocationOnMock invocation) {
		AssetAdministrationShellDescriptorEnvelop envelop = invocation.getArgument(0);
		AssetAdministrationShellDescriptorEnvelop toStore = SerializationUtils.clone(envelop);
		repoContent.put(toStore.getId(), toStore);
		return SerializationUtils.clone(toStore);
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
		Mockito.doAnswer(this::answerDeleteById).when(repo).deleteById(Mockito.anyString());
	}

	private Void answerDeleteById(InvocationOnMock invocation) {
		String id = invocation.getArgument(0);
		repoContent.remove(id);
		return null;
	}

	private void prepareExistsById() {
		Mockito.when(repo.existsById(Mockito.anyString())).thenAnswer(this::answerExistsById);
	}
	
	private boolean answerExistsById(InvocationOnMock invocation) {
		String id = invocation.getArgument(0);
		return repoContent.containsKey(id);	
	}

	private void prepareFindById() {
		Mockito.when(repo.findById(Mockito.anyString())).thenAnswer(this::answerFindById);
	}
	
	private Optional<AssetAdministrationShellDescriptorEnvelop> answerFindById(InvocationOnMock invocation) throws JsonMappingException, JsonProcessingException {
		String id = invocation.getArgument(0);
		return Optional.ofNullable(cloneRepo().get(id));	
	}

	private void prepareFindAll() {
		Mockito.when(repo.findAll()).thenAnswer(i -> cloneRepo().values());
	}

}