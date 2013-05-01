/**
 *
 */
package clime.messadmin.admin.actions;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import clime.messadmin.admin.AdminActionProvider;
import clime.messadmin.admin.BaseAdminActionProvider;
import clime.messadmin.providers.spi.ServerDataProvider;

/**
 * @author C&eacute;drik LIME
 * @since 5.2
 */
public class ReloadServerDataProvider extends BaseAdminActionProvider
		implements AdminActionProvider {
	public static final String ID = "reloadServerData";//$NON-NLS-1$

	public ReloadServerDataProvider() {
		super();
	}

	/** {@inheritDoc} */
	public String getActionID() {
		return ID;
	}

	/** {@inheritDoc} */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String scope = ReloadDataProviderHelper.getScope(request, response);
		ServerDataProvider provider = (ServerDataProvider) ReloadDataProviderHelper.getDisplayProvider(ServerDataProvider.class, request, response);
		if (scope == null || provider == null) {
			return;
		}
		setNoCache(response);
		PrintWriter out = response.getWriter();
		if (ReloadDataProviderHelper.SCOPE_TITLE.equals(scope)) {
			out.print(provider.getServerDataTitle());
		} else {
			out.print(provider.getXHTMLServerData());
		}
		out.close();
	}
}
