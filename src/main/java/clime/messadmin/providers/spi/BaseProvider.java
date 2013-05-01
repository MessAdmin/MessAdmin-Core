/**
 * 
 */
package clime.messadmin.providers.spi;


/**
 * @author C&eacute;drik LIME
 */
public abstract interface BaseProvider {

	/**
	 * Sort order for this provider.
	 * Providers with smaller priority will be executed before,
	 * and providers with higher priority will be executed after.
	 * @return priority of this provider
	 */
	int getPriority();
}
