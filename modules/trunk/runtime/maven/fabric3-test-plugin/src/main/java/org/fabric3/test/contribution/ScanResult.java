/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
package org.fabric3.test.contribution;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.fabric3.host.Constants;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.FileContributionSource;

/**
 * Result of a contribution scan.
 */
public class ScanResult {

    private List<ContributionSource> userContributions = new LinkedList<ContributionSource>();
    private List<ContributionSource> extensionContributions = new LinkedList<ContributionSource>();

    /**
     * Gets the user contributions.
     *
     * @return User contributions.
     */
    public List<ContributionSource> getUserContributions() {
        return userContributions;
    }

    /**
     * Gets the extension contributions.
     *
     * @return Extension contributions.
     */
    public List<ContributionSource> getExtensionContributions() {
        return extensionContributions;
    }

    /**
     * Adds a  contribution.
     *
     * @param contributionUrl Contribution URL.
     * @param extension       True if the contribution is an extension
     */
    public void addContribution(URL contributionUrl, boolean extension) {

        String contentType = contributionUrl.toExternalForm().endsWith(".jar") ? Constants.ZIP_CONTENT_TYPE : Constants.FOLDER_CONTENT_TYPE;

        URI uri = URI.create(new File(contributionUrl.getFile()).getName());
        ContributionSource contributionSource = new FileContributionSource(uri, contributionUrl, -1, null, contentType);
        if (extension) {
            extensionContributions.add(contributionSource);
        } else {
            userContributions.add(contributionSource);
        }
    }

}
