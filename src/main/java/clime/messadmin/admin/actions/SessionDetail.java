package clime.messadmin.admin.actions;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import clime.messadmin.admin.AdminActionProvider;
import clime.messadmin.admin.BaseAdminActionWithContextAndSession;
import clime.messadmin.model.Application;
import clime.messadmin.model.IApplicationInfo;
import clime.messadmin.model.ISessionInfo;
import clime.messadmin.model.Server;
import clime.messadmin.model.Session;
import clime.messadmin.providers.spi.DisplayFormatProvider;

/**
 * Displays the details page for a given HttpSession.
 * IMPLEMENTATION NOTE: always use include() instead of forward(), to get the real name of this servlet in jsp's
 * @author C&eacute;drik LIME
 * @since 4.2
 */
public class SessionDetail extends BaseAdminActionWithContextAndSession implements AdminActionProvider {
	public static final String ID = "sessionDetail";//$NON-NLS-1$

	public SessionDetail() {
		super();
	}

	/** {@inheritDoc} */
	public String getActionID() {
		return ID;
	}

	/** {@inheritDoc} */
	@Override
	public void serviceWithContextAndSession(HttpServletRequest request, HttpServletResponse response, String context, String sessionId) throws ServletException, IOException {
		if (METHOD_POST.equals(request.getMethod())) {
			sendRedirect(request, response);
			return;
		}
		Application application = Server.getInstance().getApplication(context);
		Session session = application.getSession(sessionId);
		//assert session != null;//check is done in BaseAdminActionWithContextAndSession#preService()
		setNoCache(response);
		IApplicationInfo applicationInfo = application.getApplicationInfo();
		ISessionInfo currentSession = session.getSessionInfo();
		DisplayFormatProvider.Util.getInstance(request).displaySessionDetailPage(request, response, applicationInfo, currentSession);
	}
}
