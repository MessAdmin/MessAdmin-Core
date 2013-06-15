/**
 * 
 */
package clime.messadmin.providers.spi;

import java.util.Iterator;

import clime.messadmin.providers.ProviderUtils;


/**
 * @author C&eacute;drik LIME
 */
public interface SizeOfProvider extends BaseProvider {
	public static class Util {
		public static long getObjectSize(Object objectToSize, ClassLoader cl) {
	        long currentItemSize = -1;
	        Iterator<SizeOfProvider> iterProv = ProviderUtils.getProviders(SizeOfProvider.class, cl).iterator();
	        while (currentItemSize < 0 && iterProv.hasNext()) {
				SizeOfProvider provider = iterProv.next();
				try {
					currentItemSize = provider.sizeof(objectToSize);
				} catch (RuntimeException rte) {
				} catch (LinkageError le) {
				}
			}
	        return currentItemSize;
		}
	}

	/**
	 * @param objectToSize
	 * @return size of objectToSize in bytes, or -1 in case of error
	 */
	long sizeof(Object objectToSize);
}
