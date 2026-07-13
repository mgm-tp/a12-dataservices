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

import java.util.Objects;

import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.JsonNode;
import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.mgmtp.a12.dataservices.common.anonymizing.Anonymizer;
import com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants;
import com.mgmtp.a12.dataservices.relationship.RelationshipLinkService;
import com.mgmtp.a12.dataservices.relationship.exception.RelationshipLinkDocumentMissingException;
import com.mgmtp.a12.dataservices.relationship.exception.RelationshipLinkDocumentNotAllowedException;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipLinkSpec;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipRoleSpec;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;

import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Modify the `linkDocument` of a link.
 *
 * The operation fires the following events: <<events,`ModelAfterLoadEvent`,`RelationshipLinkAfterUpdateEvent`>>.
 *
 * The `linkDocument` must be null when the link document model is null, but it must be provided if the model is specified.
 *   Otherwise {@link RelationshipLinkDocumentNotAllowedException} or {@link RelationshipLinkDocumentMissingException} will be thrown.
 *
 * For `relationshipModel` parameter, the error response will different for null and empty value.
 */
@Slf4j
@RemoteOperation(name = CoreOperationConstants.MODIFY_LINK_OPERATION, group = CoreOperationConstants.LINK_OPERATIONS_GROUP)
@RequiredArgsConstructor
public class ModifyLinkOperation {
	private final RelationshipLinkService relationshipLinkService;
	private final Anonymizer anonymizer;

	/**
	 * @param relationshipLinkSpec The `RelationshipLinkSpec` that contains all link information.
	 * @param linkDocument An object of type `JsonNode` that represents the link document.
	 */
	@Transactional
	public void rpc(@NonNull @JsonRpcParam("linkRef") RelationshipLinkSpec relationshipLinkSpec,
		@Nullable @JsonRpcParam("linkDocument") JsonNode linkDocument) {
		RpcExceptionMapper.mapRpcValidationException(() -> {
			log.debug("{} called with parameters [linkRef={}]", CoreOperationConstants.MODIFY_LINK_OPERATION,
				anonymizer.apply(relationshipLinkSpec.toString()));

			relationshipLinkService.update(
				relationshipLinkSpec.getId(),
				relationshipLinkSpec.getLinkDescriptor(),
				(Objects.isNull(linkDocument) || linkDocument.isNull()) ? null : linkDocument.toString()
			);

			log.debug(
				"Relationship Link has been modified for relationship [{}] between documents {}",
				relationshipLinkSpec.getLinkDescriptor().getRelationshipModel(),
				relationshipLinkSpec.getLinkDescriptor().getEntities().stream().map(RelationshipRoleSpec::getDocRef).toArray()
			);

			return null;
		}, CoreOperationConstants.MODIFY_LINK_OPERATION);
	}
}
