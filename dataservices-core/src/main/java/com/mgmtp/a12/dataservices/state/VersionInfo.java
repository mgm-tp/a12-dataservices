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
package com.mgmtp.a12.dataservices.state;

/**
 * Immutable snapshot of version metadata for the running application and its artifacts.
 * Intended for rendering build and runtime information (groupId, artifactId, Data Services and Classic versions).
 */
public class VersionInfo {

	private String groupId;
	private String artifactId;
	private String a12DataservicesVersion;
	private String a12ClassicVersion;

	/**
	 * Creates a new `VersionInfo`.
	 *
	 * @param groupId Maven group id; may be `null`.
	 * @param artifactId Maven artifact id; may be `null`.
	 * @param a12DataservicesVersion Data Services version string; may be `null`.
	 * @param a12ClassicVersion A12 Classic version string; may be `null`.
	 */
	public VersionInfo(String groupId, String artifactId, String a12DataservicesVersion, String a12ClassicVersion) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.a12DataservicesVersion = a12DataservicesVersion;
		this.a12ClassicVersion = a12ClassicVersion;
	}

	public String getA12ServicesVersion() {
		return a12DataservicesVersion;
	}

	public String getA12ClassicVersion() {
		return a12ClassicVersion;
	}

	public String getGroupId() {
		return groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}
}
