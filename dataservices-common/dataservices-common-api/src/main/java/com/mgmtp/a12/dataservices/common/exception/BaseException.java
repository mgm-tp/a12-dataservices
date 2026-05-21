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
package com.mgmtp.a12.dataservices.common.exception;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mgmtp.a12.dataservices.common.LocalizedEntry;

import lombok.Getter;
import lombok.Setter;

/**
 * Base type of all Data Services exceptions.
 * Declares all exposed fields used to construct consistent error responses.
 * All descendants are serializable and follow the {@link BaseError} contract.
 */
public abstract class BaseException extends RuntimeException implements BaseError, AnonymityException {

	private static final long serialVersionUID = 1L;

	@Getter private int code;
	@JsonSerialize(as = LocalizedEntry.class)
	@JsonDeserialize(as = LocalizedEntry.class)
	protected LocalizedMessageWithPriority longMessage;
	@JsonSerialize(as = LocalizedEntry.class)
	@JsonDeserialize(as = LocalizedEntry.class)
	protected LocalizedMessageWithPriority shortMessage;
	@Getter @Setter private String anonymityMessage;
	@Setter private ErrorLevel errorLevel = ErrorLevel.ERROR;
	@Getter @Setter private ErrorDetail errorDetail;
	@Getter @Setter private boolean recoverable = false;
	@JsonIgnore
	protected final MessagePriority priority;
	@JsonIgnore
	protected final String key;

	/**
	 * Creates a new BaseException with a code and underlying cause.
	 *
	 * @param code RPC error code.
	 * @param cause Originating exception; may be null.
	 */
	protected BaseException(int code, Throwable cause) {
		super(cause);
		this.code = code;
		this.errorDetail = new ErrorDetail(code);
		priority = null;
		key = null;
	}

	/**
	 * Creates a new BaseException with a code and no message.
	 *
	 * @param code RPC error code.
	 */
	protected BaseException(int code) {
		this.code = code;
		this.errorDetail = new ErrorDetail(code);
		priority = null;
		key = null;
	}

	/**
	 * Creates a new BaseException with a code, message, and cause.
	 *
	 * @param code RPC error code.
	 * @param message Default English message; may be null.
	 * @param cause Originating exception; may be null.
	 */
	protected BaseException(int code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
		this.errorDetail = new ErrorDetail(code);
		priority = null;
		key = null;
	}

	/**
	 * Creates a new BaseException with localization details and priority.
	 *
	 * @param code RPC error code.
	 * @param key Localization key; may be null.
	 * @param message Default English message; may be null.
	 * @param priority Message priority; may be null.
	 * @param t Originating exception; may be null.
	 */
	protected BaseException(int code, String key, String message, MessagePriority priority, Throwable t) {
		super(message, t);
		this.code = code;
		this.errorDetail = new ErrorDetail(code);
		this.key = key;
		this.priority = priority;
	}

	/**
	 * Creates a new BaseException with localization details and a cause.
	 *
	 * @param code RPC error code.
	 * @param key Localization key; may be null.
	 * @param message Default English message; may be null.
	 * @param e Originating exception; may be null.
	 */
	protected BaseException(int code, String key, String message, Throwable e) {
		this(code, key, message, null, e);
	}

	protected BaseException(int code, String key, String message) {
		this(code, key, message, null, null);
	}

	protected BaseException(int code, String key, String message, MessagePriority priority) {
		this(code, key, message, priority, null);
	}

	protected BaseException(int code, String message, MessagePriority priority) {
		this(code, null, message, priority);
	}

	protected BaseException(int code, String message) {
		this(code, null, message);
	}

	protected BaseException() {
		super();
		priority = null;
		key = null;
	}

	@Deprecated(since = "36.0.1")
	protected BaseException(String message, Throwable cause) {
		super(message, cause);
		priority = null;
		key = null;
	}

	@Deprecated(since = "36.0.1")
	protected BaseException(String key, String message, MessagePriority priority, Throwable t) {
		super(message, t);
		this.key = key;
		this.priority = priority;
	}

	@Deprecated(since = "36.0.1")
	protected BaseException(String key, String message, Throwable e) {
		this(key, message, null, e);
	}

	@Deprecated(since = "36.0.1")
	protected BaseException(String key, String message) {
		this(key, message, null, null);
	}

