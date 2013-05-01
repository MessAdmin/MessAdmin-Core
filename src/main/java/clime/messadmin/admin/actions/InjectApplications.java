package clime.messadmin.admin.actions;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import clime.messadmin.admin.AdminActionProvider;
import clime.messadmin.admin.BaseAdminActionProvider;
import clime.messadmin.admin.MessAdminServlet;
import clime.messadmin.core.Constants;
import clime.messadmin.core.MessAdmin;
import clime.messadmin.i18n.I18NSupport;

/**
 * Inject a message at the Application level.
 * IMPLEMENTATION NOTE: always use include() instead of forward(), to get the real name of this servlet in jsp's
 * @author C&eacute;drik LIME
 * @since 4.2
 */
public class InjectApplications extends BaseAdminActionProvider implements AdminActionProvider {
	public static final String ID = "injectApplications";//$NON-NLS-1$

	public InjectApplications() {
		super();
	}

	/** {@inheritDoc} */
	public String getActionID() {
		return ID;
	}

	/** {@inheritDoc} */
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String permanent =  request.getParameter("permanent");//$NON-NLS-1$
		String[] applicationIds = request.getParameterValues("applicationIds");//$NON-NLS-1$
		String message = request.getParameter("message");//$NON-NLS-1$
		int i;
		if (permanent != null) {
			i = MessAdmin.injectApplicationsPermanent(applicationIds, message);
		} else {
			i = MessAdmin.injectApplicationsOnce(applicationIds, message);
		}
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		request.setAttribute(Constants.APPLICATION_MESSAGE, I18NSupport.getLocalizedMessage(MessAdminServlet.I18N_BUNDLE_NAME, cl, "injectApplications.ok", new Integer[] {Integer.valueOf(i)}));//$NON-NLS-1$
		((BaseAdminActionProvider)AdminActionProvider.Util.getInstance(request, WebAppsList.ID)).sendRedirect(request, response);
	}
}
