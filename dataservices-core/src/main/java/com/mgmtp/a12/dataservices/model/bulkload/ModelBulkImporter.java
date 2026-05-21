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
package com.mgmtp.a12.dataservices.model.bulkload;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnmappableCharacterException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.exception.SecurityException;
import com.mgmtp.a12.dataservices.model.GenericModel;
import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.dataservices.model.bulkload.internal.CollapsingDocumentModelReferenceResolver;
import com.mgmtp.a12.dataservices.model.events.ModelsAfterImportEvent;
import com.mgmtp.a12.dataservices.utils.internal.ModelUtils;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.dataservices.utils.internal.DsResourceUtils;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.header.HeaderImpl;
import com.mgmtp.a12.model.header.HeaderParseException;
import com.mgmtp.a12.model.header.HeaderParser;
import com.mgmtp.a12.model.repository.ResourceAccessException;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.model.ModelConstants.DOCUMENT_MODEL_TYPE;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

/**
 * Responsible for bulk import of models. Supported types are zip and jar available on filesystem or classpath.
 * Additionally, class is responsible for resolving of include definitions in the document models.
 *
 * - No duplicate model names in bulk are allowed.
 * - Includes are resolved by model names. At first, included model is searched in the bulk and in case none is found or replace criteria is not met,
 * it will be searched in the database.
 */
@Slf4j
@Component public class ModelBulkImporter {

	@Autowired private DsResourceUtils dsResourceUtils;
	@Autowired private ModelService modelService;
	@Autowired private HeaderParser headerParser;
	@Autowired private DocumentModelUtils documentModelUtils;
	@Autowired private ApplicationEventPublisher eventPublisher;
	@Autowired private IDocumentModelResolver documentModelResolver;
	@Autowired private DataServicesCoreProperties dataServicesCoreProperties;
	@Autowired
	private CollapsingDocumentModelReferenceResolver.CollapsingDocumentModelReferenceResolverFactory collapsingDocumentModelReferenceResolverFactory;
	@Autowired private IDocumentModelService iDocumentModelService;

	/**
	 * Loads all models from the given location and persists them according to the configuration.
	 *
	 * @param bulkLocation root path of a batch of models. May point to a ZIP file, a directory, or a classpath location.
	 * @param configuration configuration defining overwrite behavior per model type; may be null to use defaults.
	 * @return List of imported model names.
	 * @throws IOException if reading model resources fails.
	 * @throws URISyntaxException if the location cannot be resolved to a valid URI (classpath or filesystem).
	 * @event {@link ModelsAfterImportEvent}
	 */
	public List<String> doImport(@NonNull String bulkLocation, BulkImporterConfiguration configuration) throws IOException, URISyntaxException {

		BulkImporterConfiguration config = defaultIfNull(configuration, new BulkImporterConfiguration());

		BulkImportProblemReporter bulkImportProblemReporter = new BulkImportProblemReporter();
		Stream<Header> importedModels = processModels(bulkLocation, config, bulkImportProblemReporter);
		bulkImportProblemReporter.validate();
		return importedModels
			.map(Header::getId)
			.sorted()
			.toList();
	}

	@NonNull private Stream<Header> processModels(@NonNull String bulkLocation, BulkImporterConfiguration config, BulkImportProblemReporter bulkImportProblemReporter) {
		try {
			CollapsingDocumentModelReferenceResolver importDmResolver = collapsingDocumentModelReferenceResolverFactory.getInstance(documentModelResolver);
			UniqueConstraint uniqueConstraint = new UniqueConstraint();
			List<Header> genericModels = importGenericModels(bulkLocation, config, bulkImportProblemReporter, importDmResolver, uniqueConstraint);
			Set<Header> importedModels = Stream.concat(
					genericModels.stream(),
					importDmResolver.getModels().stream()
						.map(m -> expandAndSaveDocumentModel(m, config, m.getHeader(), importDmResolver, bulkImportProblemReporter))
				)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
			eventPublisher.publishEvent(new ModelsAfterImportEvent(importedModels));
			return importedModels.stream();
		} catch (Exception e) {
			bulkImportProblemReporter.reportProblem(e);
			log.warn(e.getMessage(), e);
			return Stream.empty();
		}
	}

	private List<Header> importGenericModels(@NonNull String bulkLocation, BulkImporterConfiguration config,
		BulkImportProblemReporter bulkImportProblemReporter, CollapsingDocumentModelReferenceResolver importDmResolver,
		UniqueConstraint uniqueConstraint) throws IOException, URISyntaxException {
		return dsResourceUtils.getJsonResources(bulkLocation)
			.map(r -> saveGenericModelOrPostponeDocumentModel(config, bulkImportProblemReporter, importDmResolver, uniqueConstraint, r))
			.toList();
	}

