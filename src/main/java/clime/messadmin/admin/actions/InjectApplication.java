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
import clime.messadmin.i18n.I18NSupport;
import clime.messadmin.model.Application;
import clime.messadmin.model.Server;

/**
 * Inject a message at the Application level.
 * IMPLEMENTATION NOTE: always use include() instead of forward(), to get the real name of this servlet in jsp's
 * @author C&eacute;drik LIME
 * @since 4.2
 */
public class InjectApplication extends BaseAdminActionWithContext implements AdminActionProvider {
	public static final String ID = "injectApplication";//$NON-NLS-1$

	public InjectApplication() {
		super();
	}

	/** {@inheritDoc} */
	public String getActionID() {
		return ID;
	}

	/** {@inheritDoc} */
	public void serviceWithContext(HttpServletRequest request, HttpServletResponse response, String context) throws ServletException, IOException {
		String message = request.getParameter("message");//$NON-NLS-1$
		Application application = Server.getInstance().getApplication(context);
		application.injectPermanentMessage(message);
		final ClassLoader cl = application.getApplicationInfo().getClassLoader();
		request.setAttribute(Constants.APPLICATION_MESSAGE,
				I18NSupport.getLocalizedMessage(MessAdminServlet.I18N_BUNDLE_NAME, cl, "injectApplication.ok"));//$NON-NLS-1$
		((BaseAdminActionProvider)AdminActionProvider.Util.getInstance(request, SessionsList.ID)).sendRedirect(request, response);
	}
}
