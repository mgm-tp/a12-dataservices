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
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractSpringContextIT;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.initialization.BusinessModelInitializer;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelEntity;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelHeaderEntity;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.model.header.DefaultHeaderParser;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.header.HeaderParseException;
import com.mgmtp.a12.model.header.HeaderParser;
import com.mgmtp.a12.model.header.Label;

import lombok.Getter;
import lombok.SneakyThrows;

import static com.mgmtp.a12.dataservices.constants.DocumentModelConstants.BUSINESS_PARTNER_SUPER_MODEL;
import static com.mgmtp.a12.dataservices.constants.PathConstants.DOCUMENT_MODEL_PATH;

@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, MockitoTestExecutionListener.class, TransactionalTestExecutionListener.class })
@Transactional
public class BusinessModelInitializationIT extends AbstractSpringContextIT {

	public static final String BUSINESS_PARTNER_SUPER_INCLUDE_5 = "BusinessPartnerSuperInclude5";
	public static final String BUSINESS_PARTNER_SUPER_INCLUDE_4 = "BusinessPartnerSuperInclude4";
	public static final String BUSINESS_PARTNER_SUPER_INCLUDE_3 = "BusinessPartnerSuperInclude3";
	public static final String BUSINESS_PARTNER_SUPER_INCLUDE_2 = "BusinessPartnerSuperInclude2";
	public static final String BUSINESS_PARTNER_SUPER_INCLUDE_1 = "BusinessPartnerSuperInclude1";
	public static final String BUSINESS_PARTNER_ROOT_GROUP_NAME = "BusinessPartnerRoot";
	public static final String BUSINESS_PARTNER_ROOT_GROUP_NAME_1 = "BusinessPartnerRoot1";
	public static final String BUSINESS_PARTNER_ROOT_GROUP_NAME_2 = "BusinessPartnerRoot2";
	public static final String BUSINESS_PARTNER_ROOT_GROUP_NAME_3 = "BusinessPartnerRoot3";
	public static final String BUSINESS_PARTNER_ROOT_GROUP_NAME_4 = "BusinessPartnerRoot4";
	public static final String BUSINESS_PARTNER_ROOT_GROUP_NAME_5 = "BusinessPartnerRoot5";
	// Inside build/resources/test (Models are copied from test-resources to build directory via gradle task.)
	public static final String CLASSPATH_MODELS = "classpath:/bulkload/models";
	// Jar file is copied from test-resources to build directory via gradle task.
	public static final String CLASSPATH_MODELS_JAR = "classpath:/bulkload/models.jar";
	// Zip file is copied from test-resources to build directory via gradle task.
	public static final String CLASSPATH_MODELS_ZIP = "classpath:/bulkload/models.zip";
	// Inside the new test resources dependency
	public static final String CLASSPATH_MODELS_DEPENDENCY = "classpath:/models-bulkload/bulkload/models";
	private static final String CONFIGURATION_FIELD = "configuration";
	private static final HeaderParser PARSER = new DefaultHeaderParser();

	@Autowired private BusinessModelInitializer businessModelInitializer;
	@Autowired private ResourcePatternResolver resourcePatternResolver;

	@BeforeMethod(alwaysRun = true)
	public void setUp() {
		modelRepository.deleteAll();
		modelHeaderJpaRepository.deleteAll();
	}

	@DataProvider
	public Iterator<Object[]> modelExcerpts() {
		return
			Stream.of(CLASSPATH_MODELS, CLASSPATH_MODELS_DEPENDENCY, CLASSPATH_MODELS_JAR, CLASSPATH_MODELS_ZIP)
				.map(path -> new Object[] { path })
				.iterator();
	}

	private BulkImporterConfiguration createConfig() {
		BulkImporterConfiguration config = new BulkImporterConfiguration();
		config.setOverwriteDocumentModels(dataServicesCoreProperties.getInitialization().getImport().getModels().getOverwrite().isEnabled());
		config.setOverwriteModels(dataServicesCoreProperties.getInitialization().getImport().getModels().getOverwrite().getModels());
		config.setOverwriteModelsDefault(dataServicesCoreProperties.getInitialization().getImport().getModels().getOverwrite().isEnabled());
		config.setPaths(new String[] {});
		config.setModelTypes(Collections.emptyList());
		return config;
	}

