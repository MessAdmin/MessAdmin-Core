package clime.messadmin.admin.actions;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import clime.messadmin.admin.AdminActionProvider;
import clime.messadmin.admin.BaseAdminActionProvider;
import clime.messadmin.admin.BaseAdminActionWithContextAndSession;
import clime.messadmin.admin.MessAdminServlet;
import clime.messadmin.core.Constants;
import clime.messadmin.core.MessAdmin;
import clime.messadmin.i18n.I18NSupport;

/**
 * Set Session Max Inactive Interval.
 * IMPLEMENTATION NOTE: always use include() instead of forward(), to get the real name of this servlet in jsp's
 * @author C&eacute;drik LIME
 * @since 4.2
 */
public class SetSessionMaxInactiveInterval extends BaseAdminActionWithContextAndSession implements AdminActionProvider {
	public static final String ID = "setSessionMaxInactiveInterval";//$NON-NLS-1$

	public SetSessionMaxInactiveInterval() {
		super();
	}

	/** {@inheritDoc} */
	public String getActionID() {
		return ID;
	}

	/** {@inheritDoc} */
	@Override
	public void serviceWithContextAndSession(HttpServletRequest request, HttpServletResponse response, String context, String sessionId) throws ServletException, IOException {
		String timeoutStr = request.getParameter("timeout");//$NON-NLS-1$
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		try {
			int timeout = Integer.parseInt(timeoutStr.trim()) * 60;
			int oldTimeout = MessAdmin.setSessionMaxInactiveInterval(context, sessionId, timeout);
			if (oldTimeout != 0) {
				request.setAttribute(Constants.APPLICATION_MESSAGE,
						I18NSupport.getLocalizedMessage(MessAdminServlet.I18N_BUNDLE_NAME, cl, "setSessionMaxInactiveInterval.ok", new Object[] {Integer.valueOf(oldTimeout/60), timeoutStr}));//$NON-NLS-1$
			} else {
				request.setAttribute(Constants.APPLICATION_ERROR,
						I18NSupport.getLocalizedMessage(MessAdminServlet.I18N_BUNDLE_NAME, cl, "setSessionMaxInactiveInterval.ko", new Object[] {sessionId}));//$NON-NLS-1$
			}
		} catch (NumberFormatException nfe) {
			request.setAttribute(Constants.APPLICATION_ERROR,
					I18NSupport.getLocalizedMessage(MessAdminServlet.I18N_BUNDLE_NAME, cl, "setSessionMaxInactiveInterval.error"));//$NON-NLS-1$
		} catch (NullPointerException npe) {
			request.setAttribute(Constants.APPLICATION_ERROR,
					I18NSupport.getLocalizedMessage(MessAdminServlet.I18N_BUNDLE_NAME, cl, "setSessionMaxInactiveInterval.error"));//$NON-NLS-1$
		}
		((BaseAdminActionProvider)AdminActionProvider.Util.getInstance(request, SessionDetail.ID)).sendRedirect(request, response);
	}
}
