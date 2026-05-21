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
package com.mgmtp.a12.dataservices.relationship.migrator.model.v1;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.mgmtp.a12.dataservices.relationship.migrator.model.IRelationshipModelMigrator;
import com.mgmtp.a12.dataservices.relationship.migrator.model.v1.RelationshipModelV1.DisplayLabelV1;
import com.mgmtp.a12.dataservices.relationship.migrator.model.v1.RelationshipModelV1.EntityCharacteristicsV1;
import com.mgmtp.a12.dataservices.relationship.migrator.model.v2.RelationshipModelMigratorV2ToV3;
import com.mgmtp.a12.dataservices.relationship.migrator.model.v2.RelationshipModelV2;
import com.mgmtp.a12.dataservices.relationship.migrator.model.v2.RelationshipModelV2.RelationshipModelContentV2;
import com.mgmtp.a12.dataservices.relationship.migrator.model.v2.RelationshipModelV2.RelationshipModelContentV2.DisplayLabelV2;
import com.mgmtp.a12.dataservices.relationship.migrator.model.v2.RelationshipModelV2.RelationshipModelContentV2.EntityCharacteristicsV2;
import com.mgmtp.a12.dataservices.relationship.migrator.model.v2.RelationshipModelV2.RelationshipModelHeaderV2;
import com.mgmtp.a12.dataservices.relationship.migrator.model.v2.RelationshipModelV2.RelationshipModelHeaderV2.LabelV2;
import com.mgmtp.a12.dataservices.relationship.migrator.model.v2.RelationshipModelV2.RelationshipModelHeaderV2.LocaleCodeV2;
import com.mgmtp.a12.dataservices.relationship.migrator.model.v3.RelationshipModelV3;

import lombok.NonNull;

import static com.mgmtp.a12.dataservices.model.ModelConstants.DOCUMENT_MODEL_TYPE;

/**
 * @deprecated relationship model migration moved to the npm package
 *
 */
@Deprecated(since = "38.1.0", forRemoval = true)
public class RelationshipModelMigratorV1ToV3 implements IRelationshipModelMigrator<RelationshipModelWrapperV1, RelationshipModelV3> {
	private final String roles;
	private final Set<String> locales;

	/**
	 * Creates a migrator for V1 relationship models to V3, using provided roles and locales for enrichment.
	 *
	 * @param roles Comma-separated list of roles to include as annotations; may be null to use defaults.
	 * @param locales Default locales to use if none can be derived from labels; must not be null.
	 */
	public RelationshipModelMigratorV1ToV3(String roles, Set<String> locales) {
		this.roles = roles;
		this.locales = locales;
	}

	@Override public String getModelName(RelationshipModelWrapperV1 source) {
		return source.getRelationshipModel().getName();
	}

	@Override public String getVersion(RelationshipModelWrapperV1 source) {
		return source.getRelationshipModel().getMetaInformation().getVersion();
	}

	@Override public RelationshipModelV3 migrateModel(RelationshipModelWrapperV1 source) {
		RelationshipModelV1 sourceRelationshipModel = source.getRelationshipModel();
		RelationshipModelV2 relationshipModelV2 = new RelationshipModelV2(convertHeader(sourceRelationshipModel), convertContent(sourceRelationshipModel));
		return new RelationshipModelMigratorV2ToV3().migrateModel(relationshipModelV2);
	}

	@NonNull private RelationshipModelContentV2 convertContent(RelationshipModelV1 sourceRelationshipModel) {
		return new RelationshipModelContentV2(sourceRelationshipModel.getAssociationType(), sourceRelationshipModel.getLinkDocumentModel(),
			sourceRelationshipModel.getDuplicatesAllowed(), sourceRelationshipModel.getStorage(), sourceRelationshipModel.getEmbeddedGroupPath(),
			convertDisplayLabels(sourceRelationshipModel.getDisplayLabel()), convertEntityCharacteristics(sourceRelationshipModel.getEntityCharacteristics()));
	}

	@NonNull private RelationshipModelHeaderV2 convertHeader(RelationshipModelV1 sourceRelationshipModel) {
		return new RelationshipModelHeaderV2(sourceRelationshipModel.getName(), convertLabels(sourceRelationshipModel.getDisplayLabel()),
			convertAnnotations(roles), convertLocales(sourceRelationshipModel, locales), convertModelReferences(sourceRelationshipModel));
	}

	@Override public Class<RelationshipModelWrapperV1> getSourceType() {
		return RelationshipModelWrapperV1.class;
	}

	private List<LocaleCodeV2> convertLocales(RelationshipModelV1 relationshipModelElement, Set<String> defaultLocales) {
		Set<String> locales = Stream.concat(
				relationshipModelElement.getDisplayLabel().stream(),
				relationshipModelElement.getEntityCharacteristics().stream()
					.flatMap(ec -> ec.getDisplayLabel().stream()))
			.map(DisplayLabelV1::getLanguage)
			.collect(Collectors.toSet());

		if (locales.isEmpty()) {
			locales = defaultLocales;
		}

		return locales.stream()
			.map(LocaleCodeV2::new)
			.collect(Collectors.toList());
	}

	private List<LabelV2> convertLabels(List<DisplayLabelV1> displayLabel) {
		return Optional.ofNullable(displayLabel).stream()
			.flatMap(Collection::stream)
			.map(e -> new LabelV2(e.getLanguage(), e.getValue()))
			.collect(Collectors.toList());
	}

	private List<DisplayLabelV2> convertDisplayLabels(List<DisplayLabelV1> displayLabel) {
		return displayLabel.stream()
			.map(l -> new DisplayLabelV2(l.getLanguage(), l.getValue()))
			.collect(Collectors.toList());
	}

	private List<EntityCharacteristicsV2> convertEntityCharacteristics(List<EntityCharacteristicsV1> entityCharacteristics) {
		return entityCharacteristics.stream()
			.map(e -> new EntityCharacteristicsV2(e.getRole(), e.getDocumentModel(), e.getOrdered(), e.getNavigable(), e.getLinkConstraints(),
				e.getCandidateConstraints(), e.getDisplayLabel()))
			.collect(Collectors.toList());
	}

	private List<RelationshipModelHeaderV2.ModelReferenceV2> convertModelReferences(RelationshipModelV1 relationshipModel) {
		List<RelationshipModelHeaderV2.ModelReferenceV2> modelReferencesList = Optional.ofNullable(relationshipModel.getEntityCharacteristics()).stream()
			.flatMap(Collection::stream)
			.map(e -> new RelationshipModelHeaderV2.ModelReferenceV2("Document model", DOCUMENT_MODEL_TYPE, e.getRole(), e.getDocumentModel()))
			.collect(Collectors.toList());

		String linkDocumentModel = relationshipModel.getLinkDocumentModel();
		if (StringUtils.isNotEmpty(linkDocumentModel) && !modelReferencesList.isEmpty()) {
			modelReferencesList.add(
				new RelationshipModelHeaderV2.ModelReferenceV2("Link Document model", DOCUMENT_MODEL_TYPE, "Link Model", linkDocumentModel));
		}

		return modelReferencesList;
	}

	private List<RelationshipModelHeaderV2.AnnotationV2> convertAnnotations(String roles) {
		return Collections.singletonList(new RelationshipModelHeaderV2.AnnotationV2("roles", Optional.ofNullable(roles)
			.filter(StringUtils::isNotEmpty)
			.orElse("guest,admin,systemAdmin")));
	}
}
