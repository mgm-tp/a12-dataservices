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
package com.mgmtp.a12.dataservices.rpc.internal;

import com.mgmtp.a12.dataservices.exception.RequestIdConflictException;
import com.mgmtp.a12.dataservices.rpc.internal.jpa.entity.RequestIdEntity;
import com.mgmtp.a12.dataservices.rpc.internal.jpa.repository.RequestIdRepository;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Optional;

import static com.mgmtp.a12.dataservices.RequestIdState.SUCCESS;
import static com.mgmtp.a12.dataservices.RequestIdState.PENDING;
import static com.mgmtp.a12.dataservices.RequestIdState.FAILED;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class RequestIdManagerTest {

	@Mock
	private RequestIdRepository requestIdRepository;

	private static final String REQUEST_ID = "test-request-id";

	@BeforeMethod
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void constructor_whenRequestIdExists_shouldThrowException() {
		// Given
		RequestIdEntity existingEntity = new RequestIdEntity(REQUEST_ID, SUCCESS, Instant.now());
		when(requestIdRepository.existsById(REQUEST_ID)).thenReturn(true);
		when(requestIdRepository.findById(REQUEST_ID)).thenReturn(Optional.of(existingEntity));
		when(requestIdRepository.getReferenceById(REQUEST_ID)).thenReturn(existingEntity);

		// When/Then
		assertThatThrownBy(() -> new RequestIdManager(requestIdRepository, REQUEST_ID))
			.isInstanceOf(RequestIdConflictException.class)
			.satisfies(e -> {
				RequestIdConflictException exception = (RequestIdConflictException) e;
				assertEquals(exception.getState(), SUCCESS);
				assertEquals(exception.getRequestId(), REQUEST_ID);
			});
	}

	@Test
	public void constructor_whenRequestIdDoesNotExist_shouldInsertSuccessfully() {
		// Given
		when(requestIdRepository.existsById(REQUEST_ID)).thenReturn(false);
		doNothing().when(requestIdRepository).insert(REQUEST_ID);
		doNothing().when(requestIdRepository).flush();

		// When
		new RequestIdManager(requestIdRepository, REQUEST_ID);

		// Then
		verify(requestIdRepository).insert(REQUEST_ID);
		verify(requestIdRepository).flush();
	}

	@Test
	public void constructor_whenInsertFails_shouldThrowException() {
		// Given
		when(requestIdRepository.existsById(REQUEST_ID)).thenReturn(false);
		doThrow(new RuntimeException("DB Error")).when(requestIdRepository).insert(any());

		// When/Then
		assertThatThrownBy(() -> new RequestIdManager(requestIdRepository, REQUEST_ID))
			.isInstanceOf(RequestIdConflictException.class)
			.satisfies(e -> {
				RequestIdConflictException exception = (RequestIdConflictException) e;
				assertEquals(exception.getState(), PENDING);
				assertEquals(exception.getRequestId(), REQUEST_ID);
			});
	}

	@Test
	public void finalizeRequestSuccess_whenRequestIsPending_shouldUpdateState() {
		// Given
		when(requestIdRepository.existsById(REQUEST_ID)).thenReturn(false);
		RequestIdManager manager = new RequestIdManager(requestIdRepository, REQUEST_ID);

		RequestIdEntity entity = new RequestIdEntity(REQUEST_ID, PENDING, Instant.now());
		when(requestIdRepository.getReferenceById(REQUEST_ID)).thenReturn(entity);
		when(requestIdRepository.findById(REQUEST_ID)).thenReturn(Optional.of(entity));

		// When
		manager.finalizeRequestSuccess();

		// Then
		verify(requestIdRepository).saveAndFlush(argThat(savedEntity ->
			savedEntity.getId().equals(REQUEST_ID) && savedEntity.getState() == SUCCESS
		));
	}

	@Test
	public void finalizeRequestError_whenRequestIsPending_shouldUpdateState() {
		// Given
		when(requestIdRepository.existsById(REQUEST_ID)).thenReturn(false);
		RequestIdManager manager = new RequestIdManager(requestIdRepository, REQUEST_ID);

		RequestIdEntity entity = new RequestIdEntity(REQUEST_ID, PENDING, Instant.now());
		when(requestIdRepository.getReferenceById(REQUEST_ID)).thenReturn(entity);
		when(requestIdRepository.findById(REQUEST_ID)).thenReturn(Optional.of(entity));

		// When
		manager.finalizeRequestError();

		// Then
		verify(requestIdRepository).saveAndFlush(argThat(savedEntity ->
			savedEntity.getId().equals(REQUEST_ID) && savedEntity.getState() == FAILED
		));
	}

	@Test
	public void finalizeRequest_whenRequestIsAlreadyProcessed_shouldThrowException() {
		// Given
		when(requestIdRepository.existsById(REQUEST_ID)).thenReturn(false);
		RequestIdManager manager = new RequestIdManager(requestIdRepository, REQUEST_ID);

		RequestIdEntity entity = new RequestIdEntity(REQUEST_ID, SUCCESS, Instant.now());
		when(requestIdRepository.getReferenceById(REQUEST_ID)).thenReturn(entity);
		when(requestIdRepository.findById(REQUEST_ID)).thenReturn(Optional.of(entity));

		// When/Then
		assertThatThrownBy(manager::finalizeRequestSuccess)
			.isInstanceOf(RequestIdConflictException.class)
			.satisfies(e -> {
				RequestIdConflictException exception = (RequestIdConflictException) e;
				assertEquals(exception.getState(), SUCCESS);
				assertEquals(exception.getRequestId(), REQUEST_ID);
			});
	}
}
