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

public class TestPath {

  static final String SEGMENT_ADMINISTRATION = "administration";
  static final String SEGMENT_ASSET_KIND = "assetKind";
  static final String SEGMENT_ASSET_TYPE = "assetType";
  static final String SEGMENT_CREATOR = "creator";
  static final String SEGMENT_DATA_SPECIFICATION = "dataSpecification";
  static final String SEGMENT_DATA_SPECIFICATION_CONTENT = "dataSpecificationContent";
  static final String SEGMENT_DATA_TYPE = "dataType";
  static final String SEGMENT_DEFINITION = "definition";
  static final String SEGMENT_DESCRIPTION = "description";
  static final String SEGMENT_DISPLAY_NAME = "displayName";
  static final String SEGMENT_EMBEDDED_DATA_SPECIFICATIONS = "embeddedDataSpecifications";
  static final String SEGMENT_ENDPOINT_PROTOCOL = "endpointProtocol";
  static final String SEGMENT_ENDPOINT_PROTOCOL_VERSION = "endpointProtocolVersion";
  static final String SEGMENT_ENDPOINTS = "endpoints";
  static final String SEGMENT_EXTENSIONS = "extensions";
  static final String SEGMENT_EXTERNAL_SUBJECT_ID = "externalSubjectId";
  static final String SEGMENT_GLOBAL_ASSET_ID = "globalAssetId";
  static final String SEGMENT_HREF = "href";
  static final String SEGMENT_ID = "id";
  static final String SEGMENT_ID_SHORT = "idShort";
  static final String SEGMENT_INTERFACE = "interface";
  static final String SEGMENT_KEY = "key";
  static final String SEGMENT_KEYS = "keys";
  static final String SEGMENT_LANGUAGE = "language";
  static final String SEGMENT_LEVEL_TYPE = "levelType";
  static final String SEGMENT_MAX = "max";
  static final String SEGMENT_MIN = "min";
  static final String SEGMENT_MODEL_TYPE = "modelType";
  static final String SEGMENT_NAME = "name";
  static final String SEGMENT_NOM = "nom";
  static final String SEGMENT_PREFERRED_NAME = "preferredName";
  static final String SEGMENT_PROTOCOL_INFORMATION = "protocolInformation";
  static final String SEGMENT_REFERRED_SEMANTIC_ID = "referredSemanticId";
  static final String SEGMENT_REFERS_TO = "refersTo";
  static final String SEGMENT_REVISION = "revision";
  static final String SEGMENT_SECURITY_ATTRIBUTES = "securityAttributes";
  static final String SEGMENT_SEMANTIC_ID = "semanticId";
  static final String SEGMENT_SHORT_NAME = "shortName";
  static final String SEGMENT_SOURCE_OF_DEFINITION = "sourceOfDefinition";
  static final String SEGMENT_SPECIFIC_ASSET_IDS = "specificAssetIds";
  static final String SEGMENT_SUBMODEL_DESCRIPTORS = "submodelDescriptors";
  static final String SEGMENT_SUBPROTOCOL = "subprotocol";
  static final String SEGMENT_SUBPROTOCOL_BODY = "subprotocolBody";
  static final String SEGMENT_SUBPROTOCOL_BODY_ENCODING = "subprotocolBodyEncoding";
  static final String SEGMENT_SUPPLEMENTAL_SEMANTIC_ID = "supplementalSemanticId";
  static final String SEGMENT_SUPPLEMENTAL_SEMANTIC_IDS = "supplementalSemanticIds";
  static final String SEGMENT_SYMBOL = "symbol";
  static final String SEGMENT_TEMPLATE_ID = "templateId";
  static final String SEGMENT_TEXT = "text";
  static final String SEGMENT_TYP = "typ";
  static final String SEGMENT_TYPE = "type";
  static final String SEGMENT_UNIT = "unit";
  static final String SEGMENT_UNIT_ID = "unitId";
  static final String SEGMENT_VALUE = "value";
  static final String SEGMENT_VALUE_FORMAT = "valueFormat";
  static final String SEGMENT_VALUE_ID = "valueId";
  static final String SEGMENT_VALUE_LIST = "valueList";
  static final String SEGMENT_VALUE_REFERENCE_PAIRS = "valueReferencePairs";
  static final String SEGMENT_VALUE_TYPE = "valueType";
  static final String SEGMENT_VERSION = "version";

  private TestPath() {
  }
  public static AdministrativeInformationPath administration() {
    return new AdministrativeInformationPath(SEGMENT_ADMINISTRATION);
  }

  public abstract static class SimplePath {

    protected final String path;

    protected SimplePath(String path) {
      this.path = path;
    }

    protected SimplePath(String path, String segment) {
      this(path + "." + segment);
    }

