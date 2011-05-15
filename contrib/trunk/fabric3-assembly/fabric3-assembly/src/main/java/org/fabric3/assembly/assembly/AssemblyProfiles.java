package org.fabric3.assembly.assembly;

import org.fabric3.assembly.dependency.Dependency;
import org.fabric3.assembly.dependency.PartialDependency;
import org.fabric3.assembly.dependency.Version;
import org.fabric3.assembly.dependency.profile.Profile;
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
public abstract class AssemblyProfiles {

    protected DependencyResolver dependencyResolver = new DependencyResolver();

    protected void processProfiles(List<Profile> pProfiles, Version pVersion, File pDestinationFolder) {
        for (Profile profile : pProfiles) {
            try {
                for (PartialDependency dependency : profile.getFiles()) {
                    //TODO <capo> make here hierarchy dependency for versions
                    File f = dependencyResolver.findFile(new Dependency(dependency, pVersion));
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
