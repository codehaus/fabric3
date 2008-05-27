/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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
package org.fabric3.spi.services.contribution;

import javax.xml.namespace.QName;

import org.fabric3.spi.Constants;

/**
 * A QName-based contribution export
 *
 * @version $Rev$ $Date$
 */
public class QNameExport extends Export {
    private static final long serialVersionUID = -6813997109078522174L;
    private static final QName TYPE = new QName(Constants.FABRIC3_NS, "qname");
    private QName namespace;

    public QNameExport(QName namespace) {
        this.namespace = namespace;
    }

    public QName getNamespace() {
        return namespace;
    }

    public int match(Import contributionImport) {
        if (contributionImport instanceof QNameImport
                && ((QNameImport) contributionImport).getNamespace().equals(namespace)) {
            return EXACT_MATCH;
        }
        return NO_MATCH;
    }

    public QName getType() {
        return TYPE;
    }

}
