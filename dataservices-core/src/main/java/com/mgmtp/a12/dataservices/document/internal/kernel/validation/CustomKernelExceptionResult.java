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
package com.mgmtp.a12.dataservices.document.internal.kernel.validation;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.mgmtp.a12.kernel.md.document.api.IEntityInstance;
import com.mgmtp.a12.kernel.md.document.apiV2.PartiallyKnownDocumentMultiPointer;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentValidationResult;
import com.mgmtp.a12.kernel.md.rt.api.IMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Handle not expected exception from Kernel
 */
// TODO A12S-3793: IDocumentValidationResult is for usageOnly and should be removed in the future.
@Slf4j
@RequiredArgsConstructor
public class CustomKernelExceptionResult implements IDocumentValidationResult {
	private final Exception exception;

	@Override public boolean noErrorOccurred() {
		return false;
	}

	@Override public List<IMessage> getMessages() {
		return Collections.singletonList(new CustomKernelMessage(exception.getMessage()));
	}

	public static class CustomKernelMessage implements IMessage {
		private final String message;

		public CustomKernelMessage(String message) {
			this.message = message;
		}

		@Override public Optional<String> getRulePath() {
			return Optional.empty();
		}

		@Override public MessageType getMessageType() {
			return null;
		}

		@Override public Severity getSeverity() {
			return null;
		}

		@Override public String getErrorText() {
			return message;
		}

		@Override public IEntityInstance getEntityInstance() {
			return null;
		}

		@Override public PartiallyKnownDocumentMultiPointer getErrorFieldPointer() {
			return null;
		}

		@Override public Collection<IEntityInstance> getReferencedFields() {
			return List.of();
		}

		@Override public Collection<PartiallyKnownDocumentMultiPointer> getReferencedFieldsPointers() {
			return List.of();
		}

		@Override public Collection<IEntityInstance> getRefOmissionErrorResponsible() {
			return List.of();
		}

		@Override public Collection<PartiallyKnownDocumentMultiPointer> getRefOmissionErrorResponsiblePointers() {
			return List.of();
		}

		@Override public String getErrorCode() {
			return null;
		}

		@Override public String toString() {
			return String.format(
				"ValidateDocumentError{path: '%s', type: '%s', severity: '%s', code: '%s', entity: '%s', message: '%s', fields: %s}",
				getRulePath(), getMessageType(), getSeverity(), getErrorCode(), getEntityInstance(), getErrorText(), getReferencedFields());
		}
	}
}
