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
package com.mgmtp.a12.dataservices.attachment.persitence.internal;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Supplier;

import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import org.springframework.util.MimeTypeUtils;

import com.mgmtp.a12.dataservices.attachment.AttachmentService;
import com.mgmtp.a12.dataservices.attachment.AttachmentThumbnailUrl;
import com.mgmtp.a12.dataservices.attachment.AttachmentUrl;
import com.mgmtp.a12.dataservices.attachment.DataServicesThumbnail;
import com.mgmtp.a12.dataservices.attachment.ThumbnailType;
import com.mgmtp.a12.dataservices.attachment.internal.AttachmentMapper;
import com.mgmtp.a12.dataservices.attachment.internal.jpa.entity.AttachmentHeaderEntity;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;

@Slf4j
@RequiredArgsConstructor public class ThumbnailUtil {

	private final AttachmentService attachmentService;
	private final AttachmentMapper attachmentMapper;

	public static final String THUMBNAIL_FORMAT = "PNG";

	public AttachmentThumbnailUrl getThumbnailUrl(@NonNull String attachmentId) {
		AttachmentThumbnailUrl.AttachmentThumbnailUrlBuilder attachmentThumbnailUrlBuilder = AttachmentThumbnailUrl.builder();

		attachmentThumbnailUrlBuilder.bigThumbnailUrl(attachmentService.findThumbnailUrl(attachmentId, ThumbnailType.BIG)
			.map(AttachmentUrl::getLocation)
			.orElse(null));

		attachmentThumbnailUrlBuilder.smallThumbnailUrl(attachmentService.findThumbnailUrl(attachmentId, ThumbnailType.SMALL)
			.map(AttachmentUrl::getLocation)
			.orElse(null));

		return attachmentThumbnailUrlBuilder.build();
	}

	public AttachmentThumbnailUrl getThumbnailUrl(@NonNull AttachmentHeaderEntity attachmentHeader) {
		AttachmentThumbnailUrl.AttachmentThumbnailUrlBuilder attachmentThumbnailUrlBuilder = AttachmentThumbnailUrl.builder();

		attachmentThumbnailUrlBuilder.bigThumbnailUrl(attachmentService.findThumbnailUrl(attachmentMapper.toAttachmentHeader(attachmentHeader), ThumbnailType.BIG)
			.map(AttachmentUrl::getLocation)
			.orElse(null));

		attachmentThumbnailUrlBuilder.smallThumbnailUrl(attachmentService.findThumbnailUrl(attachmentMapper.toAttachmentHeader(attachmentHeader), ThumbnailType.SMALL)
			.map(AttachmentUrl::getLocation)
			.orElse(null));

		return attachmentThumbnailUrlBuilder.build();
	}

	public static Optional<DataServicesThumbnail> convertToDSThumbnail(InputStream thumbnailContent, ThumbnailType type, int size,
		DataServicesCoreProperties.Attachments.Thumbnail thumbnailConfig) {
		if (thumbnailConfig.getOptimization().getPerformance().isEnabled()) {
			return convertToDSThumbnailByGraphics2D(thumbnailContent, type, size);
		} else {
			return convertToDSThumbnailByThumbnailator(thumbnailContent, type, size);
		}
	}

	public static Optional<DataServicesThumbnail> convertToDSThumbnailByThumbnailator(InputStream thumbnailContent, ThumbnailType type, int size) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			Thumbnails
				.of(thumbnailContent)
				.width(size)
				.imageType(BufferedImage.TYPE_INT_ARGB)
				.outputFormat(THUMBNAIL_FORMAT)
				.toOutputStream(baos);

			return Optional.of(baos)
				.map(ByteArrayOutputStream::toByteArray)
				.map(b -> (Supplier<ByteArrayInputStream>) () -> new ByteArrayInputStream(b))
				.map(DataServicesThumbnail.builder()::content)
				.map(b -> b.mimeType(getImageMimeType()))
				.map(b -> b.type(type))
				.map(DataServicesThumbnail.DataServicesThumbnailBuilder::build);
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	public static Optional<DataServicesThumbnail> convertToDSThumbnailByGraphics2D(InputStream thumbnailContent, ThumbnailType type, int requestedSize) {
		try {
			BufferedImage originalImage = ImageIO.read(new MemoryCacheImageInputStream(thumbnailContent));
			if (originalImage == null) {
				return Optional.empty();
			}

			BufferedImage resizedImage = getResizedImage(requestedSize, originalImage);
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
				// MemoryCacheImageOutputStream is enforced to not store anything to the disk, because of security reasons.
				ImageIO.write(resizedImage, "png", new MemoryCacheImageOutputStream(baos));
				resizedImage.flush();
				return Optional.of(baos)
					.map(ByteArrayOutputStream::toByteArray)
					.map(b -> (Supplier<InputStream>) () -> new ByteArrayInputStream(b))
					.map(DataServicesThumbnail.builder()::content)
					.map(b -> b.mimeType(getImageMimeType()))
					.map(b -> b.type(type))
					.map(DataServicesThumbnail.DataServicesThumbnailBuilder::build);
			}
		} catch (IOException e) {
			return Optional.empty();
		}

	}

	@NonNull private static BufferedImage getResizedImage(int requestedSize, BufferedImage originalImage) {
		// try to keep an aspect ratio when creating thumbnail.
		ImageSize imageSize = computeThumbnailDimensions(requestedSize, originalImage);

		BufferedImage resizedImage;
		if (imageSize.thumbnailHeight == originalImage.getHeight() && imageSize.thumbnailWidth == originalImage.getWidth()) {
			resizedImage = originalImage;
		} else {
			resizedImage = new BufferedImage(imageSize.thumbnailWidth(), imageSize.thumbnailHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics2D graphics2D = resizedImage.createGraphics();
			graphics2D.drawImage(originalImage, 0, 0, imageSize.thumbnailWidth(), imageSize.thumbnailHeight(), null);
			graphics2D.dispose();
		}
		return resizedImage;
	}

	@NonNull private static ImageSize computeThumbnailDimensions(int size, BufferedImage originalImage) {
		if (originalImage.getHeight() > originalImage.getWidth()) {
			return new ImageSize(
				size,
				originalImage.getWidth() * size / originalImage.getHeight());
		} else {
			return new ImageSize(
				originalImage.getHeight() * size / originalImage.getWidth(),
				size);
		}
	}

	private record ImageSize(int thumbnailHeight, int thumbnailWidth) {
	}

	public static String getImageMimeType() {
		return MimeTypeUtils.IMAGE_PNG_VALUE;
	}
}
