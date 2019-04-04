package com.example.jboss.test.impl;

import org.jboss.logging.Logger;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

import com.example.jboss.module.ifc.MyModuleIfc;

/**
*
* Implementation of the  Module Interface. It bootstraps the
* custom service via a JBOSS module.
*
*/

public class TestModuleImpl implements MyModuleIfc {
	private static final Logger log = Logger.getLogger(TestModuleImpl.class.getName());
	
	private static final String moduleItentifier = new String("Test CUSTOM module");
	
	private static final CustomModuleTestServiceLoad myLocalLoader = new CustomModuleTestServiceLoad();
	
	/*
	 * Starts the services. JBOSS AS7 specific.
	 * 
	 */
	@Override
	public void start(StartContext context) throws StartException {		
		log.info("TestModuleImpl service started.");
		
		// Set classloader to the module class loader in order to
		// avoid issues on the ServiceLoad
		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
		
		// Perform a local class loader
		myLocalLoader.loadClass();
	}

	/*
	 * Stops the CUSTOM services. JBOSS AS7 specific.
	 * 
	 */
	@Override
	public void stop(StopContext context) {
		myLocalLoader.unloadClass();
		
		log.info("TestModuleImpl service stopped.");
	}

	@Override
	public String getName() {
		return moduleItentifier;
	}
}