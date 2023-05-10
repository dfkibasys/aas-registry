/*******************************************************************************
 * Copyright (C) 2022 DFKI GmbH
 * Author: Gerhard Sonnenberg (gerhard.sonnenberg@dfki.de)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * SPDX-License-Identifier: MIT
 ******************************************************************************/
package de.test; 

import java.util.ArrayDeque;
import java.util.List;
import java.util.ListIterator;
import java.util.function.BiFunction;

import de.dfki.cos.basys.aas.registry.model.AdministrativeInformation;
import de.dfki.cos.basys.aas.registry.model.AssetAdministrationShellDescriptor;
import de.dfki.cos.basys.aas.registry.model.DataSpecificationContentChoice;
import de.dfki.cos.basys.aas.registry.model.DataSpecificationIec61360;
import de.dfki.cos.basys.aas.registry.model.EmbeddedDataSpecification;
import de.dfki.cos.basys.aas.registry.model.Endpoint;
import de.dfki.cos.basys.aas.registry.model.Extension;
import de.dfki.cos.basys.aas.registry.model.Key;
import de.dfki.cos.basys.aas.registry.model.LangStringDefinitionTypeIec61360;
import de.dfki.cos.basys.aas.registry.model.LangStringNameType;
import de.dfki.cos.basys.aas.registry.model.LangStringPreferredNameTypeIec61360;
import de.dfki.cos.basys.aas.registry.model.LangStringShortNameTypeIec61360;
import de.dfki.cos.basys.aas.registry.model.LangStringTextType;
import de.dfki.cos.basys.aas.registry.model.LevelType;
import de.dfki.cos.basys.aas.registry.model.Object;
import de.dfki.cos.basys.aas.registry.model.ProtocolInformation;
import de.dfki.cos.basys.aas.registry.model.ProtocolInformationSecurityAttributes;
import de.dfki.cos.basys.aas.registry.model.Reference;
import de.dfki.cos.basys.aas.registry.model.ReferenceParent;
import de.dfki.cos.basys.aas.registry.model.SpecificAssetId;
import de.dfki.cos.basys.aas.registry.model.SubmodelDescriptor;
import de.dfki.cos.basys.aas.registry.model.ValueList;
import de.dfki.cos.basys.aas.registry.model.ValueReferencePair;

public class TestProcessor {

	private final AssetAdministrationShellDescriptor subject;

	public TestProcessor(AssetAdministrationShellDescriptor subject) {
		this.subject = subject;
	}

	public void visitValuesAtPath(String path, AssetAdministrationShellDescriptorVisitor visitor) {
		InternalPathProcessor processor = new InternalPathProcessor(visitor, path);
		processor.visitObject(subject, processor::visitAssetAdministrationShellDescriptor);
	}

	public static interface AssetAdministrationShellDescriptorVisitor {

		default ProcessInstruction visitResolvedPathValue(String path, Object[] objectPathToValue, String value) {
			return ProcessInstruction.CONTINUE;
		}
		
	}
	
	public enum ProcessInstruction {
		ABORT, CONTINUE
	}

	private static final class InternalPathProcessor {

		private final AssetAdministrationShellDescriptorVisitor visitor;
		private final ArrayDeque<Object> currentPathElements = new ArrayDeque<>();
		private final String path;
		private ListIterator<String> pathIterator;

		public InternalPathProcessor(AssetAdministrationShellDescriptorVisitor visitor, String path) {
			this.visitor = visitor;
			this.path = path;
			String[] pathAsArray = path.split("\\.");
			pathIterator = List.of(pathAsArray).listIterator();
		}

		public ProcessInstruction visitAdministrativeInformation(AdministrativeInformation toVisit, String segment) {
			switch (segment) {
			case TestPath.SEGMENT_VERSION:
				return visitPrimitiveValue(toVisit.getVersion());
			default:
				return ProcessInstruction.CONTINUE;
			}
		}
		
		public ProcessInstruction visitAssetAdministrationShellDescriptor(AssetAdministrationShellDescriptor toVisit, String segment) {
			switch (segment) {
			case TestPath.SEGMENT_ADMINISTRATION:
				return visitObject(toVisit.getAdministration(), this::visitAdministrativeInformation);
			default:
				return ProcessInstruction.CONTINUE;
			}
		}
		
