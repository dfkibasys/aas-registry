package org.eclipse.basyx.aas.registry.event;

import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistryEvent {

	private String id;
    private EventType type;
    private AssetAdministrationShellDescriptor assetAdministrationShellDescriptor;

    public enum EventType {
        AAS_REGISTERED,
        AAS_UNREGISTERED,
        SUBMODEL_REGISTERED,
        SUBMODEL_UNREGISTERED
    }
}