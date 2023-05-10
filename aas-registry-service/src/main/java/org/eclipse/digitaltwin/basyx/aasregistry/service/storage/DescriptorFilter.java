package org.eclipse.digitaltwin.basyx.aasregistry.service.storage;

import org.eclipse.digitaltwin.basyx.aasregistry.model.AssetKind;

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
