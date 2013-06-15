/**
 * 
 */
package clime.messadmin.model;

import java.util.EventListener;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Implementations of this interface receive notifications about changes
 * to the servlet request of the web application they are part of.
 * Implementation note: we can't use a RequestLifeCycleProvider, as we need our internal wrapper
 * @author C&eacute;drik LIME
 * @see javax.servlet.ServletRequestListener
 * @since Servlet 2.4
 */
public interface IRequestListener extends EventListener {

	/**
	 * Notification that the servlet request is about to go into scope.
	 */
	void requestInitialized(HttpServletRequest request, ServletContext servletContext);

	/**
	 * Notification that the servlet request is about to go out of scope.
	 * WARNING: {@code response} can be {@code null}!
	 */
	void requestDestroyed(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext);

	/**
	 * Notification that the servlet request processing generated an exception.
	 */
	void requestException(Exception e, HttpServletRequest request, HttpServletResponse response, ServletContext servletContext);
}
