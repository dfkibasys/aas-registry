package org.eclipse.basyx.aas.registry.util.path;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.basyx.aas.registry.util.path.PojoClassVisitor.PojoRelation;
import org.eclipse.basyx.aas.registry.util.path.PojoClassVisitor.PojoRelation.PojoRelationBuilder;
import org.eclipse.basyx.aas.registry.util.path.PojoClassVisitor.PojoRelation.PojoRelationType;

import com.fasterxml.jackson.annotation.JsonSubTypes;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class PojoClassWalker {

	private final Class<?> root;
	private final PojoClassVisitor visitor;

	public void walkClass() {
		walkClass(root, null);
		visitor.stop();
	}

	private void walkClass(Class<?> cls, String path) {
		String name = cls.getSimpleName();
		if (visitor.startType(name, cls == root)) {
			for (Field field : getFields(cls)) {
				walkField(cls, field, path);
			}
		}
		visitor.endType();
	}

	private void walkField(Class<?> subjectcls, Field field, String path) {
		if (Modifier.isStatic(field.getModifiers())) {
			return;
		}
		String methodName = field.getName();
		String fieldName = getModelPropertyOrFieldName(field);

		PojoRelation.PojoRelationBuilder builder = PojoRelation.builder().methodName(methodName).fieldName(fieldName)
				.isRootRelation(subjectcls == root).subject(subjectcls.getSimpleName());
		String newPath = generateNewPath(path, fieldName);
		Class<?> type = getFieldTypeAndAssignRange(field, builder);
				
		if (type.isPrimitive() || type.equals(String.class) || Enum.class.isAssignableFrom(type)) {
			walkPrimitiveRelation(builder);
		} else {
			walkComplexRelation(type, newPath, builder);
		}
	}

	private Class<?> getFieldTypeAndAssignRange(Field field, PojoRelationBuilder builder) {
		Class<?> type = field.getType();
		if (List.class.isAssignableFrom(type)) {
			type = getGenericClass(field, 0);
			builder.type(PojoRelationType.LIST);
		} else if (Map.class.isAssignableFrom(type)) {
			type = getGenericClass(field, 1);
			builder.type(PojoRelationType.MAP);
		} else {
			builder.type(PojoRelationType.FUNCTIONAL);
		}
		return type;
	}

	private void walkComplexRelation(Class<?> type, String newPath, PojoRelationBuilder builder) {
		String typeName = type.getSimpleName();
		List<Class<?>> subTypes = getSubTypes(type);
		PojoRelation relation = builder.range(typeName).build();
		visitor.startRelation(relation);
		if (subTypes.isEmpty()) {
			walkClass(type, newPath);
		} else {
			List<String> subTypeNames = subTypes.stream().map(Class::getSimpleName).collect(Collectors.toList());
			visitor.onSubTypeRelation(typeName, subTypeNames);
			walkClass(type, newPath);
			for (Class<?> eachSubtype : subTypes) {
				walkClass(eachSubtype, newPath);
			}
		}
		visitor.endRelation(relation);
	}

	private void walkPrimitiveRelation(PojoRelationBuilder builder) {
		PojoRelation relation = builder.build();
		visitor.startRelation(relation);
		visitor.endRelation(relation);
	}

	private static List<Field> getFields(Class<?> cls) {
		List<Field> fields = new LinkedList<>();
		getFields(cls, fields);
		return fields;
	}

	private static void getFields(Class<?> cls, List<Field> fields) {
		if (cls != null) {
			fields.addAll(List.of(cls.getDeclaredFields()));
			getFields(cls.getSuperclass(), fields);
		}
	}
	
	private String getModelPropertyOrFieldName(Field eachField) {
		org.springframework.data.elasticsearch.annotations.Field elasticField = eachField
				.getDeclaredAnnotation(org.springframework.data.elasticsearch.annotations.Field.class);
		if (elasticField != null) {
			String name = elasticField.name();
			if (!StringUtils.isEmpty(name)) {
				return name;
			}
			name = elasticField.value();
			if (!StringUtils.isEmpty(name)) {
				return name;
			}
		}
		return eachField.getName();
	}

	private String generateNewPath(String currentPath, String fieldName) {
		return currentPath == null ? fieldName : String.join(".", currentPath, fieldName);
	}

	private Class<?> getGenericClass(Field field, int pos) {
		ParameterizedType genType = (ParameterizedType) field.getGenericType();
		return (Class<?>) genType.getActualTypeArguments()[pos];
	}

	private List<Class<?>> getSubTypes(Class<?> type) {
		JsonSubTypes types = type.getAnnotation(JsonSubTypes.class);
		if (types == null) {
			return Collections.emptyList();
		}
		JsonSubTypes.Type[] referencedTypes = types.value();
		if (referencedTypes == null) {
			return Collections.emptyList();
		}
		List<Class<?>> toReturn = new ArrayList<>(referencedTypes.length);
		for (JsonSubTypes.Type eachType : referencedTypes) {
			Class<?> subType = eachType.value();
			toReturn.add(subType);
		}
		return toReturn;
	}

}
