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
package com.mgmtp.a12.dataservices.exception;

import com.mgmtp.a12.model.utils.OnlyForUsage;

/**
 * Exception Keys interface. Localization keys for exceptions used throughout the application.
 *
 */
@OnlyForUsage public interface ExceptionKeys {

	/**
	 * Distinct phases of query processing used to qualify errors.
	 */
	enum ExecutionPhase {
		/** Phase that verifies the caller may execute the query. */
		QUERY_PERMISSION_CHECK,
		/** Phase that performs initial transformations of the query before validation. */
		QUERY_PREPROCESSING,
		/** Phase that validates the query structure against the model. */
		QUERY_VALIDATION,
		/** Phase that enriches the query with additional context (e.g., locale, defaults). */
		QUERY_ENRICHMENT,
		/** Phase that translates the validated query into SQL. */
		QUERY_SQL_GENERATION,
		/** Phase that executes the generated SQL against the database. */
		QUERY_EXECUTION,
		/** Phase that post-processes the raw result rows. */
		QUERY_POSTPROCESSING,
		/** Phase that maps the processed result into the public response shape. */
		QUERY_RESPONSE_MAPPING,
		/** Unspecified phase used when no more specific phase applies. */
		QUERY_GENERAL,
		/** Phase that interacts with the query index (build or lookup). */
		QUERY_INDEXING
	}

	//Document exceptions

	/** Localization key used when a referenced document cannot be found. */
	String DOCUMENT_NOT_FOUND_ERROR_KEY = "error.document.notFound";
	/** Localization key used when a document cannot be converted between representations. */
	String DOCUMENT_CONVERSION_ERROR_KEY = "error.document.convert";

	/** Localization key used when a document fails validation against its model. */
	String DOCUMENT_VALIDATION_ERROR_KEY = "document.validation.error";
	/** Localization key used when a document operation violates a data-integrity rule. */
	String DOCUMENT_INTEGRITY_ERROR_KEY = "error.document.integrity";
	/** Localization key used when an idempotent RPC request conflicts with a previous one. */
	String REQUEST_IDEMPOTENCY_ERROR_KEY = "error.request.idempotency";
	/** Localization key used when a document operation violates a document uniqueness criterion. */
	String UNIQUE_CONSTRAINT_VIOLATION_ERROR_KEY = "error.document.unique.constraint.violation";
	/** Localization key used when a model upload contains an invalid uniqueness criterion definition. */
	String MODEL_UNIQUE_CONSTRAINT_VALIDATION_ERROR_KEY = "error.model.unique.constraint.validation";


	/** Localization key used when documents in a result are not all of the same model. */
	String DOCUMENT_MODEL_HETEROGENEITY_ERROR_KEY = "error.document.model.heterogeneity";
	/** Localization key used when serializing a document model fails. */
	String DOCUMENT_MODEL_SERIALIZATION_ERROR_KEY = "error.document.model.serialization";
	/** Localization key used when deserializing a document model fails. */
	String DOCUMENT_MODEL_DESERIALIZATION_ERROR_KEY = "error.document.model.deserialization";
	/** Localization key used when joining or composing document models fails. */
	String DOCUMENT_MODEL_JOINING_ERROR_KEY = "error.document.model.joining";


	/** Localization key used when supplied document locales are invalid for the model. */
	String DOCUMENT_LOCALES_INVALID_ERROR_KEY = "error.document.locales.invalid";
	/** Localization key used when an add-document operation targets an abstract model. */
	String DOCUMENT_ABSTRACT_MODEL_ERROR_KEY = "error.add_document.abstractness.violated";
	/** Localization key used when generating validation codes for a model fails. */
	String VALIDATION_CODES_GENERATION_ERROR_KEY = "error.model.generate.validation.codes";

	/** Localization key used when a single document field fails validation. */
	String DOCUMENT_FIELD_ERROR_KEY = "error.document.field";

	/** Localization key used when an input document reference is malformed. */
	String DOCUMENT_REFERENCE_INVALID_INPUT = "error.document.documentReference.invalidInput";

