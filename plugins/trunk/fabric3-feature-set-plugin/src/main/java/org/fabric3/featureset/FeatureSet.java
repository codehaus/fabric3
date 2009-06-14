  /*
   * Fabric3
   * Copyright (C) 2009 Metaform Systems
   *
   * Fabric3 is free software: you can redistribute it and/or modify
   * it under the terms of the GNU General Public License as
   * published by the Free Software Foundation, either version 3 of
   * the License, or (at your option) any later version, with the
   * following exception:
   *
   * Linking this software statically or dynamically with other
   * modules is making a combined work based on this software.
   * Thus, the terms and conditions of the GNU General Public
   * License cover the whole combination.
   *
   * As a special exception, the copyright holders of this software
   * give you permission to link this software with independent
   * modules to produce an executable, regardless of the license
   * terms of these independent modules, and to copy and distribute
   * the resulting executable under terms of your choice, provided
   * that you also meet, for each linked independent module, the
   * terms and conditions of the license of that module. An
   * independent module is a module which is not derived from or
   * based on this software. If you modify this software, you may
   * extend this exception to your version of the software, but
   * you are not obligated to do so. If you do not wish to do so,
   * delete this exception statement from your version.
   *
   * Fabric3 is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty
   * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
   * See the GNU General Public License for more details.
   *
   * You should have received a copy of the
   * GNU General Public License along with Fabric3.
   * If not, see <http://www.gnu.org/licenses/>.
   */
package org.fabric3.featureset;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.maven.model.Dependency;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @version $Revision$ $Date$
 */
public class FeatureSet {
    
    private Set<Dependency> extensions = new HashSet<Dependency>();
    private Set<Dependency> sharedLibraries = new HashSet<Dependency>();
    
    
    /**
     * Adds an extension to the feature set.
     * 
     * @param extension Extension to be added to the feature set.
     */
    public void addExtension(Dependency extension) {
        extensions.add(extension);
    }
    
    
    /**
     * Adds a shared library to the feature set.
     * 
     * @param shared Shared library to be added to the feature set.
     */
    public void addSharedLibrary(Dependency sharedLibrary) {
    	sharedLibraries.add(sharedLibrary);
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
            for (Dependency sharedLibrary : sharedLibraries) {
                writer.println("    <shared>");
                writer.println("        <artifactId>" + sharedLibrary.getArtifactId() + "</artifactId>");
                writer.println("        <groupId>" + sharedLibrary.getGroupId() + "</groupId>");
                writer.println("        <version>" + sharedLibrary.getVersion() + "</version>");
                writer.println("    </shared>");
            }
            writer.println("</featureSet>");
            writer.flush();
            
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
        
    }
    
    public static FeatureSet deserialize(File featureSetFile) throws ParserConfigurationException, SAXException, IOException {
    	
    	FeatureSet featureSet = new FeatureSet();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();

        Document featureSetDoc = db.parse(featureSetFile);

        NodeList extensionList = featureSetDoc.getElementsByTagName("extension");
        for (int i = 0; i < extensionList.getLength(); i++) {
            Dependency extension = createDependency(extensionList, i);
            featureSet.addExtension(extension);
        }

        NodeList sharedList = featureSetDoc.getElementsByTagName("shared");
        for (int i = 0; i < sharedList.getLength(); i++) {
            Dependency sharedLibrary = createDependency(sharedList, i);
            featureSet.addSharedLibrary(sharedLibrary);
        }
        
        return featureSet;
    }


	private static Dependency createDependency(NodeList extensionList, int i) {
		
		Element extensionElement = (Element) extensionList.item(i);

		Element artifactIdElement = (Element) extensionElement.getElementsByTagName("artifactId").item(0);
		Element groupIdElement = (Element) extensionElement.getElementsByTagName("groupId").item(0);
		Element versionElement = (Element) extensionElement.getElementsByTagName("version").item(0);

		Dependency extension = new Dependency();
		extension.setArtifactId(artifactIdElement.getTextContent());
		extension.setGroupId(groupIdElement.getTextContent());
		extension.setVersion(versionElement.getTextContent());
		
		return extension;
		
	}

	public Set<Dependency> getSharedLibraries() {
		return sharedLibraries;
	}

	public Set<Dependency> getExtensions() {
		return extensions;
	}

}
