/**
 * What the hell is this doing in the sun.misc package?
 * This mechanism is an official specification since Java 1.3!
 */
//package sun.misc;
package clime.messadmin.providers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import clime.messadmin.utils.Charsets;

/**
 * From JDK 1.3.1 / 1.4.2 / 1.5.0
 * see http://docs.oracle.com/javase/1.5.0/docs/guide/jar/jar.html#Service%20Provider
 *
 * see javax.imageio.spi.ServiceRegistry (Java 1.4+)
 * see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4640520
 * see http://docs.oracle.com/javase/6/docs/api/java/util/ServiceLoader.html (Java 6)
 * @author C&eacute;drik LIME
 */
public final class Service {

	// Private inner class implementing fully-lazy provider lookup
	//
	private static class LazyIterator<S> implements Iterator<S> {

		Class<S> service;
		ClassLoader loader;
		Enumeration<URL> configs = null;
		Iterator<String> pending = null;
		Set<String> returned = new TreeSet<String>();
		String nextName = null;

		/** {@inheritDoc} */
		public boolean hasNext() throws ServiceConfigurationError {
			if (nextName != null) {
				return true;
			}
			if (configs == null) {
				try {
					String s = prefix + service.getName();
					if (loader == null) {
						configs = ClassLoader.getSystemResources(s);
					} else {
						configs = loader.getResources(s);
					}
				} catch (IOException ioe) {
					Service.fail(service, "Error locating configuration files: " + ioe);
				}
			}
			while (pending == null || !pending.hasNext()) {
				if (!configs.hasMoreElements()) {
					return false;
				}
				pending = Service.parse(service, configs.nextElement(), returned);
			}

			nextName = pending.next();
			return true;
		}

		/** {@inheritDoc} */
		public S next() throws ServiceConfigurationError {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			String cn = nextName;
			nextName = null;
			try {
				return (S) Class.forName(cn, true, loader).newInstance();
			} catch (ClassNotFoundException cnfe) {
				Service.fail(service, "Provider " + cn + " not found");
			} catch (Exception e) {
				Service.fail(service, "Provider " + cn
						+ " could not be instantiated: " + e, e);
			}
			return null;
		}

		/** {@inheritDoc} */
		public void remove() {
			throw new UnsupportedOperationException();
		}

		private LazyIterator(Class<S> service, ClassLoader loader) {
			this.service = service;
			this.loader = loader;
		}

	}

	private static final String prefix = "META-INF/services/";//$NON-NLS-1$

	private Service() {
	}

	protected static void fail(Class<?> service, String msg) throws ServiceConfigurationError {
		throw new ServiceConfigurationError(service.getName() + ": " + msg);
	}

	protected static void fail(Class<?> service, URL url, int line, String msg) throws ServiceConfigurationError {
		fail(service, url + ":" + line + ": " + msg);
	}

	/**
	 * @since 1.4
	 */
	protected static void fail(Class<?> service, String msg, Throwable cause) throws ServiceConfigurationError {
		ServiceConfigurationError sce = new ServiceConfigurationError(
				service.getName() + ": " + msg, cause);
		throw sce;
	}

	// Parse a single line from the given configuration file, adding the name
	// on the line to the names list.
	//
	private static int parseLine(Class<?> service, URL url,
			BufferedReader r, int lc, List<String> names, Set<String> providers)
			throws IOException, ServiceConfigurationError {
		String ln = r.readLine();
		if (ln == null) {
			return -1;
		}
		int ci = ln.indexOf('#');
		if (ci >= 0) {
			ln = ln.substring(0, ci);
		}
		ln = ln.trim();
		int n = ln.length();
		if (n != 0) {
			if (ln.indexOf(' ') >= 0 || ln.indexOf('\t') >= 0) {
				fail(service, url, lc, "Illegal configuration-file syntax");
			}
			if (!Character.isJavaIdentifierStart(ln.charAt(0))) {
				fail(service, url, lc, "Illegal provider-class name: " + ln);
			}
			for (int i = 1; i < n; ++i) {
				char c = ln.charAt(i);
				if (!Character.isJavaIdentifierPart(c) && c != '.') {
					fail(service, url, lc, "Illegal provider-class name: " + ln);
				}
			}

			if (!providers.contains(ln)) {
				names.add(ln);
				providers.add(ln);
			}
		}
		return lc + 1;
	}

