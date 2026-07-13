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
package com.mgmtp.a12.contentstore.utils;

import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * All constants for the content store are defined here.
 *
 **/
@OnlyForUsage public interface Constants {

	/**
	 * Persistent type discriminator for public content.
	 */
	String PERSISTENT_TYPE_PUBLIC = "public";

	/**
	 * Persistent type discriminator for private content.
	 */
	String PERSISTENT_TYPE_PRIVATE = "private";

	/**
	 * Allowed persistent types for validation and input normalization.
	 */
	String[] ACCEPTABLE_PERSISTENT_TYPES = { PERSISTENT_TYPE_PUBLIC, PERSISTENT_TYPE_PRIVATE };

	/**
	 * Message pattern used when content cannot be found by id.
	 */
	String CANNOT_FIND_CONTENT_BY_ID_PATTERN = "Cannot find content by id %s";

	/**
	 * Message pattern used when content cannot be resolved from an event payload.
	 */
	String CANNOT_FIND_CONTENT_FROM_EVENT_PATTERN = "Cannot find content from event by id %s";

	/**
	 * Message pattern used when physical content is missing by id in the storage system.
	 */
	String CANNOT_FIND_PHYSICAL_CONTENT_BY_ID_PATTERN = "Cannot find physical content by id %s";

	/**
	 * Log pattern for missing content by id and persistent type (structured logging placeholders).
	 */
	String CANNOT_FIND_CONTENT_BY_ID_AND_TYPE_PATTERN = "Cannot find content by id {} with type {}";

	/**
	 * Message pattern used when an error occurs during content retrieval by id.
	 */
	String CANNOT_GET_CONTENT_BY_ID_PATTERN = "Error occurs while trying to get Content by id %s";

	/**
	 * Message pattern used when public content cannot be found for a given content id.
	 */
	String CANNOT_FIND_PUBLIC_CONTENT_BY_CONTENT_ID_PATTERN = "Cannot find public content for content id %s";

	/**
	 * Message pattern used when content cannot be found by ticket id in the storage system.
	 */
	String CONTENT_BY_TICKET_ID_NOT_EXIST_IN_STORAGE_SYSTEM_PATTERN = "Cannot find content by ticket id in Storage System %s";

	/**
	 * Message pattern used when persisting an input stream fails for a given content id.
	 */
	String CANNOT_PERSIST_INPUT_STREAM_BY_CONTENT_ID_PATTERN = "Cannot persist input stream for content id %s";

	/**
	 * Log pattern for an error occurring while retrieving content by id (structured logging placeholders).
	 */
	String ERROR_OCCURS_WHILE_TRYING_TO_GET_CONTENT_BY_ID_PATTERN = "Error occurs while trying to get Content by id {}";

	/**
	 * Message pattern used when content size exceeds the configured limit.
	 */
	String CONTENT_SIZE_CANNOT_EXCEED_LIMIT_PATTERN = "Content size cannot exceed %s";

	/**
	 * Message used when checking the input stream size fails unexpectedly.
	 */
	String ERROR_WHILE_TRYING_TO_CHECK_INPUT_STREAM_SIZE = "Error while trying to check input stream size";

	/**
	 * Message pattern used when a ticket is not available.
	 */
	String TICKET_WITH_ID_IS_NOT_AVAILABLE_PATTERN = "Ticket with id %s is not available";

	/**
	 * Message pattern used when the configured content location is unavailable.
	 */
	String CONTENT_LOCATION_UNAVAILABLE_ERROR_PATTERN = "Content location at [%s] is unavailable";

	/**
	 * Message pattern used when persisting content to the file system fails.
	 */
	String CANNOT_PERSIST_CONTENT_TO_FS_ERROR_PATTERN = "Error occurs while trying to persist content with id %s to File System";

	/**
	 * Message used when the content id is null or not a valid UUID format.
	 */
	String ID_UUID_FORMAT_ERROR_PATTERN = "Content id is null or not valid UUID format";

	/**
	 * Message pattern used when an input value is invalid.
	 */
	String INVALID_INPUT_ERROR_PATTEN = "Invalid input [%s]";

	/**
	 * Message pattern used when a persistent type is invalid.
	 */
	String INVALID_TYPE_ERROR_PATTEN = "Invalid persistent type [%s]";

	/**
	 * Message used when the content id is blank.
	 */
	String INVALID_CONTENT_ID_ERROR = "Content id can not be blank";

	/**
	 * Message used when the content input stream is suspected to be broken.
	 */
	String INVALID_CONTENT_INPUT_STREAM = "The content input stream might be broken";

	/**
	 * Message used when the content MIME type is mandatory but missing.
	 */
	String CONTENT_MIME_TYPE_MANDATORY_ERROR = "Content mime type is mandatory";
}
