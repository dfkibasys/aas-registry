package org.eclipse.basyx.aas.registry.model.event;

import java.util.List;

import javax.validation.Valid;

import org.eclipse.basyx.aas.registry.model.AdministrativeInformation;
import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.basyx.aas.registry.model.IdentifierKeyValuePair;
import org.eclipse.basyx.aas.registry.model.LangString;
import org.eclipse.basyx.aas.registry.model.Reference;
import org.eclipse.basyx.aas.registry.model.SubmodelDescriptor;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AasModelPaths {

	private final String ADMINISTRATION = "administration";
	
	  private AdministrativeInformation administration = null;

	  @JsonProperty("description")
	  @Valid
	  private List<LangString> description = null;

	  @JsonProperty("globalAssetId")
	  private Reference globalAssetId = null;

	  @JsonProperty("idShort")
	  private String idShort = null;

	  @JsonProperty("identification")
	  private String identification = null;

	  @JsonProperty("specificAssetIds")
	  @Valid
	  private List<IdentifierKeyValuePair> specificAssetIds = null;

	  @JsonProperty("submodelDescriptors")
	  @Valid
	  private List<SubmodelDescriptor> submodelDescriptors = null;
	
	
}
