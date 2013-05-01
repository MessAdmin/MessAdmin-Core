/**
 * 
 */
package clime.messadmin.providers.userdata;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import clime.messadmin.i18n.I18NSupport;
import clime.messadmin.model.Server;
import clime.messadmin.providers.spi.BaseTabularServerDataProvider;
import clime.messadmin.providers.spi.ServerDataProvider;

/**
 * @author C&eacute;drik LIME
 */
public class SystemPropertiesProvider extends BaseTabularServerDataProvider
		implements ServerDataProvider {
	private static final String BUNDLE_NAME = SystemPropertiesProvider.class.getName();

	/**
	 * 
	 */
	public SystemPropertiesProvider() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getServerTabularDataLabels() {
		String key = I18NSupport.getLocalizedMessage(BUNDLE_NAME, "label.key");//$NON-NLS-1$
		String value = I18NSupport.getLocalizedMessage(BUNDLE_NAME, "label.value");//$NON-NLS-1$
		return new String[] {key, value};
	}

	/**
	 * {@inheritDoc}
	 */
	protected String getTableCaption(String[] labels, Object[][] values) {
		NumberFormat numberFormatter = NumberFormat.getNumberInstance(I18NSupport.getAdminLocale());
		String caption = I18NSupport.getLocalizedMessage(BUNDLE_NAME, "table.caption", new Object[] {numberFormatter.format(values.length)});//$NON-NLS-1$
		return caption;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object[][] getServerTabularData() {
		Map sysProps = Server.getInstance().getServerInfo().getSystemProperties();
		List resultList = new ArrayList(sysProps.size());
		Iterator iter = sysProps.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry prop = (Map.Entry) iter.next();
			resultList.add(new Object[] {
				prop.getKey(),
				prop.getValue()
			});
		}
		Object[][] result = new Object[resultList.size()][];
		return (Object[][]) resultList.toArray(result);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getServerDataTitle() {
		return I18NSupport.getLocalizedMessage(BUNDLE_NAME, "title");//$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public int getPriority() {
		return 10;
	}

}
