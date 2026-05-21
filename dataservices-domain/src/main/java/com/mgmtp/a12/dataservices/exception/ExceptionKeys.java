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

/**
 * Exception Keys interface. Localization keys for exceptions used throughout the application.
 *
 */
public interface ExceptionKeys {

	/**
	 * Distinct phases of query processing used to qualify errors.
	 */
	enum ExecutionPhase {
		QUERY_PERMISSION_CHECK,
		QUERY_PREPROCESSING,
		QUERY_VALIDATION,
		QUERY_ENRICHMENT,
		QUERY_SQL_GENERATION,
		QUERY_EXECUTION,
		QUERY_POSTPROCESSING,
		QUERY_RESPONSE_MAPPING,
		QUERY_GENERAL,
		QUERY_INDEXING
	}

	//Document exceptions

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	String DOCUMENT_CREATE_ERROR_KEY = "error.document.create";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	String DOCUMENT_UPDATE_ERROR_KEY = "error.document.update";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	String DOCUMENT_DELETE_ERROR_KEY = "error.document.delete";
	String DOCUMENT_NOT_FOUND_ERROR_KEY = "error.document.notFound";
	String DOCUMENT_CONVERSION_ERROR_KEY = "error.document.convert";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	String DOCUMENT_UNIQUENESS_ERROR_KEY = "error.document.uniqueness";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	String DOCUMENT_MISMATCH_ERROR_KEY = "error.document.mismatch";
	String DOCUMENT_VALIDATION_ERROR_KEY = "document.validation.error";
	String DOCUMENT_INTEGRITY_ERROR_KEY = "error.document.integrity";
	String REQUEST_IDEMPOTENCY_ERROR_KEY = "error.request.idempotency";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	String DOCUMENT_ASSIGNED_ERROR_KEY = "error.document.assigned";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	String DOCUMENT_UNAVAILABLE_ERROR_KEY = "error.document.visibility";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	String DOCUMENT_IN_USE_ERROR_KEY = "error.document.inUse";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	String DOCUMENT_MODEL_MISMATCH_ERROR_KEY = "error.document.model.mismatch";
	String DOCUMENT_MODEL_HETEROGENEITY_ERROR_KEY = "error.document.model.heterogeneity";
	String DOCUMENT_MODEL_SERIALIZATION_ERROR_KEY = "error.document.model.serialization";
	String DOCUMENT_MODEL_DESERIALIZATION_ERROR_KEY = "error.document.model.deserialization";
	String DOCUMENT_MODEL_JOINING_ERROR_KEY = "error.document.model.joining";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	String DOCUMENT_IMPORT_DISABLED_ERROR_KEY = "error.document.import.disabled";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	String DOCUMENT_COMPUTATION_ERROR_KEY = "error.document.computation";
	String DOCUMENT_LOCALES_INVALID_ERROR_KEY = "error.document.locales.invalid";
	String DOCUMENT_ABSTRACT_MODEL_ERROR_KEY = "error.add_document.abstractness.violated";
	String VALIDATION_CODES_GENERATION_ERROR_KEY = "error.model.generate.validation.codes";


	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	String DOCUMENT_SEARCH_QUERY_ERROR_KEY = "error.document.search.query.failed";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	String DOCUMENT_SEARCH_FILTER_UNSUPPORTED_LANG_ERROR_KEY = "error.document.search.filter.unsupported.lang";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	String DOCUMENT_SEARCH_UPDATE_ERROR_KEY = "error.document.search.update.failed";
	String INVALID_FACET_ERROR_KEY = "error.document.search.query.facet.invalidInput";

	String DOCUMENT_FIELD_ERROR_KEY = "error.document.field";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	String DOCUMENT_FIELD_MISMATCH_KEY = "error.document.field.mismatch";

	String DOCUMENT_REFERENCE_INVALID_INPUT = "error.document.documentReference.invalidInput";

	//RPC exceptions
	String ADD_DOCUMENT_ERROR_KEY = "error.add_document.error";
	String COPY_DOCUMENT_ERROR_KEY = "error.copy_document.error";
	String MODIFY_DOCUMENT_NOT_FOUND_ERROR_KEY = "error.modify_document.document.notFound";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	String RPC_ROLLBACK_FAILED_ERROR_KEY = "error.rpc.rollbackFailed";
	String RPC_OPERATION_PREVIOUS_FAILED_KEY = "rpc.operation.previous.error";
	String RPC_OPERATION_ERROR_KEY = "rpc.operation.error";
	String RPC_NUMBER_OF_OPERATIONS_EXCEEDED_ERROR_KEY = "error.rpc.numberOfOperations.exceeded";
	String RPC_ID_NULL_ERROR_KEY = "error.rpc.id.null";

