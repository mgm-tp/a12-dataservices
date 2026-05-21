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
package com.mgmtp.a12.dataservices.query.projection.internal;

import java.io.StringReader;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.testng.TestException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.mgmtp.a12.dataservices.authorization.ModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.authorization.internal.DefaultDocumentModelPermissionEvaluator;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.constants.DocumentModelConstants;
import com.mgmtp.a12.dataservices.document.DataServicesDocumentMetadata;
import com.mgmtp.a12.dataservices.document.DocumentReference;
import com.mgmtp.a12.dataservices.document.DocumentSpec;
import com.mgmtp.a12.dataservices.document.IDataServicesDocumentMetadataExtractor;
import com.mgmtp.a12.dataservices.document.internal.DefaultDataServicesDocumentMetadataExtractor;
import com.mgmtp.a12.dataservices.exception.query.QueryValidationException;
import com.mgmtp.a12.dataservices.internal.query.fields.AbstractProjectionTest;
import com.mgmtp.a12.dataservices.model.document.persistence.DocumentModelReadRepository;
import com.mgmtp.a12.dataservices.model.document.persistence.internal.DocumentModelLoader;
import com.mgmtp.a12.dataservices.model.relationship.persistence.RelationshipModelLoader;
import com.mgmtp.a12.dataservices.query.DocumentTreeNodeType;
import com.mgmtp.a12.dataservices.query.DocumentTreeResult;
import com.mgmtp.a12.dataservices.query.QueryPage;
import com.mgmtp.a12.dataservices.query.internal.DefaultQueryContext;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.model.notification.RankedNotification;

