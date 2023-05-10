package de.dfki.cos.basys.aas.registry.service.tests;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.mockito.Mockito;

import de.dfki.cos.basys.aas.registry.client.api.AasRegistryPaths;
import de.dfki.cos.basys.aas.registry.events.RegistryEvent;
import de.dfki.cos.basys.aas.registry.model.AssetAdministrationShellDescriptor;
import de.dfki.cos.basys.aas.registry.model.Page;
import de.dfki.cos.basys.aas.registry.model.ShellDescriptorQuery;
import de.dfki.cos.basys.aas.registry.model.ShellDescriptorQuery.QueryTypeEnum;
import de.dfki.cos.basys.aas.registry.model.ShellDescriptorSearchRequest;
import de.dfki.cos.basys.aas.registry.model.ShellDescriptorSearchResponse;
import de.dfki.cos.basys.aas.registry.model.SortDirection;
import de.dfki.cos.basys.aas.registry.model.Sorting;
import de.dfki.cos.basys.aas.registry.model.SortingPath;

public abstract class ExtensionsTest extends BaseInterfaceTest {
	
	@Test
	public void whenSearchWithSorting_thenSorted() {
		ShellDescriptorSearchResponse response = storage.searchAasDescriptors(new ShellDescriptorSearchRequest().sortBy(new Sorting().addPathItem(SortingPath.ID)));
		String[] ids = response.getHits().stream().map(AssetAdministrationShellDescriptor::getId).toArray(String[]::new);
		assertThat(ids[0]).isEqualTo(IDENTIFICATION_1);
		assertThat(ids[1]).isEqualTo(IDENTIFICATION_2);

		response = storage.searchAasDescriptors(new ShellDescriptorSearchRequest().sortBy(new Sorting().addPathItem(SortingPath.ID).direction(SortDirection.DESC)));
		ids = response.getHits().stream().map(AssetAdministrationShellDescriptor::getId).toArray(String[]::new);
		assertThat(ids[0]).isEqualTo(IDENTIFICATION_2);
		assertThat(ids[1]).isEqualTo(IDENTIFICATION_1);
	}

	@Test
	public void whenSearchOutsideSubmodel_thenGetUnshrinkedDescriptor() {
		AssetAdministrationShellDescriptor expected = storage.getAasDescriptor(IDENTIFICATION_1);
		ShellDescriptorSearchResponse response = storage.searchAasDescriptors(new ShellDescriptorSearchRequest().query(new ShellDescriptorQuery().path(AasRegistryPaths.id()).value(IDENTIFICATION_1)));
		assertThat(response.getTotal()).isEqualTo(1);
		assertThat(response.getHits().iterator().next()).isEqualTo(expected);
	}

	@Test
	public void whenSearchWithPagination_thenReturnStepwise() throws IOException {
		List<AssetAdministrationShellDescriptor> expectedFirstPage = testResourcesLoader.loadShellDescriptorList("0");
		List<AssetAdministrationShellDescriptor> expectedSecondPage = testResourcesLoader.loadShellDescriptorList("1");
		ShellDescriptorSearchRequest request = new ShellDescriptorSearchRequest().query(new ShellDescriptorQuery().path(AasRegistryPaths.submodelDescriptors().description().text()).value(".*[R|r]obot.*").queryType(QueryTypeEnum.REGEX))
				.sortBy(new Sorting().addPathItem(SortingPath.ID).direction(SortDirection.DESC)).page(new Page().index(0).size(2));
		ShellDescriptorSearchResponse response1 = storage.searchAasDescriptors(request);
		request.setPage(new Page().index(1).size(2));
		ShellDescriptorSearchResponse response2 = storage.searchAasDescriptors(request);
		assertThat(response1.getTotal()).isEqualTo(3);
		assertThat(response2.getTotal()).isEqualTo(3);
		assertThat(response1.getHits().size()).isEqualTo(2);
		assertThat(response2.getHits().size()).isEqualTo(1);
		assertThat(response1.getHits()).isEqualTo(expectedFirstPage);
		assertThat(response2.getHits()).isEqualTo(expectedSecondPage);
	}

	@Test
	public void whenSearchWithSortingAndNullValueAdSearchPath_thenNotSorted() {
		Collection<AssetAdministrationShellDescriptor> initial = getAllAasDescriptors();
		ShellDescriptorSearchResponse response = storage.searchAasDescriptors(new ShellDescriptorSearchRequest().sortBy(new Sorting().addPathItem(SortingPath.ADMINISTRATION_VERSION)));
		assertThat(response.getHits()).isEqualTo(initial);
	}
	
