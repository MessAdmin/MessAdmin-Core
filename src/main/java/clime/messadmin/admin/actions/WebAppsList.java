package clime.messadmin.admin.actions;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import clime.messadmin.admin.AdminActionProvider;
import clime.messadmin.admin.BaseAdminActionProvider;
import clime.messadmin.model.Server;
import clime.messadmin.providers.spi.DisplayFormatProvider;

/**
 * Displays the Web Applications list.
 * IMPLEMENTATION NOTE: always use include() instead of forward(), to get the real name of this servlet in jsp's
 * @author C&eacute;drik LIME
 * @since 4.2
 */
public class WebAppsList extends BaseAdminActionProvider implements AdminActionProvider {
	public static final String ID = "webAppsList";//$NON-NLS-1$

	public WebAppsList() {
		super();
	}

	/** {@inheritDoc} */
	public String getActionID() {
		return ID;
	}

	/** {@inheritDoc} */
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (METHOD_POST.equals(request.getMethod())) {
			sendRedirect(request, response);
			return;
		}
		Set/*<ApplicationInfo>*/ applicationInfos = Server.getInstance().getApplicationInfos();
		setNoCache(response);
		DisplayFormatProvider.Util.getInstance(request).displayWebAppsList(request, response, applicationInfos);
	}
}
