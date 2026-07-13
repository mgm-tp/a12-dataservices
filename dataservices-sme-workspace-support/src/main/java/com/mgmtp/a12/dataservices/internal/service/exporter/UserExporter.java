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
package com.mgmtp.a12.dataservices.internal.service.exporter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.internal.model.LocalUserHolder;
import com.mgmtp.a12.uaa.authentication.principal.autoconfigure.AuthenticationPrincipalExtensionProperties;
import com.mgmtp.a12.uaa.authentication.user.LocalUser;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

import static com.mgmtp.a12.dataservices.constants.SmeWorkspaceConstants.FULL_USER_PATH;
import static tools.jackson.dataformat.yaml.YAMLWriteFeature.WRITE_DOC_START_MARKER;

/**
 * Exports users to TAR archive in YAML format.
 */

@RequiredArgsConstructor
@Component public class UserExporter extends AbstractTarExporter<Void> {

	private final AuthenticationPrincipalExtensionProperties authenticationPrincipalExtensionProperties;
	private final ResourcePatternResolver resourcePatternResolver;
	private final ObjectMapper yamlObjectMapper = YAMLMapper.builder()
		.disable(WRITE_DOC_START_MARKER)
		.build();

	@Override protected void exportLogic(TarArchiveOutputStream tarStream, Void unused) {
		try {
			writeFileToTar(tarStream, yamlObjectMapper.writeValueAsBytes(prepareLocalUserHolder()), FULL_USER_PATH);
		} catch (JacksonException e) {
			throw new UnexpectedException("Error when exporting user", e);
		}
	}

	@NonNull private LocalUserHolder prepareLocalUserHolder() {
		return new LocalUserHolder(Arrays.stream(authenticationPrincipalExtensionProperties.getLocalConfig().getUserResources())
			.map(this::checkAndGetResourcesByWildCard)
			.flatMap(Stream::of)
			.map(this::getLocalUser)
			.flatMap(List::stream)
			.filter(Objects::nonNull)
			.collect(Collectors.toMap(LocalUser::getUsername, obj -> obj, (existing, replacement) -> replacement))
			.values().stream()
			.toList());
	}

	private List<LocalUser> getLocalUser(Resource r) {
		try {
			String content = r.getContentAsString(StandardCharsets.UTF_8);
			if (content.startsWith("users")) {
				return yamlObjectMapper.readValue(content, LocalUserHolder.class).users();
			} else {
				return List.of(yamlObjectMapper.readValue(content, LocalUser.class));
			}
		} catch (IOException | JacksonException e) {
			throw new UnexpectedException("Error when reading user", e);
		}
	}

	private Resource[] checkAndGetResourcesByWildCard(Resource resource) {
		try {
			if (resource instanceof ByteArrayResource) {
				return new Resource[] { resource };
			}
			String path = (!resource.isFile() && resource instanceof ClassPathResource pathResource)
				? "classpath:" + pathResource.getPath()
				: "file:" + resource.getURL().getPath();
			if (path.contains("*")) {
				return resourcePatternResolver.getResources(path);
			}
			return new Resource[] { resource };
		} catch (IOException e) {
			return new Resource[] {};
		}
	}
}
