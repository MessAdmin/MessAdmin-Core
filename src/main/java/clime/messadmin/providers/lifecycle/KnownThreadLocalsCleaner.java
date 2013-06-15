/**
 *
 */
package clime.messadmin.providers.lifecycle;

import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import clime.messadmin.providers.spi.RequestExceptionProvider;
import clime.messadmin.providers.spi.RequestLifeCycleProvider;

/**
 * Takes care of clean (some of) the ThreadLocals
 * thereby avoiding (well, trying to avoid)
 * OOM (java.lang.OutOfMemoryError) on hot restart...
 *
 * @see <a href="http://opensource2.atlassian.com/confluence/spring/pages/viewpage.action?pageId=2669">Memory leaks where the classloader cannot be garbage collected</a>
 * @author C&eacute;drik LIME
 * @since 4.1
 */
public class KnownThreadLocalsCleaner implements RequestLifeCycleProvider, RequestExceptionProvider {
	/* From ProviderUtils:
	 * "Providers are cached, keyed by its Interface, and by a ClassLoader.
	 * This enables different WebApps (different ClassLoaders) to have their own set of plugins (same Interface)."
	 *
	 * Thus we have a copy of plugins per webapp. Which means the ClassLoader is an invariant for each
	 * instance of this class.
	 */

	private Method log4jNDCRemoveMethod;
	private Method log4jMDCContextMethod;
	private Method log4jMDCClearMethod;
	private Method logBackMDCClearMethod;
	private Method slf4jMDCClearMethod;

	/**
	 *
	 */
	public KnownThreadLocalsCleaner() {
		super();
		final ClassLoader thisClassLoader = Thread.currentThread().getContextClassLoader();//this.getClass().getClassLoader();
		// Apache Logging Log4J 1.x
		// org.apache.log4j.NDC.remove();
		try {
			Class log4jNdcClass = thisClassLoader.loadClass("org.apache.log4j.NDC");//$NON-NLS-1$
			log4jNDCRemoveMethod = log4jNdcClass.getMethod("remove");//$NON-NLS-1$
		} catch (Throwable e) {
			// ignore
		}
		// org.apache.log4j.MDC.getContext().clear();
		try {
			Class log4jMdcClass = thisClassLoader.loadClass("org.apache.log4j.MDC");//$NON-NLS-1$
			log4jMDCContextMethod = log4jMdcClass.getMethod("getContext");//$NON-NLS-1$
		} catch (Throwable e) {
			// ignore
		}
		// @since Log4J 1.2.16, only useful @since Log4J 1.2.17
		// org.apache.log4j.MDC.clear();
		try {
			Class log4jMdcClass = thisClassLoader.loadClass("org.apache.log4j.MDC");//$NON-NLS-1$
			log4jMDCClearMethod = log4jMdcClass.getMethod("clear");//$NON-NLS-1$
		} catch (Throwable e) {
			// ignore
		}
		// LOGBack
		// ch.qos.logback.classic.MDC.clear();
		try {
			Class logBackMdcClass = thisClassLoader.loadClass("ch.qos.logback.classic.MDC");//$NON-NLS-1$
			logBackMDCClearMethod = logBackMdcClass.getMethod("clear");//$NON-NLS-1$
		} catch (Throwable e) {
			// ignore
		}
		// SLF4J
		// org.slf4j.MDC.clear();
		try {
			Class slf4jMdcClass = thisClassLoader.loadClass("org.slf4j.MDC");//$NON-NLS-1$
			slf4jMDCClearMethod = slf4jMdcClass.getMethod("clear");//$NON-NLS-1$
		} catch (Throwable e) {
			// ignore
		}
	}

	/** {@inheritDoc} */
	public int getPriority() {
		return Integer.MAX_VALUE;
	}

	/** {@inheritDoc} */
	public void requestInitialized(HttpServletRequest request,
			HttpServletResponse response, ServletContext servletContext) {
		// do nothing
	}

	/** {@inheritDoc} */
	public void requestDestroyed(HttpServletRequest request,
			HttpServletResponse response, ServletContext servletContext) {
		cleanThreadLocals();
	}

	/** {@inheritDoc} */
	public void requestException(Exception e, HttpServletRequest request,
			HttpServletResponse response, ServletContext servletContext) {
		cleanThreadLocals();
	}

	protected void cleanThreadLocals() {
		// ThreadLocals - With Great Power, comes Great Responsibility
		// Can't do anything here. You need to manually set all your ThreadLocals to null yourself (myThreadLocal.remove() or myThreadLocal.set(null))...

		// Apache Logging Log4J 1.2
		// org.apache.log4j.NDC.remove();
		if (log4jNDCRemoveMethod != null) {
			try {
				log4jNDCRemoveMethod.invoke(null);
			} catch (Exception e) {
				// do nothing
			}
		}
		// org.apache.log4j.MDC.clear();
		if (log4jMDCClearMethod != null) {
			try {
				log4jMDCClearMethod.invoke(null);
			} catch (Exception e) {
				// do nothing
			}
		} else {
			// org.apache.log4j.MDC.getContext().clear();
			if (log4jMDCContextMethod != null) {
				try {
					Map context = (Map) log4jMDCContextMethod.invoke(null);
					if (context != null) {
						context.clear();
					}
				} catch (Exception e) {
					// do nothing
				}
			}
		}

		// LOGBack
		// ch.qos.logback.classic.MDC.clear();
		if (logBackMDCClearMethod != null) {
			try {
				logBackMDCClearMethod.invoke(null);
			} catch (Exception e) {
				// do nothing
			}
		}

		// SLF4J
		// org.slf4j.MDC.clear();
		if (slf4jMDCClearMethod != null) {
			try {
				slf4jMDCClearMethod.invoke(null);
			} catch (Exception e) {
				// do nothing
			}
		}
	}
}
