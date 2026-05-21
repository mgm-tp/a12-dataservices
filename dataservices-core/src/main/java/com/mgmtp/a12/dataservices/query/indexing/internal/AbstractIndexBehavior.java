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
package com.mgmtp.a12.dataservices.query.indexing.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import javax.sql.DataSource;

import org.apache.commons.lang3.time.StopWatch;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.persistence.internal.AggregatedDocumentRepository;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys.ExecutionPhase;
import com.mgmtp.a12.dataservices.exception.query.QueryBatchFailedException;
import com.mgmtp.a12.dataservices.internal.ConcurrentStopWatch;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.DocumentModelFieldsIndexer;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.searchtable.DocumentFieldsJpaRepository;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.facade.DocumentServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractIndexBehavior {

	protected final AggregatedDocumentRepository aggregatedDocumentRepository;
	protected final DocumentFieldsJpaRepository documentFieldsJpaRepository;
	protected final DocumentModelServiceFactory documentModelServiceFactory;
	protected final DocumentModelFieldsIndexer documentModelFieldsIndexer;
	protected final DocumentServiceFactory documentServiceFactory;
	protected final DataSource dataSource;

	@Getter protected final ConcurrentStopWatch concurrentStopWatch = new ConcurrentStopWatch();

	protected abstract Map<String, byte[]> convertToCsv(List<DataServicesDocument> dataServicesDocuments,
		IDocumentModelSearchService documentModelSearchService, BiFunction<String, String, Long> fieldTypeIdProvider) throws IOException;

	protected abstract String getCopySql(String name);

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void indexBatch(@NonNull List<DocumentReference> documents, IDocumentModelSearchService documentModelSearchService,
		BiFunction<String, String, Long> fieldTypeIdProvider) {
		try {
			Connection connection = DataSourceUtils.getConnection(dataSource);
			StopWatch stopwatch = log.isDebugEnabled() ? concurrentStopWatch.getStarted("wholeBatch") : null;
			StopWatch loadingStopWatch = log.isDebugEnabled() ? concurrentStopWatch.getStarted("loading") : null;

			List<DataServicesDocument> documentsByDocRefs = aggregatedDocumentRepository
				.findDocumentsByDocRefs(documents);

			if (loadingStopWatch != null) {
				loadingStopWatch.stop();
			}

			StopWatch preprocessorStopWatch = log.isDebugEnabled() ? concurrentStopWatch.getStarted("preprocessing") : null;

			Map<String, byte[]> csv = convertToCsv(documentsByDocRefs, documentModelSearchService, fieldTypeIdProvider);

			if (preprocessorStopWatch != null) {
				preprocessorStopWatch.stop();
			}

			CopyManager copyManager = new CopyManager(connection.unwrap(BaseConnection.class));

			StopWatch saveStopWatch = log.isDebugEnabled() ? concurrentStopWatch.getStarted("saving") : null;

			for (Map.Entry<String, byte[]> entry : csv.entrySet()) {
				try (ByteArrayInputStream inputStream = new ByteArrayInputStream(entry.getValue())) {
					copyManager.copyIn(getCopySql(entry.getKey()), inputStream);
				}
			}

			if (saveStopWatch != null) {
				saveStopWatch.stop();
			}

			if (stopwatch != null) {
				stopwatch.stop();
			}

			log.atDebug().setMessage("Batch executed successfully with {} documents of {} in {} [{}%, {}%, {}%]")
				.addArgument(documentsByDocRefs::size)
				.addArgument(() -> documents.stream()
					.map(DocumentReference::getDocumentModelName)
					.findFirst()
					.orElse("N/A"))
				.addArgument(() -> Objects.requireNonNull(stopwatch).formatTime())
				.addArgument(() -> Objects.requireNonNull(loadingStopWatch).getDuration().dividedBy(stopwatch.getDuration().dividedBy(100)))
				.addArgument(() -> Objects.requireNonNull(preprocessorStopWatch).getDuration().dividedBy(stopwatch.getDuration().dividedBy(100)))
				.addArgument(() -> Objects.requireNonNull(saveStopWatch).getDuration().dividedBy(stopwatch.getDuration().dividedBy(100)))
				.log();
		} catch (Exception e) {
			log.debug(e.getMessage(), e);
			throw new QueryBatchFailedException(ExecutionPhase.QUERY_INDEXING, e.getMessage(), documents, e).withAnonymityMessage("Batch indexing failed.");
		}
	}
}
