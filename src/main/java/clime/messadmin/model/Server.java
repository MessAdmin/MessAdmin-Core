/**
 *
 */
package clime.messadmin.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import clime.messadmin.providers.ProviderUtils;
import clime.messadmin.providers.spi.ServerLifeCycleProvider;
import clime.messadmin.utils.SessionUtils;

/**
 * @author C&eacute;drik LIME
 */
public class Server implements ServletContextListener, IRequestListener {
	private static final Server INSTANCE = new Server();
	protected final ServerInfo serverInfo = new ServerInfo();
	protected final Map<String, Application> webApps = new ConcurrentHashMap<String, Application>(); // must be synchronized
//	private final Map<String, Application> webAppsByContextPath = new ConcurrentHashMap(); // must be synchronized // cache

	/**
	 *
	 */
	private Server() {
		super();
	}

	public static Server getInstance() {
		return INSTANCE;
	}

	/**
	 * @return Returns the serverInfo.
	 */
	public IServerInfo getServerInfo() {
		return serverInfo;
	}

	/**
	 * @param internalContext
	 * @return Application associated with given internal context in this server
	 */
	public Application getApplication(String internalContext) {
		return webApps.get(internalContext);
	}
	/**
	 * @param servletContext
	 * @return Application associated with given servletContext in this server
	 */
	public Application getApplication(ServletContext servletContext) {
		return getApplication(getInternalContext(servletContext));
	}
//	/**
//	 * @param contextPath
//	 * @return Application associated with given contextPath in this server or null
//	 */
//	public Application getApplicationForContextPath(final String contextPath) {
//		if (null == contextPath) {
//			return null;
//		}
//		Application webApp = (Application) webAppsByContextPath.get(contextPath);
//		if (webApp != null) {
//			return webApp;
//		}
//		Iterator iter = new ArrayList(webApps.values()).iterator();
//		while (iter.hasNext()) {
//			webApp = (Application) iter.next();
//			if (contextPath.equals(webApp.getApplicationInfo().getContextPath())) {
//				// cache for next request
//				webAppsByContextPath.put(contextPath, webApp);
//				return webApp;
//			}
//		}
//		return null;
//	}
	/**
	 * @return all known Applications for this server
	 */
	public Set<Application> getApplications() {
		return new HashSet<Application>(webApps.values());
	}
	/**
	 * @return all known ApplicationInfos for this server
	 */
	public Set<IApplicationInfo> getApplicationInfos() {
		Set<IApplicationInfo> result = new HashSet<IApplicationInfo>(webApps.size());
		for (Application application : webApps.values()) {
			result.add(application.getApplicationInfo());
		}
		return result;
	}

	/**
	 * @return all known application internal contexts for this server
	 */
	public Set<String> getAllKnownInternalContexts() {
		return new HashSet<String>(webApps.keySet());
	}

	public String getInternalContext(final ServletContext servletContext) {
		String internalContext = SessionUtils.getContext(servletContext);
		return internalContext;
	}

	/**
	 * Convenience method to directly get a Session without going through Application
	 * @param httpSession
	 * @return Session
	 */
	public Session getSession(HttpSession httpSession) {
		if (httpSession == null) {
			return null;
		}
		try {
			Application application = getApplication(httpSession.getServletContext());
			if (application != null) {
				return application.getSession(httpSession);
			} else {
				return null;
			}
		} catch (IllegalStateException ise) {
			return null;
		}
	}

	/*****************************************/
	/**	Request/Response Listener methods	**/
	/*****************************************/

//	protected void registerContextPath(final HttpServletRequest request, final Application webApp) {
//		final String contextPath = request.getContextPath();
//		if (webAppsByContextPath.get(contextPath) == null) {
//			//webApp == getApplication(request.getSession(false).getServletContext());
//			webAppsByContextPath.put(contextPath, webApp);
//			webApp.registerContextPath(contextPath);
//		}
//	}

