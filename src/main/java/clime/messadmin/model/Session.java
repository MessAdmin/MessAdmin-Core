/**
 *
 */
package clime.messadmin.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionEvent;

import clime.messadmin.core.Constants;
import clime.messadmin.filter.MessAdminThreadLocal;
import clime.messadmin.utils.StringUtils;

/**
 * @author C&eacute;drik LIME
 */
public class Session implements HttpSessionActivationListener, IRequestListener {

	protected final SessionInfo sessionInfo;
	protected final Request cumulativeRequests = new Request(null);
	/**
	 * Map of user-defined data to store in the Session scope.
	 * This is mainly used for plugins (key == plugin FQ name, for example)
	 */
	protected Map userData;

	/**
	 * @param session
	 */
	public Session(HttpSession session) {
		super();
		sessionInfo = new SessionInfo(session);
	}
	/**
	 */
	public Session(HttpSession session, ClassLoader cl) {
		this(session);
		sessionInfo.setClassLoader(cl);
	}

	/**
	 * @return Returns the sessionInfo.
	 */
	public ISessionInfo getSessionInfo() {
		return sessionInfo;
	}

	/**
	 * @return Returns the userData.
	 */
	// implementation note: this should be synchronized, but
	// we don't really care if multiple copies are created.
	public Map getUserData() {
		if (userData == null) {
			userData = new ConcurrentHashMap();
		}
		return userData;
	}

	/** {@inheritDoc} */
	public void sessionDidActivate(final HttpSessionEvent se) {
		sessionInfo.setHttpSession(se.getSession());
	}
	/** {@inheritDoc} */
	public void sessionWillPassivate(final HttpSessionEvent se) {
		sessionInfo.setHttpSession(null);
	}


	/**
	 * If message if blank or null, remove HttpSession attribute, otherwise inject message into HttpSessions
	 * @param message
	 */
	public boolean injectMessage(String message) {
		boolean inject = StringUtils.isNotBlank(message);
		boolean actionDone = false;
		if (inject) {
			if (! message.equals(getSessionInfo().getAttribute(Constants.SESSION_MESSAGE_KEY))) {
				// Setting message
				getSessionInfo().setAttribute(Constants.SESSION_MESSAGE_KEY, message);
				actionDone = true;
			}
		} else {
			if (getSessionInfo().getAttribute(Constants.SESSION_MESSAGE_KEY) != null) {
				// Removing message
				getSessionInfo().removeAttribute(Constants.SESSION_MESSAGE_KEY);
				actionDone = true;
			}
		}
		return actionDone;
	}


	/*****************************************/
	/**	Request/Response Listener methods	**/
	/*****************************************/

	/** {@inheritDoc} */
	public void requestInitialized(final HttpServletRequest request, final ServletContext servletContext) {
		if (request == null || servletContext == null) {
			return;
		}
		final HttpSession httpSession = request.getSession(false);
		if (httpSession == null) {
			// should never happen!
			return;
		} // else
		try {
			sessionInfo.id = httpSession.getId(); // Correctly track changing Session ID
			sessionInfo.lastRequestInfo = MessAdminThreadLocal.getCurrentRequestInfo();
			cumulativeRequests.requestInitialized(sessionInfo.cumulativeRequestStats, request, servletContext);
		} catch (IllegalStateException ise) {
			// session is invalidated
		}
	}

	/** {@inheritDoc} */
	public void requestDestroyed(final HttpServletRequest request, final HttpServletResponse response, final ServletContext servletContext) {
		if (request == null || servletContext == null) { // allow null response
			return;
		}
		final HttpSession httpSession = request.getSession(false);
		if (httpSession == null) {
			// should never happen!
			return;
		} // else
		try {
			if (sessionInfo.lastRequestInfo == null) {
				sessionInfo.lastRequestInfo = MessAdminThreadLocal.getCurrentRequestInfo();
			}
			// request not already counted
			cumulativeRequests.requestDestroyed(sessionInfo.cumulativeRequestStats, request, response, servletContext);
		} catch (IllegalStateException ise) {
			// session is invalidated
		}
	}

	/** {@inheritDoc} */
	public void requestException(Exception e, HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
		if (request == null || response == null || servletContext == null) {
			return;
		}
		cumulativeRequests.requestException(sessionInfo.cumulativeRequestStats, e, request, response, servletContext);
	}
}
