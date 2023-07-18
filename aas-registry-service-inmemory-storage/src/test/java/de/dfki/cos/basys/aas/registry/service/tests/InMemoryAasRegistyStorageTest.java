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
package de.dfki.cos.basys.aas.registry.service.tests;

import static org.junit.Assert.assertThrows;

import java.io.IOException;

import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import de.dfki.cos.basys.aas.registry.model.AssetAdministrationShellDescriptor;
import de.dfki.cos.basys.aas.registry.service.configuration.InMemoryAasStorageConfiguration;
import de.dfki.cos.basys.aas.registry.service.storage.AasRegistryStorage;
import de.dfki.cos.basys.aas.registry.service.storage.memory.InMemoryAasRegistryStorage.DuplicateSubmodelIds;

@TestPropertySource(properties = { "registry.type=inMemory" })
@ContextConfiguration(classes = { InMemoryAasRegistyStorageTest.class })
public class InMemoryAasRegistyStorageTest extends AasRegistryStorageTest {

	@Bean
	public AasRegistryStorage createCloningInMemoryStorage() {
		// we save the initial storage state in some testcases
		// so we do not want to alter the object and thus need a deep copy
		return new CloningAasRegistryStorageDecorator(new InMemoryAasStorageConfiguration().storage());
	}
	
	
	@Test
	public void whenInsertDescriptorWithDuplicateSubmodelIds_thenThrowException() throws IOException {
		AssetAdministrationShellDescriptor descr = RegistryTestObjects.newAssetAdministrationShellDescriptor(IDENTIFICATION_NEW);
		descr.addSubmodelDescriptorsItem(RegistryTestObjects.newSubmodelDescriptor(IDENTIFICATION_NEW_1));
		descr.addSubmodelDescriptorsItem(RegistryTestObjects.newSubmodelDescriptor(IDENTIFICATION_NEW_1));
		
		assertThrows(DuplicateSubmodelIds.class, ()->storage.insertAasDescriptor(descr));
		
		verifyNoEventSent();
	}

}