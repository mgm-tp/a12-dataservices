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
package com.mgmtp.a12.dataservices.authorization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.javers.core.diff.custom.CustomPropertyComparator;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.expression.PropertyAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.mgmtp.a12.dataservices.AbstractDataServicesCoreTest;
import com.mgmtp.a12.dataservices.attachment.AttachmentHeader;
import com.mgmtp.a12.dataservices.configuration.DataServicesCoreProperties;
import com.mgmtp.a12.dataservices.uaa.UaaTestHelper;
import com.mgmtp.a12.model.header.Annotation;
import com.mgmtp.a12.model.header.Header;
import com.mgmtp.a12.uaa.authorization.AuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.AuthorizationService;
import com.mgmtp.a12.uaa.authorization.internal.InMemoryAuthorizationDefinitionDataHolder;
import com.mgmtp.a12.uaa.authorization.internal.RuntimeAuthorizationDefinitionRepository;
import com.mgmtp.a12.uaa.authorization.model.AuthorizationDefinition;
import com.mgmtp.a12.uaa.authorization.security.PolicyProcessorFactory;
import com.mgmtp.a12.uaa.authorization.security.PropertyChangesChecker;
import com.mgmtp.a12.uaa.authorization.security.spel.internal.SpelPolicyProcessorFactory;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

public class AuthorizationScopeTest extends AbstractDataServicesCoreTest {

	private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

	private AuthorizationDefinitionRepository authorizationDefinitionRepository = new RuntimeAuthorizationDefinitionRepository();
	private final ObjectMapper mapper = JsonMapper.builder()
		.withConfigOverride(Object.class, cfg -> cfg.setMergeable(false))
		.build();

	@Mock private SecurityContext securityContext;
	@Mock private ApplicationContext applicationContext;
	@Mock public Authentication authentication;
	@Mock private PropertyAccessor propertyAccessor;
	@Spy private List<PolicyProcessorFactory> processorFactories = new ArrayList<>();
	@Spy private Optional<List<CustomPropertyComparator>> propertyComparators = Optional.empty();

	@Spy private DataServicesCoreProperties dataServicesCoreProperties = Mockito.spy(new DataServicesCoreProperties());
	@InjectMocks private PropertyChangesChecker propertyChangesChecker = new PropertyChangesChecker(Arrays.asList("com.mgmtp"));

	@InjectMocks private AuthorizationService authorizationService;

	@BeforeClass
	void beforeClass() throws IOException {
		AuthorizationDefinition authorizationDefinition =
			mapper.readValue(resourcePatternResolver.getResource("/uaa/authorizationDefinition.json").getInputStream(), AuthorizationDefinition.class);
		InMemoryAuthorizationDefinitionDataHolder.initNewData(authorizationDefinition, new AuthorizationDefinition());
	}

	@BeforeMethod
	void beforeMethod() throws IllegalAccessException {
		Mockito.reset(authentication);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		FieldUtils.writeField(authorizationService, "applicationContext", applicationContext, true);
		List<PropertyAccessor> propertyAccessors = new ArrayList<>();
		propertyAccessors.add(propertyAccessor);
		Optional<List<PropertyAccessor>> oPropertyAccessors = Optional.of(propertyAccessors);
		FieldUtils.writeField(authorizationService, "propertyAccessors", oPropertyAccessors, true);
		processorFactories.add(new SpelPolicyProcessorFactory());
		propertyChangesChecker.initJavers();
		FieldUtils.writeField(authorizationService, "policyProcessorFactories", processorFactories, true);
		FieldUtils.writeField(authorizationService, "authorizationDefinitionRepository", authorizationDefinitionRepository, true);
		Mockito.lenient().when(applicationContext.getBean("dataServicesCoreProperties")).thenReturn(dataServicesCoreProperties);
		dataServicesCoreProperties.getAuthorization().getRoleBased().setEnabled(true);
	}

	@DataProvider public Object[][] needHaveModelRoleActions() {
		return new Object[][] {
			new Object[] { AuthConstants.MODEL_READ_PERMISSION, "MODEL_READ" },
			new Object[] { AuthConstants.MODEL_UPDATE_PERMISSION, "MODEL_UPDATE" },
			new Object[] { AuthConstants.MODEL_DELETE_PERMISSION, "MODEL_DELETE" },
		};
	}

	@DataProvider public Object[][] allModelActions() {
		return new Object[][] {
			new Object[] { AuthConstants.MODEL_READ_PERMISSION },
			new Object[] { AuthConstants.MODEL_CREATE_PERMISSION },
			new Object[] { AuthConstants.MODEL_UPDATE_PERMISSION },
			new Object[] { AuthConstants.MODEL_DELETE_PERMISSION },
		};
	}