	//RPC exceptions
	/** Localization key used when the add-document RPC operation fails. */
	String ADD_DOCUMENT_ERROR_KEY = "error.add_document.error";
	/** Localization key used when the check-uniqueness RPC operation fails. */
	String CHECK_UNIQUENESS_ERROR_KEY = "error.check_uniqueness.error";
	/** Localization key used when the copy-document RPC operation fails. */
	String COPY_DOCUMENT_ERROR_KEY = "error.copy_document.error";
	/** Localization key used when a modify-document RPC operation targets a non-existent document. */
	String MODIFY_DOCUMENT_NOT_FOUND_ERROR_KEY = "error.modify_document.document.notFound";
	/** Localization key used when a modify-document RPC operation provides an invalid document part. */
	String MODIFY_DOCUMENT_INVALID_DOCUMENT_PART_ERROR_KEY = "error.modify_document.documentPart.invalidInput";

	/** Localization key used when an RPC operation is skipped because a previous operation in the batch failed. */
	String RPC_OPERATION_PREVIOUS_FAILED_KEY = "rpc.operation.previous.error";
	/** Localization key used as a generic fallback when an RPC operation fails. */
	String RPC_OPERATION_ERROR_KEY = "rpc.operation.error";
	/** Localization key used when the number of operations in a single RPC request exceeds the configured limit. */
	String RPC_NUMBER_OF_OPERATIONS_EXCEEDED_ERROR_KEY = "error.rpc.numberOfOperations.exceeded";
	/** Localization key used when an RPC operation is missing its required `id` value. */
	String RPC_ID_NULL_ERROR_KEY = "error.rpc.id.null";

	//Models exceptions
	/** Localization key used when a referenced document model cannot be found. */
	String MODEL_NOT_FOUND_ERROR_KEY = "error.model.notFound";
	/** Localization key used when access to a document model is denied. */
	String MODEL_ACCESS_DENIED_ERROR_KEY = "error.model.accessDenied";
	/** Localization key used when the model version supplied does not match the stored version. */
	String MODEL_VERSION_MISMATCH_ERROR_KEY = "error.model.version.mismatch";
	/** Localization key used when deserializing a model fails. */
	String MODEL_DESERIALIZATION_ERROR_KEY = "error.model.deserialization";
	/** Localization key used when a model fails its validation. */
	String MODEL_VALIDATION_ERROR_KEY = "error.model.validation";
	/** Localization key used when a model uniqueness rule is violated. */
	String MODEL_UNIQUENESS_ERROR_KEY = "model.uniqueness.violated";
	/** Localization key used when a model id is required but missing. */
	String MODEL_ID_NOT_FOUND_ERROR_KEY = "error.model.idNotFound";
	/** Localization key used when the supplied model id is not in a valid format. */
	String MODEL_ID_NOT_VALID_ERROR_KEY = "error.model.idNotValid";
	/** Localization key used when the requested model type is unknown. */
	String MODEL_MODEL_TYPE_NOT_FOUND_ERROR_KEY = "error.model.modelTypeNotFound";
	/** Localization key used when expected role definitions are missing on a model. */
	String MODEL_ROLES_NOT_FOUND_ERROR_KEY = "error.model.rolesNotFound";
	/** Localization key used when two model artefacts disagree (e.g., references to different versions). */
	String MODEL_MISMATCH_ERROR_KEY = "error.model.mismatch";
	/** Localization key used when an import contains duplicate model identifiers. */
	String MODEL_DUPLICITY_ERROR_KEY = "error.model.duplicity";
	/** Localization key used as a fallback for generic model-import failures. */
	String MODEL_IMPORT_GENERIC_ERROR_KEY = "error.model.import.generic";
	/** Localization key used when no model repository is configured or available. */
	String NO_MODEL_REPOSITORY_FOUND = "error.model.repository.notFound";
	/** Localization key used when a required header annotation is missing from a model. */
	String MODEL_HEADER_ANNOTATION_IS_MISSING_ERROR_KEY = "error.model.headerAnnotationIsMissing";


	//Relationship exceptions
	/** Localization key used when a relationship model cannot be found. */
	String RELATIONSHIP_MODEL_NOT_FOUND_ERROR_KEY = "error.model.relationship.notFound";
	/** Localization key used when a relationship model cannot be deleted because links still reference it. */
	String RELATIONSHIP_MODEL_DELETE_LINK_EXISTS_ERROR_KEY = "error.model.relationship.delete.linkExist";
	/** Localization key used when serializing a relationship model fails. */
	String RELATIONSHIP_MODEL_SERIALIZATION_ERROR_KEY = "error.model.relationship.serialization";
	/** Localization key used when a relationship model fails its validation. */
	String RELATIONSHIP_MODEL_VALIDATION_ERROR_KEY = "error.model.relationship.validation";

