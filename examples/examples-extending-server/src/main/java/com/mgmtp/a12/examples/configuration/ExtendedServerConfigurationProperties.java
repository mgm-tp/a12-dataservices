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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.configuration.ExposePropertiesToActuator;
import com.mgmtp.a12.examples.document.external.enumeration.BusinessPartnerExternalEnumeration;
import com.mgmtp.a12.examples.document.staticcode.StaticValidationCodeProvider;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

import lombok.Data;

/**
 * Extended Server Configuration Properties class. All properties for the example application that extend the server are specified here.
 */
@Component
@ExposePropertiesToActuator
@ConfigurationProperties(ExtendedServerConfigurationProperties.PROPERTIES_PREFIX)
@Data
public class ExtendedServerConfigurationProperties {

	/**
	 * Configuration properties prefix for the example module.
	 */
	public static final String PROPERTIES_PREFIX = "com.mgmtp.a12.examples";

	private Attachments attachments = new Attachments();
	private Documents documents = new Documents();
	private Properties properties = new Properties();


	private AuthorizationBlackBoxExample authorizationBlackBoxExample = new AuthorizationBlackBoxExample();

	private BusinessPartnerTaxAuthorityRegistrationStatusProjection businessPartnerTaxAuthorityRegistrationStatusProjection =
		new BusinessPartnerTaxAuthorityRegistrationStatusProjection();

	private StaticValidationCode staticValidationCode = new StaticValidationCode();

	private ExtendMetadata extendMetadata = new ExtendMetadata();

	/**
	 * Attachment-related configuration.
	 */
	@Data
	public static class Documents {

		private SequenceIdGenerator sequenceIdGenerator = new SequenceIdGenerator();
		private ExternalEnumeration externalEnumeration = new ExternalEnumeration();
		private Serialization serialization = new Serialization();
		private Storage storage = new Storage();
		private Metadata metadata = new Metadata();
		private Extension extension = new Extension();
		private Encryption encryption = new Encryption();
		private StaticCode staticCode = new StaticCode();

		@Data
		public static class SequenceIdGenerator {
			/**
			 * Enables the custom {@link com.mgmtp.a12.examples.document.sequence.generator.SequenceIdGenerator}
			 * implementation for generating document IDs for {@link DocumentV2}.
			 */
			private boolean enabled = false;
		}

		@Data
		public static class ExternalEnumeration {
			/**
			 * Enables the custom {@link BusinessPartnerExternalEnumeration}
			 * implementation for loading external enumerations for {@link DocumentV2}.
			 */
			private boolean enabled = false;
		}

		@Data
		public static class StaticCode {
			/**
			 * Enables the custom {@link StaticValidationCodeProvider}
			 * implementation for using static validation code for {@link DocumentV2}.
			 */
			private boolean enabled = false;
		}

		@Data
		public static class Serialization {
			/**
			 * Enables custom serialization configuration for documents.
			 */
			private boolean enabled = false;
		}

		@Data
		public static class Storage {
			private Ram ram = new Ram();

			@Data
			public static class Ram {
				/**
				 * Enables in-memory storage for documents using {@link com.mgmtp.a12.examples.document.storage.ram.InMemoryDocumentRepository}.
				 */
				private boolean enabled = false;
			}
		}

		@Data
		public static class Metadata {
			/**
			 * Enables custom metadata configuration for documents using {@link com.mgmtp.a12.examples.document.metadata.CustomMetadata}.
			 */
			private boolean enabled = false;
		}

		@Data
		public static class Extension {
			private Model model = new Model();
			private Document document = new Document();

			@Data
			public static class Model {
				/**
				 * Enables document model migration extension using {@link com.mgmtp.a12.examples.document.extension.model.DocumentModelMigration}.
				 */
				private boolean enabled = false;
			}

			@Data
			public static class Document {
				/**
				 * Enables document validation extension using {@link com.mgmtp.a12.examples.document.extension.document.ContactModelValidationExtension} and {@link com.mgmtp.a12.examples.document.extension.document.AddressValidator}.
				 */
				private boolean enabled = false;
			}
		}

		@Data
		public static class Encryption {
			/** Enables document encryption listeners using EncryptionListeners. */
			private boolean enabled = false;
		}
	}

	@Data
	public static class Attachments {

		private Audit audit = new Audit();
		private ContentValidation contentValidation = new ContentValidation();
		private CustomThumbnails customThumbnails = new CustomThumbnails();
		private Encryption encryption = new Encryption();
		private MimeTypes mimeTypes = new MimeTypes();
		private VirusScan virusScan = new VirusScan();

		/**
		 * Audit configuration for attachment operations.
		 */
		@Data
		public static class Audit {
			private boolean enabled = false;
		}

		/**
		 * Content validation configuration for attachments.
		 */
		@Data
		public static class ContentValidation {
			private boolean enabled = false;
		}

		/**
		 * Custom thumbnail generation configuration.
		 */
		@Data
		public static class CustomThumbnails {
			private boolean enabled = false;
		}

		/**
		 * Attachment encryption configuration.
		 */
		@Data
		public static class Encryption {

			private Sync sync = new Sync();
			private Async async = new Async();

			/**
			 * Synchronous encryption settings.
			 */
			@Data
			public static class Sync {
				private boolean enabled = false;
			}

			/**
			 * Asynchronous encryption settings.
			 */
			@Data
			public static class Async {
				private boolean enabled = false;
			}
		}

		/**
		 * Mime type configuration for attachments.
		 */
		@Data
		public static class MimeTypes {

			private Custom custom = new Custom();

			/**
			 * Custom mime type mapping configuration.
			 */
			@Data
			public static class Custom {

				private boolean enabled = false;
				private String mimeType = null;
				private String replacement = null;
			}
		}

		/**
		 * Virus scan configuration for attachments.
		 */
		@Data
		public static class VirusScan {
			private boolean enabled = false;

			private ClamAV clamAV = new ClamAV();

			/**
			 * ClamAV scanner connection settings.
			 */
			@Data
			public static class ClamAV {
				private String host = "localhost";

				private int port = 3310;

				private int timeout = 2000;
			}
		}
	}

	/**
	 * Example switch for black-box authorization.
	 */
	@Data
	public static class AuthorizationBlackBoxExample {
		private boolean enabled = false;
	}

	/**
	 * Configuration for the Business Partner tax authority registration status projection.
	 */
	@Data
	public static class BusinessPartnerTaxAuthorityRegistrationStatusProjection {

		private NoOp noOp = new NoOp();

		/**
		 * NoOp variant configuration for the projection.
		 */
		@Data
		public static class NoOp {

			/**
			 * Enables the NoOp projection of `businessPartnerTaxAuthorityRegistrationStatus` projection defined in BusinessPartnerTaxAuthorityRegistrationStatusNoOp class
			 */
			private boolean enabled = false;
		}

		private boolean enabled = false;
	}

	/**
	 * Example switch for static validation code.
	 */
	@Data
	public static class StaticValidationCode {
		private boolean enabled = false;
	}

	/**
	 * Example switch for metadata extension.
	 */
	@Data
	public static class ExtendMetadata {
		private boolean enabled = false;
	}

	/**
	 * Example properties that demonstrate runtime changes.
	 */
	@Data
	public static class Properties {

		private boolean changed = false;
		private String text = "defaultValue";
	}
}
