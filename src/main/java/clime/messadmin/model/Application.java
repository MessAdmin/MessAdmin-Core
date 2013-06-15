/**
 *
 */
package clime.messadmin.model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import clime.messadmin.core.Constants;
import clime.messadmin.utils.StringUtils;

/**
 * @author C&eacute;drik LIME
 */
public class Application implements HttpSessionListener, HttpSessionActivationListener, IRequestListener {
	protected final ApplicationInfo applicationInfo;
	protected final Request cumulativeRequests = new Request(null);
	protected final Map<String, Session> activeSessions = new ConcurrentHashMap<String, Session>(); // must be synchronized
	protected final Map<String, Session> passiveSessions = new ConcurrentHashMap<String, Session>(); // must be synchronized
	/**
	 * Since the HttpSession id can (and will) change, track the last-known
	 * HttpSession id here. This is used as a o(1) cache to avoid traversing activeSessions#values.
	 * The (simpler) alternative would be to store the information into the HttpSession itself,
	 * but this is contrary to our (current) policy.
	 * Note that this problem will disappear if one day we decide to store MessAdmin Session
	 * data inside the HttpSession! :-)
	 */
	protected final Map<HttpSession, String> activeSessionsIds = new ConcurrentHashMap<HttpSession, String>(); // must be synchronized
	/**
	 * Map of user-defined data to store in the Application scope.
	 * This is mainly used for plugins (key == plugin FQ name, for example)
	 */
	protected final Map userData = new ConcurrentHashMap();

	/**
	 * @param servletContext
	 */
	public Application(ServletContext servletContext) {
		super();
		applicationInfo = new ApplicationInfo(servletContext);
	}

	/**
	 * @return Returns the applicationInfo.
	 */
	public IApplicationInfo getApplicationInfo() {
		return applicationInfo;
	}

	/**
	 * @return Returns the userData.
	 */
	public Map getUserData() {
		return userData;
	}

	/**
	 * @param httpSession
	 * @return active Session associated with HttpSession
	 */
	public Session getSession(HttpSession httpSession) {
		String sessionId = httpSession.getId();
		Session session = activeSessions.get(sessionId);
		if (session == null && activeSessionsIds.containsKey(httpSession)) {
			String sessionOldId = activeSessionsIds.get(httpSession);
			assert sessionOldId != null;
			session = activeSessions.get(sessionOldId);
			updateSessionId(sessionOldId, sessionId);
		}
		return session;
	}
	/**
	 * @param sessionId
	 * @return active Session associated with sessionId
	 */
	public Session getSession(String sessionId) {
		Session session = activeSessions.get(sessionId);
		if (session == null && activeSessionsIds.containsValue(sessionId)) {
			//httpSession = activeSessionsIds.getKeyForValue(sessionId);
			for (Map.Entry<HttpSession, String> entry : activeSessionsIds.entrySet()) {
				HttpSession httpSession = entry.getKey();
				if (httpSession.getId().equals(sessionId)) {
					String sessionOldId = entry.getValue();
					assert sessionOldId != null;
					session = activeSessions.get(sessionOldId);
					updateSessionId(sessionOldId, sessionId);
					break;
				}
			}
		}
		return session;
	}

	private void updateSessionId(String sessionOldId, String sessionNewId) {
		Session session = activeSessions.get(sessionOldId);
		if (session != null) {
			HttpSession httpSession = ((SessionInfo)session.getSessionInfo()).getHttpSession();
			activeSessions.remove(sessionOldId);
			activeSessions.put(sessionNewId, session);
			activeSessionsIds.put(httpSession, sessionNewId);
			session.sessionInfo.setHttpSession(httpSession); // update session id
		}
	}

	/**
	 * @return all known active Sessions for this application
	 */
	public Set<Session> getActiveSessions() {
		return new HashSet<Session>(activeSessions.values());
	}
	/**
	 * @return all known active SessionsInfo for this application
	 */
	public Set<ISessionInfo> getActiveSessionInfos() {
		Set<ISessionInfo> result = new HashSet<ISessionInfo>(activeSessions.size());
		for (Session session : getActiveSessions()) {
			result.add(session.getSessionInfo());
		}
		return result;
	}
	/**
	 * @return all known active Sessions ids for this application
	 */
	public Set<String> getActiveSessionsIds() {
		return new HashSet<String>(activeSessions.keySet());
	}

	/**
	 * @return all known passive Sessions for this application
	 */
	public Set<Session> getPassiveSessions() {
		return new HashSet<Session>(passiveSessions.values());
	}
	/**
	 * @return all known passive Sessions ids for this application
	 */
	public Set<String> getPassiveSessionsIds() {
		return new HashSet<String>(passiveSessions.keySet());
	}


	protected void hit() {
		++applicationInfo.hits;
	}
	public void addUsedTime(int duration) {
		applicationInfo.cumulativeRequestStats.usedTime.registerValue(duration);
	}

	/**
	 * If message if blank or null, remove ServletContext attribute, otherwise inject message into ServletContext
	 * @param message
	 */
	public boolean injectPermanentMessage(String message) {
		boolean inject = StringUtils.isNotBlank(message);
		boolean actionDone = false;
		if (inject) {
			if (! message.equals(getApplicationInfo().getAttribute(Constants.GLOBAL_MESSAGE_KEY))) {
				// Setting message
				getApplicationInfo().setAttribute(Constants.GLOBAL_MESSAGE_KEY, message);
				actionDone = true;
			}
		} else {
			if (getApplicationInfo().getAttribute(Constants.GLOBAL_MESSAGE_KEY) != null) {
				// Removing message
				getApplicationInfo().removeAttribute(Constants.GLOBAL_MESSAGE_KEY);
				actionDone = true;
			}
		}
		// remove display timestamps from sessions
		for (ISessionInfo session : getActiveSessionInfos()) {
			session.removeAttribute(Constants.GLOBAL_MESSAGE_TIMESTAMP_KEY);
		}
		return actionDone;
	}


