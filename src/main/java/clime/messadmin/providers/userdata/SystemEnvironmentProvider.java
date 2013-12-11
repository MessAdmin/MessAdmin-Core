/**
 * 
 */
package clime.messadmin.providers.userdata;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import clime.messadmin.i18n.I18NSupport;
import clime.messadmin.model.Server;
import clime.messadmin.providers.spi.BaseTabularServerDataProvider;
import clime.messadmin.providers.spi.ServerDataProvider;

/**
 * @author C&eacute;drik LIME
 */
public class SystemEnvironmentProvider extends BaseTabularServerDataProvider
		implements ServerDataProvider {
	private static final String BUNDLE_NAME = SystemEnvironmentProvider.class.getName();

	/**
	 * 
	 */
	public SystemEnvironmentProvider() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getServerTabularDataLabels() {
		String name = I18NSupport.getLocalizedMessage(BUNDLE_NAME, "label.name");//$NON-NLS-1$
		String value = I18NSupport.getLocalizedMessage(BUNDLE_NAME, "label.value");//$NON-NLS-1$
		return new String[] {name, value};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getTableCaption(String[] labels, Object[][] values) {
		NumberFormat numberFormatter = NumberFormat.getNumberInstance(I18NSupport.getAdminLocale());
		String caption = I18NSupport.getLocalizedMessage(BUNDLE_NAME, "table.caption", numberFormatter.format(values.length));//$NON-NLS-1$
		return caption;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[][] getServerTabularData() {
		Map<String, String> sysProps = Server.getInstance().getServerInfo().getSystemEnv();
		List resultList = new ArrayList(sysProps.size());
		for (Map.Entry<String, String> prop : sysProps.entrySet()) {
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
		return 20;
	}

}
