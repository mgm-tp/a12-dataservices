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
package com.mgmtp.a12.dataservices.relationship.migrator.model.v2;

import java.util.List;
import java.util.stream.Collectors;

import com.mgmtp.a12.dataservices.relationship.migrator.model.IRelationshipModelMigrator;
import com.mgmtp.a12.dataservices.relationship.migrator.model.v2.RelationshipModelV2.RelationshipModelContentV2.DisplayLabelV2;
import com.mgmtp.a12.dataservices.relationship.migrator.model.v2.RelationshipModelV2.RelationshipModelContentV2.EntityCharacteristicsV2;
import com.mgmtp.a12.dataservices.relationship.migrator.model.v2.RelationshipModelV2.RelationshipModelHeaderV2.AnnotationV2;
import com.mgmtp.a12.dataservices.relationship.migrator.model.v2.RelationshipModelV2.RelationshipModelHeaderV2.LabelV2;
import com.mgmtp.a12.dataservices.relationship.migrator.model.v2.RelationshipModelV2.RelationshipModelHeaderV2.LocaleCodeV2;
import com.mgmtp.a12.dataservices.relationship.migrator.model.v2.RelationshipModelV2.RelationshipModelHeaderV2.ModelReferenceV2;
import com.mgmtp.a12.dataservices.relationship.migrator.model.v3.RelationshipModelV3;
import com.mgmtp.a12.dataservices.relationship.migrator.model.v3.RelationshipModelV3.LabelV3;
import com.mgmtp.a12.dataservices.relationship.migrator.model.v3.RelationshipModelV3.RelationshipModelContentV3;
import com.mgmtp.a12.dataservices.relationship.migrator.model.v3.RelationshipModelV3.RelationshipModelContentV3.EntityCharacteristicsV3;
import com.mgmtp.a12.dataservices.relationship.migrator.model.v3.RelationshipModelV3.RelationshipModelHeaderV3;
import com.mgmtp.a12.dataservices.relationship.migrator.model.v3.RelationshipModelV3.RelationshipModelHeaderV3.AnnotationV3;
import com.mgmtp.a12.dataservices.relationship.migrator.model.v3.RelationshipModelV3.RelationshipModelHeaderV3.LocaleCodeV3;
import com.mgmtp.a12.dataservices.relationship.migrator.model.v3.RelationshipModelV3.RelationshipModelHeaderV3.ModelReferenceV3;

import lombok.NonNull;

/**
 * @deprecated relationship model migration moved to the npm package
 *
 */
@SuppressWarnings({"removal",  "deprecation"})
@Deprecated(since = "38.1.0", forRemoval = true)
public class RelationshipModelMigratorV2ToV3 implements IRelationshipModelMigrator<RelationshipModelV2, RelationshipModelV3> {

	@Override public String getModelName(RelationshipModelV2 source) {
		return source.getHeader().getId();
	}

	@Override public String getVersion(RelationshipModelV2 source) {
		return source.getHeader().getModelVersion();
	}

	@Override public RelationshipModelV3 migrateModel(RelationshipModelV2 source) {
		return new RelationshipModelV3(convertHeader(source), convertContent(source));
	}

	@NonNull private RelationshipModelHeaderV3 convertHeader(RelationshipModelV2 source) {
		return new RelationshipModelHeaderV3(source.getHeader().getId(), convertLabels(source.getHeader().getLabels()),
			convertAnnotations(source.getHeader().getAnnotations()), convertLocales(source.getHeader().getLocales()),
			convertModelReferences(source.getHeader().getModelReferences()));
	}

	@NonNull private RelationshipModelContentV3 convertContent(RelationshipModelV2 source) {
		return new RelationshipModelContentV3(source.getContent().getAssociationType(), source.getContent().getLinkDocumentModel(),
			source.getContent().getDuplicatesAllowed(), source.getContent().getStorage(), source.getContent().getEmbeddedGroupPath(),
			convertDisplayLabels(source.getContent().getDisplayLabel()), convertEntityCharacteristics(source.getContent().getEntityCharacteristics()));
	}

	@Override public Class<RelationshipModelV2> getSourceType() {
		return RelationshipModelV2.class;
	}

	private List<LocaleCodeV3> convertLocales(List<LocaleCodeV2> locales) {
		return locales.stream()
			.map(l -> new LocaleCodeV3(l.getCode()))
			.collect(Collectors.toList());
	}

	private List<AnnotationV3> convertAnnotations(List<AnnotationV2> annotations) {
		return annotations.stream()
			.map(l -> new AnnotationV3(l.getName(), l.getValue()))
			.collect(Collectors.toList());
	}

	private List<LabelV3> convertLabels(List<LabelV2> labels) {
		return labels.stream()
			.map(l -> new LabelV3(l.getLocale(), l.getText()))
			.collect(Collectors.toList());
	}

	private List<LabelV3> convertDisplayLabels(List<DisplayLabelV2> displayLabel) {
		return displayLabel.stream()
			.map(dl -> new LabelV3(dl.getLanguage(), dl.getValue()))
			.collect(Collectors.toList());
	}

	private List<ModelReferenceV3> convertModelReferences(List<ModelReferenceV2> modelReferences) {
		return modelReferences.stream()
			.map(l -> new ModelReferenceV3(l.getPurpose(), l.getModelType(), l.getAlias(), l.getReference()))
			.collect(Collectors.toList());
	}

	private List<EntityCharacteristicsV3> convertEntityCharacteristics(List<EntityCharacteristicsV2> entityCharacteristics) {
		return entityCharacteristics.stream()
			.map(e -> new EntityCharacteristicsV3(e.getRole(), e.getDocumentModel(), e.getOrdered(), e.getNavigable(), e.getLinkConstraints(),
				e.getCandidateConstraints(), e.getDisplayLabel().stream()
				.map(dl -> new LabelV3(dl.getLanguage(), dl.getValue()))
				.collect(Collectors.toList()))).collect(Collectors.toList());
	}
}
