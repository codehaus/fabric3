package org.fabric3.spi.services.contribution;

/**
 * @version $Rev$ $Date$
 */
public interface ContributionStoreRegistry {

    void register(ArchiveStore store);

    void unregister(ArchiveStore store);

    void register(MetaDataStore store);

    void unregister(MetaDataStore store);

}