    protected String append(String segment) {
		return path + "." + segment;
    }
    
    @Override
    public String toString() {
      return path;
    }
  }

  public static final class AdministrativeInformationPath extends SimplePath {

    private AdministrativeInformationPath(String path) {
      super(path);
    }

    private AdministrativeInformationPath(String path, String segment) {
	  super(path, segment);
	}

    public String version() {
    	return append(SEGMENT_VERSION);
    }


  }
  public static final class DataSpecificationContentChoicePath extends SimplePath {

    public DataSpecificationIec61360Path asDataSpecificationIec61360() {
      return new DataSpecificationIec61360Path(path);
    }


  }
  public static final class DataSpecificationIec61360Path extends SimplePath {

    private DataSpecificationIec61360Path(String path) {
      super(path);
    }

    public LangStringPreferredNameTypeIec61360Path preferredName() {
      return new LangStringPreferredNameTypeIec61360Path(path, SEGMENT_PREFERRED_NAME);
    }


  }
  public static final class EmbeddedDataSpecificationPath extends SimplePath {

    public ReferencePath dataSpecification() {
      return new ReferencePath(path, SEGMENT_DATA_SPECIFICATION);
    }


  }
  public static final class EndpointPath extends SimplePath {

    public String _interface() {
    	return append(SEGMENT_INTERFACE);
    }


  }
  public static final class ExtensionPath extends SimplePath {

    public String name() {
    	return append(SEGMENT_NAME);
    }


  }
  public static final class KeyPath extends SimplePath {

    public String type() {
    	return append(SEGMENT_TYPE);
    }


  }
  public static final class LangStringDefinitionTypeIec61360Path extends SimplePath {

    public ObjectPath langStringDefinitionTypeIec61360Text() {
      return new ObjectPath(path, SEGMENT_TEXT);
    }


  }
  public static final class LangStringNameTypePath extends SimplePath {

    public ObjectPath langStringNameTypeText() {
      return new ObjectPath(path, SEGMENT_TEXT);
    }


  }
  public static final class LangStringPreferredNameTypeIec61360Path extends SimplePath {

    private LangStringPreferredNameTypeIec61360Path(String path, String segment) {
	  super(path, segment);
	}

    public ObjectPath langStringPreferredNameTypeIec61360Text() {
      return new ObjectPath(path, SEGMENT_TEXT);
    }


  }
  public static final class LangStringShortNameTypeIec61360Path extends SimplePath {

    public ObjectPath langStringShortNameTypeIec61360Text() {
      return new ObjectPath(path, SEGMENT_TEXT);
    }


  }
  public static final class LangStringTextTypePath extends SimplePath {

    public ObjectPath langStringTextTypeText() {
      return new ObjectPath(path, SEGMENT_TEXT);
    }


  }
  public static final class LevelTypePath extends SimplePath {

    public String min() {
    	return append(SEGMENT_MIN);
    }


  }
  public static final class ObjectPath extends SimplePath {

    private ObjectPath(String path, String segment) {
	  super(path, segment);
	}


  }
  public static final class ProtocolInformationPath extends SimplePath {

    public String href() {
    	return append(SEGMENT_HREF);
    }


  }
  public static final class ProtocolInformationSecurityAttributesPath extends SimplePath {

    public String type() {
    	return append(SEGMENT_TYPE);
    }


  }
  public static final class ReferencePath extends SimplePath {

    private ReferencePath(String path, String segment) {
	  super(path, segment);
	}

    public ReferenceParentPath referredSemanticId() {
      return new ReferenceParentPath(path, SEGMENT_REFERRED_SEMANTIC_ID);
    }


  }
  public static final class ReferenceParentPath extends SimplePath {

    private ReferenceParentPath(String path, String segment) {
	  super(path, segment);
	}

    public String type() {
    	return append(SEGMENT_TYPE);
    }


  }
  public static final class SpecificAssetIdPath extends SimplePath {

    public String name() {
    	return append(SEGMENT_NAME);
    }


  }
  public static final class SubmodelDescriptorPath extends SimplePath {

    public AdministrativeInformationPath administration() {
      return new AdministrativeInformationPath(path, SEGMENT_ADMINISTRATION);
    }


  }
  public static final class ValueListPath extends SimplePath {

    public ValueReferencePairPath valueReferencePairs() {
      return new ValueReferencePairPath(path, SEGMENT_VALUE_REFERENCE_PAIRS);
    }


  }
  public static final class ValueReferencePairPath extends SimplePath {

    private ValueReferencePairPath(String path, String segment) {
	  super(path, segment);
	}

    public String value() {
    	return append(SEGMENT_VALUE);
    }


  }
}
