package org.fabric3.contribution.ivy.processor;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.contribution.ivy.IvyConstants;
import org.fabric3.contribution.ivy.IvyDependencyManager;
import org.fabric3.contribution.ivy.IvyMonitor;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.InstallException;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionServiceListener;
import org.fabric3.spi.contribution.xml.XmlProcessor;
import org.fabric3.spi.contribution.xml.XmlProcessorRegistry;
import org.fabric3.spi.event.DomainRecovered;
import org.fabric3.spi.event.EventService;
import org.fabric3.spi.event.Fabric3EventListener;
import org.fabric3.spi.event.RuntimeStart;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Scope;

@Scope("COMPOSITE")
@EagerInit
public class IvyFileProcessor implements XmlProcessor,ContributionServiceListener,Fabric3EventListener<DomainRecovered> {
	public final static QName IVY_QNAME = new QName("ivy-module");

	@Reference
	protected IvyDependencyManager ivyDependencyManager;
	
	@Property(required=false)
	protected int updatePolicy = IvyConstants.UPDATE_POLICY_FORCED;
	
	private List<Contribution> recordedForceUpdates = new ArrayList<Contribution>();
	
	private EventService eventService;
	
	private boolean processFiles = false;
	
	public IvyFileProcessor(
			@Reference(name = "processorRegistry") XmlProcessorRegistry registry,
			@Monitor IvyMonitor ivyMonitor,
			@Reference EventService eventService) {
		registry.register(this);
		this.monitor = ivyMonitor;
		this.eventService = eventService;
		this.eventService.subscribe(DomainRecovered.class, this);
	}

	private IvyMonitor monitor;

	public QName getType() {
		return IVY_QNAME;
	}

	public void processContent(Contribution contribution,
			XMLStreamReader reader, IntrospectionContext context)
			throws InstallException {
		if(!processFiles)
		{
			monitor.debug("Waiting for RuntimeStart Evenet before processing ivy files!");
			if(updatePolicy == IvyConstants.UPDATE_POLICY_FORCED)
			{
				recordedForceUpdates.add(contribution);
			}
			
			return;
		}
		contribution.addMetaData("ivy-module", "deploy");
		install(contribution);
		monitor.profileInstalled(contribution.getUri());
	}
	
	public synchronized void install(Contribution contribution)
	{
		List<ContributionSource> contribs = ivyDependencyManager.resolve(contribution);
		ivyDependencyManager.install(contribution.getUri(),contribs);
	}

	public void onStore(Contribution contribution) {
		// TODO Auto-generated method stub
	}

	public void onProcessManifest(Contribution contribution) {
		// TODO Auto-generated method stub
	}

	public void onInstall(Contribution contribution) {
		// TODO Auto-generated method stub
	}

	public void onUpdate(Contribution contribution) {
		// TODO Auto-generated method stub
		monitor.debug("Called update for : "+contribution);
	}

	public void onUninstall(Contribution contribution) {
		// TODO Auto-generated method stub
	}
	
	protected synchronized void uninstall(URI profile)
	{
		ivyDependencyManager.uninstall(profile);
	}
	
	public void onRemove(Contribution contribution) {
		// TODO Auto-generated method stub
		if(contribution.getMetaData(String.class, "ivy-module")!=null)
		{
			monitor.info("Contribution Removed :"+contribution.getUri());
			uninstall(contribution.getUri());
		}
	}

	public void onEvent(DomainRecovered event) {
		monitor.debug("Activating IvyFileProcessor !");
		if(updatePolicy == IvyConstants.UPDATE_POLICY_FORCED && recordedForceUpdates.size() > 0)
		{
			monitor.info("Forced update :"+recordedForceUpdates);
			for (Contribution forcedUpdate : recordedForceUpdates) {
				install(forcedUpdate);
			}
		}
		processFiles = true;
	}
	
	
}
