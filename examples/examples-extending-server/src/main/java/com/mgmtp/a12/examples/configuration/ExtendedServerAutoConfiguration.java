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
package com.mgmtp.a12.examples.configuration;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.mgmtp.a12.dataservices.common.content.ContentTypeDetector;
import com.mgmtp.a12.examples.attachment.AttachmentContentValidator;
import com.mgmtp.a12.examples.attachment.audit.AttachmentAuditService;
import com.mgmtp.a12.examples.attachment.encryption.AttachmentEncryptionAsyncListeners;
import com.mgmtp.a12.examples.attachment.encryption.AttachmentEncryptionSyncListeners;
import com.mgmtp.a12.examples.attachment.listener.AttachmentContentValidationService;
import com.mgmtp.a12.examples.attachment.mime.CustomZipTypeListener;
import com.mgmtp.a12.examples.attachment.thumbnails.CustomThumbnailListener;
import com.mgmtp.a12.examples.authorization.NoFirstElementAuthorization;
import com.mgmtp.a12.examples.document.encryption.EncryptionListeners;
import com.mgmtp.a12.examples.document.extension.document.AddressValidator;
import com.mgmtp.a12.examples.document.extension.document.ContactModelValidationExtension;
import com.mgmtp.a12.examples.document.extension.model.DocumentModelMigration;
import com.mgmtp.a12.examples.document.external.enumeration.BusinessPartnerExternalEnumeration;
import com.mgmtp.a12.examples.document.sequence.generator.SequenceIdGenerator;
import com.mgmtp.a12.examples.document.storage.ram.InMemoryDocumentRepository;
import com.mgmtp.a12.examples.extra.ExtraEntityRepository;
import com.mgmtp.a12.examples.query.BusinessPartnerTaxAuthorityRegistrationStatus;
import com.mgmtp.a12.examples.query.BusinessPartnerTaxAuthorityRegistrationStatusNoOp;
import com.mgmtp.a12.examples.util.MediaTypeUtils;
import com.mgmtp.a12.examples.util.ResourceUtil;

/**
 * Extended Server Auto Configuration class.
 *
 */
@Configuration
public class ExtendedServerAutoConfiguration {

	/**
	 * Provides {@link MediaTypeUtils} for media type detection using the configured {@link ContentTypeDetector}.
	 *
	 * @param contentTypeDetector detector used to infer content types; must not be null.
	 * @return a utility instance wired with the given detector.
	 */
	@ConditionalOnBean(ContentTypeDetector.class)
	@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples.attachments.mime-types.custom", name = "enabled", havingValue = "true")
	@Bean public MediaTypeUtils mediaTypeUtils(ContentTypeDetector contentTypeDetector) {
		return new MediaTypeUtils(contentTypeDetector);
	}

	/**
	 * Registers an authorization rule that disallows selecting the first element in certain collections.
	 *
	 * @return an authorization component used in example scenarios.
	 */
	@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples", name = "authorization-black-box-example.enabled", havingValue = "true")
	@Bean public NoFirstElementAuthorization noFirstElementAuthorization() {
		return new NoFirstElementAuthorization();
	}

	/**
	 * Enables the {@link BusinessPartnerTaxAuthorityRegistrationStatus} projection.
	 *
	 * @return a projection that enriches Business Partner results with tax authority registration status.
	 */
	@Order(100)
	@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples", name = "business-partner-tax-authority-registration-status-projection.enabled", havingValue = "true")
	@Bean public BusinessPartnerTaxAuthorityRegistrationStatus businessPartnerTaxAuthorityRegistrationStatusProjection() {
		return new BusinessPartnerTaxAuthorityRegistrationStatus();
	}

	/**
	 * Enables the NoOp projection of `businessPartnerTaxAuthorityRegistrationStatus` projection defined in BusinessPartnerTaxAuthorityRegistrationStatusNoOp class.
	 * This bean should exist next to `BusinessPartnerTaxAuthorityRegistrationStatus` bean to demostrate that multiple implementations of the same projection
	 * can exist while only one is active.
	 *
	 * @return a pass-through projection that delegates shaping/aggregation to SQL.
	 */
	@Order(99)
	@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples", name = "business-partner-tax-authority-registration-status-projection.enabled", havingValue = "true")
	@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples", name = "business-partner-tax-authority-registration-status-projection.no-op.enabled", havingValue = "true")
	@Bean public BusinessPartnerTaxAuthorityRegistrationStatusNoOp businessPartnerTaxAuthorityRegistrationStatusNoOpProjection() {
		return new BusinessPartnerTaxAuthorityRegistrationStatusNoOp();
	}

