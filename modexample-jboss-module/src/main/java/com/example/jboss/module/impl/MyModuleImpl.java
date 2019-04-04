package com.example.jboss.module.impl;

import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

import com.example.jboss.module.ifc.MyModuleIfc;

import java.util.logging.Logger;

/**
*
* Implementation of the Custom Module Interface. It bootstraps a custom
* jar via a JBOSS module.
* 
*/

public class MyModuleImpl implements MyModuleIfc {
	private static final Logger log = Logger.getLogger(MyModuleImpl.class.getName());
	
	/*
	 * Definition of the CUSTOM module
	 */
	private static final String moduleName = new String("CUSTOM Lifecycle Module");
	
	/**
	 * Triggers service' <CODE>create</CODE> and <CODE>init</CODE> methods. 
	 */
	private void create() throws StartException {
		/**
		 * TODO: ADD code to create & init the service
		 */
	}

	/*
	 * Starts the custom services.
	 * 
	 */
	@Override
	public void start(StartContext context) throws StartException {
		// Set the current thread to the proper classloader
		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
		
		/** Create custom service */
		this.create();
		
		/** Starts custom service */
		try {
			/**
			 * TODO: ADD code to start the service
			 */
		} catch (Exception e) {
			// Dump the stack trace
			e.printStackTrace();
			
			throw new StartException("Module failed to start CUSTOM service.");
		}
			
		log.info("MyModuleImpl service started.");
	}

	/*
	 * Stops the custom services.
	 * 
	 */
	@Override
	public void stop(StopContext context) {
		try {
			/**
			 * TODO - Add code for shutdown
			 */
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		log.info("MyModuleImpl service stopped.");
	}
	
	@Override
	public String getName() {
		return moduleName;
	}
}