	@DataProvider public Object[][] permissionActions() {
		return new Object[][] {
			new Object[] { AuthConstants.DOCUMENT_CREATE_PERMISSION, "DOCUMENT_CREATE", true, makeTestDsDocument() },
			new Object[] { AuthConstants.DOCUMENT_CREATE_PERMISSION, "DOCUMENT_CREATE1,document_create,DOCUMENT_UPDATE", false, makeTestDsDocument() },
			new Object[] { AuthConstants.DOCUMENT_QUERY_PERMISSION, "QUERY", true, makeTestDsDocument() },
			new Object[] { AuthConstants.DOCUMENT_UPDATE_PERMISSION, "DOCUMENT_UPDATE", true, makeTestDsDocument() },
			new Object[] { AuthConstants.DOCUMENT_UPDATE_PERMISSION, "DOCUMENT_uPDATE,DOCUMENT_CREATE,DOCUMENT_DELETE", false, makeTestDsDocument() },
			new Object[] { AuthConstants.DOCUMENT_DELETE_PERMISSION, "DOCUMENT_DELETE", true, makeTestDsDocument() },
			new Object[] { AuthConstants.DOCUMENT_DELETE_PERMISSION, "DOCUMENT_UPDATE,DOCUMENT_CREATE", false, makeTestDsDocument() },
			new Object[] { AuthConstants.DOCUMENT_PARTIAL_UPDATE_PERMISSION, "DOCUMENT_PARTIAL_UPDATE", true, makeTestDsDocument() },
			new Object[] { AuthConstants.DOCUMENT_PARTIAL_UPDATE_PERMISSION, "DOCUMENT_UPDATE,DOCUMENT_CREATE", false, makeTestDsDocument() },
			new Object[] { AuthConstants.DOCUMENT_MULTI_DELETE_PERMISSION, "DOCUMENT_MULTI_DELETE", true, makeTestDsDocument() },
			new Object[] { AuthConstants.DOCUMENT_MULTI_DELETE_PERMISSION, "DOCUMENT_UPDATE,DOCUMENT_CREATE,DOCUMENT_PARTIAL_UPDATE", false,
				makeTestDsDocument() },
			new Object[] { AuthConstants.ATTACHMENT_UPLOAD_PERMISSION, "ATTACHMENT_UPLOAD", true,
				AttachmentHeader.builder().attachmentId(UUID.randomUUID().toString()).build() },
			new Object[] { AuthConstants.ATTACHMENT_UPLOAD_PERMISSION, "ATTACHMENT_UPLOAD1,DOCUMENT_UPDATE,MANAGE_CACHES", false,
				AttachmentHeader.builder().attachmentId(UUID.randomUUID().toString()).build() },
			new Object[] { "RelativePath", "", false, null },
			new Object[] { "RelativePath", "ACCESS_ACTUATOR", true, null },
			new Object[] { "Endpoint", "", true, null },
			new Object[] { AuthConstants.EXPORT_LIST_CDD_PERMISSION, "", true, null },
		};
	}

	@Test(dataProvider = "allModelActions")
	void testModelPermission_disableRoleBase_allModelOperationSuccessfully(String scope) {
		dataServicesCoreProperties.getAuthorization().getRoleBased().setEnabled(false);
		Header header = makeTestModelHeader();
		mockUserDetail(List.of(RandomStringUtils.randomAlphabetic(5)));

		Assert.assertTrue(authorizationService.checkPermissions(header, scope)
			.isPassed()
		);
	}

	@Test(dataProvider = "allModelActions")
	void testModelPermission_withAccessRightModelManage_allModelOperationSuccessfully(String scope) {
		String roleName = RandomStringUtils.randomAlphabetic(10);
		List<Annotation> annotations = List.of(new TestAnnotation("roles", roleName));
		Header header = makeTestModelHeader(RandomStringUtils.randomAlphabetic(7), annotations);

		mockUserDetail(List.of("MODEL_MANAGE"));

		Assert.assertTrue(authorizationService.checkPermissions(header, scope)
			.isPassed()
		);
	}

	@Test void testCreateModel_withAccessRightMODEL_CREATE_and_roles_successfully() {
		String roleName = RandomStringUtils.randomAlphabetic(10);
		List<Annotation> annotations = List.of(new TestAnnotation("roles", roleName));
		Header header = makeTestModelHeader(RandomStringUtils.randomAlphabetic(7), annotations);
		Header withoutRolesHeader = makeTestModelHeader();

		mockUserDetail(roleName, List.of("MODEL_CREATE"));

		Assert.assertTrue(authorizationService.checkPermissions(header, AuthConstants.MODEL_CREATE_PERMISSION)
			.isPassed()
		);
		Assert.assertTrue(authorizationService.checkPermissions(withoutRolesHeader, AuthConstants.MODEL_CREATE_PERMISSION)
			.isNotPassed()
		);
	}

