package clime.messadmin.admin.actions;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import clime.messadmin.admin.AdminActionProvider;
import clime.messadmin.admin.BaseAdminActionWithContext;
import clime.messadmin.model.IApplicationInfo;
import clime.messadmin.model.Server;
import clime.messadmin.providers.spi.DisplayFormatProvider;

/**
 * Displays the details page for a Web Application.
 * IMPLEMENTATION NOTE: always use include() instead of forward(), to get the real name of this servlet in jsp's
 * @author C&eacute;drik LIME
 * @since 4.2
 */
public class WebAppStats extends BaseAdminActionWithContext implements AdminActionProvider {
	public static final String ID = "webAppStats";//$NON-NLS-1$

	public WebAppStats() {
		super();
	}

	/** {@inheritDoc} */
	public String getActionID() {
		return ID;
	}

	/** {@inheritDoc} */
	@Override
	public void serviceWithContext(HttpServletRequest request, HttpServletResponse response, String context) throws ServletException, IOException {
		if (METHOD_POST.equals(request.getMethod())) {
			sendRedirect(request, response);
			return;
		}
		setNoCache(response);
		IApplicationInfo webAppStats = Server.getInstance().getApplication(context).getApplicationInfo();
		DisplayFormatProvider.Util.getInstance(request).displayWebAppStatsPage(request, response, webAppStats);
	}
}
