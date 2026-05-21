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

import java.util.List;

import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.utils.internal.DocumentUtils;
import com.mgmtp.a12.examples.custom.exception.InvalidAddressException;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;


@Listeners(MockitoTestNGListener.class)
public class AddressValidatorTest {

	private static final String ZIP_PATH = "/Contact/Address/ZipCode";
	private static final String STREET_PATH = "/Contact/Address/Street";
	private static final String COUNTRY_PATH = "/Contact/Address/Country";
	private static final String CITY_PATH = "/Contact/Address/City";

	private AddressValidator createValidator() {
		return new AddressValidator();
	}

	@Test
	public void validatedAddress_validAddress_passes() {
		AddressValidator validator = createValidator();
		DocumentV2 document = mockDocument("ContactModel",
			"123456",
			"Oak Avenue",
			"Poland",
			"London");
		validator.validatedAddress(document);
		// No exception expected
		Assert.assertTrue(true);
	}

	@Test(expectedExceptions = InvalidAddressException.class,
		expectedExceptionsMessageRegExp = "City or Country field should not contain number!")
	public void validatedAddress_cityContainsNumber_throwsInvalidAddressException() {
		AddressValidator validator = createValidator();
		DocumentV2 document = mockDocument("ContactModel",
			"123456",
			"Oak Avenue",
			"Poland",
			"L0nd0n");
		validator.validatedAddress(document);
	}

	@Test(expectedExceptions = InvalidAddressException.class,
		expectedExceptionsMessageRegExp = "Country starts with forbidden letter!")
	public void validatedAddress_countryStartsWithForbiddenLetter_throwsInvalidAddressException() {
		AddressValidator validator = createValidator();
		DocumentV2 document = mockDocument("ContactModel",
			"123456",
			"Oak Avenue",
			"Belgium",
			"Paris");
		validator.validatedAddress(document);
	}

	@Test(expectedExceptions = InvalidAddressException.class,
		expectedExceptionsMessageRegExp = "Country starts with forbidden letter!")
	public void validatedAddress_streetStartsWithForbiddenLetter_throwsInvalidAddressException() {
		AddressValidator validator = createValidator();
		DocumentV2 document = mockDocument("ContactModel",
			"123456",
			"street road",
			"Poland",
			"Paris");
		validator.validatedAddress(document);
	}

	@Test(expectedExceptions = InvalidAddressException.class,
		expectedExceptionsMessageRegExp = "Invalid ZIP code provided!")
	public void validatedAddress_invalidZip_throwsInvalidAddressException() {
		AddressValidator validator = createValidator();
		DocumentV2 document = mockDocument("ContactModel",
			"12 34567",
			"Oak Avenue",
			"Poland",
			"Paris");
		validator.validatedAddress(document);
	}

	@Test(expectedExceptions = IllegalStateException.class,
		expectedExceptionsMessageRegExp = "There is no value for /Contact/Address/City")
	public void validatedAddress_missingCityValue_throwsIllegalStateException() {
		AddressValidator validator = createValidator();
		DocumentV2 document = mockDocument("ContactModel",
			"123456",
			"Oak Avenue",
			"Poland",
			null); // City missing
		validator.validatedAddress(document);
	}

	private DocumentV2 mockDocument(String modelId,
		String zip,
		String street,
		String country,
		String city) {
		DocumentV2 document = DocumentV2.empty(modelId);

		document = document.withBatchUpdates(
			List.of(
				DocumentUtils.createFieldUpdateAction(ZIP_PATH, new int[] { 1, 1, 1 }, zip),
				DocumentUtils.createFieldUpdateAction(STREET_PATH, new int[] { 1, 1, 1 }, street),
				DocumentUtils.createFieldUpdateAction(COUNTRY_PATH, new int[] { 1, 1, 1 }, country),
				DocumentUtils.createFieldUpdateAction(CITY_PATH, new int[] { 1, 1, 1 }, city)
			)
		);
		return document;
	}
}
