/**
 *
 */
package clime.messadmin.jmx.mbeans;

import java.security.Principal;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import clime.messadmin.core.Constants;
import clime.messadmin.core.MessAdmin;
import clime.messadmin.model.ErrorData;
import clime.messadmin.model.ISessionInfo;
import clime.messadmin.model.Server;

/**
 * IMPLEMENTATION NOTE: don't forget to synchronize setters!
 * @author C&eacute;drik LIME
 */
public class Session implements SessionMBean { //extends NotificationBroadcasterSupport
	private String context;
	private String sessionId;

	/**
	 *
	 */
	public Session() {
		super();
	}

	public void setContext(String ctx) {
		context = ctx;
	}
	public void setSessionId(String id) {
		sessionId = id;
	}

	// Methods from HttpSession

	public long getCreationTime() {
		try {
			HttpSession session = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
			return session == null ? -1 : session.getCreationTime();
		} catch (IllegalStateException ise) {
			// session is invalidated
			return -1;
		}
	}

	public long getLastAccessedTime() {
		try {
			HttpSession session = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
			return session == null ? -1 : session.getLastAccessedTime();
		} catch (IllegalStateException ise) {
			// session is invalidated
			return -1;
		}
	}

	public int getMaxInactiveInterval() {
		try {
			HttpSession session = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
			return session == null ? -1 : session.getMaxInactiveInterval();
		} catch (IllegalStateException ise) {
			// session is invalidated
			return 0;
		}
	}

	public void invalidate() {
		HttpSession session = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		if (session != null) {
			try {
				session.invalidate();
			} catch (IllegalStateException ise) {
				// session is invalidated
			}
		}
	}

	public boolean isNew() {
		try {
			HttpSession session = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
			return session == null ? true : session.isNew();
		} catch (IllegalStateException ise) {
			// session is invalidated
			return false;
		}
	}

	public synchronized void removeAttribute(String in_name) {
		HttpSession session = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		if (session != null) {
			try {
				session.removeAttribute(in_name);
			} catch (IllegalStateException ise) {
				// session is invalidated
			}
		}
	}

	public synchronized void setMaxInactiveInterval(int in_interval) {
		HttpSession session = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		if (session != null) {
			try {
				session.setMaxInactiveInterval(in_interval);
			} catch (IllegalStateException ise) {
				// session is invalidated
			}
		}
	}

	public Object getAttribute(String name) {
		HttpSession session = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return (session == null) ? null : session.getAttribute(name);
	}

	public Enumeration<String> getAttributeNames() {
		HttpSession session = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return (session == null) ? null : session.getAttributeNames();
	}

	public synchronized void setAttribute(String name, Object value) {
		HttpSession session = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		if (session != null) {
			session.setAttribute(name, value);
		}
	}

	public String getId() {
		return sessionId;
	}

	public ServletContext getServletContext() {
		return null;
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public HttpSessionContext getSessionContext() {
		return null;
	}
	/**
	 * @deprecated
	 */
	@Deprecated
	public Object getValue(String name) {
		return null;
	}
	/**
	 * @deprecated
	 */
	@Deprecated
	public String[] getValueNames() {
		return null;
	}
	/**
	 * @deprecated
	 */
	@Deprecated
	public void putValue(String name, Object value) {
	}
	/**
	 * @deprecated
	 */
	@Deprecated
	public void removeValue(String name) {
	}


	// Methods from SessionInfo

	/** {@inheritDoc} */
	public int getNErrors() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? 0 : extraSessionInfo.getNErrors();
	}

