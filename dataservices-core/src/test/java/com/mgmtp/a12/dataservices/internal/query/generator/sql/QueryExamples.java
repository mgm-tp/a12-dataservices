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
package com.mgmtp.a12.dataservices.internal.query.generator.sql;

import org.json.JSONObject;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgmtp.a12.dataservices.query.topology.QueryLink;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.dataservices.query.constraint.logical.AndOperator;
import com.mgmtp.a12.dataservices.query.constraint.logical.NotOperator;
import com.mgmtp.a12.dataservices.query.constraint.logical.OrOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.constraint.matching.HasOperator;
import com.mgmtp.a12.dataservices.query.constraint.range.DoubleRangeOperator;

public class QueryExamples {

	@Test
	public void listDocumentsFilter() throws JsonProcessingException {
		// Liability == 3
		QueryRoot oneFilter = QueryRoot.builder()
			.targetDocumentModel("Contract")
			.constraint(
				ExactMatchOperator.builder()
					.field("/ContractRoot/Liability")
					.value("3")
					.build())
			.build();

		// Contracts where Liability == 3 && (ContractValue BETWEEN 10000.0 AND 13000.0)
		QueryRoot twoFilters = QueryRoot.builder()
			.targetDocumentModel("Contract")
			.constraint(AndOperator
				.builder()
				.operand(ExactMatchOperator
					.builder()
					.field("/ContractRoot/Liability")
					.value("3")
					.build())
				.operand(DoubleRangeOperator
					.builder()
					.field("/ContractRoot/ContractValue")
					.from(10000.0)
					.to(13000.0)
					.build())
				.build()
			)
			.build();

		// Contracts where Liability == 2 || (Liability != 3 && (ContractValue BETWEEN 10000.0 AND 13000.0))
		QueryRoot richQuery = QueryRoot.builder()
			.targetDocumentModel("Contract")
			.constraint(OrOperator
				.builder()
				.operand(ExactMatchOperator
					.builder()
					.field("/ContractRoot/Liability")
					.value("2")
					.build())
				.operand(AndOperator
					.builder()
					.operand(NotOperator
						.builder()
						.operand(ExactMatchOperator
							.builder()
							.field("/ContractRoot/Liability")
							.value("3")
							.build())
						.build())
					.operand(DoubleRangeOperator
						.builder()
						.field("/ContractRoot/ContractValue")
						.from(10000.0)
						.to(13000.0)
						.build())
					.build())
				.build())
			.build();

		System.out.println("Contracts where Liability == 3");
		System.out.println(prettySerialize(oneFilter));
		System.out.println("Contracts where Liability == 3 && (ContractValue BETWEEN 10000.0 AND 13000.0)");
		System.out.println(prettySerialize(twoFilters));
		System.out.println(
			"Contracts where Liability == 2 || (Liability != 3 && (ContractValue BETWEEN 10000.0 AND 13000.0))");
		System.out.println(prettySerialize(richQuery));
	}