	@Deprecated(since = "36.0.1")
	protected BaseException(String key, String message, MessagePriority priority) {
		this(key, message, priority, null);
	}

	@Deprecated(since = "36.0.1")
	protected BaseException(String message, MessagePriority priority) {
		this(null, message, priority);
	}

	@Deprecated(since = "36.0.1")
	protected BaseException(String message) {
		this(null, message);
	}

	@Override public ErrorLevel getLevel() {
		return errorLevel;
	}

	@Override public LocalizedMessageWithPriority getShortMessage() {
		return shortMessage == null ? new LocalizedMessageWithPriority(key, getMessage(), priority) : shortMessage;
	}

	/** Sets the short message, respecting message priority.
	 *
	 * @param newMessage The new message to set.
	 */
	@JsonIgnore public void setShortMessage(LocalizedMessageWithPriority newMessage) {
		this.shortMessage = shouldOverwrite(getShortMessage(), newMessage, newMessage.getPriority());
	}

	/** Sets the short message, respecting message priority.
	 *
	 * @param newMessage The new message to set.
	 * @param priority The priority of the new message.
	 */
	@JsonIgnore public void setShortMessage(LocalizedEntry newMessage, MessagePriority priority) {
		this.shortMessage = shouldOverwrite(getShortMessage(), new LocalizedMessageWithPriority(newMessage, priority), priority);
	}

	/** Sets the short message without priority consideration.
	 *
	 * @param shortMessage The new short message to set.
	 */
	public void setShortMessage(LocalizedEntry shortMessage) {
		this.shortMessage = new LocalizedMessageWithPriority(shortMessage);
	}

	@Override public LocalizedMessageWithPriority getLongMessage() {
		return longMessage == null ? new LocalizedMessageWithPriority(key, getMessage(), priority) : longMessage;
	}

	/** Sets the long message, respecting message priority.
	 *
	 * @param newMessage The new message to set.
	 */
	@JsonIgnore public void setLongMessage(LocalizedMessageWithPriority newMessage) {
		this.longMessage = shouldOverwrite(getLongMessage(), newMessage, newMessage.getPriority());
	}

	/** Sets the long message, respecting message priority.
	 *
	 * @param newMessage The new message to set.
	 * @param priority The priority of the new message.
	 */
	@JsonIgnore public void setLongMessage(LocalizedEntry newMessage, MessagePriority priority) {
		this.longMessage = shouldOverwrite(getLongMessage(), new LocalizedMessageWithPriority(newMessage, priority), priority);
	}

	/** Sets the long message without priority consideration.
	 *
	 * @param longMessage The new long message to set.
	 */
	public void setLongMessage(LocalizedEntry longMessage) {
		this.longMessage = new LocalizedMessageWithPriority(longMessage);
	}

	private static LocalizedMessageWithPriority shouldOverwrite(LocalizedMessageWithPriority oldMessage, LocalizedMessageWithPriority newMessage,
		MessagePriority priority) {
		return Optional.ofNullable(oldMessage)
			.filter(om -> om.preserveValue(priority))
			.orElse(newMessage);
	}

	/** Sets the short message and returns this instance for fluent chaining.
	 *
	 * @param key The localization key.
	 * @param defaultMessage The default message.
	 * @return This {@link BaseException} instance.
	 */
	public BaseException withShortMessage(String key, String defaultMessage) {
		setShortMessage(new LocalizedEntry(key, defaultMessage));
		return this;
	}

	/** Sets the short message with priority and returns this instance for fluent chaining.
	 *
	 * @param key The localization key.
	 * @param defaultMessage The default message.
	 * @param priority The message priority.
	 * @return This {@link BaseException} instance.
	 */
	public BaseException withShortMessage(String key, String defaultMessage, MessagePriority priority) {
		setShortMessage(new LocalizedEntry(key, defaultMessage), priority);
		return this;
	}

	/** Sets the long message and returns this instance for fluent chaining.
	 *
	 * @param key The localization key.
	 * @param defaultMessage The default message.
	 * @return This {@link BaseException} instance.
	 */
	public BaseException withLongMessage(String key, String defaultMessage) {
		setLongMessage(new LocalizedEntry(key, defaultMessage));
		return this;
	}

