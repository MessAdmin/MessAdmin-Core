/**
 * 
 */
package clime.messadmin.jmx.mbeans;

import java.util.Set;

import clime.messadmin.model.IApplicationInfo;

/**
 * @author C&eacute;drik LIME
 */
public interface WebAppMBean extends IApplicationInfo {//implements NotificationEmitter

	/**
	 * @return <code>Collection&lt;String&gt;</code> of all active Sessions Ids.
	 */
	Set<String> getActiveSessionsIds();

	/**
	 * @return <code>Set&lt;String&gt;</code> of all the passivated SessionsIds.
	 */
	Set<String> getPassiveSessionsIds();

	// ApplicationInfo data are inherited

	// WebApp-related actions

	/**
	 * Set an application-level HTML message (to everyone)
	 * @param message html message to send
	 */
	void setApplicationOnceMessage(String message);
	void setApplicationPermanentMessage(String message);
	//boolean isApplicationMessageSet();//FIXME

	/**
	 * Send an HTML message to all active sessions.
	 * Future new sessions won't be affected by this.
	 * @param message html message to send
	 */
	void sendAllSessionsMessage(String message);

	//TODO: remove Application attribute(s), etc. (and mirror ServletContext operations)
}
