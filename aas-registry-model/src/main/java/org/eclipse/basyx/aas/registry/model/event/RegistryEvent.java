package org.eclipse.basyx.aas.registry.model.event;


import javax.validation.constraints.Null;

import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.basyx.aas.registry.model.Descriptor;
import org.eclipse.basyx.aas.registry.model.SubmodelDescriptor;
import org.springframework.lang.Nullable;

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
	private @Nullable String submodelId;
    private EventType type;
    private @Nullable AssetAdministrationShellDescriptor aasDescriptor;
    private @Nullable SubmodelDescriptor submodelDescriptor;

    public enum EventType {
        AAS_REGISTERED,
        AAS_UNREGISTERED,
        SUBMODEL_REGISTERED,
        SUBMODEL_UNREGISTERED
    }
}
