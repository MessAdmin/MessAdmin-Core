/**
 *
 */
package clime.messadmin.providers.userdata;

import java.text.NumberFormat;
import java.util.Iterator;
import java.util.SortedSet;

import javax.servlet.ServletContext;

import clime.messadmin.i18n.I18NSupport;
import clime.messadmin.model.HttpServletRequestInfo;
import clime.messadmin.model.Server;
import clime.messadmin.providers.spi.ApplicationDataProvider;
import clime.messadmin.providers.spi.BaseTabularApplicationDataProvider;
import clime.messadmin.utils.DateUtils;
import clime.messadmin.utils.FastDateFormat;
import clime.messadmin.utils.StringUtils;

/**
 * @author C&eacute;drik LIME
 */
public class CurrentRequestsProvider extends
		BaseTabularApplicationDataProvider implements ApplicationDataProvider {
	private static final String BUNDLE_NAME = CurrentRequestsProvider.class.getName();

	/**
	 *
	 */
	public CurrentRequestsProvider() {
		super();
	}

	/** {@inheritDoc} */
	@Override
	protected String getTableCaption(ServletContext context, String[] labels, RowIterator values) {
		if (values == null || values.getNRows() == 0) {
			return "";//$NON-NLS-1$
		} else {
			ClassLoader cl = I18NSupport.getClassLoader(context);
			NumberFormat numberFormatter = NumberFormat.getNumberInstance(I18NSupport.getAdminLocale());
			String caption = I18NSupport.getLocalizedMessage(BUNDLE_NAME, cl, "table.caption", numberFormatter.format(values.getNRows()));//$NON-NLS-1$
			return caption;
		}
	}

	/** {@inheritDoc} */
	@Override
	public String[] getApplicationTabularDataLabels(ServletContext context) {
		ClassLoader cl = I18NSupport.getClassLoader(context);
		return new String[] {
				I18NSupport.getLocalizedMessage(BUNDLE_NAME, cl, "label.time"),//$NON-NLS-1$
				I18NSupport.getLocalizedMessage(BUNDLE_NAME, cl, "label.session"),//$NON-NLS-1$
				I18NSupport.getLocalizedMessage(BUNDLE_NAME, cl, "label.host"),//$NON-NLS-1$
				I18NSupport.getLocalizedMessage(BUNDLE_NAME, cl, "label.url")//$NON-NLS-1$
		};
	}

	/** {@inheritDoc} */
	@Override
	public RowIterator getApplicationRowIterator(ServletContext context) {
		SortedSet<HttpServletRequestInfo> currentRequests = Server.getInstance().getApplication(context).getCurrentRequests();
		RowIterator result = new CurrentRequestsIterator(currentRequests);
		if (result.getNRows() == 0) {
			return null;
		} else {
			return result;
		}
	}

	/** {@inheritDoc} */
	public String getApplicationDataTitle(ServletContext context) {
		ClassLoader cl = I18NSupport.getClassLoader(context);
		return I18NSupport.getLocalizedMessage(BUNDLE_NAME, cl, "title");//$NON-NLS-1$
	}

	/** {@inheritDoc} */
	public int getPriority() {
		return 600;
	}

	private class CurrentRequestsIterator extends BaseRowIterator implements RowIterator {
		private final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance(DateUtils.DEFAULT_DATE_TIME_FORMAT);
		private final SortedSet<HttpServletRequestInfo> currentRequests;
		private final Iterator<HttpServletRequestInfo> requestsIterator;
		private HttpServletRequestInfo currentRequest;

		public CurrentRequestsIterator(SortedSet<HttpServletRequestInfo> currentRequests) {
			this.currentRequests = currentRequests;
			this.requestsIterator = currentRequests.iterator();
		}

		/** {@inheritDoc} */
		@Override
		public int getNRows() {
			return currentRequests.size();
		}

		/** {@inheritDoc} */
		public boolean hasNext() {
			return requestsIterator.hasNext();
		}

		/** {@inheritDoc} */
		@Override
		public String getCellStyle(int cellNumber, Object value) {
			switch (cellNumber) {
			case 1:
				return "text-align: center;";
			default:
				return super.getCellStyle(cellNumber, value);
			}
		}

		/** {@inheritDoc} */
		@Override
		public String getCellTitle(int cellNumber, Object value) {
			switch (cellNumber) {
			case 0:
				return DATE_FORMAT.format(currentRequest.getRequestDate());
			case 2:
				return StringUtils.escapeXml(currentRequest.getRemoteUserName());
			case 3:
				return StringUtils.escapeXml(currentRequest.getUserAgent());
			default:
				return super.getCellTitle(cellNumber, value);
			}
		}

		/** {@inheritDoc} */
		public Object[] next() {
			currentRequest = requestsIterator.next();
			Object[] result = new Object[4];
			result[0] = (DateUtils.timeIntervalToFormattedString(currentRequest.getCurrentlyUsedTime()));
			if (currentRequest.hasHttpSession()) {
				result[1] = "<input type=\"checkbox\" checked=\"checked\" disabled=\"disabled\" readonly=\"readonly\"/>";//$NON-NLS-1$
			}
			result[2] = StringUtils.escapeXml(currentRequest.getRemoteAddr());
			result[3] = StringUtils.escapeXml(currentRequest.getURL());
			return result;
		}
	}
}
