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
package org.fabric3.fabric.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @version $Revision$ $Date$
 */
public class DefaultConfigLoader implements ConfigLoader {
    
    private static DocumentBuilderFactory FACTORY = DocumentBuilderFactory.newInstance();
    
    /**
     * Loads the configuration from the specified location.
     * 
     * @param configLocation Configuration location URL.
     * @return Configuration loaded as a DOM document instance.
     * @throws ConfigLoadException If unable to load the configuration.
     */
    public Document loadConfig(URL configLocation) throws ConfigLoadException {
        
        InputStream inputStream = null;
        
        try {
            
            DocumentBuilder builder = FACTORY.newDocumentBuilder();
            
            inputStream = configLocation.openStream();
            return builder.parse(inputStream);
            
        } catch (ParserConfigurationException e) {
            throw new ConfigLoadException("Document builder not configured", configLocation.toExternalForm(), e);
        } catch (IOException e) {
            throw new ConfigLoadException("Unable to open URL", configLocation.toExternalForm(), e);
        } catch (SAXException e) {
            throw new ConfigLoadException("Unable to parse configuration", configLocation.toExternalForm(), e);
        }

    }

}
