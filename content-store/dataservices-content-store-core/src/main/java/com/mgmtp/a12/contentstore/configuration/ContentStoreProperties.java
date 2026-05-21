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
package com.mgmtp.a12.contentstore.configuration;

import java.io.File;

import com.mgmtp.a12.contentstore.events.ContentBeforeDownloadEvent;

import com.mgmtp.a12.dataservices.configuration.ExposePropertiesToActuator;
import lombok.Data;

/**
 * Configuration properties for Content Store server.
 */
@Data
@ExposePropertiesToActuator
public class ContentStoreProperties {

	/**
	 * Properties prefix for Content Store configuration.
	 */
	public static final String PROPERTIES_PREFIX = "mgmtp.a12.dataservices.contentstore";

	/**
	 * System property name for the user's home directory.
	 */
	public static final String USER_HOME = "user.home";

	private Storage storage = new Storage();

	/**
	 * Limit size of content (value is case-insensitive).  Acceptable configuration unit is: Kb(Kilobytes),
	 * Mb(Megabytes), Gb(Gigabytes).
	 *
	 * @default `10 MiB`
	 * @example `10 MiB` limited content size 10 Megabytes.
	 * @topic contentstore
	 */
	private String limitSize = "10 MiB";

	/**
	 * Expired time for available ticket (value is case-insensitive). Acceptable configuration unit is: H(hour),
	 * M(Minute), S(second).
	 *
	 * @default `5 min`
	 * @example `5 min` ticket will be considered as unavailable after 5 minutes.
	 * @topic contentstore
	 */
	private String ticketDuration = "5 min"; // Default valid duration for downloadable url is 5 mins


	private EnabledProperty ticketMultiDownload = new EnabledProperty();
	private Cache cache = new Cache();
	private Server server = new Server();
	private Extensions extensions = new Extensions();

	/**
	 * This is the base URL of Content Store which is used for public access.
	 * Downloadable URLs requested by a client (Web Browser) will point to this host.
	 * Therefore, the host here should be public-domain that is accessible from the internet.
	 * If Content Store is deployed on a cluster behind a load balancer, please make sure that this host is pointing to your Content Store public domain name.
	 * This base-url is required, for setting up relative path please use "/".
	 * This property is mandatory for starting up Content Store application:
	 * 1. For embedded mode by default this base-url property is set to localhost:8080 which works well for development mode,
	 * please be aware of setting this property properly in your production.
	 * Because this property is used to construct downloadable URLs for content, this means end users will never be able to download the content by using the default property localhost:8080.
	 * Please set this property by using your public domain which points to Data Services server and can be accessed from the internet.
	 *
	 * 2. For the standalone mode please consider the same situation. Set base-url property by using your public domain which points to Content Store server and can be accessed from the internet.
	 *
	 * @important This property is mandatory for starting up Content Store application.
	 * @topic contentstore
	 */
	private String baseUrl;

	/**
	 * This is the timeout for waiting until content stream is ready for downloading.
	 *
	 * @event ContentBeforeDownloadEvent
	 * @see ContentBeforeDownloadEvent
	 * @topic contentstore
	 */
	private long contentWaitReadyTimeout = 10_000L;

	/**
	 * This is for enabling download ready field by default, it'll set contentStream.ready = true.
	 * In case you turn off default listener here, you must set ready = true to enable downloading.
	 *
	 * @event ContentBeforeDownloadEvent
	 * @see ContentBeforeDownloadEvent
	 * @topic contentstore
	 */
	private boolean enableDefaultDownloadListener = true;

	/**
	 * Properties for enabling ticket multi-download feature.
	 */
	@Data
	public static class EnabledProperty {

		/**
		 * Property to allow client downloading content from ticket multiple times until it's expired.
		 * This is disabled by default.
		 *
		 * @default `false`
		 * @topic contentstore
		 **/
		private boolean enabled = false;
	}

	/**
	 * Cache-related properties for the Content Store.
	 *
	 * @topic contentstore_cache
	 */
	@Data
	public static class Cache {

		/**
		 * Cache timeout for default value of request public url of content parameter.
		 * Acceptable configuration unit is: Second.
		 *
		 * @default `3600`
		 * @example 3600 means the public url of content will be expired after 1 hour.
		 */
		private int timeout = 3600;
	}

