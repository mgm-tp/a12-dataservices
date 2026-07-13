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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.mgmtp.a12.contentstore.client.configuration.ContentStoreClientProperties;
import com.mgmtp.a12.dataservices.attachment.AttachmentUrl;
import com.mgmtp.a12.dataservices.attachment.TypeOfTheContent;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.entity.AttachmentHeaderEntity;
import com.mgmtp.a12.dataservices.attachment.persitence.IAttachmentRepository;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.constants.SmeWorkspaceConstants.FULL_ATTACHMENT_PATH;

/**
 * Exports attachments to TAR archive.
 */
@Slf4j @RequiredArgsConstructor
@Component public class AttachmentExporter extends AbstractTarExporter<List<AttachmentHeaderEntity>> {

	private final Optional<IAttachmentRepository> attachmentRepository;
	private final RestTemplate restTemplate = new RestTemplate();
	private final HttpServletRequest httpServletRequest;
	private final ContentStoreClientProperties contentStoreClientProperties;

	/**
	 * Performs the actual attachments export logic.
	 *
	 * @param taos The TAR archive output stream
	 * @param attachmentHeaderEntities List of attachment headers to export
	 */
	@Override protected void exportLogic(TarArchiveOutputStream taos, List<AttachmentHeaderEntity> attachmentHeaderEntities) {
		attachmentRepository.ifPresentOrElse(
			repository -> attachmentHeaderEntities.forEach(e -> {
				Optional<AttachmentUrl> attachmentUrlOptional = repository.findUrl(e.getId(), e.getFileName(),
					TypeOfTheContent.valueOf(e.getTypeOfTheContent()));
				try {
					if (attachmentUrlOptional.isPresent()) {
						writeFileToTar(taos, downloadFile(attachmentUrlOptional.get().getLocation()),
							FULL_ATTACHMENT_PATH.resolve(e.getId() + MimeTypes.getDefaultMimeTypes().forName(e.getMimeType()).getExtension()));
					}
				} catch (MimeTypeException ex) {
					throw new UnexpectedException("Error getting attachment extension", ex);
				}
			}),
			() -> log.warn("Attachments are disabled; skipping export of {} attachment(s)", attachmentHeaderEntities.size())
		);
	}

	/**
	 * Downloads a file from the Content Store and returns its content as a byte array.
	 *
	 * @param fileUrl The URL of the file to download.
	 * @return The file content as a byte array
	 */
	private byte[] downloadFile(String fileUrl) {
		if (!isAbsoluteURL(fileUrl)) {
			fileUrl = getAbsoluteUrl(fileUrl);
		}

		ResponseEntity<byte[]> response = restTemplate.exchange(
			fileUrl,
			HttpMethod.GET,
			null,
			byte[].class
		);

		return response.getBody();
	}

	private String getAbsoluteUrl(String path) {
		try {
			String remoteUrl = contentStoreClientProperties.getConfiguration().getRemoteUrl();
			String baseUrl = (remoteUrl != null && !remoteUrl.isBlank())
				? remoteUrl
				: httpServletRequest.getRequestURL().toString();
			return UriComponentsBuilder.fromUriString(baseUrl)
				.replacePath("/")
				.path(path)
				.build()
				.normalize()
				.toUriString();
		} catch (Exception e) {
			throw new UnexpectedException("Error when get attachment with " + path, e).withAnonymityMessage("Error when getting attachment.");
		}
	}

	private static boolean isAbsoluteURL(String urlString) {
		try {
			return new URI(urlString).isAbsolute();
		} catch (URISyntaxException e) {
			return false;
		}
	}
}
