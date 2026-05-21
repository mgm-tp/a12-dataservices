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
import { execSync } from "node:child_process";
import { cpSync, readdirSync, readFileSync, rmSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = dirname(fileURLToPath(import.meta.url));

const ROOT_DIR = join(__dirname, "..");
const TEMP_DIR = join(ROOT_DIR, "target", "temp");
const FIXTURES_DIR = join(ROOT_DIR, "test", "__fixtures__");
const BIN_PATH = join(ROOT_DIR, "bin", "relationship-model-migration");

describe("com.mgmtp.a12.dataservices-model-migration.steps", () => {
	beforeAll(() => {
		rmSync(TEMP_DIR, { recursive: true, force: true });
		cpSync(FIXTURES_DIR, TEMP_DIR, { recursive: true });
	});

	test("can migrate models to the next version properly", () => {
		execSync(`${BIN_PATH} ${TEMP_DIR} --next`);

		for (const filePath of readdirSync(TEMP_DIR)) {
			expect(readFileSync(join(TEMP_DIR, filePath), "utf8")).toMatchSnapshot(filePath);
		}
	});
});
