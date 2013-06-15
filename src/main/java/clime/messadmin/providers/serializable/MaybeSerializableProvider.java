/**
 *
 */
package clime.messadmin.providers.serializable;

import java.io.Serializable;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

import clime.messadmin.providers.spi.SerializableProvider;

/**
 * Determines if an object is serializable by lookup if it implements java.io.Serializable.
 * While this implementation is quick, it may not be accurate:
 * <ul>
 *   <li>a return value of true does not warranty that such an object is really serializable.</li>
 *   <li>a return value of false is a 100% warranty that the object is not serializable.</li>
 * </ul>
 * This implementation recurses into Collections and Arrays, but does not reflect into objects.
 * @author C&eacute;drik LIME
 */
public class MaybeSerializableProvider implements SerializableProvider {

	public MaybeSerializableProvider() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSerializable(Object obj) {
		return isMaybeSerializable(obj);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getPriority() {
		return 10;
	}

	/**
	 * Determines if an object is serializable by lookup if it implements java.io.Serializable.
	 * While this implementation is quick, it may not be accurate:
	 * <ul>
	 *   <li>a return value of true does not warranty that such an object is really serializable.</li>
	 *   <li>a return value of false is a 100% warranty that the object is not serializable.</li>
	 * </ul>
	 * This implementation recurses into Collections and Arrays, but does not reflect into objects.
	 * @param o
	 * @return true if o is Serializable, false if not
	 */
	private static boolean isMaybeSerializable(Object o) {
		if (o == null) {
			return true;
		}
		if (! (o instanceof Serializable)) {
			return false;
		}
		IdentityHashMap visitedObjects = new IdentityHashMap(42);
		return isMaybeSerializable(o, visitedObjects);
	}

	/**
	 * Implementation note: we need to track a list of visited objects in order to
	 * avoid graph cycle problems.
	 * @see #isMaybeSerializable(Object)
	 * @param o
	 * @param visitedObjects
	 * @return true if o is Serializable, false if not
	 */
	private static boolean isMaybeSerializable(Object o, IdentityHashMap visitedObjects) {
		if (o == null) {
			return true;
		}
		if (! (o instanceof Serializable)) {
			return false;
		}
		if (visitedObjects.containsKey(o)) {
			return true;
		}
		visitedObjects.put(o, o);
		// Collection: each member of the collection must be Serializable
		if (o instanceof Collection) {
			try {
				for (Object oo : (Collection) o) {
					if (! isMaybeSerializable(oo, visitedObjects)) {
						return false;
					}
				}
			} catch (RuntimeException rte) {
				//e.g. Hibernate Lazy Exception
				return false;
			}
		}
		// Map: each entry (key and value) of the map must be Serializable
		if (o instanceof Map) {
			try {
				Iterator iter = ((Map) o).entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry entry = (Map.Entry) iter.next();
					if (! isMaybeSerializable(entry.getKey(), visitedObjects) || ! isMaybeSerializable(entry.getValue(), visitedObjects)) {
						return false;
					}
				}
			} catch (RuntimeException rte) {
				//e.g. Hibernate Lazy Exception
				return false;
			}
		}
		// Array: each member of the collection must be Serializable
		if (o.getClass().isArray()) {
			// arrays of primitive types are Serializable
			if (o.getClass().getComponentType().isPrimitive()) {
				return true;
			}
//			// There's only 1 type for the Array. If this type is Serializable, then all objects in the array are Serializable
//			// Note: this can't be that simple! E.g. array of Collection, with non-serializable items inside...
//			if (Serializable.class.isAssignableFrom(o.getClass().getComponentType())) {
//				return true;
//			}
			// if object type is not Serializable, no need to check inside the array
//			if ((Object[]) o).length > 0 && ! Serializable.class.isAssignableFrom(o.getClass().getComponentType())) {
//				return false;
//			}
			// Array type is not Serializable, but maybe all entries are. We need to check.
			for (Object object : (Object[]) o) {
				if (! isMaybeSerializable(object, visitedObjects)) {
					return false;
				}
			}
		}
		return true;
	}
}
