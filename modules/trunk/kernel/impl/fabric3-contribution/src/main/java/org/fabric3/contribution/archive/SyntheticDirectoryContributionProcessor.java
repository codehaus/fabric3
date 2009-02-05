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
package org.fabric3.contribution.archive;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.host.contribution.InstallException;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.util.io.FileHelper;

/**
 * Synthesizes a special contribution from a directory that is configured to extend an extension point derived from the name of the directory. For
 * example, a contribution can be synthesized that extends an extension point provided by a datasource extension by making JDBC drivers available.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class SyntheticDirectoryContributionProcessor extends AbstractContributionProcessor {
    private static final String CONTENT_TYPE = "application/vnd.fabric3.synthetic";
    private static final List<String> CONTENT_TYPES = new ArrayList<String>();

    static {
        CONTENT_TYPES.add(CONTENT_TYPE);
    }

    public List<String> getContentTypes() {
        return CONTENT_TYPES;
    }

    public void processManifest(Contribution contribution, final IntrospectionContext context) throws InstallException {
        URL sourceUrl = contribution.getLocation();
        File root = FileHelper.toFile(sourceUrl);
        assert root.isDirectory();
        ContributionManifest manifest = contribution.getManifest();
        manifest.setExtension(true);
        manifest.addExtend(root.getName());
    }

    public void index(Contribution contribution, IntrospectionContext context) throws InstallException {

    }

    public void process(Contribution contribution, IntrospectionContext context, ClassLoader loader) throws InstallException {

    }

}