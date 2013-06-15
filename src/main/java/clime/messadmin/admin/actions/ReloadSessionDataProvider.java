/**
 *
 */
package clime.messadmin.admin.actions;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import clime.messadmin.admin.AdminActionProvider;
import clime.messadmin.admin.BaseAdminActionWithContextAndSession;
import clime.messadmin.model.Server;
import clime.messadmin.model.SessionInfo;
import clime.messadmin.providers.spi.SessionDataProvider;
import clime.messadmin.utils.StringUtils;

/**
 * @author C&eacute;drik LIME
 * @since 5.2
 */
public class ReloadSessionDataProvider extends BaseAdminActionWithContextAndSession
		implements AdminActionProvider {
	public static final String ID = "reloadSessionData";//$NON-NLS-1$

	public ReloadSessionDataProvider() {
		super();
	}

	/** {@inheritDoc} */
	public String getActionID() {
		return ID;
	}

	/** {@inheritDoc} */
	@Override
	public void serviceWithContextAndSession(HttpServletRequest request, HttpServletResponse response, String context, String sessionId) throws ServletException,IOException {
		String scope = ReloadDataProviderHelper.getScope(request, response);
		SessionDataProvider provider = ReloadDataProviderHelper.getDisplayProvider(SessionDataProvider.class, request, response);
		if (scope == null || provider == null) {
			return;
		}
		HttpSession httpSession = ((SessionInfo)Server.getInstance().getApplication(context).getSession(sessionId).getSessionInfo()).getHttpSession();
		if (httpSession == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, StringUtils.escapeXml(sessionId));
			return;
		}
		setNoCache(response);
		PrintWriter out = response.getWriter();
		if (ReloadDataProviderHelper.SCOPE_TITLE.equals(scope)) {
			out.print(provider.getSessionDataTitle(httpSession));
		} else {
			out.print(provider.getXHTMLSessionData(httpSession));
		}
		out.close();
	}
}