	@Test(dataProvider = "modelExcerpts")
	public void testModelsLoaded(String[] path) throws IOException {
		BulkImporterConfiguration configuration = createConfig();
		configuration.setPaths(path);

		runWithFieldOverwritten(CONFIGURATION_FIELD, configuration, businessModelInitializer, () -> {
			businessModelInitializer.importBusinessModels();
			Stream.of(
				new DocumentModelExpectations(BUSINESS_PARTNER_SUPER_MODEL)
					.expectedIncludes(BUSINESS_PARTNER_ROOT_GROUP_NAME, BUSINESS_PARTNER_ROOT_GROUP_NAME_1, BUSINESS_PARTNER_ROOT_GROUP_NAME_2, BUSINESS_PARTNER_ROOT_GROUP_NAME_3, BUSINESS_PARTNER_ROOT_GROUP_NAME_4, BUSINESS_PARTNER_ROOT_GROUP_NAME_5),
				new DocumentModelExpectations(BUSINESS_PARTNER_SUPER_INCLUDE_1)
					.expectedIncludes(BUSINESS_PARTNER_ROOT_GROUP_NAME_1, BUSINESS_PARTNER_ROOT_GROUP_NAME_3, BUSINESS_PARTNER_ROOT_GROUP_NAME_5)
					.restrictedIncludes(BUSINESS_PARTNER_ROOT_GROUP_NAME, BUSINESS_PARTNER_ROOT_GROUP_NAME_2, BUSINESS_PARTNER_ROOT_GROUP_NAME_4),
				new DocumentModelExpectations(BUSINESS_PARTNER_SUPER_INCLUDE_2)
					.expectedIncludes(BUSINESS_PARTNER_ROOT_GROUP_NAME_2, BUSINESS_PARTNER_ROOT_GROUP_NAME_4, BUSINESS_PARTNER_ROOT_GROUP_NAME_5)
					.restrictedIncludes(BUSINESS_PARTNER_ROOT_GROUP_NAME, BUSINESS_PARTNER_ROOT_GROUP_NAME_1, BUSINESS_PARTNER_ROOT_GROUP_NAME_3),
				new DocumentModelExpectations(BUSINESS_PARTNER_SUPER_INCLUDE_3)
					.expectedIncludes(BUSINESS_PARTNER_ROOT_GROUP_NAME_3, BUSINESS_PARTNER_ROOT_GROUP_NAME_5)
					.restrictedIncludes(BUSINESS_PARTNER_ROOT_GROUP_NAME, BUSINESS_PARTNER_ROOT_GROUP_NAME_1, BUSINESS_PARTNER_ROOT_GROUP_NAME_2, BUSINESS_PARTNER_ROOT_GROUP_NAME_4),
				new DocumentModelExpectations(BUSINESS_PARTNER_SUPER_INCLUDE_4)
					.expectedIncludes(BUSINESS_PARTNER_ROOT_GROUP_NAME_4, BUSINESS_PARTNER_ROOT_GROUP_NAME_5)
					.restrictedIncludes(BUSINESS_PARTNER_ROOT_GROUP_NAME, BUSINESS_PARTNER_ROOT_GROUP_NAME_1, BUSINESS_PARTNER_ROOT_GROUP_NAME_2, BUSINESS_PARTNER_ROOT_GROUP_NAME_3),
				new DocumentModelExpectations(BUSINESS_PARTNER_SUPER_INCLUDE_5)
					.expectedIncludes(BUSINESS_PARTNER_ROOT_GROUP_NAME_5)
					.restrictedIncludes(BUSINESS_PARTNER_ROOT_GROUP_NAME, BUSINESS_PARTNER_ROOT_GROUP_NAME_1, BUSINESS_PARTNER_ROOT_GROUP_NAME_2, BUSINESS_PARTNER_ROOT_GROUP_NAME_3, BUSINESS_PARTNER_ROOT_GROUP_NAME_4)
			).forEach(expectation -> {
				ModelHeaderEntity modelHeaderEntity = findHeaderByModelName(expectation.getModelName());
				Assert.assertEquals(modelHeaderEntity.getId(), expectation.getModelName());
				Assert.assertEquals(modelHeaderEntity.getAnnotationsAsMap().get("roles"), "admin,systemAdmin");
				try {
					IDocumentModel model =
						documentModelSerializer.deserialize(new StringReader(findModelByName(expectation.getModelName()).getContent()));
					Assert.assertTrue(Stream.of(model.getContent().getDocumentModelRoot())
						.flatMap(this::recurse)
						.anyMatch(expectation::isExpected));
					Assert.assertFalse(Stream.of(model.getContent().getDocumentModelRoot())
						.flatMap(this::recurse)
						.anyMatch(expectation::isRestricted));
				} catch (IOException e) {
					throw new UnexpectedException(e);
				}
			});

			Stream.of(
					ModelExpectations.ofId("DemoModelWithAllFields")
						.hasType("DocumentModel")
						.hasVersion("2")
						.hasLocales(Locale.ENGLISH, Locale.GERMAN)
						.hasLabel(Locale.ENGLISH, "English label")
						.hasLabel(Locale.GERMAN, "German label")
						.hasContent(resourcePatternResolver.getResource(CLASSPATH_MODELS + "/jsonModelFull.json").getURL()),
					ModelExpectations.ofId("DemoModelBare")
						.hasType("DocumentModel")
						.hasLocales(Locale.ENGLISH, Locale.GERMAN)
						.hasLabel(Locale.ENGLISH, "English label")
						.hasLabel(Locale.GERMAN, "German label")
						.hasContent(resourcePatternResolver.getResource(CLASSPATH_MODELS + "/jsonModelBase.json").getURL()))
				.forEach(modelExpectation -> {
					ModelEntity model = findModelByName(modelExpectation.getId());
					modelExpectation.assertId(model.getId());
					modelExpectation.assertContent(model);
				});
		});
	}

