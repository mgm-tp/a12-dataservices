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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.TargetClassAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.googlecode.jsonrpc4j.ErrorResolver;
import com.googlecode.jsonrpc4j.JsonResponse;
import com.googlecode.jsonrpc4j.JsonRpcBasicServer;
import com.googlecode.jsonrpc4j.JsonRpcInterceptor;
import com.googlecode.jsonrpc4j.RequestInterceptor;
import com.mgmtp.a12.dataservices.common.LocalizedEntry;
import com.mgmtp.a12.dataservices.common.events.CommonDataServicesEventListener;
import com.mgmtp.a12.dataservices.common.exception.BaseException;
import com.mgmtp.a12.dataservices.common.exception.ErrorDetail;
import com.mgmtp.a12.dataservices.common.exception.ErrorLevel;
import com.mgmtp.a12.dataservices.common.exception.InvalidInputException;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.exception.ExceptionCodes;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.internal.DataSourceContextHolder;
import com.mgmtp.a12.dataservices.internal.TransactionHandler;
import com.mgmtp.a12.dataservices.relationship.internal.RelationshipLinkValidationListener;

import tools.jackson.databind.DeserializationFeature;
import com.mgmtp.a12.dataservices.rpc.ExceptionDetail;
import com.mgmtp.a12.dataservices.rpc.OperationError;
import com.mgmtp.a12.dataservices.rpc.RemoteOperation;
import com.mgmtp.a12.dataservices.rpc.RpcException;
import com.mgmtp.a12.dataservices.rpc.RpcExceptionSupport;
import com.mgmtp.a12.dataservices.utils.OperationContextHolder;
import com.mgmtp.a12.dataservices.utils.internal.LoadedDocumentReferencesContextHolder;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static com.mgmtp.a12.dataservices.exception.ExceptionCodes.ACCESS_DENIED_EXCEPTION_CODE;
import static com.mgmtp.a12.dataservices.exception.ExceptionCodes.RPC_ERROR_EXCEPTION_CODE;
import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.RPC_ID_NULL_ERROR_KEY;
import static com.mgmtp.a12.dataservices.exception.ExceptionKeys.RPC_OPERATION_ERROR_KEY;

/**
 * Handles JSON-RPC requests for an RPC endpoint. Allowed operations are stored in {@link #allowedOperations}.
 * When the operation is present in the request, the matching operation class is found by {@link RemoteOperation#name()}
 * and the rpc(...) method is called with provided parameters.
 * During the operation execution, the id of operation is stored in {@link OperationContextHolder#id()} and after execution,
 * the result of the operation is stored in {@link OperationContextHolder#OPERATION_ID_TO_CONTEXT_HOLDER} map,
 * so that the result of the operation can be referenced in future operations of the current request.
 * The response of the rpc(...) method is then returned to client.
 */
@Slf4j
public class JsonRpcOperationDispatcher extends JsonRpcBasicServer implements RequestInterceptor, JsonRpcInterceptor, ErrorResolver {

	private final ObjectMapper objectMapper;
	private final ObjectMapper throwableObjectMapper;

	public static final String RPC_ERROR_MESSAGE = "JSON-RPC Request failed and rollback was performed";
	protected static final String UNKNOWN = "UNKNOWN";
	private static final String METHOD_NAME_EXECUTE = "rpc";
	private static final Pattern METHOD_WITH_VERSION = Pattern.compile("^(.*):(\\d+)$");

	private final boolean spelAllowed;
	private final Set<String> allowedOperations;
	private final DataServicesCoreProperties dataServicesCoreProperties;
	private final RelationshipLinkValidationListener linkValidator;
	private final JsonNodeSpelProcessor spelProcessor;
	private final boolean debugRpcResponses;
	private final TransactionHandler transactionHandler;
	private final boolean replicaRoutingEnabled;

	@Getter(AccessLevel.PROTECTED)
	private Map<String, Object> operations = new HashMap<>();
	private RequestInterceptor requestInterceptor;

