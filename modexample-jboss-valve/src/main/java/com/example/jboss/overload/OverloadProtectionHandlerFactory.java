package com.example.jboss.overload;

import java.lang.reflect.Method;

public class OverloadProtectionHandlerFactory {
	public static final String DEFAULT_HANDLER = "com.example.jboss.overload.OverloadProtectionMonitor";

	private static OverloadProtectionHandler currentOLPHandler = null;

	public static OverloadProtectionHandler getOLPHandlerInstance() throws Exception {

		if (currentOLPHandler == null) {
			Class<?> c = Class.forName(DEFAULT_HANDLER);
			Method factoryMethod = c.getDeclaredMethod("getInstance");
			Object singleton = factoryMethod.invoke(null);
			currentOLPHandler = (OverloadProtectionHandler) singleton;
		}
		return currentOLPHandler;

	}
}
