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
package com.mgmtp.a12.dataservices.autoconfigure.attachments;

import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.mapstruct.factory.Mappers;
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
import org.springframework.context.annotation.Import;

import com.mgmtp.a12.dataservices.attachment.IDirtyAttachmentCleanupCondition;
import com.mgmtp.a12.dataservices.attachment.header.AttachmentHeaderService;
import com.mgmtp.a12.dataservices.attachment.internal.AttachmentMapper;
import com.mgmtp.a12.dataservices.attachment.internal.CleanUpDirtyAttachmentsJob;
import com.mgmtp.a12.dataservices.attachment.internal.CleanUpStaleAttachmentsJob;
import com.mgmtp.a12.dataservices.attachment.internal.DefaultAttachmentHeaderService;
import com.mgmtp.a12.dataservices.attachment.internal.DefaultAttachmentService;
import com.mgmtp.a12.dataservices.attachment.internal.DirtyAttachmentMapper;
import com.mgmtp.a12.dataservices.attachment.internal.DirtyAttachmentService;
import com.mgmtp.a12.dataservices.attachment.internal.ThumbnailUrlGenerator;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.repository.AttachmentHeaderJpaRepository;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.repository.AttachmentReferenceJpaRepository;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.repository.DirtyAttachmentJpaRepository;
import com.mgmtp.a12.dataservices.attachment.persitence.AttachmentHeaderRepository;
import com.mgmtp.a12.dataservices.attachment.persitence.IAttachmentRepository;
import com.mgmtp.a12.dataservices.attachment.persitence.internal.DefaultAttachmentHeaderRepository;
import com.mgmtp.a12.dataservices.attachment.persitence.internal.ThumbnailUtil;
import com.mgmtp.a12.dataservices.authorization.AttachmentPermissionEvaluator;
import com.mgmtp.a12.dataservices.authorization.DocumentPermissionEvaluator;
import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.authorization.internal.DefaultAttachmentPermissionEvaluator;
import com.mgmtp.a12.dataservices.autoconfigure.attachments.internal.contentstore.ContentStoreConfiguration;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.configuration.internal.validation.condition.OnEnabledJobCondition;
import com.mgmtp.a12.dataservices.configuration.internal.validation.condition.attachment.OnEnabledAttachmentCondition;
import com.mgmtp.a12.dataservices.document.internal.attachment.AttachmentSupport;
import com.mgmtp.a12.dataservices.document.persistence.IDocumentRepository;
import com.mgmtp.a12.dataservices.document.persistence.internal.AttachmentHandler;
import com.mgmtp.a12.dataservices.query.QueryService;
import com.mgmtp.a12.dataservices.reference.GenericReference;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.facade.DocumentServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.uaa.authorization.AuthorizationService;

import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.autoconfigure.ServiceFactoryConfiguration.ATTACHMENTS;
import static com.mgmtp.a12.dataservices.autoconfigure.ServiceFactoryConfiguration.REGISTERED_QUARTZ_TRIGGER_WITH_JOB;

/**
 * Attachment service configuration for DataServices.
 *
 * Wires attachment repositories, services, mappers, permission evaluation, thumbnail utilities,
 * and maintenance jobs (dirty/stale cleanups).
 */
@Conditional(OnEnabledAttachmentCondition.class)
@Import({ ContentStoreConfiguration.class })
@RequiredArgsConstructor
@Slf4j
@Configuration public class AttachmentConfiguration {
	private final IDocumentModelResolver documentModelResolver;
	private final DocumentServiceFactory documentServiceFactory;
	private final DataServicesCoreProperties dataServicesCoreProperties;
	private final AttachmentReferenceJpaRepository attachmentReferenceJpaRepository;
	private final AttachmentHeaderJpaRepository attachmentHeaderJpaRepository;
	private final ApplicationEventPublisher eventPublisher;
	private final List<IDocumentRepository> documentRepositories;
	private final AuthorizationService authorizationService;
	private final DocumentPermissionEvaluator documentPermissionEvaluator;
	private final ModelPermissionEvaluator<IDocumentModel> modelPermissionEvaluator;
	private final RetryRegistry attachmentRetryRegistry;
	private final DirtyAttachmentJpaRepository dirtyAttachmentJpaRepo;
	private final DocumentModelServiceFactory documentModelServiceFactory;