	/** Sets both short and long messages and returns this instance for fluent chaining.
	 *
	 * @param key The localization key.
	 * @param defaultMessage The default message.
	 * @return This {@link BaseException} instance.
	 */
	public BaseException withLocalizedMessage(String key, String defaultMessage) {
		return this.withShortMessage(key, defaultMessage).withLongMessage(key, defaultMessage);
	}

	/** Sets the anonymity message and returns this instance for fluent chaining.
	 *
	 * @param anonymityMessage The anonymity message.
	 * @return This {@link BaseException} instance.
	 */
	public BaseException withAnonymityMessage(String anonymityMessage) {
		this.anonymityMessage = anonymityMessage;
		return this;
	}

	/** Updates the short message, preserving the existing key if the provided key is null.
	 *
	 * @param key The localization key; may be null.
	 * @param message The default message.
	 * @param priority The message priority.
	 * @return This {@link BaseException} instance.
	 */
	public BaseException updateShortMessage(String key, String message, MessagePriority priority) {
		key = Optional.ofNullable(key)
			.or(() -> Optional.ofNullable(getShortMessage())
				.map(LocalizedEntry::getKey))
			.orElse(null);
		setShortMessage(new LocalizedMessageWithPriority(key, message, priority));
		return this;
	}

	/** Updates the short message, preserving the existing key if the provided key is null.
	 *
	 * @param key The localization key; may be null.
	 * @param message The default message.
	 * @return This {@link BaseException} instance.
	 */
	public BaseException updateShortMessage(String key, String message) {
		return updateShortMessage(key, message, null);
	}

	/** Updates the long message, preserving the existing key if the provided key is null.
	 *
	 * @param message The default message.
	 * @param priority The message priority.
	 * @return This {@link BaseException} instance.
	 */
	public BaseException updateLongMessage(String message, MessagePriority priority) {
		String key = Optional.ofNullable(getLongMessage())
			.map(LocalizedEntry::getKey).orElse(null);
		setLongMessage(new LocalizedMessageWithPriority(key, message, priority));
		return this;
	}

	/** Updates the long message, preserving the existing key if the provided key is null.
	 *
	 * @param key The localization key; may be null.
	 * @param mess The default message.
	 * @param priority The message priority.
	 * @return This {@link BaseException} instance.
	 */
	public BaseException withLongMessage(String key, String mess, MessagePriority priority) {
		setLongMessage(new LocalizedEntry(key, mess), priority);
		return this;
	}

	@Override public String getMessage() {
		String message = super.getMessage();
		return message == null ? anonymityMessage : message;
	}

	/** Localized message with associated priority. */
	@Getter @Setter
	public static class LocalizedMessageWithPriority extends LocalizedEntry {

		private transient MessagePriority priority;

		/** Creates a new LocalizedMessageWithPriority.
		 *
		 * @param key The localization key.
		 * @param defaultMessage The default message.
		 */
		public LocalizedMessageWithPriority(String key, String defaultMessage) {
			super(key, defaultMessage);
		}

		/** Creates a new LocalizedMessageWithPriority.
		 *
		 * @param le The localized entry.
		 */
		public LocalizedMessageWithPriority(LocalizedEntry le) {
			this(le.getKey(), le.getDefaultMessage());
		}

		/** Creates a new LocalizedMessageWithPriority.
		 *
		 * @param key The localization key.
		 * @param message The default message.
		 * @param priority The message priority.
		 */
		public LocalizedMessageWithPriority(String key, String message, MessagePriority priority) {
			this(key, message);
			setPriority(priority);
		}

		/** Creates a new LocalizedMessageWithPriority.
		 *
		 * @param le The localized message with priority.
		 */
		public LocalizedMessageWithPriority(LocalizedMessageWithPriority le) {
			this(le.getKey(), le.getDefaultMessage(), le.getPriority());
		}

		/** Creates a new LocalizedMessageWithPriority.
		 *
		 * @param longMessage The localized entry.
		 * @param priority The message priority.
		 */
		public LocalizedMessageWithPriority(LocalizedEntry longMessage, MessagePriority priority) {
			this(longMessage);
			setPriority(priority);
		}

		private boolean preserveValue(MessagePriority priority) {
			return getPriority() != null && (priority == null || getPriority().ordinal() > priority.ordinal());
		}

	}

	public enum MessagePriority {
		LOW, MEDIUM, HIGH

	}
}