	public JsonRpcOperationDispatcher(Set<String> allowedOperations, RelationshipLinkValidationListener linkValidator,
		ApplicationEventPublisher ignoredApplicationEventPublisher, ObjectMapper objectMapper, DataServicesCoreProperties dataServicesCoreProperties,
		boolean spelAllowed, boolean debugRpcResponses, TransactionHandler transactionHandler, Environment environment) {

		super(objectMapper.rebuild().disable(DeserializationFeature.WRAP_EXCEPTIONS).build(), (Object) null);
		this.objectMapper = objectMapper;
		this.allowedOperations = allowedOperations;
		this.linkValidator = linkValidator;
		this.dataServicesCoreProperties = dataServicesCoreProperties;
		this.spelAllowed = spelAllowed;
		super.setRequestInterceptor(this);
		super.getInterceptorList().add(this);
		setAllowExtraParams(false);
		setAllowLessParams(true);
		setErrorResolver(this);
		setRethrowExceptions(false);
		spelProcessor = new JsonNodeSpelProcessor(objectMapper);
		throwableObjectMapper = objectMapper.rebuild().addMixIn(Throwable.class, ThrowableMixin.class).build();
		this.debugRpcResponses = debugRpcResponses;
		this.transactionHandler = transactionHandler;
		this.replicaRoutingEnabled = StringUtils.isNotBlank(environment.getProperty(RpcUtils.REPLICA_URL_PROPERTY));
	}

	private static String operationName(Object o) {
		try {
			return Optional.of(o)
				.map(Object::getClass)
				.map(x -> AnnotationUtils.findAnnotation(x, RemoteOperation.class))
				.map(RemoteOperation::name)
				.orElse(null);
		} catch (NullPointerException e) {
			throw new NullPointerException(String.format("Error getting RemoteOperation annotation on execute() for %s", o.getClass()));
		}
	}

	private static String operationGroup(Object o) {
		return Optional.of(o)
			.map(Object::getClass)
			.map(x -> AnnotationUtils.findAnnotation(x, RemoteOperation.class))
			.map(RemoteOperation::group)
			.orElse(null);
	}

	public ExceptionDetail createExceptionDetail(OperationError error, Throwable t) {
		ExceptionDetail detail = new ExceptionDetail();
		detail.setTitle(error.getShortMessage());
		detail.setDescription(error.getLongMessage());
		detail.setLevel(error.getLevel().toString());
		detail.setTimestamp(Clock.systemUTC().instant());
		detail.setLogId(UUID.randomUUID().toString());
		detail.setSource("");
		detail.setDetails(error.getErrorDetail());

		if (debugRpcResponses) {
			log.error(t.getMessage(), t);
			try {
				detail.setException(throwableObjectMapper.valueToTree(t));
				try (StringWriter sw = new StringWriter()) {
					t.printStackTrace(new PrintWriter(sw));
					detail.setStacktrace(sw.toString());
				} catch (IOException e) {
					detail.setStacktrace("N/A: %s".formatted(e.getMessage()));
				}
			} catch (Exception e) {
				log.error(t.getMessage(), e);
			}
		}

		return detail;
	}

	@Override
	public int handleRequest(InputStream input, OutputStream output) throws IOException {
		if (!replicaRoutingEnabled) {
			return runInTransaction(transactionHandler::runMethodInDefaultTransaction,
				() -> executeRequestInternal(input, output));
		}

		byte[] requestBytes = input.readAllBytes();
		JsonNode jsonRequest = objectMapper.readTree(requestBytes);
		boolean isReadOnly = RpcUtils.isAllOperationsNonMutating(jsonRequest, operations);

		try (InputStream newInput = new ByteArrayInputStream(requestBytes)) {
			if (isReadOnly) {
				return handleRequestInReadOnlyTransaction(newInput, output);
			} else {
				return runInTransaction(transactionHandler::runMethodInDefaultTransaction,
					() -> executeRequestInternal(newInput, output));
			}
		}
	}

