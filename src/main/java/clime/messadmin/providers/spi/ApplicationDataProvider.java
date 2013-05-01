/**
 * 
 */
package clime.messadmin.providers.spi;

import javax.servlet.ServletContext;

/**
 * Use this plug-in to display application-specific data.
 * See {@link clime.messadmin.providers.spi.BaseTabularApplicationDataProvider} for displaying tabular data.
 * @author C&eacute;drik LIME
 */
public interface ApplicationDataProvider extends DisplayProvider {
	/**
	 * @param context
	 * @return application-specific XHTML data for given Application, or null if it can be determined
	 */
	String getXHTMLApplicationData(ServletContext context);
	/**
	 * @param context
	 * @return application-specific data title for given Application, or null if it can be determined
	 */
	String getApplicationDataTitle(ServletContext context);
}
