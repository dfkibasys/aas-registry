package de.dfki.cos.basys.aas.registry.service.storage;

import de.dfki.cos.basys.aas.registry.model.AssetKind;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class DescriptorFilter {

	final AssetKind kind;
	final String assetType;
	
	public boolean isFiltered() {
		return kind != null;
	}

	
}