	// Must be set before the transaction opens: HibernateJpaDialect acquires the JDBC connection
	// eagerly inside doBegin() to apply read-only settings, before Spring sets the read-only flag.
	private int handleRequestInReadOnlyTransaction(InputStream input, OutputStream output) throws IOException {
		DataSourceContextHolder.setDataSourceType(DataSourceContextHolder.DataSourceType.REPLICA);
		try {
			return runInTransaction(transactionHandler::runMethodInReadOnlyTransaction,
				() -> executeRequestInternal(input, output));
		} finally {
			DataSourceContextHolder.clearDataSourceType();
		}
	}

	private static int runInTransaction(Consumer<Runnable> txRunner, IoSupplier body) throws IOException {
		// AtomicInteger/AtomicReference allow mutation from inside the lambda
		// (variables captured by lambdas must be effectively final).
		AtomicInteger result = new AtomicInteger(0);
		AtomicReference<IOException> captured = new AtomicReference<>();
		txRunner.accept(() -> {
			try {
				result.set(body.get());
			} catch (IOException e) {
				captured.set(e);
			}
		});
		if (captured.get() != null) {
			throw captured.get();
		}
		return result.get();
	}

	@FunctionalInterface
	private interface IoSupplier {
		int get() throws IOException;
	}

	/**
	 * Extracts the operation ID from a single JSON-RPC request node and stores it in {@link OperationContextHolder}
	 * before delegating to the parent implementation. This ensures the operation ID is available in error logging
	 * even when the request targets an operation that does not exist (in which case the request interceptor
	 * would not be reached).
	 */
	@Override
	protected JsonResponse handleJsonNodeRequest(JsonNode node) throws StreamReadException, DatabindException {
		if (!node.isArray()) {
			JsonNode idNode = node.get(ID);
			if (idNode != null && !idNode.isNull()) {
				OperationContextHolder.id(idNode.asText());
			}
		}
		return super.handleJsonNodeRequest(node);
	}

	private int executeRequestInternal(InputStream input, OutputStream output) throws IOException {
		AtomicReference<Throwable> caughtException = new AtomicReference<>();
		try {
			int result = super.handleRequest(input, output);
			if (result != 0) {
				OperationContextHolder.error();
			}
			linkValidator.validateLinks();
			return result;
		} catch (Exception e) {
			OperationContextHolder.error();
			caughtException.set(e);
			throw e;
		} finally {
			if (OperationContextHolder.isFailed()) {
				 // When replica routing is enabled,  IllegalStateException is thrown if no transaction is active
				if (TransactionSynchronizationManager.isActualTransactionActive()) {
					TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
				}
				Throwable failure = caughtException.get();
				log.warn("RPC operation [{}] failed; rollback triggered: {}",
					OperationContextHolder.id(), failure != null ? failure.getMessage() : "no exception");
			}
			linkValidator.clearLinks();
			OperationContextHolder.clear();
			LoadedDocumentReferencesContextHolder.clear();
		}
	}

	@Override public void setRequestInterceptor(RequestInterceptor requestInterceptor) {
		this.requestInterceptor = requestInterceptor;
	}

	@Override
	protected Class<?>[] getHandlerInterfaces(String serviceName) {
		Optional<Object> optHandler = Optional.ofNullable(operations.get(serviceName));
		if (optHandler.isEmpty()) {
			log.info("RPC method: {} doesn't exist or is not allowed", serviceName);
		}

		return optHandler
			.map(handler -> handler instanceof TargetClassAware targetClassAwareHandler ? targetClassAwareHandler.getTargetClass() : handler.getClass())
			.map(aClass -> new Class<?>[] { aClass })
			.orElse(new Class<?>[] {});
	}

	@Override
	protected String getServiceName(String methodName) {
		Matcher matcher = METHOD_WITH_VERSION.matcher(methodName);
		return (matcher.matches()) ? matcher.group(1) : methodName;
	}

	@Override
	protected String getMethodName(String methodName) {
		Matcher matcher = METHOD_WITH_VERSION.matcher(methodName);
		return (matcher.matches()) ? matcher.group(2) : METHOD_NAME_EXECUTE;
	}

	@Override
	protected Object getHandler(String serviceName) {
		if (OperationContextHolder.isFailed()) {
			throw new PreviousOperationFailedException(ExceptionKeys.RPC_OPERATION_PREVIOUS_FAILED_KEY, "Previous operation failed.");
		}
		return operations.get(serviceName);
	}

