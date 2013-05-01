/**
 * 
 */
package clime.messadmin.providers.spi;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author C&eacute;drik LIME
 */
public interface RequestExceptionProvider extends BaseProvider {

	/**
	 * Notification that the servlet request processing generated an exception.
	 */
	void requestException(Exception e, HttpServletRequest request, HttpServletResponse response, ServletContext servletContext);
}
