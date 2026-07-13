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
package com.mgmtp.a12.dataservices.constants;

import java.nio.file.Path;

import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Common constants used by the SME Workspace processing.
 */
@OnlyForUsage public interface SmeWorkspaceConstants {

	/**
	 * Constant used by the document ID generator.
	 * If the existing ID is `__NEW__`, a new one will be returned; otherwise, the existing document ID value will be retained.
	 */
	String IGNORED_ID = "__NEW__";

	String SME_WORKSPACE_PROPERTY_PATH = ".sme-workspace";
	String DATA_DIR = "data";
	String META_DIR = "meta";
	String USER_DIR = "user";
	String MODELS_DIR = "models";
	Path FULL_MODEL_PATH = Path.of(DATA_DIR, MODELS_DIR);
	String ATTACHMENTS_DIR = "attachments";
	Path FULL_ATTACHMENT_PATH = Path.of(DATA_DIR, ATTACHMENTS_DIR);
	String DOCUMENTS_DIR = "documents";
	Path FULL_DOCUMENT_PATH = Path.of(DATA_DIR, DOCUMENTS_DIR);
	String LINKS_DIR = "links";
	Path FULL_LINK_PATH = Path.of(DATA_DIR, LINKS_DIR);
	String WORKSPACEDATA_ITEMS_FILE = "workspacedata_items.json";
	Path FULL_META_PATH = Path.of(DATA_DIR, META_DIR, WORKSPACEDATA_ITEMS_FILE);
	String USERS_FILE = "users.yaml";
	Path FULL_USER_PATH = Path.of(DATA_DIR, USER_DIR, USERS_FILE);
	String ROLES_FILE = "roles.yaml";
	Path FULL_ROLE_PATH = Path.of(DATA_DIR, USER_DIR, ROLES_FILE);
	String JSON_EXTENSION = ".json";
}
