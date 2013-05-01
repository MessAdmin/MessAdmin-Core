/**
 *
 */
package clime.messadmin.providers.spi;

import javax.servlet.http.HttpSession;

/**
 * Base implementation class for SessionDataProvider displaying tabular data.
 * @author C&eacute;drik LIME
 */
public abstract class BaseTabularSessionDataProvider extends BaseTabularDataProvider implements SessionDataProvider {

	/**
	 *
	 */
	public BaseTabularSessionDataProvider() {
		super();
	}

	/**
	 * @param session
	 * @return application-specific data labels for given HttpSession, or null if it can be determined
	 */
	public abstract String[] getSessionTabularDataLabels(final HttpSession session);
	/**
	 * @param session
	 * @return application-specific data values for given HttpSession, or null if it can be determined
	 */
	public abstract RowIterator getSessionRowIterator(final HttpSession session);

	public abstract String getTableCaption(HttpSession session, String[] labels, RowIterator values);

	/**
	 * {@inheritDoc}
	 */
	public String getXHTMLSessionData(HttpSession session) {
		try {
			String[] labels = getSessionTabularDataLabels(session);
			RowIterator values = getSessionRowIterator(session);
			return buildXHTML(labels, values, "extraSessionAttributesTable-"+getClass().getName(), getTableCaption(session, labels, values));
		} catch (RuntimeException rte) {
			return "Error in " + this.getClass().getName() + ": " + rte;
		}
	}
}
