package org.eclipse.digitaltwin.basyx.aasregistry.service.tests;

import static org.junit.Assert.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.digitaltwin.basyx.aasregistry.events.RegistryEventSink;
import org.eclipse.digitaltwin.basyx.aasregistry.service.api.BasyxDescriptionApiDelegate;
import org.eclipse.digitaltwin.basyx.aasregistry.service.api.BasyxDescriptionApiDelegate.ProfileNotFoundException;
import org.eclipse.digitaltwin.basyx.aasregistry.service.api.DescriptionApiController;
import org.eclipse.digitaltwin.basyx.aasregistry.service.api.DescriptionApiDelegate;
import org.eclipse.digitaltwin.basyx.aasregistry.service.storage.AasRegistryStorage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(DescriptionApiController.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = { "description.profiles=RegistryServiceSpecification/V3.0,RegistryServiceSpecification/V3.0-AssetAdministrationShellRegistry,RegistryServiceSpecification/V3.0-SubmodelRegistry" })
public class DescriptionProfilesTest {

	@MockBean
	public AasRegistryStorage storage;

	@MockBean
	public RegistryEventSink eventSink;

	@Autowired
	public DescriptionApiDelegate delegate;
	
	@Autowired
	private MockMvc mvc;
	
	@Test
	public void whenGetDescription_ThenSuccess() throws Exception {
		this.mvc.perform(get("/description").accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(content().contentType(MediaType.APPLICATION_JSON))
		.andExpect(jsonPath("$.profiles").value(getDefinedProfiles()));
	}

	@Test
	public void whenWrongConfiguration_FailedToMapToEnum() {
		assertThrows(ProfileNotFoundException.class,
				() -> ((BasyxDescriptionApiDelegate) delegate).setValues(new String[] { "Unknown-Value" }));
	}

	private List<String> getDefinedProfiles() {
		TestPropertySource src = DescriptionProfilesTest.class.getAnnotation(TestPropertySource.class);
		String profilesDef = src.properties()[0];
		String[] definedProfiles = profilesDef.split("=")[1].split(",");
		return Arrays.stream(definedProfiles).collect(Collectors.toList());
	}
}