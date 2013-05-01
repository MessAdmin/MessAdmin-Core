/**
 * 
 */
package clime.messadmin.providers.spi;

import javax.servlet.http.HttpSession;

/**
 * Note: the invocation order of Providers is reversed in case of session/application destruction.
 * @author C&eacute;drik LIME
 */
public interface SessionLifeCycleProvider extends BaseProvider {

	/**
	 * Notification that a session was created.
	 */
	void sessionCreated(HttpSession httpSession);

	/**
	 * Notification that a session is about to be invalidated.
	 */
	void sessionDestroyed(HttpSession httpSession);

	/**
	 * Notification that the session is about to be passivated.
	 */
    void sessionWillPassivate(HttpSession httpSession);

    /**
     * Notification that the session has just been activated.
     */
    void sessionDidActivate(HttpSession httpSession);
}
