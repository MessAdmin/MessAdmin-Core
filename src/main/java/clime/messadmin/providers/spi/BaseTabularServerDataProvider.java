/**
 * 
 */
package clime.messadmin.providers.spi;

import java.text.NumberFormat;

import clime.messadmin.i18n.I18NSupport;


/**
 * Base implementation class for ServerDataProvider displaying tabular data.
 * @author C&eacute;drik LIME
 */
public abstract class BaseTabularServerDataProvider extends BaseTabularDataProvider implements ServerDataProvider {

	/**
	 * 
	 */
	public BaseTabularServerDataProvider() {
		super();
	}

	/**
	 * @return application-specific data labels for given HttpSession, or null if it can be determined
	 */
	public abstract String[] getServerTabularDataLabels();
	/**
	 * @return application-specific data values for given HttpSession, or null if it can be determined
	 */
	public abstract Object[][] getServerTabularData();

	protected String getTableCaption(String[] labels, Object[][] values) {
		if (values == null) {
			return "";//$NON-NLS-1$
		} else {
			NumberFormat numberFormatter = NumberFormat.getNumberInstance(I18NSupport.getAdminLocale());
			return ""+numberFormatter.format(values.length)+" server-specific attributes";
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getXHTMLServerData() {
		try {
			String[] labels = getServerTabularDataLabels();
			Object[][] values = getServerTabularData();
			return buildXHTML(labels, values, "extraServerAttributesTable-"+getClass().getName(), getTableCaption(labels, values));
		} catch (RuntimeException rte) {
			return "Error in " + this.getClass().getName() + ": " + rte;
		}
	}

}
