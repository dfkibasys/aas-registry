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
package de.dfki.cos.basys.aas.registry.service.api;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import de.dfki.cos.basys.aas.registry.model.ServiceDescription;
import de.dfki.cos.basys.aas.registry.model.ServiceDescription.ProfilesEnum;

@Component
public class BasyxDescriptionApiDelegate implements DescriptionApiDelegate {

	private ServiceDescription description;

	@Autowired
	public void setValues(@Value("${description.profiles}") String[] profiles) {
		description = new ServiceDescription();
		List<ProfilesEnum> profilesList = new ArrayList<>();
		for (String eachPofile : profiles) {
			ProfilesEnum value = ProfilesEnum.fromValue(eachPofile);
			if (value == null) {
				throw new ProfileNotFoundException(eachPofile);
			}
			profilesList.add(value);
		}
		description.setProfiles(profilesList);
	}

	@Override
	public ResponseEntity<ServiceDescription> getDescription() {
		return new ResponseEntity<>(description, HttpStatus.OK);
	}

	public static class ProfileNotFoundException extends IllegalArgumentException {

		private static final long serialVersionUID = 1L;

		public ProfileNotFoundException(String profile) {
			super("No profile found with name: " + profile);
		}
	}
}