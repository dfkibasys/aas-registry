package org.eclipse.basyx.aas.registry.configuration;

public class ResourceLoadingException extends RuntimeException {


	private static final long serialVersionUID = 1L;

	public ResourceLoadingException(String path, Throwable throwable) {
		super("Failed to load resource: " + path, throwable);
	}
}
