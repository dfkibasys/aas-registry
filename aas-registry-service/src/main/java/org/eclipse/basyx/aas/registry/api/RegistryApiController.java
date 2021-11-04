package org.eclipse.basyx.aas.registry.api;

import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.basyx.aas.registry.model.SubmodelDescriptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import org.eclipse.basyx.aas.registry.service.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-03T09:35:10.988Z[GMT]")
@RestController
public class RegistryApiController implements RegistryApi {

    private static final Logger log = LoggerFactory.getLogger(RegistryApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    private final RegistryService service;

    @org.springframework.beans.factory.annotation.Autowired
    public RegistryApiController(ObjectMapper objectMapper, HttpServletRequest request, RegistryService service) {
        this.objectMapper = objectMapper;
        this.request = request;
        this.service = service;
    }

    public ResponseEntity<Void> deleteAssetAdministrationShellDescriptorById(@Parameter(in = ParameterIn.PATH, description = "The Asset Administration Shell’s unique id (BASE64-URL-encoded)", required=true, schema=@Schema()) @PathVariable("aasIdentifier") String aasIdentifier) {
        //String accept = request.getHeader("Accept");
        //return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);

        if (!service.existsAssetAdministrationShellDescriptorById(aasIdentifier)) {
            return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
        }

        service.unregisterAssetAdministrationShellDescriptorById(aasIdentifier);
        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);

    }

