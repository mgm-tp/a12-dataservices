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
import { strictEqual } from "node:assert/strict";

import { DocumentSpec } from "../Document/index.js";

suite("Document Spec Test", () => {
	const documentSpec = {
		docRef: "docRef",
		documentModelName: "documentModelName",
		document: {
			key: "value"
		}
	};

	test("Document Entry Instance", () => {
		strictEqual(DocumentSpec.isInstance(documentSpec), true, "Check DocumentSpec instance!");
	});

	test("Document Entry Instance - Missing docRef", () => {
		const { docRef, ...documentSpecWithoutDocRef } = documentSpec;
		strictEqual(
			DocumentSpec.isInstance(documentSpecWithoutDocRef),
			false,
			"Check DocumentSpec instance without docRef!"
		);
	});

	test("Document Entry Instance - Missing documentModelName", () => {
		const { documentModelName, ...documentSpecWithoutModelName } = documentSpec;
		strictEqual(
			DocumentSpec.isInstance(documentSpecWithoutModelName),
			false,
			"Check DocumentSpec instance without documentModelName!"
		);
	});

	test("Document Entry Instance - Missing document", () => {
		const { document, ...documentSpecWithoutDocument } = documentSpec;
		strictEqual(
			DocumentSpec.isInstance(documentSpecWithoutDocument),
			false,
			"Check DocumentSpec instance without document!"
		);
	});

	test("Document Entry Instance - Empty document", () => {
		const emptyDocumentSpec = {
			...documentSpec,
			document: {}
		};
		strictEqual(
			DocumentSpec.isInstance(emptyDocumentSpec),
			true,
			"Check DocumentSpec instance with empty document!"
		);
	});

	test("Document Entry Instance - Invalid document", () => {
		const invalidDocumentSpec = {
			...documentSpec,
			document: "not an object"
		};
		strictEqual(
			DocumentSpec.isInstance(invalidDocumentSpec),
			false,
			"Check DocumentSpec instance with invalid document!"
		);
	});
});
