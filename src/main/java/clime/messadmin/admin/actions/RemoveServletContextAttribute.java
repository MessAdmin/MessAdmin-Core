package clime.messadmin.admin.actions;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import clime.messadmin.admin.AdminActionProvider;
import clime.messadmin.admin.BaseAdminActionWithContext;
import clime.messadmin.admin.MessAdminServlet;
import clime.messadmin.core.Constants;
import clime.messadmin.i18n.I18NSupport;
import clime.messadmin.model.IApplicationInfo;
import clime.messadmin.model.Server;
import clime.messadmin.providers.spi.ApplicationDataProvider;
import clime.messadmin.providers.spi.BaseTabularApplicationDataProvider;
import clime.messadmin.providers.spi.DisplayProvider;
import clime.messadmin.providers.spi.SizeOfProvider;
import clime.messadmin.utils.BytesFormat;
import clime.messadmin.utils.StringUtils;

/**
 * Remove an attribute from the Application context.
 * IMPLEMENTATION NOTE: always use include() instead of forward(), to get the real name of this servlet in jsp's
 * @author C&eacute;drik LIME
 * @since 4.2
 */
public class RemoveServletContextAttribute extends BaseAdminActionWithContext implements AdminActionProvider, ApplicationDataProvider {
	protected static final String BUNDLE_NAME = RemoveServletContextAttribute.class.getName();
	public static final String ID = "removeServletContextAttribute";//$NON-NLS-1$
	private static final String PARAM_ATTRIBUTE_NAME = "attributeName";//$NON-NLS-1$

	private final ServletContextAttributesTable helper;

	public RemoveServletContextAttribute() {
		super();
		helper = new ServletContextAttributesTable(this, this);
}

	/** {@inheritDoc} */
	public String getActionID() {
		return ID;
	}

	@Override
	public int getPriority() {
		return helper.getPriority();
	}

	/** {@inheritDoc} */
	public String getApplicationDataTitle(ServletContext context) {
		return helper.getApplicationDataTitle(context);
	}

	/** {@inheritDoc} */
	public String getXHTMLApplicationData(ServletContext context) {
		return helper.getXHTMLApplicationData(context);
	}

	/** {@inheritDoc} */
	@Override
	public void serviceWithContext(HttpServletRequest request, HttpServletResponse response, String context) throws ServletException, IOException {
		String name = request.getParameter(PARAM_ATTRIBUTE_NAME);
		IApplicationInfo applicationInfo = Server.getInstance().getApplication(context).getApplicationInfo();
		boolean removed = (null != applicationInfo.getAttribute(name));
		applicationInfo.removeAttribute(name);
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if (removed) {
			request.setAttribute(Constants.APPLICATION_MESSAGE,
					I18NSupport.getLocalizedMessage(MessAdminServlet.I18N_BUNDLE_NAME, cl, "removeServletContextAttribute.ok", new Object[] {name}));//$NON-NLS-1$
		} else {
			request.setAttribute(Constants.APPLICATION_ERROR,
					I18NSupport.getLocalizedMessage(MessAdminServlet.I18N_BUNDLE_NAME, cl, "removeServletContextAttribute.ko", new Object[] {context, name}));//$NON-NLS-1$
		}
		ReloadDataProviderHelper.sendRedirect(request, response, ReloadApplicationDataProvider.ID, DisplayProvider.Util.getId(this), ReloadDataProviderHelper.SCOPE_CONTENT);
	}


	private static class ServletContextAttributesTable extends BaseTabularApplicationDataProvider {
		protected final AdminActionProvider adminActionProviderCallback;
		protected final DisplayProvider displayProviderCallback;

		public ServletContextAttributesTable(AdminActionProvider adminActionProviderCallback, DisplayProvider displayProviderCallback) {
			super();
			this.adminActionProviderCallback = adminActionProviderCallback;
			this.displayProviderCallback = displayProviderCallback;
		}

		/** {@inheritDoc} */
		public String getApplicationDataTitle(ServletContext context) {
			return I18NSupport.getLocalizedMessage(BUNDLE_NAME, "title");//$NON-NLS-1$
		}

