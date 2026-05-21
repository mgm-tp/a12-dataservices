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
package com.mgmtp.a12.dataservices.query.indexing.internal;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.function.BiFunction;

import javax.sql.DataSource;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.document.DataServicesDocument;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.persistence.internal.AggregatedDocumentRepository;
import com.mgmtp.a12.dataservices.exception.query.QueryIndexingException;
import com.mgmtp.a12.dataservices.query.generator.sql.QueryGeneratorConstants.TableNames;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.DocumentModelFieldsIndexer;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.entity.jsonb.DocumentSearchEntity;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.entity.searchtable.DocumentFieldEntity;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.jsonb.DocumentSearchJpaRepository;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.searchtable.DocumentFieldsJpaRepository;
import com.mgmtp.a12.dataservices.search.customizer.internal.DocumentFieldContextImpl;
import com.mgmtp.a12.dataservices.search.customizer.internal.SearchCustomizerRegistry;
import com.mgmtp.a12.dataservices.utils.internal.DateTimeUtils;
import com.mgmtp.a12.dataservices.utils.internal.DefaultFieldFormatter;
import com.mgmtp.a12.dataservices.utils.internal.DocumentModelUtils;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationConfig;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.UpdateAction;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.utils.IDocumentV2Visitor;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.facade.DocumentServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.fieldtypes.IFieldType;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSearchService;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.ExecutionPhase.QUERY_INDEXING;
import static com.mgmtp.a12.dataservices.query.internal.QueryTopologyHelper.fieldTypeAsString;

@Slf4j
public class DocumentSearchIndexBehaviour extends AbstractIndexBehavior {

	public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	public static final TimeZone TIME_ZONE = TimeZone.getTimeZone(DateTimeUtils.STANDARD_TIME_ZONE);
	protected final DocumentSearchJpaRepository documentSearchJpaRepository;
	protected final IDocumentV2Serializer documentV2Serializer;
	protected final SearchCustomizerRegistry searchCustomizerRegistry;

	private final EntityManager entityManager;

	public static final String DOCUMENTS_TARGET = "documents";
	public static final String FIELDS_TARGET = "fields";
	public static final String CORE_SOURCE = "core";

	public DocumentSearchIndexBehaviour(AggregatedDocumentRepository aggregatedDocumentRepository, DocumentFieldsJpaRepository documentFieldsJpaRepository,
		DocumentModelServiceFactory documentModelServiceFactory, DataSource dataSource, DocumentModelFieldsIndexer documentModelFieldsIndexer,
		DocumentServiceFactory documentServiceFactory, DocumentSearchJpaRepository documentSearchJpaRepository, EntityManager entityManager,
		IDocumentV2Serializer documentV2Serializer, SearchCustomizerRegistry searchCustomizerRegistry) {
		super(aggregatedDocumentRepository, documentFieldsJpaRepository, documentModelServiceFactory, documentModelFieldsIndexer, documentServiceFactory,
			dataSource);
		this.documentSearchJpaRepository = documentSearchJpaRepository;
		this.entityManager = entityManager;
		this.documentV2Serializer = documentV2Serializer;
		this.searchCustomizerRegistry = searchCustomizerRegistry;
	}

	@Override protected String getCopySql(String target) {
		String columns = String.join(",", DocumentSearchIndexHelper.getColumnNames(target));
		String tableName = FIELDS_TARGET.equals(target) ? TableNames.DOCUMENT_FIELDS_TABLE_NAME : TableNames.DOCUMENT_SEARCH_TABLE_NAME;
		return String.format("COPY %s (%s) FROM STDIN WITH (FORMAT CSV)", tableName, columns);

	}

	@Override protected Map<String, byte[]> convertToCsv(List<DataServicesDocument> dataServicesDocuments,
		IDocumentModelSearchService documentModelSearchService, BiFunction<String, String, Long> fieldTypeIdProvider) throws IOException {

		List<DocumentSearchEntity> jsonbDocumentFieldEntities = new ArrayList<>(dataServicesDocuments.size());
		List<DocumentFieldEntity> rangeFieldEntities = new ArrayList<>(dataServicesDocuments.size() * 10);
		for (DataServicesDocument dataServicesDocument : dataServicesDocuments) {
			Pair<DocumentSearchEntity, List<DocumentFieldEntity>> documentAndFields =
				getEntitiesToPersist(documentModelSearchService, dataServicesDocument, fieldTypeIdProvider,
					dataServicesDocument.getMetadata().getDocumentModelReference());
			jsonbDocumentFieldEntities.add(documentAndFields.getLeft());
			rangeFieldEntities.addAll(documentAndFields.getRight());
		}

		return Map.of(
			DOCUMENTS_TARGET, DocumentSearchIndexCsvHelper.getDocumentSearchFieldsAsCsv(jsonbDocumentFieldEntities),
			FIELDS_TARGET, DocumentSearchIndexCsvHelper.getRangeFieldsAsCSV(rangeFieldEntities));
	}

