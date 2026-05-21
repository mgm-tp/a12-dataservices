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
package com.mgmtp.a12.dataservices.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mgmtp.a12.dataservices.configuration.documentation.DocumentationPropertyName;
import com.mgmtp.a12.dataservices.document.operation.CoreOperationConstants;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * @topic core
 */

@Data
public class DataServicesCoreProperties {

	public static final String PROPERTIES_PREFIX = "mgmtp.a12.dataservices";
	public static final String USER_HOME = "user.home";
	public static final String CLEANUP_JOB_DEFAULT_SCHEDULE = "0 */5 * * * ?";
	public static final String DS_PACKAGE_PREFIX = "com.mgmtp.a12.dataservices";
	public static final String MATCH_ALL = "*";
	private Initialization initialization = new Initialization();
	private Filesystem filesystem = new Filesystem();
	private Jobs jobs = new Jobs();
	private JsonRpc jsonRpc = new JsonRpc();
	private Attachments attachments = new Attachments();
	private Document documents = new Document();
	private Model models = new Model();
	private Search search = new Search();
	private Query query = new Query();

	private Cache cache = new Cache();
	private Logging logging = new Logging();
	private Server server = new Server();
	private Cdd cdd = new Cdd();
	private Authorization authorization = new Authorization();
	private Enumeration enumeration = new Enumeration();

	/**
	 * @topic enumeration
	 */
	@Data
	public static class Enumeration {

		private RestEndpoint restEndpoint = new RestEndpoint();

		@Data
		public static class RestEndpoint {

			/**
			 * Switch for enabling/disabling enumeration REST endpoint.
			 *
			 * @default `false`
			 */
			private boolean enabled = false;

		}

		/**
		 * Hard limit for page size for list of documents used to load external enumerations. If there are more documents available, the page size is reduced to this.
		 *
		 * @default `100`
		 */
		private int pageLimit = 100;
	}

	/**
	 * @topic initialization
	 */
	@Data
	public static class Initialization {

		@Setter(AccessLevel.PRIVATE)
		@Getter(AccessLevel.PRIVATE)
		@DocumentationPropertyName(value = "import")
		private Import imports = new Import();
		private Script scripts = new Script();
		private Migration migration = new Migration();
		private CleanUpRequestId cleanUpRequestId = new CleanUpRequestId();
		private PreCompile preCompile = new PreCompile();

		public Import getImport() {
			return imports;
		}

		public void setImport(Import imports) {
			this.imports = imports;
		}

		@Data
		public static class PreCompile {

			/**
			 * Allows whitelisting specific models while disabling all others.
			 * Setting the value to "\*" permits all existing models. Note: If "*" isn't the only string in the list, no special meaning will be applied.
			 *
			 * @default `*`
			 */
			private List<String> enabledForModels = new ArrayList<>(List.of(MATCH_ALL));
		}

		/**
		 * @topic initializationimport
		 */
		@Data
		public static class Import {

			Document documents = new Document();
			Model models = new Model();

			@Data
			public static class Model {

				Overwrite overwrite = new Overwrite();

				/**
				 * Enables import of business models on system initialization.
				 *
				 * @default `true`
				 */
				private boolean enabled = true;

				/**
				 * Enables full import of models during initialization.
				 *
				 * This property specifies which model types should be deleted. To delete all models, use "*".
				 * Only model definitions are removed; the underlying data remains. Links and documents without a model become inaccessible via the API.
				 * Models must be re-added to access data via API. Otherwise, the data will be accessible only in the database. In case no model type is
				 * provided, no deleting is done.
				 *
				 * Related configurations: `mgmtp.a12.dataservices.models.relationship.validation.enabled`, `mgmtp.a12.dataservices.models.relationship.safe-delete.enabled`
				 *
				 * @default `null`
				 */
				private List<String> typesToClear;

				/**
				 * Specifies the path where imported models are located.
				 *
				 * Examples:
				 *
				 * - file:/path/to/folder/
				 * - classpath:/jsonRpc/
				 *
				 * Comments:
				 *
				 * - Prefix `file:` or `classpath:` is mandatory here.
				 * - For `classpath` prefix, leading and trailing slashes are optional.
				 * - On Windows `/path/to/folder` represents the directory `C:\path\to\folder`.
				 * - Wildcards are not supported here.
				 *
				 */
				private String[] path;

				/**
				 * @topic modeloverwriting
				 */
				@Data
				public static class Overwrite {
					/**
					 * Enables overwriting of document models on application initialization and bulk import. Applies on importing business models by
					 * `mgmtp.a12.dataservices.initialization.import.models.path`.
					 *
					 * @default `null`
					 */
					private DocumentModel documentModels = new DocumentModel();

