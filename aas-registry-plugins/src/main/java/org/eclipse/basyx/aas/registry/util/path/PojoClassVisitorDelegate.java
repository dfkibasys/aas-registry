package org.eclipse.basyx.aas.registry.util.path;

import java.util.List;

class PojoClassVisitorDelegate implements PojoClassVisitor {

	private PojoClassVisitor[] visitors;

	public PojoClassVisitorDelegate(PojoClassVisitor... visitors) {
		this.visitors = visitors;
	}

	@Override
	public void onPrimitiveRelation(String methodName, String fieldName, boolean isRootRelation) {
		for (PojoClassVisitor eachVisitor : visitors) {
			eachVisitor.onPrimitiveRelation(methodName, fieldName, isRootRelation);
		}
	}

	@Override
	public void onComplexRelation(String methodName, String fieldName, String range, boolean isRootRelation) {
		for (PojoClassVisitor eachVisitor : visitors) {
			eachVisitor.onComplexRelation(methodName, fieldName, range, isRootRelation);
		}
	}

	@Override
	public void onSubTypeRelation(String parent, List<String> subTypes) {
		for (PojoClassVisitor eachVisitor : visitors) {
			eachVisitor.onSubTypeRelation(parent, subTypes);
		}
	}

	@Override
	public boolean visitType(String name, boolean isRoot) {
		boolean doContinue = true;
		for (PojoClassVisitor eachVisitor : visitors) {
			if (!eachVisitor.visitType(name, isRoot)) {
				doContinue = false;
			}
		}
		return doContinue;
	}

	@Override
	public void stop() {
		for (PojoClassVisitor eachVisitor : visitors) {
			eachVisitor.stop();
		}
	}

}
