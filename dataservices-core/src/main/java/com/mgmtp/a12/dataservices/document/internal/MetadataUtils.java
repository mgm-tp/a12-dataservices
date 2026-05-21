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
package com.mgmtp.a12.dataservices.document.internal;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants;
import com.mgmtp.a12.dataservices.model.metadata.internal.DocumentMetadataMetaModelProvider;
import com.mgmtp.a12.dataservices.utils.internal.DocumentUtils;
import com.mgmtp.a12.kernel.md.document.apiV2.UpdateAction;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;

import lombok.Getter;

import static com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants.CREATED_AT_PATH;
import static com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants.CREATOR_PATH;
import static com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants.DOCREF_METADATA_PATH;
import static com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants.EXTENSIONS_METADATA_PATH;
import static com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants.MODEL_REFERENCE_PATH;
import static com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants.MODEL_VERSION_PATH;
import static com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants.MODIFIED_AT_PATH;
import static com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants.MODIFIER_PATH;

@Getter
@Component public class MetadataUtils {

	private final DocumentMetadataMetaModelProvider modelProvider;
	private final boolean hasModelVersionField;
	private final boolean hasCreatorField;
	private final boolean hasCreatedAtField;
	private final boolean hasModifierField;
	private final boolean hasModifiedAtField;

	public MetadataUtils(DocumentMetadataMetaModelProvider modelProvider, DocumentModelServiceFactory documentModelServiceFactory) {
		this.modelProvider = modelProvider;
		IDocumentModelSearchService searchService = documentModelServiceFactory.createDocumentModelSearchService(modelProvider.getModel());
		this.hasModelVersionField = hasField(searchService, DocumentMetadataConstants.MODEL_VERSION_METADATA_NAME);
		this.hasCreatorField = hasField(searchService, DocumentMetadataConstants.CREATOR_METADATA_NAME);
		this.hasCreatedAtField = hasField(searchService, DocumentMetadataConstants.CREATED_AT_METADATA_NAME);
		this.hasModifierField = hasField(searchService, DocumentMetadataConstants.MODIFIER_METADATA_NAME);
		this.hasModifiedAtField = hasField(searchService, DocumentMetadataConstants.MODIFIED_AT_METADATA_NAME);
	}

	public DocumentV2 createDocumentMetadata(DocumentV2 document, DocumentReference documentReference, String currentUser, Instant currentTime, Object modelVersion) {
		document = createMandatoryMetadata(document, documentReference);
		GroupInstanceV2 extensionsGroup = document.group(EXTENSIONS_METADATA_PATH);
		List<UpdateAction> metadataToUpdate = new ArrayList<>();
		if (hasModelVersionField) {
			metadataToUpdate.add(DocumentUtils.createFieldUpdateAction(MODEL_VERSION_PATH, new int[] { 1, 1 }, modelVersion));
		}
		if (hasCreatorField) {
			metadataToUpdate.add(DocumentUtils.createFieldUpdateAction(CREATOR_PATH, new int[] { 1, 1 }, currentUser));
		}
		if (hasCreatedAtField) {
			metadataToUpdate.add(DocumentUtils.createFieldUpdateAction(CREATED_AT_PATH, new int[] { 1, 1 }, currentTime));
		}
		metadataToUpdate.addAll(buildUpdateActions(currentUser, currentTime, extensionsGroup));
		return document.withBatchUpdates(metadataToUpdate);
	}

	public DocumentV2 createMandatoryMetadata(DocumentV2 document, DocumentReference documentReference) {
		return document.withBatchUpdates(List.of(
			DocumentUtils.createFieldUpdateAction(MODEL_REFERENCE_PATH, new int[] { 1, 1 },
				documentReference.getDocumentModelName()),
			DocumentUtils.createFieldUpdateAction(DOCREF_METADATA_PATH, new int[] { 1, 1 }, documentReference.toString()))
		);
	}

	public DocumentV2 updateDocumentMetadata(DocumentV2 originalDocument, DocumentV2 updatedDocument, String currentUserName, Instant currentTime) {
		GroupInstanceV2 extensionsGroup = updatedDocument.group(DocumentMetadataConstants.EXTENSIONS_METADATA_PATH);

		List<UpdateAction> updateActions = buildUpdateActions(currentUserName, currentTime, extensionsGroup);

		return updatedDocument.withGroup(
			DocumentMetadataConstants.DOCUMENT_METADATA_GROUP_PATH, originalDocument.group(DocumentMetadataConstants.DOCUMENT_METADATA_GROUP_PATH)
		).withBatchUpdates(updateActions);
	}

	/**
	 * Checks if a field with the given name exists in the document model.
	 *
	 * @param searchService for finding a field
	 * @param fieldName the name of the field to look for
	 * @return true if the field exists, false otherwise
	 */
	private boolean hasField(IDocumentModelSearchService searchService, String fieldName) {
		String fieldPath = DocumentMetadataConstants.DOCUMENT_METADATA_GROUP_PATH
			+ DocumentMetadataConstants.DOCUMENT_METADATA_PATH_SEPARATOR
			+ fieldName;
		return searchService.getByPath(fieldPath).isPresent();
	}

	private List<UpdateAction> buildUpdateActions(String currentUserName, Instant currentTime, GroupInstanceV2 extensionsGroup) {
		List<UpdateAction> baseActions = new ArrayList<>();
		if(hasModifierField) {
			baseActions.add(DocumentUtils.createFieldUpdateAction(MODIFIER_PATH, new int[] { 1, 1 }, currentUserName));
		}
		if (hasModifiedAtField) {
			baseActions.add(DocumentUtils.createFieldUpdateAction(MODIFIED_AT_PATH, new int[] { 1, 1 }, currentTime));
		}

		if (extensionsGroup != null) {
			baseActions.add(UpdateAction.putGroup(EXTENSIONS_METADATA_PATH, extensionsGroup));
		}
		return  baseActions;
	}
}
