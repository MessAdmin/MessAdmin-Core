/**
 * 
 */
package clime.messadmin.providers.spi;

import java.util.Iterator;

import clime.messadmin.providers.ProviderUtils;

/**
 * @author C&eacute;drik LIME
 */
public interface SerializableProvider extends BaseProvider {
	public static class Util {
		public static boolean isSerializable(Object obj, ClassLoader cl) {
			Iterator iter = ProviderUtils.getProviders(SerializableProvider.class, cl).iterator();
			while (iter.hasNext()) {
				SerializableProvider sp = (SerializableProvider) iter.next();
				try {
					return sp.isSerializable(obj);
				} catch (RuntimeException rte) {
					return false;
				} catch (LinkageError le) {
					// skip: not using required ClassLoader for this object; try another plugin implementation
				}
			}
			// we should never get to this point!
			throw new IllegalStateException("Can't find any working " + SerializableProvider.class.getName() + " for object " + obj);//$NON-NLS-1$//$NON-NLS-2$
		}
	}

	/**
	 * @param obj
	 * @return true if obj is Serializable, false otherwise
	 */
	boolean isSerializable(Object obj);
}
