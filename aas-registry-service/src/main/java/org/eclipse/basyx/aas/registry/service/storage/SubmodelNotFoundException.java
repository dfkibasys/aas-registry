package org.eclipse.basyx.aas.registry.service.storage;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class SubmodelNotFoundException extends ResponseStatusException {

	private static final long serialVersionUID = 1L;

	public SubmodelNotFoundException(String aasDesriptorId, String submodelId) {
		super(HttpStatus.NOT_FOUND, "Submodel '" + submodelId + "' is not avaialbe for descriptor '" + aasDesriptorId + "'.");
	}
}
