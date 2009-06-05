/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.runtime.standalone.server;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.fabric3.host.Namespaces;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.FileContributionSource;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.runtime.standalone.SyntheticContributionSource;

/**
 * Scans a repository for extension and user contributions.
 *
 * @version $Revision$ $Date$
 */
public class RepositoryScanner {
    private static final String MANIFEST_PATH = "META-INF/sca-contribution.xml";
    DocumentBuilderFactory documentBuilderFactory;

    public RepositoryScanner() {
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
    }

    /**
     * Scans a repository directory for contributions.
     *
     * @param directory the directory
     * @return the contributions grouped by user and extension contributions
     * @throws InitializationException if there is an error scanning teh directory
     */
    public ScanResult scan(File directory) throws InitializationException {

        File[] files = directory.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                // skip directories and files beginning with '.'
                return !pathname.isDirectory() && !pathname.getName().startsWith(".");
            }
        });
        ScanResult result = new ScanResult();
        Map<URL, ContributionSource> toScan = new HashMap<URL, ContributionSource>();
        for (File file : files) {
            try {
                URI uri = URI.create(file.getName());
                URL location = file.toURI().toURL();
                ContributionSource source = new FileContributionSource(uri, location, -1, new byte[0]);
                if (!file.getName().endsWith(".jar")) {
                    // if the file is not a JAR, it must be a user contribution
                    result.addUserContribution(source);
                } else {
                    toScan.put(source.getLocation(), source);
                }
            } catch (MalformedURLException e) {
                throw new InitializationException("Error loading contribution", file.getName(), e);
            }
        }

        URL[] urls = new URL[toScan.size()];
        int i = 0;
        for (ContributionSource source : toScan.values()) {
            urls[i] = source.getLocation();
            ++i;
        }
        URLClassLoader urlClassLoader = new URLClassLoader(urls);
        Enumeration<URL> scannedManifests;
        try {
            scannedManifests = urlClassLoader.getResources(MANIFEST_PATH);
        } catch (IOException e) {
            throw new InitializationException("Error scanning repository", e);
        }

        Set<URL> manifests = new HashSet<URL>();

        while (scannedManifests.hasMoreElements()) {
            URL manifestUrl = scannedManifests.nextElement();
            URL contributionUrl = getContributionUrl(manifestUrl);
            boolean extension = isExtension(manifestUrl);
            if (extension) {
                result.addExtensionContribution(toScan.get(contributionUrl));
            } else {
                result.addUserContribution(toScan.get(contributionUrl));
            }
            manifests.add(contributionUrl);
        }

        // Make another pass and categorize all contributions without an SCA manifest as extensions. This is safe as they cannot contain deployable
        // components and hence only contain sharable artifacts.
        for (Map.Entry<URL, ContributionSource> entry : toScan.entrySet()) {
            if (!manifests.contains(entry.getKey())) {
                result.addExtensionContribution(entry.getValue());
            }
        }

        // create synthetic contributions from directories contained in /repository
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                try {
                    URI uri = URI.create("f3-" + file.getName());
                    URL location = file.toURI().toURL();
                    ContributionSource source = new SyntheticContributionSource(uri, location);
                    result.addExtensionContribution(source);
                } catch (MalformedURLException e) {
                    throw new InitializationException(e);
                }
            }
        }
        return result;


    }

    /**
     * Checks whether a contribution containing the manifest is an extension.
     *
     * @param manifestUrl the URL of the contribution manifest
     * @return true if the contribution is an extension
     * @throws InitializationException if there is an error scanning the manifest
     */
    private boolean isExtension(URL manifestUrl) throws InitializationException {
        try {
            DocumentBuilder db = documentBuilderFactory.newDocumentBuilder();

            InputStream stream = manifestUrl.openStream();
            Document document = db.parse(stream);
            stream.close();

            String extension = document.getDocumentElement().getAttributeNS(Namespaces.CORE, "extension");
            return extension != null && !"".equals(extension.trim());
        } catch (IOException e) {
            throw new InitializationException(e);
        } catch (ParserConfigurationException e) {
            throw new InitializationException(e);
        } catch (SAXException e) {
            throw new InitializationException(e);
        }
    }

    /**
     * Computes the contribution URL from the manifest URL.
     *
     * @param manifestUrl the manifest URL
     * @return the contribution URL
     * @throws InitializationException if there is an error computing the URL
     */
    private URL getContributionUrl(URL manifestUrl) throws InitializationException {

        String externalForm = manifestUrl.toExternalForm();
        String protocol = manifestUrl.getProtocol();
        String url;

        if ("jar".equals(protocol)) {
            url = externalForm.substring(0, externalForm.indexOf("!/" + MANIFEST_PATH));
            // Strip the jar protocol
            url = url.substring(4);
        } else {
            url = externalForm.substring(0, externalForm.indexOf(MANIFEST_PATH));
        }
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new InitializationException(e);
        }

    }


}
