package org.eclipse.basyx.aas.registry.util.path;

import java.util.List;

abstract class AbstractPojoClassVisitor implements PojoClassVisitor {

	@Override
	public void startRelation(PojoRelation relation) {
	}

	@Override
	public void endRelation(PojoRelation relation) {
	}

	@Override
	public boolean startType(String name, boolean isRoot) {
		return true;
	}

	@Override
	public void endType() {
	}

	@Override
	public void onSubTypeRelation(String parent, List<String> subTypes) {
	}

	@Override
	public void stop() {
	}
}