	/*****************************************/
	/**	Request/Response Listener methods	**/
	/*****************************************/

	public void registerContextPath(final String contextPath) {
		if (applicationInfo.contextPath == null) {
			applicationInfo.contextPath = contextPath;
		}
	}

	/** {@inheritDoc} */
	public void requestInitialized(final HttpServletRequest request, final ServletContext servletContext) {
		if (request == null || servletContext == null) {
			return;
		}
		if (applicationInfo.getClassLoader() == null) {
			applicationInfo.setClassLoader(Thread.currentThread().getContextClassLoader());
		}
		registerContextPath(request.getContextPath());
		cumulativeRequests.requestInitialized(applicationInfo.cumulativeRequestStats, request, servletContext);
		final HttpSession httpSession = request.getSession(false);
		if (httpSession == null) {
			return;
		} // else
		try {
			Session session = getSession(httpSession);
			if (session == null) {
				// Maybe this session comes from a serialized state. We need to register it.
				try {
					httpSession.getAttributeNames(); // this throws an IllegalStateException for an old session
					this.sessionCreated(new HttpSessionEvent(httpSession));
					session = getSession(httpSession);
				} catch (IllegalStateException ise) {
					// session is invalidated: it's not new, it's old! So, don't create anything...
				}
			}
			if (session != null) {
				// we need this test in case session comes from a serialized state @ startup
				session.requestInitialized(request, servletContext);
			}
		} catch (IllegalStateException ise) {
			// session is invalidated
		}
	}

	/** {@inheritDoc} */
	public void requestDestroyed(final HttpServletRequest request, final HttpServletResponse response, final ServletContext servletContext) {
		if (request == null || servletContext == null) { // allow null response
			return;
		}
		hit();
		cumulativeRequests.requestDestroyed(applicationInfo.cumulativeRequestStats, request, response, servletContext);
		final HttpSession httpSession = request.getSession(false);
		if (httpSession != null) {
			try {
				final Session session = getSession(httpSession);
				if (session != null) {
					session.requestDestroyed(request, response, servletContext);
				}
			} catch (IllegalStateException ise) {
				// session is invalidated
			}
		}
	}

	/** {@inheritDoc} */
	public void requestException(Exception e, HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
		if (request == null || response == null || servletContext == null) {
			return;
		}
		cumulativeRequests.requestException(applicationInfo.cumulativeRequestStats, e, request, response, servletContext);
		final HttpSession httpSession = request.getSession(false);
		if (httpSession != null) {
			try {
				final Session session = getSession(httpSession);
				if (session != null) {
					session.requestException(e, request, response, servletContext);
				}
			} catch (IllegalStateException ise) {
				// session is invalidated
			}
		}
	}

	/*********************************************/
	/**	HttpSession[Activation]Listener methods	**/
	/*********************************************/

	/**
	 * {@inheritDoc}
	 */
	public void sessionCreated(final HttpSessionEvent event) {
		HttpSession httpSession = event.getSession();
		String sessionId = httpSession.getId();
		if (activeSessionsIds.containsKey(httpSession)) {
			// weird FORM login behaviour...
			String sessionOldId = activeSessionsIds.get(httpSession);
			assert activeSessions.containsKey(sessionOldId);
			updateSessionId(sessionOldId, sessionId);
		} else {
			// real new session
			Session session = new Session(httpSession, applicationInfo.getClassLoader());
			activeSessions.put(sessionId, session);
			activeSessionsIds.put(httpSession, sessionId);
			++applicationInfo.totalCreatedSessions;
			applicationInfo.activeSessions.addValue(1);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void sessionDestroyed(final HttpSessionEvent event) {
		HttpSession httpSession = event.getSession();
		String sessionId = httpSession.getId();
		activeSessions.remove(sessionId);
		passiveSessions.remove(sessionId);
		String sessionOldId = activeSessionsIds.remove(httpSession);
		if (sessionOldId != null && ! sessionOldId.equals(sessionId)) {
			activeSessions.remove(sessionOldId);
			passiveSessions.remove(sessionOldId);
		}
		applicationInfo.activeSessions.addValue(-1);
	}

	/**
	 * {@inheritDoc}
	 */
	public void sessionWillPassivate(final HttpSessionEvent se) {
		HttpSession httpSession = se.getSession();
		String sessionId = httpSession.getId();
		// session is not active anymore: remove it from active list
		Session session = activeSessions.remove(sessionId);
		if (session == null && activeSessionsIds.containsKey(httpSession)) {
			String sessionOldId = activeSessionsIds.get(httpSession);
			assert sessionOldId != null;
			session = activeSessions.remove(sessionOldId);
		}
		activeSessionsIds.remove(httpSession);
		if (session != null) {
			applicationInfo.activeSessions.addValue(-1);
			session.sessionWillPassivate(se);
			passiveSessions.put(sessionId, session);
			++applicationInfo.passiveSessionsCount;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void sessionDidActivate(final HttpSessionEvent se) {
		HttpSession httpSession = se.getSession();
		String sessionId = httpSession.getId();
		Session session = passiveSessions.remove(sessionId);
		if (session != null) {
			--applicationInfo.passiveSessionsCount;
			// refresh list of active sessions
			activeSessions.put(sessionId, session);
			activeSessionsIds.put(httpSession, sessionId);
			session.sessionDidActivate(se);
			applicationInfo.activeSessions.addValue(1);
		} else {
			this.sessionCreated(new HttpSessionEvent(httpSession));
		}
	}
}
