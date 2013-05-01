/**
 *
 */
package clime.messadmin.providers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import clime.messadmin.providers.spi.BaseProvider;

/**
 * Fetches and caches Services (== Providers)
 * @author C&eacute;drik LIME
 */
public class ProviderUtils {
	/**
	 * This is a cache of Providers, keyed by its Interface, and by a ClassLoader.
	 * This enables different WebApps (different ClassLoaders) to have their own set of plugins (same Interface).
	 */
	private static final Map/*<ClassLoader, Map<Class, List<? extends BaseProvider>>>*/ PROVIDERS_CACHE = new SoftHashMap();//FIXME do we need soft values too?
	private static final Comparator priorityComparator = new Comparator() {
		/** {@inheritDoc} */
		public int compare(Object o1, Object o2) {
			int p1 = ((BaseProvider)o1).getPriority();
			int p2 = ((BaseProvider)o2).getPriority();
			return (p1 < p2) ? -1 : (p1 == p2) ? 0 :  1;
		}
	};

	/**
	 *
	 */
	private ProviderUtils() {
		super();
	}

	/**
	 * Implementation note: keep this method unsynchronized, as it would be a contention point otherwise.
	 * We don't care if the cache is filled multiple times.
	 * @param clazz
	 * @return list of providers for clazz, sorted by priority
	 */
	public static /*<T extends BaseProvider>*/ List/*<T>*/ getProviders(final Class/*<T>*/ clazz) {
		return getProviders(clazz, Thread.currentThread().getContextClassLoader());
	}
	public static /*<T extends BaseProvider>*/ List/*<T>*/ getProviders(final Class/*<T>*/ clazz, ClassLoader classLoader) {
		if (classLoader == null) {
			classLoader = Thread.currentThread().getContextClassLoader();
		}
		Map providersByInterface = (Map) PROVIDERS_CACHE.get(classLoader);
		if (providersByInterface == null) {
			fillProvidersCache(clazz, classLoader);
			providersByInterface = (Map) PROVIDERS_CACHE.get(classLoader);
		}

		List providers = (List) providersByInterface.get(clazz);
		if (providers == null) {
			fillProvidersCache(clazz, classLoader);
			providers = (List) providersByInterface.get(clazz);
		}
		return providers;
	}

	/**
	 * @param clazz
	 */
	private static synchronized void fillProvidersCache(final Class/*<? extends BaseProvider>*/ clazz, final ClassLoader classLoader) {
		Map/*<Class, List<? extends BaseProvider>>*/ providersByInterface = (Map) PROVIDERS_CACHE.get(classLoader);
		if (providersByInterface == null) {
			providersByInterface = new SoftHashMap/*<Class, List<? extends BaseProvider>>*/();
			// put resulting Map in cache as *last operation* (after filling said Map)
			PROVIDERS_CACHE.put(classLoader, providersByInterface);
		}

		List/*<? extends BaseProvider>*/ providers = (List) providersByInterface.get(clazz);
		if (providers == null) {
			providers = new ArrayList/*<? extends BaseProvider>*/();
			Iterator ps = Service.providers(clazz, classLoader);
			if (! ps.hasNext() && classLoader != clazz.getClassLoader()) {
				ps = Service.providers(clazz, clazz.getClassLoader());
			}
			while (ps.hasNext()) {
				try {
					BaseProvider provider = (BaseProvider) ps.next();
					providers.add(provider);
				} catch (RuntimeException rte) {
					// error while fetching provider; skipping
					System.err.println("ERROR while fetching MessAdmin Provider " + clazz + " (skipped): " + rte);
				} catch (LinkageError le) {
					// error while fetching provider; skipping
					System.err.println("ERROR while fetching MessAdmin Provider " + clazz + " (skipped): " + le);
				}
			}
			Collections.sort(providers, priorityComparator);
			// put resulting List in cache as *last operation* (after filling said List)
			providersByInterface.put(clazz, providers);
		}
	}

	/**
	 * Clear this service's provider cache.
	 *
	 * <p> After invoking this method, subsequent invocations of the
	 * <code>iterator</code> method will lazily look up and instantiate
	 * providers from scratch, just as is done by a newly-created instance of
	 * this class.
	 *
	 * <p> This method is intended for use in situations in which new providers
	 * can be installed into a running Java virtual machine.
	 */
	public static void reload() {
		PROVIDERS_CACHE.clear();
	}

	/**
	 * Clear this service's provider cache for the current ClassLoader.
	 *
	 * <p> After invoking this method, subsequent invocations of the
	 * <code>iterator</code> method will lazily look up and instantiate
	 * providers from scratch, just as is done by a newly-created instance of
	 * this class.
	 *
	 * <p> This method is intended for use in situations in which new providers
	 * can be installed into a running Java virtual machine.
	 */
	public static void deregisterCurrent() {
		PROVIDERS_CACHE.remove(Thread.currentThread().getContextClassLoader());
	}

	/**
	 * Finalizes this object prior to garbage collection.  The
	 * <code>deregisterAll</code> method is called to deregister all
	 * currently registered service providers.  This method should not
	 * be called from application code.
	 *
	 * @exception Throwable if an error occurs during superclass
	 * finalization.
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		reload();
		super.finalize();
	}
}
