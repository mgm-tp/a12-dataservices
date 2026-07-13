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
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants;
import com.mgmtp.a12.dataservices.relationship.RelationshipLink;
import com.mgmtp.a12.dataservices.relationship.RelationshipLinkService;
import com.mgmtp.a12.dataservices.relationship.spec.LinkDescriptor;
import com.mgmtp.a12.dataservices.relationship.spec.RelationshipLinkSpec;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;
import com.mgmtp.a12.dataservices.utils.OperationContextHolder;

import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Link two documents together.
 *
 * To add a link you need to have a Relationship Model (`link.linkDescriptor.relationshipModel`) which defines the document models and their
 * subtypes that can be linked together.
 *
 * Additionally, you should specify exactly two roles, each of which consist of the role name (`link.linkDescriptor.entities.role`)
 * and the {@link DocumentReference} of the linked document (`link.linkDescriptor.entities.docRef`).
 *
 * You can also add a link document which contains the link metadata.
 *
 * If the `unbounded` field in the RM is false, the RM can also define an upper limit of links for
 * a document of a role. Then the number of links for a document must not be exceeded for this role. This maximum number is defined
 * in the RM under `upperLimit`.
 *
 * This operation does not ensure the deferred data integrity constraints (upper limit). Instead, the ADD LINK operation fires
 * events that fill a ThreadLocal collection, which is used by `RelationshipLinkOperationValidator` only after all the operations are finished.
 * Calling this operation directly therefore might corrupt link integrity and cause issues with subsequent RPC operations due to the usage of
 * ThreadLocal collections for validation.
 */
@Slf4j
@RemoteOperation(name = CoreOperationConstants.ADD_LINK_OPERATION, group = CoreOperationConstants.LINK_OPERATIONS_GROUP)
@RequiredArgsConstructor
public class AddLinkOperation {
	private final RelationshipLinkService relationshipLinkService;
	private final Anonymizer anonymizer;

	/**
	 *
	 * @param linkDescriptor The LinkDescriptor.
	 * @param linkDocument An object of type JsonNode that represents the document.
	 * @see LinkDescriptor
	 * @return The result is the RelationshipLinkSpec of the newly created link.
	 */
	@Transactional
	public RelationshipLinkSpec rpc(@NonNull @JsonRpcParam("linkDescriptor") LinkDescriptor linkDescriptor, @Nullable @JsonRpcParam("linkDocument") JsonNode linkDocument) {
		return RpcExceptionMapper.mapRpcValidationException(() -> {
			log.debug("{} called with parameters [linkDescriptor={}]",
				CoreOperationConstants.ADD_LINK_OPERATION,
				anonymizer.apply(linkDescriptor.toString())
			);

			RelationshipLink createdLink = (Objects.isNull(linkDocument) || linkDocument.isNull()) ? relationshipLinkService.create(linkDescriptor) :
				relationshipLinkService.create(linkDescriptor, linkDocument.toString());
			linkDescriptor.setLinkDocumentDocRef(createdLink.getLinkDocumentDocRef());
			linkDescriptor.getEntities().forEach(entity -> {
				if (Objects.isNull(entity.getModelName()) && Objects.nonNull(entity.getDocRef())) {
					entity.setModelName(entity.getDocRef().getDocumentModelName());
				}
			});
			String sourceRoleName = linkDescriptor.getSourceRole().getRole();
			String targetRoleName = linkDescriptor.getTargetRole().getRole();
			RelationshipLinkSpec result = RelationshipLinkSpec.builder()
				.linkDescriptor(linkDescriptor)
				.id(String.valueOf(createdLink.getId()))
				.sourceRank(createdLink.getRoles().get(sourceRoleName).getOrder())
				.targetRank(createdLink.getRoles().get(targetRoleName).getOrder())
				.build();
			OperationContextHolder.put(result);
			return result;
		}, CoreOperationConstants.ADD_LINK_OPERATION);
	}
}
