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
package com.mgmtp.a12.dataservices.document;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mgmtp.a12.dataservices.marshalling.JsonRawValueDeserializer;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * @deprecated Not used anymore, will be removed in the next breaking release.
 */
@Deprecated(since = "38.1.0", forRemoval = true)
@XmlRootElement(name = "DocumentDetail")
@XmlAccessorType(XmlAccessType.FIELD)
public class DocumentDetail {

	@JsonProperty("id")
	private String id;

	@JsonProperty("type")
	private String type;

	@JsonProperty("createdUser")
	private String createdUser;

	@JsonProperty("changedUser")
	private String changedUser;

	@JsonProperty("createdDate")
	private Instant createdDate;

	@JsonProperty("changedDate")
	private Instant changedDate;

	@JsonProperty("body")
	@JsonRawValue
	@JsonDeserialize(using = JsonRawValueDeserializer.class)
	private String body;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public String getBody() {
		return body;
	}

	public void setBody(final String body) {
		this.body = body;
	}

	public String getCreatedUser() {
		return createdUser;
	}

	public void setCreatedUser(final String createdUser) {
		this.createdUser = createdUser;
	}

	public String getChangedUser() {
		return changedUser;
	}

	public void setChangedUser(final String changedUser) {
		this.changedUser = changedUser;
	}

	public Instant getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Instant createdDate) {
		this.createdDate = createdDate;
	}

	public Instant getChangedDate() {
		return changedDate;
	}

	public void setChangedDate(Instant changedDate) {
		this.changedDate = changedDate;
	}
}
