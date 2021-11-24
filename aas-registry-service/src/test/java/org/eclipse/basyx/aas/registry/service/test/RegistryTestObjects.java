package org.eclipse.basyx.aas.registry.service.test;

import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.basyx.aas.registry.model.Identifier;
import org.eclipse.basyx.aas.registry.model.KeyType;
import org.eclipse.basyx.aas.registry.model.LangString;
import org.eclipse.basyx.aas.registry.model.SubmodelDescriptor;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RegistryTestObjects {

	public AssetAdministrationShellDescriptor newAssetAdministrationShellDescriptor(String id) {
		AssetAdministrationShellDescriptor descriptor = new AssetAdministrationShellDescriptor();
		Identifier identifier = newIdentifier(id);
		descriptor.setIdentification(identifier);
		return descriptor;
	}

	public SubmodelDescriptor newSubmodelDescriptor(String id) {
		return newSubmodelDescriptor(id, null);
	}

	public SubmodelDescriptor newSubmodelDescriptor(String id, String description) {
		SubmodelDescriptor descriptor = new SubmodelDescriptor();
		Identifier identifier = newIdentifier(id);
		descriptor.setIdentification(identifier);
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

	private Identifier newIdentifier(String id) {
		Identifier identifier = new Identifier();
		identifier.setId(id);
		identifier.setIdType(KeyType.IDSHORT);
		return identifier;
	}

}
