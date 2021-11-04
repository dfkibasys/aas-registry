package org.eclipse.basyx.aas.registry.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Getter
@Setter
@Document(indexName = "shell-descriptors")
public class AssetAdministrationShellDescriptorEnvelop {

    @Id
    private String id;

    private AssetAdministrationShellDescriptor assetAdministrationShellDescriptor;

    public AssetAdministrationShellDescriptorEnvelop(AssetAdministrationShellDescriptor assetAdministrationShellDescriptor) {
        this.assetAdministrationShellDescriptor = assetAdministrationShellDescriptor;
        this.id = assetAdministrationShellDescriptor.getIdentification().getId();
    }
}
