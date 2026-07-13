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
package com.mgmtp.a12.dataservices.rpc.internal.jpa.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;

import com.mgmtp.a12.dataservices.rpc.internal.jpa.entity.RequestIdEntity;

import static com.mgmtp.a12.dataservices.rpc.internal.jpa.entity.RequestIdEntity.REQUEST_ID_TABLE;

public interface RequestIdRepository extends JpaRepository<RequestIdEntity, String> {

	/**
	 * Enforce DB INSERT operation to check UNIQUE constraint. Native JPA support doesn't distinguish between UPDATE
	 * and INSERT and use unified save() action which is not applicable here because proper action is decided
	 * by existence of ID in the entity. In our case the ID is created outside the DB, so it is supplied also
	 * for CREATE which will lead to unwanted consequences.
	 *
	 * @param id ID of the request
	 */
	@Modifying
	// dummy countQuery needed because of the spring bug https://github.com/spring-projects/spring-boot/issues/34363
	@NativeQuery(value = "insert into " + REQUEST_ID_TABLE + " (id, state) values (:id, 'PENDING')", countQuery = "select count(id) from "
		+ REQUEST_ID_TABLE + " where id=:id")
	void insert(@Param("id") String id);

	/**
	 * Get all table entries that were created before passed instant in time.
	 *
	 * @param treshold the instant in time before which entries should be returned
	 * @return the list of table entries
	 */
	List<RequestIdEntity> findByTimestampBefore(Instant threshold);

}
