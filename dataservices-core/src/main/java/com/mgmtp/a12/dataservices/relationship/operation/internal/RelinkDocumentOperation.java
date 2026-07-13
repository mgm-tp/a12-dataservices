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
package com.mgmtp.a12.dataservices.relationship.operation.internal;

import org.springframework.transaction.annotation.Transactional;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.mgmtp.a12.dataservices.common.anonymizing.Anonymizer;
import com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants;
import com.mgmtp.a12.dataservices.relationship.RelationshipLink;
import com.mgmtp.a12.dataservices.relationship.RelationshipLinkService;
import com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipLinkSpec;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;
import com.mgmtp.a12.dataservices.utils.OperationContextHolder;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Change the link assignment for a specific document. It deletes the link by `linkRef` reference and adds a new link which
 * is defined by `linkDescriptor`.
 *
 * * The operation reports an error if the `linkRef` references a link that does not exist,
 *   because the link document must be reused for newly created link.
 * * The operation reports an error if the `linkDocument` is required in the new relationship,
 *   but it is not present in the old link.
 *
 * The operation fires the following events: <<events,`RelationshipLinkAfterCreateEvent, RelationshipLinkAfterDeleteEvent`>>.
 *
 * For `relationshipModel` parameter, the error response will different for null and empty value.
 */
@Slf4j
@RemoteOperation(name = CoreOperationConstants.RELINK_DOCUMENT_OPERATION, group = CoreOperationConstants.LINK_OPERATIONS_GROUP)
@RequiredArgsConstructor
public class RelinkDocumentOperation {
	private final RelationshipLinkService relationshipLinkService;
	private final Anonymizer anonymizer;

	/**
	 * @param linkDescriptor Descriptor of the new link (must contain the desired document reference of linkRef).
	 * @param linkRef The reference to the link that needs to be moved.
	 * @return The `RelationshipLinkSpec` of a new link.
	 * @see LinkDescriptor
	 */
	@Transactional
	public RelationshipLinkSpec rpc(@NonNull @JsonRpcParam("linkDescriptor") LinkDescriptor linkDescriptor, @NonNull @JsonRpcParam("linkRef") String linkRef) {
		return RpcExceptionMapper.mapRpcValidationException(() -> {
			log.debug("{} called with parameters [linkDescriptor={}, linkRef={}]", CoreOperationConstants.RELINK_DOCUMENT_OPERATION,
				anonymizer.apply(linkDescriptor.toString()), anonymizer.apply(linkRef));

			RelationshipLink newLink = relationshipLinkService.relink(linkDescriptor, linkRef);

			String sourceRoleName = linkDescriptor.getSourceRole().getRole();
			String targetRoleName = linkDescriptor.getTargetRole().getRole();
			RelationshipLinkSpec result = RelationshipLinkSpec.builder()
				.linkDescriptor(linkDescriptor)
				.id(String.valueOf(newLink.getId()))
				.sourceRank(newLink.getRoles().get(sourceRoleName).getOrder())
				.targetRank(newLink.getRoles().get(targetRoleName).getOrder())
				.build();
			OperationContextHolder.put(result);
			return result;
		}, CoreOperationConstants.RELINK_DOCUMENT_OPERATION);
	}
}
