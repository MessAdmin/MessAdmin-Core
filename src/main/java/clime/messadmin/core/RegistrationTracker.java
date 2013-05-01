/**
 *
 */
package clime.messadmin.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;

/**
 * This is a Listener and Filter tracker that ensures only 1 instance
 * of each MessAdminListener and MessAdminFilter are registered
 * for each unique application (ServletContext).
 * @author C&eacute;drik LIME
 */
public class RegistrationTracker {
	public static final RegistrationTracker LISTENER_INSTANCE = new RegistrationTracker();
	public static final RegistrationTracker FILTER_INSTANCE = new RegistrationTracker();

	private final Map<ServletContext, Object> instances = new ConcurrentHashMap<ServletContext, Object>();

	/**
	 *
	 */
	private RegistrationTracker() {
		super();
	}

	/**
	 * @see Map#put(Object, Object)
	 */
	public Object register(ServletContext key, Object value) {
		return instances.put(key, value);
	}

	/**
	 * @see Map#remove(Object)
	 */
	public Object unregister(ServletContext key) {
		return instances.remove(key);
	}

	/**
	 * @see Map#get(Object)
	 */
	public Object get(ServletContext key) {
		return instances.get(key);
	}
}
