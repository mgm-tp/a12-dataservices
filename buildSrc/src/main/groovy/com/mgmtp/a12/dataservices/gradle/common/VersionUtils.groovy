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
package com.mgmtp.a12.dataservices.gradle.common

import groovy.text.StreamingTemplateEngine
import org.gradle.api.GradleException
import org.gradle.util.internal.VersionNumber

class VersionUtils {

    /**
     * Groups:
     *  <major>, <minor>, <patch>: 0 or integer number
     *      Valid input: 0, 1, 11, 113
     *      Invalid input: ds, %^, 01, 02
     *
     *  <prerelease>: pre or rc version with suffix is any numbers. (Optional)
     *      Valid input: pre.1, pre.2, rc.1, rc.2
     *      Invalid input: pre.sdf, prev.01, rc.sdf, rcv.2323
     *
     *  <snapshot>: SNAPSHOT version (Optional)
     *      Valid input: SNAPSHOT
     *
     *  <build>: build version with suffix is any characters. (Optional)
     *      Valid input: build.1.adfdsaf, build.2.123sdf, build.13232323
     *      Invalid input: build123
     *
     * Right format: MAJOR.MINOR.PATCH-(rc|pre|build|SNAPSHOT).number
     *    - number must start from 01
     *    - number is omitted for SNAPSHOT
     *
     * Example:
     *  - 1.1.2
     *  - 1.1.2-SNAPSHOT
     *  - 1.1.2-rc.1
     *  - 1.1.2-rc.2
     *  - 1.1.2-pre.1
     *  - 1.1.2-pre.2
     *  - 1.1.2-build.1
     *  - 1.1.2-build.12.afsedadfs
     */
    static String VERSION_REGEX = /^(?<major>0|[1-9]\d*)\.(?<minor>0|[1-9]\d*)\.(?<patch>0|[1-9]\d*)(?:-(?<prerelease>(?:pre|rc)(?:\.(?:\d+))))?(?:-(?<snapshot>(?:SNAPSHOT)))?(?:-(?<build>(?:build)(?:\.(?:\w*))*))?$/

    static void validateVersion(String version) {
        if (!(version =~ VERSION_REGEX)) {
            throw new GradleException(
                    new StreamingTemplateEngine().createTemplate('''
Invalid version: ${version}.
Please make sure you input the right format: MAJOR.MINOR.PATCH-(rc|pre|build|SNAPSHOT).number 
    * number must start from 1
    * number is omitted for SNAPSHOT
Example: 
    - 1.1.2
    - 1.1.2-SNAPSHOT
    - 1.1.2-rc.1
    - 1.1.2-rc.2
    - 1.1.2-pre.1
    - 1.1.2-pre.2
    - 1.1.2-build.1
    - 1.1.2-build.12.afsedadfs
''').make([version: version]).toString()
            )
        }
    }

    /**
     * Function for comparing dependency version
     * @param source dependency the version need to compare
     * @param g target dependency group
     * @param m target dependency module
     * @param v target dependency version
     * @return boolean true if source dependency version below or equal target dependency version
     */
    static boolean belowVersion(dependency, g, m, v) {
        def sourceVer = VersionNumber.parse(dependency.getVersion())
        def targetVer = VersionNumber.parse(v)
        if (dependency.getGroup() == g && (!m || (dependency.getName() == m || dependency.getModule() == m))) {
            return sourceVer <= targetVer
        }
        return true
    }

    /**
     * Function for comparing dependency version
     * @param source dependency the version need to compare
     * @param g target dependency group
     * @param m target dependency module
     * @param v target dependency version
     * @return boolean true if source dependency version above or equal target dependency version
     */
    static boolean aboveVersion(dependency, g, m, v) {
        def sourceVer = VersionNumber.parse(dependency.getVersion())
        def targetVer = VersionNumber.parse(v)
        if (dependency.getGroup() == g && (!m || (dependency.getName() == m || dependency.getModule() == m))) {
            return sourceVer >= targetVer
        }
        return true
    }

    /**
     * Function for comparing dependency version
     * @param source dependency the version need to compare
     * @param g target dependency group
     * @param m target dependency module
     * @param v target dependency version
     * @return boolean true if source dependency version equal target dependency version
     */
    static boolean equalVersion(dependency, g, m, v) {
        def sourceVer = VersionNumber.parse(dependency.getVersion())
        def targetVer = VersionNumber.parse(v)
        if (dependency.getGroup() == g && (!m || (dependency.getName() == m || dependency.getModule() == m))) {
            return sourceVer == targetVer
        }
        return true
    }
}
