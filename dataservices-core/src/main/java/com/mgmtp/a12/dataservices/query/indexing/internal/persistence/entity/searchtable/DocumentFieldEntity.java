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
package com.mgmtp.a12.dataservices.query.indexing.internal.persistence.entity.searchtable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.Immutable;

import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.ColumnNames;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.TableNames;

import io.hypersistence.utils.hibernate.type.range.PostgreSQLRangeType;
import io.hypersistence.utils.hibernate.type.range.Range;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder(toBuilder = true) @AllArgsConstructor @NoArgsConstructor
@Immutable
@Table(name = TableNames.DOCUMENT_FIELDS_TABLE_NAME)
@Entity public class DocumentFieldEntity implements Serializable {

	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id @Column(name = ColumnNames.ID_COLUMN_NAME) private Long id;

	@Column(name = ColumnNames.MODEL_NAME_COLUMN_NAME) private String modelName;

	@Column(name = ColumnNames.DOC_REF_COLUMN_NAME)
	private String docRef;

	@Column(name = ColumnNames.FIELD_NAME_COLUMN_NAME) private String fieldName;

	@Column(name = ColumnNames.REPETITIONS_COLUMN_NAME)
	private int[] repetitions;

	@Column(name = ColumnNames.FIELD_TYPE_ID_COLUMN_NAME) private Long fieldTypeId;

	@Column(name = QueryGeneratorConstants.ColumnNames.FIELD_TYPE_COLUMN_NAME) String fieldType;

	@Column(name = ColumnNames.VALUE_COLUMN_NAME, columnDefinition = QueryGeneratorConstants.TEXT_TYPE) @JdbcTypeCode(SqlTypes.VARCHAR) @Lob
	private String value;

	@Column(name = ColumnNames.TYPED_VALUE_COLUMN_NAME, columnDefinition = QueryGeneratorConstants.TEXT_TYPE) @JdbcTypeCode(SqlTypes.VARCHAR)
	private String typedValue;

	@Column(name = ColumnNames.NUMBER_VALUE_COLUMN_NAME)
	private BigDecimal numberValue;

	@Column(name = ColumnNames.TIMESTAMP_VALUE_COLUMN_NAME)
	private LocalDateTime timestampValue;

	@Type(PostgreSQLRangeType.class)
	@Column(
		name = ColumnNames.TS_RANGE_VALUE_COLUMN_NAME,
		columnDefinition = "tsrange"
	)
	private Range<LocalDateTime> tsRangeValue;

	@Column(name = ColumnNames.SOURCE_COLUMN_NAME)
	private String source;
}
