/**
 * 
 */
package clime.messadmin.providers.spi;

import javax.servlet.ServletContext;

/**
 * Note: the invocation order of Providers is reversed in case of session/application destruction.
 * @author C&eacute;drik LIME
 */
public interface ApplicationLifeCycleProvider extends BaseProvider {

	/**
	 * Notification that the web application initialization
	 * process is starting.
	 * All ServletContextListeners are notified of context
	 * initialization before any filter or servlet in the web
	 * application is initialized.
	 */
    void contextInitialized(ServletContext servletContext);

	/**
	 * Notification that the servlet context is about to be shut down.
	 * All servlets and filters have been destroy()ed before any
	 * ServletContextListeners are notified of context
	 * destruction.
	 */
    void contextDestroyed(ServletContext servletContext);
}
