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
package com.mgmtp.a12.dataservices.document;

import java.io.StringReader;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.mgmtp.a12.dataservices.document.internal.entity.DocumentEntity;
import com.mgmtp.a12.dataservices.document.support.DocumentSupport;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

/**
 * Mapper for mapping between {@link DataServicesDocument} and {@link DocumentEntity}.
 *
 */
@Mapper(componentModel = SPRING)
public abstract class DocumentMapper {

	@Autowired protected DocumentSupport documentSupport;

	/**
	 * Maps a {@link DataServicesDocument} and its serialized content to a {@link DocumentEntity}.
	 *
	 * @param dataServicesDocument the Data Services document to map; must not be null
	 * @param documentContent the JSON content of the document; must not be null
	 * @return a new {@link DocumentEntity} populated with id, model name, and content
	 */
	@Mapping(target = "content", source = "documentContent")
	@Mapping(target = "modelName", expression = "java(dataServicesDocument.getMetadata().getDocumentModelReference())")
	@Mapping(target = "id", expression = "java(dataServicesDocument.getMetadata().getDocRef().getDocumentId())")
	public abstract DocumentEntity toDocumentEntity(DataServicesDocument dataServicesDocument, String documentContent);

	/**
	 * Converts a {@link DocumentEntity} to kernel {@link DocumentV2} by deserializing its JSON content.
	 *
	 * @param documentEntity the persisted document entity; must not be null
	 * @return the deserialized {@link DocumentV2} content of the entity
	 */
	public DocumentV2 convertToDocumentContent(DocumentEntity documentEntity) {
		return documentSupport.convertJSONToDocument(documentEntity.getDocRef().getDocumentModelName(), new StringReader(documentEntity.getContent()), documentEntity.getDocRef());
	}
}
