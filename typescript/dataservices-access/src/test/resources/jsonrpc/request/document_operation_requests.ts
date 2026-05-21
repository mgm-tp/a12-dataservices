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
const addDocumentRequest = {
	jsonrpc: "2.0",
	id: "AddDocument",
	method: "ADD_DOCUMENT",
	params: {
		document: {
			ContractRoot: {
				ContractName: "Contract1",
				ContractValue: 1
			}
		},
		documentModelName: "Contract1",
		locale: "de"
	}
};

const copyDocumentRequest = {
	jsonrpc: "2.0",
	id: "CopyDocument",
	method: "COPY_DOCUMENT",
	params: {
		docRef: "Contract1/1",
		locale: "de"
	}
};

const modifyDocumentRequest = {
	jsonrpc: "2.0",
	id: "ModifyDocument",
	method: "MODIFY_DOCUMENT",
	params: {
		docRef: "Contract/1",
		document: {
			ContractRoot: {
				ContractName: "Contract1",
				ContractValue: 1
			}
		},
		locale: "de"
	}
};

const partialModifyDocumentRequest = {
	jsonrpc: "2.0",
	method: "PARTIAL_MODIFY_DOCUMENT",
	id: "PartialDocumentModify",
	params: {
		docRef: "Contract/1",
		documentPart: [
			{
				path: "/ContractRoot/ContractName",
				repetitions: [1, 1],
				value: "Contract name"
			},
			{
				path: "/ContractRoot/ContractValue",
				repetitions: [1, 1],
				value: "1000"
			}
		],
		locale: "de"
	}
};

const deleteDocumentRequest = {
	jsonrpc: "2.0",
	id: "DeleteDocument",
	method: "DELETE_DOCUMENT",
	params: {
		docRef: "Contract/1",
		locale: "de"
	}
};

const multiDeleteDocumentRequest = {
	jsonrpc: "2.0",
	method: "MULTI_DELETE_DOCUMENTS",
	id: "MultiDocumentDelete",
	params: {
		docRefs: ["Contract/1", "Contract/2", "Contract/3"]
	}
};

const getDocumentRequest = {
	jsonrpc: "2.0",
	method: "GET_DOCUMENT",
	id: "GetDocument",
	params: {
		docRef: "Contract/1"
	}
};

const validateDocumentRequest = {
	jsonrpc: "2.0",
	id: "ValidateDocument",
	method: "VALIDATE_DOCUMENT",
	params: {
		document: {
			ContractRoot: {
				ContractName: "Contract name",
				ContractValue: 100
			}
		},
		documentModelName: "Contract",
		locale: "de"
	}
};

export {
	addDocumentRequest,
	copyDocumentRequest,
	modifyDocumentRequest,
	partialModifyDocumentRequest,
	deleteDocumentRequest,
	multiDeleteDocumentRequest,
	getDocumentRequest,
	validateDocumentRequest
};