					/**
					 * Configures default for model overwriting on application initialization and bulk import. Applies on importing business models by
					 * `mgmtp.a12.dataservices.initialization.import.models.path`.
					 *
					 * @default `true`
					 */
					private boolean enabled = true;

					/**
					 * Enables model overwriting by particular type of model on application initialization and bulk import. When not provided, value `mgmtp.a12.dataservices.initialization.import.models.overwrite.enabled` will be used. For example: `mgmtp.a12.dataservices.initialization.import.models.overwrite.models.my-custom-model-type=false` will deny overwriting models of type "my-custom-model-type" on application initialization.
					 * Applies on importing user models by `mgmtp.a12.dataservices.initialization.import.models.path`.
					 *
					 * @default `null`
					 */
					private Map<String, Boolean> models;

					@Data
					public static class DocumentModel {
						/**
						 * Enables overwriting of document models on application initialization. Applies on importing business models by
						 * `mgmtp.a12.dataservices.initialization.import.models.path`.
						 *
						 * @default `true`
						 */
						private boolean enabled = true;
					}
				}
			}
		}

		@Data
		public static class Script {

			JsonRpc jsonRpc = new JsonRpc();

			@Data
			public static class JsonRpc {
				/**
				 * Enables the execution of JSON-RPC requests on server initialization.
				 *
				 * @default `false`
				 */
				private boolean enabled = false;

				/**
				 * Pattern indicating the resources as JSON-RPC requests to be executed on initialization. Supports providing multiple paths.
				 *
				 * Path examples:
				 *
				 * - file:/path/to/folder/*
				 * - file:/path/to/folder/*.json
				 * - file:/path/to/folder/singleRequest.json
				 * - classpath:/jsonRpc/*
				 * - classpath:/jsonRpc/*.json
				 * - classpath:/jsonRpc/singleRequest.json
				 *
				 * Comments:
				 *
				 * - Prefix `file:` or `classpath:` is mandatory here.
				 * - For `classpath` prefix, leading slashes are optional.
				 * - It will be executed ordered by file name ASC.
				 * - On Windows `/path/to/folder` represents the directory `C:\path\to\folder`.
				 * - Double asterisks (`**`) are not supported here.
				 * - The property is ignored if the `mgmtp.a12.dataservices.initialization.scripts.jsonRpc.enabled` property is `false`.
				 *
				 * @default empty list.
				 */
				private List<String> paths = new ArrayList<>();
			}
		}

		@Data
		public static class Migration {
			/**
			 * Enables migration of custom tasks (e.g. Document or Model migration) on system initialization.
			 *
			 * @default `true`
			 */
			private boolean enabled = true;
		}

		@Data
		public static class CleanUpRequestId {
			/**
			 * Enables clean up of table REQUEST_ID on system initialization.
			 *
			 * @default `false`
			 */
			private boolean enabled = false;
		}
	}

	/**
	 * @topic attachment
	 */
	@Data
	public static class Filesystem {

		private Write write = new Write();

		@Data
		public static class Write {

			/**
			 * Enable/disable writes to the File system. Disabling file system writes will disable the file based attachment persister, loader, and document import functionality.
			 *
			 * @deprecated The switch is not used anywhere since the introduction of the content store. It will be dropped without replacement.
			 * @default `true`
			 */
			@Deprecated(since = "38.2.0", forRemoval = true)
			private boolean enabled = true;
		}
	}

	/**
	 * @topic query
	 */
	@Data
	public static class Query {

		private PageRequest pageRequest = new PageRequest();
		private Reindexing reindexing = new Reindexing();
		private SimpleSearch simpleSearch = new SimpleSearch();
		private ExactMatch exactMatch = new ExactMatch();
		private Validation validation = new Validation();
		private Aggregation aggregation = new Aggregation();

		/**
		 * Hard limit for query depth. If this limit is exceeded, an `InvalidInputException` is thrown.
		 *
		 * @default `10`
		 */
		private int maxQueryDepth = 10;

		/**
		 * Hard limit for the result of each `links` section. If this limit is exceeded, an `InvalidInputException` is thrown.
		 *
		 * @default `10_000`
		 */
		private int maxLinksSize = 10_000;

		/**
		 * List of disabled operators. If a query contains one of the disabled operators, an `InvalidInputException` is thrown.
		 * Possible values are:
		 *
		 * - and
		 * - or
		 * - not
		 * - exact_match
		 * - simple_search
		 * - has
		 * - undefined match
		 * - date_range
		 * - datefragment_range
		 * - double_range
		 *
		 * @default empty list
		 */
		private List<String> disabledOperators = Collections.emptyList();

