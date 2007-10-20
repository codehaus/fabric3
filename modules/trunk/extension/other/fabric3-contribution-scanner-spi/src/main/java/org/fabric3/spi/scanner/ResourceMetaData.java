package org.fabric3.spi.scanner;

/**
 * Provides metadata for a FileSystemResource that has been sent to a DirectoryScannerDestination
 *
 * @version $Rev$ $Date$
 */
public interface ResourceMetaData {

    /**
     * Returns the last-updated artifact checksum.
     *
     * @return the last-updated artifact checksum
     */
    byte[] getChecksum();

    /**
     * Returns the last-updated artifact timestamp.
     *
     * @return the last-updated artifact timestamp
     */
    long getTimestamp();
}