	/** Localization key used when relationship validation finds a mismatched model version. */
	String RELATIONSHIP_VALIDATION_WRONG_VERSION_ERROR_KEY = "error.validation.model.relationship.wrongVersion";
	/** Localization key used when the number of role entities in a link is invalid for the relationship. */
	String RELATIONSHIP_VALIDATION_ENTITY_SIZE_ERROR_KEY = "error.validation.entities.size";
	/** Localization key (with role placeholder) used when a relationship link contains an invalid entity for a role. */
	String RELATIONSHIP_VALIDATION_BAD_ENTITY_ERROR_KEY = "error.validation.link.badEntity_%s";

	/** Localization key used when a relationship link cannot be found. */
	String RELATIONSHIP_LINK_NOT_FOUND_ERROR_KEY = "error.relationship.link.notFound";
	/** Localization key used when a relationship link id is malformed. */
	String RELATIONSHIP_LINK_ID_INVALID_ERROR_KEY = "error.relationship.link.id.invalid";
	/** Localization key used when a relationship references an invalid model name. */
	String RELATIONSHIP_INVALID_MODEL_NAME_ERROR_KEY = "error.relationship.model.name.invalid";
	/** Localization key used when a link operation is missing a required role reference. */
	String RELATIONSHIP_LINK_ROLE_MISSING_ERROR_KEY = "error.link.role.missing";
	/** Localization key used for generic relationship link validation failures. */
	String RELATIONSHIP_LINK_VALIDATION_ERROR_KEY = "error.link_validation";
	/** Localization key used when a role characteristic required for link validation is missing. */
	String RELATIONSHIP_LINK_VALIDATION_ROLE_MISSING_ERROR_KEY = "error.link.validation.roleCharacteristic.missing";
	/** Localization key used when a link validates against the wrong model. */
	String RELATIONSHIP_LINK_VALIDATION_BAD_MODEL_ERROR_KEY = "error.validation.link.badModel";
	/** Localization key used when the model expected by link validation is missing. */
	String RELATIONSHIP_LINK_VALIDATION_MODEL_MISSING_ERROR_KEY = "error.link.model.notFound";
	/** Localization key used when a link points to a document that cannot be found during validation. */
	String RELATIONSHIP_LINK_VALIDATION_DOCUMENT_NOT_FOUND_ERROR_KEY = "error.link.document.notFound";
	/** Localization key used when serializing a relationship link document fails. */
	String RELATIONSHIP_LINK_DOCUMENT_SERIALIZATION_ERROR_KEY = "error.link.document.serialization";
	/** Localization key used when a relationship link document fails validation. */
	String RELATIONSHIP_LINK_DOCUMENT_VALIDATION_ERROR_KEY = "error.link.document.validation";
	/** Localization key used when an add-link operation references a document that cannot be found. */
	String RELATIONSHIP_LINK_ADD_DOCUMENT_NOT_FOUND_ERROR_KEY = "error.add_link.document.notFound";
	/** Localization key used when an add-link operation references a document of an incompatible model. */
	String RELATIONSHIP_LINK_ADD_DOCUMENT_BAD_MODEL_ERROR_KEY = "error.add_link.document.badModel";
	/** Localization key used when an add-link operation references a predecessor link that cannot be found. */
	String RELATIONSHIP_LINK_ADD_PREDECESSOR_LINK_NOT_FOUND_ERROR_KEY = "error.add_link.predecessorLinkRef.notFound";

	/** Localization key used when a relink operation targets a document not compatible with the relationship. */
	String RELATIONSHIP_RELINK_DOCUMENT_NOT_COMPATIBLE_ERROR_KEY = "error.relink_document.relationship.notCompatible";


	//Attachments exceptions

	/** Localization key used when a referenced attachment cannot be found. */
	String ATTACHMENT_NOT_FOUND_ERROR_KEY = "error.attachment.notFound";
	/** Localization key used as a generic fallback for attachment failures. */
	String ATTACHMENT_GENERAL_ERROR_KEY = "error.attachment.general";

	/** Localization key used when converting an attachment to a thumbnail fails. */
	String ATTACHMENT_THUMBNAIL_CONVERSION_ERROR_KEY = "error.attachment.thumbnail.conversion.error";

	/** Localization key used when an attachment uses an unsupported or unexpected type. */
	String ATTACHMENT_INVALID_TYPE_ERROR_KEY = "error.attachment.invalidType";
	/** Localization key used when an incoming HTTP request body cannot be parsed. */
	String HTTP_MESSAGE_NOT_READABLE_ERROR_KEY = "error.http.message.notReadable";