		/**
		 * Hard limit for the number of operands of an `or` operator. If this limit is exceeded, an `InvalidInputException` is thrown.
		 *
		 * @default `1000`
		 */
		private int maxOrOperands = 1000;

		/**
		 * Hard limit for the number of `or` operators per query. If this limit is exceeded, an `InvalidInputException` is thrown.
		 *
		 * @default `1000`
		 */
		private int maxOrOperators = 1000;

		/**
		 * Hard limit for the number of operands of an `or` operator. If this limit is exceeded, an `InvalidInputException` is thrown.
		 *
		 * @default `1000`
		 */
		private int maxAndOperands = 1000;

		/**
		 * Hard limit for the number of `and` operators per query. If this limit is exceeded, an `InvalidInputException` is thrown.
		 *
		 * @default `1000`
		 */
		private int maxAndOperators = 1000;

		@Data
		public static class PageRequest {

			/**
			 * Hard limit for query page number. If this limit is exceeded, an `InvalidInputException` is thrown.
			 *
			 * @default `100`
			 */
			private int pageNumberLimit = 100;

			/**
			 * Hard limit for query page size. If this limit is exceeded, an `InvalidInputException` is thrown.
			 *
			 * @default `100`
			 */
			private int pageSizeLimit = 100;
		}

		@Data
		public static class Reindexing {

			/**
			 * A list of model names to which the reindexing operation will be applied.
			 * Setting the value to "\*" permits all existing models. Note: If "*" isn't the only string in the list, no special meaning will be applied.
			 *
			 * @default `*` meaning that all models are considered
			 */
			private List<String> applyToModels = List.of(MATCH_ALL);

			/**
			 * A switch that allows index manipulation operations to be performed during the DS initialization.
			 *
			 * Possible values:
			 *
			 * - `REBUILD_INDEX`: Deletes the complete content of the index and reconstructs it based on the current documents in the system.
			 * - `INDEX_NEW_ONLY`: Indexes only the documents that have not been indexed yet.
			 * - `DISABLED`: No index operations are performed on initialization.
			 *
			 * Note that it is possible to control indexing on per-model basis using `mgmtp.a12.dataservices.query.reindexing.applyToModels` property.
			 *
			 * @default `DISABLED`
			 */
			private Mode mode = Mode.DISABLED;

			/**
			 * A switch to allow ignoring errors during re-indexing. If an error occurs, it will be logged,
			 * but the server initialization will continue without interruption.
			 *
			 * NOTE: DS by default skips documents that can not be deserialized during reindexing. This property does not change this behavior.
			 *
			 * @default `false`
			 */
			private boolean ignoreErrors = false;

			/**
			 * Number of threads to use for reindexing.
			 *
			 * @default `5`
			 */
			private int numberOfThreads = 5;

			/**
			 * Number of documents to reindex in a single batch. Batches are processed in parallel in {@link #numberOfThreads} threads.
			 *
			 * @default `2_000`
			 */
			private int batchSize = 2_000;

			private Vacuum vacuum = new Vacuum();

			private ModelFields modelFields = new ModelFields();

			public enum Mode {
				DISABLED, REBUILD_INDEX, INDEX_NEW_ONLY
			}

			@Data
			public static class Vacuum {
				/**
				 * Controls whether the `VACUUM ANALYZE` should be executed after index rebuild.
				 *
				 * @default `true`
				 */
				private boolean enabled = true;
			}

			@Data
			public static class ModelFields {
				/**
				 * Controls whether the `model index fields` should be re-indexed.
				 *
				 * @default `true`
				 */
				private boolean enabled = true;
			}
		}

		@Data
		public static class SimpleSearch {
			ExcludingMetadata excludingMetadata = new ExcludingMetadata();

			/**
			 * The minimum size of a token that can be included in the search. Tokens smaller than this size will be ignored in the search process.
			 * A value less than 3 is not recommended because it can degrade performance by increasing the number of irrelevant matches.
			 *
			 * @default `3`
			 */
			private int minSearchableTokenSize = 3;

			/**
			 * The maximum allowed length (in characters) for an input value
			 * provided in a `simple_search` operation.
			 * This limit helps to prevent excessively long inputs that could
			 * negatively impact PostgreSQL regular expression search.
			 * Any input value exceeding this configured length will result in an error
			 * response from the API.
			 */
			private int maxInputValueLength = 100;

			@Data
			public static class ExcludingMetadata {

