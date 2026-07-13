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
package com.mgmtp.a12.contentstore.annotation.internal;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Documented @Target({ ElementType.TYPE, ElementType.METHOD })
@Inherited
@Retention(value = RetentionPolicy.RUNTIME) @Transactional
public @interface ContentStoreTransactional {

	/**
	 * Alias for {@link #transactionManager}.
	 *
	 * @see #transactionManager
	 */
	@AliasFor(annotation = Transactional.class, attribute = "transactionManager")
	String value() default "csTransactionManager";

	/**
	 * A *qualifier* value for the specified transaction.
	 *
	 * May be used to determine the target transaction manager, matching the
	 * qualifier value (or the bean name) of a specific
	 *
	 * {@link org.springframework.transaction.TransactionManager TransactionManager}
	 * bean definition.
	 *
	 * @see #value
	 * @see org.springframework.transaction.PlatformTransactionManager
	 * @see org.springframework.transaction.ReactiveTransactionManager
	 * @since 4.2
	 */
	@AliasFor(annotation = Transactional.class, attribute = "value")
	String transactionManager() default "csTransactionManager";

	/**
	 * Defines zero (0) or more transaction labels.
	 *
	 * Labels may be used to describe a transaction, and they can be evaluated
	 * by individual transaction managers. Labels may serve a solely descriptive
	 * purpose or map to pre-defined transaction manager-specific options.
	 *
	 * See the documentation of the actual transaction manager implementation
	 * for details on how it evaluates transaction labels.
	 *
	 * @see org.springframework.transaction.interceptor.DefaultTransactionAttribute#getLabels()
	 * @since 5.3
	 */
	@AliasFor(annotation = Transactional.class, attribute = "label")
	String[] label() default {};

	/**
	 * The transaction propagation type.
	 *
	 * Defaults to {@link Propagation#REQUIRED}.
	 *
	 * @see org.springframework.transaction.interceptor.TransactionAttribute#getPropagationBehavior()
	 */
	@AliasFor(annotation = Transactional.class, attribute = "propagation")
	Propagation propagation() default Propagation.REQUIRED;

	/**
	 * The transaction isolation level.
	 *
	 * Defaults to {@link Isolation#DEFAULT}.
	 *
	 * Exclusively designed for use with {@link Propagation#REQUIRED} or
	 * {@link Propagation#REQUIRES_NEW} since it only applies to newly started
	 * transactions. Consider switching the "validateExistingTransactions" flag to
	 * "true" on your transaction manager if you'd like isolation level declarations
	 * to get rejected when participating in an existing transaction with a different
	 * isolation level.
	 *
	 * @see org.springframework.transaction.interceptor.TransactionAttribute#getIsolationLevel()
	 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#setValidateExistingTransaction
	 */
	@AliasFor(annotation = Transactional.class, attribute = "isolation")
	Isolation isolation() default Isolation.DEFAULT;

	/**
	 * The timeout for this transaction (in seconds).
	 *
	 * Defaults to the default timeout of the underlying transaction system.
	 *
	 * Exclusively designed for use with {@link Propagation#REQUIRED} or
	 * {@link Propagation#REQUIRES_NEW} since it only applies to newly started
	 * transactions.
	 *
	 * @return the timeout in seconds
	 * @see org.springframework.transaction.interceptor.TransactionAttribute#getTimeout()
	 */
	@AliasFor(annotation = Transactional.class, attribute = "timeout")
	int timeout() default TransactionDefinition.TIMEOUT_DEFAULT;

	/**
	 * The timeout for this transaction (in seconds).
	 *
	 * Defaults to the default timeout of the underlying transaction system.
	 *
	 * Exclusively designed for use with {@link Propagation#REQUIRED} or
	 * {@link Propagation#REQUIRES_NEW} since it only applies to newly started
	 * transactions.
	 *
	 * @return the timeout in seconds as a String value, e.g. a placeholder
	 * @see org.springframework.transaction.interceptor.TransactionAttribute#getTimeout()
	 * @since 5.3
	 */
	@AliasFor(annotation = Transactional.class, attribute = "timeoutString")
	String timeoutString() default "";

