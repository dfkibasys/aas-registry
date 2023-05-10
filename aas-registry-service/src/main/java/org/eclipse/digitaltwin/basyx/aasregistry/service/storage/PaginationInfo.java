package org.eclipse.digitaltwin.basyx.aasregistry.service.storage;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class PaginationInfo {

	final Integer limit;
	final String cursor;

	public boolean hasLimit() {
		return limit != null && limit != -1;
	}
	
	public boolean hasCursor()  {
		return cursor != null;
	}

	public boolean isPaged() {
		return hasLimit() || hasCursor();
	}
	
}