    public ResponseEntity<Void> deleteSubmodelDescriptorById(@Parameter(in = ParameterIn.PATH, description = "The Asset Administration Shell’s unique id (BASE64-URL-encoded)", required=true, schema=@Schema()) @PathVariable("aasIdentifier") String aasIdentifier,@Parameter(in = ParameterIn.PATH, description = "The Submodel’s unique id (BASE64-URL-encoded)", required=true, schema=@Schema()) @PathVariable("submodelIdentifier") String submodelIdentifier) {
        //String accept = request.getHeader("Accept");
        //return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);

        if (!service.existsSubmodelDescriptorById(aasIdentifier, submodelIdentifier)) {
            return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
        }

        service.unregisterSubmodelDescriptorById(aasIdentifier, submodelIdentifier);
        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);


    }

    public ResponseEntity<List<AssetAdministrationShellDescriptor>> getAllAssetAdministrationShellDescriptors() {
//        String accept = request.getHeader("Accept");
//        if (accept != null && accept.contains("application/json")) {
//            try {
//                return new ResponseEntity<List<AssetAdministrationShellDescriptor>>(objectMapper.readValue("[ {\n  \"identification\" : {\n    \"id\" : \"id\"\n  },\n  \"idShort\" : \"idShort\",\n  \"specificAssetIds\" : [ \"\", \"\" ],\n  \"administration\" : {\n    \"version\" : \"version\",\n    \"revision\" : \"revision\"\n  },\n  \"description\" : [ {\n    \"language\" : \"language\",\n    \"text\" : \"text\"\n  }, {\n    \"language\" : \"language\",\n    \"text\" : \"text\"\n  } ],\n  \"submodelDescriptors\" : [ {\n    \"idShort\" : \"idShort\",\n    \"description\" : [ null, null ]\n  }, {\n    \"idShort\" : \"idShort\",\n    \"description\" : [ null, null ]\n  } ],\n  \"globalAssetId\" : {\n    \"keys\" : [ {\n      \"idType\" : \"Custom\",\n      \"type\" : \"Asset\",\n      \"value\" : \"value\"\n    }, {\n      \"idType\" : \"Custom\",\n      \"type\" : \"Asset\",\n      \"value\" : \"value\"\n    } ]\n  }\n}, {\n  \"identification\" : {\n    \"id\" : \"id\"\n  },\n  \"idShort\" : \"idShort\",\n  \"specificAssetIds\" : [ \"\", \"\" ],\n  \"administration\" : {\n    \"version\" : \"version\",\n    \"revision\" : \"revision\"\n  },\n  \"description\" : [ {\n    \"language\" : \"language\",\n    \"text\" : \"text\"\n  }, {\n    \"language\" : \"language\",\n    \"text\" : \"text\"\n  } ],\n  \"submodelDescriptors\" : [ {\n    \"idShort\" : \"idShort\",\n    \"description\" : [ null, null ]\n  }, {\n    \"idShort\" : \"idShort\",\n    \"description\" : [ null, null ]\n  } ],\n  \"globalAssetId\" : {\n    \"keys\" : [ {\n      \"idType\" : \"Custom\",\n      \"type\" : \"Asset\",\n      \"value\" : \"value\"\n    }, {\n      \"idType\" : \"Custom\",\n      \"type\" : \"Asset\",\n      \"value\" : \"value\"\n    } ]\n  }\n} ]", List.class), HttpStatus.NOT_IMPLEMENTED);
//            } catch (IOException e) {
//                log.error("Couldn't serialize response for content type application/json", e);
//                return new ResponseEntity<List<AssetAdministrationShellDescriptor>>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        }
//
//        return new ResponseEntity<List<AssetAdministrationShellDescriptor>>(HttpStatus.NOT_IMPLEMENTED);

        var result = service.getAllAssetAdministrationShellDescriptors();
        return new ResponseEntity<List<AssetAdministrationShellDescriptor>>(result, HttpStatus.OK);

    }

    public ResponseEntity<List<SubmodelDescriptor>> getAllSubmodelDescriptors(@Parameter(in = ParameterIn.PATH, description = "The Asset Administration Shell’s unique id (BASE64-URL-encoded)", required=true, schema=@Schema()) @PathVariable("aasIdentifier") String aasIdentifier) {
//        String accept = request.getHeader("Accept");
//        if (accept != null && accept.contains("application/json")) {
//            try {
//                return new ResponseEntity<List<SubmodelDescriptor>>(objectMapper.readValue("[ {\n  \"idShort\" : \"idShort\",\n  \"description\" : [ null, null ]\n}, {\n  \"idShort\" : \"idShort\",\n  \"description\" : [ null, null ]\n} ]", List.class), HttpStatus.NOT_IMPLEMENTED);
//            } catch (IOException e) {
//                log.error("Couldn't serialize response for content type application/json", e);
//                return new ResponseEntity<List<SubmodelDescriptor>>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        }
//
//        return new ResponseEntity<List<SubmodelDescriptor>>(HttpStatus.NOT_IMPLEMENTED);

        if (!service.existsAssetAdministrationShellDescriptorById(aasIdentifier)) {
            return new ResponseEntity<List<SubmodelDescriptor>>(HttpStatus.NOT_FOUND);
        }

        var result = service.getAllSubmodelDescriptors(aasIdentifier);
        return new ResponseEntity<List<SubmodelDescriptor>>(result, HttpStatus.OK);

    }

    public ResponseEntity<AssetAdministrationShellDescriptor> getAssetAdministrationShellDescriptorById(@Parameter(in = ParameterIn.PATH, description = "The Asset Administration Shell’s unique id (BASE64-URL-encoded)", required=true, schema=@Schema()) @PathVariable("aasIdentifier") String aasIdentifier) {
//        String accept = request.getHeader("Accept");
//        if (accept != null && accept.contains("application/json")) {
//            try {
//                return new ResponseEntity<AssetAdministrationShellDescriptor>(objectMapper.readValue("{\n  \"identification\" : {\n    \"id\" : \"id\"\n  },\n  \"idShort\" : \"idShort\",\n  \"specificAssetIds\" : [ \"\", \"\" ],\n  \"administration\" : {\n    \"version\" : \"version\",\n    \"revision\" : \"revision\"\n  },\n  \"description\" : [ {\n    \"language\" : \"language\",\n    \"text\" : \"text\"\n  }, {\n    \"language\" : \"language\",\n    \"text\" : \"text\"\n  } ],\n  \"submodelDescriptors\" : [ {\n    \"idShort\" : \"idShort\",\n    \"description\" : [ null, null ]\n  }, {\n    \"idShort\" : \"idShort\",\n    \"description\" : [ null, null ]\n  } ],\n  \"globalAssetId\" : {\n    \"keys\" : [ {\n      \"idType\" : \"Custom\",\n      \"type\" : \"Asset\",\n      \"value\" : \"value\"\n    }, {\n      \"idType\" : \"Custom\",\n      \"type\" : \"Asset\",\n      \"value\" : \"value\"\n    } ]\n  }\n}", AssetAdministrationShellDescriptor.class), HttpStatus.NOT_IMPLEMENTED);
//            } catch (IOException e) {
//                log.error("Couldn't serialize response for content type application/json", e);
//                return new ResponseEntity<AssetAdministrationShellDescriptor>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        }
//
//        return new ResponseEntity<AssetAdministrationShellDescriptor>(HttpStatus.NOT_IMPLEMENTED);

        var result = service.getAssetAdministrationShellDescriptorById(aasIdentifier);
        if (result.isPresent()) {
            return new ResponseEntity<AssetAdministrationShellDescriptor>(result.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<AssetAdministrationShellDescriptor>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<SubmodelDescriptor> getSubmodelDescriptorById(@Parameter(in = ParameterIn.PATH, description = "The Asset Administration Shell’s unique id (BASE64-URL-encoded)", required=true, schema=@Schema()) @PathVariable("aasIdentifier") String aasIdentifier,@Parameter(in = ParameterIn.PATH, description = "The Submodel’s unique id (BASE64-URL-encoded)", required=true, schema=@Schema()) @PathVariable("submodelIdentifier") String submodelIdentifier) {
//        String accept = request.getHeader("Accept");
//        if (accept != null && accept.contains("application/json")) {
//            try {
//                return new ResponseEntity<SubmodelDescriptor>(objectMapper.readValue("{\n  \"idShort\" : \"idShort\",\n  \"description\" : [ null, null ]\n}", SubmodelDescriptor.class), HttpStatus.NOT_IMPLEMENTED);
//            } catch (IOException e) {
//                log.error("Couldn't serialize response for content type application/json", e);
//                return new ResponseEntity<SubmodelDescriptor>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        }
//
//        return new ResponseEntity<SubmodelDescriptor>(HttpStatus.NOT_IMPLEMENTED);

        if (!service.existsAssetAdministrationShellDescriptorById(aasIdentifier)) {
            return new ResponseEntity<SubmodelDescriptor>(HttpStatus.NOT_FOUND);
        }

        var result = service.getSubmodelDescriptorById(aasIdentifier, submodelIdentifier);
        if (result.isPresent()) {
            return new ResponseEntity<SubmodelDescriptor>(result.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<SubmodelDescriptor>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<AssetAdministrationShellDescriptor> postAssetAdministrationShellDescriptor(@Parameter(in = ParameterIn.DEFAULT, description = "Asset Administration Shell Descriptor object", required=true, schema=@Schema()) @Valid @RequestBody AssetAdministrationShellDescriptor body) {
//        String accept = request.getHeader("Accept");
//        if (accept != null && accept.contains("application/json")) {
//            try {
//                return new ResponseEntity<AssetAdministrationShellDescriptor>(objectMapper.readValue("{\n  \"identification\" : {\n    \"id\" : \"id\"\n  },\n  \"idShort\" : \"idShort\",\n  \"specificAssetIds\" : [ \"\", \"\" ],\n  \"administration\" : {\n    \"version\" : \"version\",\n    \"revision\" : \"revision\"\n  },\n  \"description\" : [ {\n    \"language\" : \"language\",\n    \"text\" : \"text\"\n  }, {\n    \"language\" : \"language\",\n    \"text\" : \"text\"\n  } ],\n  \"submodelDescriptors\" : [ {\n    \"idShort\" : \"idShort\",\n    \"description\" : [ null, null ]\n  }, {\n    \"idShort\" : \"idShort\",\n    \"description\" : [ null, null ]\n  } ],\n  \"globalAssetId\" : {\n    \"keys\" : [ {\n      \"idType\" : \"Custom\",\n      \"type\" : \"Asset\",\n      \"value\" : \"value\"\n    }, {\n      \"idType\" : \"Custom\",\n      \"type\" : \"Asset\",\n      \"value\" : \"value\"\n    } ]\n  }\n}", AssetAdministrationShellDescriptor.class), HttpStatus.NOT_IMPLEMENTED);
//            } catch (IOException e) {
//                log.error("Couldn't serialize response for content type application/json", e);
//                return new ResponseEntity<AssetAdministrationShellDescriptor>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        }
//
//        return new ResponseEntity<AssetAdministrationShellDescriptor>(HttpStatus.NOT_IMPLEMENTED);

        if (!service.existsAssetAdministrationShellDescriptorById(body.getIdentification().getId())) {
            return new ResponseEntity<AssetAdministrationShellDescriptor>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        var result = service.registerAssetAdministrationShellDescriptor(body);
        if (result != null) {
            return new ResponseEntity<AssetAdministrationShellDescriptor>(result, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<AssetAdministrationShellDescriptor>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<SubmodelDescriptor> postSubmodelDescriptor(@Parameter(in = ParameterIn.PATH, description = "The Asset Administration Shell’s unique id (BASE64-URL-encoded)", required=true, schema=@Schema()) @PathVariable("aasIdentifier") String aasIdentifier,@Parameter(in = ParameterIn.DEFAULT, description = "Submodel Descriptor object", required=true, schema=@Schema()) @Valid @RequestBody SubmodelDescriptor body) {
//        String accept = request.getHeader("Accept");
//        if (accept != null && accept.contains("application/json")) {
//            try {
//                return new ResponseEntity<SubmodelDescriptor>(objectMapper.readValue("{\n  \"idShort\" : \"idShort\",\n  \"description\" : [ null, null ]\n}", SubmodelDescriptor.class), HttpStatus.NOT_IMPLEMENTED);
//            } catch (IOException e) {
//                log.error("Couldn't serialize response for content type application/json", e);
//                return new ResponseEntity<SubmodelDescriptor>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        }
//
//        return new ResponseEntity<SubmodelDescriptor>(HttpStatus.NOT_IMPLEMENTED);

        if (!service.existsAssetAdministrationShellDescriptorById(aasIdentifier)) {
            return new ResponseEntity<SubmodelDescriptor>(HttpStatus.NOT_FOUND);
        }

        if (!service.existsSubmodelDescriptorById(aasIdentifier, body.getIdentification().getId())) {
            return new ResponseEntity<SubmodelDescriptor>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        var result = service.registerSubmodelDescriptor(aasIdentifier, body);
        if (result != null) {
            return new ResponseEntity<SubmodelDescriptor>(result, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<SubmodelDescriptor>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public ResponseEntity<Void> putAssetAdministrationShellDescriptorById(@Parameter(in = ParameterIn.PATH, description = "The Asset Administration Shell’s unique id (BASE64-URL-encoded)", required=true, schema=@Schema()) @PathVariable("aasIdentifier") String aasIdentifier,@Parameter(in = ParameterIn.DEFAULT, description = "Asset Administration Shell Descriptor object", required=true, schema=@Schema()) @Valid @RequestBody AssetAdministrationShellDescriptor body) {
        //String accept = request.getHeader("Accept");
        //return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);

        //fixme: why is the return type different from the postAssetAdministrationShellDescriptor method?

        if (!service.existsAssetAdministrationShellDescriptorById(aasIdentifier)) {
            return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
        }

        //fixme: what if the IdType differs?
        body.getIdentification().setId(aasIdentifier);

        var result = service.registerAssetAdministrationShellDescriptor(body);
        if (result != null) {
            return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Void> putSubmodelDescriptorById(@Parameter(in = ParameterIn.PATH, description = "The Asset Administration Shell’s unique id (BASE64-URL-encoded)", required=true, schema=@Schema()) @PathVariable("aasIdentifier") String aasIdentifier,@Parameter(in = ParameterIn.PATH, description = "The Submodel’s unique id (BASE64-URL-encoded)", required=true, schema=@Schema()) @PathVariable("submodelIdentifier") String submodelIdentifier,@Parameter(in = ParameterIn.DEFAULT, description = "Submodel Descriptor object", required=true, schema=@Schema()) @Valid @RequestBody SubmodelDescriptor body) {
        //String accept = request.getHeader("Accept");
        //return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);

        //fixme: why is the return type different from the postSubmodelDescriptor method?

        if (!service.existsAssetAdministrationShellDescriptorById(aasIdentifier)) {
            return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
        }

        //fixme: what if the IdType differs?
        body.getIdentification().setId(submodelIdentifier);

        var result = service.registerSubmodelDescriptor(aasIdentifier, body);
        if (result != null) {
            return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