				/**
				 * Whether to exclude metadata from the search
				 *
				 * @default `false`
				 */
				private boolean enabled = false;
			}

		}

		@Data
		public static class ExactMatch {

			/**
			 * The maximum allowed length (in characters) for an input value
			 * provided in a `exact_match` operation.
			 * This limit helps to prevent excessively long inputs that could
			 * negatively impact PostgreSQL regular expression search.
			 * Any input value exceeding this configured length will result in an error
			 * response from the API.
			 */
			private int maxInputValueLength = 100;

			EnumerationValueMatch enumerationValueMatch = new EnumerationValueMatch();

			@Data
			public static class EnumerationValueMatch {

				/**
				 * Enumeration value matching is only performed if the Accept-Language header is absent in the request.
				 * If the header is present, exact matching for enumeration values is disabled by default.
				 * This property enables additional matching for enumeration values when the header is provided.
				 * If enabled, matching will occur on both the provided language and the enumeration value.
				 *
				 * Note: Enabling this will negatively impact performance for all queries using the `exact_match` operator on enumeration fields.
				 * @default `false`
				 */
				private boolean enabled = false;
			}

		}

		@Data
		public static class Validation {
			/**
			 * This is a switch to enable/disable query validation. Please read documentation about validation phase of
			 * Query API. This property should not be used in productional environments there are performance and security
			 * concerns. In version 39.0.0 we will change a default value of this property to `false`.
			 *
			 * @default `true`
			 */
			private boolean enabled = true;
		}

		@Data
		public static class Aggregation {

			/**
			 * Fixed page size for aggregation endpoint. In aggregation endpoint it is not possible to have pagination, so this value is used to limit the number of results returned.
			 *
			 * @default `10`
			 */
			private int listSize = 10;

			/**
			 * Default precision for aggregation functions.
			 *
			 * @default `2`
			 */
			private int defaultPrecision = 2;
		}
	}

	/**
	 * This is no longer used anywhere. It is kept here because of tests so far.
	 */
	@Data
	@Deprecated(since = "38.0.0", forRemoval = true)
	public static class Search {

		private Paging paging = new Paging();

		@Data
		public static class Paging {

			private LinkPaging links = new LinkPaging();

			@Data
			public static class LinkPaging {

				/**
				 * Hard limit for query offset for list of links. If query contains offset bigger than this value, the query offset is reduced to this.
				 *
				 * @default `5_000`
				 */
				private int offsetLimit = 5_000;

				/**
				 * Hard limit for query page size for list of links. If query contains page size bigger than this value, the query page size is reduced to this.
				 *
				 * @default `100`
				 */
				private int pageLimit = 100;
			}
		}
	}

	/**
	 * @topic documents
	 */
	@Data
	public static class Document {

		private Computation computation = new Computation();
		private Validation validation = new Validation();
		private MultiDelete multiDelete = new MultiDelete();
		private Delete delete = new Delete();
		private PersistTransientFields persistTransientFields = new PersistTransientFields();

		@Data
		public static class PersistTransientFields {

			/**
			 * Switch for enabling/disabling persistence (consequently indexing) of transient fields
			 *
			 * @default `false`
			 */
			private boolean enabled = false;
		}

		@Data
		public static class Delete {

			private CascadeLinks cascadeLinks = new CascadeLinks();

			@Data
			public static class CascadeLinks {

				/**
				 * Contains a list of document model names for which links must not be deleted.
				 * To disable deletion for all models, use "*". If a model name is specified in this list, deletion of any links belonging to that
				 * model will not be performed.
				 *
				 * @default `null`
				 */
				private List<String> disabledForModels;
			}
		}

		@Data
		public static class MultiDelete {

			/**
			 * Hard limit for maximum amount of documents to be deleted in {@link com.mgmtp.a12.dataservices.document.operation.internal.MultiDeleteDocumentsOperation}.
			 *
			 * @default `50`.
			 */
			private int limit = 50;
		}

		@Data
		public static class Computation {
			CleanupErrorAndNotComputedValue cleanupErrorAndNotComputedValue = new CleanupErrorAndNotComputedValue();

			/**
			 * Enable computation for provided document models on save.
			 *
			 * @default `null`
			 */
			private List<String> enabledForModels;

			@Data
			public static class CleanupErrorAndNotComputedValue {

				/**
				 * If `true`, we apply kernel API for cleaning up error and not computed field after computation.
				 *
				 * @default `false`
				 */
				private boolean enabled = false;
			}
		}

		@Data
		public static class Validation {
			/**
			 * If `true`, documents are fully validated by default on save. Only documents of models listed
			 * in {@link #partialForModels} and {@link #skipForModels} are handled differently.
			 *
			 * @default `true`
			 */
			private boolean enabled = true;