	private @Nullable Header saveGenericModelOrPostponeDocumentModel(BulkImporterConfiguration config, BulkImportProblemReporter bulkImportProblemReporter,
		CollapsingDocumentModelReferenceResolver importDmResolver, UniqueConstraint uniqueConstraint, Resource r) {
		Header headerFromResource = new HeaderImpl();
		try {
			headerFromResource = getHeaderFromResource(r, uniqueConstraint);
			if (DOCUMENT_MODEL_TYPE.equals(headerFromResource.getModelType())) {
				importDmResolver.addModel(r);
				return null;
			} else {
				try (InputStream ip = r.getInputStream()) {
					String content = IOUtils.toString(ip, StandardCharsets.UTF_8);
					return getSavedHeader(headerFromResource, content, config.getOverwriteModel(headerFromResource.getModelType()));
				}
			}
		} catch (AccessDeniedException e) {
			throw createAccessDeniedException(headerFromResource.getId(), e);
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
			bulkImportProblemReporter.reportProblem(e);
			return null;
		}
	}

	private Header expandAndSaveDocumentModel(IDocumentModel m, BulkImporterConfiguration config, Header header,
		CollapsingDocumentModelReferenceResolver importDmResolver, BulkImportProblemReporter bulkImportProblemReporter) {
		iDocumentModelService.expand(m, importDmResolver);
		try {
			String documentModelContent = documentModelUtils.serializeDocumentModel(m);
			return getSavedHeader(header, documentModelContent, config.getOverwriteDocumentModels());
		} catch (ResourceAccessException e) {
			log.warn(e.getMessage(), e);
			bulkImportProblemReporter.reportProblem(
				(Exception) new InvalidInputException(ExceptionKeys.MODEL_BULK_IMPORT_HEADER_PARSING_ERROR_KEY, "Invalid model in the batch", e));
		} catch (AccessDeniedException e) {
			log.warn(e.getMessage(), e);
			bulkImportProblemReporter.reportProblem((Exception) createAccessDeniedException(header.getId(), e));
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
			bulkImportProblemReporter.reportProblem(e);
		}
		return null;
	}

	private Header getHeaderFromResource(Resource r, UniqueConstraint uniqueConstraint) {
		try {
			CharsetDecoder decoder =
				StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
			Header header = headerParser.parseJson(decoder.decode(ByteBuffer.wrap(r.getContentAsByteArray())).toString());
			ModelUtils.validateHeader(header, dataServicesCoreProperties.getAuthorization().getRoleBased().isEnabled());
			uniqueConstraint.validate(header.getId());
			return header;
		} catch (MalformedInputException | UnmappableCharacterException e) {
			throw new InvalidInputException(ExceptionKeys.MODEL_BULK_IMPORT_HEADER_PARSING_ERROR_KEY,
				"Resource %s is not encoded with UTF-8 charset".formatted(r), e).withAnonymityMessage("Decoding resource failed.");
		} catch (HeaderParseException | IOException e) {
			throw new InvalidInputException(ExceptionKeys.MODEL_BULK_IMPORT_HEADER_PARSING_ERROR_KEY, String.format("Unable to parse resource %s", r), e)
				.withAnonymityMessage("Resource header parsing failed.");
		}
	}

	@Nullable private Header getSavedHeader(Header header, String documentModelContent, boolean overwriteAllowed) {
		return Optional.ofNullable(createOrUpdateModel(header, documentModelContent, overwriteAllowed))
			.map(GenericModel::getHeader)
			.orElse(header);
	}

	private @Nullable GenericModel createOrUpdateModel(Header header, String documentModelContent, boolean overwriteAllowed) {
		if (!modelService.exists(header)) {
			return modelService.create(documentModelContent);
		} else if (overwriteAllowed) {
			return modelService.update(documentModelContent);
		} else {
			return null;
		}
	}

	private static class UniqueConstraint {
		final Set<String> processedIds = new HashSet<>();

		private void validate(String id) {
			if (processedIds.contains(id)) {
				throw new InvalidInputException(ExceptionKeys.MODEL_DUPLICITY_ERROR_KEY, String.format("Duplicate files for model %s", id));
			}
			processedIds.add(id);
		}
	}

	private SecurityException createAccessDeniedException(String modelName, Exception e) {
		return new SecurityException(ExceptionKeys.MODEL_ACCESS_DENIED_ERROR_KEY, String.format("Access to model [%s] has been denied!", modelName), e);
	}
}
