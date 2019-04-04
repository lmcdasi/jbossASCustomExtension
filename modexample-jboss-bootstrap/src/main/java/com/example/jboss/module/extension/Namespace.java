package com.example.jboss.module.extension;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * An enumeration of the supported subsystem namespaces.
 * 
 */
public enum Namespace {
    // must be first
    /**
     * Unknown revision
     */
    UNKNOWN(null),
    /**
     * supports multiple versioning of CUSTOM extension module - related
     * to slot no.
     */
    MODULE_1_0("urn:com.example.jboss.module.extension:1.0");

    /**
     * The current namespace version.
     */
    public static final Namespace CURRENT = MODULE_1_0;

    private final String name;

    Namespace(final String name) {
        this.name = name;
    }

    /**
     * Get the URI of this namespace.
     * 
     * @return the URI
     */
    public String getUriString() {
        return name;
    }

    /**
     * Set of all namespaces, excluding the special {@link #UNKNOWN}
     * value.
     */
    public static final EnumSet<Namespace> STANDARD_NAMESPACES = EnumSet.complementOf(EnumSet.of(Namespace.UNKNOWN));

    private static final Map<String, Namespace> MAP;

    static {
        final Map<String, Namespace> map = new HashMap<String, Namespace>();
        for (Namespace namespace : values()) {
            final String name = namespace.getUriString();
            if (name != null) {
                map.put(name, namespace);
            }
        }
        MAP = map;
    }

    /**
     * @param uri
     * @return revision no for the provided uri
     */
    public static Namespace forUri(String uri) {
        final Namespace element = MAP.get(uri);
        return element == null ? UNKNOWN : element;
    }
}
