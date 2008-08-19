package org.fabric3.console;

import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanServer;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class Fabric3CoreDomainAction extends Action {

	private String selectedSubDomain;
	
	private String selectedComponent;
	
	private List<String> readSubDomains;
	
	private List<String> readComponents;
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		String userSelectedSubdomain = (String) request.getAttribute("selectedSubDomain");
		String userSelectedComponent = (String) request.getAttribute("selectedComponent");
		
		System.out.println("User Selected Subdomain" + userSelectedSubdomain);
		System.out.println("User Selected Component" + userSelectedComponent);
		if(userSelectedSubdomain == null) {
		    readSubDomains = readSubDomains();
		    setSelectedSubDomain(readSubDomains.get(0));
		} else {
			setSelectedSubDomain(userSelectedSubdomain);
		}
		
		if(userSelectedComponent == null) {
		     readComponents = readComponents(userSelectedSubdomain);
		     setSelectedComponent(readComponents.get(0));
		} else {
			setSelectedComponent(userSelectedComponent);
		}
		
		request.setAttribute("subDomains", readSubDomains);
		
		request.setAttribute("selectedSubDomain", selectedSubDomain);
	      
		request.setAttribute("components", readComponents);
	    
		request.setAttribute("selectedComponent", selectedComponent);
	      
		return mapping.findForward("success");
		
	}

	public String getSelectedSubDomain() {
		return selectedSubDomain;
	}

	public void setSelectedSubDomain(String selectedSubDomain) {
		this.selectedSubDomain = selectedSubDomain;
	}

	public String getSelectedComponent() {
		return selectedComponent;
	}

	public void setSelectedComponent(String selectedComponent) {
		this.selectedComponent = selectedComponent;
	}

	private List<String> readComponents(String selectedSubDomain2) {
		
      List<String> components = new ArrayList<String>();
      components.add("Component1" + selectedSubDomain);
      components.add("Component2" + selectedSubDomain);
      return components;		
	}

	private List<String> readSubDomains() {
		
		//MBeanServer mbeanServer = getMBeanServer();
		
		List<String> subDomains = new ArrayList<String>();
		subDomains.add("Subdomain1");
		subDomains.add("Subdomain2");
		return subDomains;
	}
	
    /*
     * Gets the MBean server from Weblogic.
     */
    private MBeanServer getMBeanServer() throws NamingException {
        
        Context ctx = null;        
        try {
            ctx = new InitialContext();
            Object mbeanServer = ctx.lookup("java:comp/env/jmx/runtime");
            return (MBeanServer) PortableRemoteObject.narrow(mbeanServer, MBeanServer.class);
        } finally {
            ctx.close();
        }
        
    }

}