	@Test
	public void whenSearchWithTwoSortingPaths_thenSorted() throws IOException {
		List<AssetAdministrationShellDescriptor> expected = testResourcesLoader.loadShellDescriptorList();
		ShellDescriptorSearchResponse response = storage.searchAasDescriptors(new ShellDescriptorSearchRequest().sortBy(new Sorting().addPathItem(SortingPath.ADMINISTRATION_VERSION).addPathItem(SortingPath.ADMINISTRATION_REVISION)));
		assertThat(response.getHits()).isEqualTo(expected);
	}

	@Test
	public void whenSearchWithSortingButNoSortPath_thenNotSorted() {
		Collection<AssetAdministrationShellDescriptor> initial = getAllAasDescriptors();
		ShellDescriptorSearchResponse response = storage.searchAasDescriptors(new ShellDescriptorSearchRequest().sortBy(new Sorting()));
		assertThat(response.getHits()).isEqualTo(initial);
	}
	
	@Test
	public void whenDeleteAllShellDescritors_thenEventsAreSendAndDescriptorsRemoved() {
		List<AssetAdministrationShellDescriptor> oldState = getAllAasDescriptors();
		assertThat(oldState).isNotEmpty();
		Set<String> aasIdsOfRemovedDescriptors = storage.clear();
		// listener is invoked for each removal
		Mockito.verify(getEventSink(), Mockito.times(aasIdsOfRemovedDescriptors.size())).consumeEvent(Mockito.any(RegistryEvent.class));
		List<AssetAdministrationShellDescriptor> newState = getAllAasDescriptors();
		assertThat(newState).isEmpty();
	}
	
	@Test
	public void whenMatchSearchBySubModel_thenReturnDescriptorList() throws IOException {
		ShellDescriptorSearchRequest request = new ShellDescriptorSearchRequest().query(new ShellDescriptorQuery().queryType(QueryTypeEnum.MATCH).path(AasRegistryPaths.submodelDescriptors().id()).value(IDENTIFICATION_2_1));
		ShellDescriptorSearchResponse result = storage.searchAasDescriptors(request);
		AssetAdministrationShellDescriptor descriptor = testResourcesLoader.loadAssetAdminShellDescriptor();

		assertThat(result.getTotal()).isEqualTo(1);
		assertThat(result.getHits().get(0)).isEqualTo(descriptor);
	}

	@Test
	public void whenMatchSearchBySubModelAndNotFound_thenReturnEmptyList() {
		ShellDescriptorSearchRequest query = new ShellDescriptorSearchRequest().query(new ShellDescriptorQuery().queryType(QueryTypeEnum.MATCH).path(AasRegistryPaths.submodelDescriptors().id()).value(UNKNOWN));
		ShellDescriptorSearchResponse result = storage.searchAasDescriptors(query);
		assertThat(result.getTotal()).isZero();
		assertThat(result.getHits().size()).isZero();
	}

	@Test
	public void whenRegexSearchBySubModel_thenReturnDescriptorList() throws IOException {
		ShellDescriptorSearchRequest request = new ShellDescriptorSearchRequest().query(new ShellDescriptorQuery().queryType(QueryTypeEnum.REGEX).path(AasRegistryPaths.submodelDescriptors().idShort()).value(".*_24"));
		ShellDescriptorSearchResponse result = storage.searchAasDescriptors(request);
		AssetAdministrationShellDescriptor descriptor = testResourcesLoader.loadAssetAdminShellDescriptor();
		assertThat(result.getTotal()).isEqualTo(1);
		assertThat(result.getHits().get(0)).isEqualTo(descriptor);
	}

	@Test
	public void whenRegexSearchBySubModelAndNotFound_thenReturnEmptyList() {
		ShellDescriptorSearchRequest query = new ShellDescriptorSearchRequest().query(new ShellDescriptorQuery().queryType(QueryTypeEnum.REGEX).path(AasRegistryPaths.submodelDescriptors().idShort()).value(".*_333_.*"));
		ShellDescriptorSearchResponse result = storage.searchAasDescriptors(query);
		assertThat(result.getTotal()).isZero();
		assertThat(result.getHits().size()).isZero();
	}
	
}
