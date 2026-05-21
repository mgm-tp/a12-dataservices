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


import lombok.Data;
import lombok.NonNull;

import static org.apache.commons.lang3.SystemProperties.USER_HOME;

/**
 * Configuration class for seed data initialization.
 * This class defines the structure and properties for managing seed data, including import, export, delete, and metadata settings.
 *
 * @topic seed data
 */
@Data
public class SeedDataProperties {

	@NonNull private Initialization initialization = new Initialization();
	@NonNull private SeedData seedData = new SeedData();


	/**
	 * Nested class representing initialization settings.
	 */
	@Data
	public static class Initialization {

		private SeedData seedData = new SeedData();

		/**
		 * Nested class representing seed file settings.
		 */
		@Data
		public static class SeedData {

			/**
			 * Flag indicating if seed data initialization is enabled.
			 * If enabled, the default dataServicesInitializationService bean is disabled, and seed data will be used for initialization.
			 */
			private boolean enabled = false;

			private SeedFile seedFile = new SeedFile();

			@Data
			public static class SeedFile {
				/**
				 * Path to the seed file.
				 */
				private String path = "";
			}
		}

	}
	/**
	 * Nested class representing seed data settings.
	 */
	@Data
	public static class SeedData {

		private Import imports = new Import();

		private Export export = new Export();

		private Delete delete = new Delete();

		private MetaData metaData = new MetaData();

		/**
		 * Gets the import settings.
		 *
		 * @return The import settings.
		 */
		public Import getImport() {
			return imports;
		}

		/**
		 * Sets the import settings.
		 *
		 * @param imports The import settings to set.
		 */
		public void setImport(Import imports) {
			this.imports = imports;
		}

		/**
		 * Nested class representing import settings.
		 */
		@Data
		public static class Import {
			/**
			 * Flag indicating if import is enabled.
			 */
			private boolean enabled = false;
		}

		/**
		 * Nested class representing export settings.
		 */
		@Data
		public static class Export {
			/**
			 * Flag indicating if export is enabled.
			 */
			private boolean enabled = false;
		}

		/**
		 * Nested class representing delete settings.
		 */
		@Data
		public static class Delete {
			/**
			 * Flag indicating if delete is enabled.
			 */
			private boolean enabled = false;
		}

		/**
		 * Nested class representing metadata settings.
		 */
		@Data
		public static class MetaData {
			/**
			 * Path to metadata json file.
			 * @default `${user.home}/a12/dataservices/meta/seed_metadata.json`
			 * @example `/var/lib/a12/dataservices/meta/seed_metadata.json`
			 */
			private String path = System.getProperty(USER_HOME).concat("/a12/dataservices/meta/seed_metadata.json");
		}
	}

}
