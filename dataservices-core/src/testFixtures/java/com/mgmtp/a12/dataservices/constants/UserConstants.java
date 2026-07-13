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

public interface UserConstants {

	/*
	 * Definition of models and their assigned roles:
	 * | Name                           | Roles                           |
	 * |--------------------------------|---------------------------------|
	 * | Address                        | admin, guest                    |
	 * | BusinessPartnerSuper           | admin, guest                    |
	 * | BusinessPartner                | admin, guest                    |
	 * | BusinessPartnerLTD             | admin, guest                    |
	 * | Contract                       | admin, guest                    |
	 * | CoInsuredAdditionalFields      | admin, guest                    |
	 *
	 * Definition of users and their assigned roles:
	 * | Name                           | Role                            |
	 * |--------------------------------|---------------------------------|
	 * | admin                          | admin,systemAdmin               |
	 * | guest                          | guest                           |
	 * | actuator                       | ActuatorAccess                  |
	 * | document_create_user           | DocumentCreate                  |
	 * | document_copy_user             | DocumentCopy                    |
	 * | document_delete_user           | DocumentDelete                  |
	 * | document_multi_delete_user     | MultiDocumentDelete             |
	 * | document_partial_update_user   | DocumentPartialUpdate           |
	 * | document_read_user             | DocumentRead                    |
	 * | document_update_user           | DocumentUpdate                  |
	 * | document_write_user            | DocumentWrite                   |
	 * | model_create_user              | ModelCreate                     |
	 * | model_delete_user              | ModelDelete                     |
	 * | model_read_user                | ModelRead                       |
	 * | model_update_user              | ModelUpdate                     |
	 * | model_manager_user             | modelManager                    |
	 *
	 * Definition of roles and their access rights:
	 * | Role Name                      | Access Right                                    |
	 * |--------------------------------|-------------------------------------------------|
	 * | guest                          | MODEL_READ, QUERY, LINK_READ                    |
	 * | actuator                       | ACCESS_ACTUATOR                                 |
	 * | DocumentWrite                  | MODEL_READ, DOCUMENT_CREATE, DOCUMENT_UPDATE,   |
	 * |                                | DOCUMENT_PARTIAL_UPDATE, DOCUMENT_DELETE,       |
	 * |                                | LINK_WRITE                                      |
	 * | DocumentRead                   | MODEL_READ, QUERY                               |
	 * | DocumentCreate                 | MODEL_READ, DOCUMENT_CREATE                     |
	 * | DocumentUpdate                 | MODEL_READ, DOCUMENT_UPDATE                     |
	 * | DocumentPartialUpdate          | MODEL_READ, DOCUMENT_PARTIAL_UPDATE             |
	 * | DocumentDelete                 | MODEL_READ, DOCUMENT_DELETE                     |
	 * | MultiDocumentDelete            | MODEL_READ, DOCUMENT_MULTI_DELETE               |
	 * | DocumentCopy                   | MODEL_READ, QUERY, DOCUMENT_CREATE              |
	 * | ModelCreate                    | MODEL_CREATE                                    |
	 * | ModelRead                      | MODEL_READ                                      |
	 * | ModelUpdate                    | MODEL_UPDATE                                    |
	 * | ModelDelete                    | MODEL_DELETE                                    |
	 * | modelManager                   | MODEL_MANAGE                                    |
	 */
	String ADMIN_USER = "admin";
	String GUEST_USER = "guest";
	String ACTUATOR_USER = "actuator";

	String DOCUMENT_WRITE_USER = "document_write_user";
	String DOCUMENT_CREATE_USER = "document_create_user";
	String DOCUMENT_UPDATE_USER = "document_update_user";
	String DOCUMENT_PARTIAL_UPDATE_USER = "document_partial_update_user";
	String DOCUMENT_DELETE_USER = "document_delete_user";
	String DOCUMENT_MULTI_DELETE_USER = "document_multi_delete_user";
	String DOCUMENT_READ_USER = "document_read_user";
	String DOCUMENT_COPY_USER = "document_copy_user";

	String MODEL_MANAGER_USER = "model_manager_user";
	String MODEL_READ_USER = "model_read_user";
	String MODEL_CREATE_USER = "model_create_user";
	String MODEL_UPDATE_USER = "model_update_user";
	String MODEL_DELETE_USER = "model_delete_user";

}
