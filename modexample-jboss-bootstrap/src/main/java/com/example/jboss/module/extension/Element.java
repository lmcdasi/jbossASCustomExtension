package com.example.jboss.module.extension;

import java.util.HashMap;
import java.util.Map;

/**
 * An enumeration of all the possible XML elements in the Module schema, by
 * name.
 *
 */
public enum Element {
	// must be first
	UNKNOWN(null), CONFIGURATION("configuration"), MODULES("modules"), MODULE("module"), PROPERTIES("properties"),
	PROPERTY("property"),;

	private final String name;

	Element(final String name) {
		this.name = name;
	}

	/**
	 * Get the local name of this element.
	 *
	 * @return the local name
	 */
	public String getLocalName() {
		return name;
	}

	private static final Map<String, Element> MAP;

	static {
		final Map<String, Element> map = new HashMap<String, Element>();
		for (Element element : values()) {
			final String name = element.getLocalName();
			if (name != null)
				map.put(name, element);
		}
		MAP = map;
	}

	public static Element forName(String localName) {
		final Element element = MAP.get(localName);
		return element == null ? UNKNOWN : element;
	}
}
