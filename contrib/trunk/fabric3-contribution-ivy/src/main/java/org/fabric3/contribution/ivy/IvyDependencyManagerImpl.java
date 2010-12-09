package org.fabric3.contribution.ivy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.retrieve.RetrieveOptions;
import org.apache.ivy.util.filter.Filter;
import org.apache.ivy.util.filter.FilterHelper;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.contribution.ivy.util.IvyLogger;
import org.fabric3.host.contribution.ContributionNotFoundException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.host.contribution.InputStreamContributionSource;
import org.fabric3.host.contribution.InstallException;
import org.fabric3.host.contribution.RemoveException;
import org.fabric3.host.contribution.StoreException;
import org.fabric3.host.contribution.UninstallException;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.domain.Domain;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class IvyDependencyManagerImpl implements IvyDependencyManager {

	private Element config;

	@Reference
	protected HostInfo hostInfo;

	@Monitor
	protected IvyMonitor monitor;

	@Reference
	protected ContributionService contributionService;
	@Reference
	protected MetaDataStore metaDataStore;

	@Reference(name = "assembly")
	Domain domain;

	private String[] artifactTypes = new String[] { "jar" };
	
	@Property(required=false)
	protected int updatePolicy = IvyConstants.UPDATE_POLICY_FORCED;
	
	public IvyDependencyManagerImpl() {

	}

	@Property(name = "config", required = false)
	public void setConfigElement(Element config) {
		this.config = config;
	}

	public String[] getArtifactTypes() {
		return artifactTypes;
	}

	public Filter getArtifactTypeFilter() {
		return FilterHelper.getArtifactTypeFilter(getArtifactTypes());
	}

	/*
	 * @TODO Create profiles for the dependencies Before a dependency gets
	 * installed check if it is already installed -> when installed add the
	 * profile to the artifact -> else store the artifact and add the profile
	 */
	public void install(Contribution ivyContribution) {

	}
	
	protected boolean undeployContribs(List<URI> contribs)
	{
		try {
			for (URI uri : contribs) {
				Contribution uninstallContrib = metaDataStore.find(uri);
				if(!uninstallContrib.isLocked())
					continue;
				List<Deployable> deployables = uninstallContrib.getManifest().getDeployables();
				for (Deployable deployable : deployables) {
					
					domain.undeploy(deployable.getName(), true);
				}
			}
		} catch (DeploymentException e) {
			monitor.error(e.getMessage(), e);
			return false;
		}
		
		return true;
	}
	
	public void uninstall(URI profile)
	{
		try {
			if(contributionService.profileExists(profile))
			{
				List<URI> contribs = contributionService.getSortedContributionsInProfile(profile);
				
				//check if the contributions contain deplyed artifacts
				undeployContribs(contribs);
				
				contributionService.uninstallProfile(profile);
				contributionService.removeProfile(profile);
			}
		} catch (UninstallException e) {
			monitor.error("uninstall caught : "+e.getMessage(), e);
		} catch (ContributionNotFoundException e) {
			monitor.error("uninstall caught :"+e.getMessage(), e);
		} catch (RemoveException e) {
			monitor.error("uninstall caught :"+e.getMessage(), e);
		}
	}

	public void install(URI profile, List<ContributionSource> contributions) {

		try {
			// first check if the profile already exists
			boolean profileExists = contributionService.profileExists(profile);

			if (profileExists) {
				monitor.info("Profile :" + profile
						+ " already exists - will reinstall the profile");
				uninstall(profile);
			}

			List<URI> profileUris = new ArrayList<URI>();

			for (ContributionSource contributionSource : contributions) {
				monitor.debug("Trying to install contribution uri :"
						+ contributionSource.getUri());
				// check if already installed
				URI contribUri = contributionSource.getUri();
				Contribution contrib = metaDataStore.find(contribUri);
				if (contrib != null) {
					contrib.addProfile(profile);
				} else {
					URI stored = contributionService.store(contributionSource);
					profileUris.add(stored);
				}
			}

			monitor.info("Installing : " + profile+" with contributions : "+profileUris);
			contributionService.registerProfile(profile, profileUris);
			contributionService.installProfile(profile);
			

			for (Iterator<URI> iterator = profileUris.iterator(); iterator
					.hasNext();) {
				URI uri = (URI) iterator.next();
				Contribution contrib = metaDataStore.find(uri);
				if (contrib.isLocked())
					iterator.remove();
			}

			domain.include(profileUris);
		} catch (Exception e) {
			monitor.error("install caught:"+e.getMessage(), e);
		} 
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.fabric3.contribution.ivy.IvyDependencyManager#resolve(org.fabric3
	 * .spi.contribution.Contribution)
	 */
	public List<ContributionSource> resolve(Contribution invyContribution) {
		List<ContributionSource> sources = new ArrayList<ContributionSource>();
		Ivy ivy = Ivy.newInstance();
		ivy.getLoggerEngine().setDefaultLogger(new IvyLogger(monitor));
		configureIvy(ivy);
		String pattern = "/[module]/[type]/[artifact]-[revision].[ext]";
		try {
			ResolveOptions resOpts = new ResolveOptions();
			resOpts.setArtifactFilter(getArtifactTypeFilter());
			ResolveReport report = ivy.resolve(invyContribution.getLocation());
			RetrieveOptions opts = new RetrieveOptions();
			Map toCopy = ivy.getRetrieveEngine().determineArtifactsToCopy(
					report.getModuleDescriptor().getModuleRevisionId(),
					pattern, opts);

			Iterator it = toCopy.keySet().iterator();
			while (it.hasNext()) {
				ArtifactDownloadReport artifactReport = (ArtifactDownloadReport) it
						.next();
				
				//based on the downloadStatus I can determine if we have to handle a new
				//version of a snapshot -> if the corresponding contribution is already installed
				//this information can be used to determine if we should update the current
				//installed version. This handling should be configured to enable
				//update of existing contributions ! This will work with recovery since the profile
				//will already be installed
				// this will only work if the local ivy repository is not defined in the f3/tmp directory
				//since then the dependencies will always need to be downloaded at startup but runtime
				//changes can be tracked this way. At the moment UPDATE_POLICY_FORCED will always
				//install the latest dependencies - this is not really always requiered and must be added.
				
				
				File localFile = artifactReport.getLocalFile();
				// monitor.info("Next File ContributionSource : "+fileName);
				InputStreamContributionSource source = new InputStreamContributionSource(
						new URI(localFile.getName()), new FileInputStream(
								localFile));
				
				sources.add(source);
			}

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return sources;
	}

	protected void configureIvy(Ivy ivy) {
		if (this.config == null) {
			try {
				ivy.configureDefault();
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		} else {
			try {
				Document document = createDocument();
				File tmpSettings = File.createTempFile("tmpIvySettings",
						".xml", hostInfo.getTempDir());
				FileOutputStream fop = new FileOutputStream(tmpSettings);
				writeDocument(fop, document);
				fop.flush();
				fop.close();
				ivy.configure(tmpSettings);
				//tmpSettings.delete();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void printConfig() {
		try {
			monitor.info("Got config document : " + this.config.toString());
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();
			Node settingsNode = config.getFirstChild();
			document.adoptNode(settingsNode);
			document.appendChild(settingsNode);
			File tmpSettings = File.createTempFile("tmpSettings", ".xml");
			FileOutputStream fop = new FileOutputStream(tmpSettings);

			writeDocument(fop, document);
			fop.flush();
			fop.close();

			Ivy ivy = Ivy.newInstance();
			ivy.configure(tmpSettings);
			ModuleRevisionId mrid = ModuleRevisionId.newInstance(
					"com.gridynamics.testing.f3", "simple", "0.0.1-SNAPSHOT");

			ResolveOptions opts = new ResolveOptions();
			opts.setConfs(new String[] { "default" });
			opts.setRefresh(true);
			ivy.resolve(mrid, opts, true);
			tmpSettings.delete();
		} catch (DOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Transforms a document to an InputSource.
	 * 
	 * @param document
	 *            the document
	 * @return the InputSource
	 * @throws TransformerException
	 *             if the document cannot be transformed
	 */
	private InputSource transform(Document document)
			throws TransformerException {
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer();
		DOMSource source = new DOMSource(document);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		StreamResult result = new StreamResult(stream);
		transformer.transform(source, result);
		return new InputSource(new ByteArrayInputStream(stream.toByteArray()));
	}

	protected Document createDocument() throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.newDocument();
		Node settingsNode = config.getFirstChild();
		settingsNode = settingsNode.cloneNode(true);
		document.adoptNode(settingsNode);
		document.appendChild(settingsNode);
		return document;
	}

	protected void writeDocument(FileOutputStream fop, Document document)
			throws TransformerException {
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer();
		DOMSource source = new DOMSource(document);
		StreamResult result = new StreamResult(fop);
		transformer.transform(source, result);
	}

}
