package de.dfki.cos.basys.aas.registry.service.storage;

import java.nio.charset.StandardCharsets;
import java.util.List;

import de.dfki.cos.basys.aas.registry.model.AssetAdministrationShellDescriptor;
import de.dfki.cos.basys.aas.registry.model.SubmodelDescriptor;
import de.dfki.cos.basys.aas.registry.service.errors.AasDescriptorNotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@RequiredArgsConstructor
public class CursorEncodingRegistryStorageDecorator implements AasRegistryStorage {

	@Delegate
	private final AasRegistryStorage storage;

	@Override
	public CursorResult<List<AssetAdministrationShellDescriptor>> getAllAasDescriptors(@NonNull PaginationInfo pRequest, @NonNull DescriptorFilter filter) {
		PaginationInfo decoded = decodeCursor(pRequest);
		CursorResult<List<AssetAdministrationShellDescriptor>> result = storage.getAllAasDescriptors(decoded, filter);
		return encodeCursor(result);
	}

	@Override
	public CursorResult<List<SubmodelDescriptor>> getAllSubmodels(@NonNull String aasDescriptorId, @NonNull PaginationInfo pRequest) throws AasDescriptorNotFoundException {
		PaginationInfo decoded = decodeCursor(pRequest);
		CursorResult<List<SubmodelDescriptor>> result = storage.getAllSubmodels(aasDescriptorId, decoded);
		return encodeCursor(result);
	}

	private <T> CursorResult<T> encodeCursor(CursorResult<T> result) {
		String encodedCursor = encodeCursor(result.getCursor());
		return new CursorResult<>(encodedCursor, result.getResult());
	}
	
	private PaginationInfo decodeCursor(PaginationInfo info) {
		String cursor = decodeCursor(info.getCursor());
		return new PaginationInfo(info.getLimit(), cursor);
	}

	private String decodeCursor(String cursor) {
		if (cursor == null) {
			return null;
		}
		return new String(java.util.Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
	}

	private String encodeCursor(String cursor) {
		if (cursor == null) {
			return null;
		}
		return new String(java.util.Base64.getUrlEncoder().encode(cursor.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);		
	}	
}