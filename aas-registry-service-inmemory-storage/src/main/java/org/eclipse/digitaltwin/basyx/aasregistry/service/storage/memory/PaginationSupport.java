package org.eclipse.digitaltwin.basyx.aasregistry.service.storage.memory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.digitaltwin.basyx.aasregistry.model.Descriptor;
import org.eclipse.digitaltwin.basyx.aasregistry.service.storage.CursorResult;
import org.eclipse.digitaltwin.basyx.aasregistry.service.storage.DescriptorFilter;
import org.eclipse.digitaltwin.basyx.aasregistry.service.storage.PaginationInfo;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PaginationSupport<T extends Descriptor> {

	final TreeMap<String, T> sortedDescriptorMap;

	public CursorResult<List<T>> getDescriptorsPaged(PaginationInfo pInfo) {
		return getDescriptorsPagedAndFiltered(pInfo, null, null);
	}
	
	public CursorResult<List<T>> getDescriptorsPagedAndFiltered(PaginationInfo pInfo, DescriptorFilter filter, Predicate<T> filterMethod) {
				
		Map<String, T> cursorView = getCursorView(pInfo);
		Stream<Entry<String, T>> eStream = cursorView.entrySet().stream();
		
		eStream = applyFilter(filter, e -> filterMethod.test(e.getValue()), eStream);
		Stream<T> tStream = eStream.map(Entry::getValue);
		tStream = applyLimit(pInfo, tStream);
		
		List<T> descriptorList = tStream.collect(Collectors.toList());
		
		String cursor = computeNextCursor(descriptorList);
		return new CursorResult<>(cursor, Collections.unmodifiableList(descriptorList));
	}


	private Stream<Entry<String, T>> applyFilter(DescriptorFilter filter, Predicate<Entry<String, T>> filterMethod, Stream<Entry<String, T>> aStream) {
		if (filter != null && filter.isFiltered()) {
			return aStream.filter(filterMethod);
		}
		return aStream;
	}

	private Stream<T> applyLimit(PaginationInfo info, Stream<T> aStream) {
		if (info.hasLimit()) {
			return aStream.limit(info.getLimit());
		}
		return aStream;
	}

	private String computeNextCursor(List<T> descriptorList) {
		if (!descriptorList.isEmpty()) {
			String lastId = descriptorList.get(descriptorList.size()-1).getId();
			return sortedDescriptorMap.higherKey(lastId);
		}
		return null;
	}


	private Map<String, T> getCursorView(PaginationInfo info) {
		if (info.hasCursor()) {
			return sortedDescriptorMap.tailMap(info.getCursor());
		} else {
			return sortedDescriptorMap;
		}
	}

}