	private final Optional<IDirtyAttachmentCleanupCondition> attachmentCleanupCondition;
	private final ThumbnailUrlGenerator thumbnailUrlGenerator;

	/**
	 * Provides support utilities for attachment handling within documents.
	 *
	 * @return The {@link AttachmentSupport}.
	 */
	@Bean public AttachmentSupport attachmentSupport() {
		return new AttachmentSupport(documentModelResolver, documentModelServiceFactory);
	}

	/**
	 * Provides the attachment handler to apply attachment operations to documents.
	 *
	 * @param attachmentHeaderRepository Repository for attachment headers.
	 * @param dirtyAttachmentService Service handling dirty attachment records.
	 * @return The {@link AttachmentHandler}.
	 */
	@Bean public AttachmentHandler attachmentHandler(AttachmentHeaderRepository attachmentHeaderRepository, DirtyAttachmentService dirtyAttachmentService) {
		return new AttachmentHandler(attachmentHeaderService(attachmentHeaderRepository, dirtyAttachmentService), attachmentSupport());
	}

	/**
	 * Mapper for attachment entities and DTOs.
	 *
	 * @return The {@link AttachmentMapper}.
	 */
	@Bean public AttachmentMapper attachmentMapper() {
		return Mappers.getMapper(AttachmentMapper.class);
	}

	/**
	 * Mapper for dirty attachment entities.
	 *
	 * @return The {@link DirtyAttachmentMapper}.
	 */
	@Bean public DirtyAttachmentMapper dirtyAttachmentMapper() {
		return Mappers.getMapper(DirtyAttachmentMapper.class);
	}

	/**
	 * Provides the attachment header repository implementation.
	 *
	 * @return The {@link AttachmentHeaderRepository}.
	 */
	@Bean public AttachmentHeaderRepository attachmentHeaderRepository() {
		return new DefaultAttachmentHeaderRepository(
			attachmentHeaderJpaRepository,
			attachmentReferenceJpaRepository,
			attachmentMapper()
		);
	}

	/**
	 * Provides the attachment header service.
	 *
	 * @param attachmentHeaderRepository Repository for attachment headers.
	 * @param dirtyAttachmentService Service for tracking dirty attachments.
	 * @return The {@link AttachmentHeaderService}.
	 */
	@Bean public AttachmentHeaderService attachmentHeaderService(AttachmentHeaderRepository attachmentHeaderRepository,
		DirtyAttachmentService dirtyAttachmentService) {
		return new DefaultAttachmentHeaderService(attachmentHeaderRepository, dirtyAttachmentService);
	}

	/**
	 * Provides the service managing dirty attachment records.
	 *
	 * @param dirtyAttachmentMapper Mapper for dirty attachments.
	 * @return The {@link DirtyAttachmentService}.
	 */
	@Bean public DirtyAttachmentService dirtyAttachmentService(DirtyAttachmentMapper dirtyAttachmentMapper) {
		return new DirtyAttachmentService(dirtyAttachmentJpaRepo, dirtyAttachmentMapper, dataServicesCoreProperties);
	}

