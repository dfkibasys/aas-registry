package org.eclipse.basyx.aas.registry.service.storage;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AasDescriptorNotFoundException extends ResponseStatusException {

	private static final long serialVersionUID = 1L;

	public AasDescriptorNotFoundException(String aasDesriptorId) {
		super(HttpStatus.NOT_FOUND, "The parent aasDescriptor '" + aasDesriptorId + "' is not available.");
	}
}
