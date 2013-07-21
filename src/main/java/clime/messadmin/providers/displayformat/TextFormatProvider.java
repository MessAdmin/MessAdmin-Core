/**
 *
 */
package clime.messadmin.providers.displayformat;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import clime.messadmin.i18n.I18NSupport;
import clime.messadmin.model.IApplicationInfo;
import clime.messadmin.model.IServerInfo;
import clime.messadmin.model.ISessionInfo;
import clime.messadmin.providers.spi.DisplayFormatProvider;
import clime.messadmin.providers.spi.SerializableProvider;
import clime.messadmin.providers.spi.SizeOfProvider;
import clime.messadmin.utils.BytesFormat;

/**
 * @author C&eacute;drik LIME
 * @since 4.1
 */
public class TextFormatProvider implements DisplayFormatProvider {
	public static final String ID = "text";//$NON-NLS-1$

	/**
	 * Modified ISO 8601 datetime format: {@value}
	 * @see <a href="http://www.w3.org/TR/NOTE-datetime">ISO 8601 / European Standard EN 28601  DateTime</a>
	 * @see <a href="http://www.ietf.org/rfc/rfc3339.txt">RFC 3399</a>
	 */
	protected static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";//$NON-NLS-1$
	private transient ServletConfig config;

	public TextFormatProvider() {
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
		return "text/plain;charset=UTF-8";//$NON-NLS-1$
	}

	/** {@inheritDoc} */
	public void init(ServletConfig config) throws ServletException {
		this.config = config;
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

	/* (non-Javadoc)
	 * @see clime.messadmin.providers.spi.DisplayFormatProvider#preProcess(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void preProcess(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		// nothing
	}

	/** {@inheritDoc} */
	public void displayWebAppsList(HttpServletRequest req, HttpServletResponse resp,
			Set/*<IApplicationInfo>*/ applicationInfos) throws ServletException, IOException {
		NumberFormat numberFormat = NumberFormat.getNumberInstance(I18NSupport.getAdminLocale());
		PrintWriter out = resp.getWriter();
		writeMenu(req, resp, out);
		out.println();
		out.println("List of available applications:");
		out.println();
		Iterator iter = applicationInfos.iterator();
		while (iter.hasNext()) {
			IApplicationInfo app = (IApplicationInfo) iter.next();
			writeMenu(req, resp, out, app);
			out.println("Id:\t\t\t" + app.getInternalContextPath());
			out.println("Context:\t\t" + app.getContextPath());
			out.println("Name:\t\t\t" + app.getServletContextName());
			out.println("# active sessions:\t" + numberFormat.format(app.getActiveSessionsCount()));
			out.println();
		}
		out.close();
	}

	/** {@inheritDoc} */
	public void displaySessionsListPage(HttpServletRequest req, HttpServletResponse resp,
			String sortBy, String orderBy,
			IApplicationInfo webAppStats, Collection/*<ISessionInfo>*/ activeSessions, Collection/*<String>*/ passiveSessionsIds)
			throws ServletException, IOException {
		BytesFormat bytesFormat = BytesFormat.getBytesInstance(I18NSupport.getAdminLocale(), false);
		NumberFormat numberFormat = NumberFormat.getNumberInstance(I18NSupport.getAdminLocale());
		DateFormat dateTimeFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, I18NSupport.getAdminLocale());
		((SimpleDateFormat)dateTimeFormat).applyPattern(DATE_TIME_PATTERN);
		PrintWriter out = resp.getWriter();
		writeMenu(req, resp, out);
		writeMenu(req, resp, out, webAppStats);
		out.println();
		out.println("" + numberFormat.format(activeSessions.size()) + " active sessions for " + webAppStats.getContextPath());
		out.println("(" + numberFormat.format(passiveSessionsIds.size()) + " passivated sessions)");
		out.println();
		Iterator iter = activeSessions.iterator();
		while (iter.hasNext()) {
			ISessionInfo session = (ISessionInfo) iter.next();
			writeMenu(req, resp, out, webAppStats, session);
			out.println("Id:\t\t\t" + session.getId());
			out.println("Locale:\t\t\t" + session.getGuessedLocale());
			out.println("User:\t\t\t" + session.getGuessedUser());
			out.println("Creation date:\t\t" + dateTimeFormat.format(new Date(session.getCreationTime())));
			out.println("Last access time:\t" + dateTimeFormat.format(new Date(session.getLastAccessedTime())));
			out.println("Idle time:\t\t" + session.getIdleTime());
			out.println("TTL:\t\t\t" + session.getTTL());
			out.println("Size:\t\t\t" + bytesFormat.format(session.getSize()));
			out.println();
		}
		out.close();
	}

