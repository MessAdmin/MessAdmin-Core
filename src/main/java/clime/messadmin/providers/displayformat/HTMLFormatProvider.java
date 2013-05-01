/**
 *
 */
package clime.messadmin.providers.displayformat;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import clime.messadmin.model.IApplicationInfo;
import clime.messadmin.model.IServerInfo;
import clime.messadmin.model.ISessionInfo;
import clime.messadmin.providers.spi.DisplayFormatProvider;

/**
* IMPLEMENTATION NOTE: always use include() instead of forward(), to get the real name of this servlet in jsp's
* @author C&eacute;drik LIME
* @since 4.1
*/
public class HTMLFormatProvider implements DisplayFormatProvider {
	public static final String ID = "html";//$NON-NLS-1$

	private transient ServletConfig config;

	protected String staticFilesRoot = "/MessAdmin/";// css, js, etc. //$NON-NLS-1$
	protected String webFilesRoot = "/MessAdmin/";//$NON-NLS-1$
	protected String serverInfosJspPath = webFilesRoot + "serverInfos.jsp";//$NON-NLS-1$
	protected String webAppsListJspPath = webFilesRoot + "webAppsList.jsp";//$NON-NLS-1$
	protected String webAppStatsJspPath = webFilesRoot + "webAppStats.jsp";//$NON-NLS-1$
	protected String sessionsListJspPath = webFilesRoot + "sessionsList.jsp";//$NON-NLS-1$
	protected String sessionDetailJspPath = webFilesRoot + "sessionDetail.jsp";//$NON-NLS-1$

	public HTMLFormatProvider() {
		super();
	}

	/** {@inheritDoc} */
	public int getPriority() {
		return 0;
	}

	/** {@inheritDoc} */
	public String getFormatID() {
		return ID;
	}

	/** {@inheritDoc} */
	public String getContentType() {
		return "text/html;charset=UTF-8";//should be "application/xhtml+xml", but IE screws us once again... //$NON-NLS-1$
	}

	/** {@inheritDoc} */
	public void init(ServletConfig config) throws ServletException {
		this.config = config;
		String initStaticFilesRoot = getServletConfig().getInitParameter("StaticFilesRoot");//$NON-NLS-1$
		if (initStaticFilesRoot != null) {
			staticFilesRoot = initStaticFilesRoot;
		}
		if (! staticFilesRoot.startsWith("/")) {//$NON-NLS-1$
			staticFilesRoot = '/' + staticFilesRoot;
		}
		if (! staticFilesRoot.endsWith("/")) {//$NON-NLS-1$
			staticFilesRoot += '/';
		}

		String initWebFilesRoot = getServletConfig().getInitParameter("WebFilesRoot");//$NON-NLS-1$
		if (null != initWebFilesRoot) {
			webFilesRoot = initWebFilesRoot;
		}
		if (! webFilesRoot.startsWith("/")) {//$NON-NLS-1$
			webFilesRoot = '/' + webFilesRoot;
		}
		if (! webFilesRoot.endsWith("/")) {//$NON-NLS-1$
			webFilesRoot += '/';
		}

		String initServerInfosJspPath = getServletConfig().getInitParameter("ServerInfosJsp");//$NON-NLS-1$
		if (null != initServerInfosJspPath) {
			serverInfosJspPath = webFilesRoot + initServerInfosJspPath;
		}
		String initWebAppsListJspPath = getServletConfig().getInitParameter("WebAppsListJsp");//$NON-NLS-1$
		if (null != initWebAppsListJspPath) {
			webAppsListJspPath = webFilesRoot + initWebAppsListJspPath;
		}
		String initWebAppStatsJspPathJspPath = getServletConfig().getInitParameter("WebAppStatsJsp");//$NON-NLS-1$
		if (null != initWebAppStatsJspPathJspPath) {
			webAppStatsJspPath = webFilesRoot + initWebAppStatsJspPathJspPath;
		}
		String initSessionsListJspPath = getServletConfig().getInitParameter("SessionsListJsp");//$NON-NLS-1$
		if (null != initSessionsListJspPath) {
			sessionsListJspPath = webFilesRoot + initSessionsListJspPath;
		}
		String initSessionDetailJspPath = getServletConfig().getInitParameter("SessionDetailJsp");//$NON-NLS-1$
		if (null != initSessionDetailJspPath) {
			sessionDetailJspPath = webFilesRoot + initSessionDetailJspPath;
		}
	}
	/**
	 * Returns this servlet's {@link ServletConfig} object.
	 *
	 * @return ServletConfig 	the <code>ServletConfig</code> object
	 *				that initialized this servlet
	 */
	public ServletConfig getServletConfig() {
		return config;
	}
	/**
	 * Returns a reference to the {@link ServletContext} in which this servlet
	 * is running.  See {@link ServletConfig#getServletContext}.
	 *
	 * <p>This method is supplied for convenience. It gets the
	 * context from the servlet's <code>ServletConfig</code> object.
	 *
	 * @return ServletContext 	the <code>ServletContext</code> object
	 *				passed to this servlet by the <code>init</code>
	 *				method
	 */
	public ServletContext getServletContext() {
		ServletConfig sc = getServletConfig();
		if (sc == null) {
			throw new IllegalStateException("err.servlet_config_not_initialized");
		}
		return sc.getServletContext();
	}

