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
package org.fabric3.fabric.services.contribution;

import java.io.File;
import static java.io.File.separator;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.namespace.QName;

import com.thoughtworks.xstream.XStream;
import org.osoa.sca.Constants;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.services.xstream.XStreamFactory;
import org.fabric3.fabric.util.FileHelper;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionStoreRegistry;
import org.fabric3.spi.services.contribution.Export;
import org.fabric3.spi.services.contribution.Import;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.contribution.MetaDataStoreException;
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.spi.services.contribution.ResourceElement;
import org.fabric3.spi.services.contribution.Symbol;

/**
 * Default IndexStore implementation
 *
 * @version $Rev$ $Date$
 */
public class MetaDataStoreImpl implements MetaDataStore {
    public static final QName COMPOSITE = new QName(Constants.SCA_NS, "composite");
    private final ContributionStoreRegistry registry;
    private final XStream xstream;
    private File root;
    private String repository;
    private Map<URI, Contribution> cache = new ConcurrentHashMap<URI, Contribution>();
    private Map<QName, Map<Export, Contribution>> exportsToContributionCache =
            new ConcurrentHashMap<QName, Map<Export, Contribution>>();
    private String storeId = DEFAULT_STORE;
    private boolean persistent = true;
    private File baseDir;

    public MetaDataStoreImpl(@Reference HostInfo hostInfo,
                             @Reference ContributionStoreRegistry registry,
                             @Reference XStreamFactory xstreamFactory) {

        this.registry = registry;
        this.xstream = xstreamFactory.createInstance();
        URL url = hostInfo.getBaseURL();
        if (url != null) {
            String pathname = url.getFile();
            baseDir = new File(pathname);
        }
    }

    @Property(required = false)
    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    @Property(required = false)
    // TODO fixme when boolean properties supported
    public void setPersistent(String persistent) {
        this.persistent = Boolean.valueOf(persistent);
    }

    @Property(required = false)
    public void setRepository(String repository) {
        this.repository = repository;
    }

    @Init
    public void init() throws IOException {
        if (persistent) {
            if (repository != null) {
                root = new File(repository + separator + "index" + separator);
            } else {
                root = new File(baseDir, "stores" + separator + storeId + separator + "index");
            }
            FileHelper.forceMkdir(root);
            if (!root.exists() || !this.root.isDirectory() || !root.canRead()) {
                throw new IOException("The repository location is not a directory: " + repository);
            }
            recover();
        }
        registry.register(this);
    }

    public String getId() {
        return storeId;
    }

    public void store(Contribution contribution) throws MetaDataStoreException {
        if (persistent) {
            FileOutputStream fos = null;
            try {
                File directory = new File(root, contribution.getUri().getPath());
                FileHelper.forceMkdir(directory);
                File index = new File(directory, "contribution.ser");
                fos = new FileOutputStream(index);
                xstream.toXML(contribution, fos);
            } catch (IOException e) {
                String identifier = contribution.getUri().toString();
                throw new MetaDataStoreException("Error storing contribution", identifier, e);
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        // TODO log exception
                        e.printStackTrace();
                    }
                }
            }
        }
        cache.put(contribution.getUri(), contribution);
        addToExports(contribution);
    }


    public Contribution find(URI contributionUri) {
        return cache.get(contributionUri);
    }

    @SuppressWarnings({"unchecked"})
    public <S extends Symbol> ResourceElement<S, ?> resolve(S symbol) {
        for (Contribution contribution : cache.values()) {
            for (Resource resource : contribution.getResources()) {
                for (ResourceElement<?, ?> element : resource.getResourceElements()) {
                    if (element.getSymbol().equals(symbol)) {
                        return (ResourceElement<S, ?>) element;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Resolves an import to a Contribution that exports it
     *
     * @param imprt the import to resolve
     * @return the contribution or null
     */
    public Contribution resolve(Import imprt) {
        Map<Export, Contribution> map = exportsToContributionCache.get(imprt.getType());
        if (map == null) {
            return null;
        }
        Contribution candidate = null;
        int matchLevel = Export.NO_MATCH;
        for (Map.Entry<Export, Contribution> entry : map.entrySet()) {
            int level = entry.getKey().match(imprt);
            if (level == Export.EXACT_MATCH) {
                return entry.getValue();
            } else {
                if (candidate == null || matchLevel > level) {
                    candidate = entry.getValue();
                    matchLevel = level;
                }
            }
        }
        return candidate;
    }

    public List<Contribution> resolveTransitiveImports(Contribution contribution) throws UnresolvableImportException {
        ArrayList<Contribution> contributions = new ArrayList<Contribution>();
        resolveTransitiveImports(contribution, contributions);
        return contributions;
    }

    private void resolveTransitiveImports(Contribution contribution, List<Contribution> dependencies)
            throws UnresolvableImportException {
        for (Import imprt : contribution.getManifest().getImports()) {
            Contribution imported = resolve(imprt);
            if (imported == null) {
                String identifier = contribution.getUri().toString();
                throw new UnresolvableImportException("Unable to resolve import in contribution", identifier);
            }
            if (!dependencies.contains(imported)) {
                dependencies.add(imported);
            }
            resolveTransitiveImports(imported, dependencies);
        }
    }

    /**
     * Rebuilds the contribution index from its serialized form on disk
     *
     * @throws java.io.IOException if an error occurs reading from disk
     */
    private void recover() throws IOException {
        File directory = new File(root, storeId);
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                File[] indexFiles = file.listFiles();
                for (File indexFile : indexFiles) {
                    if ("contribution.ser".equals(indexFile.getName())) {
                        FileInputStream fin = null;
                        try {
                            fin = new FileInputStream(indexFile);
                            Contribution contribution = (Contribution) xstream.fromXML(fin);
                            // FIXME handle class cast exception gracefully
                            cache.put(contribution.getUri(), contribution);
                        } catch (FileNotFoundException e) {
                            // should not happen
                            throw new AssertionError();
                        } finally {
                            if (fin != null) {
                                fin.close();
                            }
                        }
                    }
                }
            }
        }
        // rebuild the exports to contribution cache
        for (Contribution contribution : cache.values()) {
            addToExports(contribution);
        }

    }

    /**
     * Adds the contribution exports to the cached list of exports for the domain
     *
     * @param contribution the contribution containing the exports to add
     */
    private void addToExports(Contribution contribution) {

        if (contribution.getManifest() == null) {
            return;
        }

        List<Export> exports = contribution.getManifest().getExports();
        if (exports.size() > 0) {
            for (Export export : exports) {
                Map<Export, Contribution> map = exportsToContributionCache.get(export.getType());
                if (map == null) {
                    map = new ConcurrentHashMap<Export, Contribution>();
                    exportsToContributionCache.put(export.getType(), map);
                }
                map.put(export, contribution);
            }
        }
    }

}
