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
package com.mgmtp.a12.dataservices.cdd.domain.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.mgmtp.a12.dataservices.cdd.jms.internal.ComposeDocumentModel;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.model.header.Annotation;
import com.mgmtp.a12.model.header.Header;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.mgmtp.a12.dataservices.cdd.CddConstants.CDM_QUERY_ROOT_ANNOTATION;

/**
 * This class is identical to CDM with exception that it is bound to specific document reference via targetDocRef field
 */
@JsonPropertyOrder({
	"docRef", "modelName",
	"relationshipModelName", "children"
})
@Data @NoArgsConstructor @AllArgsConstructor(access = AccessLevel.PRIVATE) @Builder(toBuilder = true)
public class CddSkeleton implements ICddSkeletonNode {

	private DocumentReference targetDocRef;
	@Builder.Default private List<CddSkeletonGroup> children = new ArrayList<>();
	@JsonIgnore private ComposeDocumentModel cdm;

	@Override
	public DocumentReference getTargetDocRef() {
		return targetDocRef;
	}

	public DocumentReference getDocRef() {
		return getModelName() == null ? null : Optional.ofNullable(getTargetDocRef())
			.map(t -> new DocumentReference(getModelName(), t.toString()))
			.orElse(null);
	}

	public String getModelName() {
		return Optional.ofNullable(getCdm())
			.map(ComposeDocumentModel::getHeader)
			.map(Header::getId)
			.orElseThrow(() -> new IllegalStateException("CDM name is missing"));
	}

	@Override public String getTargetDocumentModel() {
		return Optional.ofNullable(getCdm()).stream()
			.map(ComposeDocumentModel::getHeader)
			.map(Header::getAnnotations)
			.flatMap(Collection::stream)
			.filter(a -> Objects.equals(a.getName(), CDM_QUERY_ROOT_ANNOTATION))
			.map(Annotation::getValue)
			.findAny()
			.orElse(null);
	}

	public static class CddSkeletonBuilder {}
}
