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
package org.fabric3.jpa.provider;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.spi.PersistenceUnitInfo;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.fabric3.jpa.Fabric3JpaException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @version $Revision$ $Date$
 */
public class ClasspathPersistenceUnitScanner implements PersistenceUnitScanner {

    private static DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    
    private Map<String, PersistenceUnitInfo> persistenceUnitInfos = new HashMap<String, PersistenceUnitInfo>();

    /**
     * @see org.fabric3.jpa.provider.PersistenceUnitScanner#getPersistenceUnitInfo(java.lang.String, java.lang.ClassLoader)
     */
    public PersistenceUnitInfo getPersistenceUnitInfo(String unitName, ClassLoader classLoader) {

        synchronized (persistenceUnitInfos) {

            if (persistenceUnitInfos.containsKey(unitName)) {
                return persistenceUnitInfos.get(unitName);
            }

            try {

                DocumentBuilder db = dbf.newDocumentBuilder();

                Enumeration<URL> persistenceUnitUrls = classLoader.getResources("META-INF/persistence.xml");

                while (persistenceUnitUrls.hasMoreElements()) {

                    URL persistenceUnitUrl = persistenceUnitUrls.nextElement();
                    Document persistenceDom = db.parse(persistenceUnitUrl.openStream());

                    String rootJarUrl = persistenceUnitUrl.toString();
                    rootJarUrl = rootJarUrl.substring(0, rootJarUrl.lastIndexOf("META-INF"));

                    PersistenceUnitInfoImpl info = new PersistenceUnitInfoImpl(persistenceDom, classLoader, rootJarUrl);
                    if (unitName.equals(info.getPersistenceUnitName())) {
                        persistenceUnitInfos.put(unitName, info);
                        return info;
                    }

                }

            } catch (IOException ex) {
                throw new Fabric3JpaException(ex);
            } catch (ParserConfigurationException ex) {
                throw new Fabric3JpaException(ex);
            } catch (SAXException ex) {
                throw new Fabric3JpaException(ex);
            }

        }

        throw new Fabric3JpaException("Unable to find persistence unit: " + unitName);

    }

}
