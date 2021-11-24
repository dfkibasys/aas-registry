package org.eclipse.basyx.aas.registry.service.test.util;

import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.basyx.aas.registry.model.LangString;
import org.eclipse.basyx.aas.registry.model.SubmodelDescriptor;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RegistryTestObjects {

	public AssetAdministrationShellDescriptor newAssetAdministrationShellDescriptor(String id) {
		AssetAdministrationShellDescriptor descriptor = new AssetAdministrationShellDescriptor();
		descriptor.setIdentification(id);
		return descriptor;
	}

	public SubmodelDescriptor newSubmodelDescriptor(String id) {
		return newSubmodelDescriptor(id, null);
	}

	public SubmodelDescriptor newSubmodelDescriptor(String id, String description) {
		SubmodelDescriptor descriptor = new SubmodelDescriptor();
		descriptor.setIdentification(id);
		addDescription(descriptor, description);
		return descriptor;
	}

	private void addDescription(SubmodelDescriptor descriptor, String description) {
		if (description != null) {
			LangString lString = newDescription(description);
			descriptor.addDescriptionItem(lString);
		}
	}

	private LangString newDescription(String sDescr) {
		LangString descr = new LangString();
		descr.setLanguage("de-DE");
		descr.setText(sDescr);
		return descr;
	}
}