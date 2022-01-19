package org.eclipse.basyx.aas.registry.util.path;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.basyx.aas.registry.util.path.SearchPathInfo.ComplexRangeRelationInfo;
import org.eclipse.basyx.aas.registry.util.path.SearchPathInfo.ConstantInfo;
import org.eclipse.basyx.aas.registry.util.path.SearchPathInfo.GenerationTarget;
import org.eclipse.basyx.aas.registry.util.path.SearchPathInfo.ModelInfo;
import org.eclipse.basyx.aas.registry.util.path.SearchPathInfo.PrimitiveRangeRelationInfo;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SearchPathGenerator {

	private final Class<?> cls;
		
	public SearchPathInfo generate(String targetPackageName, String targetClassName) {
		SearchPathInfo info = new SearchPathInfo();

		GenerationTarget target = new GenerationTarget(targetPackageName, targetClassName);
		info.setTarget(target);

		PojoClassVisitorDelegate delegate = new PojoClassVisitorDelegate(new ConstantFiller(info),
				new PrimitiveRangeRootRelationFiller(info), new ComplexRangeRootRelationFiller(info),
				new ModelFiller(info));
		PojoWalkCycleDetector visitor = new PojoWalkCycleDetector(delegate);
		PojoClassWalker walker = new PojoClassWalker(cls, visitor);
		walker.walkClass();
		return info;
	}

	private static class ModelFiller extends AbstractPojoClassVisitor {

		private List<ModelInfo> models = new LinkedList<>();
		private Map<String, ModelInfo> lookupModels = new HashMap<>();

		private Set<String> rootRanges = new HashSet<>();
		private Set<String> innerRanges = new HashSet<>();
		private Set<String> foundSubTypes = new HashSet<>();
		private Map<String, List<String>> typeSubTypeMapping = new HashMap<>();

		private ModelInfo current;

		public ModelFiller(SearchPathInfo info) {
			info.setModels(models);
		}

		@Override
		public void onComplexRelation(String methodName, String fieldName, String range, boolean isRootRelation) {
			if (!isRootRelation) {
				ComplexRangeRelationInfo info = new ComplexRangeRelationInfo(methodName, fieldName, range);
				current.getComplexRangeRelations().add(info);
				innerRanges.add(range);
			} else {
				rootRanges.add(range);
			}
		}

		@Override
		public void onPrimitiveRelation(String methodName, String fieldName, boolean isRootRelation) {
			if (!isRootRelation) {
				PrimitiveRangeRelationInfo info = new PrimitiveRangeRelationInfo(methodName, fieldName);
				current.getPrimitiveRangeRelations().add(info);
			}
		}

		@Override
		public void onSubTypeRelation(String base, List<String> subTypes) {
			foundSubTypes.addAll(subTypes);
			typeSubTypeMapping.put(base, subTypes);
		}

		@Override
		public boolean visitType(String name, boolean isRoot) {
			if (!isRoot) {
				current = new ModelInfo(name);
				current.setPrimitiveRangeRelations(new LinkedList<>());
				current.setComplexRangeRelations(new LinkedList<>());
				models.add(current);
				lookupModels.put(name, current);
			}
			return true;
		}

		@Override
		public void stop() {
			for (ModelInfo eachInfo : models) {
				String modelName = eachInfo.getName();
				if (foundSubTypes.contains(modelName) || rootRanges.contains(modelName)) {
					eachInfo.setSinglePathConstructor(modelName);
				}
				if (innerRanges.contains(modelName)) {
					eachInfo.setPathAndSegmentConstructor(modelName);
				}
			}
			for (Entry<String, List<String>> eachMapping : typeSubTypeMapping.entrySet()) {
				ModelInfo info = lookupModels.get(eachMapping.getKey());
				info.setSubModels(eachMapping.getValue());
			}
		}
	}

	private static class ConstantFiller extends AbstractPojoClassVisitor {

		private Set<ConstantInfo> constants = new TreeSet<>(Comparator.comparing(ConstantInfo::getName));
		private Set<String> duplicateFilter = new HashSet<>();

		public ConstantFiller(SearchPathInfo info) {
			info.setConstants(constants);
		}

		@Override
		public void onPrimitiveRelation(String methodName, String  fieldName, boolean isRootRelation) {
			if (duplicateFilter.add(fieldName)) {
				constants.add(new ConstantInfo(fieldName));
			}
		}

		@Override
		public void onComplexRelation(String methodName, String fieldName, String range, boolean isRootRelation) {
			if (duplicateFilter.add(fieldName)) {
				constants.add(new ConstantInfo(fieldName));
			}
		}
	}

	private static class PrimitiveRangeRootRelationFiller extends AbstractPojoClassVisitor {

		private Set<PrimitiveRangeRelationInfo> relations = new TreeSet<>(
				Comparator.comparing(PrimitiveRangeRelationInfo::getAttributeName));

		public PrimitiveRangeRootRelationFiller(SearchPathInfo info) {
			info.setPrimitiveRangeRelations(relations);
		}

		@Override
		public void onPrimitiveRelation(String methodName, String fieldName, boolean isRootRelation) {
			if (isRootRelation) {
				relations.add(new PrimitiveRangeRelationInfo(methodName, fieldName));
			}
		}
	}

	private static class ComplexRangeRootRelationFiller extends AbstractPojoClassVisitor {

		private Set<ComplexRangeRelationInfo> relations = new TreeSet<>(
				Comparator.comparing(ComplexRangeRelationInfo::getAttributeName)
						.thenComparing(ComplexRangeRelationInfo::getModelName));

		public ComplexRangeRootRelationFiller(SearchPathInfo info) {
			info.setComplexRangeRelations(relations);
		}

		@Override
		public void onComplexRelation(String methodName, String fieldName, String range, boolean isRootRelation) {
			if (isRootRelation) {
				relations.add(new ComplexRangeRelationInfo(methodName, fieldName, range));
			}
		}
	}
}