			/**
			 * For documents of these models validate just fields which are set.
			 *
			 * @default `null`
			 */
			private List<String> partialForModels;
			/**
			 * Skip validation of these models on save.
			 *
			 * @default `null`
			 */
			private List<String> skipForModels;

			/**
			 * Default validation locale when there is no validation locale provided in request.
			 *
			 * @default `en`
			 * @topic core
			 */
			private String language;

			private ValidationCodeList list = new ValidationCodeList();

			/**
			 * @topic internal
			 */
			@Data
			public static class ValidationCodeList {
				/**
				 * Hard limit for result size of the {@link com.mgmtp.a12.dataservices.model.operation.internal.ListModelsOperation}.
				 *
				 * @default `50`
				 */
				private int hardLimit = 50;
			}
		}
	}

	/**
	 * @topic initialization
	 */
	@Data
	public static class Model {

		ModelsList list = new ModelsList();

		Relationship relationship = new Relationship();
		Metadata metadata = new Metadata();

		@Data
		public static class ModelsList {

			/**
			 * Hard limit for result size of the {@link com.mgmtp.a12.dataservices.model.operation.internal.ListModelsOperation}. How many models can a single user fetch.
			 *
			 * @default `50`
			 */
			private int hardLimit = 50;
		}

		@Data
		public static class Relationship {

			Validation validation = new Validation();
			SafeDelete safeDelete = new SafeDelete();

			@Data
			public static class SafeDelete {

				/**
				 * If `true`, relationship models are checked for links when deleting. If links exist, the deletion is aborted and an error is returned.
				 *
				 * @default `true`
				 */
				private boolean enabled = true;
			}

			@Data
			public static class Validation {

				/**
				 * If `true`, relationship models are fully validated when saving
				 *
				 * @default `true`
				 */
				private boolean enabled = true;
			}
		}

		@Data
		public static class Metadata {

			private Document document = new Document();

			@Data
			public static class Document {

				/**
				 * Path to the document metadata JSON file within the classpath resources folder.
				 * The file must be located in the resources directory of the project (e.g., `src/main/resources`).
				 * Use absolute paths starting with `/` for files in the root of the resources folder.
				 *
				 * @default `/com/mgmtp/a12/platform/model/document-meta-data.json`
				 */
				private String path = "/com/mgmtp/a12/platform/model/document-meta-data.json";
			}
		}
	}

	/**
	 * @topic attachment
	 */
	@Data
	public static class Attachments {

		private Extension ext = new Extension();
		private Thumbnail thumbnail = new Thumbnail();
		private Cleanup cleanup = new Cleanup();
		private RestEndpoint restEndpoint = new RestEndpoint();
		private MimeType mimeType = new MimeType();
		private Type type = new Type();

		/**
		 * Switch for enabling/disabling attachment handling.
		 *
		 * @default `true`
		 */
		private boolean enabled = true;

		@Data
		public static class RestEndpoint {

			/**
			 * Switch for enabling/disabling attachment REST endpoint.
			 *
			 * @default `false`
			 */
			private boolean enabled = false;
		}

		@Data
		public static class MimeType {

			private ProbeMimeType probeMimeType = new ProbeMimeType();
			private InMemoryTemp inMemoryTemp = new InMemoryTemp();

			@Data
			public static class ProbeMimeType {

				/**
				 * Enable/disable Data Services probes mime type by itself or delegate to Content Store.
				 *
				 * @default `false`
				 */
				private boolean enabled;
			}

			@Data
			public static class InMemoryTemp {

				/**
				 * If enabled, enforces probing mime type to use in-memory JimFs as temporary storage during detection.
				 *
				 * @default `false`
				 */
				private boolean enabled;
			}
		}

		@Data
		public static class Thumbnail {

			private Preview preview = new Preview();
			private Optimization optimization = new Optimization();
			private Generation generation = new Generation();
			/**
			 * Size in pixels for small thumbnail.
			 *
			 * @default `32`
			 */
			private int sizeSmall = 32;

			/**
			 * Size in pixels for big thumbnail.
			 *
			 * @default `64`
			 */
			private int sizeBig = 64;

			@Data
			public static class Preview {
				/**
				 * Switch for load thumbnail functionality
				 *
				 * @default `false`
				 */
				private boolean enabled = false;
			}

			@Data
			public static class Optimization {
				/**
				 * Base url for thumbnail for optimization
				 */
				private String baseUrl;

				private Url url = new Url();

				private Performance performance = new Performance();

