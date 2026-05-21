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
package com.mgmtp.a12.dataservices.server.internal.rest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mgmtp.a12.dataservices.api.common.rest.NoCache;
import com.mgmtp.a12.dataservices.common.exception.UnexpectedException;
import com.mgmtp.a12.dataservices.exception.ExceptionCodes;
import com.mgmtp.a12.dataservices.exception.ExceptionKeys;
import com.mgmtp.a12.dataservices.model.GenericModel;
import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.dataservices.model.bulkload.BulkImporterConfiguration;
import com.mgmtp.a12.dataservices.model.bulkload.ModelBulkImporter;
import com.mgmtp.a12.dataservices.model.document.SecuredValidationCodeGenerator;
import com.mgmtp.a12.dataservices.server.uaa.SecuredController;
import com.mgmtp.a12.dataservices.experimental.ListIProblemReporter;

import lombok.RequiredArgsConstructor;

/**
 * API to create, read, update and delete models.
 * All below-mentioned endpoints work only with JSON, and they share context path `/v2/models`. All models that adhere to the
 * metadata definition can be persisted and served via following REST endpoints. No other models will be accepted.
 *
 * All below-mentioned CRUD operations are extensible via <<i-model-repository,IModelRepository>> concept.
 *
 * @topic Models
 * @title Models REST API V2
 */
@RequestMapping("#{@dataServicesCoreProperties.server.contextPath}/v2/models")
@RequiredArgsConstructor
@SecuredController
@RestController public class ModelsV2ControllerImpl {

	static final String MODEL_ID_PARAM = "model-id";

	private final ModelService modelService;
	private final ModelBulkImporter bulkImporter;
	private final SecuredValidationCodeGenerator securedValidationCodeGenerator;

	/**
	 * @param modelId Required Model to load.
	 * @return Content of the requested model.
	 * @title Load Model
	 * @authorizationScope Model Read
	 * @responseSuccess 200 OK:: The response contains a body with the persisted model.
	 */
	@NoCache
	@GetMapping(path = "/{" + MODEL_ID_PARAM + "}", produces = { MediaType.APPLICATION_JSON_VALUE })
	public GenericModel loadModel(@PathVariable(MODEL_ID_PARAM) String modelId) {
		return modelService.load(modelId);
	}

	/**
	 * @param modelContent Content of the model to update.
	 * @return Content of the persisted model.
	 * @title Update Model
	 * @authorizationScope Model Update
	 * @responseSuccess 200 OK:: The response contains the persisted model. Please note that this model might be
	 * different to the model that was sent because of custom extensions which are able to change the model before saving.
	 * @responseError 400 Bad Request:: Model validation failed. Model is not acceptable -> The payload of the request
	 * is not a valid A12 model.
	 * @note The roles annotation of the model is mandatory. Without a role definition the server will not be able to
	 * persist the model.
	 */
	@PutMapping(path = { "", "/" }, produces = { MediaType.APPLICATION_JSON_VALUE }, consumes = { MediaType.APPLICATION_JSON_VALUE })
	@Transactional
	public GenericModel updateModel(@RequestBody String modelContent) {
		return modelService.update(modelContent);
	}

	/**
	 * @param modelContent Content of the model to create.
	 * @return Persisted model.
	 * @title Create Model
	 * @authorizationScope Model Create
	 * @responseSuccess 200 OK::
	 * The response contains the persisted model. Please note that this model might be
	 * different to the model that was send because of custom extensions which are able to change the model before saving.
	 * @responseError 400 Bad Request:: Model validation failed. Model is not acceptable -> The payload of the request
	 * is not a valid A12 model.
	 * 409 Conflict:: Model creating failure -> Model might be already created.
	 * @note The roles annotation of the model is mandatory. Without a role definition the server will not be able to
	 * persist the model.
	 */
	@PostMapping(path = { "", "/" }, produces = { MediaType.APPLICATION_JSON_VALUE }, consumes = { MediaType.APPLICATION_JSON_VALUE })
	@Transactional
	public GenericModel createModel(Reader modelContent) {
		try {
			return modelService.create(IOUtils.toString(modelContent));
		} catch (IOException e) {
			throw new UnexpectedException(e).withAnonymityMessage("Create model failed.");
		}
	}

