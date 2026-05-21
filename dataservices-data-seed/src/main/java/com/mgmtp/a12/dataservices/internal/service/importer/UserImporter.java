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
package com.mgmtp.a12.dataservices.internal.service.importer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.time.StopWatch;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.model.LocalUserHolder;
import com.mgmtp.a12.uaa.authentication.principal.autoconfigure.AuthenticationPrincipalExtensionProperties;
import com.mgmtp.a12.uaa.authentication.user.LocalUser;
import com.mgmtp.a12.uaa.authentication.user.LocalUserManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserImporter extends AbstractImporter<Void, Void> {
	private static final String USERS_FILE = "users.yaml";

	private final AuthenticationPrincipalExtensionProperties authenticationPrincipalExtensionProperties;
	private final Optional<LocalUserManager> localUserManagerOpt;

	private final ObjectMapper yamlObjectMapper = YAMLMapper.builder()
		.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
		.build();

	public Void doImportLogic(Path usersPath, Void metadata) {
		try {
			StopWatch sw = StopWatch.createStarted();
			byte[] contents = Files.readAllBytes(usersPath);
			LocalUserHolder localUserHolder = yamlObjectMapper.readValue(
				contents,
				LocalUserHolder.class
			);

			Map<String, FileUrlResource> currentUsers = getCanOverrideUser();
			List<Resource> userResources = new ArrayList<>();

			if (currentUsers.containsKey(USERS_FILE)) {
				FileUrlResource fileUrlResource = currentUsers.get(USERS_FILE);
				Files.copy(new ByteArrayInputStream(yamlObjectMapper.writeValueAsBytes(localUserHolder)),
					fileUrlResource.getFile().toPath(),
					StandardCopyOption.REPLACE_EXISTING);
				userResources.add(fileUrlResource);
			} else {
				for (LocalUser user : localUserHolder.users()) {
					String username = user.getUsername();
					if (currentUsers.containsKey(username)) {
						FileUrlResource fileUrlResource = currentUsers.get(username);
						Files.copy(new ByteArrayInputStream(yamlObjectMapper.writeValueAsBytes(user)),
							fileUrlResource.getFile().toPath(),
							StandardCopyOption.REPLACE_EXISTING);
						userResources.add(fileUrlResource);
					} else {
						userResources.add(new ByteArrayResource(yamlObjectMapper.writeValueAsBytes(user)));
					}
				}
			}

			authenticationPrincipalExtensionProperties.getLocalConfig().setUserResources(userResources.toArray(new Resource[0]));
			localUserManagerOpt.ifPresent(localUserManager ->
				localUserManager.reloadUsers(new String(contents, StandardCharsets.UTF_8)));
			log.debug("Users imported in {}", sw.formatTime());
		} catch (IOException e) {
			throw new InvalidInputException(
				ExceptionKeys.EXPORT_SEED_DATA_IMPORT_ERROR_KEY,
				"Cannot parse user",
				e
			);
		}
		return null;
	}

	@NotNull private Map<String, FileUrlResource> getCanOverrideUser() {
		Map<String, FileUrlResource> currentUsers = new HashMap<>();
		Resource[] userResources = authenticationPrincipalExtensionProperties.getLocalConfig().getUserResources();
		if (userResources == null) {
			return currentUsers;
		}
		Arrays.stream(userResources)
			.forEach(resource -> {
				if (resource instanceof FileUrlResource fileUrlResource) {
					try {
						if (Objects.equals(fileUrlResource.getFilename(), USERS_FILE)) {
							currentUsers.put(USERS_FILE, fileUrlResource);
						} else {
							LocalUser localUser = yamlObjectMapper.readValue(fileUrlResource.getContentAsByteArray(), LocalUser.class);
							currentUsers.put(localUser.getUsername(), fileUrlResource);
						}
					} catch (IOException e) {
						log.warn("Cannot load user from {}", fileUrlResource.getURL().getPath());
					}
				}
			});
		return currentUsers;
	}
}
