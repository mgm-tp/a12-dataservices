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
package com.mgmtp.a12.dataservices.internal.rest;

import org.springframework.context.annotation.Conditional;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.mgmtp.a12.dataservices.internal.condition.ExportSeedDataEnabledCondition;
import com.mgmtp.a12.dataservices.internal.service.SeedDataService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This API provides the ability to export Seed Data.
 *
 * @title Internal Seed Data Export REST API
 * @topic Seed Data (Internal API): For testing and demo purposes only. Not intended for production use.
 */

@Slf4j
@RequiredArgsConstructor
@RequestMapping("#{@dataServicesCoreProperties.server.contextPath}/internal/seed-data")
@Conditional(ExportSeedDataEnabledCondition.class)
@RestController public class ExportSeedDataController {

	private final SeedDataService seedDataService;

	/**
	 * Endpoint allows downloading the compressed archive of the seed data, exported folder example:
	 *
	 * - /data/meta/seed_metadata.json
	 * - /data/models/Contract.json
	 * - /data/models/BusinessPartner.json
	 * - /data/models/ContractBusinessPartner.json
	 * - /data/attachments/e4csdw43-6a00-418a-a7d6-d9f5b7f82df2.jpg
	 * - /data/attachments/e4cbe2wa-2efd-418a-12ed-d9f5b7fd3wf0.cert
	 * - /data/documents/Contract/8ed0be43-bd0c-438c-a0de-c8e8712c83b1.json
	 * - /data/documents/BusinessPartner/8ed0b2eds-2csa-2wsa-asf2-c8e8712c83b1.json
	 * - /data/links/ContractBusinessPartner/8fd0b2eds-2csa-2wsa-asf2-c8e8712c83b2.json
	 * - /data/links/ContractBusinessPartner/8ad0b2eds-2csa-2wsa-asf2-c8e8712c83b3.json
	 * - /data/user/users.yaml
	 *
	 * @return {@link ResponseEntity#ok()} in case of success. Response body is {@link InputStreamResource}.
	 * @responseSuccess 200 OK:: File is ready to be downloaded.
	 * @title Export Seed Data Content
	 */
	@GetMapping(path = { "", "/" })
	public StreamingResponseBody export(@RequestParam(required = false) boolean includeModels) {
		log.debug("Exporting seed-data");
		return outputStream -> seedDataService.exportAllData(outputStream, includeModels);
	}
}
