package clime.messadmin.admin.actions;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import clime.messadmin.admin.AdminActionProvider;
import clime.messadmin.admin.BaseAdminActionWithContextAndSession;
import clime.messadmin.admin.MessAdminServlet;
import clime.messadmin.core.Constants;
import clime.messadmin.core.MessAdmin;
import clime.messadmin.i18n.I18NSupport;
import clime.messadmin.model.Server;
import clime.messadmin.providers.spi.BaseTabularSessionDataProvider;
import clime.messadmin.providers.spi.DisplayProvider;
import clime.messadmin.providers.spi.SerializableProvider;
import clime.messadmin.providers.spi.SessionDataProvider;
import clime.messadmin.providers.spi.SizeOfProvider;
import clime.messadmin.utils.BytesFormat;
import clime.messadmin.utils.StringUtils;

/**
 * Remove a Session attribute.
 * IMPLEMENTATION NOTE: always use include() instead of forward(), to get the real name of this servlet in jsp's
 * @author C&eacute;drik LIME
 * @since 4.2
 */
// TODO: set (String/OGNL) attributes and Max Inactive Interval on sessions
public class RemoveSessionAttribute extends BaseAdminActionWithContextAndSession implements AdminActionProvider, SessionDataProvider {
	protected static final String BUNDLE_NAME = RemoveSessionAttribute.class.getName();
	public static final String ID = "removeSessionAttribute";//$NON-NLS-1$
	private static final String PARAM_ATTRIBUTE_NAME = "attributeName";//$NON-NLS-1$

	private final SessionAttributesTable helper;

