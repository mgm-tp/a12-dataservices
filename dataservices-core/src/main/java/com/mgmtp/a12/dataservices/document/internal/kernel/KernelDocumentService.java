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
package com.mgmtp.a12.dataservices.document.internal.kernel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.util.Assert;

import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.document.exception.DocumentComputationException;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.internal.DocumentationDiagram;
import com.mgmtp.a12.dataservices.utils.internal.GenericUtils;
import com.mgmtp.a12.kernel.core.customfieldtype.ICustomFieldTypeFactory;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentMultiPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.utils.IDocumentV2Visitor;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.rt.api.DocumentProcessingConfig;
import com.mgmtp.a12.kernel.md.rt.api.DocumentProcessingConfigBuilder;
import com.mgmtp.a12.kernel.md.rt.api.IComputedFieldInstance;
import com.mgmtp.a12.kernel.md.rt.api.ICustomConditionFactory;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentComputationResult;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentRtService;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentValidationResult;
import com.mgmtp.a12.model.header.Header;

import lombok.extern.slf4j.Slf4j;

/**
 * Service provides an integration layer for kernel API. Configuration is also applied.
 *
 * This service supports custom condition factories and custom field type factories which are
 * applied during document computation and validation operations via {@link DocumentProcessingConfig}.
 * These factories allow extending the kernel's validation and computation capabilities with
 * custom logic.
 */
@DocumentationDiagram
@Slf4j
public class KernelDocumentService {

	private final IDocumentRtService rtService;
	private final IDocumentModelResolver documentModelResolver;
	private final List<ICustomConditionFactory> customConditionFactories;
	private final List<ICustomFieldTypeFactory> customFieldTypeFactories;
	private final List<String> partialValidationForModels;
	private final List<String> skipValidationForModels;
	private final List<String> computationEnabledForModels;
	private final boolean validationEnabledByDefault;
	private final boolean enabledCleanupErrorAndNotComputedValue;

	public KernelDocumentService(boolean validationEnabledByDefault, List<String> partialValidationForModels,
		List<ICustomConditionFactory> customConditionFactories, List<ICustomFieldTypeFactory> customFieldTypeFactories,
		List<String> skipValidationForModels,
		List<String> computationEnabledForModels, IDocumentRtService rtService, IDocumentModelResolver documentModelResolver,
		boolean enabledCleanupErrorAndNotComputedValue) {

		if (partialValidationForModels != null && skipValidationForModels != null) {
			partialValidationForModels.stream()
				.distinct()
				.filter(skipValidationForModels::contains)
				.findAny()
				.ifPresent(c -> {
					throw new InvalidInputException(ExceptionKeys.MISCONFIGURATION_ERROR_KEY, "Model %s present in both: partialValidationForModels and skipValidationForModels.".formatted(c));
				});
		}

		this.customConditionFactories = customConditionFactories;
		this.customFieldTypeFactories = customFieldTypeFactories;
		this.validationEnabledByDefault = validationEnabledByDefault;
		this.partialValidationForModels = partialValidationForModels;
		this.skipValidationForModels = skipValidationForModels;
		this.computationEnabledForModels = computationEnabledForModels;
		this.rtService = rtService;
		this.documentModelResolver = documentModelResolver;
		this.enabledCleanupErrorAndNotComputedValue = enabledCleanupErrorAndNotComputedValue;
	}

	/**
	 * This wrapper provides checking if computation is enabled by configuration
	 */
	public DocumentV2 computeDocument(DocumentV2 document, Locale locale) {
		StopWatch stopWatch = StopWatch.createStarted();
		if (GenericUtils.matchOrAll(document.getDocumentModelId(), computationEnabledForModels)) {
			DocumentV2 computedDocV2 = compute(document, locale);
			log.trace("Computation for document [{}] finished in [{}] ms", document.getId(), stopWatch.getTime());
			return computedDocV2;
		}
		return document;
	}

	/**
	 * This wrapper provides checking if validation is enabled by configuration
	 */
	public Optional<IDocumentValidationResult> validateDocument(DocumentV2 document, Locale locale) {
		// TODO A12S-4246: PartialValidation should not have precedence over SkipValidation
		if (partialValidationForModels != null && partialValidationForModels.contains(document.getDocumentModelId())) {
			return Optional.of(validatePartially(document, locale));
		} else if (skipValidationForModels != null && skipValidationForModels.contains(document.getDocumentModelId())) {
			return Optional.empty();
		} else if (validationEnabledByDefault) {
			return Optional.of(validateFull(document, locale));
		} else {
			return Optional.empty();
		}
	}

