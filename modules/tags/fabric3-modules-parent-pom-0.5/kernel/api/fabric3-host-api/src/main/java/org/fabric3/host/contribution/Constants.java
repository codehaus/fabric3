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
package org.fabric3.host.contribution;

import javax.xml.namespace.QName;

/**
 * Definitions of constants.
 *
 * @version $Rev$ $Date$
 */
public final class Constants {
    /**
     * A changeSet represented as XML.
     */
    public static final String CHANGESET_XML = "application/x-fabric3.fabric3.changeSet+xml";
    public static final String ZIP_CONTENT_TYPE = "application/zip";
    public static final String FOLDER_CONTENT_TYPE = "application/vnd.fabric3.folder";
    public static final String COMPOSITE_CONTENT_TYPE = "text/vnd.fabric3.composite+xml";
    public static final String DEFINITIONS_TYPE = "text/vnd.fabric3.definitions+xml";
    public static final String JAVA_CONTENT_TYPE = "application/java-vm";
    public final static String CONTENT_UNKONWN = "content/unknown";
    public final static String CONTENT_DEFAULT = "application/octet-stream";
    
    public static final String URI_PREFIX = "sca://contribution/";

    public final static QName COMPOSITE_TYPE = new QName("http://www.osoa.org/xmlns/sca/1.0", "composite");

    private Constants() {
    }

}
