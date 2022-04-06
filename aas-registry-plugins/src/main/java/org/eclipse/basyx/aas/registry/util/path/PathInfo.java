package org.eclipse.basyx.aas.registry.util.path;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.UtilityClass;

@Data
public class PathInfo {

	private GenerationTarget pathsTarget;

	private GenerationTarget processorTarget;

	private String inputClassPackageName;

	private Set<ConstantInfo> constants;

	private ModelInfo rootModel;

	private Set<PrimitiveRange> primitiveRanges;

	private Set<ModelInfo> models;

	private Set<ModelInfo> allModels;

	@Data
	@AllArgsConstructor
	private static class PrimitiveRange {

		private boolean functional;

		private String range;
	}

	@Getter
	@Setter
	@EqualsAndHashCode
	@ToString
	public static class ConstantInfo {

		private String name;

		private String nameUpper;

		ConstantInfo(String name) {
			this.name = name;
			this.nameUpper = ConstantGenerator.generateConstant(name);
		}
	}

	@Getter
	@Setter
	@EqualsAndHashCode
	@ToString
	public abstract static class RelationInfo {

		private String methodName;

		private String attributeNameUpperFirst;

		private String attributeName;

		private String attributeNameUpper;

		private boolean listRange;

		RelationInfo(String methodName, String attributeName, boolean isListRange) {
			this.methodName = methodName;
			this.attributeNameUpperFirst = toUpperFirst(attributeName);
			this.listRange = isListRange;
			this.attributeNameUpper = ConstantGenerator.generateConstant(attributeName);
		}

		private String toUpperFirst(String name) {
			if (name.length() < 1) {
				return name;
			}
			return "" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
		}
	}

	@ToString
	@EqualsAndHashCode(callSuper = true)
	public static class PrimitiveRangeRelationInfo extends RelationInfo {

		public PrimitiveRangeRelationInfo(String methodName, String attributeName, boolean isListRange) {
			super(methodName, attributeName, isListRange);
		}
	}

	@EqualsAndHashCode(callSuper = true)
	@Getter
	@Setter
	public static class ComplexRangeRelationInfo extends RelationInfo {

		private String modelName;

		public ComplexRangeRelationInfo(String methodName, String attributeName, String rangeName, boolean isListRange) {
			super(methodName, attributeName, isListRange);
			this.modelName = rangeName;
		}

	}

	@Data
	@RequiredArgsConstructor
	public static class BaseConfig {
		private final boolean skipLists;
		private final Map<String, ModelInfo> lookupTable = new HashMap<>();
	}

	@Data
	public static class ModelInfo {

		private final String name;

		private String singlePathConstructor;

		private String pathAndSegmentConstructor;

		private List<String> subModels;

		private List<PrimitiveRangeRelationInfo> primitiveRangeRelations = new LinkedList<>();

		private List<ComplexRangeRelationInfo> complexRangeRelations = new LinkedList<>();

		private PathInfo info; // backpointer for global access in templates

		public ModelInfo(String name) {
			this.name = name;
		}

	}

	@UtilityClass
	private static class ConstantGenerator {

		public String generateConstant(String name) {
			StringBuilder builder = new StringBuilder();
			for (int i = 0, len = name.length(); i < len; i++) {
				char c = name.charAt(i);
				if (Character.isUpperCase(c)) {
					builder.append('_');
					builder.append(c);
				} else {
					builder.append(Character.toUpperCase(c));
				}
			}
			return builder.toString();
		}
	}

}