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
package com.mgmtp.a12.dataservices.autoconfigure.attachments.internal.contentstore;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Import;

import com.mgmtp.a12.contentstore.autoconfigure.internal.ContentStoreAutoConfiguration;
import com.mgmtp.a12.contentstore.service.ContentStoreService;
import com.mgmtp.a12.dataservices.attachment.persitence.IAttachmentRepository;
import com.mgmtp.a12.dataservices.attachment.persitence.internal.contentstore.ContentStoreMapper;
import com.mgmtp.a12.dataservices.attachment.persitence.internal.contentstore.EmbeddedContentStoreAttachmentRepository;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.configuration.internal.validation.condition.contentstore.EmbeddedContentStoreModeCondition;

import lombok.RequiredArgsConstructor;

@Conditional(value = EmbeddedContentStoreModeCondition.class)
@Import({ ContentStoreAutoConfiguration.class })
@RequiredArgsConstructor
public class EmbeddedContentStoreConfiguration {

	private final ContentStoreService contentStoreService;
	private final DataServicesCoreProperties dataServicesCoreProperties;

	@ConditionalOnMissingBean(IAttachmentRepository.class)
	@Bean public IAttachmentRepository embeddedContentStoreAttachmentRepository(ContentStoreMapper contentStoreMapper) {
		return new EmbeddedContentStoreAttachmentRepository(contentStoreService, dataServicesCoreProperties, contentStoreMapper);
	}
}
