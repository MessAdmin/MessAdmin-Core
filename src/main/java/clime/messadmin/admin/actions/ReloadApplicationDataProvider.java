/**
 *
 */
package clime.messadmin.admin.actions;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import clime.messadmin.admin.AdminActionProvider;
import clime.messadmin.admin.BaseAdminActionWithContext;
import clime.messadmin.providers.spi.ApplicationDataProvider;
import clime.messadmin.utils.StringUtils;

/**
 * @author C&eacute;drik LIME
 * @since 5.2
 */
public class ReloadApplicationDataProvider extends BaseAdminActionWithContext
		implements AdminActionProvider {
	public static final String ID = "reloadApplicationData";//$NON-NLS-1$

	public ReloadApplicationDataProvider() {
		super();
	}

	/** {@inheritDoc} */
	public String getActionID() {
		return ID;
	}

	/** {@inheritDoc} */
	public void serviceWithContext(HttpServletRequest request, HttpServletResponse response, String context) throws ServletException, IOException {
		String scope = ReloadDataProviderHelper.getScope(request, response);
		ApplicationDataProvider provider = (ApplicationDataProvider) ReloadDataProviderHelper.getDisplayProvider(ApplicationDataProvider.class, request, response);
		if (scope == null || provider == null) {
			return;
		}
		ServletContext servletContext = super.getServletContext(context);
		if (servletContext == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, StringUtils.escapeXml(context));
			return;
		}
		setNoCache(response);
		PrintWriter out = response.getWriter();
		if (ReloadDataProviderHelper.SCOPE_TITLE.equals(scope)) {
			out.print(provider.getApplicationDataTitle(servletContext));
		} else {
			out.print(provider.getXHTMLApplicationData(servletContext));
		}
		out.close();
	}
}