import lombok.NonNull;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class JsonbCddProjectionImplementationTest extends AbstractProjectionTest {

	private final DocumentModelReadRepository documentModelReadRepository = mock(DocumentModelReadRepository.class);
	private final DefaultQueryContext.InternalQueryAction internalQueryAction = mock(DefaultQueryContext.InternalQueryAction.class);

	private final ModelPermissionEvaluator<IDocumentModel> documentModelPermissionEvaluator = mock(DefaultDocumentModelPermissionEvaluator.class);

	private final DocumentModelServiceFactory documentModelServiceFactory = new DocumentModelServiceFactory();
	private final IDataServicesDocumentMetadataExtractor dataServicesDocumentMetadataExtractor =
		new DefaultDataServicesDocumentMetadataExtractor();

	private final JsonbCddProjectionImplementation cddProjectionImplementation =
		new JsonbCddProjectionImplementation(documentModelService, documentModelServiceFactory, dataServicesCoreProperties, objectMapper, documentTreeHelper,
			Optional.of(kernelTestSupport.getKernelDocumentService()), documentSupport, cdmHelper);

	private final DocumentModelLoader documentModelLoader =
		new DocumentModelLoader(documentModelPermissionEvaluator, eventPublisher, documentModelReadRepository);
	@Spy private final DefaultQueryContext queryContext = new DefaultQueryContext(documentModelLoader, mock(RelationshipModelLoader.class),
		internalQueryAction, documentModelServiceFactory, queryContextHelper, indexedModelFieldCache, Locale.getDefault().toString(), null);

	@BeforeMethod
	public void setUp() {

		doAnswer(invocation -> kernelTestSupport.getDocumentModelResolver().getDocumentModelById(invocation.getArgument(0, String.class)))
			.when(documentModelReadRepository).readModel(anyString());
	}

	@Test public void testPreprocess() {
		@NonNull QueryRoot result = cddProjectionImplementation.preprocess(QueryRoot.builder()
			.targetDocumentModel(DocumentModelConstants.CONTRACT_CDM_MODEL)
			.build(), queryContext);

		assertEquals(result.getTargetDocumentModel(), DocumentModelConstants.CONTRACT_DOCUMENT_MODEL);
	}

	@Test(expectedExceptions = QueryValidationException.class, expectedExceptionsMessageRegExp = "Fields are not allowed in query with cdd projection\\.")
	public void testPreprocessFieldsPresent() {
		cddProjectionImplementation.preprocess(QueryRoot.builder()
			.targetDocumentModel(DocumentModelConstants.CONTRACT_CDM_MODEL)
			.field("/ContractRoot/Name")
			.build(), queryContext);

		fail();
	}

	@Test
	public void testQueryWithConstraintForParent() throws JsonProcessingException {
		DocumentTreeResult r = new DocumentTreeResult();
		r.setDocRef(DocumentReference.builder()
			.documentModelName(DocumentModelConstants.CONTRACT_AUTOMOTIVE_DOCUMENT_MODEL)
			.documentId("1")
			.build());
		r.setType(DocumentTreeNodeType.ROOT);
		QueryRoot ppQuery = cddProjectionImplementation.constructQueryFromLinks(
			documentModelReadRepository.readModel(DocumentModelConstants.CONTRACT_CDM_MODEL),
			JsonbCddProjectionImplementation.constructDocRefConstraints(Stream.of(r.getDocRef().toString())),
			cdmHelper.cdmToLinks(documentModelReadRepository.readModel(DocumentModelConstants.CONTRACT_CDM_MODEL).getContent()
				.getDocumentModelRoot(), queryContext).toList(),
			dataServicesCoreProperties.getQuery().getPageRequest()
		);

		JSONAssert.assertEquals("""
			{
			    "links": [
			        {
			            "links": [
			                {
			                    "backReference": "/ContractBusinessPartner/PartnerAddresses",
			                    "relationshipModel": "PartnerAddresses",
			                    "targetRole": "Address",
			                    "maxDepth": 1
			                },
			                {
			                    "backReference": "/ContractBusinessPartner/PartnerPostalAddress",
			                    "relationshipModel": "PartnerPostalAddress",
			                    "targetRole": "Address",
			                    "maxDepth": 1
			                }
			            ],
			            "backReference": "/ContractBusinessPartner",
			            "relationshipModel": "ContractBusinessPartner",
			            "targetRole": "Partner",
			            "maxDepth": 1
			        },
			        {
			            "backReference": "/ContractCoInsuredPartner",
			            "relationshipModel": "ContractCoInsuredPartner",
			            "targetRole": "Partner",
			            "maxDepth": 1
			        }
			    ],
			    "constraint": {
			        "operator": "OrOperator",
			        "operands": [
			            {
			                "operator": "ExactMatchOperator",
			                "field": "/__meta/docRef",
			                "value": "ContractAutomotive/1",
			                "caseSensitive": true
			            }
			        ]
			    },
			    "targetDocumentModel": "Contract",
			    "exclude": true,
			    "paging": {
			        "pageNumber": 0,
			        "pageSize": 10000
			    }
			}
			""", jsonMapper.writeValueAsString(ppQuery), true);
	}

	@Test public void testPostProcess() throws JsonProcessingException {

		Page<DocumentTreeResult> rootDocuments = prepareCddPostProcess();
		Mockito.reset(kernelTestSupport.getKernelDocumentService());
		@NonNull QueryPage<DocumentSpec> result = cddProjectionImplementation.postprocess(QueryRoot.builder()
			.targetDocumentModel(DocumentModelConstants.CONTRACT_CDM_MODEL)
			.build(), rootDocuments, queryContext);

		verifyNoInteractions(kernelTestSupport.getKernelDocumentService());

		Consumer<RankedNotification> notifier =
			notification -> {throw new TestException("Got error during deserialization of document: %s".formatted(notification.getMessage()));};
		DocumentSpec documentSpec = result.getContent().getFirst();
		DocumentV2 docV2 = kernelTestSupport.getDocumentV2Serializer()
			.deserializeV2(new StringReader(documentSpec.getDocument()), DocumentModelConstants.CONTRACT_CDM_MODEL,
				kernelTestSupport.getDocumentDeserializationConfig(), notifier);

		DataServicesDocumentMetadata md =
			dataServicesDocumentMetadataExtractor.getMetadata(docV2);

		JSONAssert.assertEquals(
			String.format("""
					{
					  "ContractRoot" : {
					    "ContractName" : "Contract *",
					    "LengthOfContract" : 10,
					    "ContractValue" : 5000,
					    "Liability" : 10,
					    "CostToCustomer" : 2500,
					    "NoOfCoInsuredCustomers" : 5,
					    "MaxDiscount" : "Jana has a 50%% discount",
					    "CostPerCoInsured" : 2500,
					    "Valid" : false,
					    "Type" : "Health",
					    "ChangeLog" : [ {
					      "Number" : 3,
					      "ChangeTimestamp" : "2023-12-31",
					      "Description" : "Description3",
					      "User" : "Developer3",
					      "Changes" : [ {
					        "Title" : "Fix contract value",
					        "Details" : "Contract value was incorrect",
					        "Status" : "reverted"
					      } ]
					    }, {
					      "ChangeTimestamp" : "2023-03-15",
					      "Description" : "Change 1",
					      "Changes" : [ {
					        "Title" : "Apply new conditions",
					        "Details" : "Missing few of the conditions",
					        "Status" : "approved"
					      } ]
					    } ]
					  },
					  "ContractBusinessPartner" : {
					    "BusinessPartnerRoot" : {
					      "Name" : "Microoogle",
					      "Industry" : "Legal",
					      "StartOfRelationship" : "2022-08-01",
					      "CustomerDiscount" : "100%%",
					      "Employment" : {
					        "income" : 9999
					      },
					      "PersonOrEntity" : "Legal Entity"
					    },
					    "PartnerAddresses" : [ {
					      "AddressRoot" : {
					        "Location" : "mgm - Prague",
					        "Street" : "Letenské náměstí",
					        "HouseNumber" : "4/157",
					        "City" : "Praha 7",
					        "PostCode" : "170 00",
					        "Country" : "Czech Republic"
					      },
					      "__meta" : {
					        "docRef" : "Address/36388f5c-3036-4d39-bbe5-828337a2316e",
					        "modelReference" : "Address",
					        "creator" : "admin",
					        "createdAt" : "2025-03-28T10:41:18",
					        "modifier" : "admin",
					        "modifiedAt" : "2025-03-28T10:41:18"
					      }
					    }, {
					      "AddressRoot" : {
					        "Location" : "mgm - Vietnam",
					        "Street" : "Pasteur",
					        "HouseNumber" : "7",
					        "City" : "Đà Nẵng",
					        "PostCode" : "Hải Châu 1",
					        "Country" : "Vietnam"
					      },
					      "__meta" : {
					        "docRef" : "Address/3a8f8803-3e27-459f-8f59-19ac4de45127",
					        "modelReference" : "Address",
					        "creator" : "admin",
					        "createdAt" : "2025-03-28T10:41:18",
					        "modifier" : "admin",
					        "modifiedAt" : "2025-03-28T10:41:18"
					      }
					    } ],
					    "PartnerPostalAddress" : {
					      "AddressRoot" : {
					        "Location" : "mgm - Nürnberg",
					        "Street" : "Vordere Cramergasse",
					        "HouseNumber" : "11",
					        "City" : "Nürnberg",
					        "PostCode" : "90478",
					        "Country" : "Germany"
					      },
					      "__meta" : {
					        "docRef" : "Address/05c36862-da6a-4784-8735-cdb76f9a2313",
					        "modelReference" : "Address",
					        "creator" : "admin",
					        "createdAt" : "2025-03-28T10:41:18",
					        "modifier" : "admin",
					        "modifiedAt" : "2025-03-28T10:41:18"
					      }
					    },
					    "__meta" : {
					      "docRef" : "BusinessPartnerLTD/1a28681a-bb0a-44ae-9da3-5ea9a7b47731",
					      "modelReference" : "BusinessPartnerLTD",
					      "creator" : "admin",
					      "createdAt" : "2025-03-28T10:41:17",
					      "modifier" : "admin",
					      "modifiedAt" : "2025-03-28T10:41:17"
					    }
					  },
					  "ContractCoInsuredPartner" : [ {
					    "BusinessPartnerRoot" : {
					      "Name" : "CraigsRainforest",
					      "Industry" : "Commerce",
					      "StartOfRelationship" : "2019-08-06",
					      "CustomerDiscount" : "80%%",
					      "Employment" : {
					        "income" : 3333
					      },
					      "PersonOrEntity" : "Legal Entity"
					    },
					    "__meta" : {
					      "docRef" : "BusinessPartnerLTD/7af624da-72a7-4f88-beaa-16f89661ff06",
					      "modelReference" : "BusinessPartnerLTD",
					      "creator" : "admin",
					      "createdAt" : "2025-03-28T10:41:18",
					      "modifier" : "admin",
					      "modifiedAt" : "2025-03-28T10:41:18"
					    }
					  }, {
					    "BusinessPartnerRoot" : {
					      "Name" : "Alphameta",
					      "Industry" : "IT",
					      "StartOfRelationship" : "2017-08-03",
					      "CustomerDiscount" : "90%%",
					      "Employment" : {
					        "income" : 555.25
					      },
					      "PersonOrEntity" : "Legal Entity"
					    },
					    "__meta" : {
					      "docRef" : "BusinessPartnerLTD/d4776ae0-6b41-4c05-ae6c-bb755abf2134",
					      "modelReference" : "BusinessPartnerLTD",
					      "creator" : "admin",
					      "createdAt" : "2025-03-28T10:41:18",
					      "modifier" : "admin",
					      "modifiedAt" : "2025-03-28T10:41:18"
					    }
					  } ],
					  "__meta" : {
					    "docRef" : "Contract/3454f7df-9875-4a77-b940-4c44583d1b39",
					    "modelReference" : "Contract",
					    "creator" : "admin",
					    "createdAt" : "%s",
					    "modifier" : "admin",
					    "modifiedAt" : "%s"
					  }
					}
					""",
				getFormattedDate(md.getCreatedAt()),
				getFormattedDate(md.getModifiedAt())
			),
			documentSpec.getDocument(),
			false
		);
	}

	@Test public void testCddComputationPostProcess() throws JsonProcessingException {
		Page<DocumentTreeResult> rootDocuments = prepareCddPostProcess();
		dataServicesCoreProperties.getDocuments().getComputation().setEnabledForModels(List.of(DocumentModelConstants.CONTRACT_CDM_MODEL));
		doReturn(rootDocuments.getContent().getFirst().getDocument())
			.when(objectMapper).valueToTree(any(DocumentV2.class));
		cddProjectionImplementation.postprocess(QueryRoot.builder()
			.targetDocumentModel(DocumentModelConstants.CONTRACT_CDM_MODEL)
			.build(), rootDocuments, queryContext);
		verify(kernelTestSupport.getKernelDocumentService(), Mockito.times(1))
			.computeDocument(any(DocumentV2.class), any(Locale.class));
		dataServicesCoreProperties.getDocuments().getComputation().setEnabledForModels(null);
	}

	@Test public void testCddComputationEnabledForModelsMatchingBehavior() throws JsonProcessingException {

		Page<DocumentTreeResult> rootDocuments = prepareCddPostProcess();
		doReturn(rootDocuments.getContent().getFirst().getDocument())
			.when(objectMapper).valueToTree(any(DocumentV2.class));

		List<String> originalValue = dataServicesCoreProperties.getDocuments().getComputation().getEnabledForModels();
		try {
			// 1) Wildcard "*" => computation for all models
			dataServicesCoreProperties.getDocuments().getComputation().setEnabledForModels(List.of(DataServicesCoreProperties.MATCH_ALL));
			cddProjectionImplementation.postprocess(QueryRoot.builder()
				.targetDocumentModel(DocumentModelConstants.CONTRACT_CDM_MODEL)
				.build(), rootDocuments, queryContext);
			verify(kernelTestSupport.getKernelDocumentService(), Mockito.times(1))
				.computeDocument(any(DocumentV2.class), any(Locale.class));

			// 2) Other model model => no computation
			Mockito.reset(kernelTestSupport.getKernelDocumentService());
			dataServicesCoreProperties.getDocuments().getComputation().setEnabledForModels(List.of("SomeOtherModel"));
			cddProjectionImplementation.postprocess(QueryRoot.builder()
				.targetDocumentModel(DocumentModelConstants.CONTRACT_CDM_MODEL)
				.build(), rootDocuments, queryContext);
			verifyNoInteractions(kernelTestSupport.getKernelDocumentService());

			// 3) Empty property => no computation
			Mockito.reset(kernelTestSupport.getKernelDocumentService());
			dataServicesCoreProperties.getDocuments().getComputation().setEnabledForModels(List.of());
			cddProjectionImplementation.postprocess(QueryRoot.builder()
				.targetDocumentModel(DocumentModelConstants.CONTRACT_CDM_MODEL)
				.build(), rootDocuments, queryContext);
			verifyNoInteractions(kernelTestSupport.getKernelDocumentService());

			// 4) Null => no sompuation
			Mockito.reset(kernelTestSupport.getKernelDocumentService());
			dataServicesCoreProperties.getDocuments().getComputation().setEnabledForModels(null);
			cddProjectionImplementation.postprocess(QueryRoot.builder()
				.targetDocumentModel(DocumentModelConstants.CONTRACT_CDM_MODEL)
				.build(), rootDocuments, queryContext);
			verifyNoInteractions(kernelTestSupport.getKernelDocumentService());
		} finally {
			dataServicesCoreProperties.getDocuments().getComputation().setEnabledForModels(originalValue);
		}
	}

	@NotNull private Page<DocumentTreeResult> prepareCddPostProcess() throws JsonProcessingException {
		when(internalQueryAction.apply(anyList(), any(QueryRoot.class), eq(queryContext))).thenReturn(
			new PageImpl<>(List.of(
				DocumentTreeResult.builder()
					.docRef(new DocumentReference("BusinessPartner/1"))
					.type(DocumentTreeNodeType.CHILD)
					.document(JsonMapper.builder().build().readTree("""
						{
							"__meta": {
								"docRef": "BusinessPartnerLTD/1a28681a-bb0a-44ae-9da3-5ea9a7b47731",
								"creator": "admin",
								"modifier": "admin",
								"createdAt": "2025-03-28T10:41:17",
								"modifiedAt": "2025-03-28T10:41:17",
								"modelVersion": null,
								"modelReference": "BusinessPartnerLTD"
							},
							"BusinessPartnerRoot": {
								"Name": "Microoogle",
								"Industry": "Legal",
								"Employment": {
									"income": 9999
								},
								"SubtypeGroup": {
									"Type": "LLC"
								},
								"PersonOrEntity": "Legal Entity",
								"CustomerDiscount": "100%",
								"StartOfRelationship": "2022-08-01"
							}
						}
						"""))
					.relationshipModel("ContractBusinessPartner")
					.sourceRole("Contract")
					.sourceDocRef(new DocumentReference("Contract/1"))
					.targetRole("Partner")
					.targetDocRef(new DocumentReference("BusinessPartner/1"))
					.linkId("1")
					.backReference("/ContractBusinessPartner")
					.build(),

				DocumentTreeResult.builder()
					.docRef(new DocumentReference("BusinessPartner/2"))
					.type(DocumentTreeNodeType.CHILD)
					.document(JsonMapper.builder().build().readTree("""
						{
						    "__meta": {
						        "docRef": "BusinessPartnerLTD/7af624da-72a7-4f88-beaa-16f89661ff06",
						        "creator": "admin",
						        "modifier": "admin",
						        "createdAt": "2025-03-28T10:41:18",
						        "modifiedAt": "2025-03-28T10:41:18",
						        "modelVersion": null,
						        "modelReference": "BusinessPartnerLTD"
						    },
						    "BusinessPartnerRoot": {
						        "Name": "CraigsRainforest",
						        "Industry": "Commerce",
						        "Employment": {
						            "income": 3333
						        },
						        "SubtypeGroup": {
						            "Type": "KG"
						        },
						        "PersonOrEntity": "Legal Entity",
						        "CustomerDiscount": "80%",
						        "StartOfRelationship": "2019-08-06"
						    }
						 }
						"""))
					.relationshipModel("ContractCoInsuredPartner")
					.sourceRole("Contract")
					.sourceDocRef(new DocumentReference("Contract/1"))
					.targetRole("Partner")
					.targetDocRef(new DocumentReference("BusinessPartner/2"))
					.linkId("2")
					.backReference("/ContractCoInsuredPartner")
					.build(),

				DocumentTreeResult.builder()
					.docRef(new DocumentReference("BusinessPartner/3"))
					.type(DocumentTreeNodeType.CHILD)
					.document(JsonMapper.builder().build().readTree("""
						{
						    "__meta": {
						        "docRef": "BusinessPartnerLTD/d4776ae0-6b41-4c05-ae6c-bb755abf2134",
						        "creator": "admin",
						        "modifier": "admin",
						        "createdAt": "2025-03-28T10:41:18",
						        "modifiedAt": "2025-03-28T10:41:18",
						        "modelVersion": null,
						        "modelReference": "BusinessPartnerLTD"
						    },
						    "BusinessPartnerRoot": {
						        "Name": "Alphameta",
						        "Industry": "IT",
						        "Employment": {
						            "income": 555.25
						        },
						        "SubtypeGroup": {
						            "Type": "GmbH"
						        },
						        "PersonOrEntity": "Legal Entity",
						        "CustomerDiscount": "90%",
						        "StartOfRelationship": "2017-08-03"
						    }
						 }
						"""))
					.relationshipModel("ContractCoInsuredPartner")
					.sourceRole("Contract")
					.sourceDocRef(new DocumentReference("Contract/1"))
					.targetRole("Partner")
					.targetDocRef(new DocumentReference("BusinessPartner/3"))
					.linkId("3")
					.backReference("/ContractCoInsuredPartner")
					.build(),

				DocumentTreeResult.builder()
					.docRef(new DocumentReference("Address/1"))
					.type(DocumentTreeNodeType.CHILD)
					.document(JsonMapper.builder().build().readTree("""
						{
						    "__meta": {
						        "docRef": "Address/05c36862-da6a-4784-8735-cdb76f9a2313",
						        "creator": "admin",
						        "modifier": "admin",
						        "createdAt": "2025-03-28T10:41:18",
						        "modifiedAt": "2025-03-28T10:41:18",
						        "modelVersion": null,
						        "modelReference": "Address"
						    },
						    "AddressRoot": {
						        "City": "Nürnberg",
						        "Street": "Vordere Cramergasse",
						        "Country": "Germany",
						        "Location": "mgm - Nürnberg",
						        "PostCode": "90478",
						        "HouseNumber": "11"
						    }
						 }
						"""))
					.relationshipModel("PartnerPostalAddress")
					.sourceRole("Partner")
					.sourceDocRef(new DocumentReference("BusinessPartner/1"))
					.targetRole("Address")
					.targetDocRef(new DocumentReference("Address/1"))
					.linkId("4")
					.backReference("/ContractBusinessPartner/PartnerPostalAddress")
					.build(),

				DocumentTreeResult.builder()
					.docRef(new DocumentReference("Address/2"))
					.type(DocumentTreeNodeType.CHILD)
					.document(JsonMapper.builder().build().readTree("""
						{
							"__meta": {
								"docRef": "Address/36388f5c-3036-4d39-bbe5-828337a2316e",
								"creator": "admin",
								"modifier": "admin",
								"createdAt": "2025-03-28T10:41:18",
								"modifiedAt": "2025-03-28T10:41:18",
								"modelVersion": null,
								"modelReference": "Address"
							},
							"AddressRoot": {
								"City": "Praha 7",
								"Street": "Letenské náměstí",
								"Country": "Czech Republic",
								"Location": "mgm - Prague",
								"PostCode": "170 00",
								"HouseNumber": "4/157"
							}
						}
						"""))
					.relationshipModel("PartnerAddresses")
					.sourceRole("Partner")
					.sourceDocRef(new DocumentReference("BusinessPartner/1"))
					.targetRole("Address")
					.targetDocRef(new DocumentReference("Address/2"))
					.linkId("5")
					.backReference("/ContractBusinessPartner/PartnerAddresses")
					.build(),

				DocumentTreeResult.builder()
					.docRef(new DocumentReference("Address/3"))
					.type(DocumentTreeNodeType.CHILD)
					.document(JsonMapper.builder().build().readTree("""
						{
						    "__meta": {
						        "docRef": "Address/3a8f8803-3e27-459f-8f59-19ac4de45127",
						        "creator": "admin",
						        "modifier": "admin",
						        "createdAt": "2025-03-28T10:41:18",
						        "modifiedAt": "2025-03-28T10:41:18",
						        "modelVersion": null,
						        "modelReference": "Address"
						    },
						    "AddressRoot": {
						        "City": "Đà Nẵng",
						        "Street": "Pasteur",
						        "Country": "Vietnam",
						        "Location": "mgm - Vietnam",
						        "PostCode": "Hải Châu 1",
						        "HouseNumber": "7"
						    }
						 }
						"""))
					.relationshipModel("PartnerAddresses")
					.sourceRole("Partner")
					.sourceDocRef(new DocumentReference("BusinessPartner/1"))
					.targetRole("Address")
					.targetDocRef(new DocumentReference("Address/3"))
					.linkId("6")
					.backReference("/ContractBusinessPartner/PartnerAddresses")
					.build()

			))
		);
		@NonNull Page<DocumentTreeResult> rootDocuments = new PageImpl<>(List.of(DocumentTreeResult.builder()
			.docRef(new DocumentReference("Contract/1"))
			.type(DocumentTreeNodeType.ROOT)
			.document(JsonMapper.builder().build().readTree("""
				{
				    "__meta": {
				        "docRef": "Contract/3454f7df-9875-4a77-b940-4c44583d1b39",
				        "creator": "admin",
				        "modifier": "admin",
				        "createdAt": "2025-03-28T10:41:16",
				        "modifiedAt": "2025-03-28T10:41:16",
				        "modelVersion": null,
				        "modelReference": "Contract"
				    },
				    "ContractRoot": {
				        "Type": "Health",
				        "Valid": false,
				        "ChangeLog": [
				            {
				                "User": "Developer3",
				                "Number": 3,
				                "Changes": [
				                    {
				                        "Title": "Fix contract value",
				                        "Status": "reverted",
				                        "Details": "Contract value was incorrect"
				                    }
				                ],
				                "Description": "Description3",
				                "ChangeTimestamp": "2023-12-31"
				            },
				            {
				                "Changes": [
				                    {
				                        "Title": "Apply new conditions",
				                        "Status": "approved",
				                        "Details": "Missing few of the conditions"
				                    }
				                ],
				                "Description": "Change 1",
				                "ChangeTimestamp": "2023-03-15"
				            }
				        ],
				        "Liability": 10,
				        "MaxDiscount": "Jana has a 50% discount",
				        "ContractName": "Contract *",
				        "ContractValue": 5000,
				        "CostToCustomer": 2500,
				        "CostPerCoInsured": 2500,
				        "LengthOfContract": 10,
				        "NoOfCoInsuredCustomers": 5
				    }
				  }
				"""))
			.linkId("-1")
			.build()));
		QueryRoot query = QueryRoot.builder()
			.targetDocumentModel(DocumentModelConstants.CONTRACT_CDM_MODEL)
			.build();
		doReturn(query).when(queryContext).getOriginalQuery();
		@NonNull QueryPage<DocumentSpec> result = cddProjectionImplementation.postprocess(query, rootDocuments, queryContext);

		Consumer<RankedNotification> notifier =
			notification -> {throw new TestException("Got error during deserialization of document: %s".formatted(notification.getMessage()));};
		DocumentSpec documentSpec = result.getContent().getFirst();
		DocumentV2 docV2 = kernelTestSupport.getDocumentV2Serializer()
			.deserializeV2(new StringReader(documentSpec.getDocument()), DocumentModelConstants.CONTRACT_CDM_MODEL,
				kernelTestSupport.getDocumentDeserializationConfig(), notifier);

		DataServicesDocumentMetadata md =
			dataServicesDocumentMetadataExtractor.getMetadata(docV2);

		JSONAssert.assertEquals(
			String.format("""
					{
					  "ContractRoot" : {
					    "ContractName" : "Contract *",
					    "LengthOfContract" : 10,
					    "ContractValue" : 5000,
					    "Liability" : 10,
					    "CostToCustomer" : 2500,
					    "NoOfCoInsuredCustomers" : 5,
					    "MaxDiscount" : "Jana has a 50%% discount",
					    "CostPerCoInsured" : 2500,
					    "Valid" : false,
					    "Type" : "Health",
					    "ChangeLog" : [ {
					      "Number" : 3,
					      "ChangeTimestamp" : "2023-12-31",
					      "Description" : "Description3",
					      "User" : "Developer3",
					      "Changes" : [ {
					        "Title" : "Fix contract value",
					        "Details" : "Contract value was incorrect",
					        "Status" : "reverted"
					      } ]
					    }, {
					      "ChangeTimestamp" : "2023-03-15",
					      "Description" : "Change 1",
					      "Changes" : [ {
					        "Title" : "Apply new conditions",
					        "Details" : "Missing few of the conditions",
					        "Status" : "approved"
					      } ]
					    } ]
					  },
					  "ContractBusinessPartner" : {
					    "BusinessPartnerRoot" : {
					      "Name" : "Microoogle",
					      "Industry" : "Legal",
					      "StartOfRelationship" : "2022-08-01",
					      "CustomerDiscount" : "100%%",
					      "Employment" : {
					        "income" : 9999
					      },
					      "PersonOrEntity" : "Legal Entity"
					    },
					    "PartnerAddresses" : [ {
					      "AddressRoot" : {
					        "Location" : "mgm - Prague",
					        "Street" : "Letenské náměstí",
					        "HouseNumber" : "4/157",
					        "City" : "Praha 7",
					        "PostCode" : "170 00",
					        "Country" : "Czech Republic"
					      },
					      "__meta" : {
					        "docRef" : "Address/36388f5c-3036-4d39-bbe5-828337a2316e",
					        "modelReference" : "Address",
					        "creator" : "admin",
					        "createdAt" : "2025-03-28T10:41:18",
					        "modifier" : "admin",
					        "modifiedAt" : "2025-03-28T10:41:18"
					      }
					    }, {
					      "AddressRoot" : {
					        "Location" : "mgm - Vietnam",
					        "Street" : "Pasteur",
					        "HouseNumber" : "7",
					        "City" : "Đà Nẵng",
					        "PostCode" : "Hải Châu 1",
					        "Country" : "Vietnam"
					      },
					      "__meta" : {
					        "docRef" : "Address/3a8f8803-3e27-459f-8f59-19ac4de45127",
					        "modelReference" : "Address",
					        "creator" : "admin",
					        "createdAt" : "2025-03-28T10:41:18",
					        "modifier" : "admin",
					        "modifiedAt" : "2025-03-28T10:41:18"
					      }
					    } ],
					    "PartnerPostalAddress" : {
					      "AddressRoot" : {
					        "Location" : "mgm - Nürnberg",
					        "Street" : "Vordere Cramergasse",
					        "HouseNumber" : "11",
					        "City" : "Nürnberg",
					        "PostCode" : "90478",
					        "Country" : "Germany"
					      },
					      "__meta" : {
					        "docRef" : "Address/05c36862-da6a-4784-8735-cdb76f9a2313",
					        "modelReference" : "Address",
					        "creator" : "admin",
					        "createdAt" : "2025-03-28T10:41:18",
					        "modifier" : "admin",
					        "modifiedAt" : "2025-03-28T10:41:18"
					      }
					    },
					    "__meta" : {
					      "docRef" : "BusinessPartnerLTD/1a28681a-bb0a-44ae-9da3-5ea9a7b47731",
					      "modelReference" : "BusinessPartnerLTD",
					      "creator" : "admin",
					      "createdAt" : "2025-03-28T10:41:17",
					      "modifier" : "admin",
					      "modifiedAt" : "2025-03-28T10:41:17"
					    }
					  },
					  "ContractCoInsuredPartner" : [ {
					    "BusinessPartnerRoot" : {
					      "Name" : "CraigsRainforest",
					      "Industry" : "Commerce",
					      "StartOfRelationship" : "2019-08-06",
					      "CustomerDiscount" : "80%%",
					      "Employment" : {
					        "income" : 3333
					      },
					      "PersonOrEntity" : "Legal Entity"
					    },
					    "__meta" : {
					      "docRef" : "BusinessPartnerLTD/7af624da-72a7-4f88-beaa-16f89661ff06",
					      "modelReference" : "BusinessPartnerLTD",
					      "creator" : "admin",
					      "createdAt" : "2025-03-28T10:41:18",
					      "modifier" : "admin",
					      "modifiedAt" : "2025-03-28T10:41:18"
					    }
					  }, {
					    "BusinessPartnerRoot" : {
					      "Name" : "Alphameta",
					      "Industry" : "IT",
					      "StartOfRelationship" : "2017-08-03",
					      "CustomerDiscount" : "90%%",
					      "Employment" : {
					        "income" : 555.25
					      },
					      "PersonOrEntity" : "Legal Entity"
					    },
					    "__meta" : {
					      "docRef" : "BusinessPartnerLTD/d4776ae0-6b41-4c05-ae6c-bb755abf2134",
					      "modelReference" : "BusinessPartnerLTD",
					      "creator" : "admin",
					      "createdAt" : "2025-03-28T10:41:18",
					      "modifier" : "admin",
					      "modifiedAt" : "2025-03-28T10:41:18"
					    }
					  } ],
					  "__meta" : {
					    "docRef" : "Contract/3454f7df-9875-4a77-b940-4c44583d1b39",
					    "modelReference" : "Contract",
					    "creator" : "admin",
					    "createdAt" : "%s",
					    "modifier" : "admin",
					    "modifiedAt" : "%s"
					  }
					}
					""",
				getFormattedDate(md.getCreatedAt()),
				getFormattedDate(md.getModifiedAt())
			),
			documentSpec.getDocument(),
			false
		);
		return rootDocuments;
	}

	private String getFormattedDate(Instant instant) {
		return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
			.withZone(ZoneOffset.UTC)
			.format(instant);
	}
}
