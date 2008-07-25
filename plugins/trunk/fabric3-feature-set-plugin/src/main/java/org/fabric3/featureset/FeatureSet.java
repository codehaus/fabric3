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
package org.fabric3.featureset;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @version $Revision$ $Date$
 */
public class FeatureSet {
    
    private Set<Dependency> extensions = new HashSet<Dependency>();
    
    /**
     * Adds an extension to the feature set.
     * 
     * @param extension Extension to be added to the feature set.
     */
    public void addExtension(Dependency extension) {
        extensions.add(extension);
    }
    
    /**
     * Serializes the feature set to the deployable artifact file.
     * 
     * @param artifactFile File to which the feture set needs to be written.
     * @throws FileNotFoundException 
     */
    public void serialize(File artifactFile) throws FileNotFoundException {
        
        PrintWriter writer = null;
        
        try {
            
            writer = new PrintWriter(new FileOutputStream(artifactFile));
            
            writer.println("<featureSet>");
            for (Dependency extension : extensions) {
                writer.println("    <extension>");
                writer.println("        <artifactId>" + extension.getArtifactId() + "</artifactId>");
                writer.println("        <groupId>" + extension.getGroupId() + "</groupId>");
                writer.println("        <version>" + extension.getVersion() + "</version>");
                writer.println("    </extension>");
            }
            writer.println("</featureSet>");
            writer.flush();
            
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
        
    }

}
