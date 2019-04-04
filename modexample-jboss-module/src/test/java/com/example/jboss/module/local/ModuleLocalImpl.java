package com.example.jboss.module.local;

import org.jboss.logging.Logger;

public class ModuleLocalImpl implements ModuleLocalIfc {
	private static final Logger log = Logger.getLogger(ModuleLocalImpl.class.getName());
	
	@Override
	public void logHello() {
		log.info("ModuleLocalImpl --> log Hello");		
	}

}
