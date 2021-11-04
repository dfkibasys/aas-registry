package org.eclipse.basyx.aas.registry.api;

import org.eclipse.basyx.aas.registry.model.Identifier;
import org.eclipse.basyx.aas.registry.model.IdentifierKeyValuePair;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-03T09:35:10.988Z[GMT]")
@RestController
public class LookupApiController implements LookupApi {

    private static final Logger log = LoggerFactory.getLogger(LookupApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    @org.springframework.beans.factory.annotation.Autowired
    public LookupApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

    public ResponseEntity<Void> deleteAllAssetLinksById(@Parameter(in = ParameterIn.PATH, description = "The Asset Administration Shell’s unique id (BASE64-URL-encoded)", required=true, schema=@Schema()) @PathVariable("aasIdentifier") String aasIdentifier) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<List<Identifier>> getAllAssetAdministrationShellIdsByAssetLink(@Parameter(in = ParameterIn.QUERY, description = "The key-value-pair of an Asset identifier" ,schema=@Schema()) @Valid @RequestParam(value = "assetIds", required = false) List<IdentifierKeyValuePair> assetIds) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<List<Identifier>>(objectMapper.readValue("[ {\n  \"id\" : \"id\"\n}, {\n  \"id\" : \"id\"\n} ]", List.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<List<Identifier>>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<List<Identifier>>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<List<IdentifierKeyValuePair>> getAllAssetLinksById(@Parameter(in = ParameterIn.PATH, description = "The Asset Administration Shell’s unique id (BASE64-URL-encoded)", required=true, schema=@Schema()) @PathVariable("aasIdentifier") String aasIdentifier) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<List<IdentifierKeyValuePair>>(objectMapper.readValue("[ \"\", \"\" ]", List.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<List<IdentifierKeyValuePair>>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<List<IdentifierKeyValuePair>>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<List<IdentifierKeyValuePair>> postAllAssetLinksById(@Parameter(in = ParameterIn.PATH, description = "The Asset Administration Shell’s unique id (BASE64-URL-encoded)", required=true, schema=@Schema()) @PathVariable("aasIdentifier") String aasIdentifier,@Parameter(in = ParameterIn.DEFAULT, description = "Asset identifier key-value-pairs", required=true, schema=@Schema()) @Valid @RequestBody List<IdentifierKeyValuePair> body) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<List<IdentifierKeyValuePair>>(objectMapper.readValue("[ \"\", \"\" ]", List.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<List<IdentifierKeyValuePair>>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<List<IdentifierKeyValuePair>>(HttpStatus.NOT_IMPLEMENTED);
    }

}