	/**
	 * Order -100 was chosen to be executed before the initialization listener (DataServicesCoreInitializationListener), which
	 * has 100 as its Order value.
	 * To execute your listener between this and the initialization, choose a value from -99 to 99.
	 */
	@Order(-100)
	@CommonDataServicesEventListener public void handleContextRefresh(ContextRefreshedEvent event) {
		final boolean isAllowingAllRpcOperations = allowAllRpcOperations();

		operations = event.getApplicationContext().getBeansWithAnnotation(RemoteOperation.class).values()
			.stream()
			.filter(operation -> isAllowingAllRpcOperations || isOperationAllowed(operation))
			.collect(Collectors.toMap(JsonRpcOperationDispatcher::operationName, Function.identity()));
	}

	@Override
	public void interceptRequest(JsonNode request) throws Throwable {
		sanitizeRequest(request);
		String operationId = getOperationId(request);
		if (operationId == null) {
			throw new InvalidInputException(RPC_ERROR_EXCEPTION_CODE, RPC_ID_NULL_ERROR_KEY, "'id' property must not be null");
		}
		OperationContextHolder.id(getOperationId(request));
		if (requestInterceptor != null) {
			requestInterceptor.interceptRequest(request);
		}
	}

	private void sanitizeRequest(JsonNode request) {
		boolean containsNulCharacter = request.toString().contains("\\u0000");
		if (containsNulCharacter) {
			throw new InvalidInputException(RPC_ERROR_EXCEPTION_CODE, RPC_OPERATION_ERROR_KEY,
				String.format("Invalid request [id = %s] passed to RPC endpoint, NUL character is not allowed in requests.",
					request.get(JsonRpcBasicServer.ID)));
		}
	}

	private boolean isOperationAllowed(Object operation) {
		String group = operationGroup(operation);
		String name = operationName(operation);
		return allowedOperations.contains(name)
			|| Objects.nonNull(group) && allowedOperations.contains(group);
	}

	private boolean allowAllRpcOperations() {
		if (allowedOperations.contains(DataServicesCoreProperties.MATCH_ALL)) {
			if (allowedOperations.size() == 1) {
				return true;
			}

			log.warn("mgmtp.a12.dataservices.jsonRpc.allowedOperations contain '*' but list size is bigger than 1. No special meaning is applied");
		}

		return false;
	}

	private String getOperationId(JsonNode request) {
		JsonNode node = request.get(JsonRpcBasicServer.ID);
		return node == null || node.isNull() ? null : node.asText();
	}

	/**
	 * Resolves exceptions from RPC operations into JSON-RPC error responses.
	 * Integrates with `OperationContextHolder` for RPC lifecycle management and provides structured error details.
	 * 
	 * @param t the exception thrown during RPC operation execution
	 * @param method the RPC method that encountered the exception  
	 * @param arguments the JSON arguments passed to the RPC method
	 * @return JSON-RPC error object for client response
	 */
	@Override
	public JsonError resolveError(Throwable t, Method method, List<JsonNode> arguments) {
		OperationContextHolder.error();
		OperationError operationError = createOperationError(t, OperationContextHolder.id());
		ExceptionDetail exceptionDetail = createExceptionDetail(operationError, t);
		log.warn("RPC operation [{}] failed: {}", operationError.getOperationId(), t.getMessage());
		return new JsonError(operationError.getCode(), exceptionDetail.getTitle().getDefaultMessage(), exceptionDetail);
	}

	private String sanitizeOperationId(String operationId) {
		return Optional.ofNullable(operationId)
			.filter(StringUtils::isNotBlank)
			.orElse(UNKNOWN);
	}

	private OperationError createOperationError(Throwable e, String operationId) {
		String oid = sanitizeOperationId(operationId);
		if (e instanceof RpcException rpcException) {
			return rpcException.getOperationError().withOperationId(oid);
		} else {
			OperationError.OperationErrorBuilder builder = e instanceof BaseException baseException
				? createOperationErrorBuilder(baseException, oid)
				: createOperationErrorBuilder(e, oid);
			return builder.build();
		}
	}

