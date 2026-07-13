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
package com.mgmtp.a12.dataservices.authorization;


import com.mgmtp.a12.model.Model;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Evaluating model permission for current users.
 * By default, the interface implementations are internal. We do not recommend you to inject its implementations
 * directly in your bean but rather inject the interface with generic type.
 * [source,java]
 * ----
 * private final ModelPermissionEvaluator<IDocumentModel> documentModelPermissionEvaluator;
 *
 * public MyBean(ModelPermissionEvaluator<IDocumentModel> documentModelPermissionEvaluator) {
 *     this.documentModelPermissionEvaluator = documentModelPermissionEvaluator;
 * }
 * ----
 * [source,java]
 * ----
 * private final ModelPermissionEvaluator<RelationshipModel> relationshipModelPermissionEvaluator;
 *
 * public MyBean(ModelPermissionEvaluator<RelationshipModel> relationshipModelPermissionEvaluator) {
 *     this.relationshipModelPermissionEvaluator = relationshipModelPermissionEvaluator;
 * }
 * ----
 *
 * @param <T> presents different type of model.
 */
@OnlyForUsage public interface ModelPermissionEvaluator<T extends Model> {

	/**
	 * @param header The model headers.
	 * @throws org.springframework.security.access.AccessDeniedException if user does not have Model Create permission.
	 */
	void checkModelCreatePermission(Header header);

	/**
	 *
	 * @param header The model headers.
	 * @throws org.springframework.security.access.AccessDeniedException if user does not have Model Update permission.
	 */
	void checkModelUpdatePermission(Header header);

	/**
	 *
	 * @param header The model headers.
	 * @throws org.springframework.security.access.AccessDeniedException if user does not have Model Delete permission.
	 */
	void checkModelDeletePermission(Header header);

	/**
	 * @param modelId The model id.
	 * @throws org.springframework.security.access.AccessDeniedException if user does not have Model Read permission.
	 */
	void checkModelReadPermission(String modelId);

	/**
	 * @param header The header of the model.
	 * @throws org.springframework.security.access.AccessDeniedException if user does not have Model Read permission.
	 */
	void checkModelReadPermission(Header header);

	/**
	 * @param model The checking model.
	 * @throws org.springframework.security.access.AccessDeniedException if user does not have Model Read permission.
	 */
	void checkModelReadPermission(T model);

	/**
	 * @param modelId The model id.
	 * @return true if user has Model Read permission.
	 */
	boolean hasModelReadPermission(String modelId);

	/**
	 * @param header The model header.
	 * @return true if user has Model Read permission.
	 */
	boolean hasModelReadPermission(Header header);

	/**
	 * @param model The checking model.
	 * @return true if user has Model Read permission.
	 */
	boolean hasModelReadPermission(T model) ;
}
