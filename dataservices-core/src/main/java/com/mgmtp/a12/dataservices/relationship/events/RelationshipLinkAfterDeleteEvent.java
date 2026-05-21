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
package com.mgmtp.a12.dataservices.relationship.events;

import com.mgmtp.a12.dataservices.relationship.RelationshipLink;
import com.mgmtp.a12.dataservices.relationship.model.RelationshipModel;
import com.mgmtp.a12.dataservices.common.events.internal.EventDocumentation;

/**
 * The event is published after the link is successfully deleted.
 *
 * The RelationshipLinkAfterDeleteEvent represents the deletion of the link for a relationship model
 * between the entities described in the linkDescriptor of the RelationshipLinkSpec.
 * One RelationshipLinkAfterDeleteEvent instance could be shared between multiple instances of event consumers, therefore it must not be mutable.
 *
 * @topic Relationship events
 */

@EventDocumentation public final class RelationshipLinkAfterDeleteEvent extends AbstractRelationshipLinkEvent {
	/**
	 * Creates a new event for a deleted relationship link.
	 *
	 * @param relationshipModel The relationship model context; must not be null.
	 * @param link The deleted link instance; must not be null.
	 */
	public RelationshipLinkAfterDeleteEvent(RelationshipModel relationshipModel, RelationshipLink link) {
		super(relationshipModel, link);
	}
}
