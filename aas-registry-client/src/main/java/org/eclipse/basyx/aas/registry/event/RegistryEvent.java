package org.eclipse.basyx.aas.registry.event;

import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.basyx.aas.registry.model.SubmodelDescriptor;

public class RegistryEvent {

    public static enum EventType {
        AAS_REGISTERED,
        AAS_UNREGISTERED,
        SUBMODEL_REGISTERED,
        SUBMODEL_UNREGISTERED
    }

    private String id;
    private EventType type;
    private AssetAdministrationShellDescriptor assetAdministrationShellDescriptor;

    public RegistryEvent(String id, EventType type, AssetAdministrationShellDescriptor assetAdministrationShellDescriptor) {
        this.id = id;
        this.type = type;
        this.assetAdministrationShellDescriptor = assetAdministrationShellDescriptor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptor() {
        return assetAdministrationShellDescriptor;
    }

    public void setAssetAdministrationShellDescriptor(AssetAdministrationShellDescriptor assetAdministrationShellDescriptor) {
        this.assetAdministrationShellDescriptor = assetAdministrationShellDescriptor;
    }
}
