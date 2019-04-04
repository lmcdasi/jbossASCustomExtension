package com.example.jboss.test.impl;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.jboss.logging.Logger;

import com.example.jboss.module.local.ModuleLocalIfc;

public class CustomModuleTestServiceLoad {
	private static final Logger log = Logger.getLogger(CustomModuleTestServiceLoad.class.getName());
	
	public void loadClass() {
		ServiceLoader<?> serviceLoader = ServiceLoader.load(ModuleLocalIfc.class);
		
		Iterator<?> serviceIter = serviceLoader.iterator();
		while(serviceIter.hasNext()) {
			log.info("CustomModuleTestServiceLoad --> ModuleLocalImpl loaded from local ServiceLoader.");
			
			((ModuleLocalIfc) serviceIter.next()).logHello();
		}
		
		log.info("CustomModuleTestServiceLoad --> end");
	}

	public void unloadClass() {
		log.info("Unloading class");
	}
}