	public void saveDocumentFields(DataServicesDocument documentToCreate, IDocumentModelSearchService documentModelSearchService,
		BiFunction<String, String, Long> fieldIdProvider) {

		Pair<DocumentSearchEntity, List<DocumentFieldEntity>> documentAndFields =
			getEntitiesToPersist(documentModelSearchService, documentToCreate, fieldIdProvider, documentToCreate.getMetadata().getDocumentModelReference());
		DocumentSearchEntity documentSearchEntity = documentAndFields.getLeft();

		Session session = entityManager.unwrap(Session.class);

		session.createNativeQuery(
				"""
					insert into document_search (model_name, doc_ref, value, original_value, search_data)
					values (:modelName, :docRef, cast(:value as jsonb), cast(:originalValue as jsonb), :searchData)
					on conflict (doc_ref) do update
					    set model_name = excluded.model_name,
					        doc_ref = excluded.doc_ref,
					        value = excluded.value,
					        original_value = excluded.original_value,
					        search_data = excluded.search_data
					""", Integer.class)
			.addSynchronizedEntityClass(DocumentSearchEntity.class)
			.setParameter("modelName", documentSearchEntity.getModelName())
			.setParameter("docRef", documentSearchEntity.getDocRef())
			.setParameter("value", documentSearchEntity.getValue())
			.setParameter("originalValue", documentSearchEntity.getOriginalValue())
			.setParameter("searchData", documentSearchEntity.getSearchData())
			.executeUpdate();

		if (!documentAndFields.getRight().isEmpty()) {
			documentFieldsJpaRepository.saveAll(documentAndFields.getRight());
		}
	}

	public void dropDocumentFields(DataServicesDocument document) {
		String docRef = document.getMetadata().getDocRef().toString();
		documentSearchJpaRepository.deleteByDocRef(docRef);
		documentFieldsJpaRepository.deleteDocumentFieldEntitiesByDocRef(docRef);
	}

	private Pair<DocumentSearchEntity, List<DocumentFieldEntity>> getEntitiesToPersist(IDocumentModelSearchService documentModelSearchService,
		DataServicesDocument dataServicesDocument, BiFunction<String, String, Long> fieldTypeIdProvider, String modelName) {

		// Prepare both documents: one for search values (on the left), and one with original values minus non-indexable fields (on the right).
		List<DocumentFieldEntity> documentFieldEntities = new ArrayList<>();
		Pair<DocumentV2, DocumentV2> searchDocuments = prepareDocumentsForSearchTable(documentModelSearchService, dataServicesDocument, fieldTypeIdProvider,
			documentFieldEntities);

		DocumentSearchEntity jsonbDocumentField = makeDocumentSearchEntity(dataServicesDocument, documentModelSearchService, searchDocuments, modelName);

		return Pair.of(jsonbDocumentField, documentFieldEntities);
	}