	/** {@inheritDoc} */
	public void displaySessionDetailPage(HttpServletRequest req, HttpServletResponse resp,
			IApplicationInfo webAppStats, ISessionInfo currentSession) throws ServletException, IOException {
		BytesFormat bytesFormat = BytesFormat.getBytesInstance(I18NSupport.getAdminLocale(), false);
		NumberFormat numberFormat = NumberFormat.getNumberInstance(I18NSupport.getAdminLocale());
		PrintWriter out = resp.getWriter();
		writeMenu(req, resp, out);
		writeMenu(req, resp, out, webAppStats);
		out.println();
		out.println("Id:\t\t" + currentSession.getId());
		out.println("Remote host:\t" + currentSession.getRemoteHost());
		out.println("User:\t\t" + currentSession.getGuessedUser());
		out.println("Locale:\t\t" + currentSession.getGuessedLocale());
		out.println("# hits:\t\t" + numberFormat.format(currentSession.getHits()));
		out.println("Size:\t\t" + bytesFormat.format(currentSession.getSize()));
		out.println();
		// Session attributes
		Map<String, Object> attributes = currentSession.getAttributes();
		out.println("# attributes:\t" + numberFormat.format(attributes.size()));
		Iterator<Map.Entry<String, Object>> iter = attributes.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, Object> entry = iter.next();
			out.println("\tAttribute name:\t" + entry.getKey());
			out.println("\tAttribute Class:\t" + (entry.getValue() != null ? entry.getValue().getClass().getName() : ""));
			out.println("\tAttribute size:\t"
					+ bytesFormat.format(SizeOfProvider.Util.getObjectSize(entry.getValue(), webAppStats.getClassLoader())));
			if (! SerializableProvider.Util.isSerializable(entry.getValue(), webAppStats.getClassLoader())) {
				out.println("\tAttribute is not Serializable!");
			}
			out.println();
		}
		out.close();
	}

	/** {@inheritDoc} */
	public void displayWebAppStatsPage(HttpServletRequest req, HttpServletResponse resp,
			IApplicationInfo webAppStats) throws ServletException, IOException {
		BytesFormat bytesFormat = BytesFormat.getBytesInstance(I18NSupport.getAdminLocale(), false);
		NumberFormat numberFormat = NumberFormat.getNumberInstance(I18NSupport.getAdminLocale());
		DateFormat dateTimeFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, I18NSupport.getAdminLocale());
		((SimpleDateFormat)dateTimeFormat).applyPattern(DATE_TIME_PATTERN);
		PrintWriter out = resp.getWriter();
		writeMenu(req, resp, out);
		writeMenu(req, resp, out, webAppStats);
		out.println();
		out.println("Details for " + webAppStats.getContextPath() + " (" + webAppStats.getServletContextName() + ')');
		out.println();
		out.println("# active sessions:\t" + numberFormat.format(webAppStats.getActiveSessionsCount()));
		out.println("# passive sessions:\t" + numberFormat.format(webAppStats.getPassiveSessionsCount()));
		out.println("Total sessions size:\t" + bytesFormat.format(webAppStats.getActiveSessionsSize()));
		out.println("# created sessions:\t" + numberFormat.format(webAppStats.getTotalCreatedSessions()));
		out.println("max concurrent sessions:\t" + numberFormat.format(webAppStats.getMaxConcurrentSessions()));
		out.println();
		out.println("# hits:\t\t\t" + numberFormat.format(webAppStats.getHits()));
		out.println("Mean requests size:\t" + bytesFormat.format(webAppStats.getRequestMeanLength()));
		out.println("Total requests size:\t" + bytesFormat.format(webAppStats.getRequestTotalLength()));
		out.println("Mean response size:\t" + bytesFormat.format(webAppStats.getResponseMeanLength()));
		out.println("Total response size:\t" + bytesFormat.format(webAppStats.getResponseTotalLength()));
		out.println();
		// ServletContext attributes
		Map<String, Object> attributes = webAppStats.getAttributes();
		out.println("# attributes:\t" + numberFormat.format(attributes.size()));
		Iterator<Map.Entry<String, Object>> iter = attributes.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, Object> entry = iter.next();
			out.println("\tAttribute name:\t" + entry.getKey());
			out.println("\tAttribute Class:\t" + (entry.getValue() != null ? entry.getValue().getClass().getName() : ""));
			out.println("\tAttribute size:\t"
					+ bytesFormat.format(SizeOfProvider.Util.getObjectSize(entry.getValue(), webAppStats.getClassLoader())));
			out.println();
		}
		out.close();
	}

	/** {@inheritDoc} */
	public void displayServerInfosPage(HttpServletRequest req, HttpServletResponse resp,
			IServerInfo serverInfo) throws ServletException, IOException {
		BytesFormat bytesFormat = BytesFormat.getBytesInstance(I18NSupport.getAdminLocale(), false);
		NumberFormat numberFormat = NumberFormat.getNumberInstance(I18NSupport.getAdminLocale());
		DateFormat dateTimeFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, I18NSupport.getAdminLocale());
		((SimpleDateFormat)dateTimeFormat).applyPattern(DATE_TIME_PATTERN);
		PrintWriter out = resp.getWriter();
		writeMenu(req, resp, out);
		out.println();
		out.println("Server Information");
		out.println();
		out.println("Startup time:\t" + dateTimeFormat.format(serverInfo.getStartupTime()));
		out.println("# CPUs:\t\t" + numberFormat.format(serverInfo.getAvailableProcessors()));
		if (serverInfo.getSystemLoadAverage() >= 0) {
			out.println("System load average:\t" + numberFormat.format(serverInfo.getSystemLoadAverage()));
		}
		out.println("Free memory:\t" + bytesFormat.format(serverInfo.getFreeMemory()));
		out.println("Total memory:\t" + bytesFormat.format(serverInfo.getTotalMemory()));
		out.println("Max memory:\t" + bytesFormat.format(serverInfo.getMaxMemory()));
		out.close();
	}

	protected void writeMenu(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) {
		out.print("Server information:\t");
		out.println(resp.encodeURL(req.getRequestURL().append("?action=serverInfos&format=").append(getFormatID()).toString()));
		out.print("Applications list:\t");
		out.println(resp.encodeURL(req.getRequestURL().append("?action=webAppsList&format=").append(getFormatID()).toString()));
	}
	protected void writeMenu(HttpServletRequest req, HttpServletResponse resp, PrintWriter out, IApplicationInfo app) {
		out.print("Application stats:\t");
		out.println(resp.encodeURL(req.getRequestURL().append("?context=").append(app.getInternalContextPath()).append("&action=webAppStats&format=").append(getFormatID()).toString()));
		out.print("Sessions list:\t");
		out.println(resp.encodeURL(req.getRequestURL().append("?context=").append(app.getInternalContextPath()).append("&action=sessionsList&format=").append(getFormatID()).toString()));
	}
	protected void writeMenu(HttpServletRequest req, HttpServletResponse resp, PrintWriter out, IApplicationInfo app, ISessionInfo session) {
		out.print("Session details:\t");
		out.println(resp.encodeURL(req.getRequestURL().append("?context=").append(app.getInternalContextPath()).append("&action=sessionDetail&sessionId=").append(session.getId()).append("&format=").append(getFormatID()).toString()));
	}
}