	/**
	 * A boolean flag that can be set to `true` if the transaction is
	 * effectively read-only, allowing for corresponding optimizations at runtime.
	 *
	 * Defaults to `false`.
	 *
	 * This just serves as a hint for the actual transaction subsystem;
	 * it will *not necessarily* cause failure of write access attempts.
	 * A transaction manager which cannot interpret the read-only hint will
	 * *not* throw an exception when asked for a read-only transaction
	 * but rather silently ignore the hint.
	 *
	 * @see org.springframework.transaction.interceptor.TransactionAttribute#isReadOnly()
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager#isCurrentTransactionReadOnly()
	 */
	@AliasFor(annotation = Transactional.class, attribute = "readOnly")
	boolean readOnly() default false;

	/**
	 * Defines zero (0) or more exception {@linkplain Class types}, which must be
	 * subclasses of {@link Throwable}, indicating which exception types must cause
	 * a transaction rollback.
	 *
	 * By default, a transaction will be rolled back on {@link RuntimeException}
	 * and {@link Error} but not on checked exceptions (business exceptions). See
	 * {@link org.springframework.transaction.interceptor.DefaultTransactionAttribute#rollbackOn(Throwable)}
	 * for a detailed explanation.
	 *
	 * This is the preferred way to construct a rollback rule (in contrast to
	 * {@link #rollbackForClassName}), matching the exception type and its subclasses
	 * in a type-safe manner. See the {@linkplain Transactional class-level javadocs}
	 * for further details on rollback rule semantics.
	 *
	 * @see #rollbackForClassName
	 * @see org.springframework.transaction.interceptor.RollbackRuleAttribute#RollbackRuleAttribute(Class)
	 * @see org.springframework.transaction.interceptor.DefaultTransactionAttribute#rollbackOn(Throwable)
	 */
	@AliasFor(annotation = Transactional.class, attribute = "rollbackFor")
	Class<? extends Throwable>[] rollbackFor() default {};

	/**
	 * Defines zero (0) or more exception name patterns (for exceptions which must be a
	 * subclass of {@link Throwable}), indicating which exception types must cause
	 * a transaction rollback.
	 *
	 * See the {@linkplain Transactional class-level javadocs} for further details
	 * on rollback rule semantics, patterns, and warnings regarding possible
	 * unintentional matches.
	 *
	 * @see #rollbackFor
	 * @see org.springframework.transaction.interceptor.RollbackRuleAttribute#RollbackRuleAttribute(String)
	 * @see org.springframework.transaction.interceptor.DefaultTransactionAttribute#rollbackOn(Throwable)
	 */
	@AliasFor(annotation = Transactional.class, attribute = "rollbackForClassName")
	String[] rollbackForClassName() default {};

	/**
	 * Defines zero (0) or more exception {@link Class types}, which must be
	 * subclasses of {@link Throwable}, indicating which exception types must
	 * *not* cause a transaction rollback.
	 *
	 * This is the preferred way to construct a rollback rule (in contrast to
	 * {@link #noRollbackForClassName}), matching the exception type and its subclasses
	 * in a type-safe manner. See the {@linkplain Transactional class-level javadocs}
	 * for further details on rollback rule semantics.
	 *
	 * @see #noRollbackForClassName
	 * @see org.springframework.transaction.interceptor.NoRollbackRuleAttribute#NoRollbackRuleAttribute(Class)
	 * @see org.springframework.transaction.interceptor.DefaultTransactionAttribute#rollbackOn(Throwable)
	 */
	@AliasFor(annotation = Transactional.class, attribute = "noRollbackFor")
	Class<? extends Throwable>[] noRollbackFor() default {};

	/**
	 * Defines zero (0) or more exception name patterns (for exceptions which must be a
	 * subclass of {@link Throwable}) indicating which exception types must *not*
	 * cause a transaction rollback.
	 *
	 * See the {@linkplain Transactional class-level javadocs} for further details
	 * on rollback rule semantics, patterns, and warnings regarding possible
	 * unintentional matches.
	 *
	 * @see #noRollbackFor
	 * @see org.springframework.transaction.interceptor.NoRollbackRuleAttribute#NoRollbackRuleAttribute(String)
	 * @see org.springframework.transaction.interceptor.DefaultTransactionAttribute#rollbackOn(Throwable)
	 */
	@AliasFor(annotation = Transactional.class, attribute = "noRollbackForClassName")
	String[] noRollbackForClassName() default {};
}
