package org.eclipse.basyx.aas.registry.repository;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.basyx.aas.registry.model.SubmodelDescriptor;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;

public class DescriptorMapper {

	public List<AssetAdministrationShellDescriptor> mapHits(SearchHits<AssetAdministrationShellDescriptor> hits) {
		List<AssetAdministrationShellDescriptor> result = new ArrayList<>();
		for (SearchHit<AssetAdministrationShellDescriptor> eachHit : hits.getSearchHits()) {
			AssetAdministrationShellDescriptor descriptor = eachHit.getContent();
			Map<String, SearchHits<?>> innerHits = eachHit.getInnerHits();
			if (!innerHits.isEmpty()) {
				// we just had one innerHit query
				Entry<String, SearchHits<?>> innerEntry = innerHits.entrySet().iterator().next();
				shrinkDescriptor(descriptor, innerEntry.getValue());
			}
			result.add(descriptor);
		}
		return result;
	}

	private void shrinkDescriptor(AssetAdministrationShellDescriptor descriptor, SearchHits<?> hits) {
		List<SubmodelDescriptor> matchingSubModels = new LinkedList<>();
		for (SearchHit<?> eachHit : hits.getSearchHits()) {
			SubmodelDescriptor content = (SubmodelDescriptor) eachHit.getContent();
			matchingSubModels.add(content);
		}
		descriptor.setSubmodelDescriptors(matchingSubModels);
	}

}