	/**
	 * Kernel computation is executed for the document and its linked document model and locale. The computation result is then used to enrich the document that
	 * is passed as parameter.
	 *
	 * Custom condition factories and custom field type factories configured in this service are applied
	 * via {@link DocumentProcessingConfig} during computation.
	 *
	 * @param document will be enriched by computed values
	 * @param locale the locale for computation, can be `null` (will be resolved from document model)
	 * @return the document enriched with computed field values
	 * @throws DocumentComputationException if computation errors occur
	 */
	public DocumentV2 compute(DocumentV2 document, Locale locale) {
		List<String> errors = new ArrayList<>();

		IDocumentComputationResult computationResult = rtService.compute(document, createDocumentProcessingConfig(document, locale, true));

		if (enabledCleanupErrorAndNotComputedValue) {
			document = computationResult.applyTo(document);
		} else {
			for (IComputedFieldInstance field : computationResult.getComputedFieldInstancesWithChanges()) {
				document = document.withFieldValue(field.pointer(), field.getValueV2());
			}
		}
		computationResult.getComputedFieldInstancesWithErrors()
			.forEach(field -> errors.add("Computation for field %s failed: %s".formatted(field.pointer().fullName(), field.getErrorMessage())));

		if (!errors.isEmpty()) {
			log.warn("Errors occurred while computation for document [{}]", document.getId());
			throw new DocumentComputationException(errors).withAnonymityMessage("Computation failed.");
		}
		return document;
	}

	/**
	 * Validation of full document with a specific locale.
	 *
	 * Custom condition factories and custom field type factories configured in this service are applied
	 * via {@link DocumentProcessingConfig} during validation.
	 *
	 * @param document document to be validated
	 * @param locale for which validation should be performed, can be `null` (will be resolved from document model)
	 * @return validation result
	 */
	public IDocumentValidationResult validateFull(DocumentV2 document, Locale locale) {
		StopWatch stopWatch = StopWatch.createStarted();
		Assert.notNull(document, "Document must not be NULL.");
		IDocumentValidationResult documentValidationResult = rtService.validateFull(document, createDocumentProcessingConfig(document, locale, false));
		log.debug("Full document validation for document [{}] and locale [{}] finished in [{} ms]", document.getDocumentModelId(), locale, stopWatch.getTime());
		return documentValidationResult;
	}

	/**
	 * Validation of provided instances of document with a specific locale.
	 *
	 * Custom condition factories and custom field type factories configured in this service are applied
	 * via {@link DocumentProcessingConfig} during validation.
	 *
	 * @param document document to be validated
	 * @param locale for which validation should be performed, can be `null` (will be resolved from document model)
	 * @return validation result
	 */
	public IDocumentValidationResult validatePartially(DocumentV2 document, Locale locale) {
		StopWatch stopWatch = StopWatch.createStarted();
		Assert.notNull(document, "Document must not be NULL.");
		Set<DocumentMultiPointer> documentPointers = new HashSet<>();
		document.traverse(new IDocumentV2Visitor() {
			@Override public void visitField(DocumentPointer pointerRelativeToBase, FieldInstanceV2 field) {
				documentPointers.add(DocumentMultiPointer.of(pointerRelativeToBase));
			}
		});

		IDocumentValidationResult documentValidationResult =
			rtService.validatePart(document, documentPointers, createDocumentProcessingConfig(document, locale, false));
		log.debug("Partial document validation for document [{}] and locale [{}] finished in [{}] ms", document.getDocumentModelId(), locale,
			stopWatch.getTime());
		return documentValidationResult;
	}

	Locale resolveLocale(DocumentV2 document, Locale preferredLocale, boolean skipNonExisting) {

		Locale firstLocaleFromMetadata = Optional.ofNullable(document)
			.map(DocumentV2::getDocumentModelId)
			.map(documentModelResolver::getDocumentModelById)
			.map(IDocumentModel::getHeader)
			.map(Header::getLocales)
			.map(Collection::stream)
			.flatMap(Stream::findFirst)
			.orElseThrow(() -> new InvalidInputException(ExceptionKeys.DOCUMENT_LOCALES_INVALID_ERROR_KEY,
				"Unable to lookup locale. Define language(s) in document model."));

		Locale localeToFind = (preferredLocale == null) ? firstLocaleFromMetadata : preferredLocale;

		return Optional.of(document)
			.map(DocumentV2::getDocumentModelId)
			.map(documentModelResolver::getDocumentModelById)
			.map(IDocumentModel::getHeader)
			.map(Header::getLocales).stream()
			.flatMap(Collection::stream)
			.filter(language -> Strings.CS.equals(language.getLanguage(), localeToFind.getLanguage()))
			.findFirst()
			.orElse(skipNonExisting ? firstLocaleFromMetadata : localeToFind);
	}

	private DocumentProcessingConfig createDocumentProcessingConfig(DocumentV2 document, Locale locale, boolean skipNonExisting) {
		Locale resolvedLocale = resolveLocale(document, locale, skipNonExisting);
		DocumentProcessingConfigBuilder builder = DocumentProcessingConfig.builder(resolvedLocale);
		if (CollectionUtils.isNotEmpty(customFieldTypeFactories)) {
			customFieldTypeFactories.forEach(builder::customFieldTypeFactory);
		}
		if (CollectionUtils.isNotEmpty(customConditionFactories)) {
			customConditionFactories.forEach(builder::customConditionFactory);
		}
		return builder.build();
	}
}