	@Test(dataProvider = "modelExcerpts")
	public void initializeFullModelImport(String[] path) {
		prepareModels();

		BulkImporterConfiguration configuration = createConfig();
		configuration.setPaths(path);
		configuration.setModelTypes(Collections.singletonList(DataServicesCoreProperties.MATCH_ALL));

		runWithFieldOverwritten(CONFIGURATION_FIELD, configuration, businessModelInitializer, () -> {
			businessModelInitializer.importBusinessModels();
			Assert.assertFalse(modelHeaderJpaRepository.existsById("DomainAbstract"));
			Assert.assertFalse(modelHeaderJpaRepository.existsById("Person"));
			Assert.assertFalse(modelHeaderJpaRepository.existsById("Model1"));
			Assert.assertFalse(modelHeaderJpaRepository.existsById("Model2"));
			Assert.assertEquals(modelHeaderJpaRepository.findAll().size(), 12);
		});
	}

	@Test(dataProvider = "modelExcerpts")
	public void initializeFullModelImportWithTypes(String[] path) {
		prepareModels();

		BulkImporterConfiguration configuration = createConfig();
		configuration.setPaths(path);
		configuration.setModelTypes(Collections.singletonList("model1"));

		runWithFieldOverwritten(CONFIGURATION_FIELD, configuration, businessModelInitializer, () -> {
			businessModelInitializer.importBusinessModels();
			Assert.assertTrue(modelHeaderJpaRepository.existsById("DomainAbstract"));
			Assert.assertTrue(modelHeaderJpaRepository.existsById("Person"));
			Assert.assertFalse(modelHeaderJpaRepository.existsById("Model1"));
			Assert.assertFalse(modelHeaderJpaRepository.existsById("Model2"));
			Assert.assertEquals(modelHeaderJpaRepository.findAll().size(), 14);
		});
	}

	@Test
	public void initializeFullModelNoPath() {
		prepareModels();

		BulkImporterConfiguration configuration = createConfig();
		configuration.setModelTypes(Collections.singletonList(DataServicesCoreProperties.MATCH_ALL));

		runWithFieldOverwritten(CONFIGURATION_FIELD, configuration, businessModelInitializer, () -> {
			businessModelInitializer.importBusinessModels();
			Assert.assertFalse(modelHeaderJpaRepository.existsById("DomainAbstract"));
			Assert.assertFalse(modelHeaderJpaRepository.existsById("Person"));
			Assert.assertFalse(modelHeaderJpaRepository.existsById("Model1"));
			Assert.assertFalse(modelHeaderJpaRepository.existsById("Model2"));
			Assert.assertEquals(modelHeaderJpaRepository.findAll().size(), 0);
		});
	}