	/** {@inheritDoc} */
	public ErrorData getLastError() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? null : extraSessionInfo.getLastError();
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, Object> getAttributes() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? Collections.EMPTY_MAP : extraSessionInfo.getAttributes();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLastRequestURL() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? null : extraSessionInfo.getLastRequestURL();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getRemoteAddr() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? null : extraSessionInfo.getRemoteAddr();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getRemoteHost() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? null : extraSessionInfo.getRemoteHost();
	}

	/**
	 * {@inheritDoc}
	 */
	public Principal getUserPrincipal() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? null : extraSessionInfo.getUserPrincipal();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getRemoteUser() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? null : extraSessionInfo.getRemoteUser();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getHits() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? -1 : extraSessionInfo.getHits();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getRequestLastLength() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? -1 : extraSessionInfo.getRequestLastLength();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getResponseLastLength() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? -1 : extraSessionInfo.getResponseLastLength();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getRequestMinLength() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? -1 : extraSessionInfo.getRequestMinLength();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getResponseMinLength() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? -1 : extraSessionInfo.getResponseMinLength();
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getRequestMinLengthDate() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? null : extraSessionInfo.getRequestMinLengthDate();
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getResponseMinLengthDate() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? null : extraSessionInfo.getResponseMinLengthDate();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getRequestMaxLength() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? -1 : extraSessionInfo.getRequestMaxLength();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getResponseMaxLength() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? -1 : extraSessionInfo.getResponseMaxLength();
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getRequestMaxLengthDate() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? null : extraSessionInfo.getRequestMaxLengthDate();
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getResponseMaxLengthDate() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? null : extraSessionInfo.getResponseMaxLengthDate();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getRequestTotalLength() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? -1 : extraSessionInfo.getRequestTotalLength();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getResponseTotalLength() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? -1 : extraSessionInfo.getResponseTotalLength();
	}

	/**
	 * {@inheritDoc}
	 */
	public double getRequestMeanLength() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? -1 : extraSessionInfo.getRequestMeanLength();
	}

	/**
	 * {@inheritDoc}
	 */
	public double getResponseMeanLength() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? -1 : extraSessionInfo.getResponseMeanLength();
	}

	/**
	 * {@inheritDoc}
	 */
	public double getRequestStdDevLength() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? -1 : extraSessionInfo.getRequestStdDevLength();
	}

	/**
	 * {@inheritDoc}
	 */
	public double getResponseStdDevLength() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? -1 : extraSessionInfo.getResponseStdDevLength();
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getLastRequestDate() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? null : extraSessionInfo.getLastRequestDate();
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getLastResponseDate() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? null : extraSessionInfo.getLastResponseDate();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSecure() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? false : extraSessionInfo.isSecure();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSslCipherSuite() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? null : extraSessionInfo.getSslCipherSuite();
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer getSslAlgorithmSize() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? null : extraSessionInfo.getSslAlgorithmSize();
	}

//	/**
//	 * {@inheritDoc}
//	 */
//	public X509Certificate[] getSslCertificates() {
//		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
//		return extraSessionInfo == null ? null : extraSessionInfo.getSslCertificates();
//	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSerializable() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? true : extraSessionInfo.isSerializable();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getSize() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? -1 : extraSessionInfo.getSize();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUserAgent() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? null : extraSessionInfo.getUserAgent();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getAuthType() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? null : extraSessionInfo.getAuthType();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getReferer() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? null : extraSessionInfo.getReferer();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getLastResponseStatus() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? -1 : extraSessionInfo.getLastResponseStatus();
	}

	// Some more methods

	/**
	 * {@inheritDoc}
	 */
	public int getIdleTime() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? -1 : extraSessionInfo.getIdleTime();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getTTL() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? -1 : extraSessionInfo.getTTL();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getAge() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? -1 : extraSessionInfo.getAge();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getGuessedUser() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? null : extraSessionInfo.getGuessedUser();
	}

	/**
	 * {@inheritDoc}
	 */
	public Locale getGuessedLocale() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? null : extraSessionInfo.getGuessedLocale();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getLastUsedTime() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? -1 : extraSessionInfo.getLastUsedTime();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getMinUsedTime() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? -1 : extraSessionInfo.getMinUsedTime();
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getMinUsedTimeDate() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? null : extraSessionInfo.getMinUsedTimeDate();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getMaxUsedTime() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? -1 : extraSessionInfo.getMaxUsedTime();
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getMaxUsedTimeDate() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? null : extraSessionInfo.getMaxUsedTimeDate();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getTotalUsedTime() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? -1 : extraSessionInfo.getTotalUsedTime();
	}

	/**
	 * {@inheritDoc}
	 */
	public double getMeanUsedTime() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? 0 : extraSessionInfo.getMeanUsedTime();
	}

	/**
	 * {@inheritDoc}
	 */
	public double getStdDevUsedTime() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? 0 : extraSessionInfo.getStdDevUsedTime();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Map.Entry<String, String>> getSessionSpecificData() {
		ISessionInfo extraSessionInfo = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		return extraSessionInfo == null ? null : extraSessionInfo.getSessionSpecificData();
	}

	// Session-related actions

	/**
	 * {@inheritDoc}
	 */
	public synchronized void sendMessage(String in_message) {
		MessAdmin.injectSessions(context, new String[] {sessionId}, in_message);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getHasPendingMessage() {
		HttpSession session = Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo();
		try {
			return session == null ? false : session.getAttribute(Constants.SESSION_MESSAGE_KEY) != null;
		} catch (IllegalStateException ise) {
			// session is invalidated
			return false;
		}
	}

}
