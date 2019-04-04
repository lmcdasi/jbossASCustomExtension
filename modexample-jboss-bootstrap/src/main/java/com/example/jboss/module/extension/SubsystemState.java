package com.example.jboss.module.extension;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.modules.ModuleIdentifier;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;

import com.example.jboss.module.extension.services.FrameworkBootstrapService;

import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * The module subsystem state.
 *
 */
public final class SubsystemState implements Serializable, Service<SubsystemState> {
	public static final ServiceName SERVICE_NAME = FrameworkBootstrapService.SERVICE_NAME.append("subsystemstate");

	protected static final SubsystemState INSTANCE = new SubsystemState();
	
	private static final long serialVersionUID = -8339785547808483973L;
	private final Map<String, Dictionary<String, String>> configurations = new LinkedHashMap<String, Dictionary<String, String>>();
	private final Map<String, Object> properties = new LinkedHashMap<String, Object>();
	private final List<MyModule> modules = new ArrayList<MyModule>();
	private Activation activationPolicy = Activation.EAGER;

	public static ServiceController<SubsystemState> addService(ServiceTarget serviceTarget) {
		ServiceBuilder<SubsystemState> builder = serviceTarget.addService(SERVICE_NAME, INSTANCE);
		builder.setInitialMode(Mode.ACTIVE);
		return builder.install();
	}

	public Set<String> getConfigurations() {
		synchronized (configurations) {
			Collection<String> values = configurations.keySet();
			return Collections.unmodifiableSet(new HashSet<String>(values));
		}
	}

	public boolean hasConfiguration(String pid) {
		synchronized (configurations) {
			return configurations.containsKey(pid);
		}
	}

	public Dictionary<String, String> getConfiguration(String pid) {
		synchronized (configurations) {
			return configurations.get(pid);
		}
	}

	public Dictionary<String, String> putConfiguration(String pid, Dictionary<String, String> props) {
		synchronized (configurations) {
			return configurations.put(pid, new UnmodifiableDictionary<String, String>(props));
		}
	}

	public Dictionary<String, String> removeConfiguration(String pid) {
		synchronized (configurations) {
			return configurations.remove(pid);
		}
	}

	/** The only activation supported by custom module - eager */
	public enum Activation {
		EAGER
	}

	public Map<String, Object> getProperties() {
		synchronized (properties) {
			return Collections.unmodifiableMap(properties);
		}
	}

	void addProperty(String name, Object value) {
		synchronized (properties) {
			properties.put(name, value);
		}
	}

	Object setProperty(String name, Object value) {
		synchronized (properties) {
			if (value == null)
				return properties.remove(name);
			else
				return properties.put(name, value);
		}
	}

	public boolean isModuleExisting(String id) {
		ModuleIdentifier identifier = ModuleIdentifier.fromString(id);
		synchronized (modules) {
			for (Iterator<MyModule> it = modules.iterator(); it.hasNext();) {
				MyModule module = it.next();
				if (module.getIdentifier().equals(identifier)) {
					return true;
				}
			}
			return false;
		}
	}

	public List<MyModule> getModules() {
		synchronized (modules) {
			return Collections.unmodifiableList(modules);
		}
	}

	protected void addModule(MyModule module) {
		synchronized (modules) {
			modules.add(module);
		}
	}

	protected MyModule removeModule(String id) {
		ModuleIdentifier identifier = ModuleIdentifier.fromString(id);
		synchronized (modules) {
			for (Iterator<MyModule> it = modules.iterator(); it.hasNext();) {
				MyModule module = it.next();
				if (module.getIdentifier().equals(identifier)) {
					it.remove();
					return module;
				}
			}
			return null;
		}
	}

	public Activation getActivationPolicy() {
		return activationPolicy;
	}

	boolean isEmpty() {
		return properties.isEmpty() && modules.isEmpty() && configurations.isEmpty();
	}

	public static class MyModule implements Serializable {
		private static final long serialVersionUID = 3099324500850258630L;
		private final ModuleIdentifier identifier;
		private final Integer startlevel;

		MyModule(ModuleIdentifier identifier, Integer startlevel) {
			this.identifier = identifier;
			this.startlevel = startlevel;
		}

		public ModuleIdentifier getIdentifier() {
			return identifier;
		}

		public Integer getStartLevel() {
			return startlevel;
		}
	}

	public SubsystemState getValue() throws IllegalStateException, IllegalArgumentException {
		return this;
	}

	public void start(StartContext context) throws StartException {
		/** Nothing to do */
	}

	public void stop(StopContext context) {
		/** Nothing to do */
	}
}