	/** {@inheritDoc} */
	public void requestInitialized(final HttpServletRequest request, final ServletContext servletContext) {
		if (request == null || servletContext == null) {
			return;
		}
//		final HttpSession httpSession = request.getSession(false);
//		if (httpSession == null) {
//			final Application application = getApplicationForContextPath(request.getContextPath());
//			if (application != null) {
//				application.requestInitialized(request, servletContext);
//			}
//			return;
//		} // else
		try {
			final Application application = getApplication(servletContext);
			application.requestInitialized(request, servletContext);
		} catch (IllegalStateException ise) {
			// session is invalidated
		}
	}

	/** {@inheritDoc} */
	public void requestDestroyed(final HttpServletRequest request, final HttpServletResponse response, final ServletContext servletContext) {
		if (request == null || servletContext == null) { // allow null response
			return;
		}
//		final HttpSession httpSession = request.getSession(false);
//		if (httpSession == null) {
//			final Application application = getApplicationForContextPath(request.getContextPath());
//			if (application != null) {
//				application.requestDestroyed(request, response, servletContext);
//			}
//			return;
//		} // else
		try {
			final Application application = getApplication(servletContext);
//			registerContextPath(request, application); // in case HttpSession has been created after request
			application.requestDestroyed(request, response, servletContext);
		} catch (IllegalStateException ise) {
			// session is invalidated
		}
	}

	/** {@inheritDoc} */
	public void requestException(Exception e, HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
		if (request == null || response == null || servletContext == null) {
			return;
		}
		try {
			final Application application = getApplication(servletContext);
//			registerContextPath(request, application); // in case HttpSession has been created after request
			application.requestException(e, request, response, servletContext);
		} catch (IllegalStateException ise) {
			// session is invalidated
		}
	}

	/*************************************/
	/**	ServletContextListener methods	**/
	/*************************************/

	/**
	 * {@inheritDoc}
	 */
	public void contextInitialized(final ServletContextEvent sce) {
		String internalContext = getInternalContext(sce.getServletContext());
		if (! webApps.containsKey(internalContext)) {
			Application application = new Application(sce.getServletContext());
			webApps.put(internalContext, application);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void contextDestroyed(final ServletContextEvent sce) {
		String internalContext = getInternalContext(sce.getServletContext());
		/*Application application = (Application)*/ webApps.remove(internalContext);
//		if (application.getApplicationInfo().getContextPath() != null) {
//			webAppsByContextPath.remove(application.getApplicationInfo().getContextPath());
//		}
	}

	/*****************************/
	/**	ServerListener methods	**/
	/*****************************/

	private boolean isInitialized = false;

	/**
	 * @see ServerLifeCycleProvider#serverInitialized()
	 */
	public synchronized void serverInitialized() {
		if (isInitialized || !webApps.isEmpty()) {
			// don't initialize twice, or while running
			return;
		}
		isInitialized = true;
		// Put Server-specific actions here
		Iterator iter = ProviderUtils.getProviders(ServerLifeCycleProvider.class).iterator();
		while (iter.hasNext()) {
			ServerLifeCycleProvider lc = (ServerLifeCycleProvider) iter.next();
			try {
				lc.serverInitialized();
			} catch (RuntimeException rte) {
			}
		}
	}

	/**
	 * @see ServerLifeCycleProvider#serverDestroyed()
	 */
	public synchronized void serverDestroyed() {
		if (! isInitialized || ! webApps.isEmpty()) {
			// don't destroy if not initialized, or if running
			return;
		}
		isInitialized = false;
		List providers = new ArrayList(ProviderUtils.getProviders(ServerLifeCycleProvider.class));
		Collections.reverse(providers);
		Iterator iter = providers.iterator();
		while (iter.hasNext()) {
			ServerLifeCycleProvider lc = (ServerLifeCycleProvider) iter.next();
			try {
				lc.serverDestroyed();
			} catch (RuntimeException rte) {
			}
		}
		// Put Server-specific actions here
		ProviderUtils.reload();
	}
}
