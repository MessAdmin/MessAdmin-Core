/**
 * 
 */
package clime.messadmin.providers.spi;


/**
 * Note: the invocation order of Providers is reversed in case of session/application destruction.
 * @author C&eacute;drik LIME
 */
public interface ServerLifeCycleProvider extends BaseProvider {

	/**
	 * Notification that the server initialization
	 * process is starting.
	 */
    void serverInitialized();

	/**
	 * Notification that the server is about to be shut down.
	 */
    void serverDestroyed();
}
