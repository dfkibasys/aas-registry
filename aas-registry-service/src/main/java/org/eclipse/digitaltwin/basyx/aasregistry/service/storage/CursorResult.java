package org.eclipse.digitaltwin.basyx.aasregistry.service.storage;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CursorResult<T> {

	final String cursor;

	final T result;

}
