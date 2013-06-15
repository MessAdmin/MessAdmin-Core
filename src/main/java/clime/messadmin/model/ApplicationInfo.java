/**
 *
 */
package clime.messadmin.model;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import clime.messadmin.model.stats.MinMaxTracker;
import clime.messadmin.providers.ProviderUtils;
import clime.messadmin.providers.spi.ApplicationDataProvider;
import clime.messadmin.providers.spi.SizeOfProvider;
import clime.messadmin.utils.SessionUtils;

/**
 * Container for WebApp-related statistics
 * @author C&eacute;drik LIME
 */
public class ApplicationInfo implements IApplicationInfo {
	private static transient Method getContextPath = null;

	private final ServletContext servletContext;
	private ClassLoader classLoader;

	/**
	 * Do we have full MessAdmin capabilities, or is this
	 * application monitored via the simpler AutoProbe/Servlet2 plugin?
	 * Will be set to {@code true} when/if the MessAdminFilter is initialized.
	 */
	protected volatile boolean messAdminFullMode = false;

	protected final RequestInfo cumulativeRequestStats = new RequestInfo(null);

	protected final long startupTime = System.currentTimeMillis();
	protected MinMaxTracker activeSessions = new MinMaxTracker(0, 0, 0);
	protected volatile int passiveSessionsCount = 0;
	protected volatile long totalCreatedSessions = 0;//simple cumulative counter
	protected volatile int hits = 0;//number of hits

	// cache from HttpServletRequest
	protected volatile String contextPath;

	static {
		// @since Servlet 2.5
		try {
			getContextPath = ServletContext.class.getMethod("getContextPath");//$NON-NLS-1$
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		}
	}

	/**
	 *
	 */
	public ApplicationInfo(ServletContext servletContext) {
		super();
		this.servletContext = servletContext;
//		for (int i = 0; i < responseStatus.length; ++i) {
//			responseStatus[i] = new HitsCounter();
//		}
		if (getContextPath != null) {
			try {
				this.contextPath = (String) getContextPath.invoke(servletContext);
			} catch (Exception ignore) {
				//nothing
			}
		}
	}


	/**
	 * @see #messAdminFullMode
	 */
	public boolean isMessAdminFullMode() {
		return messAdminFullMode;
	}
	public void setMessAdminFullMode(boolean messAdminFullMode) {
		this.messAdminFullMode = messAdminFullMode;
	}

	public ServletContext getServletContext() {
		return servletContext;
	}

	/** {@inheritDoc} */
	public ClassLoader getClassLoader() {
		return classLoader;
	}
	void setClassLoader(ClassLoader cl) {
		classLoader = cl;
	}

	public String getInternalContextPath() {
		String context = SessionUtils.getContext(servletContext);
		return context;
	}

	/** {@inheritDoc} */
	public int getNErrors() {
		return cumulativeRequestStats.getNErrors();
	}
	/** {@inheritDoc} */
	public ErrorData getLastError() {
		return cumulativeRequestStats.getLastError();
	}

	/** {@inheritDoc} */
	public int getHits() {
		return hits;
	}

	/** {@inheritDoc} */
	public long getMaxConcurrentSessions() {
		return activeSessions.getMax(); //concurrentSessions.getMax();
	}

	/** {@inheritDoc} */
	public Date getMaxConcurrentSessionsDate() {
		return activeSessions.getMaxAccessTime(); //concurrentSessions.getMaxAccessTime();
	}

	/** {@inheritDoc} */
	public long getTotalCreatedSessions() {
		return totalCreatedSessions;
	}

	/** {@inheritDoc} */
	public long getRequestTotalLength() {
		return cumulativeRequestStats.getRequestTotalLength();
	}

	/** {@inheritDoc} */
	public long getRequestMaxLength() {
		return cumulativeRequestStats.getRequestMaxLength();
	}

	/** {@inheritDoc} */
	public Date getRequestMaxLengthDate() {
		return cumulativeRequestStats.getRequestMaxLengthDate();
	}

	/** {@inheritDoc} */
	public double getRequestMeanLength() {
		return cumulativeRequestStats.getRequestMeanLength();
	}

	/** {@inheritDoc} */
	public double getRequestStdDevLength() {
		return cumulativeRequestStats.getRequestStdDevLength();
	}

	/** {@inheritDoc} */
	public long getResponseTotalLength() {
		return cumulativeRequestStats.getResponseTotalLength();
	}

	/** {@inheritDoc} */
	public long getResponseMaxLength() {
		return cumulativeRequestStats.getResponseMaxLength();
	}

	/** {@inheritDoc} */
	public Date getResponseMaxLengthDate() {
		return cumulativeRequestStats.getResponseMaxLengthDate();
	}

	/** {@inheritDoc} */
	public double getResponseMeanLength() {
		return cumulativeRequestStats.getResponseMeanLength();
	}

	/** {@inheritDoc} */
	public double getResponseStdDevLength() {
		return cumulativeRequestStats.getResponseStdDevLength();
	}

