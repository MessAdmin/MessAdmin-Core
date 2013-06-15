/**
 *
 */
package clime.messadmin.providers.spi;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import clime.messadmin.model.IApplicationInfo;
import clime.messadmin.model.IServerInfo;
import clime.messadmin.model.ISessionInfo;
import clime.messadmin.providers.ProviderUtils;

/**
 * Provider for displaying the administration data.
 * Sample implementation can be HTML, text, CSV...
 * @author C&eacute;drik LIME
 * @since 4.1
 */
public interface DisplayFormatProvider extends BaseProvider {
	public static class Util {
		public static final String FORMAT_PARAMETER_NAME = "format";//$NON-NLS-1$
		public static final String DEFAULT_FORMAT = "html";//$NON-NLS-1$
		public static DisplayFormatProvider getInstance(HttpServletRequest request) {
			String format = request.getParameter(FORMAT_PARAMETER_NAME);
			if (format == null) {
				format = DEFAULT_FORMAT;
			}
			return getInstance(format);
		}
		public static DisplayFormatProvider getInstance(String format) {
			for (DisplayFormatProvider provider : ProviderUtils.getProviders(DisplayFormatProvider.class)) {
				if (format.equalsIgnoreCase(provider.getFormatID())) {
					return provider;
				}
			}
			throw new IllegalArgumentException(format);
		}
	}

	/**
	 * e.g. "html", "text", "xml", "csv"
	 */
	String getFormatID();

	/**
	 * e.g. "text/html;charset=UTF-8", "application/xhtml+xml;charset=UTF-8", "text/plain;charset=UTF-8"
	 */
	String getContentType();

	/**
	 * @see javax.servlet.Servlet#init(ServletConfig)
	 */
	void init(ServletConfig config) throws ServletException;

	/**
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 */
	void preProcess(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;

	/**
	 * Displays the Web Applications list
	 * @param req
	 * @param resp
	 * @param applicationInfos
	 * @throws ServletException
	 * @throws IOException
	 */
	void displayWebAppsList(HttpServletRequest req, HttpServletResponse resp,
			Set/*<IApplicationInfo>*/ applicationInfos) throws ServletException, IOException;

	/**
	 * Displays the HttpSessions list for a given WebApp (context)
	 * @param req
	 * @param resp
	 * @param sortBy
	 * @param orderBy
	 * @param webAppStats
	 * @param activeSessions
	 * @param passiveSessionsIds
	 * @throws ServletException
	 * @throws IOException
	 */
	void displaySessionsListPage(HttpServletRequest req, HttpServletResponse resp,
			String sortBy, String orderBy,
			IApplicationInfo webAppStats, Collection/*<ISessionInfo>*/ activeSessions, Collection/*<String>*/ passiveSessionsIds)
			throws ServletException, IOException;

	/**
	 * Displays the details page for a given HttpSession
	 * @param req
	 * @param resp
	 * @param webAppStats
	 * @param currentSession
	 * @throws ServletException
	 * @throws IOException
	 */
	void displaySessionDetailPage(HttpServletRequest req, HttpServletResponse resp,
			IApplicationInfo webAppStats, ISessionInfo currentSession) throws ServletException, IOException;

	/**
	 * Displays the details page for a Web Application
	 * @param req
	 * @param resp
	 * @param webAppStats
	 * @throws ServletException
	 * @throws IOException
	 */
	void displayWebAppStatsPage(HttpServletRequest req, HttpServletResponse resp,
			IApplicationInfo webAppStats) throws ServletException, IOException;

	/**
	 * Displays the page for the Server Informations
	 * @param req
	 * @param resp
	 * @param serverInfo
	 * @throws ServletException
	 * @throws IOException
	 */
	void displayServerInfosPage(HttpServletRequest req, HttpServletResponse resp,
			IServerInfo serverInfo) throws ServletException, IOException;
}