	private OperationError.OperationErrorBuilder createOperationErrorBuilder(Throwable e, String oid) {
		int exceptionCode = e instanceof AccessDeniedException ? ACCESS_DENIED_EXCEPTION_CODE : RPC_ERROR_EXCEPTION_CODE;
		String longMessageKey = e instanceof AccessDeniedException
			? ExceptionKeys.SECURITY_NOT_AUTHORIZED_ERROR_KEY
			: ExceptionKeys.INVALID_INPUT_ERROR_KEY;
		return OperationError.builder()
			.operationId(oid)
			.code(exceptionCode)
			.level(ErrorLevel.ERROR)
			.genericMessage()
			.errorDetail(new ErrorDetail(exceptionCode, "GENERAL", OffsetDateTime.now()))
			.shortMessage(new LocalizedEntry(RPC_OPERATION_ERROR_KEY, RPC_ERROR_MESSAGE))
			.longMessage(new LocalizedEntry(longMessageKey, e.getMessage()));
	}

	private OperationError.OperationErrorBuilder createOperationErrorBuilder(BaseException e, String oid) {
		e.updateShortMessage(RPC_OPERATION_ERROR_KEY, RPC_ERROR_MESSAGE);
		return OperationError.builder()
			.operationId(oid)
			.code(e.getCode())
			.level(getLevel(e))
			.genericMessage()
			.errorDetail(getErrorDetail(e.getErrorDetail()))
			.shortMessage(e.getShortMessage())
			.longMessage(e.getLongMessage());
	}

	private ErrorDetail getErrorDetail(ErrorDetail errorDetail) {
		return Optional.ofNullable(errorDetail)
			.map(ed -> new ErrorDetail(ed.getCode(), ed.getSubsystem(), ed.getTime()))
			.orElse(null);
	}

	private ErrorLevel getLevel(BaseException e) {
		return ErrorLevel.valueOf(e.getLevel().name());
	}

	@Override
	public void preHandle(Object target, Method method, List<JsonNode> params) {
		if (spelAllowed) {
			params.replaceAll(spelProcessor::evaluateSpel);
		}
	}

	@Override
	public void preHandleJson(JsonNode json) {
		int methodCallCount = json.isArray() ? json.size() : 1;

		if (methodCallCount > dataServicesCoreProperties.getJsonRpc().getMaxMethodCallsPerRequest()) {
			throw RpcExceptionSupport.createException(
				ExceptionCodes.LIMIT_OF_RPC_OPERATIONS_EXCEEDED_ERROR_CODE,
				ExceptionKeys.RPC_NUMBER_OF_OPERATIONS_EXCEEDED_ERROR_KEY,
				"Maximum number of operations per single RPC request exceeded",
				"Maximum number of operations per single RPC request exceeded");
		}
	}

	@Override
	public void postHandle(Object target, Method method, List<JsonNode> params, JsonNode result) {
		//skip
	}

	@Override
	public void postHandleJson(JsonNode json) {
		//skip
	}

	public static class PreviousOperationFailedException extends BaseException {
		PreviousOperationFailedException(String key, String message) {
			super(ExceptionCodes.OPERATION_FAILED_EXCEPTION_CODE, key, message);
		}
	}

	@SuppressWarnings({ "WeakerAccess", "unused" })
	static class NoCloseOutputStream extends OutputStream {

		private final OutputStream ops;
		private boolean closeAttempted = false;

		public NoCloseOutputStream(OutputStream ops) {
			this.ops = ops;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void write(int b) throws IOException {
			this.ops.write(b);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void write(byte[] b) throws IOException {
			this.ops.write(b);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			this.ops.write(b, off, len);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void flush() throws IOException {
			this.ops.flush();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override public void close() {
			closeAttempted = true;
		}

		/**
		 * @return the closeAttempted
		 */
		public boolean wasCloseAttempted() {
			return closeAttempted;
		}

	}

	private interface ThrowableMixin {

		@JsonIgnore
		StackTraceElement[] getStackTrace();

		@JsonIgnore
		Throwable getCause();

		@JsonIgnore
		Throwable[] getSuppressed();
	}

}
