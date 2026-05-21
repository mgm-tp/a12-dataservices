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
package com.mgmtp.a12.dataservices.model.bulkload.internal;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.model.metadata.DocumentModelMetadataInjectorFactory;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.DocumentModelExpansionException;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelReferenceResolver;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class CollapsingDocumentModelReferenceResolver implements IDocumentModelReferenceResolver {

	private final IDocumentModelResolver documentModelResolver;
	private final DocumentModelUtils documentModelUtils;
	private final IDocumentModelService iDocumentModelService;
	private final Map<String, IDocumentModel> models = new HashMap<>();
	private final DocumentModelMetadataInjectorFactory documentModelMetadataInjectorFactory;
	private final Locale defaultLocale;

	@Override public IDocumentModel getDocumentModel(String modelId) {
		return Optional.ofNullable(models.get(modelId))
			.or(() -> Optional.ofNullable(documentModelResolver)
				.map(r -> r.getDocumentModelById(modelId)))
			.map(dm -> {
				iDocumentModelService.collapse(dm);
				dm = documentModelMetadataInjectorFactory.getInstance(dm, defaultLocale).getDocumentModelWithoutMetadata();
				return dm;
			})
			.orElseThrow(() -> new DocumentModelExpansionException(String.format("Cannot resolve included model [%s]", modelId)));
	}

	public Collection<IDocumentModel> getModels() {
		return models.values();
	}

	public void addModel(Resource resource) {
		try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
			Optional.of(reader)
				.map(documentModelUtils::deserializeDocumentModel)
				.ifPresentOrElse(this::addModel,
					() -> log.warn("There is no model in {}", resource));
		} catch (IOException e) {
			log.error("Failed to expand {}", resource);
			throw new UnexpectedException(e).withAnonymityMessage("Adding model failed.");
		}
	}

	public void addModel(IDocumentModel model) {
		models.put(model.getHeader().getId(), model);
	}

	@RequiredArgsConstructor
	@Component public static class CollapsingDocumentModelReferenceResolverFactory {

		private final IDocumentModelService documentModelService;
		private final DocumentModelUtils documentModelUtils;
		private final DocumentModelMetadataInjectorFactory documentModelMetadataInjectorFactory;
		private final Locale defaultLocale;

		public CollapsingDocumentModelReferenceResolver getInstance(IDocumentModelResolver documentResolver) {
			return new CollapsingDocumentModelReferenceResolver(documentResolver, documentModelUtils, documentModelService,
				documentModelMetadataInjectorFactory, defaultLocale);
		}
	}
}