	/**
	 * @param modelId Required Model to delete.
	 * @return True if the model was deleted.
	 * @title Delete Model
	 * @authorizationScope Model Delete
	 * @responseSuccess 200 OK:: If model was deleted or if model with `model-id` does not exist anymore.
	 * @example `Product`
	 */
	@NoCache
	@DeleteMapping(path = "/{" + MODEL_ID_PARAM + "}")
	@Transactional
	public boolean deleteModel(@PathVariable(MODEL_ID_PARAM) String modelId) {
		return modelService.delete(modelId);
	}

	/**
	 *
	 * @param modelName The model to be validated.
	 * @return Validation code converted to String.
	 * @title Generate Validation Code
	 * @authorizationScope Model Read
	 */
	@NoCache
	@GetMapping(path = "/{" + MODEL_ID_PARAM + "}/validationCode", produces = { "application/javascript;charset=UTF-8", MediaType.APPLICATION_JSON_VALUE })
	public String generateValidationCode(@PathVariable(MODEL_ID_PARAM) String modelName) {
		ListIProblemReporter pr = new ListIProblemReporter();
		String result = securedValidationCodeGenerator.generateValidationCode(modelName, pr);
		pr.validate(ExceptionCodes.VALIDATION_CODES_GENERATION_EXCEPTION_CODE, ExceptionKeys.VALIDATION_CODES_GENERATION_ERROR_KEY, "Error while validation codes generation");
		return result;
	}

	/**
	 * Endpoint allowing a bulk import of models to database.
	 *
	 * @param modelBulk Stream of zip of models.
	 * @return List of imported model IDs.
	 * @title Import Models
	 * @headers Content-type:: `application/octet-stream`
	 * @authorizationScope Model Update
	 * @authorizationScope Model Create
	 * @responseSuccess 200 OK:: The response contains list of the names of all created models.
	 * @responseError 400 Bad Request::Model validation failed. Model is not acceptable -> The payload of the request
	 * is not a valid A12 model.
	 */
	@PutMapping(path = { "", "/" }, produces = { MediaType.APPLICATION_JSON_VALUE })
	@Transactional
	public List<String> importModelBulk(InputStream modelBulk) {
		File tempFile = makeTempFile();
		try {
			FileUtils.copyInputStreamToFile(modelBulk, tempFile);
		} catch (IOException e) {
			throw new UnexpectedException(ExceptionKeys.FILE_SYSTEM_IO_ERROR_KEY, "Unable to save bulk to tempfile");
		}
		return importModelBulkInternal(tempFile);
	}

	private List<String> importModelBulkInternal(File tempFile) {
		try {
			return bulkImporter.doImport(getBulkLocation(tempFile), new BulkImporterConfiguration());
		} catch (URISyntaxException e) {
			throw new UnexpectedException(ExceptionKeys.URI_FORMATION_ERROR_KEY, "Invalid base URI of bulk");
		} catch (IOException e) {
			throw new UnexpectedException(ExceptionKeys.FILE_SYSTEM_IO_ERROR_KEY, "Problem getting files in bulk");
		} finally {
			FileUtils.deleteQuietly(tempFile);
		}
	}

	private String getBulkLocation(File tempFile) {
		return tempFile.toURI().toString();
	}

	private File makeTempFile() {
		try {
			File tempFile = File.createTempFile("bulkimport", ".zip", FileUtils.getTempDirectory());
			tempFile.deleteOnExit();
			return tempFile;
		} catch (IOException e) {
			throw new UnexpectedException(ExceptionKeys.FILE_SYSTEM_IO_ERROR_KEY, "Unable to make temporary file");
		}
	}
}
