package org.eclipse.basyx.aas.registry.util.path;

import java.util.HashSet;

class PojoWalkCycleDetector extends PojoClassVisitorDelegate {

	private HashSet<String> typeNames = new HashSet<>();

	public PojoWalkCycleDetector(PojoClassVisitor visitor) {
		super(visitor);
	}

	@Override
	public boolean startType(String name, boolean isRoot) {
		boolean doProcess = typeNames.add(name);
		if (doProcess) {
			super.startType(name, isRoot);
		}
		return doProcess;
	}

}
