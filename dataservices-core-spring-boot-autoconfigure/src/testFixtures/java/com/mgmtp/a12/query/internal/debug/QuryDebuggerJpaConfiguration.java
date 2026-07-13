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
package com.mgmtp.a12.query.internal.debug;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelEntity;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.entity.ModelHeaderEntity;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelHeaderJpaRepository;
import com.mgmtp.a12.dataservices.model.persistence.internal.jpa.repository.ModelJpaRepository;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.entity.ModelFieldEntity;
import com.mgmtp.a12.dataservices.query.indexing.internal.persistence.repository.ModelFieldsJpaRepository;
import com.mgmtp.a12.model.header.Header;

public class QuryDebuggerJpaConfiguration {
	@Bean ModelHeaderJpaRepository modelHeaderJpaRepository() {
		return new ModelHeaderJpaRepository() {
			@Override public List<Header> findByModelType(String modelType) {
				return List.of();
			}

			@Override public List<String> findIdsByModelType(String modelType) {
				return List.of();
			}

			@Override public List<Header> findAllByModelTypeIn(List<String> modelTypes) {
				return List.of();
			}

			@Override public List<Header> findAllByIdIn(Collection<String> modelNames) {
				return List.of();
			}

			@Override public void flush() {

			}

			@Override public <S extends ModelHeaderEntity> S saveAndFlush(S entity) {
				return null;
			}

			@Override public <S extends ModelHeaderEntity> List<S> saveAllAndFlush(Iterable<S> entities) {
				return List.of();
			}

			@Override public void deleteAllInBatch(Iterable<ModelHeaderEntity> entities) {

			}

			@Override public void deleteAllByIdInBatch(Iterable<String> strings) {

			}

			@Override public void deleteAllInBatch() {

			}

			@Override public ModelHeaderEntity getOne(String s) {
				return null;
			}

			@Override public ModelHeaderEntity getById(String s) {
				return null;
			}

			@Override public ModelHeaderEntity getReferenceById(String s) {
				return null;
			}

			@Override public <S extends ModelHeaderEntity> List<S> findAll(Example<S> example) {
				return List.of();
			}

			@Override public <S extends ModelHeaderEntity> List<S> findAll(Example<S> example, Sort sort) {
				return List.of();
			}

			@Override public <S extends ModelHeaderEntity> List<S> saveAll(Iterable<S> entities) {
				return List.of();
			}

			@Override public List<ModelHeaderEntity> findAll() {
				return List.of();
			}

			@Override public List<ModelHeaderEntity> findAllById(Iterable<String> strings) {
				return List.of();
			}

			@Override public <S extends ModelHeaderEntity> S save(S entity) {
				return null;
			}

			@Override public Optional<ModelHeaderEntity> findById(String s) {
				return Optional.empty();
			}

			@Override public boolean existsById(String s) {
				return false;
			}

			@Override public long count() {
				return 0;
			}

			@Override public void deleteById(String s) {

			}

			@Override public void delete(ModelHeaderEntity entity) {

			}

			@Override public void deleteAllById(Iterable<? extends String> strings) {

			}

			@Override public void deleteAll(Iterable<? extends ModelHeaderEntity> entities) {

			}

			@Override public void deleteAll() {

			}

			@Override public List<ModelHeaderEntity> findAll(Sort sort) {
				return List.of();
			}

			@Override public Page<ModelHeaderEntity> findAll(Pageable pageable) {
				return null;
			}

			@Override public <S extends ModelHeaderEntity> Optional<S> findOne(Example<S> example) {
				return Optional.empty();
			}

			@Override public <S extends ModelHeaderEntity> Page<S> findAll(Example<S> example, Pageable pageable) {
				return null;
			}

			@Override public <S extends ModelHeaderEntity> long count(Example<S> example) {
				return 0;
			}

			@Override public <S extends ModelHeaderEntity> boolean exists(Example<S> example) {
				return false;
			}

			@Override public <S extends ModelHeaderEntity, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
				return null;
			}
		};
	}

	@Bean ModelFieldsJpaRepository modelFieldsJpaRepository() {
		return new ModelFieldsJpaRepository() {
			@Override public long deleteByModelName(String modelName) {
				return 0;
			}

			@Override public ModelFieldEntity getByModelNameAndFieldName(String model, String path) {
				return null;
			}

			@Override public void flush() {

			}

			@Override public <S extends ModelFieldEntity> S saveAndFlush(S entity) {
				return null;
			}

			@Override public <S extends ModelFieldEntity> List<S> saveAllAndFlush(Iterable<S> entities) {
				return List.of();
			}

			@Override public void deleteAllInBatch(Iterable<ModelFieldEntity> entities) {

			}

			@Override public void deleteAllByIdInBatch(Iterable<Long> longs) {

			}

			@Override public void deleteAllInBatch() {

			}

			@Override public ModelFieldEntity getOne(Long aLong) {
				return null;
			}

			@Override public ModelFieldEntity getById(Long aLong) {
				return null;
			}

			@Override public ModelFieldEntity getReferenceById(Long aLong) {
				return null;
			}

			@Override public <S extends ModelFieldEntity> List<S> findAll(Example<S> example) {
				return List.of();
			}

			@Override public <S extends ModelFieldEntity> List<S> findAll(Example<S> example, Sort sort) {
				return List.of();
			}

			@Override public <S extends ModelFieldEntity> List<S> saveAll(Iterable<S> entities) {
				return List.of();
			}

			@Override public List<ModelFieldEntity> findAll() {
				return List.of();
			}

			@Override public List<ModelFieldEntity> findAllById(Iterable<Long> longs) {
				return List.of();
			}

			@Override public <S extends ModelFieldEntity> S save(S entity) {
				return null;
			}

			@Override public Optional<ModelFieldEntity> findById(Long aLong) {
				return Optional.empty();
			}

			@Override public boolean existsById(Long aLong) {
				return false;
			}

			@Override public long count() {
				return 0;
			}

			@Override public void deleteById(Long aLong) {

			}

			@Override public void delete(ModelFieldEntity entity) {

			}

			@Override public void deleteAllById(Iterable<? extends Long> longs) {

			}

			@Override public void deleteAll(Iterable<? extends ModelFieldEntity> entities) {

			}

			@Override public void deleteAll() {

			}

			@Override public List<ModelFieldEntity> findAll(Sort sort) {
				return List.of();
			}

			@Override public Page<ModelFieldEntity> findAll(Pageable pageable) {
				return null;
			}

			@Override public <S extends ModelFieldEntity> Optional<S> findOne(Example<S> example) {
				return Optional.empty();
			}

			@Override public <S extends ModelFieldEntity> Page<S> findAll(Example<S> example, Pageable pageable) {
				return null;
			}

			@Override public <S extends ModelFieldEntity> long count(Example<S> example) {
				return 0;
			}

			@Override public <S extends ModelFieldEntity> boolean exists(Example<S> example) {
				return false;
			}

			@Override public <S extends ModelFieldEntity, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
				return null;
			}
		};
	}

	@Bean ModelJpaRepository modelJpaRepository() {
		return new ModelJpaRepository() {
			@Override public List<ModelEntity> findAll(Sort sort) {
				return List.of();
			}

			@Override public Page<ModelEntity> findAll(Pageable pageable) {
				return null;
			}

			@Override public <S extends ModelEntity> S save(S entity) {
				return null;
			}

			@Override public <S extends ModelEntity> List<S> saveAll(Iterable<S> entities) {
				return List.of();
			}

			@Override public Optional<ModelEntity> findById(String s) {
				return Optional.empty();
			}

			@Override public boolean existsById(String s) {
				return false;
			}

			@Override public List<ModelEntity> findAll() {
				return List.of();
			}

			@Override public List<ModelEntity> findAllById(Iterable<String> strings) {
				return List.of();
			}

			@Override public long count() {
				return 0;
			}

			@Override public void deleteById(String s) {

			}

			@Override public void delete(ModelEntity entity) {

			}

			@Override public void deleteAllById(Iterable<? extends String> strings) {

			}

			@Override public void deleteAll(Iterable<? extends ModelEntity> entities) {

			}

			@Override public void deleteAll() {

			}

			@Override public void flush() {

			}

			@Override public <S extends ModelEntity> S saveAndFlush(S entity) {
				return null;
			}

			@Override public <S extends ModelEntity> List<S> saveAllAndFlush(Iterable<S> entities) {
				return List.of();
			}

			@Override public void deleteAllInBatch(Iterable<ModelEntity> entities) {

			}

			@Override public void deleteAllByIdInBatch(Iterable<String> strings) {

			}

			@Override public void deleteAllInBatch() {

			}

			@Override public ModelEntity getOne(String s) {
				return null;
			}

			@Override public ModelEntity getById(String s) {
				return null;
			}

			@Override public ModelEntity getReferenceById(String s) {
				return null;
			}

			@Override public <S extends ModelEntity> Optional<S> findOne(Example<S> example) {
				return Optional.empty();
			}

			@Override public <S extends ModelEntity> List<S> findAll(Example<S> example) {
				return List.of();
			}

			@Override public <S extends ModelEntity> List<S> findAll(Example<S> example, Sort sort) {
				return List.of();
			}

			@Override public <S extends ModelEntity> Page<S> findAll(Example<S> example, Pageable pageable) {
				return null;
			}

			@Override public <S extends ModelEntity> long count(Example<S> example) {
				return 0;
			}

			@Override public <S extends ModelEntity> boolean exists(Example<S> example) {
				return false;
			}

			@Override public <S extends ModelEntity, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
				return null;
			}
		};
	}

}
