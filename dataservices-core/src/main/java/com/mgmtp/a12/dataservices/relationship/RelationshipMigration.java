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
package com.mgmtp.a12.dataservices.relationship;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.relationship.migrator.model.IRelationshipModelMigrator;
import com.mgmtp.a12.dataservices.relationship.migrator.model.v1.RelationshipModelMigratorV1ToV3;
import com.mgmtp.a12.dataservices.relationship.migrator.model.v2.RelationshipModelMigratorV2ToV3;
import com.mgmtp.a12.dataservices.relationship.migrator.model.v3.RelationshipModelV3;
import com.mgmtp.a12.model.migration.MigrationResult;
import com.mgmtp.a12.model.migration.MigrationTool;

import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.relationship.migrator.model.v2.RelationshipModelV2.RelationshipModelHeaderV2.V2_MODEL_VERSION;
import static com.mgmtp.a12.dataservices.relationship.migrator.model.v3.RelationshipModelV3.RelationshipModelHeaderV3.V3_MODEL_VERSION;

/**
 * @deprecated use `dataservices-relationship-model-migration` npm package instead.
 *
 * Class provides solution to converting old Relationship model structure to new Models structure
 */
@SuppressWarnings({"removal", "deprecation"})
@Slf4j
@Deprecated(since = "38.1.0", forRemoval = true)
public class RelationshipMigration implements MigrationTool {

	private final ObjectMapper objectMapper;

	public static final Pattern ROLE_NAME_PATTERN = Pattern.compile("[_a-zA-Z][-_.a-zA-Z0-9]{0,84}");

	private static final String FIELD_RELATIONSHIP_MODEL = "relationshipModel";
	private static final String FIELD_HEADER = "header";
	private static final String FIELD_NAME = "name";
	private static final String FIELD_MODEL_VERSION = "modelVersion";
	private static final String FIELD_ID = "id";

