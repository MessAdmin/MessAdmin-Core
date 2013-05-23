/**
 * 
 */
package clime.messadmin.jmx.mbeans;

import clime.messadmin.model.ISessionInfo;

/**
 * @author C&eacute;drik LIME
 */
public interface SessionMBean extends ISessionInfo {//implements NotificationEmitter

	// Methods from HttpSession are inherited

	// Methods from ISessionInfo are inherited

	// Some more methods

	// Session-related actions

	/**
	 * Send an HTML message to this user
	 * @param message
	 */
	void sendMessage(String message);

	boolean getHasPendingMessage();
}
