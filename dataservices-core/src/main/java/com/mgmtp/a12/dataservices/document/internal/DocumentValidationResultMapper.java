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
package com.mgmtp.a12.dataservices.document.internal;

import java.util.List;
import java.util.Optional;

import com.mgmtp.a12.dataservices.document.DocumentValidationResult;
import com.mgmtp.a12.kernel.md.document.apiV2.PartiallyKnownDocumentMultiPointer;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentValidationResult;
import com.mgmtp.a12.kernel.md.rt.api.IMessage;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DocumentValidationResultMapper {

	public static List<DocumentValidationResult> toDocumentValidationResults(IDocumentValidationResult documentValidationResult) {
		return documentValidationResult.getMessages().stream()
			.map(DocumentValidationResultMapper::toDocumentValidationResult)
			.toList();
	}

	public static DocumentValidationResult toDocumentValidationResult(IMessage message) {

		Optional<IMessage> messageOpt = Optional.of(message);
		DocumentValidationResult.DocumentValidationResultBuilder bld = DocumentValidationResult.builder();
		messageOpt
			.map(IMessage::getErrorText)
			.ifPresent(bld::errorText);
		messageOpt
			.map(IMessage::getErrorCode)
			.ifPresent(bld::errorCode);
		messageOpt
			.map(IMessage::getMessageType)
			.map(String::valueOf)
			.ifPresent(bld::messageType);
		messageOpt
			.flatMap(IMessage::getRulePath)
			.ifPresent(bld::rulePath);
		messageOpt
			.map(IMessage::getReferencedFieldsPointers)
			.map(p -> p.stream()
				.map(PartiallyKnownDocumentMultiPointer::fullName)
				.toList())
			.ifPresent(bld::referencedFieldsPointers);
		messageOpt
			.map(IMessage::getSeverity)
			.map(Enum::name)
			.ifPresent(bld::severityType);
		messageOpt
			.map(IMessage::getErrorFieldPointer)
			.map(PartiallyKnownDocumentMultiPointer::fullName)
			.ifPresent(bld::errorFieldPointer);
		messageOpt
			.map(IMessage::getRefOmissionErrorResponsiblePointers)
			.map(x -> x.stream()
				.map(PartiallyKnownDocumentMultiPointer::fullName)
				.toList())
			.ifPresent(bld::refOmissionErrorResponsiblePointers);
		return bld.build();
	}

}