	/**
	 * Creates a migration tool with a preconfigured {@link ObjectMapper}.
	 *
	 * @param objectMapper Jackson mapper used for reading and writing models; must not be null.
	 */
	public RelationshipMigration(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper.copy().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	/**
	 * Migrate a list of models passed in String representation.
	 *
	 * @param models list of model strings
	 * @return list of migration results per model
	 */
	@Override public List<MigrationResult> migrate(List<String> models) {
		List<MigrationResult> result = new ArrayList<>();
		models.forEach(model -> detectModelId(model).ifPresentOrElse(
			id -> {
				try (StringWriter writer = new StringWriter()) {
					Result res = convertModel(model, writer, "", new HashSet<>(), false);
					result.add(getMigrationResult(res, writer, model, id));
				} catch (Exception e) {
					log.warn("Could not migrate model", e);
					result.add(new MigrationResult(model, id, MigrationResult.Status.ERROR, Optional.of("Could not migrate model, reason: " + e.getMessage())));
				}
			},
			() -> {
				log.warn("Could not migrate model because model id could not be detected");
				result.add(new MigrationResult(model, null, MigrationResult.Status.ERROR,
					Optional.of("Could not migrate model because model id could not be detected")));
			}
		));
		return result;
	}

	/**
	 * Converts an old relationship model to the new models structure.
	 *
	 * @param model Reader providing the relationship model JSON to convert; must not be null.
	 * @param outputModel Writer receiving the converted model JSON; must not be null.
	 * @param roles Comma-separated roles annotation to include in the migrated header; may be null.
	 * @param locales Preferred locales to include when none are derivable from labels; must not be null.
	 * @param formattedOutput Whether to pretty-print the output JSON; false writes a single line.
	 * @return Migration {@link Result} indicating the outcome.
	 * @throws IOException If the input cannot be read or the output cannot be written.
	 */
	public Result convertModel(Reader model, Writer outputModel, String roles, Set<String> locales, boolean formattedOutput) throws IOException {
		return convertModel(IOUtils.toString(model), outputModel, roles, locales, formattedOutput);
	}

	/**
	 * Converts model content provided as a string to the target relationship model version.
	 *
	 * @param modelContent JSON string of the relationship model to migrate; must not be null.
	 * @param outputModel Writer receiving the migrated model JSON; must not be null.
	 * @param roles Comma-separated roles to include as header annotation; may be null.
	 * @param locales Locales to include when not derivable from labels; must not be null.
	 * @param formattedOutput Whether to pretty-print the output JSON; false writes a single line.
	 * @return Migration {@link Result} indicating whether migration happened or was skipped.
	 */
	public Result convertModel(String modelContent, Writer outputModel, String roles, Set<String> locales, boolean formattedOutput) {
		try {
			int version = detectVersion(modelContent);
			switch (version) {
			case 1:
				migrate(outputModel, formattedOutput, modelContent, new RelationshipModelMigratorV1ToV3(roles, locales));
				return Result.MIGRATED;
			case 2:
				migrate(outputModel, formattedOutput, modelContent, new RelationshipModelMigratorV2ToV3());
				return Result.MIGRATED;
			case 3:
				validate(objectMapper.readValue(modelContent, RelationshipModelV3.class));
				return Result.UP_TO_DATE;
			default:
				return Result.UNSUPPORTED;
			}
		} catch (IOException e) {
			return findFailureReason(modelContent);
		}
	}

	private <T> void migrate(Writer outputModel, boolean formattedOutput, String modelContent,
		IRelationshipModelMigrator<T, RelationshipModelV3> m) throws IOException {
		T sourceRelationshipModel = objectMapper.readValue(modelContent, m.getSourceType());
		log.info("Migrating Relationship model [{}] from version {} to version {}", m.getModelName(sourceRelationshipModel),
			m.getVersion(sourceRelationshipModel), V3_MODEL_VERSION);
		RelationshipModelV3 outputRelationshipModelV3 = m.migrateModel(sourceRelationshipModel);
		getOutputObjectMapper(formattedOutput).writeValue(outputModel, outputRelationshipModelV3);
		validate(outputRelationshipModelV3);
	}

	private MigrationResult getMigrationResult(Result res, Writer writer, String model, String id) {
		return res.equals(Result.MIGRATED) ?
			new MigrationResult(writer.toString(), id, getMappedResultStatus(res), getErrorMessage(res)) :
			new MigrationResult(model, id, getMappedResultStatus(res), getErrorMessage(res));
	}

	private int detectVersion(String modelContent) throws IOException {
		JsonNode node = objectMapper.readTree(modelContent);
		if (node.has(FIELD_RELATIONSHIP_MODEL)) {
			return 1;
		} else if (node.has(FIELD_HEADER)) {
			String version = node.get(FIELD_HEADER).get(FIELD_MODEL_VERSION).asText();
			if (V2_MODEL_VERSION.equals(version)) {
				return 2;
			} else if (V3_MODEL_VERSION.equals(version)) {
				return 3;
			}
		}
		return -1;
	}

	private Optional<String> detectModelId(String modelContent) {
		try {
			JsonNode node = objectMapper.readTree(modelContent);
			if (node.has(FIELD_RELATIONSHIP_MODEL) && node.get(FIELD_RELATIONSHIP_MODEL).has(FIELD_NAME)) {
				return Optional.of(node.get(FIELD_RELATIONSHIP_MODEL).get(FIELD_NAME).asText());
			}
			if (node.has(FIELD_HEADER) && node.get(FIELD_HEADER).has(FIELD_ID)) {
				return Optional.of(node.get(FIELD_HEADER).get(FIELD_ID).asText());
			}
		} catch (IOException e) {
			log.error("Could not parse model content as JSON node", e);
		}
		return Optional.empty();
	}

	private void validate(RelationshipModelV3 outputRelationshipModelV3) {
		String relationshipModelName = outputRelationshipModelV3.getHeader().getId();
		String linkDocumentModel = outputRelationshipModelV3.getContent().getLinkDocumentModel();
		if (StringUtils.isNotBlank(linkDocumentModel)) {
			validateName(linkDocumentModel, relationshipModelName, "link document model name", DocumentReference.MODEL_NAME_PATTERN);
		}
		outputRelationshipModelV3.getContent().getEntityCharacteristics().forEach(c -> {
			validateName(c.getDocumentModel(), relationshipModelName, "document model name", DocumentReference.MODEL_NAME_PATTERN);
			validateName(c.getRole(), relationshipModelName, "role name", ROLE_NAME_PATTERN);
		});
	}

	private void validateName(String name, String modelName, String validationType, Pattern pattern) {
		if (StringUtils.isBlank(name)) {
			log.warn(String.format("The %s in RelationshipModel %s is missing", validationType, modelName));
		}
		if (!pattern.matcher(name).matches()) {
			log.warn(String.format("RelationshipModel %s has wrong %s: %s", modelName, validationType, name));
		}
	}

	private ObjectMapper getOutputObjectMapper(boolean formattedOutput) {
		return formattedOutput ? objectMapper.copy().enable(SerializationFeature.INDENT_OUTPUT) : objectMapper;
	}

	/**
	 * Logs the version of the unsupported model, if possible
	 *
	 * @param model the json representation of the model
	 * @return reason of failure
	 */
	private Result findFailureReason(String model) {
		try {
			RelationshipModelV3 newVersionModel = objectMapper.readValue(model, RelationshipModelV3.class);
			Result reason = V2_MODEL_VERSION.equals(newVersionModel.getHeader().getModelVersion()) ? Result.UP_TO_DATE : Result.UNSUPPORTED;
			log.info(Result.UP_TO_DATE.equals(reason)
					? "Relationship model [{}] version is {}, which is already the current version"
					: "Relationship model [{}] version {} is not supported to migration",
				newVersionModel.getHeader().getId(), newVersionModel.getHeader().getModelVersion());
			return reason;
		} catch (Exception e) {
			return Result.UNSUPPORTED;
			//Ignored: not able to identify the version, no log needed
		}
	}

	private MigrationResult.Status getMappedResultStatus(Result result) {
		return switch (result) {
			case ERROR -> MigrationResult.Status.ERROR;
			case MIGRATED -> MigrationResult.Status.SUCCESS;
			case UP_TO_DATE, UNSUPPORTED -> MigrationResult.Status.SKIPPED;
		};
	}

	private Optional<String> getErrorMessage(Result result) {
		return switch (result) {
			case ERROR -> Optional.of("Model could not be migrated");
			case MIGRATED -> Optional.empty();
			case UP_TO_DATE -> Optional.of("Model not migrated, already up-to-date");
			case UNSUPPORTED -> Optional.of("Model not migrated, not supported");
		};
	}

	/**
	 * Outcome of a migration attempt.
	 *
	 * MIGRATED – model was converted to the new version.
	 * UP_TO_DATE – model already matches the target version.
	 * UNSUPPORTED – model structure or version cannot be migrated.
	 * ERROR – unexpected failure during migration.
	 */
	public enum Result {MIGRATED, UP_TO_DATE, UNSUPPORTED, ERROR}

}