		public ProcessInstruction visitDataSpecificationContentChoice(DataSpecificationContentChoice toVisit, String segment) {
			switch (segment) {
			default:
				if (toVisit instanceof DataSpecificationIec61360) {
					return visitDataSpecificationIec61360((DataSpecificationIec61360) toVisit, segment);
				}
				return ProcessInstruction.CONTINUE;
			}
		}
		
		public ProcessInstruction visitDataSpecificationIec61360(DataSpecificationIec61360 toVisit, String segment) {
			switch (segment) {
			case TestPath.SEGMENT_PREFERRED_NAME:
				return visitObjectList(toVisit.getPreferredName(), this::visitLangStringPreferredNameTypeIec61360);
			default:
				return ProcessInstruction.CONTINUE;
			}
		}
		
		public ProcessInstruction visitEmbeddedDataSpecification(EmbeddedDataSpecification toVisit, String segment) {
			switch (segment) {
			case TestPath.SEGMENT_DATA_SPECIFICATION:
				return visitObject(toVisit.getDataSpecification(), this::visitReference);
			default:
				return ProcessInstruction.CONTINUE;
			}
		}
		
		public ProcessInstruction visitEndpoint(Endpoint toVisit, String segment) {
			switch (segment) {
			case TestPath.SEGMENT_INTERFACE:
				return visitPrimitiveValue(toVisit.getInterface());
			default:
				return ProcessInstruction.CONTINUE;
			}
		}
		
		public ProcessInstruction visitExtension(Extension toVisit, String segment) {
			switch (segment) {
			case TestPath.SEGMENT_NAME:
				return visitPrimitiveValue(toVisit.getName());
			default:
				return ProcessInstruction.CONTINUE;
			}
		}
		
		public ProcessInstruction visitKey(Key toVisit, String segment) {
			switch (segment) {
			case TestPath.SEGMENT_TYPE:
				return visitPrimitiveValue(toVisit.getType());
			default:
				return ProcessInstruction.CONTINUE;
			}
		}
		
		public ProcessInstruction visitLangStringDefinitionTypeIec61360(LangStringDefinitionTypeIec61360 toVisit, String segment) {
			switch (segment) {
			case TestPath.SEGMENT_TEXT:
				return visitObject(toVisit.getText(), this::visitObject);
			default:
				return ProcessInstruction.CONTINUE;
			}
		}
		
		public ProcessInstruction visitLangStringNameType(LangStringNameType toVisit, String segment) {
			switch (segment) {
			case TestPath.SEGMENT_TEXT:
				return visitObject(toVisit.getText(), this::visitObject);
			default:
				return ProcessInstruction.CONTINUE;
			}
		}
		
		public ProcessInstruction visitLangStringPreferredNameTypeIec61360(LangStringPreferredNameTypeIec61360 toVisit, String segment) {
			switch (segment) {
			case TestPath.SEGMENT_TEXT:
				return visitObject(toVisit.getText(), this::visitObject);
			default:
				return ProcessInstruction.CONTINUE;
			}
		}
		
		public ProcessInstruction visitLangStringShortNameTypeIec61360(LangStringShortNameTypeIec61360 toVisit, String segment) {
			switch (segment) {
			case TestPath.SEGMENT_TEXT:
				return visitObject(toVisit.getText(), this::visitObject);
			default:
				return ProcessInstruction.CONTINUE;
			}
		}
		
		public ProcessInstruction visitLangStringTextType(LangStringTextType toVisit, String segment) {
			switch (segment) {
			case TestPath.SEGMENT_TEXT:
				return visitObject(toVisit.getText(), this::visitObject);
			default:
				return ProcessInstruction.CONTINUE;
			}
		}
		
		public ProcessInstruction visitLevelType(LevelType toVisit, String segment) {
			switch (segment) {
			case TestPath.SEGMENT_MIN:
				return visitPrimitiveValue(toVisit.isMin());
			default:
				return ProcessInstruction.CONTINUE;
			}
		}
		
		public ProcessInstruction visitObject(Object toVisit, String segment) {
			switch (segment) {
			default:
				return ProcessInstruction.CONTINUE;
			}
		}
		
