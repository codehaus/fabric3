package org.fabric3.assembly.assembly;

import org.fabric3.assembly.configuration.Profile;
import org.fabric3.assembly.dependency.Dependency;
import org.fabric3.assembly.maven.DependencyResolver;
import org.fabric3.assembly.utils.FileUtils;
import org.fabric3.assembly.utils.FileUtils2;
import org.fabric3.assembly.utils.LoggerUtils;
import org.fabric3.assembly.utils.ZipUtils;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

/**
 * @author Michal Capo
 */
public abstract class AbstractAssemblyProfiles {

    protected DependencyResolver mDependencyResolver = new DependencyResolver();

    protected void processProfiles(List<Profile> pProfiles, File pDestinationFolder) {
        for (Profile profile : pProfiles) {
            try {
                for (Dependency dependency : profile.getDependencies()) {
                    if (dependency.isVersionLess()) {
                        dependency.setVersion(profile.getVersion());
                    }
                    File f = mDependencyResolver.findFile(dependency);
                    switch (FileUtils2.fileExtension(f)) {
                        case JAR:
                            FileUtils.copy(f, FileUtils.file(pDestinationFolder, f.getName()));
                            break;
                        case ZIP:
                            ZipUtils.unzipInSameFolder(f, pDestinationFolder);
                            break;
                        case UNKNOWN:
                            LoggerUtils.logWarn(MessageFormat.format("Cannot handle file: {0}. Only jar or zip file is supported.", f.getName()));
                            break;
                    }
                }
            } catch (IOException e) {
                LoggerUtils.log("Cannot unzip extension.", e);
            }
        }

        FileUtils.delete(pDestinationFolder, "NOTICE.txt", "LICENSE.txt");
    }

}
