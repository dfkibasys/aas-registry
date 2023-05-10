package de.dfki.cos.basys.aas.registry.service.storage;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CursorResult<T> {

	final String cursor;

	final T result;

}
