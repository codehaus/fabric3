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
package org.fabric3.spi.services.contribution;

import java.io.Serializable;
import java.net.URI;
import java.util.Set;

import org.fabric3.host.contribution.StoreException;
import org.fabric3.model.type.ValidationContext;

/**
 * Implementations store contribution metadata.
 *
 * @version $Rev$ $Date$
 */
public interface MetaDataStore {

    /**
     * Stores the contribution metadata
     *
     * @param contribution the contribution metadata
     * @throws StoreException if an error storing the metadata occurs
     */
    void store(Contribution contribution) throws StoreException;

    /**
     * Returns the contribution for the given URI.
     *
     * @param contributionUri the contribution URI
     * @return the contribution for the given URI or null if not found
     */
    Contribution find(URI contributionUri);

    /**
     * Returns the installed contributions in the domain.
     *
     * @return the installed contributions in the domain
     */
    Set<Contribution> getContributions();

    /**
     * Removes the contribution metadata.
     *
     * @param contributionUri the contribution uri
     */
    void remove(URI contributionUri);

    /**
     * Resolves a resource element by its symbol against the entire domain symbol space.
     *
     * @param symbol the symbol used to represent the resource element.
     * @return the resource element or null if not found
     * @throws StoreException if an error occurs during resolution
     */
    <S extends Symbol> ResourceElement<S, ?> resolve(S symbol) throws StoreException;

    /**
     * Resolves the containing resource for a resource element symbol against the given contribution symbol space.
     *
     * @param uri    the contribution uri
     * @param symbol the symbol used to represent the resource element.
     * @return the resource or null if not found
     */
    public Resource resolveContainingResource(URI uri, Symbol symbol);

    /**
     * Resolves a resource element by its symbol against the given contribution uri.
     *
     * @param contributionUri the contribution URI to resolve against
     * @param type            the class representing the resource
     * @param symbol          the symbol used to represent the resource element.
     * @param context         the context to which validation errors and warnings are reported
     * @return the resource element or null if not found
     * @throws org.fabric3.host.contribution.StoreException
     *          if an error occurs during resolution
     */
    <S extends Symbol, V extends Serializable> ResourceElement<S, V> resolve(URI contributionUri, Class<V> type, S symbol, ValidationContext context)
            throws StoreException;

    /**
     * Returns true if the import is resolved.
     *
     * @param imprt the import
     * @return true if the import is resolved
     */
    boolean isResolved(Import imprt);

    /**
     * Resolves an import to a matching export and returns the associated ContributionWire.
     *
     * @param uri   the importing contribution  URI
     * @param imprt the import to resolve @return a ContributionWire or null
     * @return the ContributionWire
     * @throws UnresolvedImportException if the import cannot be resolved
     */
    ContributionWire<?,?> resolve(URI uri, Import imprt) throws UnresolvedImportException;

    /**
     * Resolves contributions that import the contribution represented by the given URI.
     *
     * @param uri the contribution URI
     * @return the set of contributions that import the contribution
     */
    Set<Contribution> resolveDependentContributions(URI uri);

    /**
     * Resolves the contribution that contains the given symbol
     *
     * @param symbol the symbol to resolve
     * @return the contribution or null
     */
    Contribution resolveContainingContribution(Symbol symbol);
}
