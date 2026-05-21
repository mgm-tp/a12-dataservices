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
package com.mgmtp.a12.examples.relationship;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.common.events.CommonDataServicesEventListener;
import com.mgmtp.a12.dataservices.relationship.events.RelationshipLinkAfterCreateEvent;
import com.mgmtp.a12.dataservices.relationship.events.RelationshipLinkAfterDeleteEvent;
import com.mgmtp.a12.dataservices.relationship.events.RelationshipLinkAfterUpdateEvent;

import static com.mgmtp.a12.dataservices.authorization.AuthConstants.UAA_PERMISSION_TEMPLATE_PREFIX;
import static com.mgmtp.a12.dataservices.authorization.AuthConstants.UAA_PERMISSION_TEMPLATE_SUFFIX;

/**
 * Listens to relationship link lifecycle events and applies authorization checks for link write operations.
 */
@Component public class RelationshipLinkListener {

	static final String LINK_WRITE_PERMISSION = "Link Write";
	static final String UAA_LINK_WRITE_PERMISSION = UAA_PERMISSION_TEMPLATE_PREFIX + LINK_WRITE_PERMISSION + UAA_PERMISSION_TEMPLATE_SUFFIX;


	/**
	 * Handles post-create events for relationship links.
	 *
	 * @param event the event published after a link is created; never null.
	 */
	@PreAuthorize(UAA_LINK_WRITE_PERMISSION)
	@CommonDataServicesEventListener public void listenOnLinkCreated(RelationshipLinkAfterCreateEvent event) {
		//Extended listener on link events
	}

	/**
	 * Handles post-update events for relationship links.
	 *
	 * @param event the event published after a link is updated; never null.
	 */
	@PreAuthorize(UAA_LINK_WRITE_PERMISSION)
	@CommonDataServicesEventListener public void linkUpdatedListener(RelationshipLinkAfterUpdateEvent event) {
		//Extended listener on link events
	}

	/**
	 * Handles post-delete events for relationship links.
	 *
	 * @param event the event published after a link is deleted; never null.
	 */
	@PreAuthorize(UAA_LINK_WRITE_PERMISSION)
	@CommonDataServicesEventListener public void listenOnLinkDeleted(RelationshipLinkAfterDeleteEvent event) {
		//Extended listener on link events
	}
}
