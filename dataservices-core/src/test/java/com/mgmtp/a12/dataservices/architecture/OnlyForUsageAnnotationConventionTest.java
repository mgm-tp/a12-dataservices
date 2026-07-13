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
package com.mgmtp.a12.dataservices.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

/**
 * ArchUnit tests to enforce the `@OnlyForUsage` annotation convention.
 *
 * Rules tested:
 *
 * - Public interfaces without "I" prefix in public API packages MUST have `@OnlyForUsage`
 * - Extension interfaces with "I" prefix MUST NOT have `@OnlyForUsage`
 */
public class OnlyForUsageAnnotationConventionTest {

	/**
	 * Packages to scan for annotation convention enforcement.
	 */
	private static final String[] PACKAGES_TO_SCAN = {
		"com.mgmtp.a12.dataservices.client",
		"com.mgmtp.a12.dataservices.query",
		"com.mgmtp.a12.dataservices.document",
		"com.mgmtp.a12.dataservices.relationship",
		"com.mgmtp.a12.dataservices.reference",
		"com.mgmtp.a12.dataservices.model",
		"com.mgmtp.a12.dataservices.cdd",
		"com.mgmtp.a12.dataservices.attachment",
		"com.mgmtp.a12.dataservices.authorization",
		"com.mgmtp.a12.dataservices.common",
		"com.mgmtp.a12.dataservices.export",
		"com.mgmtp.a12.contentstore.client",
		"com.mgmtp.a12.contentstore.service",
		"com.mgmtp.a12.contentstore.exception",
		"com.mgmtp.a12.contentstore.utils"
	};

	private JavaClasses classes;
	private JavaParser javaParser;

	@BeforeClass
	public void setUp() {
		// Import all classes from the packages we want to analyze
		classes = new ClassFileImporter().importPackages(PACKAGES_TO_SCAN);
		ParserConfiguration config = new ParserConfiguration()
			.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
		javaParser = new JavaParser(config);
	}

	/**
	 * Test that public interfaces without "I" prefix have the `@OnlyForUsage` annotation.
	 * TODO A12S-7006: Modify and enable again.
	 */
	@Test(enabled = false, description = "Should have @OnlyForUsage annotation on usage-only interfaces")
	public void shouldHaveOnlyForUsageAnnotationOnUsageOnlyInterfaces() {
		classes()
			.that(ArchUnitPredicates.areInterfaces())
			.and(ArchUnitPredicates.arePublic())
			.and(ArchUnitPredicates.areNotInInternalPackages())
			.and(ArchUnitPredicates.haveSimpleNameNotStartingWith("I"))
			.and(ArchUnitPredicates.areNotConstants())
			.and(ArchUnitPredicates.areNotInnerClasses())
			.should(haveOnlyForUsageAnnotationInSource())
			.check(classes);
	}

	/**
	 * Test that extension interfaces with "I" prefix do NOT have the `@OnlyForUsage` annotation.
	 */
	@Test(description = "Should not have @OnlyForUsage annotation on extension interfaces")
	public void shouldNotHaveOnlyForUsageAnnotationOnExtensionInterfaces() {
		classes()
			.that(ArchUnitPredicates.areInterfaces())
			.and(ArchUnitPredicates.arePublic())
			.and(ArchUnitPredicates.areNotInInternalPackages())
			.and(ArchUnitPredicates.haveSimpleNameStartingWith("I"))
			.should(notHaveOnlyForUsageAnnotationInSource())
			.check(classes);
	}

	/**
	 * ArchUnit condition to check that a class has `@OnlyForUsage` annotation in its source code.
	 */
	private ArchCondition<JavaClass> haveOnlyForUsageAnnotationInSource() {
		return new ArchCondition<>("have @OnlyForUsage annotation in source code") {
			@Override
			public void check(JavaClass javaClass, ConditionEvents events) {
				boolean hasAnnotation = hasOnlyForUsageInSource(javaClass);
				if (!hasAnnotation) {
					String message = String.format("Interface %s should have @OnlyForUsage annotation",
						javaClass.getName());
					events.add(SimpleConditionEvent.violated(javaClass, message));
				}
			}
		};
	}

