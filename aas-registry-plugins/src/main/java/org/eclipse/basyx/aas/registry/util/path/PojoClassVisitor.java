package org.eclipse.basyx.aas.registry.util.path;

import java.util.List;

interface PojoClassVisitor {

	void onPrimitiveRelation(String methodName, String fieldName, boolean isRootRelation);

	void onComplexRelation(String methodName, String fieldName, String range, boolean isRootRelation);

	void onSubTypeRelation(String parent, List<String> subTypes);

	boolean visitType(String name, boolean isRoot);

	void stop();
}
