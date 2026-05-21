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
package com.mgmtp.a12.dataservices.attachment.persitence.internal.contentstore;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.contentstore.utils.internal.UrlUtils;
import com.mgmtp.a12.dataservices.attachment.AttachmentHeader;
import com.mgmtp.a12.dataservices.attachment.ThumbnailType;
import com.mgmtp.a12.dataservices.attachment.internal.ThumbnailUrlGenerator;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ThumbnailUrlGeneratorImpl implements ThumbnailUrlGenerator {

	public static final String EMPTY_CS_CONTEXT_PATH = "";
	private final DataServicesCoreProperties dataServicesCoreProperties;

	@Override public Optional<String> generateThumbnailUrl(@NonNull AttachmentHeader attachmentHeader, @NonNull ThumbnailType thumbnailType) {
		// try generate url if  mgmtp.a12.dataservices.attachments.thumbnail.optimization.url.enabled is true, otherwise return Optional.empty()
		return Optional.ofNullable(ThumbnailType.BIG.equals(thumbnailType) ? attachmentHeader.getThumbnailBigId() : attachmentHeader.getThumbnailSmallId())
			.filter(id -> dataServicesCoreProperties.getAttachments().getThumbnail().getOptimization().getUrl().isEnabled())
			.filter(StringUtils::isNotBlank)
			.map(id -> UrlUtils.buildContentUrl(
				dataServicesCoreProperties.getAttachments().getThumbnail().getOptimization().getBaseUrl(), EMPTY_CS_CONTEXT_PATH, id));
	}
}
