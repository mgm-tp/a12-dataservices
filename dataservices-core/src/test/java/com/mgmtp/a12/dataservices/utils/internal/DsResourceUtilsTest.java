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
package com.mgmtp.a12.dataservices.utils.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ResourceUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import lombok.SneakyThrows;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

public class DsResourceUtilsTest {

	/**
	 * jarmodels is a folder inside this jar (resources/utils/resourceSorting/models.jar)
	 * that is being loaded as a test dependency from gradle
	 */
	private static final String JSON_FILES_LOADED_ON_CLASSPATH_FROM_JAR_PATTERN = "classpath:/jarmodels/**/*.json";
	private static final String PATH_PREFIX = "classpath:utils/resourceSorting/";
	private static final ResourcePatternResolver RESOURCE_PATTERN_RESOLVER = new PathMatchingResourcePatternResolver();
	private static final String INSIDE_JAR_PATTERN = "jar:%s!/";
	private static final String JSON_FILES_INSIDE_JAR_PATTERN = INSIDE_JAR_PATTERN + "**/*.json";

	private final ResourcePatternResolver resourcePatternResolver = mock(ResourcePatternResolver.class);
	private final DsResourceUtils dsResourceUtils = new DsResourceUtils(resourcePatternResolver);

	@DataProvider public static Object[][] resourceProvider() {
		return new Object[][] {
			new Object[] { "model" },
			new Object[] { "model.zip" }
		};
	}

	@SneakyThrows
	@Test(dataProvider = "resourceProvider") public void testLocalPath(String path) {
		File resourceRootDir = ResourceUtils.getFile(Objects.requireNonNull(getClass().getResource("/model/ContractCDM.json")).toURI())
			.getParentFile().getParentFile().getCanonicalFile();

		String absolutePath = "%s/%s".formatted(resourceRootDir.getAbsolutePath(), path);
		Path currentDir = new File(System.getProperty("user.dir")).toPath();
		String relativePath = "%s/%s".formatted(currentDir.relativize(resourceRootDir.toPath()), path);

		assertResource(absolutePath);
		assertResource("file:%s".formatted(absolutePath));
		assertResource(relativePath);
		assertResource("file:./%s".formatted(relativePath));
	}

	@DataProvider(name = "multiplePaths")
	private Object[][] multiplePaths() {
		return new Object[][] {
			{ Arrays.asList("folderB/file2.json", "folderB/file1.json"),
				"folderB/file1.json",
				"folderB/file2.json" },
			{ Arrays.asList("folderB/file1.json", "folderA/folderAB/folderABC/file5.json", "folderA/file2.txt"),
				"folderA/file2.txt",
				"folderA/folderAB/folderABC/file5.json",
				"folderB/file1.json" },
			{ Arrays.asList("folderB/file2.json", "folderB/file1.json"),
				"folderB/file1.json",
				"folderB/file2.json" },
		};
	}

	@DataProvider(name = "patternCases")
	private Object[][] patternCases() {
		return new Object[][] {
			{ "folderB/**/*.json",
				"folderB/file1.json",
				"folderB/file2.json" },
			{ "folderA/**/*.json",
				"folderA/file3.json",
				"folderA/folderAB/file4.json",
				"folderA/folderAB/folderABC/file5.json" },
			{ "**/*.json",
				"folderA/file3.json",
				"folderA/folderAB/file4.json",
				"folderA/folderAB/folderABC/file5.json",
				"folderB/file1.json",
				"folderB/file2.json" },
			{ "**/*.txt",
				"folderA/file1.txt",
				"folderA/file2.txt",
				"folderB/file1.txt",
				"folderB/file2.txt" },
			{ "**/*.*",
				"folderA/file1.txt",
				"folderA/file2.txt",
				"folderA/file3.json",
				"folderA/folderAB/file4.json",
				"folderA/folderAB/folderABC/file5.json",
				"folderB/file1.json",
				"folderB/file1.txt",
				"folderB/file2.json",
				"folderB/file2.txt",
				"folderB/folderC/file1.txt" }
		};
	}

	@Test
	public void testSameFolder() throws IOException {
		Resource[] resources = getSeparatedResources("folderA/file2.txt", "folderA/file3.json", "folderA/file1.txt");
		DsResourceUtils.sortByURI(resources);
		assertOrderIsCorrect(resources, PATH_PREFIX,
			"folderA/file1.txt",
			"folderA/file2.txt",
			"folderA/file3.json"
		);
	}

	@Test
	public void testDifferentFolderSameLevel() throws IOException {
		Resource[] resources = getSeparatedResources("folderB/file2.txt", "folderA/file3.json");
		DsResourceUtils.sortByURI(resources);
		assertOrderIsCorrect(resources, PATH_PREFIX,
			"folderA/file3.json",
			"folderB/file2.txt"
		);
	}

	@Test
	public void testDeeperComesFirst() throws IOException {
		Resource[] resources = getSeparatedResources("folderB/file2.txt", "folderA/folderAB/file4.json");
		DsResourceUtils.sortByURI(resources);
		assertOrderIsCorrect(resources, PATH_PREFIX,
			"folderA/folderAB/file4.json",
			"folderB/file2.txt"
		);
	}