	// Input validation exceptions

	/** Localization key used when generic input validation fails (not specific to any operation). */
	String INVALID_INPUT_ERROR_KEY = "error.input.invalid";

	// Query exceptions

	/** Localization key used when generic input validation rejects a query. */
	String QUERY_INVALID_INPUT_ERROR_KEY = "error.input.invalid";
	/** Localization key used when query paging parameters are invalid. */
	String QUERY_INVALID_PAGING_ERROR_KEY = "error.query.invalid.paging";
	/** Localization key used when the requested page size exceeds the configured limit. */
	String QUERY_PAGE_REQUEST_LIMIT_EXCEEDED_ERROR_KEY = "error.query.pageRequest.limit.exceeded";

	/** Localization key used when the number of links traversed by a query exceeds the configured limit. */
	String QUERY_LINKS_LIMIT_EXCEEDED_ERROR_KEY = "error.query.links.limit.exceeded";
	/** Localization key used when a query specifies an invalid sorting order. */
	String QUERY_INVALID_SORTING_ERROR_KEY = "error.query.invalid.sorting";
	/** Localization key used when a referenced query projection cannot be found. */
	String QUERY_PROJECTION_NOT_FOUND_ERROR_KEY = "error.query.projection.notFound";
	/** Localization key used when a query requests an unsupported locale. */
	String QUERY_UNSUPPORTED_LOCALE_ERROR_KEY = "error.query.unsupported.locale";

	// General exceptions
	// TODO A12S-4148: Rename to error.unknown in breaking release
	/** Localization key used as a final fallback when no more specific key applies. */
	String UNKNOWN_ERROR_KEY = "UNKNOWN";
	/** Localization key used when the current principal is not authorized for the requested action. */
	String SECURITY_NOT_AUTHORIZED_ERROR_KEY = "error.security.notAuthorized";
	/** Localization key used when an ABAC rule is detected to be invalid. */
	String SECURITY_INVALID_ABAC_RULE_ERROR_KEY = "error.security.abac.rule.invalid";
	/** Localization key used when a time/date value cannot be parsed. */
	String TIME_FORMAT_ERROR_KEY = "error.time.format";
	/** Localization key used when a value cannot be converted to or from JSON. */
	String CONVERT_JSON_ERROR_KEY = "error.convert.json";

	/** Localization key used when a file-system I/O operation fails. */
	String FILE_SYSTEM_IO_ERROR_KEY = "error.file.creation.error";
	/** Localization key used when resolving a resource from its location fails. */
	String RESOURCE_RESOLUTION_ERROR_KEY = "error.resource.resolution";
	/** Localization key used when a URI cannot be constructed from its parts. */
	String URI_FORMATION_ERROR_KEY = "error.uri.formation.error";
	/** Localization key used when a feature is disabled by configuration. */
	String FUNCTIONALITY_DISABLED_ERROR_KEY = "error.functionality.disabled";
	/** Localization key used when application configuration contains conflicting or invalid values. */
	String MISCONFIGURATION_ERROR_KEY = "error.configuration.invalid";
	/** Localization key used when a field instance cannot be converted to its Java type. */
	String FIELD_INSTANCE_TO_JAVA_TYPE_ERROR_KEY = "error.fieldInstance.javaConversion";
	/** Localization key used when a configured hard limit is exceeded. */
	String HARD_LIMIT_EXCEEDED_ERROR_KEY = "error.input.hardlimit.exceeded";
	/** Localization key used when a query is structurally invalid. */
	String INVALID_QUERY_ERROR_KEY = "error.input.query.invalid";

	//misc
	/** Localization key used when an external enumeration value cannot be resolved. */
	String EXTERNAL_ENUM_NOT_FOUND_ERROR_KEY = "error.external.enumeration.notFound";

	/** Localization key used when the content store server cannot be reached. */
	String CONTENT_STORE_SERVER_CONNECTION_ERROR = "error.content.store.server.connection.error";

	/** Localization key used when exporting a CDD list fails. */
	String EXPORT_LIST_CDD_ERROR_KEY = "error.list.cdd.export.error";
	/** Localization key used when an SME workspace import fails. */
	String SME_WORKSPACE_IMPORT_ERROR_KEY = "error.sme-workspace.import";
}