	@Test
	public void listDocumentsWhereConditionsOnLinks() throws JsonProcessingException {
		QueryRoot contractsWithBPWithPostalAddressInBerlin = QueryRoot
			.builder()
			.targetDocumentModel("Contract")
			.constraint(HasOperator
				.builder()
				.relationshipModel("PolicyHolder")
				.targetRole("holder")
				.constraint(HasOperator
					.builder()
					.relationshipModel("PostalAddress")
					.targetRole("address")
					.constraint(ExactMatchOperator
						.builder()
						.field("AddressRoot/City")
						.value("Berlin")
						.build())
					.build())
				.build())
			.build();

		QueryRoot contractsWithBPWithPostalAddressNotInBerlin = QueryRoot
			.builder()
			.targetDocumentModel("Contract")
			.constraint(HasOperator
				.builder()
				.relationshipModel("PolicyHolder")
				.targetRole("holder")
				.constraint(HasOperator
					.builder()
					.relationshipModel("PostalAddress")
					.targetRole("address")
					.constraint(NotOperator
						.builder()
						.operand(ExactMatchOperator
							.builder()
							.field("AddressRoot/City")
							.value("Berlin")
							.build())
						.build())
					.build())
				.build())
			.build();

		QueryRoot contractsWithBPWithoutPostalAddress = QueryRoot
			.builder()
			.targetDocumentModel("Contract")
			.constraint(HasOperator
				.builder()
				.relationshipModel("PolicyHolder")
				.targetRole("holder")
				.constraint(NotOperator
					.builder()
					.operand(HasOperator
						.builder()
						.relationshipModel("PostalAddress")
						.targetRole("address")
						.build())
					.build())
				.build())
			.build();

		System.out.println("Contracts Only where the PolicyHolder has an address in Berlin");
		System.out.println(prettySerialize(contractsWithBPWithPostalAddressInBerlin));
		System.out.println("Contracts Only where the PolicyHolder has an address not in Berlin");
		System.out.println(prettySerialize(contractsWithBPWithPostalAddressNotInBerlin));
		System.out.println("Contracts Only where the PolichyHolder has no Postal Address");
		System.out.println(prettySerialize(contractsWithBPWithoutPostalAddress));
	}

	@Test
	public void listCandidates() throws JsonProcessingException {
		QueryRoot policyHolderCandidatesWithoutContracts = QueryRoot
			.builder()
			.targetDocumentModel("BusinessPartner")
			.constraint(AndOperator
				.builder()
				.operand(NotOperator
					.builder()
					.operand(HasOperator
						.builder()
						.relationshipModel("PolicyHolder")
						.targetRole("contract")
						.build())
					.build())
				.operand(HasOperator
					.builder()
					.relationshipModel("PostalAddress")
					.targetRole("address")
					.build())
				.build())
			.build();

		// meta filter notation is missing. Do we need a separate operator ?
		QueryRoot policyHolderCandidatesForContract324 =
			QueryRoot
				.builder()
				.targetDocumentModel("BusinessPartner")
				.constraint(AndOperator
					.builder()
					.operand(NotOperator
						.builder()
						.operand(HasOperator
							.builder()
							.relationshipModel("PolicyHolder")
							.targetRole("contract")
							.constraint(ExactMatchOperator
								.builder()
								.field("__docRef__")
								.value("Contract/324")
								.build())
							.build())
						.build())
					.operand(HasOperator
						.builder()
						.relationshipModel("PostalAddress")
						.targetRole("address")
						.build())
					.build())
				.build();

		QueryRoot policyHolderCandidatesForContract324WithAddressInBerlin = QueryRoot
			.builder()
			.targetDocumentModel("BusinessPartner")
			.constraint(AndOperator
				.builder()
				.operand(NotOperator
					.builder()
					.operand(HasOperator
						.builder()
						.relationshipModel("PolicyHolder")
						.targetRole("contract")
						.constraint(ExactMatchOperator
							.builder()
							.field("__docRef__")
							.value("Contract/324")
							.build())
						.build())
					.build())
				.operand(HasOperator
					.builder()
					.relationshipModel("PostalAddress")
					.targetRole("address")
					.constraint(ExactMatchOperator
						.builder()
						.field("AddressRoot/City")
						.value("Berlin")
						.build())
					.build())
				.build())
			.build();

		System.out.println("PolicyHolder Candidates without contracts with addresses for any contract");
		System.out.println(prettySerialize(policyHolderCandidatesWithoutContracts));
		System.out.println("PolicyHolder Candidates for Contract/324 with addresses");
		System.out.println(prettySerialize(policyHolderCandidatesForContract324));
		System.out.println("PolicyHolder Candidates for Contract/324 with addresses in Berlin");
		System.out.println(prettySerialize(policyHolderCandidatesForContract324WithAddressInBerlin));
	}

