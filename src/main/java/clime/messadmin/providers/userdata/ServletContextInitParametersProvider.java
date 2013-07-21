/**
 *
 */
package clime.messadmin.providers.userdata;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletContext;

import clime.messadmin.i18n.I18NSupport;
import clime.messadmin.providers.spi.ApplicationDataProvider;
import clime.messadmin.providers.spi.BaseTabularApplicationDataProvider;
import clime.messadmin.utils.StringUtils;

/**
 * @author C&eacute;drik LIME
 */
public class ServletContextInitParametersProvider extends
		BaseTabularApplicationDataProvider implements ApplicationDataProvider {
	private static final String BUNDLE_NAME = ServletContextInitParametersProvider.class.getName();

	/**
	 *
	 */
	public ServletContextInitParametersProvider() {
		super();
	}

	/** {@inheritDoc} */
	@Override
	protected String getTableCaption(ServletContext context, String[] labels, RowIterator values) {
		if (values == null || values.getNRows() == 0) {
			return "";//$NON-NLS-1$
		} else {
			NumberFormat numberFormatter = NumberFormat.getNumberInstance(I18NSupport.getAdminLocale());
			String caption = I18NSupport.getLocalizedMessage(BUNDLE_NAME, "table.caption", new Object[] {numberFormatter.format(values.getNRows())});//$NON-NLS-1$
			return caption;
		}
	}

	/** {@inheritDoc} */
	@Override
	public String[] getApplicationTabularDataLabels(ServletContext context) {
		String name = I18NSupport.getLocalizedMessage(BUNDLE_NAME, "label.name");//$NON-NLS-1$
		String value = I18NSupport.getLocalizedMessage(BUNDLE_NAME, "label.value");//$NON-NLS-1$
		return new String[] {name, value};
	}

	/** {@inheritDoc} */
	@Override
	public RowIterator getApplicationRowIterator(ServletContext context) {
		RowIterator result = new ServletContextInitParametersIterator(context);
		if (result.getNRows() == 0) {
			return null;
		} else {
			return result;
		}
	}

	/** {@inheritDoc} */
	public String getApplicationDataTitle(ServletContext context) {
		return I18NSupport.getLocalizedMessage(BUNDLE_NAME, "title");//$NON-NLS-1$
	}

	/** {@inheritDoc} */
	public int getPriority() {
		return 5;
	}

	private class ServletContextInitParametersIterator extends BaseRowIterator implements RowIterator {
		private final ServletContext context;
		private final Enumeration<String> attributeNames;
		private String currentKey = null;
		private Object currentValue = null;

		public ServletContextInitParametersIterator(ServletContext context) {
			this.context = context;
			this.attributeNames = context.getInitParameterNames();
		}

		/** {@inheritDoc} */
		@Override
		public int getNRows() {
			List<String> list = Collections.list(context.getInitParameterNames());
			return list.size();
		}

		/** {@inheritDoc} */
		public boolean hasNext() {
			return attributeNames.hasMoreElements();
		}

		/** {@inheritDoc} */
		@Override
		public String getCellStyle(int cellNumber, Object value) {
			if (cellNumber == 0) {
				return "text-align: center;";
			} else {
				return super.getCellStyle(cellNumber, value);
			}
		}

		/** {@inheritDoc} */
		public Object[] next() {
			currentKey = attributeNames.nextElement();
			currentValue = context.getInitParameter(currentKey);
			Object[] result = new Object[2];
			result[0] = StringUtils.escapeXml(currentKey);
			if (currentValue != null) {
				result[1] = StringUtils.escapeXml(currentValue.toString());
			}
			return result;
		}
	}
}