	/**
	 * Provides the default attachment service handling persistence, permissions, and thumbnail generation.
	 *
	 * @param attachmentRepository Repository for attachment contents and metadata.
	 * @param attachmentHeaderRepository Repository for attachment headers.
	 * @param dirtyAttachmentService Service for tracking and cleaning dirty attachments.
	 * @param queryService Query service used to locate references/documents.
	 * @return The {@link DefaultAttachmentService}.
	 */
	@Bean
	public DefaultAttachmentService defaultAttachmentService(IAttachmentRepository attachmentRepository, AttachmentHeaderRepository attachmentHeaderRepository,
		DirtyAttachmentService dirtyAttachmentService, QueryService queryService) {
		System.setProperty("thumbnailator.conserveMemoryWorkaround",
			String.valueOf(
				dataServicesCoreProperties.getAttachments().getThumbnail().getGeneration().getThumbnailator().getConserveMemoryWorkaround().isEnabled()));
		ImageIO.setUseCache(dataServicesCoreProperties.getAttachments().getThumbnail().getGeneration().getImageDiskCache().isEnabled());
		return new DefaultAttachmentService(
			attachmentRepository,
			attachmentHeaderService(attachmentHeaderRepository, dirtyAttachmentService),
			dataServicesCoreProperties,
			eventPublisher,
			documentRepositories,
			attachmentPermissionEvaluator(),
			modelPermissionEvaluator,
			attachmentRetryRegistry,
			thumbnailUrlGenerator,
			queryService
		);
	}

	/**
	 * Exposes the job that cleans up dirty (unreferenced) attachments.
	 *
	 * @param defaultAttachmentService Default attachment service.
	 * @param attachmentHeaderRepository Repository for attachment headers.
	 * @param dirtyAttachmentService Service managing dirty attachments.
	 * @return The {@link CleanUpDirtyAttachmentsJob}.
	 */
	@Conditional(OnEnabledJobCondition.class)
	@Bean public CleanUpDirtyAttachmentsJob cleanUpDirtyAttachmentsJob(DefaultAttachmentService defaultAttachmentService,
		AttachmentHeaderRepository attachmentHeaderRepository, DirtyAttachmentService dirtyAttachmentService) {
		return new CleanUpDirtyAttachmentsJob(
			dirtyAttachmentService,
			defaultAttachmentService,
			attachmentHeaderService(attachmentHeaderRepository, dirtyAttachmentService),
			attachmentCleanupCondition,
			dataServicesCoreProperties
		);
	}

	/**
	 * Exposes the job that cleans up stale temporary attachments based on expiry configuration.
	 *
	 * @param defaultAttachmentService Default attachment service.
	 * @param attachmentHeaderRepository Repository for attachment headers.
	 * @param dirtyAttachmentService Service managing dirty attachments.
	 * @return The {@link CleanUpStaleAttachmentsJob}.
	 */
	@Conditional(OnEnabledJobCondition.class)
	@Bean public CleanUpStaleAttachmentsJob cleanUpStaleAttachmentsJob(DefaultAttachmentService defaultAttachmentService,
		AttachmentHeaderRepository attachmentHeaderRepository, DirtyAttachmentService dirtyAttachmentService) {
		return new CleanUpStaleAttachmentsJob(attachmentHeaderService(attachmentHeaderRepository, dirtyAttachmentService), defaultAttachmentService);
	}

	/**
	 * Job to clean up files in the attachment storage location which are not referenced by any {@link GenericReference}.
	 * See {@link CleanUpDirtyAttachmentsJob}.
	 *
	 * @return The job details wrapped in a {@link JobDetail} object
	 * @scheduler cleanUpDirtyAttachmentsJob
	 * @default `${mgmtp.a12.dataservices.jobs.attachments.cleanUpDirtyAttachments.schedule}`
	 */
	@Conditional(OnEnabledJobCondition.class)
	@Bean public JobDetail cleanUpDirtyAttachmentsJobDetail() {
		return JobBuilder.newJob()
			.ofType(CleanUpDirtyAttachmentsJob.class)
			.withIdentity("cleanUpDirtyAttachmentsJob", ATTACHMENTS)
			.withDescription("Clean up dirty attachments job.")
			.storeDurably()
			.build();
	}

