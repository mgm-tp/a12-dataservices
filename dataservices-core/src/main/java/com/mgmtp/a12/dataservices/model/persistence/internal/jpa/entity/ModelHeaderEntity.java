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
package com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mgmtp.a12.model.header.Annotation;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.model.header.Label;

import jakarta.persistence.Cacheable;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
@Table(name = "model_headers")
@Cacheable @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@NoArgsConstructor @Data
@Entity public class ModelHeaderEntity implements Header, Serializable {

	@Id @Column(nullable = false, unique = true) private String id;

	@Column(nullable = false) private String modelType;

	@Column private String modelVersion;

	@Column private String description;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "model_locales", joinColumns = @JoinColumn(name = "model_id"))
	@Column(name = "locale") private Set<Locale> locales;

	@ElementCollection(targetClass = ModelReference.class, fetch = FetchType.EAGER)
	@CollectionTable(name = "model_references", joinColumns = @JoinColumn(name = "model_id", referencedColumnName = "id"))
	private Set<? extends com.mgmtp.a12.model.header.ModelReference> modelReferences;

	@ElementCollection(fetch = FetchType.EAGER)
	@MapKeyColumn(name = "name")
	@CollectionTable(name = "model_annotations", joinColumns = @JoinColumn(name = "model_id"))
	@Column(name = "annotation_value") private Map<String, String> annotations;

	@ElementCollection(fetch = FetchType.EAGER)
	@MapKeyColumn(name = "locale")
	@CollectionTable(name = "model_labels", joinColumns = @JoinColumn(name = "model_id"))
	@Column(name = "text") private Map<Locale, String> labels;

	public ModelHeaderEntity(@NonNull Header header) {
		id = header.getId();
		modelVersion = header.getModelVersion();
		modelType = header.getModelType();
		description = header.getDescription();
		locales = header.getLocales() == null ? null
			: new HashSet<>(header.getLocales());
		labels = header.getLabels() == null ? null
			: header.getLabels().stream()
			.filter(label -> label.getLocale() != null && StringUtils.isNotEmpty(label.getText()))
			.collect(Collectors.toMap(Label::getLocale, Label::getText));
		modelReferences = header.getModelReferences() == null ? null
			: header.getModelReferences().stream()
			.map(ModelReference::new)
			.collect(Collectors.toSet());
		annotations = Optional.ofNullable(header.getAnnotations())
			.map(a -> a.stream().collect(Collectors.toMap(Annotation::getName, Annotation::getValue)))
			.orElse(null);
	}

	@Override public List<Locale> getLocales() {
		return Optional.ofNullable(locales)
			.map(ArrayList::new)
			.orElse(null);
	}

	@Override public List<com.mgmtp.a12.model.header.ModelReference> getModelReferences() {
		return Optional.ofNullable(modelReferences)
			.map(ArrayList<com.mgmtp.a12.model.header.ModelReference>::new)
			.orElse(null);
	}

	@Override public List<Annotation> getAnnotations() {
		return annotations.entrySet().stream().map(this::entryToAnnotation).toList();
	}

	private Annotation entryToAnnotation(Map.Entry<String, String> entry) {
		return new Annotation() {
			@Override public String getName() {
				return entry.getKey();
			}

			@Override public String getValue() {
				return entry.getValue();
			}
		};
	}

	@Override public List<Label> getLabels() {
		return Optional.ofNullable(labels)
			.stream()
			.map(Map::entrySet)
			.flatMap(Collection::stream)
			.map(this::entryToLabel)
			.toList();
	}

	private Label entryToLabel(Map.Entry<Locale, String> entry) {
		return new Label() {
			@Override public Locale getLocale() {
				return entry.getKey();
			}

			@Override public String getText() {
				return entry.getValue();
			}
		};
	}

	@JsonIgnore
	@Transient public Set<Locale> getLocalesAsSet() {
		return locales;
	}

	@JsonIgnore
	@Transient public Map<Locale, String> getLabelsAsMap() {
		return labels;
	}

	@JsonIgnore
	@Transient public Map<String, String> getAnnotationsAsMap() {
		return annotations;
	}

	@JsonIgnore
	@Transient public Set<? extends com.mgmtp.a12.model.header.ModelReference> getModelReferencesAsSet() {
		return modelReferences;
	}

	@NoArgsConstructor @Data @EqualsAndHashCode @ToString
	@Embeddable public static class ModelReference implements com.mgmtp.a12.model.header.ModelReference {

		@Column(name = "REFERENCE_TYPE", nullable = false) private String modelType;
		@Column(name = "REFERENCE_ID", nullable = false) private String reference;
		@Column private String purpose;
		@Column private String alias;

		public ModelReference(com.mgmtp.a12.model.header.ModelReference r) {
			modelType = r.getModelType();
			reference = r.getReference();
			purpose = r.getPurpose();
			alias = r.getAlias();
		}
	}
}