	/**
	 * Prepares documents for the search table that only contains indexable fields and collects range-aware field entities.
	 * Use `getLeft()` to get the document with searchable values, and `getRight()` to get the original document with non-indexable fields removed.
	 *
	 * @param documentModelSearchService the document model search service
	 * @param originalDocument the original `DataServicesDocument`
	 * @param fieldTypeIdProvider function to provide field type IDs
	 * @param documentFieldEntities the list to collect range-aware field entities
	 * @return a pair of the document with search values (on the left), and the original document with non-indexable fields removed (on the right).
	 */
	private Pair<DocumentV2, DocumentV2> prepareDocumentsForSearchTable(IDocumentModelSearchService documentModelSearchService,
		DataServicesDocument originalDocument, BiFunction<String, String, Long> fieldTypeIdProvider, List<DocumentFieldEntity> documentFieldEntities) {

		final DocumentV2[] documentWithOriginalValues = { originalDocument.getKernelDocument() };
		Collection<UpdateAction> searchableDocumentUpdates = new ArrayList<>();

		documentWithOriginalValues[0].traverse(new IDocumentV2Visitor() {
			@Override public void visitField(DocumentPointer pointerRelativeToBase, FieldInstanceV2 field) {
				IDocumentV2Visitor.super.visitField(pointerRelativeToBase, field);
				if (!isIndexable(documentModelSearchService, pointerRelativeToBase)) {
					documentWithOriginalValues[0] = documentWithOriginalValues[0].withFieldRemoved(pointerRelativeToBase);
				} else {
					searchableDocumentUpdates.add(UpdateAction.putFieldValue(pointerRelativeToBase, field.value()));
					DocumentReference documentReference = originalDocument.getMetadata().getDocRef();

					if (!customizeFieldForSearch(documentFieldEntities, documentModelSearchService, fieldTypeIdProvider, documentReference, field,
						pointerRelativeToBase)) {
						addFieldsForSearch(documentFieldEntities, documentModelSearchService, fieldTypeIdProvider, documentReference, field,
							pointerRelativeToBase);
					}
				}
			}
		});
		DocumentV2 documentWithSearchableValues = DocumentV2
			.empty(documentWithOriginalValues[0].getDocumentModelId())
			.withBatchUpdates(searchableDocumentUpdates);

		return Pair.of(documentWithSearchableValues, documentWithOriginalValues[0]);
	}

	/**
	 * Adds a `DocumentFieldEntity` to the provided list if the field is range-aware.
	 *
	 * @param documentFieldEntities the list to collect range-aware field entities
	 * @param documentModelSearchService the document model search service
	 * @param fieldTypeIdProvider function to provide field type IDs
	 * @param documentReference the document reference
	 * @param field the field instance
	 * @param pointerRelativeToBase the document pointer relative to the base document
	 */
	private void addFieldsForSearch(List<DocumentFieldEntity> documentFieldEntities, IDocumentModelSearchService documentModelSearchService,
		BiFunction<String, String, Long> fieldTypeIdProvider, DocumentReference documentReference, FieldInstanceV2 field,
		DocumentPointer pointerRelativeToBase) {

		DocumentSearchIndexHelper.getIFieldTypeIfRangeAware(documentModelSearchService, pointerRelativeToBase.fullName())
			.ifPresent(fieldType -> {
					if (field.value() != null) {
						DocumentFieldEntity coreEntity = makeFieldEntity(documentReference, fieldType, field.value(), fieldTypeIdProvider,
							pointerRelativeToBase);
						if (coreEntity != null) {
							documentFieldEntities.add(coreEntity);
						}
					}
				}
			);

	}

	/**
	 * Applies custom field indexing logic through registered search customizers.
	 *
	 * This method allows customization of field indexing behavior by delegating to registered search customizers.
	 * Customizers can add additional field entities or skip core field indexing entirely.
	 *
	 * @param documentFieldEntities the list to collect additional field entities from customizers
	 * @param documentModelSearchService the document model search service
	 * @param fieldTypeIdProvider function to provide field type IDs
	 * @param documentReference the document reference
	 * @param field the field instance to customize
	 * @param pointerRelativeToBase the document pointer relative to the base document
	 * @return true if core field indexing should be skipped, false otherwise
	 */
	private boolean customizeFieldForSearch(List<DocumentFieldEntity> documentFieldEntities, IDocumentModelSearchService documentModelSearchService,
		BiFunction<String, String, Long> fieldTypeIdProvider, DocumentReference documentReference, FieldInstanceV2 field,
		DocumentPointer pointerRelativeToBase) {

		return Optional.of(documentModelSearchService)
			.filter(x -> field.value() != null)
			.filter(x -> searchCustomizerRegistry.hasCustomizers())
			.flatMap(s -> s.getByPath(pointerRelativeToBase.fullName()))
			.filter(IField.class::isInstance)
			.map(IField.class::cast)
			.map(iField -> customizeFieldForSearch(documentFieldEntities, documentModelSearchService, fieldTypeIdProvider, documentReference, field,
				pointerRelativeToBase, iField))
			.orElse(false);
	}

	private boolean customizeFieldForSearch(List<DocumentFieldEntity> documentFieldEntities, IDocumentModelSearchService documentModelSearchService,
		BiFunction<String, String, Long> fieldTypeIdProvider, DocumentReference documentReference, FieldInstanceV2 field,
		DocumentPointer pointerRelativeToBase, IField iField) {

		DocumentFieldContextImpl documentFieldContext = searchCustomizerRegistry.customizeDocumentFields(documentModelSearchService,
			documentReference, pointerRelativeToBase, field, iField, fieldTypeIdProvider);
		documentFieldEntities.addAll(documentFieldContext.getAdditionalFields());
		return documentFieldContext.isCoreFieldIndexingSkipped();
	}

