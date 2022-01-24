package org.eclipse.basyx.aas.registry.util.path;

import java.util.List;

class PojoClassVisitorDelegate implements PojoClassVisitor {

	private PojoClassVisitor[] visitors;

	public PojoClassVisitorDelegate(PojoClassVisitor... visitors) {
		this.visitors = visitors;
	}

	@Override
	public void startRelation(PojoRelation relation) {
		for (PojoClassVisitor eachVisitor : visitors) {
			eachVisitor.startRelation(relation);
		}
	}
	
	@Override
	public void endRelation(PojoRelation relation) {
		for (PojoClassVisitor eachVisitor : visitors) {
			eachVisitor.endRelation(relation);
		}	
	}

	@Override
	public void onSubTypeRelation(String parent, List<String> subTypes) {
		for (PojoClassVisitor eachVisitor : visitors) {
			eachVisitor.onSubTypeRelation(parent, subTypes);
		}
	}

	@Override
	public boolean startType(String name, boolean isRoot) {
		boolean doContinue = true;
		for (PojoClassVisitor eachVisitor : visitors) {
			if (!eachVisitor.startType(name, isRoot)) {
				doContinue = false;
			}
		}
		return doContinue;
	}
	
	@Override
	public void endType() {
		for (PojoClassVisitor eachVisitor : visitors) {
			eachVisitor.endType();
		}		
	}

	@Override
	public void stop() {
		for (PojoClassVisitor eachVisitor : visitors) {
			eachVisitor.stop();
		}
	}

}