	/**
	 * Creates the Quartz trigger for {@link CleanUpDirtyAttachmentsJob}.
	 *
	 * @param cleanUpDirtyAttachmentsJobDetail The job detail for cleaning up dirty attachments.
	 * @return The configured Quartz {@link Trigger}.
	 */
	@ConditionalOnBean(name = "cleanUpDirtyAttachmentsJobDetail", value = JobDetail.class)
	@Bean public Trigger cleanUpDirtyAttachmentsTrigger(JobDetail cleanUpDirtyAttachmentsJobDetail) {
		Trigger trigger = TriggerBuilder.newTrigger()
			.forJob(cleanUpDirtyAttachmentsJobDetail)
			.withIdentity("cleanUpDirtyAttachmentsTrigger", ATTACHMENTS)
			.withDescription("Clean up dirty attachments trigger.")
			.withSchedule(CronScheduleBuilder.cronSchedule(dataServicesCoreProperties.getJobs().getAttachments().getCleanUpDirtyAttachments().getSchedule()))
			.startNow()
			.build();
		log.info(REGISTERED_QUARTZ_TRIGGER_WITH_JOB, trigger, cleanUpDirtyAttachmentsJobDetail);
		return trigger;
	}

	/**
	 * Job to clean up files in the attachment storage location which were never referenced by any {@link GenericReference}
	 * and which are older than the time configured in `mgmtp.a12.dataservices.jobs.attachments.temporary.expireHours`.
	 * See {@link CleanUpStaleAttachmentsJob}.
	 *
	 * @return The job details wrapped in a {@link JobDetail} object
	 * @scheduler cleanUpStaleAttachmentsJob
	 * @default `${mgmtp.a12.dataservices.jobs.attachments.cleanUpStaleAttachments.schedule}`
	 */
	@Conditional(OnEnabledJobCondition.class)
	@Bean public JobDetail cleanUpStaleAttachmentsJobDetail() {
		return JobBuilder.newJob()
			.ofType(CleanUpStaleAttachmentsJob.class)
			.withIdentity("cleanUpStaleAttachmentsJob", ATTACHMENTS)
			.withDescription("Clean up stale attachments job.")
			.storeDurably()
			.build();
	}

	/**
	 * Creates the Quartz trigger for {@link CleanUpStaleAttachmentsJob}.
	 *
	 * @param cleanUpStaleAttachmentsJobDetail The job detail for cleaning up stale attachments.
	 * @return The configured Quartz {@link Trigger}.
	 */
	@ConditionalOnBean(name = "cleanUpStaleAttachmentsJobDetail", value = JobDetail.class)
	@Bean public Trigger cleanUpStaleAttachmentsTrigger(JobDetail cleanUpStaleAttachmentsJobDetail) {
		Trigger trigger = TriggerBuilder.newTrigger()
			.forJob(cleanUpStaleAttachmentsJobDetail)
			.withIdentity("cleanUpStaleAttachmentsTrigger", ATTACHMENTS)
			.withDescription("Clean up stale attachments trigger.")
			.withSchedule(CronScheduleBuilder.cronSchedule(dataServicesCoreProperties.getJobs().getAttachments().getCleanUpStaleAttachments().getSchedule()))
			.usingJobData(CleanUpStaleAttachmentsJob.ATTACHMENT_EXPIRE_HOURS,
				dataServicesCoreProperties.getJobs().getAttachments().getTemporary().getExpireHours())
			.startNow()
			.build();
		log.info(REGISTERED_QUARTZ_TRIGGER_WITH_JOB, trigger, cleanUpStaleAttachmentsJobDetail);
		return trigger;
	}

	/**
	 * Provides the permission evaluator for attachments.
	 *
	 * @return The {@link AttachmentPermissionEvaluator}.
	 */
	@Bean public AttachmentPermissionEvaluator attachmentPermissionEvaluator() {
		return new DefaultAttachmentPermissionEvaluator(authorizationService);
	}

	/**
	 * Provides utility functions for thumbnail generation and access.
	 *
	 * @param defaultAttachmentService Default attachment service used to access attachments.
	 * @param attachmentMapper Mapper for attachment DTOs/entities.
	 * @return The {@link ThumbnailUtil}.
	 */
	@Bean public ThumbnailUtil thumbnailUtil(DefaultAttachmentService defaultAttachmentService, AttachmentMapper attachmentMapper) {
		return new ThumbnailUtil(defaultAttachmentService, attachmentMapper);
	}
}
