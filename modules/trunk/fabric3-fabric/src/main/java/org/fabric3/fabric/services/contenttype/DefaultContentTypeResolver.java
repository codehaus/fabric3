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
package org.fabric3.fabric.services.contenttype;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.spi.services.contenttype.ContentTypeResolutionException;
import org.fabric3.spi.services.contenttype.ContentTypeResolver;

/**
 * Content type resolver that is implemented using a configured map.
 * 
 * @version $Revision$ $Date$
 */
public class DefaultContentTypeResolver implements ContentTypeResolver {
    
    // Unknown content
    private static final String UNKNOWN_CONTENT = "content/unknown";
    
    // Extension to content type map
    private Map<String, String> extensionMap = new HashMap<String, String>();
    
    /**
     * @param extensionMap Injected extension map.
     */
    public void setExtensionMap(Map<String, String> extensionMap) {
        this.extensionMap = extensionMap;
    }

    /**
     * @see org.fabric3.spi.services.contenttype.ContentTypeResolver#getContentType(java.net.URL)
     */
    public String getContentType(URL contentUrl) throws ContentTypeResolutionException {
        
        if(contentUrl == null) {
            throw new IllegalArgumentException("Content URL cannot be null");
        }
        
        String urlString = contentUrl.toExternalForm();
        
        try {
            
            URLConnection connection = contentUrl.openConnection();
            String contentType = connection.getContentType();
            
            if(contentType == null || UNKNOWN_CONTENT.equals(contentType)) {
                String extension = urlString.substring(urlString.lastIndexOf('.') + 1);
                contentType = extensionMap.get(extension);
            }
            
            if(contentType == null) {
                throw new ContentTypeResolutionException("Unable to resolve content type", urlString);
            }
            
            return contentType;
        } catch (IOException ex) {
            throw new ContentTypeResolutionException("Unable to resolve content type", urlString, ex);
        }
        
    }

}
