package org.eclipse.basyx.aas.registry.util.path;

import java.util.List;

class AbstractPojoClassVisitor implements PojoClassVisitor {

	@Override
	public void onPrimitiveRelation(String methodName, String fieldName, boolean isRootRelation) {
	}

	@Override
	public void onComplexRelation(String methodName, String fieldName, String range, boolean isRootRelation) {
	}

	@Override
	public boolean visitType(String name, boolean isRoot) {
		return true;
	}

	@Override
	public void onSubTypeRelation(String parent, List<String> subTypes) {

	}

	@Override
	public void stop() {

	}
}
