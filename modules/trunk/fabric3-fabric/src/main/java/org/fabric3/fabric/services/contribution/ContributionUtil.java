package org.fabric3.fabric.services.contribution;

import java.net.URI;
import java.util.StringTokenizer;

/**
 * Miscellaneous utility methods for the contribution infrastructure
 *
 * @version $Rev$ $Date$
 */
public final class ContributionUtil {

    private ContributionUtil() {
    }

    /**
     * Calculates a filesystem path based on the domain URI
     *
     * @param uri the domain URI
     * @return the filesystem path
     */
    public static String getDomainPath(URI uri) {
        StringBuilder buf = new StringBuilder(uri.getScheme());
        StringTokenizer path = new StringTokenizer(uri.getPath(), "/");
        while (path.hasMoreTokens()) {
            buf.append("/").append(path.nextToken());
        }
        return buf.toString();
    }

}
