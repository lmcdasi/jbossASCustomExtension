package com.example.jboss.module.engvalve;

import java.util.List;

import org.apache.catalina.Host;

import org.jboss.as.web.VirtualHost;
import org.jboss.as.web.WebSubsystemServices;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController.State;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

import com.example.jboss.module.ifc.MyModuleIfc;
import com.example.jboss.valveasmodule.impl.OverloadProtectionValve;

public class OverloadProtectionHttpEngineValveStartup implements MyModuleIfc {
	private static final Logger log = Logger.getLogger(OverloadProtectionHttpEngineValveStartup.class.getName());
	private static final String moduleItentifier = new String("Test Catalina Valve module");
	private static final OverloadProtectionValve globalValve = new OverloadProtectionValve();
	
	public void start(StartContext context) throws StartException {		
		log.info("HTTP Overload Protection Engine Valve service started.");
		
		// Set classloader to the module class loader in order to
		// avoid issues on the ServiceLoad
		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

		List<ServiceName> myList = context.getController().getServiceContainer().getServiceNames();
		
		/* HACK the startup order - for now */
		while(context.getController().getServiceContainer().getRequiredService(WebSubsystemServices.JBOSS_WEB).getState() != State.UP) {
			log.info("JBoss web service state: " + context.getController().getServiceContainer().getRequiredService(WebSubsystemServices.JBOSS_WEB).getState());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new StartException(e);
			}
		}

		// Start the OverloadProtection Service - a Catalina Valve
		try {
			// Get the default virtual host service instance
			VirtualHost virtualServer = (VirtualHost) context.getController().getServiceContainer().getRequiredService(WebSubsystemServices.JBOSS_WEB_HOST.append("default-host")).getValue();
			
			// Get the Catalina host instance
			Host host = virtualServer.getHost();
			
			// Add valve
			host.getParent().getPipeline().addValve(globalValve);
			
			log.info("HTTP Overload Protection Engine Valve added");
		} catch (Throwable tex) {
			log.error("Error performing Catalina addValve operation", tex);
			myList.forEach(srv -> log.info("Service --> " + srv.getSimpleName()));
		}
	}

	public void stop(StopContext context) {
		log.info("HTTP Overload Protection Engine Valve stopped.");
		
		// Set classloader to the module class loader in order to
		// avoid issues on the ServiceLoad
		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

		// Start the OLP - Engine Valve Base
		try {
			// Get the default virtual host instance
			VirtualHost virtualServer = (VirtualHost) context.getController().getServiceContainer().getRequiredService(WebSubsystemServices.JBOSS_WEB_HOST.append("default-host")).getValue();
			
			// Get the Catalina host instance
			Host host = virtualServer.getHost();
			
			// Remove valve
			host.getParent().getPipeline().removeValve(globalValve);
			
			log.info("HTTP Overload Protection Engine Valve removed");
		} catch (Throwable tex) {
			log.debug("Error performing Catalina removeValve operation", tex);
		}
	}

	public String getName() {
		return moduleItentifier;
	}
}
