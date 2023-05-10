package de.dfki.cos.basys.aas.registry.service.errors;

public class AasDescriptorAlreadyExistsException extends DescriptorAlreadyExistsException {

	private static final long serialVersionUID = 1L;

	public AasDescriptorAlreadyExistsException(String aasDesriptorId) {
		super("AasDescriptor '" + aasDesriptorId + "' is already available.");
	}
}
