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
package com.mgmtp.a12.dataservices.autoconfigure;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;

import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.configuration.internal.validation.condition.KernelDocumentServiceCondition;
import com.mgmtp.a12.dataservices.configuration.internal.validation.condition.OnEnabledJobCondition;
import com.mgmtp.a12.dataservices.document.internal.kernel.KernelDocumentService;
import com.mgmtp.a12.dataservices.document.support.DocumentSupport;
import com.mgmtp.a12.dataservices.document.support.internal.DefaultDocumentSupport;
import com.mgmtp.a12.dataservices.initialization.BusinessModelInitializer;
import com.mgmtp.a12.dataservices.initialization.DataServicesInitializationListener;
import com.mgmtp.a12.dataservices.initialization.InitializationService;
import com.mgmtp.a12.dataservices.initialization.internal.DataServicesInitializationService;
import com.mgmtp.a12.dataservices.initialization.internal.JsonRpcInitializer;
import com.mgmtp.a12.dataservices.query.indexing.QueryIndexManager;
import com.mgmtp.a12.dataservices.migration.internal.MigrationRunner;
import com.mgmtp.a12.dataservices.relationship.internal.ranks.DefragmentRanksJob;
import com.mgmtp.a12.dataservices.relationship.internal.ranks.RelationshipRankService;
import com.mgmtp.a12.dataservices.rpc.CleanUpRequestIdJob;
import com.mgmtp.a12.dataservices.rpc.internal.RequestIdService;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeFactory;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationConfig;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.rt.api.ICustomConditionFactory;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentRtService;
import com.mgmtp.a12.uaa.authentication.backend.BackendAuthenticationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service Factory configuration for DataServices.
 *
 * Provides and wires core service beans (initialization, jobs, document support, kernel services)
 * used across the DataServices module.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class ServiceFactoryConfiguration {

	/**
	 * Quartz group name used for attachment-related jobs.
	 */
	public static final String ATTACHMENTS = "attachments";
	/**
	 * Log message template used when a Quartz trigger is registered with a job.
	 */
	public static final String REGISTERED_QUARTZ_TRIGGER_WITH_JOB = "Registered QUARTZ trigger {} with job {}";
	private final DataServicesCoreProperties dataServicesCoreProperties;
	private final IDocumentModelResolver documentModelResolver;

	/**
	 * Registers a listener that reacts to DataServices initialization events.
	 *
	 * @return The {@link DataServicesInitializationListener}.
	 */
	@Bean public DataServicesInitializationListener dataServicesInitializationListener() {
		return new DataServicesInitializationListener();
	}


	/**
	 * Provides the {@link InitializationService} orchestrating migrations, model initialization and index setup.
	 *
	 * @param migrationRunner Runs database and relationship migrations.
	 * @param businessModelInitializer Initializes business models.
	 * @param applicationEventPublisher Publishes initialization events.
	 * @param backendAuthenticationService Performs backend authentication for privileged operations.
	 * @param requestIdService Manages request IDs used by RPC.
	 * @param jsonRpcInitializer Optional initializer for JSON-RPC endpoints.
	 * @param customConditionFactories Additional custom condition factories.
	 * @param customFieldTypeFactories Additional custom field type factories.
	 * @param queryIndexManager Manages query index updates.
	 * @return The configured {@link InitializationService}.
	 */
	@Bean public InitializationService dataServicesInitializationService(MigrationRunner migrationRunner,
		BusinessModelInitializer businessModelInitializer, ApplicationEventPublisher applicationEventPublisher,
		BackendAuthenticationService backendAuthenticationService, RequestIdService requestIdService, Optional<JsonRpcInitializer> jsonRpcInitializer,
		List<ICustomConditionFactory> customConditionFactories, List<ICustomFieldTypeFactory> customFieldTypeFactories, QueryIndexManager queryIndexManager) {

		return new DataServicesInitializationService(dataServicesCoreProperties, migrationRunner, businessModelInitializer,
			applicationEventPublisher, backendAuthenticationService, requestIdService, jsonRpcInitializer, customConditionFactories, customFieldTypeFactories,
			queryIndexManager);
	}

	/**
	 * Job to clean up table REQUEST_ID by deleting entries which are older than the time configured in `mgmtp.a12.dataservices.jobs.requests.cleanupRequestId.expireHours`.
	 * See {@link CleanUpRequestIdJob}.
	 *
	 * @return The job details wrapped in a {@link JobDetail} object
	 * @scheduler cleanUpRequestIdJob
	 * @default `${mgmtp.a12.dataservices.jobs.requests.cleanUpRequestId.schedule}`
	 */
	@Conditional(OnEnabledJobCondition.class)
	@Bean public JobDetail cleanUpRequestIdJobDetail() {
		return JobBuilder.newJob()
			.ofType(CleanUpRequestIdJob.class)
			.withIdentity("cleanUpRequestIdJob", "requests")
			.withDescription("Clean up table request_id job.")
			.storeDurably()
			.build();
	}

	/**
	 * Creates the Quartz trigger for {@link CleanUpRequestIdJob}.
	 *
	 * @param cleanUpRequestIdJobDetail The job detail for cleaning up request IDs.
	 * @return The configured Quartz {@link Trigger}.
	 */
	@Conditional(OnEnabledJobCondition.class)
	@Bean public Trigger cleanUpRequestIdTrigger(JobDetail cleanUpRequestIdJobDetail) {
		Trigger trigger = TriggerBuilder.newTrigger()
			.forJob(cleanUpRequestIdJobDetail)
			.withIdentity("cleanRequestIdTrigger", "requests")
			.withDescription("Clean up table request_id job trigger.")
			.withSchedule(CronScheduleBuilder.cronSchedule(dataServicesCoreProperties.getJobs().getRequests().getCleanupRequestId().getSchedule()))
			.usingJobData(CleanUpRequestIdJob.REQUEST_ID_EXPIRE_HOURS,
				dataServicesCoreProperties.getJobs().getRequests().getCleanupRequestId().getExpireHours())
			.startNow()
			.build();
		log.info(REGISTERED_QUARTZ_TRIGGER_WITH_JOB, trigger, cleanUpRequestIdJobDetail);
		return trigger;
	}

	/**
	 * Exposes the {@link CleanUpRequestIdJob} bean.
	 *
	 * @return A new instance of {@link CleanUpRequestIdJob}.
	 */
	@Conditional(OnEnabledJobCondition.class)
	@Bean public CleanUpRequestIdJob cleanUpRequestIdJob() {
		return new CleanUpRequestIdJob();
	}

	/**
	 * Creates and registers the Quartz trigger for {@link DefragmentRanksJob}.
	 *
	 * @param defragmentRanksJobDetail The job detail of the defragmentation job.
	 * @param applicationContext The application context used to register the trigger bean.
	 * @return The configured Quartz {@link Trigger}.
	 */
	@ConditionalOnBean(name = "defragmentRanksJobDetail", value = JobDetail.class)
	@Bean public Trigger defragmentRanksJobTrigger(JobDetail defragmentRanksJobDetail, GenericApplicationContext applicationContext) {
		String triggerBeanName = "DefragmentRanks_trigger";
		DataServicesCoreProperties.Jobs.RelationshipJobs.RankRecalculation rankRecalculation =
			dataServicesCoreProperties.getJobs().getRelationships().getRankRecalculation();

		if (!rankRecalculation.isEnabled()) {
			log.info("DefragmentRanks job is not enabled.");
		}
		applicationContext.registerBean(triggerBeanName, Trigger.class, () -> {
			TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
				.forJob(defragmentRanksJobDetail)
				.withIdentity(triggerBeanName, "links")
				.withDescription("Defragment ranks");
			if (StringUtils.isNotBlank(rankRecalculation.getSchedule())) {
				triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(rankRecalculation.getSchedule()));
			} else {
				triggerBuilder.startAt(Date.from(Instant.EPOCH)).endAt(Date.from(Instant.EPOCH)); // disable the trigger if no schedule provided
				if (rankRecalculation.isEnabled()) {
					log.warn("No schedule provided for DefragmentRanks => job is disabled.");
				}
			}
			return triggerBuilder.build();
		});
		Trigger trigger = (Trigger) applicationContext.getBean(triggerBeanName);
		log.info(REGISTERED_QUARTZ_TRIGGER_WITH_JOB, trigger, defragmentRanksJobDetail);
		return trigger;
	}

	/**
	 * Job to defragment ranks for relationship links.
	 *
	 * @scheduler defragmentRanksJob
	 * @default `${mgmtp.a12.dataservices.link.rankRecalculateScheduler.defragmentSchedule}`
	 */
	@Conditional({ OnEnabledJobCondition.class })
	@Bean public JobDetail defragmentRanksJobDetail() {
		JobBuilder jobBuilder = JobBuilder.newJob()
			.ofType(DefragmentRanksJob.class)
			.withIdentity("defragmentRanksJob", "links")
			.withDescription("Defragment ranks job.");
		DataServicesCoreProperties.Jobs.RelationshipJobs.RankRecalculation rankRecalculateScheduler = dataServicesCoreProperties
			.getJobs().getRelationships().getRankRecalculation();
		String rmToReorder = rankRecalculateScheduler.getRmsToReorder() == null ? ""
			: String.join(",", rankRecalculateScheduler.getRmsToReorder());
		if (StringUtils.isNotBlank(rmToReorder)) {
			jobBuilder
				.usingJobData(DefragmentRanksJob.RM_TO_REORDER, rmToReorder)
				.usingJobData(DefragmentRanksJob.ENABLED, rankRecalculateScheduler.isEnabled());
		} else {
			jobBuilder.usingJobData(DefragmentRanksJob.ENABLED, false);
			if (rankRecalculateScheduler.isEnabled()) {
				log.warn("No relationship models specified for rank recalculating => disabling the job.");
			}
		}
		return jobBuilder.storeDurably().build();
	}

	/**
	 * Exposes the {@link DefragmentRanksJob} bean.
	 *
	 * @param rankService Rank service used to recalculate and compact ranks.
	 * @param coreProperties Core properties controlling job behavior.
	 * @param authenticationService Backend authentication for privileged execution.
	 * @return A new instance of {@link DefragmentRanksJob}.
	 */
	@Conditional(OnEnabledJobCondition.class)
	@Bean public DefragmentRanksJob defragmentRanksJob(RelationshipRankService rankService, DataServicesCoreProperties coreProperties,
		BackendAuthenticationService authenticationService) {
		return new DefragmentRanksJob(rankService, coreProperties, authenticationService);
	}

	/**
	 * Provides the {@link KernelDocumentService} configured from DataServices properties.
	 *
	 * @param rtService Runtime service for document computation and validation.
	 * @param documentModelResolver Resolves document models by identifier.
	 * @return The configured {@link KernelDocumentService}.
	 */
	@Conditional(KernelDocumentServiceCondition.class)
	@Bean public KernelDocumentService kernelDocumentService(IDocumentRtService rtService, IDocumentModelResolver documentModelResolver) {
		final DataServicesCoreProperties.Document documentProperties = dataServicesCoreProperties.getDocuments();
		DataServicesCoreProperties.Document.Validation validation = documentProperties.getValidation();
		DataServicesCoreProperties.Document.Computation computation = documentProperties.getComputation();

		return new KernelDocumentService(validation.isEnabled(), validation.getPartialForModels(), validation.getSkipForModels(),
			computation.getEnabledForModels(),
			rtService, documentModelResolver, computation.getCleanupErrorAndNotComputedValue().isEnabled());
	}

	/**
	 * Exposes the default {@link DocumentSupport} with JSON and V2 serialization support.
	 *
	 * @param documentJsonDeserializationConfig JSON deserialization configuration.
	 * @param documentJsonSerializationConfig JSON serialization configuration.
	 * @param documentV2Serializer Serializer for {@link com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2}.
	 * @return The configured {@link DocumentSupport}.
	 */
	@Bean public DocumentSupport defaultDocumentSupport(DocumentDeserializationConfig documentJsonDeserializationConfig,
		DocumentSerializationConfig documentJsonSerializationConfig, IDocumentV2Serializer documentV2Serializer) {
		return new DefaultDocumentSupport(documentJsonDeserializationConfig, documentJsonSerializationConfig, documentModelResolver, documentV2Serializer);
	}
}
