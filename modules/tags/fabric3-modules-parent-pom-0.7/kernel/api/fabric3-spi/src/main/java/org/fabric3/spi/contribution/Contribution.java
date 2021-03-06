/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.spi.contribution;

import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 * The base representation of a deployed contribution
 *
 * @version $Rev$ $Date$
 */
@SuppressWarnings({"SerializableHasSerializationMethods"})
public class Contribution implements Serializable {
    private static final long serialVersionUID = 2511879480122631196L;

    private final URI uri;
    private ContributionState state = ContributionState.STORED;
    private URL location;
    private byte[] checksum;
    private long timestamp;
    private String contentType;
    private boolean persistent;
    private ContributionManifest manifest = new ContributionManifest();
    private List<Resource> resources = new ArrayList<Resource>();
    private List<ContributionWire<?, ?>> wires = new ArrayList<ContributionWire<?, ?>>();

    private Set<QName> lockOwners = new HashSet<QName>();

    public Contribution(URI uri) {
        this.uri = uri;
    }

    /**
     * Instantiates a new Contribution instance.
     *
     * @param uri         the contribution URI
     * @param location    a dereferenceble URL for the contribution archive
     * @param checksum    the checksum for the contribution artifact
     * @param timestamp   the time stamp of the contribution artifact
     * @param contentType the MIME type of the contribution
     * @param persistent  true if the contribution is persistent
     */
    public Contribution(URI uri, URL location, byte[] checksum, long timestamp, String contentType, boolean persistent) {
        this.uri = uri;
        this.location = location;
        this.checksum = checksum;
        this.timestamp = timestamp;
        this.contentType = contentType;
        this.persistent = persistent;
    }

    /**
     * Returns the contribution URI.
     *
     * @return the contribution URI
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Returns the contribution lifecycle state.
     *
     * @return the contribution lifecycle state
     */
    public ContributionState getState() {
        return state;
    }

    /**
     * Sets the contribution lifecycle state.
     *
     * @param state the contribution lifecycle state
     */
    public void setState(ContributionState state) {
        this.state = state;
    }

    /**
     * Returns the locally dereferenceable URL for the contribution artifact.
     *
     * @return the dereferenceable URL for the contribution artifact
     */
    public URL getLocation() {
        return location;
    }

    /**
     * Returns the MIME type for the contribution.
     *
     * @return the MIME type for the contribution
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Returns the contribution artifact checksum.
     *
     * @return the contribution artifact checksum
     */
    public byte[] getChecksum() {
        return checksum;
    }

    /**
     * Returns the timestamp of the most recent update to the artifact.
     *
     * @return the timestamp of the most recent update to the artifact
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Returns true if the contribution is persistent.
     *
     * @return true if the contribution is persistent.
     */
    public boolean isPersistent() {
        return persistent;
    }

    /**
     * Returns the contribution manifest.
     *
     * @return the contribution manifest
     */
    public ContributionManifest getManifest() {
        return manifest;
    }

    /**
     * Sets the contribution manifest.
     *
     * @param manifest the contribution manifest
     */
    public void setManifest(ContributionManifest manifest) {
        this.manifest = manifest;
    }

    /**
     * Adds a resource to the contribution.
     *
     * @param resource the resource
     */
    public void addResource(Resource resource) {
        resources.add(resource);
    }

    /**
     * Returns the list of resources for the contribution.
     *
     * @return the list of resources
     */
    public List<Resource> getResources() {
        return resources;
    }

    /**
     * Returns a ResourceElement matching the symbol or null if not found.
     *
     * @param symbol the symbol to match
     * @return a ResourceElement matching the symbol or null if not found
     */
    @SuppressWarnings({"unchecked"})
    public <T extends Symbol> ResourceElement<T, Serializable> findResourceElement(Symbol<T> symbol) {
        for (Resource resource : resources) {
            for (ResourceElement<?, ?> element : resource.getResourceElements()) {
                if (element.getSymbol().equals(symbol)) {
                    return (ResourceElement<T, Serializable>) element;
                }
            }
        }
        return null;
    }

    /**
     * Adds a wire for an import
     *
     * @param wire the wire
     */
    public void addWire(ContributionWire<?, ?> wire) {
        wires.add(wire);
    }

    /**
     * Returns the wires for this contribution.
     *
     * @return the wires for this contribution
     */
    public List<ContributionWire<?, ?>> getWires() {
        return wires;
    }

    /**
     * Acquires a lock for the contribution. If a contribution is locked, it cannot be uninstalled. Locks may be acquired by multiple owners, for
     * example, deployable composites that are contained in a contribution when they are deployed.
     *
     * @param owner the lock owner
     */
    public void acquireLock(QName owner) {
        if (lockOwners.contains(owner)) {
            throw new IllegalStateException("Lock already held by owner for contribution" + uri + " :" + owner);
        }
        lockOwners.add(owner);
    }

    /**
     * Releases a lock held by the given owner.
     *
     * @param owner the lock owner
     */
    public void releaseLock(QName owner) {
        if (!lockOwners.remove(owner)) {
            throw new IllegalStateException("Lock not held by owner for contribution" + uri + " :" + owner);
        }
    }

    /**
     * Returns the set of current lock owners.
     *
     * @return the set of current lock owners
     */
    public Set<QName> getLockOwners() {
        return lockOwners;
    }

    /**
     * Returns true if the contribution is locked. Locked contributions cannot be uninstalled.
     *
     * @return true if the contribution is locked
     */
    public boolean isLocked() {
        return !lockOwners.isEmpty();
    }
}