	/**
	 * Server-related properties for Content Store controllers and context.
	 *
	 * @topic contentstore_server
	 */
	@Data
	public static class Server {

		/**
		 * Mappings in Content Store have the following structure: SPRING_CONTEXT_PATH/CONTENT_STORE_CONTEXT_PATH/...
		 * This property should be used to set CONTENT_STORE_CONTEXT_PATH.
		 * Its purpose is to give an ability to differentiate with DATA_SERVICES_CONTEXT_PATH by introducing your own prefix variable.
		 *
		 * NOTES:
		 * 1. Don't put leading '/' if SPRING_CONTEXT_PATH has trailing '/'. It will result in '//' prefix in the mappings.
		 * 2. There is a configuration called `mgmtp.a12.uaa.authentication.context-path`.
		 * It should be equal to this property for the application to function properly.
		 *
		 * @default `/cs`
		 */
		private String contextPath = "/cs";
		private ApiControllers api = new ApiControllers();
		private PubControllers pub = new PubControllers();

		@Data
		public static class ApiControllers {

			/**
			 * Enable/disable exposing of API controllers.
			 * By default it's enabled, but you could disable it if you use Content Store in embedded mode.
			 *
			 * @default `false`
			 */
			private boolean enabled = false;
			private MimeType mimeType = new MimeType();

			/**
			 * Properties related to MIME type handling.
			 */
			@Data
			public static class MimeType {

				private TrustExternalMimeType trustExternalMimeType = new TrustExternalMimeType();

				/**
				 * Properties related to trusting externally provided MIME types.
				 */
				@Data
				public static class TrustExternalMimeType {

					/**
					 * Enable/disable mandatory request parameter `mimeType` in content uploading API: "POST /api/content".
					 * By default, it's disabled, this means Content Store will probe mime type from the uploading content, the request parameter is ignored.
					 * If this property is enabled, the request parameter is mandatory and Content Store will take it as content mime type.
					 *
					 * @default `false`
					 */
					private boolean enabled;
				}
			}
		}

		/**
		 * Properties for public controllers.
		 */
		@Data
		public static class PubControllers {

			/**
			 * Enable/disable exposing of public controllers.
			 * By default it's enabled, but you could disable it if you use Content Store as library
			 * of your application, and you handle in your way.
			 *
			 * @default `true`
			 */
			private boolean enabled = true;
		}
	}

	/**
	 * Storage-related properties defining the underlying content storage implementation.
	 *
	 * @topic contentstore_storage
	 */
	@Data
	public static class Storage {

		public enum BundledContentImplementations {FS, DB, OTHER}

		/**
		 * Default implementation of content storage.
		 * Can be one of `FS` for filesystem storage, `DB` for database storage or `OTHER` - in this case none
		 * of the bundled content storages is used, and you must provide your own implementation.
		 *
		 * @default 'FS' content will be persisted to file system.
		 */
		private BundledContentImplementations contentStorage = BundledContentImplementations.FS;

		private FileSystem fs = new FileSystem();

		/**
		 * Properties for file system-based content storage.
		 */
		@Data
		public static class FileSystem {

			/**
			 * Content location on file system (Prefix `file:` is mandatory).
			 *
			 * @default `${user.home}/a12/dataservices/contentstore/contents`
			 * @example `file:/var/lib/a12/dataservices/contentstore/contents`
			 */
			private File location = new File(System.getProperty(USER_HOME).concat("/a12/dataservices/contentstore/contents"));
		}

	}

	/**
	 * Extension-related properties for optional integrations (e.g., Apache Tika).
	 *
	 * @topic contentstore_extensions
	 */
	@Data
	public static class Extensions {
		private Tika tika = new Tika();

		/**
		 * Apache Tika related properties.
		 */
		@Data
		public static class Tika {
			private InMemoryTemp inMemoryTemp = new InMemoryTemp();

			/**
			 * Properties for using in-memory temporary storage during MIME type detection.
			 */
			@Data
			public static class InMemoryTemp {

				/**
				 * If enabled, enforces Tika to use in-memory JimFs as temporary storage during Mime-Type detection.
				 *
				 * @default `false`
				 */
				boolean enabled = false;
			}
		}
	}
}
