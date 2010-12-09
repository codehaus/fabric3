package org.fabric3.contribution.ivy;

import java.net.URI;
import java.util.List;

import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.spi.contribution.Contribution;

public interface IvyDependencyManager {

	public List<ContributionSource> resolve(Contribution invyContribution);

	public void install(URI profile, List<ContributionSource> contributions);

	public void uninstall(URI profile);

}