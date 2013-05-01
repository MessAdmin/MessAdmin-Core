/**
 *
 */
package clime.messadmin.providers.lifecycle;

import java.beans.Introspector;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Timer;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import clime.messadmin.providers.spi.ApplicationLifeCycleProvider;

/**
 * Takes care of deregistering (some of) the evil Singletons
 * when the app shuts down, thereby avoiding (well, trying to avoid)
 * OOM (java.lang.OutOfMemoryError) on hot restart...
 *
 * @see <a href="http://opensource.atlassian.com/confluence/spring/pages/viewpage.action?pageId=2669">Memory leaks where the classloader cannot be garbage collected</a>
 * @see <a href="http://wiki.apache.org/tomcat/MemoryLeakProtection">Tomcat Memory Leak Protection</a>
 * @author C&eacute;drik LIME
 */
public class EvilSingletonsUnregisterer implements ApplicationLifeCycleProvider {

	/**
	 *
	 */
	public EvilSingletonsUnregisterer() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getPriority() {
		return Integer.MAX_VALUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public void contextDestroyed(ServletContext servletContext) {
		// ThreadLocals - With Great Power, comes Great Responsibility
		// Can't do anything here. (Well, almost; see Tomcat article.) You need to manually set all your ThreadLocals to null yourself (myThreadLocal.remove() or myThreadLocal.set(null))...

		// Threads
		// Can't do anything here. Make sure you stop all your created Threads when stopping the application.

		final ClassLoader thisClassLoader = Thread.currentThread().getContextClassLoader();//this.getClass().getClassLoader();

		// Jakarta Commons IO >= 1.3
		// org.apache.commons.io.FileCleaner.exitWhenFinished();
		try {
			Class fileCleanerClass = thisClassLoader.loadClass("org.apache.commons.io.FileCleaner");//$NON-NLS-1$
			if (fileCleanerClass != null && fileCleanerClass.getClassLoader() == thisClassLoader) {
				// If the given classloader is the same as the classloader of FileCleaner, this means that the lib
				// has been deployed inside the war, so the thread should be stopped and all resources released.
				// If the classloader is different, then the thread is owned by the container, so don't stop it.
				Method ewfMethod = fileCleanerClass.getMethod("exitWhenFinished", null);//$NON-NLS-1$
				ewfMethod.invoke(null, null);
			}
		} catch (Throwable ignore) {
		}

		// MySQL Connector/J <= 5.1.10
		// @see http://bugs.mysql.com/bug.php?id=36565
		// com.mysql.jdbc.ConnectionImpl.cancelTimer.cancel()
		try {
			Class connectionImplClass = thisClassLoader.loadClass("com.mysql.jdbc.ConnectionImpl");//$NON-NLS-1$
			if (connectionImplClass != null && connectionImplClass.getClassLoader() == thisClassLoader) {
				Field cancelTimerField = connectionImplClass.getDeclaredField("cancelTimer");//$NON-NLS-1$
				cancelTimerField.setAccessible(true);
				Timer cancelTimer = (Timer) cancelTimerField.get(null);
				cancelTimer.cancel();
			}
		} catch (Throwable ignore) {
		}

		// Jakarta Commons Logging <= 1.0.4
		// org.apache.commons.logging.LogFactory.release(Thread.currentThread().getContextClassLoader());
		try {
			ClassLoader loader = thisClassLoader;
			while (loader != null) {
				Class logFactoryClass = loader.loadClass("org.apache.commons.logging.LogFactory");//$NON-NLS-1$
				Method releaseMethod = logFactoryClass.getMethod("release", new Class[] {ClassLoader.class});//$NON-NLS-1$
				releaseMethod.invoke(null, new Object[] {thisClassLoader});
				loader = logFactoryClass.getClassLoader().getParent();
			}
		} catch (Throwable ignore) {
		}

		// LOGBack
		// new ch.qos.logback.classic.selector.servlet.ContextDetachingSCL().contextDestroyed(ServletContextEvent sce)
		try {
			Class cdClass = thisClassLoader.loadClass("ch.qos.logback.classic.selector.servlet.ContextDetachingSCL");//$NON-NLS-1$
			Object instance = cdClass.newInstance();
			Method destroyMethod = cdClass.getMethod("contextDestroyed", new Class[] {ServletContextEvent.class});//$NON-NLS-1$
			destroyMethod.invoke(instance, new Object[] {new ServletContextEvent(servletContext)});
		} catch (Throwable ignore) {
		}

		// Apache Logging Log4J 1.x
		// org.apache.log4j.LogManager.shutdown();
		try {
			Class lmClass = thisClassLoader.loadClass("org.apache.log4j.LogManager");//$NON-NLS-1$
			Method shutdownMethod = lmClass.getMethod("shutdown", null);//$NON-NLS-1$
			shutdownMethod.invoke(null, null);
		} catch (Throwable ignore) {
		}

		// java.sql.DriverManager - Evil Singleton
		// Although registering the JDBC driver in your web app is a horrible, horrible thing to do (the container should always manage your connections), some apps do just that.
		// Unregister JDBC drivers during shutdown: remove any drivers that were loaded by the same classloader that loaded the web app.
		/*
		 * DriverManager.getDrivers() has a nasty side-effect of registering
		 * drivers that are visible to this class loader but haven't yet been
		 * loaded. Therefore, the first call to this method a) gets the list
		 * of originally loaded drivers and b) triggers the unwanted
		 * side-effect. The second call gets the complete list of drivers
		 * ensuring that both original drivers and any loaded as a result of the
		 * side-effects are all de-registered.
		 */
		DriverManager.getDrivers();
		Enumeration drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			Driver o = (Driver) drivers.nextElement();
			if (o.getClass().getClassLoader() == thisClassLoader) {
				// Current driver 'o' is being deregistered
				try {
					DriverManager.deregisterDriver(o);
				} catch (SQLException sqle) {
					//throw new RuntimeException(sqle);
					System.err.println("Failed to cleanup DriverManager " + o + " for webapp: " + servletContext.getServletContextName());
					sqle.printStackTrace(System.err);
				}
			} else {
				// Driver 'o' wasn't loaded by this webapp, so no touching it
			}
		}

		/*
		 * java.util.ResourceBundle cache
		 *
		 * Clear the ResourceBundle cache of any bundles loaded by this class loader
	 	 * or any class loader where this loader is a parent class loader.
	 	 *
		 * The ResourceBundle is using WeakReferences so it shouldn't be pinning the
		 * class loader in memory. However, it is. Therefore clear out the
		 * references.
		 *
		 * First try ResourceBundle.clearCache(ClassLoader) (Java 6),
		 * default do manually iterating if running Java <= 5
		 */
		try {
			Method clearCache = ResourceBundle.class.getMethod("clearCache", new Class[] {ClassLoader.class});//$NON-NLS-1$
			if (clearCache != null) {
				// Java >= 6
				clearCache.invoke(null, new Object[] {thisClassLoader});
			} else {
				// Java <= 5
//				Set<CacheKey> set = ResourceBundle.cacheList.keySet();
//				for (CacheKey key : set) {
//					if (key.getLoader() == thisClassLoader) {
//						set.remove(key);
//					}
//				}
				Field cacheListField = ResourceBundle.class.getDeclaredField("cacheList");//$NON-NLS-1$
				cacheListField.setAccessible(true);
				// Java >= 6 uses ConcurrentMap extends Map
				// Java <= 5 uses SoftCache extends AbstractMap implements Map
				Map cacheList = (Map) cacheListField.get(null);
				// Class loader references are in the key
				Iterator keysIter = cacheList.keySet().iterator();
				Field loaderRefField = null;
				while (keysIter.hasNext()) {
					Object key = keysIter.next();
					if (loaderRefField == null) {
						loaderRefField = key.getClass().getDeclaredField("loaderRef");//$NON-NLS-1$
						loaderRefField.setAccessible(true);
					}
					Reference loaderRef = (Reference) loaderRefField.get(key);
					ClassLoader loader = (ClassLoader) loaderRef.get();
					while (loader != null && loader != thisClassLoader) {
						loader = loader.getParent();
					}
					if (loader != null) {
						keysIter.remove();
					}
				}
			}
		} catch (Throwable ignore) {
		}

		// java.beans.Introspector - Slightly less Evil singleton
		// Flushes the cache of classes
		// Note this is going to clear ALL the classes, regardless of what ClassLoader or application they came from, but it's all that's available.
		Introspector.flushCaches();
	}

	/**
	 * {@inheritDoc}
	 */
	public void contextInitialized(ServletContext servletContext) {
		// Source: org.apache.catalina.core.JreMemoryLeakPreventionListener
		/*
		 * Several components end up opening JarURLConnections without first
		 * disabling caching. This effectively locks the file. Whilst more
		 * noticeable and harder to ignore on Windows, it affects all
		 * operating systems.
		 *
		 * Those libraries/components known to trigger this issue include:
		 * - log4j versions 1.2.15 and earlier
		 * - javax.xml.bind.JAXBContext.newInstance()
		 */
		// Set the default URL caching policy to not to cache
		try {
			// Doesn't matter that this JAR doesn't exist - just as long as
			// the URL is well-formed
			// The setDefaultUseCaches should have been static...
			// http://bugs.sun.com/view_bug.do?bug_id=4528126
			URL url = new URL("jar:file://dummy.jar!/");
			URLConnection uConn = url.openConnection();
			uConn.setDefaultUseCaches(false);
		} catch (MalformedURLException ignore) {
			throw new IllegalArgumentException(ignore.toString());
		} catch (IOException ignore) {
			System.err.println("Failed to set URLConnection#defaultUseCache(false): " + ignore.getLocalizedMessage());
		}
	}

}
