package de.dfki.cos.basys.aas.registry.service.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public abstract class DescriptorAlreadyExistsException extends ResponseStatusException {

	private static final long serialVersionUID = 1L;

	protected DescriptorAlreadyExistsException(String reason) {
		super(HttpStatus.CONFLICT, reason);
	}
}