		/** {@inheritDoc} */
		public int getPriority() {
			return 0;
		}

		/** {@inheritDoc} */
		@Override
		protected String getTableCaption(ServletContext context, String[] labels, RowIterator values) {
			if (values == null || values.getNRows() < 0) {
				return "";//$NON-NLS-1$
			} else {
				NumberFormat numberFormatter = NumberFormat.getNumberInstance(I18NSupport.getAdminLocale());
				String caption = I18NSupport.getLocalizedMessage(BUNDLE_NAME, "caption", new Object[] {numberFormatter.format(values.getNRows())});//$NON-NLS-1$
				return caption;
			}
		}

		/** {@inheritDoc} */
		@Override
		public String[] getApplicationTabularDataLabels(ServletContext context) {
			return new String[] {
					I18NSupport.getLocalizedMessage(BUNDLE_NAME, "label.remove"),//$NON-NLS-1$
					I18NSupport.getLocalizedMessage(BUNDLE_NAME, "label.size"),//$NON-NLS-1$
					I18NSupport.getLocalizedMessage(BUNDLE_NAME, "label.name"),//$NON-NLS-1$
					I18NSupport.getLocalizedMessage(BUNDLE_NAME, "label.value")//$NON-NLS-1$
			};
		}

		/** {@inheritDoc} */
		@Override
		public RowIterator getApplicationRowIterator(ServletContext context) {
			return new ServletContextAttributesIterator(context);
		}


		private class ServletContextAttributesIterator extends BaseRowIterator implements RowIterator {
			private final String internalContext;
			private final ClassLoader cl;
			private final ServletContext context;
			private final Enumeration<String> attributeNames;
			private String currentKey = null;
			private Object currentValue = null;
			private final BytesFormat bytesFormat = BytesFormat.getBytesInstance(I18NSupport.getAdminLocale(), true);

			public ServletContextAttributesIterator(ServletContext context) {
				this.cl = getClassLoader(context);
				this.internalContext = getInternalContext(context);
				this.context = context;
				this.attributeNames = context.getAttributeNames();
			}

			/** {@inheritDoc} */
			@Override
			public int getNRows() {
				List<String> list = Collections.list(context.getAttributeNames());
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
				currentValue = context.getAttribute(currentKey);
				Object[] result = new Object[4];
				{
					String submitUrl = "?" + ACTION_PARAMETER_NAME + '=' + ID + '&' +
							CONTEXT_KEY + '=' + StringUtils.escapeXml(internalContext) + '&' +
							PARAM_ATTRIBUTE_NAME + '=' + StringUtils.escapeXml(currentKey);
					StringBuilder form = new StringBuilder(64);
					form.append("<form action=\"").append(submitUrl).append("\" method=\"post\"><div>");
//					form.append("<input type=\"hidden\" name=\"").append(ACTION_PARAMETER_NAME).append("\" value=\""+ID+"\" />");
//					form.append("<input type=\"hidden\" name=\"").append(CONTEXT_KEY).append("\" value=\"").append(internalContext).append("\" />");
//					form.append("<input type=\"hidden\" name=\"").append(SESSION_KEY).append("\" value=\"").append(StringUtils.escapeXml(sessionId)).append("\" />");
//					form.append("<input type=\"hidden\" name=\"").append(PARAM_ATTRIBUTE_NAME).append("\" value=\"").append(StringUtils.escapeXml(currentKey)).append("\" />");
					form.append(buildSubmitButton(submitUrl, I18NSupport.getLocalizedMessage(BUNDLE_NAME, "remove"), displayProviderCallback));
					form.append("</div></form>");
					result[0] = form.toString();
				}
				result[1] = bytesFormat.format(SizeOfProvider.Util.getObjectSize(currentValue, cl));
				result[2] = StringUtils.escapeXml(currentKey);
				if (currentValue != null) {
					result[3] = "<span title=\"" + StringUtils.escapeXml(currentValue.getClass().toString()) + "\">" + StringUtils.escapeXml(currentValue.toString()) + "</span>";
				}
				return result;
			}
		}
	}
}
