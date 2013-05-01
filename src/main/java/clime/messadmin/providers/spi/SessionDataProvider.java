/**
 * 
 */
package clime.messadmin.providers.spi;

import javax.servlet.http.HttpSession;

/**
 * Use this plug-in to display session-specific data.
 * See {@link clime.messadmin.providers.spi.BaseTabularSessionDataProvider} for displaying tabular data.
 * @author C&eacute;drik LIME
 */
public interface SessionDataProvider extends DisplayProvider {

	/**
	 * @param session
	 * @return application-specific XHTML data for given HttpSession, or null if it can be determined
	 */
	String getXHTMLSessionData(HttpSession session);

	/**
	 * @param session
	 * @return application-specific data title for given HttpSession, or null if it can be determined
	 */
	String getSessionDataTitle(HttpSession session);
}
