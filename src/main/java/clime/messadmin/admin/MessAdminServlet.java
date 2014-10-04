package clime.messadmin.admin;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import clime.messadmin.core.Constants;
import clime.messadmin.i18n.I18NSupport;
import clime.messadmin.providers.ProviderUtils;
import clime.messadmin.providers.spi.DisplayFormatProvider;
import clime.messadmin.taglib.fmt.BundleTag;
import clime.messadmin.taglib.jstl.fmt.LocalizationContext;

/**
 * Servlet implementation class for Servlet: MessAdminServlet
 * IMPLEMENTATION NOTE: always use include() instead of forward(), to get the real name of this servlet in jsp's
 * @author C&eacute;drik LIME
 */
 public class MessAdminServlet extends HttpServlet implements Servlet {

	public static final String I18N_BUNDLE_NAME = "clime.messadmin.admin.i18n.core";//$NON-NLS-1$

	protected String authorizationPassword = null;

	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public MessAdminServlet() {
		super();
	}

	/** {@inheritDoc} */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			process(request, response);
		} finally {
			I18NSupport.setAdminLocale(null);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			process(request, response);
		} finally {
			I18NSupport.setAdminLocale(null);
		}
	}

	/**
	 * Dispatcher method which processes the request
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		AdminActionProvider displayActionProvider;
		try {
			displayActionProvider = AdminActionProvider.Util.getInstance(request);
		} catch (IllegalArgumentException iae) {
			response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, iae.getLocalizedMessage());
			return;
		}
		//assert displayActionProvider != null;

		// Authenticate, except for public actions
		if ( ! (displayActionProvider instanceof PublicAdminActionProvider)
				&&
				! HTTPAuthorizationProvider.checkAccess(authorizationPassword, request, response)) {
			return;
		}

		if (request.getParameter(Constants.APPLICATION_MESSAGE) != null) {
			request.setAttribute(Constants.APPLICATION_MESSAGE, request.getParameter(Constants.APPLICATION_MESSAGE));
		}
		if (request.getParameter(Constants.APPLICATION_ERROR) != null) {
			request.setAttribute(Constants.APPLICATION_ERROR, request.getParameter(Constants.APPLICATION_ERROR));
		}

//		resp.setContentType(displayProvider.getContentType());
		//resp.setCharacterEncoding("UTF-8");//$NON-NLS-1$
		// set response locale from client's browser settings.
		LocalizationContext l10nContext = BundleTag.findMatch(request, I18N_BUNDLE_NAME);
		if (l10nContext != null && l10nContext.getLocale() != null) {
			response.setLocale(l10nContext.getLocale());
		} else {
			response.setLocale(I18NSupport.DEFAULT_ADMIN_LOCALE);
		}
		I18NSupport.setAdminLocale(response.getLocale()); // necessary for plugins

		response.setHeader("X-FRAME-OPTIONS", "SAMEORIGIN");// IE8+: prevent clickjacking attacks on the Admin Console
		response.setHeader("X-UA-Compatible", "IE=edge");// IE

		if (displayActionProvider.preService(request, response)) {
			displayActionProvider.service(request, response);
		}
	}


	/** {@inheritDoc} */
	@Override
	public String getServletInfo() {
		return "MessAdminServlet, copyright (c) 2005--2015 Cédrik LIME";
	}

	/** {@inheritDoc} */
	@Override
	public void init() throws ServletException {
		super.init();
		String initAuthorizationPassword = getServletConfig().getInitParameter("AuthorizationPassword");//$NON-NLS-1$
		if (initAuthorizationPassword != null) {
			authorizationPassword = initAuthorizationPassword;
		}
		// Initialize all known (from this webapp's point of view) providers
		for (DisplayFormatProvider provider : ProviderUtils.getProviders(DisplayFormatProvider.class)) {
			provider.init(getServletConfig());
		}
		for (AdminActionProvider provider : ProviderUtils.getProviders(AdminActionProvider.class)) {
			provider.init(getServletConfig());
		}
	}

	/** {@inheritDoc} */
	@Override
	public void destroy() {
		for (AdminActionProvider provider : ProviderUtils.getProviders(AdminActionProvider.class)) {
			provider.destroy();
		}
		super.destroy();
	}
 }
