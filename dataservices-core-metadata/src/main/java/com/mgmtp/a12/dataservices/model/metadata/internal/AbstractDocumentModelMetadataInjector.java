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
package com.mgmtp.a12.dataservices.model.metadata.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.mgmtp.a12.dataservices.exception.ExceptionCodes;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.experimental.ListIProblemReporter;
import com.mgmtp.a12.dataservices.model.metadata.DocumentMetadataConstants;
import com.mgmtp.a12.dataservices.model.metadata.DocumentModelMetadataInjector;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModelContent;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelCopyService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.DocumentModelJoiningService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.JoiningType;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.item.DocumentModelJoiningItem;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.item.JoiningItem;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.item.SelectionJoiningItem;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.model.metadata.DocumentModelMetadataInjectorFactory.META_GROUP_NAME_PATTERN;

@Slf4j
@RequiredArgsConstructor
public class AbstractDocumentModelMetadataInjector implements DocumentModelMetadataInjector {

	public static final String SELECTION_MODEL_V_1_MODEL_NAME = "SelectionModel_V1";
	public static final String UNSELECT = "Unselected";
	public static final String SELECT = "Selected";

	@Getter(AccessLevel.PROTECTED) private final IDocumentModel documentModel;
	private final DocumentModelJoiningService documentModelJoiningService;
	private final DocumentModelService documentModelService;
	@Getter(AccessLevel.PROTECTED) private final Locale locale;

	@Override @NonNull public IDocumentModel getDocumentModelWithMetadata(IDocumentModel documentMetadataModel,
		IDocumentModel attachmentMetadataModel) {

		DocumentModel originalDocumentModel = getInternalDocumentModelCopy();
		Optional<DocumentModel> documentMetadataModelInternal = Optional.ofNullable(documentMetadataModel)
			.map(documentModelService::convertFromExternal);

		// Attachments are not supported yet. This is preparation for it, but disabled for now.
		attachmentMetadataModel = null;
		Optional<DocumentModel> attachmentMetadataModelInternal = Optional.ofNullable(attachmentMetadataModel)
			.map(documentModelService::convertFromExternal);

		List<JoiningItem> joiningItems = new ArrayList<>();
		removeMetadata(originalDocumentModel);
		documentMetadataModelInternal.ifPresent(m -> customizeJoiningItems(joiningItems, m));
		attachmentMetadataModelInternal.ifPresent(a -> addAttachmentDecoration(joiningItems, originalDocumentModel, a));
		DocumentModel enrichedModel = doKernelJoining(originalDocumentModel, getLocale(), joiningItems);

		getDocumentMetadataGroup(documentMetadataModelInternal).ifPresent(documentMetadataGroup -> addMetadata(documentMetadataGroup, enrichedModel));

		return documentModelService.convertToExternal(enrichedModel);
	}

	@Override public IDocumentModel getDocumentModelWithoutMetadata() {
		return removeMetadata(getInternalDocumentModelCopy());
	}

	/**
	 * Applies specific logic to metadata for a particular type of document.
	 * It's intended for extending default logic.
	 * It's used for adding CDM sub-document metadata, e.g.
	 *
	 * @param documentMetadataGroup Metadata group from DocumentMetadataModel.
	 * @param enrichedModel Model to be enriched by an extra metadata.
	 */
	protected void addMetadata(Group documentMetadataGroup, DocumentModel enrichedModel) {
		// By default, do nothing. In children here could come extra action to add additional metadata elements.
	}

	/**
	 * Used to extend kernel joining part. It's used to add regular document root metadata, e.g.
	 *
	 * @param joiningItems to be extended by extra joining rules.
	 * @param documentMetadataModelInternal document metadata model.
	 */
	protected void customizeJoiningItems(List<JoiningItem> joiningItems, DocumentModel documentMetadataModelInternal) {
		// By default, do nothing. In children here could come extra action to add additional joining items.
	}

	/**
	 * @return copy of the document model in Kernel's internal form.
	 */
	protected DocumentModel getInternalDocumentModelCopy() {
		return DocumentModelCopyService.copy(documentModelService.convertFromExternal(getDocumentModel()));
	}

	/**
	 * Creates a copy of the given {@link Group} and adds the parent group ID as a prefix to the ID of all elements within the copied group.
	 *
	 * @param originalGroup The {@link Group} to be copied.
	 * @param parentGroup The parent {@link Group} whose ID will be added as a prefix to the IDs of all elements in the copied group.
	 * @return The cloned {@link Group} with updated element IDs.
	 */
	@NonNull protected Group copyGroupAndAddParentIdPrefixToAllElements(Group originalGroup, Group parentGroup) {
		Group g = DocumentModelCopyService.copy(originalGroup);
		UniqueFieldIdVisitor visitor = new UniqueFieldIdVisitor(parentGroup.getId());
		new DocumentModelWalker().acceptElements(List.of(g), visitor);
		return g;
	}

	/**
	 * Shortcut to call {@link DocumentModelWalker} and return the visitor in the chain. Useful if the visitor collects some data, which can be retrieved then.
	 *
	 * @param documentModel document model to walk through.
	 * @param visitor visitor to apply.
	 * @param <T> type of the visitor.
	 * @return the visitor.
	 */

	protected static <T extends DocumentModelVisitor> T getVisitorAfterWalk(DocumentModel documentModel, T visitor) {
		new DocumentModelWalker().acceptDocumentModel(documentModel, visitor);
		return visitor;
	}

