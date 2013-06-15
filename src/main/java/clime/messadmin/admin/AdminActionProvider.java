package clime.messadmin.admin;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import clime.messadmin.admin.actions.SessionsList;
import clime.messadmin.admin.actions.WebAppsList;
import clime.messadmin.model.Server;
import clime.messadmin.providers.ProviderUtils;
import clime.messadmin.providers.spi.BaseProvider;

/**
 * Provider for displaying the administration data in various formats.
 * An AdminAction is an extended {@link javax.servlet.Servlet}.
 * Implementations will probably want to extend {@link javax.servlet.http.HttpServlet}.
 * @author C&eacute;drik LIME
 * @since 4.2
 */
public interface AdminActionProvider extends BaseProvider, Servlet {
	public static String ACTION_PARAMETER_NAME = "action";//$NON-NLS-1$

	public static class Util {
		public static AdminActionProvider getInstance(HttpServletRequest request) {
			String action = request.getParameter(ACTION_PARAMETER_NAME);
			if (action == null) {
				// Get the WebApp context we want to work with
				String context = BaseAdminActionWithContext.getContext(request);
				// If no or invalid context, display a list of available WebApps
				if (context == null) {
					action = WebAppsList.ID;
				} else {
					action = SessionsList.ID;
				}
			}
			return getInstance(request, action);
		}
		public static AdminActionProvider getInstance(HttpServletRequest request, String action) {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			// Get the WebApp context we want to work with
			String context = BaseAdminActionWithContext.getContext(request);
			if (context != null) {
				cl = Server.getInstance().getApplication(context).getApplicationInfo().getClassLoader();
			}

			Iterator<AdminActionProvider> iter = ProviderUtils.getProviders(AdminActionProvider.class, cl).iterator();
			while (iter.hasNext()) {
				AdminActionProvider provider = iter.next();
				if (action.equalsIgnoreCase(provider.getActionID())) {
					return provider;
				}
			}
			throw new IllegalArgumentException(action);
		}
	}

	/**
	 * e.g.
	 */
	String getActionID();

	/**
	 * Called before {@link #service(HttpServletRequest, HttpServletResponse)}.
	 * If returning {@code false}, this method should take care of returning a suitable HTTP response.
	 * @return {@code true} if processing can continue, {@code false} if processing should stop
	 */
	boolean preService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

	/**
	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
}
