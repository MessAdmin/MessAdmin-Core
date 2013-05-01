package clime.messadmin.admin.actions;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import clime.messadmin.admin.AdminActionProvider;
import clime.messadmin.admin.BaseAdminActionProvider;
import clime.messadmin.admin.BaseAdminActionWithContext;
import clime.messadmin.admin.MessAdminServlet;
import clime.messadmin.core.Constants;
import clime.messadmin.core.MessAdmin;
import clime.messadmin.i18n.I18NSupport;

/**
 * Inject a message at the Session level.
 * IMPLEMENTATION NOTE: always use include() instead of forward(), to get the real name of this servlet in jsp's
 * @author C&eacute;drik LIME
 * @since 4.2
 */
public class InjectSessions extends BaseAdminActionWithContext implements AdminActionProvider {
	public static final String ID = "injectSessions";//$NON-NLS-1$

	public InjectSessions() {
		super();
	}

	/** {@inheritDoc} */
	public String getActionID() {
		return ID;
	}

	/** {@inheritDoc} */
	@Override
	public void serviceWithContext(HttpServletRequest request, HttpServletResponse response, String context) throws ServletException, IOException {
		String[] sessionIds = request.getParameterValues("sessionIds");//$NON-NLS-1$
		String message = request.getParameter("message");//$NON-NLS-1$
		int i = MessAdmin.injectSessions(context, sessionIds, message);
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		request.setAttribute(Constants.APPLICATION_MESSAGE,
				I18NSupport.getLocalizedMessage(MessAdminServlet.I18N_BUNDLE_NAME, cl, "injectSessions.ok", new Integer[] {Integer.valueOf(i)}));//$NON-NLS-1$
		((BaseAdminActionProvider)AdminActionProvider.Util.getInstance(request, SessionsList.ID)).sendRedirect(request, response);
	}
}
