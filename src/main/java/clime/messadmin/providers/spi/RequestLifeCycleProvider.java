/**
 * 
 */
package clime.messadmin.providers.spi;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Note: this is about the same as a ServletFilter or a ServletRequestListener, except you don't have to change your web.xml and call chain.doFilter()
 * Note: the invocation order of Providers is reversed in case of session/application destruction.
 * @author C&eacute;drik LIME
 */
public interface RequestLifeCycleProvider extends BaseProvider {

	/**
	 * Notification that the servlet request is about to go into scope.
	 * WARNING: {@code response} can be {@code null}!
	 */
	void requestInitialized(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext);

	/**
	 * Notification that the servlet request is about to go out of scope.
	 * WARNING: {@code response} can be {@code null}!
	 */
	void requestDestroyed(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext);
}
