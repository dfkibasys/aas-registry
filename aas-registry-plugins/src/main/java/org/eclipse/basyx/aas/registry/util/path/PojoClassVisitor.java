package org.eclipse.basyx.aas.registry.util.path;

import java.util.List;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

interface PojoClassVisitor {

	void startRelation(PojoRelation relation);

	void endRelation(PojoRelation relation);
	
	void onSubTypeRelation(String parent, List<String> subTypes);

	boolean startType(String name, boolean isRoot);
	
	void endType();

	void stop();

	@Builder
	@Getter
	@ToString
	@EqualsAndHashCode
	@RequiredArgsConstructor
	static class PojoRelation {

		private final String subject;
		private final String methodName;
		private final String fieldName;
		private final String range;
		private final PojoRelationType type;
		private final boolean isRootRelation;

		enum PojoRelationType {
			FUNCTIONAL, LIST, MAP
		}
		
		public boolean isPrimitive() {
			return range == null;
		}
		
		public boolean isComplex() {
			return range != null;
		}
	}

}