	/**
	 * ArchUnit condition to check that a class does NOT have `@OnlyForUsage` annotation in its source code.
	 */
	private ArchCondition<JavaClass> notHaveOnlyForUsageAnnotationInSource() {
		return new ArchCondition<>("not have @OnlyForUsage annotation in source code") {
			@Override
			public void check(JavaClass javaClass, ConditionEvents events) {
				boolean hasAnnotation = hasOnlyForUsageInSource(javaClass);
				if (hasAnnotation) {
					String message = String.format("Interface %s should not have @OnlyForUsage annotation",
						javaClass.getName());
					events.add(SimpleConditionEvent.violated(javaClass, message));
				}
			}
		};
	}

	/**
	 * Check if a JavaClass has the `@OnlyForUsage` annotation in its source code.
	 * Since the annotation has SOURCE retention, we need to parse the source file.
	 *
	 * @param javaClass the class to check
	 * @return `true` if the class has `@OnlyForUsage` annotation, `false` otherwise
	 */
	private boolean hasOnlyForUsageInSource(JavaClass javaClass) {
		Optional<Path> sourceFile = findSourceFile(javaClass);

		if (sourceFile.isEmpty()) {
			return false;
		}

		try {
			ParseResult<CompilationUnit> parseResult = javaParser.parse(sourceFile.get());
			if (!parseResult.isSuccessful()) {
				return false;
			}

			CompilationUnit cu = parseResult.getResult().orElse(null);
			if (cu == null) {
				return false;
			}

			// Find the type declaration for this class
			Optional<TypeDeclaration<?>> typeDecl = cu.getTypes().stream()
				.filter(type -> type.getNameAsString().equals(javaClass.getSimpleName()))
				.findFirst();

			if (typeDecl.isEmpty()) {
				return false;
			}

			TypeDeclaration<?> type = typeDecl.get();

			// Check if the type has @OnlyForUsage annotation
			return type.getAnnotations().stream()
				.anyMatch(this::isOnlyForUsageAnnotation);

		} catch (IOException e) {
			// If we can't read the source file, assume no annotation
			return false;
		}
	}

	/**
	 * Check if an annotation is the `@OnlyForUsage` annotation.
	 *
	 * @param annotation the annotation to check
	 * @return `true` if it's `@OnlyForUsage`, `false` otherwise
	 */
	private boolean isOnlyForUsageAnnotation(AnnotationExpr annotation) {
		String name = annotation.getNameAsString();
		// Check for both simple name and fully qualified name
		return "OnlyForUsage".equals(name) ||
			"com.mgmtp.a12.model.utils.OnlyForUsage".equals(name);
	}

	/**
	 * Finds the source file for a given JavaClass by searching in multiple source directories.
	 *
	 * @param javaClass the class to find source for
	 * @return the path to the source file if found
	 */
	private Optional<Path> findSourceFile(JavaClass javaClass) {
		// Convert package name to path
		String packagePath = javaClass.getPackageName().replace('.', '/');
		String fileName = javaClass.getSimpleName() + ".java";
		String relativePath = packagePath + "/" + fileName;

		// Get the project root directory (parent of current working directory when running from dataservices-core)
		Path currentDir = Paths.get("").toAbsolutePath();
		Path projectRoot = currentDir.getFileName().toString().equals("dataservices-core")
			? currentDir.getParent()
			: currentDir;

		// All source directories to search (relative to project root)
		String[] sourceDirs = {
			"dataservices-core/src/main/java",
			"dataservices-modelgraph-api/src/main/java",
			"dataservices-domain/src/main/java",
			"dataservices-client-api/src/main/java",
			"dataservices-server-api/src/main/java",
			"dataservices-security-user-api/src/main/java",
			"dataservices-mass-data-generator/src/main/java",
			"dataservices-sme-workspace-support/src/main/java",
			"dataservices-client/src/main/java",
			"dataservices-server/src/main/java",
			"dataservices-core-metadata/src/main/java",
			"dataservices-json-schema/src/main/java",
			"dataservices-common/dataservices-common-api/src/main/java",
			"dataservices-common/dataservices-common-lib/src/main/java",
			"dataservices-core-spring-boot-autoconfigure/src/main/java",
			"dataservices-server-spring-boot-autoconfigure/src/main/java",
			"dataservices-client-spring-boot-autoconfigure/src/main/java",
			"content-store/dataservices-content-store-domain/src/main/java",
			"content-store/dataservices-content-store-client/src/main/java",
			"content-store/dataservices-content-store-core/src/main/java",
			"content-store/dataservices-content-store-server/src/main/java",
			"content-store/dataservices-content-store-core-spring-boot-autoconfigure/src/main/java",
			"content-store/dataservices-content-store-server-spring-boot-autoconfigure/src/main/java"
		};

		// Search for the source file in all directories
		for (String sourceDir : sourceDirs) {
			Path sourceFile = projectRoot.resolve(sourceDir).resolve(relativePath);
			if (Files.exists(sourceFile)) {
				return Optional.of(sourceFile);
			}
		}

		return Optional.empty();
	}

