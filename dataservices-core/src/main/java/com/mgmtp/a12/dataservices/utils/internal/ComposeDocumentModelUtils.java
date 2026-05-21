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
package com.mgmtp.a12.dataservices.utils.internal;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.dataservices.cdd.domain.internal.CddSkeletonGroup;
import com.mgmtp.a12.dataservices.cdd.internal.CddSupport;
import com.mgmtp.a12.dataservices.cdd.jms.internal.ComposeDocumentModel;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.model.header.Annotation;
import com.mgmtp.a12.model.header.Header;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.mgmtp.a12.dataservices.cdd.CddConstants.CDM_QUERY_ROOT_ANNOTATION;
import static com.mgmtp.a12.dataservices.cdd.CddConstants.CDM_RELATIONSHIP_ANNOTATION;
import static com.mgmtp.a12.dataservices.model.ModelConstants.DOCUMENT_MODEL_TYPE;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ComposeDocumentModelUtils {

	public static boolean isComposeDocumentModel(Header header) {
		return DOCUMENT_MODEL_TYPE.equals(header.getModelType()) && Optional.of(header)
			.map(Header::getAnnotations)
			.stream()
			.flatMap(Collection::stream)
			.anyMatch(ComposeDocumentModelUtils::hasCrdAnnotation);
	}

	public static boolean isDocumentModel(Header header) {
		return DOCUMENT_MODEL_TYPE.equals(header.getModelType()) && header.getAnnotations().stream().noneMatch(ComposeDocumentModelUtils::hasCrdAnnotation);
	}

	public static boolean hasCrdAnnotation(Annotation m) {
		return Objects.equals(m.getName(), CDM_QUERY_ROOT_ANNOTATION) && StringUtils.isNotBlank(m.getValue());
	}

	public static String getCrdModelName(IDocumentModel documentModel) {

		return Optional.of(documentModel)
			.map(IDocumentModel::getHeader)
			.map(ModelUtils::getAnnotations)
			.flatMap(annotations -> annotations
				.filter(a -> Objects.equals(a.getName(), CDM_QUERY_ROOT_ANNOTATION))
				.map(Annotation::getValue)
				.findAny())
			.orElse(null);
	}

	public static String getCrdModelName(Header header) {
		return ModelUtils.getAnnotations(header)
			.filter(ComposeDocumentModelUtils::hasCrdAnnotation)
			.map(Annotation::getValue)
			.findAny()
			.orElseThrow(() -> new InvalidInputException("Model: " + header.getId() + " isn't a CDM"));
	}

	/**
	 * returns skeleton groups with no link information for all {@link IGroup}s that has cdd annotation.
	 *
	 * @param cdm compose document model to process
	 * @return stream of skeleton groups that has cdd annotations
	 */
	public static Stream<CddSkeletonGroup> getCdmSkeletonGroups(ComposeDocumentModel cdm) {
		return getAllGroups(cdm.getContent().getDocumentModelRoot())
			.filter(a -> ModelUtils.hasAnnotation(a, CDM_RELATIONSHIP_ANNOTATION))
			.map(CddSkeletonGroup::new);
	}

	/**
	 * Get flat stream of provided group and its descendants
	 *
	 * @param group top level group
	 * @return stream of all groups in the tree
	 */
	public static Stream<IGroup> getAllGroups(IGroup group) {
		return Stream.concat(Stream.of(group),
			CddSupport.getDirectChildGroups(group)
				.flatMap(ComposeDocumentModelUtils::getAllGroups));
	}

	/**
	 * Get `RootGroup` from cdm, if path is provided then root will be calculated from path.
	 * @param path the String path of cdm, from which we will get group as `RootGroup`.
	 * @param cdm Compose document model.
	 * @param documentModelServiceFactory the factory to retrieve search service for querying group.
	 * @return `RootGroup` from input cdm.
	 */
	public static IGroup getRootGroup(String path, IDocumentModel cdm, DocumentModelServiceFactory documentModelServiceFactory) {
		return path == null || StringUtils.equals(path, "/")
			? cdm.getContent().getDocumentModelRoot()
			: documentModelServiceFactory.createDocumentModelSearchService(cdm).getByPath(path)
			.filter(IGroup.class::isInstance)
			.map(IGroup.class::cast)
			.orElseThrow(() -> new NotFoundException(String.format("No group at path %s in the document model %s", path, cdm.getHeader().getId())).withAnonymityMessage("No group at path in document model."));
	}
}