				@Data
				public static class Url {
					/**
					 * Thumbnail url is auto-computed on Data Services side. If enabling this config, we must config base thumbnail url: `mgmtp.a12.dataservices.attachments.thumbnail.optimization.baseUrl`.
					 *
					 * @default `false`
					 */
					private boolean enabled = false;
				}

				@Data
				public static class Performance {
					/**
					 * Try to use `java.awt.Graphics2D` for generating thumbnail to increase performance.
					 * If enabled `Graphics2D` will be used to generate thumbnail. By default, it's disabled, `Thumbnailator` will be used.
					 *
					 * @default `false`
					 */
					private boolean enabled = false;
				}
			}

			@Data
			public static class Generation {

				// TODO: A12S-4948 enable library configuration via properties
				private Thumbnailator thumbnailator = new Thumbnailator();

				private ImageDiskCache imageDiskCache = new ImageDiskCache();

				@Data
				public static class Thumbnailator {

					private ConserveMemoryWorkaround conserveMemoryWorkaround = new ConserveMemoryWorkaround();

					@Data
					public static class ConserveMemoryWorkaround {

						/**
						 * This property is disabled by default, if enabled, the workaround solution provided by `Thumbnailator` will be applied by setting system argument `-Dthumbnailator.conserveMemoryWorkaround=true`.
						 * Both height and width of image have dimensions larger than 1800 pixels `Thumbnailator` will invoke code to load a smaller image to memory from the
						 * source image when creating a thumbnail.
						 * This property is only applied if mgmtp.a12.dataservices.attachments.thumbnail.optimization.performance.enabled=false
						 *
						 * @default `false`
						 */
						private boolean enabled = false;
					}
				}

				@Data
				public static class ImageDiskCache {

					/**
					 * Sets a flag indicating whether `ImageIO` should use disk-based cache when creating ImageInputStreams and ImageOutputStreams.
					 * Setting this property to false disallows the use of disk for future streams, which may be advantageous when working with small images,
					 * as the overhead of creating and destroying files is removed.
					 * By default, this property is false.
					 *
					 * @default `false`
					 */
					private boolean enabled = false;
				}
			}
		}

		@Data
		public static class Extension {

			private FileSystem fs = new FileSystem();
			private ContentStore contentstore = new ContentStore();

			@Data
			public static class FileSystem {

				/**
				 * Attachment location on file system (from version V1.5 on). Prefix `file:` is always mandatory for value.
				 *
				 * @default `${user.home}/a12/dataservices/attachments`
				 * @example `file:/var/lib/a12/dataservices/attachments`
				 */
				private File location = new File(System.getProperty(USER_HOME).concat("/a12/dataservices/attachments"));
			}

			@Data
			public static class ContentStore {
				/**
				 * Ticket expiration time in seconds.
				 *
				 * @default `300`
				 */
				private long ticketTimeout = 300;
				private Embedded embedded = new Embedded();

			}

			@Data
			public static class Embedded {
				/**
				 * Switch for using content store embedded mode
				 *
				 * @default `true`
				 */
				private boolean enabled = true;
			}
		}

		@Data
		public static class Cleanup {
			private Retry retry = new Retry();

			@Data
			public static class Retry {
				/**
				 * Maximum count of retries for recoverable errors.
				 *
				 * @default `5`
				 */
				private int max = 5;
				/**
				 * Delay before retry after recoverable error. See {@link com.mgmtp.a12.dataservices.common.quantity.internal.QuantityParsers#parseTimeQuantity(String)} for possible values.
				 *
				 * @default `"5 min"`
				 */
				private String delay = "5 min";
			}
		}

		@Data
		public static class Type {

			private PublicType publicType = new PublicType();

			@Data
			public static class PublicType {

				/**
				 * List of Document Models which attachments will be public.
				 *
				 * @default empty list
				 */
				private List<String> models = new ArrayList<>();
			}
		}

	}

	/**
	 * @topic RPC
	 */
	@Data
	public static class JsonRpc {
		/**
		 * Enables/disables JSON-RPC endpoint.
		 *
		 * @default `false`
		 */
		private boolean enabled = false;

		/**
		 * Limit for maximum number of method calls per single RPC request
		 *
		 * @default 100
		 */
		private int maxMethodCallsPerRequest = 100;