	@Test
	public void listLinks() throws JsonProcessingException {
		QueryRoot listBusinessPartnerLinksOfContract324 = QueryRoot
			.builder()
			.targetDocumentModel("BusinessPartner")
			.constraint(HasOperator
				.builder()
				.relationshipModel("Co-Insured")
				.targetRole("contract")
				.constraint(ExactMatchOperator
					.builder()
					.field("__docRef__")
					.value("Contract/324")
					.build())
				.build())
			.build();

		QueryRoot listBusinessPartnerLinksOfContract324InITIndustry = QueryRoot
			.builder()
			.targetDocumentModel("BusinessPartner")
			.constraint(AndOperator
				.builder()
				.operand(HasOperator
					.builder()
					.relationshipModel("Co-Insured")
					.targetRole("contract")
					.constraint(ExactMatchOperator
						.builder()
						.field("__docRef__")
						.value("Contract/324")
						.build())
					.build())
				.operand(ExactMatchOperator
					.builder()
					.field("BusinessPartnerRoot/Industry")
					.value("IT")
					.build())
				.build())
			.build();

		// Validation of business partner done by has operator
		QueryRoot reliableBusinessPartnersOfContract324 = QueryRoot
			.builder()
			.targetDocumentModel("BusinessPartner")
			.constraint(AndOperator
				.builder()
				.operand(HasOperator
					.builder()
					.relationshipModel("Co-Insured")
					.targetRole("contract")
					.constraint(ExactMatchOperator
						.builder()
						.field("__docRef__")
						.value("Contract/324")
						.build())
					.build())
				.operand(HasOperator
					.builder()
					.relationshipModel("PartnerValidation")
					.targetRole("validation")
					.constraint(ExactMatchOperator
						.builder()
						.field("ValidationRoot/Status")
						.value("Reliable")
						.build())
					.build())
				.build())
			.build();

		System.out.println("Co-insured business partners of Contract/324");
		System.out.println(prettySerialize(listBusinessPartnerLinksOfContract324));
		System.out.println("IT Industry Co-insured business partners of Contract/324");
		System.out.println(prettySerialize(listBusinessPartnerLinksOfContract324InITIndustry));
		System.out.println("Reliable Co-insured business partners of Contract/324");
		System.out.println(prettySerialize(reliableBusinessPartnersOfContract324));
	}

	@Test
	public void listCDDs() throws JsonProcessingException {
		QueryRoot listBusinessPartnerLinksOfContract324 = QueryRoot
			.builder()
			.targetDocumentModel("BusinessPartner")
			.link(QueryLink
				.builder()
				.relationshipModel("ContractCoInsuredPartner")
				.targetRole("Contract")
				.build())
			.constraint(HasOperator
				.builder()
				.relationshipModel("ContractCoInsuredPartner")
				.targetRole("contract")
				.constraint(ExactMatchOperator
					.builder()
					.field("__docRef__")
					.value("Contract/324")
					.build())
				.build())
			.build();

		QueryRoot listCdd324 = QueryRoot
			.builder()
			.targetDocumentModel("Contract")
			.link(QueryLink
				.builder()
				.relationshipModel("ContractCoInsuredPartner")
				.link(QueryLink
					.builder()
					.relationshipModel("PostalAddress")
					.targetRole("Address")
					.build())
				.link(QueryLink
					.builder()
					.relationshipModel("PermanentAddress")
					.targetRole("Address")
					.build())
				.build())
			.link(QueryLink
				.builder()
				.relationshipModel("PolicyHolder")
				.targetRole("PolicyHolder")
				.constraint(ExactMatchOperator
					.builder()
					.field("/BusinessPartner/Industry")
					.value("IT")
					.build())
				.build())
			.constraint(ExactMatchOperator
				.builder()
				.field("__docRef__")
				.value("Contract/324")
				.build())
			.build();

		System.out.println("Co-insured business partners of Contract/324");
		System.out.println(prettySerialize(listBusinessPartnerLinksOfContract324));
		System.out.println("LIST_CDDS of 324");
		System.out.println(prettySerialize(listCdd324));
	}

	private String prettySerialize(QueryRoot query) throws JsonProcessingException {
		return new JSONObject(new ObjectMapper().writeValueAsString(query)).toString(4);
	}
}
