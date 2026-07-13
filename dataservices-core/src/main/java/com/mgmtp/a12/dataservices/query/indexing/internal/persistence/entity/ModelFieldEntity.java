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
package com.mgmtp.a12.dataservices.query.indexing.internal.persistence.entity;

import java.io.Serializable;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import tools.jackson.databind.JsonNode;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder(toBuilder = true) @AllArgsConstructor @NoArgsConstructor
@Table(name = QueryGeneratorConstants.TableNames.MODEL_FIELDS_TABLE_NAME)
@Entity public class ModelFieldEntity implements Serializable {

	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id @Column(name = QueryGeneratorConstants.ColumnNames.ID_COLUMN_NAME, nullable = false) private long id;

	@Column(name = QueryGeneratorConstants.ColumnNames.MODEL_NAME_COLUMN_NAME) String modelName;

	@Column(name = QueryGeneratorConstants.ColumnNames.FIELD_NAME_COLUMN_NAME) String fieldName;

	@Column(name = QueryGeneratorConstants.ColumnNames.FIELD_TYPE_COLUMN_NAME) String fieldType;

	/**
	 * JSON serialized structure of the `IField` comes into this property.
	 */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = QueryGeneratorConstants.ColumnNames.DATA_COLUMN_NAME) JsonNode data;

	/**
	 * Builder for {@link ModelFieldEntity}. Declared explicitly so that references to this type
	 * are visible to the javadoc compiler, which does not run Lombok annotation processors.
	 * Lombok populates the builder methods at compile time.
	 */
	public static class ModelFieldEntityBuilder {
	}
}
