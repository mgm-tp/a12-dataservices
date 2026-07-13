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
import notice from "eslint-plugin-notice";
import unusedImports from "eslint-plugin-unused-imports";

import * as devtoolsConfigs from "@com.mgmtp.a12.devtools/eslint-config";

export default [
	...devtoolsConfigs.strict,
	{
		ignores: [
			"prettier.config.js",
			"eslint.config.js",
			"copyright.js",
			"lib",
			"build",
			"node_modules",
			"**/resources"
		]
	},
	{
		languageOptions: {
			parserOptions: {
				projectService: true
			}
		},
		plugins: {
			notice,
			"unused-imports": unusedImports
		},
		rules: {
			"@typescript-eslint/consistent-type-exports": "error",
			"@typescript-eslint/consistent-type-imports": "error",
			"@typescript-eslint/no-empty-object-type": "warn",
			"@typescript-eslint/no-unsafe-function-type": "warn",
			"@typescript-eslint/no-explicit-any": "warn",
			"@typescript-eslint/no-empty-function": "warn",
			"@typescript-eslint/no-unused-vars": [
				"warn",
				{
					ignoreRestSiblings: true
				}
			],
			"no-inner-declarations": "off",
			eqeqeq: "error",
			"import/no-extraneous-dependencies": "error",
			"unused-imports/no-unused-imports": "error",
			"no-restricted-imports": [
				"error",
				{
					patterns: ["@com.mgmtp.a12*/**/internal/**", "@com.mgmtp.a12*/**/src/**"]
				}
			],
			"notice/notice": [
				"error",
				{
					templateFile: "copyright.js",
					chars: 2000
				}
			]
		}
	}
];
