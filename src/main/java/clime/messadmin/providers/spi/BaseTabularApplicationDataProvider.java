/**
 *
 */
package clime.messadmin.providers.spi;

import javax.servlet.ServletContext;

/**
 * Base implementation class for ApplicationDataProvider displaying tabular data.
 * @author C&eacute;drik LIME
 */
public abstract class BaseTabularApplicationDataProvider extends BaseTabularDataProvider implements ApplicationDataProvider {

	/**
	 *
	 */
	public BaseTabularApplicationDataProvider() {
		super();
	}

	/**
	 * @param context
	 * @return application-specific data labels for given Application, or null if it can be determined
	 */
	protected abstract String[] getApplicationTabularDataLabels(final ServletContext context);
	/**
	 * @param context
	 * @return application-specific data values for given Application, or null if it can be determined
	 */
	protected abstract RowIterator getApplicationRowIterator(final ServletContext context);

	protected abstract String getTableCaption(ServletContext context, String[] labels, RowIterator values);

	/**
	 * {@inheritDoc}
	 */
	public String getXHTMLApplicationData(ServletContext context) {
		try {
			String[] labels = getApplicationTabularDataLabels(context);
			RowIterator values = getApplicationRowIterator(context);
			return buildXHTML(labels, values, "extraApplicationAttributesTable-"+getClass().getName(), getTableCaption(context, labels, values));
		} catch (RuntimeException rte) {
			return "Error in " + this.getClass().getName() + ": " + rte;
		}
	}

}
