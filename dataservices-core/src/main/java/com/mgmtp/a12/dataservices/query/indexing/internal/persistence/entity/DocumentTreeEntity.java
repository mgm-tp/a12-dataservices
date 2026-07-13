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
package com.mgmtp.a12.dataservices.query.indexing.internal.persistence.entity;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames;
import com.mgmtp.a12.dataservices.query.DocumentTreeNodeType;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Converter;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor(access = AccessLevel.PRIVATE) @Builder(toBuilder = true)
@Entity public class DocumentTreeEntity implements Serializable {

	@EmbeddedId
	private CompositeId id;

	@Column(name = ColumnNames.LINK_DOCUMENT_COLUMN_ALIAS, columnDefinition = QueryGeneratorConstants.JSON_TYPE) private String linkDocument;

	@Column(name = ColumnNames.MODEL_NAME_COLUMN_ALIAS) private @NotNull String modelName;

	@Column(name = ColumnNames.CONTENT_COLUMN_ALIAS, columnDefinition = QueryGeneratorConstants.JSON_TYPE) private String content;

	@Column(name = ColumnNames.TOTAL_COUNT_COLUMN_ALIAS) private Long totalCount;

	@Converter public static class DocRefConverter implements AttributeConverter<DocumentReference, String> {

		@Override public String convertToDatabaseColumn(DocumentReference docRef) {
			return docRef == null || !docRef.isValid() ? null : docRef.toString();
		}

		@Override public DocumentReference convertToEntityAttribute(String dbData) {
			return StringUtils.isBlank(dbData) ? new BlankDocumentReference() : new DocumentReference(dbData);
		}
	}

	public static class BlankDocumentReference extends DocumentReference {

		@Override public String toString() {
			return "BlankDocumentReference";
		}
	}

	@Data
	@Embeddable public static class CompositeId implements Serializable {

		@Convert(converter = DocRefConverter.class)
		@Column(name = ColumnNames.DOC_REF_COLUMN_ALIAS) private DocumentReference docRef;

		@Column(name = ColumnNames.RELATIONSHIP_MODEL_COLUMN_ALIAS) private String relationshipModel;

		@Column(name = ColumnNames.SOURCE_ROLE_COLUMN_ALIAS) private String sourceRole;

		@Convert(converter = DocRefConverter.class)
		@Column(name = ColumnNames.SOURCE_DOCREF_COLUMN_ALIAS) private DocumentReference sourceDocRef;

		@Column(name = ColumnNames.TARGET_ROLE_COLUMN_ALIAS) private String targetRole;

		@Convert(converter = DocRefConverter.class)
		@Column(name = ColumnNames.TARGET_DOCREF_COLUMN_ALIAS) private DocumentReference targetDocRef;

		@Column(name = ColumnNames.LINK_ID_COLUMN_ALIAS, nullable = false) private String linkId;

		@Enumerated(EnumType.STRING)
		@Column(name = ColumnNames.TYPE_COLUMN_ALIAS) private DocumentTreeNodeType type;

		@Column(name = ColumnNames.BACKREFERENCE_COLUMN_ALIAS) private String backReference;

		@Column(name = ColumnNames.INTERNAL_ID_COLUMN_ALIAS) private String internalId;

		@Column(name = ColumnNames.DEPTH_COLUMN_ALIAS, nullable = false) private @NotNull int depth;

		@Column private boolean fieldsProjection;
	}

}
