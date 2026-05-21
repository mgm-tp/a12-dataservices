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
package com.mgmtp.a12.dataservices.initialization.internal;

import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.ApplicationEventPublisher;

import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.initialization.BusinessModelInitializer;
import com.mgmtp.a12.dataservices.initialization.InitializationService;
import com.mgmtp.a12.dataservices.initialization.events.DataServicesCustomInitializationEvent;
import com.mgmtp.a12.dataservices.query.indexing.QueryIndexManager;
import com.mgmtp.a12.dataservices.migration.internal.MigrationRunner;
import com.mgmtp.a12.dataservices.rpc.internal.RequestIdService;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeFactory;
import com.mgmtp.a12.kernel.md.facade.DocumentRtCustomExtensionService;
import com.mgmtp.a12.kernel.md.rt.api.ICustomConditionFactory;
import com.mgmtp.a12.uaa.authentication.backend.BackendAuthenticationService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Gathers all steps of the initialization sequence for the application.
 * Is called either from InitApp, of from StandaloneApplication.
 * In a cluster - based application, this should be skipped and the init task should be relayed to the InitApp.
 *
 * There is no wrapping transaction for the whole initialization task.
 * Every init step is responsible for its own transaction handling.
 */
@Slf4j @RequiredArgsConstructor
public class DataServicesInitializationService implements InitializationService {

	public static final String ERROR_MESSAGE_TEMPLATE = "Application initialization: {} Initialization is aborted at this step.";

	@NonNull private final DataServicesCoreProperties dataServicesCoreProperties;
	private final MigrationRunner migrationRunner;
	private final BusinessModelInitializer businessModelInitializer;
	@NonNull private final ApplicationEventPublisher applicationEventPublisher;
	@NonNull private final BackendAuthenticationService backendAuthenticationService;
	private final RequestIdService requestIdService;
	private final Optional<JsonRpcInitializer> jsonRpcInitializer;
	private final List<ICustomConditionFactory> customConditionFactories;
	private final List<ICustomFieldTypeFactory> customFieldTypeFactories;
	private final QueryIndexManager queryIndexManager;

	/**
	 * Runs all initialization steps.
	 * It should be called from outside any existing transactions, so all transactionality and rollback will be handled properly.
	 *
	 * @event {@link DataServicesCustomInitializationEvent}
	 */
	@Override public void runInitialization() {
		initializeKernel();
		// This authentication is based on overriding UAA properties
		// mgmtp.a12.uaa.authentication.backend.enabled=true
		// mgmtp.a12.uaa.authentication.backend.grant-super-user-privileges.enabled=true
		backendAuthenticationService.executeWithBackendAuthentication(
			dataServicesCoreProperties.getAuthorization().getBackendJob().getPrincipal().getUsername(),
			() -> {
				cleanRequestIdsConditionally();
				importModelsConditionally();
				runMigrationConditionally();

				queryIndexManager.indexQuery();

				try {
					applicationEventPublisher.publishEvent(new DataServicesCustomInitializationEvent());
					runJsonRpcConditionally();
				} catch (RuntimeException e) {
					rethrowWithLog(e,
						"Error occurred during initialization phase after reindexing. Database was rollbacked, but the index could stay inconsistent. Rebuild of the index is required!");
				}

				return null;
			}
		);
	}

	private void initializeKernel() {
		if (CollectionUtils.isNotEmpty(customConditionFactories) || CollectionUtils.isNotEmpty(customFieldTypeFactories)) {
			DocumentRtCustomExtensionService kernelExtensionService = new DocumentRtCustomExtensionService();
			if (customFieldTypeFactories != null) {
				customFieldTypeFactories.forEach(kernelExtensionService::registerCustomFieldTypesV2);
			}
			if (customConditionFactories != null) {
				customConditionFactories.forEach(kernelExtensionService::registerCustomConditionsV2);
			}
		}
	}

	private void importModelsConditionally() {
		try {
			if (dataServicesCoreProperties.getInitialization().getImport().getModels().isEnabled()) {
				businessModelInitializer.importBusinessModels();
			} else {
				log.info("Models import disabled by configuration: mgmtp.a12.dataservices.initialization.import.models.enabled");
			}
		} catch (RuntimeException e) {
			rethrowWithLog(e, "Importing of models failed.");
		}
	}

	private void cleanRequestIdsConditionally() {
		try {
			if (dataServicesCoreProperties.getInitialization().getCleanUpRequestId().isEnabled()) {
				requestIdService.cleanRequestIds();
			}
		} catch (RuntimeException e) {
			rethrowWithLog(e, "Cleaning of request IDs failed.");
		}
	}

	private void runMigrationConditionally() {
		try {
			if (dataServicesCoreProperties.getInitialization().getMigration().isEnabled()) {
				migrationRunner.migrate();
			} else {
				log.info("Migration tasks disabled by configuration: mgmtp.a12.dataservices.initialization.migration.enabled");
			}
		} catch (RuntimeException e) {
			rethrowWithLog(e, "Running of migration failed.");
		}
	}

	private void runJsonRpcConditionally() {
		if (dataServicesCoreProperties.getInitialization().getScripts().getJsonRpc().isEnabled()
			&& dataServicesCoreProperties.getJsonRpc().isEnabled()) {
			// if jsonRpcInitializer is empty, then it is invalid application state
			jsonRpcInitializer.orElseThrow(() -> new IllegalStateException("RPC is enabled but there is no bean for jsonRpcInitializer present"))
				.execute();
		} else {
			log.info("Initialization of JSON-RPC requests disabled by configuration: mgmtp.a12.dataservices.initialization.scripts.jsonRpc.enabled");
		}
	}

	private static void rethrowWithLog(RuntimeException e, String message) throws RuntimeException {
		log.error(ERROR_MESSAGE_TEMPLATE, message);
		throw e;
	}
}
