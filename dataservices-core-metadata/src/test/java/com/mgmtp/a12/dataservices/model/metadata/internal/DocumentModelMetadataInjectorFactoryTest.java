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
package com.mgmtp.a12.dataservices.model.metadata.internal;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractKernelAwareTest;
import com.mgmtp.a12.dataservices.TestResourcesDocumentModelResolver;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.utils.internal.AbstractListProblemReporter;
import com.mgmtp.a12.dataservices.utils.internal.DataServicesDocumentProblemReporterException;
import com.mgmtp.a12.dataservices.experimental.ListIProblemReporter;
import com.mgmtp.a12.dataservices.utils.internal.ProblemFormatter;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;
import com.mgmtp.a12.model.notification.RankedNotification;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Slf4j
public class DocumentModelMetadataInjectorFactoryTest extends AbstractKernelAwareTest {

	@DataProvider public static Object[][] metadataProvider() {
		return new Object[][] {
			new Object[] { DocumentModelConstants.BUSINESS_PARTNER_DOCUMENT_MODEL,
				List.of("/BusinessPartnerRoot/Attachment"),
				List.of(
					"/__meta") },
			new Object[] { DocumentModelConstants.CONTRACT_CDM_MODEL,
				List.of(
					"/ContractBusinessPartner/BusinessPartnerRoot/Attachment",
					"/ContractBusinessPartner/PartnerAddresses",
					"/ContractBusinessPartner/PartnerPostalAddress",
					"/ContractBusinessPartner",
					"/ContractCoInsuredPartner/BusinessPartnerRoot/Attachment",
					"/ContractCoInsuredPartner/relationship",
					"/ContractCoInsuredPartner"
				),
				List.of(
					"/ContractBusinessPartner/PartnerAddresses/__meta",
					"/ContractBusinessPartner/PartnerPostalAddress/__meta",
					"/ContractBusinessPartner/__meta",
					"/ContractCoInsuredPartner/relationship/__meta",
					"/ContractCoInsuredPartner/__meta",
					"/__meta") }
		};
	}

	@SneakyThrows
	@Test(dataProvider = "metadataProvider") public void testMetadataEnrichment(String documentModel, List<String> documentPaths, List<String> metadataPaths) {
		IDocumentModel enriched = kernelTestSupport.getDocumentModelMetadataInjector(documentModelResolver.getDocumentModelById(documentModel))
			.getDocumentModelWithMetadata(
				documentModelResolver.getDocumentModelById(TestResourcesDocumentModelResolver.DOCUMENT_METADATA_MODEL_NAME),
				documentModelResolver.getDocumentModelById(TestResourcesDocumentModelResolver.ATTACHMENT_METADATA_MODEL_NAME)
			);

		log.info("enriched DM paths:\n{}", documentModelPaths(enriched.getContent().getDocumentModelRoot()).collect(Collectors.joining(",\n")));
		log.info("enriched MD paths:\n{}", metadataPaths(enriched.getContent().getDocumentModelRoot()).collect(Collectors.joining(",\n")));
		logModelContent(enriched);

		assertPaths(enriched, documentPaths);
		assertPaths(enriched, metadataPaths);

		IDocumentModel cleanuedUp = kernelTestSupport.getDocumentModelMetadataInjector(enriched).getDocumentModelWithoutMetadata();

		log.info("cleaned up DM paths:\n{}", documentModelPaths(cleanuedUp.getContent().getDocumentModelRoot()).collect(Collectors.joining(",\n")));
		log.info("cleaned up MD paths:\n{}", metadataPaths(cleanuedUp.getContent().getDocumentModelRoot()).collect(Collectors.joining(",\n")));

		assertPaths(cleanuedUp, documentPaths);
		assertNotPaths(cleanuedUp, metadataPaths);

		logModelContent(cleanuedUp);
	}

	private void assertPaths(IDocumentModel enriched, Collection<String> metadataPaths) {
		IDocumentModelSearchService modelSearchService = documentModelServiceFactory.createDocumentModelSearchService(enriched);
		AbstractListProblemReporter<Error> lpr = new ErrorReporter();
		metadataPaths.forEach(p -> {
			Optional<IElement> e = modelSearchService.getByPath(p);
			try {
				assertTrue(e.isPresent(), "%s should be present in %s".formatted(p, enriched.getHeader().getId()));
				e.ifPresent(g -> {
					assertTrue(g instanceof IGroup);
					assertEquals(iDocumentModelService.getPath(g), p);
				});
			} catch (Error ex) {
				lpr.reportProblem(ex);
			}
		});
		analyzeErrors(lpr);
	}

	private void assertNotPaths(IDocumentModel enriched, Collection<String> metadataPaths) {
		IDocumentModelSearchService modelSearchService = documentModelServiceFactory.createDocumentModelSearchService(enriched);
		AbstractListProblemReporter<Error> lpr = new ErrorReporter();
		metadataPaths.forEach(p -> {
			Optional<IElement> e = modelSearchService.getByPath(p);
			try {
				assertFalse(e.isPresent(), "%s shouldn't be present in %s".formatted(p, enriched.getHeader().getId()));
			} catch (Error ex) {
				lpr.reportProblem(ex);
			}
		});
		analyzeErrors(lpr);
	}

	private static void analyzeErrors(AbstractListProblemReporter<Error> lpr) {
		try {
			lpr.validate(0, "");
		} catch (DataServicesDocumentProblemReporterException e) {
			throw new AssertionError(e);
		}
	}

	private void logModelContent(IDocumentModel enriched) throws IOException {
		if (!log.isDebugEnabled()) {
			return;
		}
		try (StringWriter sw = new StringWriter()) {
			Consumer<RankedNotification> pr = new ListIProblemReporter();
			documentModelSerializer.serialize(enriched, sw, pr);
		}
	}

	private Stream<String> documentModelPaths(IGroup group) {

		return Stream.concat(
			Stream.of(iDocumentModelService.getPath(group)),
			group.getElements().stream()
				.filter(IGroup.class::isInstance)
				.map(IGroup.class::cast)
				.flatMap(this::documentModelPaths)
		);
	}

	private Stream<String> metadataPaths(IGroup group) {
		if (group.getName().startsWith("__attachment_meta_") || "__meta".equals(group.getName())) {
			return Stream.of(iDocumentModelService.getPath(group));
		} else {
			return group.getElements().stream()
				.filter(IGroup.class::isInstance)
				.map(IGroup.class::cast)
				.flatMap(this::metadataPaths);
		}
	}

	private static class ErrorReporter extends AbstractListProblemReporter<Error> {
		@Override public ProblemFormatter<Error> getFormatter() {
			return e -> "%s: %s%n".formatted(e.getMessage(), e);
		}
	}
}
