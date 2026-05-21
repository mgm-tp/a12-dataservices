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
package com.mgmtp.a12.dataservices.document.persistence.internal;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mgmtp.a12.dataservices.document.internal.entity.DocumentEntity;

/**
 * Lowest JPA level API which should be only used by upper level extendable API
 */
public interface DocumentJpaRepository extends JpaRepository<DocumentEntity, String> {

	String QUERY_DOC_BY_MODEL_NAME = "SELECT d.id FROM DocumentEntity d WHERE d.modelName = :modelName";

	@Query(value = QUERY_DOC_BY_MODEL_NAME) List<String> findIdByModelName(@Param("modelName") String modelName);

	@Query(value = QUERY_DOC_BY_MODEL_NAME) List<String> findIdByModelName(@Param("modelName") String modelName, Pageable pageable);

	List<DocumentEntity> findByModelName(String modelName);

	List<DocumentEntity> findByIdInOrderById(List<String> ids);

	void deleteByModelNameAndId(String modelName, String id);

	void deleteAllByModelNameAndIdIn(String modelName, List<String> ids);

}