	@SneakyThrows
	private void prepareModels()  {
		modelsFunctions.createModel(DOCUMENT_MODEL_PATH + "DomainAbstract.json");
		modelsFunctions.createModel(DOCUMENT_MODEL_PATH + "DomainSimplePerson.json");

		modelsFunctions.saveModel(resourceFunctions.loadResource(DOCUMENT_MODEL_PATH + "custom/model1type1.json"), ModelEntity.class);
		modelsFunctions.saveModel(resourceFunctions.loadResource(DOCUMENT_MODEL_PATH + "custom/model1type1.json").replace("Model1", "Model2"),
			ModelEntity.class);
	}

	private Stream<IElement> recurse(IElement element) {
		return element instanceof IGroup
			? Stream.concat(
			Stream.of(element),
			((IGroup) element).getElements().stream().flatMap(this::recurse))
			: Stream.of(element);
	}

	private static class DocumentModelExpectations {

		@Getter
		private final String modelName;
		private String[] expected;
		private String[] restricted;

		DocumentModelExpectations(String modelName) {
			this.modelName = modelName;
		}

		public DocumentModelExpectations expectedIncludes(String... expected) {
			this.expected = expected;
			return this;
		}

		public DocumentModelExpectations restrictedIncludes(String... notExpected) {
			this.restricted = notExpected;
			return this;
		}

		boolean isExpected(IElement element) {
			if (expected == null) {
				return false;
			}
			return Arrays.stream(expected).anyMatch(m -> m.equals(element.getName()));
		}

		boolean isRestricted(IElement element) {
			if (restricted == null) {
				return false;
			}
			return Arrays.stream(restricted).anyMatch(m -> m.equals(element.getName()));
		}
	}

	private static class ModelExpectations {
		@Getter
		private final String id;
		private String content;
		private String type;
		private Header header;
		private String version;
		private Set<Locale> locales;
		private Map<Locale, String> labels;

		private ModelExpectations(String id) {
			this.id = id;
		}

		public static ModelExpectations ofId(String id) {
			return new ModelExpectations(id);
		}

		public ModelExpectations hasContent(URL path) throws IOException {
			content = IOUtils.toString(path, StandardCharsets.UTF_8);
			return this;
		}

		public ModelExpectations hasType(String type) {
			this.type = type;
			return this;
		}

		public ModelExpectations hasVersion(String version) {
			this.version = version;
			return this;
		}

		public ModelExpectations hasLocales(Locale... locales) {
			this.locales = Set.copyOf(Arrays.asList(locales));
			return this;
		}

		public void assertId(String id) {
			Assert.assertEquals(id, this.id);
		}

		private void assertId() {
			Assert.assertEquals(this.header.getId(), id);
		}

		public void assertContent(ModelEntity model) {
			content = model.getContent();
			JSONAssert.assertEquals(this.content, content, JSONCompareMode.LENIENT);
			try {
				header = PARSER.parseJson(content);
			} catch (HeaderParseException e) {
				Assert.fail(e.getMessage());
			}
			assertId();
			assertVersion();
			assertType();
			assertLocales();
			assertLabels();
		}

		private void assertLabels() {
			Assert.assertEquals(header.getLabels().stream().collect(Collectors.toMap(Label::getLocale, Label::getText)), labels);
		}

		private void assertLocales() {
			Assert.assertEquals(Set.copyOf(header.getLocales()), locales);
		}

		private void assertType() {
			Assert.assertEquals(header.getModelType(), type);
		}

		private void assertVersion() {
			Assert.assertEquals(header.getModelVersion(), version, String.format("For model %s", id));
		}

		public ModelExpectations hasLabel(Locale locale, String text) {
			if (labels == null) {
				labels = new HashMap<>();
			}
			labels.put(locale, text);
			return this;
		}
	}
}
