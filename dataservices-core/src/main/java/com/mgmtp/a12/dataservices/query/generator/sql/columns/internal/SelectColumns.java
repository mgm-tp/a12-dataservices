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
package com.mgmtp.a12.dataservices.query.generator.sql.columns.internal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorContext;
import com.mgmtp.a12.dataservices.query.generator.sql.internal.SqlGeneratorHelpersInternal;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class SelectColumns {

	private final List<Column> columns = new ArrayList<>();
	private final Set<Table> tables = new HashSet<>();
	@Getter private final QueryGeneratorContext queryGeneratorContext;

	public Table withTable(CharSequence alias, CharSequence name) {
		return withTable(alias, name, true);
	}

	public Table withTable(CharSequence name, boolean schemaAware) {
		return withTable(name, name, schemaAware);
	}

	private Table withTable(CharSequence alias, CharSequence name, boolean schemaAware) {
		Table t = new Table(alias, name, schemaAware);
		tables.add(t);
		return t;
	}

	public SelectColumns column(CharSequence alias, CharSequence name) {
		columns.add(new Column(alias, name, null));
		return this;
	}

	public SelectColumns column(CharSequence alias, CharSequence name, CharSequence type) {
		columns.add(new Column(alias, name, type));
		return this;
	}

	public StringBuilder renderColumns(StringBuilder sb, QueryGeneratorContext generatorContext) {
		for (Iterator<Column> iterator = columns.iterator(); iterator.hasNext(); ) {
			Column c = iterator.next();
			c.render(sb);
			if (iterator.hasNext()) {
				sb.append(',');
			}
		}

		return sb;
	}

	public StringBuilder renderTables(StringBuilder sb, QueryGeneratorContext generatorContext) {
		for (Iterator<Table> iterator = tables.iterator(); iterator.hasNext(); ) {
			Table t = iterator.next();
			if (t != null) {
				if (t.schemaAware) {
					SqlGeneratorHelpersInternal.renderSchema(sb, queryGeneratorContext.getSchema());
				}
				t.render(sb);
				if (iterator.hasNext()) {
					sb.append(',');
				}
			}
		}
		return sb;
	}

	@RequiredArgsConstructor
	static class Column {

		private final CharSequence alias;
		private final CharSequence name;
		private final CharSequence type;

		public CharSequence render(StringBuilder sb) {
			sb.append(name);
			if (StringUtils.isNoneBlank(type)) {
				sb.append(QueryGeneratorConstants.CAST_OPERATOR)
					.append(type);
			}
			sb.append(QueryGeneratorConstants.AS_KEYWORD)
				.append(alias);
			return sb;
		}
	}

	@RequiredArgsConstructor
	public class Table implements Comparable<Table> {

		@Getter(AccessLevel.PRIVATE) private final CharSequence alias;
		private final CharSequence name;
		private final boolean schemaAware;

		public CharSequence render(StringBuilder sb) {
			if (StringUtils.isNotBlank(name) && !Objects.equals(name, alias)) {
				sb.append(name);
				sb.append(QueryGeneratorConstants.AS_KEYWORD);
			}
			return sb.append(alias);
		}

		public Table column(CharSequence alias, CharSequence name) {
			SelectColumns.this.column(alias, columnWithTable(name));
			return this;
		}

		public Table column(CharSequence name) {
			SelectColumns.this.column(name, columnWithTable(name));
			return this;
		}

		public Table column(CharSequence alias, CharSequence name, CharSequence type) {
			SelectColumns.this.column(alias, columnWithTable(name), type);
			return this;
		}

		public SelectColumns end() {
			return SelectColumns.this;
		}

		private CharSequence columnWithTable(CharSequence column) {
			return new StringBuilder()
				.append(alias)
				.append('.')
				.append(column);
		}

		@Override public int compareTo(Table o) {
			return Comparator.comparing(Table::getAlias, Comparator.comparing(CharSequence::toString)).compare(o, this);
		}

		@Override public boolean equals(Object obj) {
			return obj instanceof Table t && Objects.equals(t.getAlias(), getAlias());
		}

		@Override public int hashCode() {
			return Objects.hashCode(getAlias());
		}
	}

	public abstract static class SelectColumnsBuilder<C extends SelectColumns, B extends SelectColumnsBuilder<C, B>> {

	}
}
