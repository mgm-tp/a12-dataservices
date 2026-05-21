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
package com.mgmtp.a12.dataservices.attachment.internal;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mgmtp.a12.dataservices.attachment.AttachmentAnnotation;
import com.mgmtp.a12.dataservices.attachment.AttachmentHeader;
import com.mgmtp.a12.dataservices.attachment.AttachmentHeaderSpec;
import com.mgmtp.a12.dataservices.attachment.AttachmentReference;
import com.mgmtp.a12.dataservices.attachment.AttachmentUrl;
import com.mgmtp.a12.dataservices.attachment.DataServicesAttachmentURL;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.entity.AttachmentAnnotationEntity;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.entity.AttachmentHeaderEntity;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.entity.AttachmentReferenceEntity;
import com.mgmtp.a12.dataservices.reference.GenericReference;

/**
 * Mapper class for attachment and thumbnail handling.
 * Despite the mapper uses the default component model, Spring bean is created by {@link AttachmentConfiguration#attachmentMapper}.
 */
@Mapper public abstract class AttachmentMapper {

	@Mapping(target = "smallThumbnailUrl", expression = "java(smallThumbnailUrl)")
	@Mapping(target = "bigThumbnailUrl", expression = "java(bigThumbnailUrl)")
	public abstract AttachmentHeaderSpec toHeaderSpec(AttachmentHeader attachmentHeader, String bigThumbnailUrl, String smallThumbnailUrl);

	public abstract DataServicesAttachmentURL toDataServicesAttachmentURL(AttachmentUrl attachmentUrl);

	@Mapping(target = "attachmentId", source = "id")
	@Mapping(target = "filename", source = "fileName")
	@Mapping(target = "references", expression = "java(getReferences(e))")
	@Mapping(target = "annotations", expression = "java(getAnnotations(e))")
	public abstract AttachmentHeader toAttachmentHeader(AttachmentHeaderEntity e);

	@Mapping(target = "id", source = "attachmentId")
	@Mapping(target = "fileName", source = "filename")
	@Mapping(target = "references", expression = "java(getReferenceEntities(header))")
	@Mapping(target = "annotations", expression = "java(getAnnotationEntities(header))")
	public abstract AttachmentHeaderEntity toAttachmentHeaderEntity(AttachmentHeader header);

	@NotNull public static AttachmentAnnotation toAttachmentAnnotation(AttachmentAnnotationEntity a) {
		return new AttachmentAnnotation(a.getName(), a.getAnnotationValue());
	}

	protected List<AttachmentReference<GenericReference>> getReferences(AttachmentHeaderEntity headerEntity) {
		return headerEntity.getReferences()
			.stream()
			.map(referenceEntity -> AttachmentReference.parse(referenceEntity.getType(), referenceEntity.getReference()))
			.toList();
	}

	protected List<AttachmentAnnotation> getAnnotations(AttachmentHeaderEntity headerEntity) {
		return headerEntity.getAnnotations()
			.stream()
			.map(annotationEntity -> AttachmentAnnotation.builder()
				.name(annotationEntity.getName())
				.value(annotationEntity.getAnnotationValue())
				.build())
			.toList();
	}

	protected List<AttachmentReferenceEntity> getReferenceEntities(AttachmentHeader header) {
		return Optional.ofNullable(header.getReferences())
			.stream()
			.flatMap(List::stream)
			.map(this::buildAttachmentReferenceEntity)
			.toList();
	}

	protected List<AttachmentAnnotationEntity> getAnnotationEntities(AttachmentHeader header) {
		return Optional.ofNullable(header.getAnnotations())
			.stream()
			.flatMap(List::stream)
			.map(a -> AttachmentAnnotationEntity.builder()
				.name(a.getName())
				.annotationValue(a.getValue())
				.build())
			.toList();
	}

	public AttachmentReferenceEntity buildAttachmentReferenceEntity(AttachmentReference<? extends GenericReference> r) {
		return AttachmentReferenceEntity.builder()
			.reference(r.getReference().toString())
			.type(r.getType())
			.build();
	}
}
