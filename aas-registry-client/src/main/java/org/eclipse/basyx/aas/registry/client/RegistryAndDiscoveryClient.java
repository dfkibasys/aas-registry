package org.eclipse.basyx.aas.registry.client;

import org.eclipse.basyx.aas.registry.client.api.RegistryAndDiscoveryInterfaceApi;
import org.eclipse.basyx.aas.registry.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistryAndDiscoveryClient {
    private ApiClient apiClient;

    private RegistryAndDiscoveryInterfaceApi api;

    public RegistryAndDiscoveryClient(String endpoint) {
        apiClient = new ApiClient();
        apiClient.setBasePath(endpoint);
        api = new RegistryAndDiscoveryInterfaceApi(apiClient);
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    /**
     * Deletes all Asset identifier key-value-pair linked to an Asset Administration Shell to edit discoverable content
     * 
     * <p><b>204</b> - Asset identifier key-value-pairs deleted successfully
     * @param aasIdentifier The Asset Administration Shell’s unique id (BASE64-URL-encoded) (required)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void deleteAllAssetLinksById(String aasIdentifier) throws RestClientException {
        deleteAllAssetLinksByIdWithHttpInfo(aasIdentifier);
    }

    /**
     * Deletes all Asset identifier key-value-pair linked to an Asset Administration Shell to edit discoverable content
     * 
     * <p><b>204</b> - Asset identifier key-value-pairs deleted successfully
     * @param aasIdentifier The Asset Administration Shell’s unique id (BASE64-URL-encoded) (required)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> deleteAllAssetLinksByIdWithHttpInfo(String aasIdentifier) throws RestClientException {
        var encodedAasIdentifier = encodeId(aasIdentifier);
        return api.deleteAllAssetLinksByIdWithHttpInfo(encodedAasIdentifier);
    }
    /**
     * Deletes an Asset Administration Shell Descriptor, i.e. de-registers an AAS
     * 
     * <p><b>204</b> - Asset Administration Shell Descriptor deleted successfully
     * @param aasIdentifier The Asset Administration Shell’s unique id (BASE64-URL-encoded) (required)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void deleteAssetAdministrationShellDescriptorById(String aasIdentifier) throws RestClientException {
        deleteAssetAdministrationShellDescriptorByIdWithHttpInfo(aasIdentifier);
    }

    /**
     * Deletes an Asset Administration Shell Descriptor, i.e. de-registers an AAS
     * 
     * <p><b>204</b> - Asset Administration Shell Descriptor deleted successfully
     * @param aasIdentifier The Asset Administration Shell’s unique id (BASE64-URL-encoded) (required)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> deleteAssetAdministrationShellDescriptorByIdWithHttpInfo(String aasIdentifier) throws RestClientException {
        var encodedAasIdentifier = encodeId(aasIdentifier);
        return api.deleteAssetAdministrationShellDescriptorByIdWithHttpInfo(encodedAasIdentifier);
    }
    /**
     * Deletes a Submodel Descriptor, i.e. de-registers a submodel
     * 
     * <p><b>204</b> - Submodel Descriptor deleted successfully
     * @param aasIdentifier The Asset Administration Shell’s unique id (BASE64-URL-encoded) (required)
     * @param submodelIdentifier The Submodel’s unique id (BASE64-URL-encoded) (required)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void deleteSubmodelDescriptorById(String aasIdentifier, String submodelIdentifier) throws RestClientException {
        deleteSubmodelDescriptorByIdWithHttpInfo(aasIdentifier, submodelIdentifier);
    }

    /**
     * Deletes a Submodel Descriptor, i.e. de-registers a submodel
     * 
     * <p><b>204</b> - Submodel Descriptor deleted successfully
     * @param aasIdentifier The Asset Administration Shell’s unique id (BASE64-URL-encoded) (required)
     * @param submodelIdentifier The Submodel’s unique id (BASE64-URL-encoded) (required)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> deleteSubmodelDescriptorByIdWithHttpInfo(String aasIdentifier, String submodelIdentifier) throws RestClientException {
        var encodedAasIdentifier = encodeId(aasIdentifier);
        var encodedSubmodelIdentifier = encodeId(submodelIdentifier);
        return api.deleteSubmodelDescriptorByIdWithHttpInfo(encodedAasIdentifier, encodedSubmodelIdentifier);
    }
    /**
     * Returns all Asset Administration Shell Descriptors
     * 
     * <p><b>200</b> - Requested Asset Administration Shell Descriptors
     * @return List&lt;AssetAdministrationShellDescriptor&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public List<AssetAdministrationShellDescriptor> getAllAssetAdministrationShellDescriptors() throws RestClientException {
        return getAllAssetAdministrationShellDescriptorsWithHttpInfo().getBody();
    }

    /**
     * Returns all Asset Administration Shell Descriptors
     * 
     * <p><b>200</b> - Requested Asset Administration Shell Descriptors
     * @return ResponseEntity&lt;List&lt;AssetAdministrationShellDescriptor&gt;&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<List<AssetAdministrationShellDescriptor>> getAllAssetAdministrationShellDescriptorsWithHttpInfo() throws RestClientException {
        return api.getAllAssetAdministrationShellDescriptorsWithHttpInfo();
    }
    /**
     * Returns a list of Asset Administration Shell ids based on Asset identifier key-value-pairs
     * 
     * <p><b>200</b> - Requested Asset Administration Shell ids
     * @param assetIds The key-value-pair of an Asset identifier (optional)
     * @return List&lt;String&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public List<String> getAllAssetAdministrationShellIdsByAssetLink(List<IdentifierKeyValuePair> assetIds) throws RestClientException {
        return getAllAssetAdministrationShellIdsByAssetLinkWithHttpInfo(assetIds).getBody();
    }

    /**
     * Returns a list of Asset Administration Shell ids based on Asset identifier key-value-pairs
     * 
     * <p><b>200</b> - Requested Asset Administration Shell ids
     * @param assetIds The key-value-pair of an Asset identifier (optional)
     * @return ResponseEntity&lt;List&lt;String&gt;&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<List<String>> getAllAssetAdministrationShellIdsByAssetLinkWithHttpInfo(List<IdentifierKeyValuePair> assetIds) throws RestClientException {
        return api.getAllAssetAdministrationShellIdsByAssetLinkWithHttpInfo(assetIds);
    }
    /**
     * Returns a list of Asset identifier key-value-pairs based on an Asset Administration Shell id to edit discoverable content
     * 
     * <p><b>200</b> - Requested Asset identifier key-value-pairs
     * @param aasIdentifier The Asset Administration Shell’s unique id (BASE64-URL-encoded) (required)
     * @return List&lt;IdentifierKeyValuePair&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public List<IdentifierKeyValuePair> getAllAssetLinksById(String aasIdentifier) throws RestClientException {
        return getAllAssetLinksByIdWithHttpInfo(aasIdentifier).getBody();
    }

    /**
     * Returns a list of Asset identifier key-value-pairs based on an Asset Administration Shell id to edit discoverable content
     * 
     * <p><b>200</b> - Requested Asset identifier key-value-pairs
     * @param aasIdentifier The Asset Administration Shell’s unique id (BASE64-URL-encoded) (required)
     * @return ResponseEntity&lt;List&lt;IdentifierKeyValuePair&gt;&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<List<IdentifierKeyValuePair>> getAllAssetLinksByIdWithHttpInfo(String aasIdentifier) throws RestClientException {
        var encodedAasIdentifier = encodeId(aasIdentifier);
        return api.getAllAssetLinksByIdWithHttpInfo(encodedAasIdentifier);
    }
    /**
     * Returns all Submodel Descriptors
     * 
     * <p><b>200</b> - Requested Submodel Descriptors
     * @param aasIdentifier The Asset Administration Shell’s unique id (BASE64-URL-encoded) (required)
     * @return List&lt;SubmodelDescriptor&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public List<SubmodelDescriptor> getAllSubmodelDescriptors(String aasIdentifier) throws RestClientException {
        return getAllSubmodelDescriptorsWithHttpInfo(aasIdentifier).getBody();
    }

    /**
     * Returns all Submodel Descriptors
     * 
     * <p><b>200</b> - Requested Submodel Descriptors
     * @param aasIdentifier The Asset Administration Shell’s unique id (BASE64-URL-encoded) (required)
     * @return ResponseEntity&lt;List&lt;SubmodelDescriptor&gt;&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<List<SubmodelDescriptor>> getAllSubmodelDescriptorsWithHttpInfo(String aasIdentifier) throws RestClientException {
        var encodedAasIdentifier = encodeId(aasIdentifier);
        return api.getAllSubmodelDescriptorsWithHttpInfo(encodedAasIdentifier);
    }
    /**
     * Returns a specific Asset Administration Shell Descriptor
     * 
     * <p><b>200</b> - Requested Asset Administration Shell Descriptor
     * @param aasIdentifier The Asset Administration Shell’s unique id (BASE64-URL-encoded) (required)
     * @return AssetAdministrationShellDescriptor
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptorById(String aasIdentifier) throws RestClientException {
        return getAssetAdministrationShellDescriptorByIdWithHttpInfo(aasIdentifier).getBody();
    }

    /**
     * Returns a specific Asset Administration Shell Descriptor
     * 
     * <p><b>200</b> - Requested Asset Administration Shell Descriptor
     * @param aasIdentifier The Asset Administration Shell’s unique id (BASE64-URL-encoded) (required)
     * @return ResponseEntity&lt;AssetAdministrationShellDescriptor&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<AssetAdministrationShellDescriptor> getAssetAdministrationShellDescriptorByIdWithHttpInfo(String aasIdentifier) throws RestClientException {
        var encodedAasIdentifier = encodeId(aasIdentifier);
        return api.getAssetAdministrationShellDescriptorByIdWithHttpInfo(encodedAasIdentifier);
    }
    /**
     * Returns a specific Submodel Descriptor
     * 
     * <p><b>200</b> - Requested Submodel Descriptor
     * @param aasIdentifier The Asset Administration Shell’s unique id (BASE64-URL-encoded) (required)
     * @param submodelIdentifier The Submodel’s unique id (BASE64-URL-encoded) (required)
     * @return SubmodelDescriptor
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public SubmodelDescriptor getSubmodelDescriptorById(String aasIdentifier, String submodelIdentifier) throws RestClientException {
        return getSubmodelDescriptorByIdWithHttpInfo(aasIdentifier, submodelIdentifier).getBody();
    }

    /**
     * Returns a specific Submodel Descriptor
     * 
     * <p><b>200</b> - Requested Submodel Descriptor
     * @param aasIdentifier The Asset Administration Shell’s unique id (BASE64-URL-encoded) (required)
     * @param submodelIdentifier The Submodel’s unique id (BASE64-URL-encoded) (required)
     * @return ResponseEntity&lt;SubmodelDescriptor&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<SubmodelDescriptor> getSubmodelDescriptorByIdWithHttpInfo(String aasIdentifier, String submodelIdentifier) throws RestClientException {
        var encodedAasIdentifier = encodeId(aasIdentifier);
        var encodedSubmodelIdentifier = encodeId(submodelIdentifier);
        return api.getSubmodelDescriptorByIdWithHttpInfo(encodedAasIdentifier, encodedSubmodelIdentifier);
    }
    /**
     * Creates all Asset identifier key-value-pair linked to an Asset Administration Shell to edit discoverable content
     * 
     * <p><b>201</b> - Asset identifier key-value-pairs created successfully
     * @param body Asset identifier key-value-pairs (required)
     * @param aasIdentifier The Asset Administration Shell’s unique id (BASE64-URL-encoded) (required)
     * @return List&lt;IdentifierKeyValuePair&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public List<IdentifierKeyValuePair> postAllAssetLinksById(List<IdentifierKeyValuePair> body, String aasIdentifier) throws RestClientException {
        return postAllAssetLinksByIdWithHttpInfo(body, aasIdentifier).getBody();
    }

    /**
     * Creates all Asset identifier key-value-pair linked to an Asset Administration Shell to edit discoverable content
     * 
     * <p><b>201</b> - Asset identifier key-value-pairs created successfully
     * @param body Asset identifier key-value-pairs (required)
     * @param aasIdentifier The Asset Administration Shell’s unique id (BASE64-URL-encoded) (required)
     * @return ResponseEntity&lt;List&lt;IdentifierKeyValuePair&gt;&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<List<IdentifierKeyValuePair>> postAllAssetLinksByIdWithHttpInfo(List<IdentifierKeyValuePair> body, String aasIdentifier) throws RestClientException {
        var encodedAasIdentifier = encodeId(aasIdentifier);
        return api.postAllAssetLinksByIdWithHttpInfo(body, encodedAasIdentifier);
    }
    /**
     * Creates a new Asset Administration Shell Descriptor, i.e. registers an AAS
     * 
     * <p><b>201</b> - Asset Administration Shell Descriptor created successfully
     * @param body Asset Administration Shell Descriptor object (required)
     * @return AssetAdministrationShellDescriptor
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public AssetAdministrationShellDescriptor postAssetAdministrationShellDescriptor(AssetAdministrationShellDescriptor body) throws RestClientException {
        return postAssetAdministrationShellDescriptorWithHttpInfo(body).getBody();
    }

    /**
     * Creates a new Asset Administration Shell Descriptor, i.e. registers an AAS
     * 
     * <p><b>201</b> - Asset Administration Shell Descriptor created successfully
     * @param body Asset Administration Shell Descriptor object (required)
     * @return ResponseEntity&lt;AssetAdministrationShellDescriptor&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<AssetAdministrationShellDescriptor> postAssetAdministrationShellDescriptorWithHttpInfo(AssetAdministrationShellDescriptor body) throws RestClientException {
        return api.postAssetAdministrationShellDescriptorWithHttpInfo(body);
    }
    /**
     * Creates a new Submodel Descriptor, i.e. registers a submodel
     * 
     * <p><b>201</b> - Submodel Descriptor created successfully
     * @param body Submodel Descriptor object (required)
     * @param aasIdentifier The Asset Administration Shell’s unique id (BASE64-URL-encoded) (required)
     * @return SubmodelDescriptor
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public SubmodelDescriptor postSubmodelDescriptor(SubmodelDescriptor body, String aasIdentifier) throws RestClientException {
        return postSubmodelDescriptorWithHttpInfo(body, aasIdentifier).getBody();
    }

    /**
     * Creates a new Submodel Descriptor, i.e. registers a submodel
     * 
     * <p><b>201</b> - Submodel Descriptor created successfully
     * @param body Submodel Descriptor object (required)
     * @param aasIdentifier The Asset Administration Shell’s unique id (BASE64-URL-encoded) (required)
     * @return ResponseEntity&lt;SubmodelDescriptor&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<SubmodelDescriptor> postSubmodelDescriptorWithHttpInfo(SubmodelDescriptor body, String aasIdentifier) throws RestClientException {
        var encodedAasIdentifier = encodeId(aasIdentifier);
        return api.postSubmodelDescriptorWithHttpInfo(body, aasIdentifier);
    }
    /**
     * Updates an existing Asset Administration Shell Descriptor
     * 
     * <p><b>204</b> - Asset Administration Shell Descriptor updated successfully
     * @param body Asset Administration Shell Descriptor object (required)
     * @param aasIdentifier The Asset Administration Shell’s unique id (BASE64-URL-encoded) (required)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void putAssetAdministrationShellDescriptorById(AssetAdministrationShellDescriptor body, String aasIdentifier) throws RestClientException {
        putAssetAdministrationShellDescriptorByIdWithHttpInfo(body, aasIdentifier);
    }

    /**
     * Updates an existing Asset Administration Shell Descriptor
     * 
     * <p><b>204</b> - Asset Administration Shell Descriptor updated successfully
     * @param body Asset Administration Shell Descriptor object (required)
     * @param aasIdentifier The Asset Administration Shell’s unique id (BASE64-URL-encoded) (required)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> putAssetAdministrationShellDescriptorByIdWithHttpInfo(AssetAdministrationShellDescriptor body, String aasIdentifier) throws RestClientException {
        var encodedAasIdentifier = encodeId(aasIdentifier);
        return api.putAssetAdministrationShellDescriptorByIdWithHttpInfo(body, aasIdentifier);
    }
    /**
     * Updates an existing Submodel Descriptor
     * 
     * <p><b>204</b> - Submodel Descriptor updated successfully
     * @param body Submodel Descriptor object (required)
     * @param aasIdentifier The Asset Administration Shell’s unique id (BASE64-URL-encoded) (required)
     * @param submodelIdentifier The Submodel’s unique id (BASE64-URL-encoded) (required)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void putSubmodelDescriptorById(SubmodelDescriptor body, String aasIdentifier, String submodelIdentifier) throws RestClientException {
        putSubmodelDescriptorByIdWithHttpInfo(body, aasIdentifier, submodelIdentifier);
    }

    /**
     * Updates an existing Submodel Descriptor
     * 
     * <p><b>204</b> - Submodel Descriptor updated successfully
     * @param body Submodel Descriptor object (required)
     * @param aasIdentifier The Asset Administration Shell’s unique id (BASE64-URL-encoded) (required)
     * @param submodelIdentifier The Submodel’s unique id (BASE64-URL-encoded) (required)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> putSubmodelDescriptorByIdWithHttpInfo(SubmodelDescriptor body, String aasIdentifier, String submodelIdentifier) throws RestClientException {
        var encodedAasIdentifier = encodeId(aasIdentifier);
        var encodedSubmodelIdentifier = encodeId(submodelIdentifier);
        return api.putSubmodelDescriptorByIdWithHttpInfo(body, encodedAasIdentifier, encodedSubmodelIdentifier);
    }
    /**
     * 
     * 
     * <p><b>200</b> - Search response
     * @param body  (required)
     * @return ShellDescriptorSearchResponse
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ShellDescriptorSearchResponse searchShellDescriptors(ShellDescriptorSearchQuery body) throws RestClientException {
        return searchShellDescriptorsWithHttpInfo(body).getBody();
    }

    /**
     * 
     * 
     * <p><b>200</b> - Search response
     * @param body  (required)
     * @return ResponseEntity&lt;ShellDescriptorSearchResponse&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<ShellDescriptorSearchResponse> searchShellDescriptorsWithHttpInfo(ShellDescriptorSearchQuery body) throws RestClientException {
        return api.searchShellDescriptorsWithHttpInfo(body);
    }

    private String encodeId(String id) {
        return Base64.getUrlEncoder().encodeToString(id.getBytes(StandardCharsets.UTF_8));
    }
    
}