	//Models exceptions
	String MODEL_NOT_FOUND_ERROR_KEY = "error.model.notFound";
	String MODEL_ACCESS_DENIED_ERROR_KEY = "error.model.accessDenied";
	String MODEL_VERSION_MISMATCH_ERROR_KEY = "error.model.version.mismatch";
	String MODEL_DESERIALIZATION_ERROR_KEY = "error.model.deserialization";
	String MODEL_VALIDATION_ERROR_KEY = "error.model.validation";
	String MODEL_UNIQUENESS_ERROR_KEY = "model.uniqueness.violated";
	String MODEL_ID_NOT_FOUND_ERROR_KEY = "error.model.idNotFound";
	String MODEL_ID_NOT_VALID_ERROR_KEY = "error.model.idNotValid";
	String MODEL_MODEL_TYPE_NOT_FOUND_ERROR_KEY = "error.model.modelTypeNotFound";
	String MODEL_ROLES_NOT_FOUND_ERROR_KEY = "error.model.rolesNotFound";
	String MODEL_MISMATCH_ERROR_KEY = "error.model.mismatch";
	String MODEL_DUPLICITY_ERROR_KEY = "error.model.duplicity";
	String MODEL_BULK_IMPORT_GENERIC_ERROR_KEY = "error.model.bulk.import.generic";
	String MODEL_BULK_IMPORT_HEADER_PARSING_ERROR_KEY = "error.model.bulk.import.header.parsing";
	String NO_MODEL_REPOSITORY_FOUND = "error.model.repository.notFound";
	String MODEL_HEADER_ANNOTATION_IS_MISSING_ERROR_KEY = "error.model.headerAnnotationIsMissing";


	//Relationship exceptions
	String RELATIONSHIP_MODEL_NOT_FOUND_ERROR_KEY = "error.model.relationship.notFound";
	String RELATIONSHIP_MODEL_DELETE_LINK_EXISTS_ERROR_KEY = "error.model.relationship.delete.linkExist";
	String RELATIONSHIP_MODEL_SERIALIZATION_ERROR_KEY = "error.model.relationship.serialization";
	String RELATIONSHIP_MODEL_VALIDATION_ERROR_KEY = "error.model.relationship.validation";

	String RELATIONSHIP_VALIDATION_WRONG_VERSION_ERROR_KEY = "error.validation.model.relationship.wrongVersion";
	String RELATIONSHIP_VALIDATION_ENTITY_SIZE_ERROR_KEY = "error.validation.entities.size";
	String RELATIONSHIP_VALIDATION_BAD_ENTITY_ERROR_KEY = "error.validation.link.badEntity_%s";

	String RELATIONSHIP_LINK_NOT_FOUND_ERROR_KEY = "error.relationship.link.notFound";
	String RELATIONSHIP_LINK_ID_INVALID_ERROR_KEY = "error.relationship.link.id.invalid";
	String RELATIONSHIP_INVALID_MODEL_NAME_ERROR_KEY = "error.relationship.model.name.invalid";
	String RELATIONSHIP_LINK_ROLE_MISSING_ERROR_KEY = "error.link.role.missing";
	String RELATIONSHIP_LINK_VALIDATION_ERROR_KEY = "error.link_validation";
	String RELATIONSHIP_LINK_VALIDATION_ROLE_MISSING_ERROR_KEY = "error.link.validation.roleCharacteristic.missing";
	String RELATIONSHIP_LINK_VALIDATION_BAD_MODEL_ERROR_KEY = "error.validation.link.badModel";
	String RELATIONSHIP_LINK_VALIDATION_MODEL_MISSING_ERROR_KEY = "error.link.model.notFound";
	String RELATIONSHIP_LINK_VALIDATION_DOCUMENT_NOT_FOUND_ERROR_KEY = "error.link.document.notFound";
	String RELATIONSHIP_LINK_DOCUMENT_SERIALIZATION_ERROR_KEY = "error.link.document.serialization";
	String RELATIONSHIP_LINK_DOCUMENT_VALIDATION_ERROR_KEY = "error.link.document.validation";
	String RELATIONSHIP_LINK_ADD_DOCUMENT_NOT_FOUND_ERROR_KEY = "error.add_link.document.notFound";
	String RELATIONSHIP_LINK_ADD_DOCUMENT_BAD_MODEL_ERROR_KEY = "error.add_link.document.badModel";
	String RELATIONSHIP_LINK_ADD_PREDECESSOR_LINK_NOT_FOUND_ERROR_KEY = "error.add_link.predecessorLinkRef.notFound";

	String RELATIONSHIP_RELINK_DOCUMENT_NOT_COMPATIBLE_ERROR_KEY = "error.relink_document.relationship.notCompatible";


	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	String OPERATION_DISABLED_ERROR_KEY = "error.operation.disabled";

