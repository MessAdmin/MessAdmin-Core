package clime.messadmin.admin;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import clime.messadmin.admin.actions.WebAppsList;
import clime.messadmin.core.Constants;
import clime.messadmin.i18n.I18NSupport;
import clime.messadmin.model.ApplicationInfo;
import clime.messadmin.model.Server;

/**
 * IMPLEMENTATION NOTE: always use include() instead of forward(), to get the real name of this servlet in jsp's
 * @author C&eacute;drik LIME
 * @since 4.2
 */
public abstract class BaseAdminActionWithContext extends BaseAdminActionProvider implements AdminActionProvider {

	public static final String CONTEXT_KEY = "context";//$NON-NLS-1$

	public BaseAdminActionWithContext() {
		super();
	}

	public static String getContext(HttpServletRequest request) {
		// Get the WebApp context we want to work with
		String context = request.getParameter(CONTEXT_KEY);
		if (context == null && Server.getInstance().getAllKnownInternalContexts().size() == 1) {
			context = (String) Server.getInstance().getAllKnownInternalContexts().toArray()[0];//req.getContextPath();
		}
		// If no or invalid context, display a list of available WebApps
		if (context == null || Server.getInstance().getApplication(context) == null) {
			return null;
		} else {
			return context;
		}
	}

	/** {@inheritDoc} */
	@Override
	public StringBuffer getURL(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		StringBuffer url = super.getURL(request, response);
		url.append('&').append(CONTEXT_KEY).append('=').append(clime.messadmin.taglib.core.Util.URLEncode(request.getParameter(CONTEXT_KEY), response.getCharacterEncoding()));
		return url;
	}

	/** {@inheritDoc} */
	@Override
	public boolean preService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if ( ! super.preService(request, response)) {
			return false;
		}
		// Get the WebApp context we want to work with
		String context = getContext(request);
		if (context == null) {
			String error = I18NSupport.getLocalizedMessage(MessAdminServlet.I18N_BUNDLE_NAME, "webAppDetail.null.error", request.getParameter(CONTEXT_KEY));//$NON-NLS-1$
			log(error);
			request.setAttribute(Constants.APPLICATION_ERROR, error);
			((BaseAdminActionProvider)AdminActionProvider.Util.getInstance(request, WebAppsList.ID)).sendRedirect(request, response);
			return false;
		}
		// Some attributes needed by some or all JSPs
		request.setAttribute(CONTEXT_KEY, context);
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public final void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		serviceWithContext(request, response, getContext(request));
	}

	abstract public void serviceWithContext(HttpServletRequest request, HttpServletResponse response, String context) throws ServletException, IOException;

	/**
	 * Convenience method to get the ServletContext associated with an internal context
	 */
	protected ServletContext getServletContext(String context) {
		return ((ApplicationInfo)Server.getInstance().getApplication(context).getApplicationInfo()).getServletContext();
	}
}
