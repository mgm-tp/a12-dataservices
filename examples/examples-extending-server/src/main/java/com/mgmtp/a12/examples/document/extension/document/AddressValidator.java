/*
 * SPDX-License-Identifier: EUPL-1.2 OR LicenseRef-commercial
 *
 * Copyright (c) 2012-2026 mgm technology partners GmbH
 *
 * Dual License
 * ------------
 * This source file is part of the mgm A12 Platform and available under
 * a choice of two different licenses:
 *
 * 1. Open-Source License – EUPL v1.2
 *    You may redistribute and/or modify this file under the terms of the
 *    European Union Public License, version 1.2 - see https://eupl.eu/.
 *
 * 2. Commercial License
 *    Alternatively, you may obtain a commercial license from
 *    mgm technology partners GmbH, that permits use of this software
 *    under different terms (including support and maintenance services).
 *
 *    Please contact a12-license@mgm-tp.com for more information.
 *
 * You must select and comply with exactly one of the above license options.
 *
 * Warranty Disclaimer (applies to either option)
 * ----------------------------------------------
 * THIS SOFTWARE IS PROVIDED “AS IS” AND WITHOUT WARRANTY OF ANY KIND,
 * WHETHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT, EXCEPT WHERE SUCH DISCLAIMERS ARE HELD TO BE
 * LEGALLY INVALID. SEE THE RESPECTIVE LICENSE TEXT FOR DETAILS.
 */
package com.mgmtp.a12.examples.document.extension.document;

import java.util.regex.Pattern;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.examples.custom.exception.InvalidAddressException;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Validates address fields for documents of model `ContactModel`.
 * Implements simple format checks and throws {@link com.mgmtp.a12.examples.custom.exception.InvalidAddressException} on violations.
 */
@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples.documents.extension.document", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Component public class AddressValidator {

	/**
	 * Checker for input document that has to determine if document is of `ContactModel` model name. In case of true,
	 * validation should validate address by calling method implementing validation logic (in our case mocked validation).
	 * If validation fails custom exception is thrown affecting the response.
	 *
	 * @param document the document to validate; never null.
	 * @throws com.mgmtp.a12.examples.custom.exception.InvalidAddressException if any validation rule is violated.
	 */
	public void validatedAddress(DocumentV2 document) {
		if ("ContactModel".equals(document.getDocumentModelId())) {
			Address address = new Address(document);
			// new YourFavoriteAddressValidation().validate(address);
			checkAddress(address);
		}
	}

	/**
	 * Method for executing address validation (mocked or API call). This part could be adapted to utilize 3rd party address validation.
	 *
	 * Our example case implements several mocked validations for address fields.
	 */
	private void checkAddress(Address address) {
		checkForNumber(address.getCity());
		checkForNumber(address.getCountry());
		validateStartingLetter(address.getCountry().toLowerCase());
		validateStartingLetter(address.getStreet().toLowerCase());
		validateZipCode(address.getZip());
	}

	/**
	 * Validation to prevent user intentionally putting number in city or country field
	 *
	 * Valid case: "London", "Macedonia"
	 * Invalid case: "L0nd0n", "Mac3d0nia"
	 */
	private void checkForNumber(String value) {
		if (Pattern.compile("\\d").matcher(value).find()) {
			throw new InvalidAddressException(ExceptionKeys.DOCUMENT_VALIDATION_ERROR_KEY, "City or Country field should not contain number!");
		}
	}

	/**
	 * Validation to prevent user using words starting with specified letter
	 *
	 * Valid case: "Poland", "Czechia"
	 * Invalid case: "Belgium", "England"
	 */
	private void validateStartingLetter(String value) {
		if (value.startsWith("s") || value.startsWith("t") || value.startsWith("e") || value.startsWith("b")) {
			throw new InvalidAddressException(ExceptionKeys.DOCUMENT_VALIDATION_ERROR_KEY, "Country starts with forbidden letter!");
		}
	}

	/**
	 * Validation for correct ZIP code format
	 *
	 * Valid case: "123 456", "123-456", "123456"
	 * Invalid case: "1234 4567", "England"
	 */
	private void validateZipCode(String value) {
		if (!Pattern.compile("^[\\w\\d]{2,3}([-]|\\s)?([\\w\\d]{2,3})?$").matcher(value).find()) {
			throw new InvalidAddressException(ExceptionKeys.DOCUMENT_VALIDATION_ERROR_KEY, "Invalid ZIP code provided!");
		}
	}

	@Data
	private class Address {
		private String street;
		private String zip;
		private String city;
		private String country;

		public Address(DocumentV2 document) {
			String addressPath = "/Contact/Address";
			this.zip = resolveString(document, addressPath + "/ZipCode");
			this.street = resolveString(document, addressPath + "/Street");
			this.country = resolveString(document, addressPath + "/Country");
			this.city = resolveString(document, addressPath + "/City");
		}

		private String resolveString(DocumentV2 document, String path) {
			Object value =  document.fieldValue(path);
			if (value != null) {
				return String.valueOf(value);
			} else {
				throw new IllegalStateException(String.format("There is no value for %s", path));
			}
		}

	}

}

