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
package com.mgmtp.a12.examples.attachment;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.attachment.AttachmentAnnotation;
import com.mgmtp.a12.dataservices.attachment.AttachmentHeader;
import com.mgmtp.a12.dataservices.attachment.IDirtyAttachmentCleanupCondition;

/**
 * Cleanup condition that prevents deletion of attachments marked with the {@link #REUSABLE} annotation set to `true`.
 * Attachments are considered deletable only if no annotation named `reusable` evaluates to `true`.
 */
@Component public class ReusableAttachmentCleanupCondition implements IDirtyAttachmentCleanupCondition {

	/**
	 * Annotation key indicating that an attachment is reusable and should not be deleted when dirty.
	 */
	public static final String REUSABLE = "reusable";

	/**
	 * Determines whether an attachment can be deleted based on its {@link AttachmentHeader} annotations.
	 * An attachment is deletable only if the `reusable` annotation is absent or evaluates to `false`.
	 *
	 * @param attachmentHeader header containing attachment metadata and annotations; must not be null.
	 * @return `true` if the attachment can be deleted; `false` if it is marked reusable.
	 */
	@Override public boolean canBeDeleted(AttachmentHeader attachmentHeader) {
		return attachmentHeader.getAnnotations().stream()
			.filter(a -> Objects.equals(a.getName(), REUSABLE))
			.map(AttachmentAnnotation::getValue)
			.noneMatch(Boolean::parseBoolean);
	}
}