		public ProcessInstruction visitProtocolInformation(ProtocolInformation toVisit, String segment) {
			switch (segment) {
			case TestPath.SEGMENT_HREF:
				return visitPrimitiveValue(toVisit.getHref());
			default:
				return ProcessInstruction.CONTINUE;
			}
		}
		
		public ProcessInstruction visitProtocolInformationSecurityAttributes(ProtocolInformationSecurityAttributes toVisit, String segment) {
			switch (segment) {
			case TestPath.SEGMENT_TYPE:
				return visitPrimitiveValue(toVisit.getType());
			default:
				return ProcessInstruction.CONTINUE;
			}
		}
		
		public ProcessInstruction visitReference(Reference toVisit, String segment) {
			switch (segment) {
			case TestPath.SEGMENT_REFERRED_SEMANTIC_ID:
				return visitObject(toVisit.getReferredSemanticId(), this::visitReferenceParent);
			default:
				return ProcessInstruction.CONTINUE;
			}
		}
		
		public ProcessInstruction visitReferenceParent(ReferenceParent toVisit, String segment) {
			switch (segment) {
			case TestPath.SEGMENT_TYPE:
				return visitPrimitiveValue(toVisit.getType());
			default:
				return ProcessInstruction.CONTINUE;
			}
		}
		
		public ProcessInstruction visitSpecificAssetId(SpecificAssetId toVisit, String segment) {
			switch (segment) {
			case TestPath.SEGMENT_NAME:
				return visitPrimitiveValue(toVisit.getName());
			default:
				return ProcessInstruction.CONTINUE;
			}
		}
		
		public ProcessInstruction visitSubmodelDescriptor(SubmodelDescriptor toVisit, String segment) {
			switch (segment) {
			case TestPath.SEGMENT_ADMINISTRATION:
				return visitObject(toVisit.getAdministration(), this::visitAdministrativeInformation);
			default:
				return ProcessInstruction.CONTINUE;
			}
		}
		
		public ProcessInstruction visitValueList(ValueList toVisit, String segment) {
			switch (segment) {
			case TestPath.SEGMENT_VALUE_REFERENCE_PAIRS:
				return visitObjectList(toVisit.getValueReferencePairs(), this::visitValueReferencePair);
			default:
				return ProcessInstruction.CONTINUE;
			}
		}
		
		public ProcessInstruction visitValueReferencePair(ValueReferencePair toVisit, String segment) {
			switch (segment) {
			case TestPath.SEGMENT_VALUE:
				return visitPrimitiveValue(toVisit.getValue());
			default:
				return ProcessInstruction.CONTINUE;
			}
		}
		
		private <T> ProcessInstruction visitObjectList(List<T> list,
				BiFunction<T, String, ProcessInstruction> processor) {
			if (list != null) {
				for (T eachValue : list) {
					if (visitObject(eachValue, processor) == ProcessInstruction.ABORT) {
						return ProcessInstruction.ABORT;
					}
				}
			}
			return ProcessInstruction.CONTINUE;
		}
		
		private <T> ProcessInstruction visitObject(T object, BiFunction<T, String, ProcessInstruction> processor) {
			if (object != null && pathIterator.hasNext()) {
				String token = pathIterator.next();
				currentPathElements.addLast(object);
				ProcessInstruction instruction = processor.apply(object, token);
				currentPathElements.removeLast();
				pathIterator.previous();
				return instruction;
			}
			return ProcessInstruction.CONTINUE;
		}

		private ProcessInstruction visitPrimitiveValue(Object value) {
			if (value != null) {
				Object[] objectPath = currentPathElements.toArray();
				return visitor.visitResolvedPathValue(path, objectPath, value.toString());
			}
			return ProcessInstruction.CONTINUE;
		}

		private ProcessInstruction visitPrimitiveValueList(List<?> values) {
			if (values != null) {
				Object[] objectPath = currentPathElements.toArray();
				for (Object eachValue : values) {
					ProcessInstruction instruction = visitor.visitResolvedPathValue(path, objectPath, eachValue.toString());
					if (instruction == ProcessInstruction.ABORT) {
						return ProcessInstruction.ABORT;
					}
				}
			}
			return ProcessInstruction.CONTINUE;
		}
	}
}
