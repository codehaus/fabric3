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
package org.fabric3.fabric.services.contribution.manifest;

import javax.xml.namespace.QName;

import org.fabric3.spi.Constants;
import org.fabric3.spi.services.contribution.Import;

/**
 * Represents an <code>import.java</code> entry in a contribution manifest.
 *
 * @version $Rev$ $Date$
 */
public class JavaImport extends Import {
    private static final long serialVersionUID = -7863768515125756048L;
    private static final QName TYPE = new QName(Constants.FABRIC3_NS, "java");
    private String packageName;

    public JavaImport(String namespace) {
        this.packageName = namespace;
    }

    public String getPackageName() {
        return packageName;
    }

    public QName getType() {
        return TYPE;
    }


    public String toString() {
        return "Import package [" + packageName + "]";
    }
}
