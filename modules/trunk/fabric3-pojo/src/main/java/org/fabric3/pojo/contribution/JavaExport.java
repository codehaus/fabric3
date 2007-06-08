/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.pojo.contribution;

import javax.xml.namespace.QName;

import org.fabric3.spi.Constants;
import org.fabric3.spi.services.contribution.Export;
import org.fabric3.spi.services.contribution.Import;

/**
 * @version $Rev$ $Date$
 */
public class JavaExport extends Export {
    private static final long serialVersionUID = -1362112844218693711L;
    private static final QName TYPE = new QName(Constants.FABRIC3_NS, "java");
    private String packageName;

    public JavaExport(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public int match(Import contributionImport) {
        if (contributionImport instanceof JavaImport
                && ((JavaImport) contributionImport).getPackageName().startsWith(packageName)) {
            return EXACT_MATCH;
        }
        return NO_MATCH;
    }

    public QName getType() {
        return TYPE;
    }
}