	// Parse the content of the given URL as a provider-configuration file.
	//
	// @param  service
	//         The service type for which providers are being sought;
	//         used to construct error detail strings
	//
	// @param  u
	//         The URL naming the configuration file to be parsed
	//
	// @return A (possibly empty) iterator that will yield the provider-class
	//         names in the given configuration file that are not yet members
	//         of the returned set
	//
	// @throws ServiceConfigurationError
	//         If an I/O error occurs while reading from the given URL, or
	//         if a configuration-file format error is detected
	//
	protected static Iterator<String> parse(Class<?> service, URL url, Set<String> providers) throws ServiceConfigurationError {
		InputStream in = null;
		BufferedReader r = null;
		List<String> names = new ArrayList<String>();
		try {
			in = url.openStream();
			r = new BufferedReader(new InputStreamReader(in, Charsets.UTF_8));//$NON-NLS-1$
			int lc = 1;
			while ((lc = parseLine(service, url, r, lc, names, providers)) >= 0) {
			}
		} catch (IOException ioe) {
			fail(service, "Error reading configuration file: " + ioe);
		} finally {
			try {
				if (r != null) {
					r.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException ioe) {
				fail(service, "Error closing configuration file: " + ioe);
			}
		}
		return names.iterator();
	}

	/**
	 * Lazily locates and instantiates the available providers for the given service class and class loader.
	 *
	 * @param service The interface or abstract class representing this service
	 * @param loader The class loader to be used to load provider-configuration files and provider classes, or null if the system class loader (or, failing that, the bootstrap class loader) is to be used
	 * @return An iterator that lazily locates and instantiates providers for this service
	 * @throws ServiceConfigurationError
	 */
	public static <S> Iterator<S> providers(Class<S> service, ClassLoader loader) throws ServiceConfigurationError {
		return new LazyIterator(service, loader);
	}

	/**
	 * Lazily locates and instantiates the available providers for the given service class, using the current thread's context class loader.
	 *
	 * <p> An invocation of this convenience method of the form
	 *
	 * <blockquote><pre>
	 * Service.lookup(<i>service</i>)</pre></blockquote>
	 *
	 * is equivalent to
	 *
	 * <blockquote><pre>
	 * Service.lookup(<i>service</i>,
	 *                Thread.currentThread().getContextClassLoader())</pre></blockquote>
	 *
	 * @param service The interface or abstract class representing this service
	 * @return An iterator that lazily locates and instantiates providers for this service
	 * @throws ServiceConfigurationError
	 */
	public static <S> Iterator<S> providers(Class<S> service) throws ServiceConfigurationError {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		return providers(service, cl);
	}

	/**
	 * Lazily locates and instantiates the available providers for the given service class, using the extension class loader.
	 *
	 * <p> This convenience method simply locates the extension class loader,
	 * call it <tt><i>extClassLoader</i></tt>, and then returns
	 *
	 * <blockquote><pre>
	 * Service.lookup(<i>service</i>, <i>extClassLoader</i>)</pre></blockquote>
	 *
	 * <p> If the extension class loader cannot be found then the system class
	 * loader is used; if there is no system class loader then the bootstrap
	 * class loader is used.
	 *
	 * <p> This method is intended for use when only installed providers are
	 * desired.  The resulting service will only find and load providers that
	 * have been installed into the current Java virtual machine; providers on
	 * the application's class path will be ignored.
	 *
	 * @param service The interface or abstract class representing this service
	 * @return An iterator that lazily locates and instantiates providers for this service
	 * @throws ServiceConfigurationError
	 */
	public static <S> Iterator<S> installedProviders(Class<S> service) throws ServiceConfigurationError {
		ClassLoader cl = ClassLoader.getSystemClassLoader();
		ClassLoader extClassLoader = null;
		for (; cl != null; cl = cl.getParent()) {
			extClassLoader = cl;
		}
		return providers(service, extClassLoader);
	}
}