	/**
	 * Registers synchronous listeners that encrypt attachments on upload.
	 *
	 * @return synchronous encryption listeners.
	 */
	@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples.attachments.encryption.sync", name = "enabled", havingValue = "true")
	@Bean public AttachmentEncryptionSyncListeners attachmentEncryptionSyncListeners() {
		return new AttachmentEncryptionSyncListeners();
	}

	/**
	 * Registers asynchronous listeners that encrypt attachments using an in-memory map for intermediate content storage.
	 *
	 * @return asynchronous encryption listeners.
	 */
	@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples.attachments.encryption.async", name = "enabled", havingValue = "true")
	@Bean public AttachmentEncryptionAsyncListeners attachmentEncryptionAsyncListeners() {
		return new AttachmentEncryptionAsyncListeners();
	}

	/**
	 * Registers custom thumbnail generation for attachments.
	 *
	 * @param resourceUtil utility used to read thumbnail template resources; must not be null.
	 * @return a custom thumbnail listener.
	 */
	@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples.attachments.custom-thumbnails", name = "enabled", havingValue = "true")
	@Bean public CustomThumbnailListener customThumbnailConfiguration(ResourceUtil resourceUtil) {
		return new CustomThumbnailListener(resourceUtil);
	}

	/**
	 * Registers an attachment audit service for logging and persistence.
	 *
	 * @param extraEntityRepository repository used to store audit entries; must not be null.
	 * @return an audit service instance.
	 */
	@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples.attachments.audit", name = "enabled", havingValue = "true")
	@Bean public AttachmentAuditService attachmentAuditListener(ExtraEntityRepository extraEntityRepository) {
		return new AttachmentAuditService(extraEntityRepository);
	}

	/**
	 * Registers a custom ZIP media type listener to override detection rules.
	 *
	 * @param properties configuration properties used to determine the custom mapping; must not be null.
	 * @return a listener that registers a custom ZIP mime type replacement.
	 */
	@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples.attachments.mime-types.custom", name = "enabled", havingValue = "true")
	@Bean public CustomZipTypeListener customZipTypeListener(ExtendedServerConfigurationProperties properties) {
		return new  CustomZipTypeListener(properties);
	}

	/**
	 * Registers content validation for attachments by aggregating {@link AttachmentContentValidator}s.
	 *
	 * @param attachmentContentValidators validators applied to attachment content; may be empty.
	 * @return a validation service that invokes all configured validators.
	 */
	@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples.attachments.content-validation", name = "enabled", havingValue = "true")
	@Bean public AttachmentContentValidationService attachmentContentValidationService(List<AttachmentContentValidator> attachmentContentValidators) {
		return new AttachmentContentValidationService(attachmentContentValidators);
	}

	@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples.documents.sequence-id-generator", name = "enabled", havingValue = "true")
	@Bean public SequenceIdGenerator sequenceIdGenerator() { return new SequenceIdGenerator(); }

	@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples.documents.external-enumeration", name = "enabled", havingValue = "true")
	@Bean public BusinessPartnerExternalEnumeration businessPartnerExternalEnumeration() { return new BusinessPartnerExternalEnumeration(); }

	@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples.documents.storage.ram", name = "enabled", havingValue = "true")
	@Bean public InMemoryDocumentRepository inMemoryDocumentRepository() { return new InMemoryDocumentRepository(); }

	@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples.documents.encryption", name = "enabled", havingValue = "true")
	@Bean public EncryptionListeners encryptionListeners() { return new EncryptionListeners(); }

	@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples.documents.extension.model", name = "enabled", havingValue = "true")
	@Bean public DocumentModelMigration documentModelMigration() { return new DocumentModelMigration(); }

	@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples.documents.extension.document", name = "enabled", havingValue = "true")
	@Bean public AddressValidator addressValidator() { return new AddressValidator(); }

	@ConditionalOnProperty(prefix = "com.mgmtp.a12.examples.documents.extension.document", name = "enabled", havingValue = "true")
	@Bean public ContactModelValidationExtension contactModelValidationExtension(AddressValidator addressValidator) { return new ContactModelValidationExtension(addressValidator); }
}
