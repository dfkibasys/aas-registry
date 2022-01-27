package org.eclipse.basys.aas.registry.client.api;

import static org.junit.Assert.assertTrue;

import org.eclipse.basyx.aas.registry.model.AssetAdministrationShellDescriptor;
import org.eclipse.basyx.aas.registry.model.Key;
import org.eclipse.basyx.aas.registry.model.KeyElements;
import org.eclipse.basyx.aas.registry.model.ModelReference;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;

public class DescriminatorTest {

	private static ObjectMapper MAPPER = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
	private static ObjectWriter WRITER = MAPPER.writerWithDefaultPrettyPrinter();
	private static ObjectReader READER = MAPPER.readerFor(AssetAdministrationShellDescriptor.class);

	@Test
	public void testDeserializingWithDescriminatorIsSuccessful() throws JsonMappingException, JsonProcessingException {
		AssetAdministrationShellDescriptor descriptor = new AssetAdministrationShellDescriptor();
		descriptor.setIdentification("test");
		Key key = new Key().type(KeyElements.PROPERTY).value("test");
		descriptor.setGlobalAssetId(new ModelReference().addKeysItem(key));

		String serialized = WRITER.writeValueAsString(descriptor);
		// read successful
		READER.readValue(serialized);
		// because the serialized string contains the reference type
		// so that we can deserialize
		assertTrue(serialized.contains("refType\" : \"ModelReference"));
	}

	@Test
	public void testJsonMappingNeedsRefTypeProperty() throws JsonMappingException, JsonProcessingException {
		String globalRefDescr = "{ \"identification\" : \"test\",  \"globalAssetId\" : { \"refType\" : \"GlobalReference\",  \"value\" : [] }}";
		AssetAdministrationShellDescriptor descr = READER.readValue(globalRefDescr);
		Assert.assertEquals("test", descr.getIdentification());

		// This is the reason why we need the descriminator with a type property
		String modelRefDescr = "{ \"identification\" : \"test\",  \"globalAssetId\" : {   \"keys\" : [] }}";
		Assert.assertThrows(InvalidTypeIdException.class, () -> READER.readValue(modelRefDescr));
	}

}