	/** {@inheritDoc} */
	public ResponseStatusInfo getResponseStatusInfo() {
		return cumulativeRequestStats.getResponseStatusInfo();
	}

	/** {@inheritDoc} */
	public int getActiveSessionsCount() {
		return (int) activeSessions.getLastValue();
	}

	/** {@inheritDoc} */
	public int getPassiveSessionsCount() {
		return passiveSessionsCount;
	}

	/** {@inheritDoc} */
	public long getActiveSessionsSize() {
		/* Copy all sessions' attributes in a temporary structure, to avoid concurrent modifications problems */
		Set sessions = Server.getInstance().getApplication(servletContext).getActiveSessionInfos();
		List sessionsAttributes = new ArrayList(sessions.size());
		final long shellSize = SizeOfProvider.Util.getObjectSize(sessionsAttributes, classLoader);
		Iterator iter = sessions.iterator();
		// for each session
		while (iter.hasNext()) {
			HttpSession httpSession = ((SessionInfo) iter.next()).getHttpSession();
			try {
				Map attributes = new HashMap();
				Enumeration enumeration = httpSession.getAttributeNames();
				// for each session attribute
				while (enumeration.hasMoreElements()) {
					String name = (String) enumeration.nextElement();
					Object attribute = httpSession.getAttribute(name);
					attributes.put(name, attribute);
				}
				sessionsAttributes.add(attributes);
			} catch (IllegalStateException ise) {
				// Session is invalidated: don't count anything
			}
		}
		return Math.max(-1, SizeOfProvider.Util.getObjectSize(sessionsAttributes, classLoader) - shellSize);
	}

	/** {@inheritDoc} */
	public Date getStartupTime() {
		return new Date(startupTime);
	}

	/** {@inheritDoc} */
	public long getUsedTimeTotal() {
		return cumulativeRequestStats.getTotalUsedTime();
	}

	/** {@inheritDoc} */
	public long getUsedTimeMax() {
		return cumulativeRequestStats.getMaxUsedTime();
	}

	/** {@inheritDoc} */
	public Date getUsedTimeMaxDate() {
		return cumulativeRequestStats.getMaxUsedTimeDate();
	}

	/** {@inheritDoc} */
	public double getUsedTimeMean() {
		return cumulativeRequestStats.getMeanUsedTime();
	}

	/** {@inheritDoc} */
	public double getUsedTimeStdDev() {
		return cumulativeRequestStats.getStdDevUsedTime();
	}

	/** {@inheritDoc} */
	public List getApplicationSpecificData() {
		List result = new ArrayList();
		for (ApplicationDataProvider ad : ProviderUtils.getProviders(ApplicationDataProvider.class, classLoader)) {
			result.add(new DisplayDataHolder.ApplicationDataHolder(ad, servletContext));
		}
		return result;
	}

	/** {@inheritDoc} */
	public String getContextPath() {
		return contextPath;
	}

	/** {@inheritDoc} */
	public String getServerInfo() {
		return servletContext.getServerInfo();
	}

	/** {@inheritDoc} */
	public String getServletContextName() {
		String name = servletContext.getServletContextName();
		// try to fetch information from META-INF/MANIFEST.MF
		InputStream is = servletContext.getResourceAsStream("/META-INF/MANIFEST.MF");//$NON-NLS-1$
		if (is != null) {
			try {
				Properties manifest = new Properties();
				manifest.load(is);
				String appName    = manifest.getProperty("Implementation-Title");//$NON-NLS-1$
				String appVersion = manifest.getProperty("Implementation-Version");//$NON-NLS-1$
				if (name == null) {
					name = appName;
				}
				if (appVersion != null) {
					name = (name==null?"":name) + '/' + appVersion;
				}
			} catch (IOException ioe) {
				// ignore
			} finally {
				try {
					is.close();
				} catch (IOException ioe) {
					// ignore
				}
			}
		}
		return (name == null) ? "" : name;//$NON-NLS-1$
	}


	/** {@inheritDoc} */
	public String getInitParameter(String name) {
		return servletContext.getInitParameter(name);
	}

	/** {@inheritDoc} */
	public Map getInitParameters() {
		Map result = new HashMap();
		Enumeration enumeration = servletContext.getInitParameterNames();
		while (enumeration.hasMoreElements()) {
			String name = (String) enumeration.nextElement();
			String value = servletContext.getInitParameter(name);
			result.put(name, value);
		}
		return result;
	}


	/** {@inheritDoc} */
	public Map getAttributes() {
		Map result = new HashMap();
		Enumeration enumeration = servletContext.getAttributeNames();
		while (enumeration.hasMoreElements()) {
			String name = (String) enumeration.nextElement();
			Object value = servletContext.getAttribute(name);
			result.put(name, value);
		}
		return result;
	}

	/** {@inheritDoc} */
	public Object getAttribute(String name) {
		return servletContext.getAttribute(name);
	}

	/** {@inheritDoc} */
	public void setAttribute(String name, Object object) {
		servletContext.setAttribute(name, object);
	}

	/** {@inheritDoc} */
	public void removeAttribute(String name) {
		servletContext.removeAttribute(name);
	}

}