		/**
		 * Allows using specified RPC operations or group of operation. You can set the value to "\*" to allow all existing RPC operation.
		 * Note that in case "*" wouldn't be the only string in the list, no special meaning would be applied.
		 * Other pre-defined operation groups are:
		 *
		 * - `DOCUMENT_OPERATIONS`: All document operations (excluding CDD handling, relationships and attachments).
		 * - `CDD_OPERATIONS` : All CDD operations (without potentially needed document and link operations).
		 * - `LINK_OPERATIONS` : All relationship (aka link) operations (without potentially needed document operations).
		 * - `ATTACHMENT_OPERATIONS` : All attachment and thumbnail operations (without potentially needed document operations).
		 *
		 * By default, the `A12_INTERNAL_OPERATIONS` group is enabled because these operations are mandatory for projects that have a12-client as
		 * frontend application. Please make sure to enable this group if you have your own configuration.
		 *
		 * @default `A12_INTERNAL_OPERATIONS`
		 */
		private Set<String> allowedOperations = Set.of(CoreOperationConstants.A12_INTERNAL_OPERATIONS_GROUP);
		private Spel spel = new Spel();

		@Data
		public static class Spel {
			/**
			 * Enables use of placeholder resolution in JSON-RPC requests.
			 *
			 * @default `false`
			 */
			private boolean enabled = false;
		}

	}

	/**
	 * @topic jobs
	 */
	@Data
	public static class Jobs {

		private AttachmentJobs attachments = new AttachmentJobs();
		private RelationshipJobs relationships = new RelationshipJobs();
		private RequestJobs requests = new RequestJobs();

		/**
		 * Enable All jobs. See link:https://www.quartz-scheduler.org/[Quartz documentation,window=_blank].
		 *
		 * @default `true`
		 */
		private boolean enabled = true;

		@Data
		public static class AttachmentJobs {

			private Cleanup cleanup = new Cleanup();
			private CleanUpDirtyAttachments cleanUpDirtyAttachments = new CleanUpDirtyAttachments();
			private CleanUpStaleAttachments cleanUpStaleAttachments = new CleanUpStaleAttachments();
			private Temporary temporary = new Temporary();

			@Data
			public static class Cleanup {

				/**
				 * Cron expression to plan attachment cleanup job. See the link:https://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html[Quartz Trigger tutorial,window=_blank].
				 *
				 * @default "0 *&#47;5 * * * ?"
				 */
				private String schedule = CLEANUP_JOB_DEFAULT_SCHEDULE;
			}

			@Data
			public static class CleanUpDirtyAttachments {

				/**
				 * Cron expression to plan attachment cleanup job. See the link:https://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html[Quartz Trigger tutorial,window=_blank].
				 *
				 * @default "0 *&#47;5 * * * ?"
				 */
				private String schedule = CLEANUP_JOB_DEFAULT_SCHEDULE;
			}

			@Data
			public static class CleanUpStaleAttachments {

				/**
				 * Cron expression to plan attachment cleanup job. See the link:https://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html[Quartz Trigger tutorial,window=_blank].
				 *
				 * @default "0 *&#47;5 * * * ?"
				 */
				private String schedule = CLEANUP_JOB_DEFAULT_SCHEDULE;
			}

			@Data
			public static class Temporary {
				/**
				 * Time in hours after which a temporary attachment will be deleted
				 *
				 * @default `48`
				 */
				private int expireHours = 48;

				/**
				 * A map that holds the time in hours after which a temporary attachments in specified context will be deleted.
				 *
				 * @default `null`
				 */
				private Map<String, Integer> contextExpireHours = new HashMap<>();
			}
		}

		@Data
		public static class RelationshipJobs {

			private RankRecalculation rankRecalculation = new RankRecalculation();

			@Data
			public static class RankRecalculation {
				/**
				 * Enables/disables the rank reorder scheduler job.
				 *
				 * @default false
				 */
				private boolean enabled = false;

				/**
				 * Cron schedule to trigger recalculation of all assigned link ranks.
				 *
				 * @default `null`
				 */
				private String schedule;

				/**
				 * List of relationship model names whose document's ranks should be reordered by the job.
				 *
				 * @default `null`
				 */
				private List<String> rmsToReorder;
			}
		}

		@Data
		public static class RequestJobs {

			private CleanupRequestId cleanupRequestId = new CleanupRequestId();

			@Data
			public static class CleanupRequestId {

				/**
				 * Cron expression to plan request_id cleanup job. See the link:https://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html[Quartz Trigger tutorial,window=_blank].
				 *
				 * @default "0 *&#47;5 * * * ?"
				 */
				private String schedule = CLEANUP_JOB_DEFAULT_SCHEDULE;

				/**
				 * The amount of time in hours after which an idempotence id (entry in table request_id) will be deleted.
				 *
				 * @default `720` (i.e. one month)
				 */
				private int expireHours = 720;
			}

		}
	}