	/**
	 * Helper class with ArchUnit predicates for filtering classes.
	 */
	private static class ArchUnitPredicates {

		/**
		 * Predicate to match interface types only.
		 */
		static com.tngtech.archunit.base.DescribedPredicate<JavaClass> areInterfaces() {
			return com.tngtech.archunit.base.DescribedPredicate.describe(
				"are interfaces",
				JavaClass::isInterface
			);
		}

		/**
		 * Predicate to match public classes only.
		 */
		static com.tngtech.archunit.base.DescribedPredicate<JavaClass> arePublic() {
			return com.tngtech.archunit.base.DescribedPredicate.describe(
				"are public",
				javaClass -> javaClass.getModifiers().contains(com.tngtech.archunit.core.domain.JavaModifier.PUBLIC)
			);
		}

		/**
		 * Predicate to exclude classes in internal packages (containing '.internal').
		 */
		static com.tngtech.archunit.base.DescribedPredicate<JavaClass> areNotInInternalPackages() {
			return com.tngtech.archunit.base.DescribedPredicate.describe(
				"are not in internal packages",
				javaClass -> !javaClass.getPackageName().contains(".internal")
			);
		}

		/**
		 * Predicate to match classes whose simple name does NOT start with the given prefix.
		 */
		static com.tngtech.archunit.base.DescribedPredicate<JavaClass> haveSimpleNameNotStartingWith(String prefix) {
			return com.tngtech.archunit.base.DescribedPredicate.describe(
				"have simple name not starting with '" + prefix + "'",
				javaClass -> !javaClass.getSimpleName().startsWith(prefix)
			);
		}

		/**
		 * Predicate to match classes whose simple name starts with the given prefix.
		 */
		static com.tngtech.archunit.base.DescribedPredicate<JavaClass> haveSimpleNameStartingWith(String prefix) {
			return com.tngtech.archunit.base.DescribedPredicate.describe(
				"have simple name starting with '" + prefix + "'",
				javaClass -> javaClass.getSimpleName().startsWith(prefix)
			);
		}

		/**
		 * Predicate to exclude Constants classes and annotation types (which are usage-only by nature).
		 */
		static com.tngtech.archunit.base.DescribedPredicate<JavaClass> areNotConstants() {
			return com.tngtech.archunit.base.DescribedPredicate.describe(
				"are not Constants classes or annotation types",
				javaClass -> !javaClass.getSimpleName().endsWith("Constants") && !javaClass.isAnnotation()
			);
		}

		/**
		 * Predicate to exclude inner classes (which typically don't need @OnlyForUsage).
		 */
		static com.tngtech.archunit.base.DescribedPredicate<JavaClass> areNotInnerClasses() {
			return com.tngtech.archunit.base.DescribedPredicate.describe(
				"are not inner classes",
				javaClass -> !javaClass.getName().contains("$")
			);
		}
	}
}