	/** {@inheritDoc} */
	public void preProcess(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// Some attributes needed by some or all JSPs
		req.setAttribute("autorefresh", req.getParameter("autorefresh"));//$NON-NLS-1$ //$NON-NLS-2$
		req.setAttribute("StaticFilesRoot", req.getContextPath() + staticFilesRoot);//$NON-NLS-1$
		req.setAttribute("WebFilesRoot", req.getContextPath() + webFilesRoot);//$NON-NLS-1$
	}

	/** {@inheritDoc} */
	public void displayWebAppsList(HttpServletRequest req, HttpServletResponse resp,
			Set/*<IApplicationInfo>*/ applicationInfos) throws ServletException, IOException {
		req.setAttribute("applications", applicationInfos);//$NON-NLS-1$
		getServletContext().getRequestDispatcher(webAppsListJspPath).include(req, resp);
	}

	/** {@inheritDoc} */
	public void displaySessionsListPage(HttpServletRequest req, HttpServletResponse resp,
			String sortBy, String orderBy,
			IApplicationInfo webAppStats, Collection/*<ISessionInfo>*/ activeSessions, Collection/*<String>*/ passiveSessionsIds)
			throws ServletException, IOException {
		// keep sort order
		req.setAttribute("sort", sortBy);//$NON-NLS-1$
		req.setAttribute("order", orderBy);//$NON-NLS-1$
		req.setAttribute("activeSessions", activeSessions);//$NON-NLS-1$
		req.setAttribute("passiveSessionsIds", passiveSessionsIds);//$NON-NLS-1$
		req.setAttribute("webAppStats", webAppStats);//$NON-NLS-1$
		getServletContext().getRequestDispatcher(sessionsListJspPath).include(req, resp);
	}

	/** {@inheritDoc} */
	public void displaySessionDetailPage(HttpServletRequest req, HttpServletResponse resp,
			IApplicationInfo webAppStats, ISessionInfo currentSession) throws ServletException, IOException {
		req.setAttribute("webAppStats", webAppStats);//$NON-NLS-1$
		req.setAttribute("currentSession", currentSession);//$NON-NLS-1$
		getServletContext().getRequestDispatcher(sessionDetailJspPath).include(req, resp);
	}

	/** {@inheritDoc} */
	public void displayWebAppStatsPage(HttpServletRequest req, HttpServletResponse resp,
			IApplicationInfo webAppStats) throws ServletException, IOException {
		req.setAttribute("webAppStats", webAppStats);//$NON-NLS-1$
		getServletContext().getRequestDispatcher(webAppStatsJspPath).include(req, resp);
	}

	/** {@inheritDoc} */
	public void displayServerInfosPage(HttpServletRequest req, HttpServletResponse resp,
			IServerInfo serverInfo) throws ServletException, IOException {
		req.setAttribute("serverInfos", serverInfo);//$NON-NLS-1$
		getServletContext().getRequestDispatcher(serverInfosJspPath).include(req, resp);
	}


	/**
	 * @return the staticFilesRoot
	 */
	public String getStaticFilesRoot() {
		return staticFilesRoot;
	}

	/**
	 * @return the webFilesRoot
	 */
	public String getWebFilesRoot() {
		return webFilesRoot;
	}

}
