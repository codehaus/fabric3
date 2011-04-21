package org.fabric3.assembly.assembly;

import org.fabric3.assembly.exception.ProfileNotFoundException;
import org.fabric3.assembly.maven.DependencyResolver;
import org.fabric3.assembly.profile.Profile;
import org.fabric3.assembly.profile.Profiles;
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

    protected void processProfiles(List<String> pProfiles, File pDestinationFolder) {
        for (String profile : pProfiles) {
            try {
                Profile p = Profiles.getProfileByName(profile);
                for (String file : p.getFiles()) {
                    File f = dependencyResolver.findFile(file);
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
            } catch (ProfileNotFoundException e) {
                LoggerUtils.logWarn(MessageFormat.format("Profile {0} not found.", e.getMessage()));
            } catch (IOException e) {
                LoggerUtils.log("Cannot unzip extension.", e);
            }
        }

        FileUtils.delete(pDestinationFolder, "NOTICE.txt", "LICENSE.txt");
    }

}