	//Attachments exceptions

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	String ATTACHMENT_THUMBNAIL_INVALID_LOCATION_ERROR_KEY = "error.attachment.thumbnail.invalidLocation";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	String ATTACHMENT_THUMBNAIL_INCOMPLETE_ERROR_KEY = "error.attachment.thumbnail.incompleteInput";
	String ATTACHMENT_NOT_FOUND_ERROR_KEY = "error.attachment.notFound";
	String ATTACHMENT_GENERAL_ERROR_KEY = "error.attachment.general";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	String ATTACHMENT_IO_ERROR_KEY = "error.attachment.io.error";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	String ATTACHMENT_THUMBNAIL_GENERATION_ERROR_KEY = "error.attachment.thumbnail.generation.error";
	String ATTACHMENT_THUMBNAIL_CONVERSION_ERROR_KEY = "error.attachment.thumbnail.conversion.error";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	String ATTACHMENT_CORRUPTED_DATA_ERROR_KEY = "error.attachment.data.corrupted";
	String ATTACHMENT_INVALID_TYPE_ERROR_KEY = "error.attachment.invalidType";
	String ATTACHMENT_EMPTY_FILE_ERROR_KEY = "error.attachment.emptyFile";

	//Document import exceptions

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	String DOCUMENT_IMPORT_IO_ERROR_KEY = "error.document.import.io.error";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	String DOCUMENT_IMPORT_MODEL_RESOLUTION_ERROR_KEY = "error.document.import.model.resolution.error";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	String DOCUMENT_IMPORT_SERIALIZATION_ERROR_KEY = "error.document.import.serialization.error";

	// Query exceptions

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	String QUERY_TOPOLOGY_INVALID_AGGREAGATION_AND_FIELDS_ERROR_KEY = "error.query.topology.invalid.aggregation_and_fields";
	String QUERY_INVALID_INPUT_ERROR_KEY = "error.input.invalid";
	String QUERY_INVALID_PAGING_ERROR_KEY = "error.query.invalid.paging";
	String QUERY_PAGE_REQUEST_LIMIT_EXCEEDED_ERROR_KEY = "error.query.pageRequest.limit.exceeded";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	String QUERY_DEPTH_LIMIT_EXCEEDED_ERROR_KEY = "error.query.depth.limit.exceeded";
	String QUERY_LINKS_LIMIT_EXCEEDED_ERROR_KEY = "error.query.links.limit.exceeded";
	String QUERY_INVALID_SORTING_ERROR_KEY = "error.query.invalid.sorting";
	String QUERY_PROJECTION_NOT_FOUND_ERROR_KEY = "error.query.projection.notFound";
	String QUERY_UNSUPPORTED_LOCALE_ERROR_KEY = "error.query.unsupported.locale";

	// General exceptions
	// TODO A12S-4148: Rename to error.unknown in breaking release
	String UNKNOWN_ERROR_KEY = "UNKNOWN";
	String SECURITY_NOT_AUTHORIZED_ERROR_KEY = "error.security.notAuthorized";
	String SECURITY_INVALID_ABAC_RULE_ERROR_KEY = "error.security.abac.rule.invalid";
	String TIME_FORMAT_ERROR_KEY = "error.time.format";
	String CONVERT_JSON_ERROR_KEY = "error.convert.json";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	String UNSUPPORTED_TYPE_ERROR_KEY = "error.unsupported.type";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	String TOKEN_NOT_FOUND_ERROR_KEY = "error.token.notFound";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	String NO_PERSISTER_FOUND = "error.model.persister.notFound";
	String FILE_SYSTEM_IO_ERROR_KEY = "error.file.creation.error";
	String URI_FORMATION_ERROR_KEY = "error.uri.formation.error";
	String FUNCTIONALITY_DISABLED_ERROR_KEY = "error.functionality.disabled";
	String FIELD_INSTANCE_TO_JAVA_TYPE_ERROR_KEY = "error.fieldInstance.javaConversion";
	String HARD_LIMIT_EXCEEDED_ERROR_KEY = "error.input.hardlimit.exceeded";
	String INVALID_QUERY_ERROR_KEY = "error.input.query.invalid";

	//misc
	String EXTERNAL_ENUM_NOT_FOUND_ERROR_KEY = "error.external.enumeration.notFound";

	/**
	 * @deprecated Not used anymore, will be removed in the next breaking release.
	 */
	@Deprecated(since = "38.1.0", forRemoval = true)
	String NOT_FOUND = "error.controller.notFound";
	String CONTENT_STORE_SERVER_CONNECTION_ERROR = "error.content.store.server.connection.error";

	String EXPORT_LIST_CDD_ERROR_KEY = "error.list.cdd.export.error";
	String EXPORT_SEED_DATA_IMPORT_ERROR_KEY = "error.seed.data.import";
}