	public RemoveSessionAttribute() {
		super();
		helper = new SessionAttributesTable(this, this);
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
	public String getSessionDataTitle(HttpSession session) {
		return helper.getSessionDataTitle(session);
	}

	/** {@inheritDoc} */
	public String getXHTMLSessionData(HttpSession session) {
		return helper.getXHTMLSessionData(session);
	}

	/** {@inheritDoc} */
	@Override
	public void serviceWithContextAndSession(HttpServletRequest request, HttpServletResponse response, String context, String sessionId) throws ServletException, IOException {
		String name = request.getParameter(PARAM_ATTRIBUTE_NAME);
		boolean removed = MessAdmin.removeSessionAttribute(context, sessionId, name);
		final ClassLoader cl = Server.getInstance().getApplication(context).getApplicationInfo().getClassLoader();
		if (removed) {
			request.setAttribute(Constants.APPLICATION_MESSAGE,
					I18NSupport.getLocalizedMessage(MessAdminServlet.I18N_BUNDLE_NAME, cl, "removeSessionAttribute.ok", name));//$NON-NLS-1$
		} else {
			request.setAttribute(Constants.APPLICATION_ERROR,
					I18NSupport.getLocalizedMessage(MessAdminServlet.I18N_BUNDLE_NAME, cl, "removeSessionAttribute.ko", name));//$NON-NLS-1$
		}
		ReloadDataProviderHelper.sendRedirect(request, response, ReloadSessionDataProvider.ID, DisplayProvider.Util.getId(this), ReloadDataProviderHelper.SCOPE_CONTENT);
	}


	private static class SessionAttributesTable extends BaseTabularSessionDataProvider {
		protected final AdminActionProvider adminActionProviderCallback;
		protected final DisplayProvider displayProviderCallback;

		public SessionAttributesTable(AdminActionProvider adminActionProviderCallback, DisplayProvider displayProviderCallback) {
			super();
			this.adminActionProviderCallback = adminActionProviderCallback;
			this.displayProviderCallback = displayProviderCallback;
		}

		/** {@inheritDoc} */
		public String getSessionDataTitle(HttpSession session) {
			return I18NSupport.getLocalizedMessage(BUNDLE_NAME, I18NSupport.getClassLoader(session), "title");//$NON-NLS-1$
		}

		/** {@inheritDoc} */
		public int getPriority() {
			return 0;
		}

		/** {@inheritDoc} */
		@Override
		public String getTableCaption(HttpSession session, String[] labels, RowIterator values) {
			if (values == null || values.getNRows() < 0) {
				return "";//$NON-NLS-1$
			} else {
				NumberFormat numberFormatter = NumberFormat.getNumberInstance(I18NSupport.getAdminLocale());
				String caption = I18NSupport.getLocalizedMessage(BUNDLE_NAME, I18NSupport.getClassLoader(session), "caption", numberFormatter.format(values.getNRows()));//$NON-NLS-1$
				return caption;
			}
		}

		/** {@inheritDoc} */
		@Override
		public String[] getSessionTabularDataLabels(HttpSession session) {
			final ClassLoader cl = I18NSupport.getClassLoader(session);
			return new String[] {
					I18NSupport.getLocalizedMessage(BUNDLE_NAME, cl, "label.remove"),//$NON-NLS-1$
					I18NSupport.getLocalizedMessage(BUNDLE_NAME, cl, "label.size"),//$NON-NLS-1$
					I18NSupport.getLocalizedMessage(BUNDLE_NAME, cl, "label.name"),//$NON-NLS-1$
					I18NSupport.getLocalizedMessage(BUNDLE_NAME, cl, "label.value")//$NON-NLS-1$
			};
		}

		/** {@inheritDoc} */
		@Override
		public RowIterator getSessionRowIterator(HttpSession session) {
			return new SessionAttributesIterator(session);
		}


		private class SessionAttributesIterator extends BaseRowIterator implements RowIterator {
			private final String internalContext;
			private final ClassLoader cl;
			private final HttpSession session;
			private final String sessionId;
			private final Enumeration<String> attributeNames;
			private String currentKey = null;
			private Object currentValue = null;
			private final BytesFormat bytesFormat = BytesFormat.getBytesInstance(I18NSupport.getAdminLocale(), true);

			public SessionAttributesIterator(HttpSession session) {
				this.cl = getClassLoader(session);
				this.internalContext = getInternalContext(session);
				this.session = session;
				this.sessionId = session.getId();
				this.attributeNames = session.getAttributeNames();
			}

			/** {@inheritDoc} */
			@Override
			public int getNRows() {
				List<String> list = Collections.list(session.getAttributeNames());
				return list.size();
			}

			/** {@inheritDoc} */
			public boolean hasNext() {
				return attributeNames.hasMoreElements();
			}

			/** {@inheritDoc} */
			@Override
			public String getRowStyle() {
				boolean isSerializable = SerializableProvider.Util.isSerializable(currentValue, cl);
				if (! isSerializable) {
					return "background-color: #EE0000;";
				} else {
					return null;
				}
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
				currentValue = session.getAttribute(currentKey);
				Object[] result = new Object[4];
				{
					String submitUrl = "?" + ACTION_PARAMETER_NAME + '=' + ID + '&' +
							CONTEXT_KEY + '=' + StringUtils.escapeXml(internalContext) + '&' +
							SESSION_KEY + '=' + StringUtils.escapeXml(sessionId) + '&' +
							PARAM_ATTRIBUTE_NAME + '=' + StringUtils.escapeXml(currentKey);
					StringBuilder form = new StringBuilder(64);
					form.append("<form action=\"").append(submitUrl).append("\" method=\"post\"><div>");
//					form.append("<input type=\"hidden\" name=\"").append(ACTION_PARAMETER_NAME).append("\" value=\""+ID+"\" />");
//					form.append("<input type=\"hidden\" name=\"").append(CONTEXT_KEY).append("\" value=\"").append(internalContext).append("\" />");
//					form.append("<input type=\"hidden\" name=\"").append(SESSION_KEY).append("\" value=\"").append(StringUtils.escapeXml(sessionId)).append("\" />");
//					form.append("<input type=\"hidden\" name=\"").append(PARAM_ATTRIBUTE_NAME).append("\" value=\"").append(StringUtils.escapeXml(currentKey)).append("\" />");
					form.append(buildSubmitButton(submitUrl, I18NSupport.getLocalizedMessage(BUNDLE_NAME, cl, "remove"), displayProviderCallback));
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