	@Test void testCreateModel_withoutAccessRightMODEL_CREATE_notPassed() {
		String roleName = RandomStringUtils.randomAlphabetic(10);
		List<Annotation> annotations = List.of(new TestAnnotation("roles", roleName));
		Header header = makeTestModelHeader(RandomStringUtils.randomAlphabetic(7), annotations);

		mockUserDetail(roleName, List.of("MODEL_CREATE1, MODEL_UPDATE, MODEL_READ, MODEL_DELETE"));

		Assert.assertTrue(authorizationService.checkPermissions(header, AuthConstants.MODEL_CREATE_PERMISSION)
			.isNotPassed()
		);
	}

	@Test(dataProvider = "needHaveModelRoleActions")
	void testUpdateModel_withAccessRight_belongToRoles_passed(String scope, String accessRight) {
		String roleName = RandomStringUtils.randomAlphabetic(10);
		List<Annotation> annotations = List.of(new TestAnnotation("roles", roleName));
		Header header = makeTestModelHeader(RandomStringUtils.randomAlphabetic(7), annotations);
		Header withoutRolesHeader = makeTestModelHeader();

		mockUserDetail(roleName, List.of(accessRight));

		Assert.assertTrue(authorizationService.checkPermissions(header, scope)
			.isPassed()
		);
		Assert.assertTrue(authorizationService.checkPermissions(withoutRolesHeader, scope)
			.isNotPassed()
		);
	}

	@Test(dataProvider = "needHaveModelRoleActions")
	void testUpdateModel_withAccessRightNotBelongToModelRoles_failed(String scope, String accessRight) {
		String roleName1 = "roleName1";
		String roleName2 = "roleName2";
		String roleDoesNotBelongToModel = "roleDoesNotBelongToModel";
		List<Annotation> annotations = List.of(new TestAnnotation("roles", "%s,%s".formatted(roleName1, roleName2)));

		// Model have `roleName1` and `roleName2`
		Header header = makeTestModelHeader(RandomStringUtils.randomAlphabetic(7), annotations);

		List<GrantedAuthority> authorities = List.of(
			createGrantedAuthority(roleName1, List.of(RandomStringUtils.randomAlphabetic(10))),
			createGrantedAuthority(roleDoesNotBelongToModel, List.of(accessRight))
		);

		// Role `roleDoesNotBelongToModel` have model action but does not belong to model then check failed.
		mockUserDetailWithAuthority(authorities);

		Assert.assertTrue(authorizationService.checkPermissions(header, scope)
			.isNotPassed()
		);
	}

	@Test(dataProvider = "permissionActions")
	void testCheckUploadPermission_success(String scope, String accessRight, boolean result, Object resource) {

		UaaTestHelper.TestUserDetails userDetails = mockUserDetail(List.of(accessRight));
		if (resource == null) {
			resource = userDetails;
		}

		Assert.assertEquals(
			authorizationService.checkPermissions(resource, scope).isPassed(),
			result
		);
	}

	public GrantedAuthority createGrantedAuthority(String roleName, Collection<String> accessRights) {
		UaaTestHelper.TestGrantedAuthority grantedAuthority = new UaaTestHelper.TestGrantedAuthority();
		grantedAuthority.setName(roleName);
		grantedAuthority.setAccessRights(accessRights.stream()
			.map(UaaTestHelper.TestAccessRight::new)
			.toList()
		);
		return grantedAuthority;
	}

	public UaaTestHelper.TestUserDetails mockUserDetail(Collection<String> accessRights) {
		return mockUserDetail(RandomStringUtils.randomAlphabetic(10), accessRights);
	}

	public UaaTestHelper.TestUserDetails mockUserDetail(String roleName, Collection<String> accessRights) {
		return this.mockUserDetailWithAuthority(List.of(createGrantedAuthority(roleName, accessRights)));
	}

	public UaaTestHelper.TestUserDetails mockUserDetailWithAuthority(Collection<GrantedAuthority> authorities) {
		UaaTestHelper.TestUserDetails userDetails = UaaTestHelper.createUser();
		userDetails.setAuthorities(authorities);
		Mockito.when(authentication.getPrincipal()).thenReturn(userDetails);
		return userDetails;
	}

}