	private IDocumentModel removeMetadata(DocumentModel documentModel) {
		MetadataRemovingVisitor visitor = new MetadataRemovingVisitor();
		getVisitorAfterWalk(documentModel, visitor);
		log.debug("Groups to remove:\n%s".formatted(visitor.getGroupsToRemove().stream().map(Element::getName).collect(Collectors.joining("\n"))));
		visitor.getGroupsToRemove().forEach(g -> g.getParent().removeElement(g));
		return documentModelService.convertToExternal(documentModel);
	}

	private void addAttachmentDecoration(List<JoiningItem> joiningItems, DocumentModel originalDocumentModel, DocumentModel attachmentMetadataModelInternal) {
		joiningItems.add(new SelectionJoiningItem(selectAllAttachments(originalDocumentModel), JoiningType.SELECTION_DECORATION));
		joiningItems.add(new DocumentModelJoiningItem(attachmentMetadataModelInternal, JoiningType.DECORATION_MODEL_GROUP));
	}

	/**
	 * Call kernel to apply all {@link JoiningItem}s.
	 *
	 * @param documentModel original document model.
	 * @param locale locale for kernel.
	 * @param joiningItems list of {@link JoiningItem}s.
	 * @return copy of the original document model enriched by metadata from joiningItems.
	 */
	private DocumentModel doKernelJoining(DocumentModel documentModel, Locale locale, List<JoiningItem> joiningItems) {
		ListIProblemReporter pr = new ListIProblemReporter();
		DocumentModel enrichedModel = documentModelJoiningService
			.join(documentModel, joiningItems, true, locale, pr)
			.orElse(documentModel);
		pr.validate(ExceptionCodes.DOCUMENT_MODEL_JOINING_EXCEPTION_CODE, ExceptionKeys.DOCUMENT_MODEL_JOINING_ERROR_KEY, "Error while joining document models");
		return enrichedModel;
	}

	/**
	 * Crawl through the document model and find paths to all attachments.
	 *
	 * @param documentModel original document model.
	 * @return Selection document to select all attachment fields.
	 */
	private @NonNull DocumentV2 selectAllAttachments(DocumentModel documentModel) {
		return getVisitorAfterWalk(documentModel, new AttachmentMetadataAddingVisitor()).getAttachmentSelection();
	}

	@NonNull private static Optional<Group> getDocumentMetadataGroup(Optional<DocumentModel> documentMetadataModelInternal1) {
		return documentMetadataModelInternal1
			.map(DocumentModel::getContent)
			.map(DocumentModelContent::getModelRoot)
			.map(Group::getElements)
			.stream()
			.flatMap(Collection::stream)
			.filter(Group.class::isInstance)
			.filter(g -> DocumentMetadataConstants.DOCUMENT_METADATA_GROUP_NAME.equals(g.getName()))
			.map(Group.class::cast)
			.findAny();
	}

	/**
	 * {@link DocumentModelVisitor} decoration attachment fields by metadata.
	 */
	@Getter
	private class AttachmentMetadataAddingVisitor extends DocumentModelVisitor {
		private DocumentV2 attachmentSelection = prepareSelectionDocument(UNSELECT, UNSELECT);
		private final AtomicInteger groupCounter = new AtomicInteger(1);

		// TODO [A12S-6276] : This method is just for testing. Must be removed in production.
		@Override public VisitProcess visitGroup(Group group) {
			if (isAttachmentElement(group)) {
				attachmentSelection =
					attachmentSelection.withFieldValue(DocumentPointer.of("/Data/Selected[%d]/path".formatted(groupCounter.getAndIncrement())),
						documentModelService.getPath(group));
			}
			return super.visitGroup(group);
		}

		@Override public VisitProcess visitField(Field field) {
			if (isAttachmentElement(field)) {
				attachmentSelection =
					attachmentSelection.withFieldValue(DocumentPointer.of("/Data/Selected[%d]/path".formatted(groupCounter.getAndIncrement())),
						documentModelService.getPath(field));
			}
			return super.visitField(field);
		}

		private boolean isAttachmentElement(Element element) {
			return "Attachment".equals(element.getName());
		}

		private static DocumentV2 prepareSelectionDocument(String dataDefault, String validationDefault) {
			@NonNull DocumentV2 metadataDeselection = DocumentV2.empty(SELECTION_MODEL_V_1_MODEL_NAME);
			metadataDeselection = metadataDeselection.withFieldValue(DocumentPointer.of("/Validation/Default"), validationDefault);
			metadataDeselection = metadataDeselection.withFieldValue(DocumentPointer.of("/Data/Default"), dataDefault);
			return metadataDeselection;
		}
	}

	@Getter
	private static class MetadataRemovingVisitor extends DocumentModelVisitor {

		private final Set<Group> groupsToRemove = new HashSet<>();

		@Override public VisitProcess visitGroup(Group group) {

			if (META_GROUP_NAME_PATTERN.matcher(group.getName()).matches()) {
				groupsToRemove.add(group);
				return VisitProcess.CONTINUE_BUT_DONT_GO_DEEPER;
			}
			return super.visitGroup(group);
		}
	}

	@RequiredArgsConstructor
	private static class UniqueFieldIdVisitor extends DocumentModelVisitor {
		private final String metaGroupId;

		@Override public VisitProcess visitElement(Element element) {
			element.setId(metaGroupId + "_" + element.getId());
			return super.visitElement(element);
		}
	}
}