	@Test
	public void testDeeperComesLater() throws IOException {
		Resource[] resources = getSeparatedResources("folderB/folderC/file1.txt", "folderA/file3.json");
		DsResourceUtils.sortByURI(resources);
		assertOrderIsCorrect(resources, PATH_PREFIX,
			"folderA/file3.json",
			"folderB/folderC/file1.txt"
		);
	}

	@Test
	public void testFilesInsideJar() throws IOException {
		Resource jarFile = getResource("models.jar");
		Resource[] resources = RESOURCE_PATTERN_RESOLVER.getResources(JSON_FILES_INSIDE_JAR_PATTERN.formatted(jarFile.getURI()));
		DsResourceUtils.sortByURI(resources);
		assertOrderIsCorrect(resources, INSIDE_JAR_PATTERN.formatted(jarFile.getURI()),
			"jarmodels/brand/DomainBrand.json",
			"jarmodels/brand-rels/DomainBrandRels.json",
			"jarmodels/campaign/DomainCampaign.json",
			"jarmodels/includes/DomainProductInclude1.json",
			"jarmodels/includes/DomainProductInclude4.json",
			"jarmodels/includes/level2/DomainProductInclude2.json",
			"jarmodels/includes/level2/DomainProductInclude3.json",
			"jarmodels/includes/level2/DomainProductInclude5.json",
			"jarmodels/jsonModelBase.json",
			"jarmodels/jsonModelFull.json",
			"jarmodels/product/DomainProduct.json",
			"jarmodels/product-rels/DomainProductRels.json"
		);
	}

	@Test
	public void testFilesLoadedOnTheClasspathFromAJar() throws IOException {
		Resource[] resources = RESOURCE_PATTERN_RESOLVER.getResources(JSON_FILES_LOADED_ON_CLASSPATH_FROM_JAR_PATTERN);
		DsResourceUtils.sortByURI(resources);
		assertOrderIsCorrect(resources, "classpath:",
			"jarmodels/brand/DomainBrand.json",
			"jarmodels/brand-rels/DomainBrandRels.json",
			"jarmodels/campaign/DomainCampaign.json",
			"jarmodels/includes/DomainProductInclude1.json",
			"jarmodels/includes/DomainProductInclude4.json",
			"jarmodels/includes/level2/DomainProductInclude2.json",
			"jarmodels/includes/level2/DomainProductInclude3.json",
			"jarmodels/includes/level2/DomainProductInclude5.json",
			"jarmodels/jsonModelBase.json",
			"jarmodels/jsonModelFull.json",
			"jarmodels/product/DomainProduct.json",
			"jarmodels/product-rels/DomainProductRels.json"
		);
	}

	@DataProvider(name = "uriParts")
	private Object[][] uriParts() {
		return new Object[][] {
			{ "http:test1", "ftp:test1" }, // scheme
			{ "http://www.def.com", "http://www.abc.com" }, // host
			{ "http://www.abc.com:789", "http://www.abc.com:123" }, // port
			{ "http://www.abc.com:123/def", "http://www.abc.com:123/abc" }, // path
			{ "http://www.abc.com:123/abc?ghi=asd", "http://www.abc.com:123/abc?def=asd" }, // query
			{ "http://www.abc.com:123/abc?abc=asd#mmm", "http://www.abc.com:123/abc?abc=asd#aaa" } // fragment
		};
	}

	@Test(dataProvider = "uriParts")
	public void testUriParts(String second, String first) throws IOException {
		Resource[] resources = new Resource[] {
			RESOURCE_PATTERN_RESOLVER.getResource(second),
			RESOURCE_PATTERN_RESOLVER.getResource(first)
		};
		DsResourceUtils.sortByURI(resources);
		assertOrderIsCorrect(resources, "",
			first, second);
	}

	@Test(dataProvider = "patternCases")
	public void testPatternCases(String path, String... expectedOrder) throws IOException {
		Resource[] resources = RESOURCE_PATTERN_RESOLVER.getResources(PATH_PREFIX + path);
		DsResourceUtils.sortByURI(resources);
		assertOrderIsCorrect(resources, PATH_PREFIX, expectedOrder);
	}

	@Test(dataProvider = "multiplePaths")
	public void testMultiplePaths(List<String> paths, String... expectedOrder) throws IOException {
		Resource[] resources = paths.stream()
			.map(this::getResource)
			.toArray(Resource[]::new);
		DsResourceUtils.sortByURI(resources);
		assertOrderIsCorrect(resources, PATH_PREFIX, expectedOrder);
	}

	private void assertOrderIsCorrect(Resource[] resources, String prefix, String... expectedOrder) throws IOException {
		for (int i = 0; i < expectedOrder.length; i++) {
			Assert.assertEquals(
				resources[i].getURI(),
				getResource(expectedOrder[i], prefix).getURI()
			);
		}
	}

	private Resource[] getSeparatedResources(String... paths) {
		return Arrays.stream(paths).map(this::getResource).toArray(Resource[]::new);
	}

	private Resource getResource(String path, String prefix) {
		return RESOURCE_PATTERN_RESOLVER.getResource(prefix + path);
	}

	private Resource getResource(String path) {
		return getResource(path, PATH_PREFIX);
	}

	private void assertResource(String path) throws IOException {
		List<Resource> resources = dsResourceUtils.getJsonResources(path).toList();
		assertEquals(resources.size(), 17,
			"Path: %s\nResults:\n%s".formatted(path, resources.stream().map(Resource::getFilename).collect(Collectors.joining("\n"))));
	}
}
