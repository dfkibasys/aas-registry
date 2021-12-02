package org.eclipse.basyx.aas.registry.model;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(indexName = "shell-descriptors")
public class AssetAdministrationShellDescriptorEnvelop implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private AssetAdministrationShellDescriptor assetAdministrationShellDescriptor;
	
	@Id
	private String id;

	public AssetAdministrationShellDescriptorEnvelop(
			AssetAdministrationShellDescriptor assetAdministrationShellDescriptor) {
		this.assetAdministrationShellDescriptor = assetAdministrationShellDescriptor;
		this.id = assetAdministrationShellDescriptor.getIdentification();
	}
}