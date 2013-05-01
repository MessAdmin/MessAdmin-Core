/**
 *
 */
package clime.messadmin.model;

import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionEvent;

import clime.messadmin.core.Constants;
import clime.messadmin.utils.SessionUtils;
import clime.messadmin.utils.StringUtils;

/**
 * @author C&eacute;drik LIME
 */
public class Session implements HttpSessionActivationListener, IRequestListener {
	private static final String X_FORWARDED_FOR = "X-Forwarded-For";//$NON-NLS-1$

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

	private String getXForwardedFor(final HttpServletRequest request, final String remoteHostToAdd) {
		String remoteHost;
		String xForwardedFor = null;
		Enumeration xffEnum = request.getHeaders(X_FORWARDED_FOR);
		// Concatenate all X-Forwarded-For HTTP headers
		if (xffEnum != null && xffEnum.hasMoreElements()) {
			while (xffEnum.hasMoreElements()) {
				String xForwardedForHeader = (String) xffEnum.nextElement();
				if (StringUtils.isNotBlank(xForwardedForHeader)) {
					if (xForwardedFor == null) {// 1rst time in loop
						xForwardedFor = xForwardedForHeader;
					} else {
						xForwardedFor = xForwardedFor + ", " + xForwardedForHeader;//$NON-NLS-1$
					}
				}
			}
		}
		if (StringUtils.isNotBlank(xForwardedFor)) {
			remoteHost = X_FORWARDED_FOR + ": " + xForwardedFor + ", " + remoteHostToAdd;//$NON-NLS-1$//$NON-NLS-2$
		} else {
			remoteHost = remoteHostToAdd;
		}
		return remoteHost;
	}

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
			sessionInfo.id          = httpSession.getId(); // Correctly track changing Session ID
			sessionInfo.authType    = request.getAuthType();
			sessionInfo.remoteAddr  = getXForwardedFor(request, request.getRemoteAddr());
			sessionInfo.remoteHost  = getXForwardedFor(request, request.getRemoteHost());
			sessionInfo.lastRequestURL = SessionUtils.getRequestURLWithMethodAndQueryString(request);
			sessionInfo.userPrincipal = request.getUserPrincipal();
			sessionInfo.remoteUser  = request.getRemoteUser();
			sessionInfo.isSecure    = request.isSecure();
			sessionInfo.userAgent   = request.getHeader("user-agent");//$NON-NLS-1$
			sessionInfo.referer     = request.getHeader("referer");//$NON-NLS-1$
			sessionInfo.sslCipherSuite   = (String)request.getAttribute(Constants.SSL_CIPHER_SUITE);//$NON-NLS-1$
			sessionInfo.sslAlgorithmSize = (Integer)request.getAttribute(Constants.SSL_KEY_SIZE);//$NON-NLS-1$
//			sessionInfo.sslCertificates = (X509Certificate[])request.getAttribute(Constants.SSL_CERTIFICATE);//$NON-NLS-1$
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
