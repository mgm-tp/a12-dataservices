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
import com.mgmtp.a12.dataservices.common.exception.NotFoundException;
import com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants;
import com.mgmtp.a12.dataservices.relationship.RelationshipLink;
import com.mgmtp.a12.dataservices.relationship.RelationshipLinkService;
import com.mgmtp.a12.dataservices.relationship.internal.RelationshipValidationSupport;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipLinkSpec;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Delete one link of the relationship.
 *
 * If the link doesn't exist, the operation silently finishes without error to achieve idempotent behavior.
 *
 * If there is no error the result will always return the status code 200 regardless if the link has been found or not.
 */
@Slf4j
@RemoteOperation(name = CoreOperationConstants.DELETE_LINK_OPERATION, group = CoreOperationConstants.LINK_OPERATIONS_GROUP)
@RequiredArgsConstructor
public class DeleteLinkOperation {
	private final RelationshipLinkService relationshipLinkService;
	private final Anonymizer anonymizer;

	/**
	 * Deletes a relationship link as specified by the given {@link RelationshipLinkSpec}.
	 * If a non-existing link id is provided in `relationshipLinkSpec.getId()`, authorization will not be applied because there is no authorization for a non-existing link.
	 *
	 * `relationshipLinkSpec.getLinkDescriptor().getRelationshipModel()` will not be evaluated for authorization until a valid link id is provided in `relationshipLinkSpec`.
	 *
	 * @param relationshipLinkSpec the specification containing all link information
	 */
	@Transactional
	public void rpc(@NonNull @JsonRpcParam("linkRef") RelationshipLinkSpec relationshipLinkSpec) {
		RpcExceptionMapper.mapRpcValidationException(() -> {
			log.debug("{} called with parameters [linkRef={}]", CoreOperationConstants.DELETE_LINK_OPERATION,
				anonymizer.apply(relationshipLinkSpec.toString()));
			try {
				String linkId = relationshipLinkSpec.getId();

				// TODO A12S-4849:  We need to validate the link descriptor before deleting the link.
				// This requires fetching the link entity in the operation which is an anti-pattern, we should only need the link id to delete it.
				RelationshipLink linkToDelete = relationshipLinkService.load(linkId);

				RelationshipValidationSupport.validateLinkCharacteristic(relationshipLinkSpec.getLinkDescriptor());
				RelationshipValidationSupport.validateLink(linkToDelete, relationshipLinkSpec.getLinkDescriptor());

				relationshipLinkService.delete(relationshipLinkSpec.getId());
			} catch (NotFoundException ex) {} // ignored.

			return null;
		}, CoreOperationConstants.DELETE_LINK_OPERATION);
	}
}
