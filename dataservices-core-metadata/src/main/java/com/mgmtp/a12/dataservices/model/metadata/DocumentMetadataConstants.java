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
package com.mgmtp.a12.dataservices.model.metadata;

/**
 * All metadata constants for documents are stored here.
 *
 */
public interface DocumentMetadataConstants {
	/**
	 * Group name used to hold document-level metadata.
	 */
	String DOCUMENT_METADATA_GROUP_NAME = "__meta";
	/**
	 * Separator used in document metadata paths.
	 */
	String DOCUMENT_METADATA_PATH_SEPARATOR = "/";
	/**
	 * Absolute path to the document metadata group starting at the model root.
	 */
	String DOCUMENT_METADATA_GROUP_PATH = DOCUMENT_METADATA_PATH_SEPARATOR + DOCUMENT_METADATA_GROUP_NAME;
	/**
	 * Key for the timestamp when the document was last modified.
	 */
	String MODIFIED_AT_METADATA_NAME = "modifiedAt";
	/**
	 * Absolute path to the modified-at metadata field.
	 */
	String MODIFIED_AT_PATH = DOCUMENT_METADATA_GROUP_PATH + DOCUMENT_METADATA_PATH_SEPARATOR + MODIFIED_AT_METADATA_NAME;
	/**
	 * Key for the user or system that last modified the document.
	 */
	String MODIFIER_METADATA_NAME = "modifier";
	/**
	 * Absolute path to the modifier metadata field.
	 */
	String MODIFIER_PATH = DOCUMENT_METADATA_GROUP_PATH + DOCUMENT_METADATA_PATH_SEPARATOR + MODIFIER_METADATA_NAME;
	/**
	 * Key for the timestamp when the document was created.
	 */
	String CREATED_AT_METADATA_NAME = "createdAt";
	/**
	 * Absolute path to the created-at metadata field.
	 */
	String CREATED_AT_PATH = DOCUMENT_METADATA_GROUP_PATH + DOCUMENT_METADATA_PATH_SEPARATOR + CREATED_AT_METADATA_NAME;
	/**
	 * Key for the user or system that created the document.
	 */
	String CREATOR_METADATA_NAME = "creator";
	/**
	 * Absolute path to the creator metadata field.
	 */
	String CREATOR_PATH = DOCUMENT_METADATA_GROUP_PATH + DOCUMENT_METADATA_PATH_SEPARATOR + CREATOR_METADATA_NAME;
	/**
	 * Key for the version of the document model that produced the document.
	 */
	String MODEL_VERSION_METADATA_NAME = "modelVersion";
	/**
	 * Absolute path to the model-version metadata field.
	 */
	String MODEL_VERSION_PATH = DOCUMENT_METADATA_GROUP_PATH + DOCUMENT_METADATA_PATH_SEPARATOR + MODEL_VERSION_METADATA_NAME;
	/**
	 * Key for the reference identifier of the document model (e.g., model code or URI).
	 */
	String MODEL_REFERENCE_METADATA_NAME = "modelReference";
	/**
	 * Absolute path to the model-reference metadata field.
	 */
	String MODEL_REFERENCE_PATH = DOCUMENT_METADATA_GROUP_PATH + DOCUMENT_METADATA_PATH_SEPARATOR + MODEL_REFERENCE_METADATA_NAME;
	/**
	 * Key for the serialized document reference of the document.
	 */
	String DOC_REF_METADATA_NAME = "docRef";
	/**
	 * Absolute path to the document-reference metadata field.
	 */
	String DOCREF_METADATA_PATH = DOCUMENT_METADATA_GROUP_PATH + DOCUMENT_METADATA_PATH_SEPARATOR + DOC_REF_METADATA_NAME;
	/**
	 * Repetition constraint for the metadata group, formatted as `min, max` (e.g., `1, 1`).
	 */
	String META_REPETITION_VALUE = "1, 1";
	/**
	 * Key for extension-specific metadata nested under the document metadata group.
	 */
	String EXTENSIONS_METADATA_NAME = "extensions";
	String EXTENSIONS_METADATA_PATH = DOCUMENT_METADATA_GROUP_PATH + DOCUMENT_METADATA_PATH_SEPARATOR + EXTENSIONS_METADATA_NAME;

}