	private DocumentSearchEntity makeDocumentSearchEntity(DataServicesDocument dataServicesDocument,
		IDocumentModelSearchService documentModelSearchService, Pair<DocumentV2, DocumentV2> searchDocuments, String modelName) {

		return DocumentSearchEntity.builder()
			.docRef(dataServicesDocument.getMetadata().getDocRef().toString())
			.modelName(dataServicesDocument.getMetadata().getDocumentModelReference())
			.originalValue(serializeDocument(searchDocuments.getRight()))
			.searchData(prepareSearchDataColumn(documentModelSearchService, searchDocuments.getLeft(), modelName))
			.value(DocumentSearchIndexHelper.prepareSearchableJsonOfEnums(documentModelSearchService, searchDocuments.getLeft()))
			.build();
	}

	private String prepareSearchDataColumn(IDocumentModelSearchService documentModelSearchService, DocumentV2 indexableDocument, String modelName) {
		String baseSearchData = SearchDataBuildHelper.buildSearchData(documentModelSearchService, indexableDocument);
		return searchCustomizerRegistry.customizeSearchData(documentModelSearchService, indexableDocument, modelName, baseSearchData);
	}

	private String getFormattedValue(Object value, IFieldType fieldType) {
		return DocumentSearchIndexHelper.getFieldFormatter(fieldType).format(value, DateTimeUtils.getFieldFormat(fieldType).orElse(null), TIME_ZONE);
	}

	@NotNull private Optional<String> getFormattedTypedValue(Object value, IFieldType fieldType) {
		return Optional.ofNullable(fieldType)
			.map(DocumentSearchIndexHelper::getFieldFormatter)
			.filter(f -> !(f instanceof DefaultFieldFormatter))
			.map(f -> f.format(value, null, TIME_ZONE));
	}

	private DocumentFieldEntity makeFieldEntity(DocumentReference documentReference,
		IFieldType fieldType, Object value, BiFunction<String, String, Long> fieldTypeIdProvider, DocumentPointer pointerRelativeToBase) {

		String formattedValue = getFormattedValue(value, fieldType);
		if (formattedValue == null) {
			return null;
		}

		String typeValueString = getFormattedTypedValue(value, fieldType).orElse(null);

		DocumentFieldEntity.DocumentFieldEntityBuilder fieldBuilder = DocumentFieldEntity.builder()
			.docRef(documentReference.toString())
			.modelName(documentReference.getDocumentModelName())
			.fieldName(pointerRelativeToBase.fullName())
			.repetitions(pointerRelativeToBase.repetitionIndexes().stream().mapToInt(Integer::intValue).toArray())
			.value(formattedValue)
			.fieldType(fieldTypeAsString(fieldType))
			.fieldTypeId(fieldTypeIdProvider.apply(documentReference.getDocumentModelName(), pointerRelativeToBase.fullName()))
			.typedValue(typeValueString)
			.numberValue(DocumentSearchIndexHelper.getNumberValue(formattedValue, fieldType))
			.timestampValue(DocumentSearchIndexHelper.getDateTimeValue(typeValueString, fieldType))
			.tsRangeValue(DocumentSearchIndexHelper.getRangeValue(typeValueString, fieldType))
			.source(CORE_SOURCE);

		return fieldBuilder.build();
	}

	private boolean isIndexable(IDocumentModelSearchService documentModelSearchService, DocumentPointer pointerRelativeToBase) {
		return DocumentModelUtils.findField(documentModelSearchService, pointerRelativeToBase.fullName())
			.filter(documentModelFieldsIndexer::isIndexable)
			.isPresent();
	}

	private String serializeDocument(DocumentV2 document) {
		try (StringWriter stringWriter = new StringWriter()) {
			DocumentSerializationConfig serializationConfig = DocumentSerializationConfig.builder()
				.format(DocumentSerializationConfig.Format.JSON)
				.build();
			documentV2Serializer.serializeV2(document, stringWriter, serializationConfig);
			return stringWriter.toString();
		} catch (IOException e) {
			throw new QueryIndexingException(QUERY_INDEXING, "Serializing document failed", e);
		}
	}
}
