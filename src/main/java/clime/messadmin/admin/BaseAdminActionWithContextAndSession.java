package clime.messadmin.admin;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import clime.messadmin.admin.actions.SessionsList;
import clime.messadmin.core.Constants;
import clime.messadmin.i18n.I18NSupport;
import clime.messadmin.model.Server;

/**
 * IMPLEMENTATION NOTE: always use include() instead of forward(), to get the real name of this servlet in jsp's
 * @author C&eacute;drik LIME
 * @since 4.2
 */
public abstract class BaseAdminActionWithContextAndSession extends BaseAdminActionWithContext implements AdminActionProvider {

	public static final String SESSION_KEY = "sessionId";//$NON-NLS-1$

	public BaseAdminActionWithContextAndSession() {
		super();
	}

	public static String getSession(HttpServletRequest request) {
		// Get the WebApp context we want to work with
		String context = getContext(request);
		// Get the Session ID we want to work with
		String session = request.getParameter(SESSION_KEY);
		// If no or invalid session, return null
		if (session == null || Server.getInstance().getApplication(context) == null || Server.getInstance().getApplication(context).getSession(session) == null) {
			return null;
		} else {
			return session;
		}
	}

	/** {@inheritDoc} */
	@Override
	public StringBuffer getURL(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		StringBuffer url = super.getURL(request, response);
		url.append('&').append(SESSION_KEY).append('=').append(clime.messadmin.taglib.core.Util.URLEncode(request.getParameter(SESSION_KEY), response.getCharacterEncoding()));
		return url;
	}

	/** {@inheritDoc} */
	@Override
	public boolean preService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if ( ! super.preService(request, response)) {
			return false;
		}
		// Get the Session ID we want to work with
		String session = getSession(request);
		if (session == null) {
			String error = I18NSupport.getLocalizedMessage(MessAdminServlet.I18N_BUNDLE_NAME, "sessionDetail.null.error", new Object[] {request.getParameter(SESSION_KEY)});//$NON-NLS-1$
			log(error);
			request.setAttribute(Constants.APPLICATION_ERROR, error);
			((BaseAdminActionProvider)AdminActionProvider.Util.getInstance(request, SessionsList.ID)).sendRedirect(request, response);
			return false;
		}
		// Some attributes needed by some or all JSPs
		request.setAttribute(SESSION_KEY, session);
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public final void serviceWithContext(HttpServletRequest request, HttpServletResponse response, String context) throws ServletException, IOException {
		serviceWithContextAndSession(request, response, getContext(request), getSession(request));
	}

	abstract public void serviceWithContextAndSession(HttpServletRequest req, HttpServletResponse resp, String context, String sessionId) throws ServletException, IOException;
}
