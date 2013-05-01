/**
 * 
 */
package clime.messadmin.providers.spi;


/**
 * Use this plug-in to display server-specific data.
 * See {@link clime.messadmin.providers.spi.BaseTabularServerDataProvider} for displaying tabular data.
 * @author C&eacute;drik LIME
 */
public interface ServerDataProvider extends DisplayProvider {

	/**
	 * @return server-specific XHTML data, or null if it can be determined
	 */
	String getXHTMLServerData();

	/**
	 * @return server-specific data title for given HttpSession, or null if it can be determined
	 */
	String getServerDataTitle();
}