	/**
	 * @topic cache
	 */
	@Data
	public static class Cache {

		private ModelGraph modelGraph = new ModelGraph();

		@Data
		public static class ModelGraph {
			/**
			 * Switch on for better production performance for LIST_* operations.
			 *
			 * @default `true`
			 */
			private boolean enabled = true;
		}
	}

	/**
	 * @topic logger
	 */
	@Data
	public static class Logging {

		private Anonymization anonymization = new Anonymization();

		@Data
		public static class Anonymization {
			/**
			 * Control whether to render anonymous sensitive data for logging.
			 *
			 * @default `true`
			 */
			private boolean enabled = true;
		}
	}

	@Data
	public static class Server {
		/**
		 * Properties for the exception mappers
		 *
		 * @topic core
		 */
		private ExceptionMapping exceptionMapping = new ExceptionMapping();

		/**
		 * Mappings in Data Services have the following structure: SPRING_CONTEXT_PATH/DATA_SERVICES_CONTEXT_PATH/...
		 * This property should be used to set DATA_SERVICES_CONTEXT_PATH (if you want to set SPRING_CONTEXT_PATH use `server.servlet.contextPath instead`).
		 * Its purpose is to give an ability to differentiate with DATA_SERVICES_CONTEXT_PATH by introducing your own context path variable.
		 * NOTES:
		 * 1. Don't put leading '/' if SPRING_CONTEXT_PATH has trailing '/'. It will result in '//' prefix in the mappings.
		 * 2. There is a configuration called `mgmtp.a12.uaa.authentication.contextPath`. It should be equal to this property for the application to function properly.
		 *
		 * @default `/api`
		 * @topic core
		 */
		private String contextPath = "/api";

		@Data
		public static class ExceptionMapping {
			/**
			 * Defines whether the exception should be added to the header
			 * of responses in the exception mappers.
			 *
			 * @topic core
			 * @default `false`
			 */
			private boolean shouldAddExceptionToHeader = false;
		}
	}

	/**
	 * @topic CDD
	 */
	@Data
	public static class Cdd {

		private Model model = new Model();
		private Export export = new Export();

		@Data
		public static class Model {

			private ModificationAfterInitialization modificationAfterInitialization = new ModificationAfterInitialization();

			@Data
			public static class ModificationAfterInitialization {
				/**
				 * Enables/disables CDM readonly after initialization.
				 *
				 * @default false
				 */
				private boolean enabled = false;
			}
		}

		@Data
		public static class Export {
			private Csv csv = new Csv();

			/**
			 * Hard limit for max export row size for list cdd.
			 *
			 * @default `65536`
			 */
			private int maxRowSize = 65536;

			@Data
			public static class Csv {
				/**
				 * The delimiter used in exported csv file
				 *
				 * @default `;`
				 */
				private Character delimiter = ';';
			}

			/**
			 * Specifies the canonical name of the character set that is used to encode the content saved to the storage.
			 * Allowed values depend on the JDK in use. Most common encodings (canonical names) are
			 *
			 * - ISO-8859-1: ISO-8859-1, Latin Alphabet No. 1
			 * - UTF-8: Eight-bit Unicode (or UCS) Transformation Format
			 * - US-ASCII: American Standard Code for Information Interchange
			 *
			 * @default `UTF-8`
			 */
			private String charset = "UTF-8";
		}
	}

	/**
	 * @topic Authorization
	 */
	@Data
	public static class Authorization {
		private RoleBased roleBased = new RoleBased();
		private BackendJob backendJob = new BackendJob();

		@Data
		public static class RoleBased {
			/**
			 * Configuration for role based authorization.
			 * If value is false, DS will disable all model based authorization.
			 *
			 * @default `true`
			 */
			private boolean enabled = true;
		}

		@Data
		public static class BackendJob {

			private Principal principal = new Principal();

			@Data
			public static class Principal {

				/**
				 * Configuration for defining backend job username.
				 * This user is used in the following places:
				 *
				 * - initialization of the application.
				 * - link rank defragmentation.
				 * - kernel cache preloader.
				 *
				 * This implies that the user must have at least permissions to modify documents and models.
				 * Additionally, it must have permission to all actions
				 * executed in the events handlers provided as customization and also to all actions executed from RPC initializer if provided.
				 *
				 * So, the recommended set of permissions is at least:
				 *
				 * - `Model Read`
				 * - `Model Create`
				 * - `Model Update`
				 * - `Query`
				 * - `Document Create`
				 * - `Document Update`
				 * - `Document Delete`
				 *
				 * @default `superUser`
				 */
				private String username = "superUser";
			}
		}
	}
}
