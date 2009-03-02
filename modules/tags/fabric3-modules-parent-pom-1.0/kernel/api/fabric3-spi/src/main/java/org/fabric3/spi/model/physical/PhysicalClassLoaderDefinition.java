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
package org.fabric3.spi.model.physical;

import java.io.Serializable;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A definition used to provision classloaders on a runtime.
 *
 * @version $Rev$ $Date$
 */
public class PhysicalClassLoaderDefinition implements Serializable {
    private static final long serialVersionUID = 1869864181383360066L;

    private URI uri;
    private URI contributionUri;
    private Set<PhysicalClassLoaderWireDefinition> wireDefinitions = new LinkedHashSet<PhysicalClassLoaderWireDefinition>();

    public PhysicalClassLoaderDefinition(URI uri) {
        this.uri = uri;
    }

    /**
     * Returns the classloader uri.
     *
     * @return the classloader uri
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Adds a PhysicalClassLoaderWireDefinition that describes a wire to another contribution classloader.
     *
     * @param definition the PhysicalClassLoaderDefinition
     */
    public void add(PhysicalClassLoaderWireDefinition definition) {
        wireDefinitions.add(definition);
    }

    /**
     * Returns a set of PhysicalClassLoaderWireDefinition that describe the wires to other contribution classloaders.
     *
     * @return a set of PhysicalClassLoaderWireDefinition that describe the wires to other contribution classloader
     */
    public Set<PhysicalClassLoaderWireDefinition> getWireDefinitions() {
        return wireDefinitions;
    }

    /**
     * Sets the contribution asociated with the classloader.
     *
     * @param uri the URI to add
     */
    public void setContributionUri(URI uri) {
        contributionUri = uri;
    }

    /**
     * Returns the URI of the contributions associated with this classloader as an ordered Set. Order is guaranteed for set iteration.
     *
     * @return the URI
     */
    public URI getContributionUri() {
        return contributionUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PhysicalClassLoaderDefinition that = (PhysicalClassLoaderDefinition) o;

        if (contributionUri != null ? !contributionUri.equals(that.contributionUri) : that.contributionUri != null) return false;
        if (uri != null ? !uri.equals(that.uri) : that.uri != null) return false;
        if (wireDefinitions != null ? !wireDefinitions.equals(that.wireDefinitions) : that.wireDefinitions != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uri != null ? uri.hashCode() : 0;
        result = 31 * result + (contributionUri != null ? contributionUri.hashCode() : 0);
        result = 31 * result + (wireDefinitions != null ? wireDefinitions.hashCode() : 0);
        return result;
    }
}
