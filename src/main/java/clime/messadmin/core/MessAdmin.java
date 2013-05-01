/**
 * 
 */
package clime.messadmin.core;

import java.util.Set;

import clime.messadmin.model.Application;
import clime.messadmin.model.Server;
import clime.messadmin.model.Session;

/**
 * Manages actions on WebApp and Sessions. Used by admin servlet and JMX.
 * @author C&eacute;drik LIME
 */
public class MessAdmin {

	private MessAdmin() {
		super();
	}


	public static String getVersion() {
		String result = "";//$NON-NLS-1$
		Package p = MessAdmin.class.getPackage();
		if (p != null) {
			result = p.getImplementationVersion();
		}
		return result;
	}

	/**
	 * If message if blank or null, remove ServletContext attribute, otherwise inject message into ServletContext
	 * @param applicationIds
	 * @param message
	 * @return number of modified sessions
	 */
	public static int injectApplicationsPermanent(String[] applicationIds, String message) {
		if (null == applicationIds) {
			return 0;
		}
		int nbAffectedApplications = 0;
		for (int i = 0; i < applicationIds.length; ++i) {
			String applicationId = applicationIds[i];
			Application application = Server.getInstance().getApplication(applicationId);
			if (null == application) {
				// Shouldn't happen, but let's play nice...
				//log("WARNING: can't inject message for null application " + applicationId);
				continue;
			}
			boolean actionDone = application.injectPermanentMessage(message);
			if (actionDone) {
				++nbAffectedApplications;
			}
		}
		return nbAffectedApplications;
	}

	/**
	 * If message if blank or null, remove ServletContext attribute, otherwise inject message into ServletContext
	 * @param applicationIds
	 * @param message
	 * @return number of modified sessions
	 */
	public static int injectApplicationsOnce(String[] applicationIds, String message) {
		if (null == applicationIds) {
			return 0;
		}
		int nbAffectedApplications = 0;
		for (int i = 0; i < applicationIds.length; ++i) {
			String applicationId = applicationIds[i];
			int result = injectAllSessions(applicationId, message);
			if (result > 0) {
				++nbAffectedApplications;
			}
		}
		return nbAffectedApplications;
	}

	/**
	 * If message if blank or null, remove HttpSession attribute, otherwise inject message into HttpSessions
	 * @param message
	 * @return number of modified sessions
	 */
	public static int injectAllSessions(String context, String message) {
		Set/*<String>*/ activeSessionIds = Server.getInstance().getApplication(context).getActiveSessionsIds();
		String[] sessionIds = new String[activeSessionIds.size()];
		sessionIds = (String[]) activeSessionIds.toArray(sessionIds);
		return injectSessions(context, sessionIds, message);
	}

	/**
	 * If message if blank or null, remove HttpSession attribute, otherwise inject message into HttpSessions
	 * @param sessionIds
	 * @param message
	 * @return number of modified sessions
	 */
	public static int injectSessions(String context, String[] sessionIds, String message) {
		if (null == sessionIds) {
			return 0;
		}
		int nbAffectedSessions = 0;
		Application application = Server.getInstance().getApplication(context);
		for (int i = 0; i < sessionIds.length; ++i) {
			String sessionId = sessionIds[i];
			Session session = application.getSession(sessionId);
			if (null == session) {
				// Shouldn't happen, but let's play nice...
				//log("WARNING: can't inject message for null session " + sessionId);
				continue;
			}
			boolean actionDone = session.injectMessage(message);
			if (actionDone) {
				++nbAffectedSessions;
			}
		}
		return nbAffectedSessions;
	}

	/**
	 * Invalidate HttpSessions
	 * @param sessionIds
	 * @return number of invalidated sessions
	 */
	public static int invalidateSessions(String context, String[] sessionIds) {
		if (null == sessionIds) {
			return 0;
		}
		int nbAffectedSessions = 0;
		Application application = Server.getInstance().getApplication(context);
		for (int i = 0; i < sessionIds.length; ++i) {
			String sessionId = sessionIds[i];
			Session session = application.getSession(sessionId);
			if (null == session) {
				// Shouldn't happen, but let's play nice...
				//log("WARNING: can't invalidate null session " + sessionId);
				continue;
			}
			try {
				session.getSessionInfo().invalidate();
				++nbAffectedSessions;
				//log("Invalidating session id " + sessionId);
			} catch (IllegalStateException ise) {
				//log("Can't invalidate already invalidated session id " + sessionId);
			}
		}
		return nbAffectedSessions;
	}

	/**
	 * Removes an attribute from an HttpSession
	 * @param sessionId
	 * @param attributeName
	 * @return true if there was an attribute removed, false otherwise
	 */
	public static boolean removeSessionAttribute(String context, String sessionId, String attributeName) {
		Session session = Server.getInstance().getApplication(context).getSession(sessionId);
		if (null == session) {
			// Shouldn't happen, but let's play nice...
			//log("WARNING: can't remove attribute '" + attributeName + "' for null session " + sessionId);
			return false;
		}
		boolean wasPresent = false;
		try {
			wasPresent = (null != session.getSessionInfo().getAttribute(attributeName));
			session.getSessionInfo().removeAttribute(attributeName);
		} catch (IllegalStateException ise) {
			//log("Can't remote attribute '" + attributeName + "' for invalidated session id " + session.getId());
		}
		return wasPresent;
	}

	/**
	 * Sets the maximum inactive interval (session timeout) an HttpSession
	 * @param sessionId
	 * @param maxInactiveInterval in seconds
	 * @return old value for maxInactiveInterval
	 */
	public static int setSessionMaxInactiveInterval(String context, String sessionId, int maxInactiveInterval) {
		Session session = Server.getInstance().getApplication(context).getSession(sessionId);
		if (null == session) {
			// Shouldn't happen, but let's play nice...
			//log("WARNING: can't set timout for null session " + sessionId);
			return 0;
		}
		try {
			int oldMaxInactiveInterval = session.getSessionInfo().getMaxInactiveInterval();
			session.getSessionInfo().setMaxInactiveInterval(maxInactiveInterval);
			return oldMaxInactiveInterval;
		} catch (IllegalStateException ise) {
			// invalidated session
			return 0;
		}
	}
}
