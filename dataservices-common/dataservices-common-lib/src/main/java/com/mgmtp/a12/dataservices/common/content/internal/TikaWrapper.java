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
package com.mgmtp.a12.dataservices.common.content.internal;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.util.Optional;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TemporaryResources;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MediaType;

import com.google.common.jimfs.Jimfs;

import static com.google.common.jimfs.Configuration.unix;

/**
 * Class wraps implementation of tika, provide factory for initiating this class with extra logic that allows to persist temporary files in memory.
 */
public class TikaWrapper implements AutoCloseable, Closeable {

	private final TikaInputStream tikaInputStream;
	private Detector detector;

	private TikaWrapper(TikaInputStream inputStream, Detector detector) {
		tikaInputStream = inputStream;
		this.detector = detector;
	}

	public long getLength() throws IOException {
		return tikaInputStream.getLength();
	}

	public InputStream getInputStream() {
		return tikaInputStream;
	}

	public MediaType detect(String filename) throws IOException {
		Metadata metadata = new Metadata();
		// the key must be present in the metadata to trigger the magic detection. Value is not needed.
		metadata.add(Metadata.MIME_TYPE_MAGIC, null);
		Optional.ofNullable(filename)
			.ifPresent(f -> metadata.add(TikaCoreProperties.RESOURCE_NAME_KEY, filename));
		return detector.detect(tikaInputStream, metadata);
	}

	@Override public void close() throws IOException {
		tikaInputStream.close();
	}

	public static class TikaWrapperFactory {

		private final Detector detector;
		private final boolean isInMemFileSystem;

		public TikaWrapperFactory(boolean isInMemFileSystem) {
			this.isInMemFileSystem = isInMemFileSystem;
			detector = TikaConfig.getDefaultConfig().getDetector();
		}

		public TikaWrapper get(byte[] bytes) {
			return new TikaWrapper(TikaInputStream.get(new ByteArrayInputStream(bytes), getTemporaryResources(), null), detector);
		}

		public TikaWrapper get(InputStream inputStream) {
			return new TikaWrapper(TikaInputStream.get(inputStream, getTemporaryResources(), null), detector);
		}

		private TemporaryResources getTemporaryResources() {
			TemporaryResources temporaryResources = new TemporaryResources();
			if (isInMemFileSystem) {
				FileSystem fileSystem = Jimfs.newFileSystem(unix());
				temporaryResources.setTemporaryFileDirectory(fileSystem.getPath("/"));
				temporaryResources.addResource(fileSystem);
			}
			return temporaryResources;
		}
	}
}
