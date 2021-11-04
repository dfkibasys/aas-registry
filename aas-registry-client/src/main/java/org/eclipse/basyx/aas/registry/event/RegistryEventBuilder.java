package org.eclipse.basyx.aas.registry.event;

import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.basyx.aas.registry.model.SubmodelDescriptor;

public class RegistryEventBuilder {
    private String id;
    private RegistryEvent.EventType type;
    private AssetAdministrationShellDescriptor assetAdministrationShellDescriptor;

    public RegistryEventBuilder setId(String id) {
        this.id = id;
        return this;
    }

    public RegistryEventBuilder setType(RegistryEvent.EventType type) {
        this.type = type;
        return this;
    }

    public RegistryEventBuilder setAssetAdministrationShellDescriptor(AssetAdministrationShellDescriptor assetAdministrationShellDescriptor) {
        this.assetAdministrationShellDescriptor = assetAdministrationShellDescriptor;
        return this;
    }

    public RegistryEvent build() {
        return new RegistryEvent(id, type, assetAdministrationShellDescriptor);
    }
}