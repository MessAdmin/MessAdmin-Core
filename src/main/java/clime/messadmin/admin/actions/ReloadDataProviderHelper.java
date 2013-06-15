/**
 *
 */
package clime.messadmin.admin.actions;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import clime.messadmin.admin.AdminActionProvider;
import clime.messadmin.admin.BaseAdminActionProvider;
import clime.messadmin.providers.ProviderUtils;
import clime.messadmin.providers.spi.DisplayProvider;
import clime.messadmin.utils.StringUtils;

/**
 * @author C&eacute;drik LIME
 * @since 5.2
 */
public class ReloadDataProviderHelper {
	public static final String PARAM_PROVIDER = "provider";//$NON-NLS-1$
	public static final String PARAM_SCOPE   = "scope";//$NON-NLS-1$
	public static final String SCOPE_TITLE   = "title";//$NON-NLS-1$
	public static final String SCOPE_CONTENT = "content";//$NON-NLS-1$

	public ReloadDataProviderHelper() {
		super();
	}

	public static String getScope(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String scope = request.getParameter(PARAM_SCOPE);
		if (StringUtils.isBlank(scope)) {
			scope = SCOPE_TITLE;
		}
		if (!SCOPE_TITLE.equals(scope) && !SCOPE_CONTENT.equals(scope)) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, StringUtils.escapeXml(scope));
			return null;
		}
		return scope;
	}

	public static <T extends DisplayProvider> T getDisplayProvider(Class<T> providerClass, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String providerId = request.getParameter(PARAM_PROVIDER);
		if (StringUtils.isBlank(providerId)) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, StringUtils.escapeXml(providerId));
			return null;
		}
		String scope = request.getParameter(PARAM_SCOPE);
		if (StringUtils.isBlank(scope)) {
			scope = SCOPE_TITLE;
		}
		if (!SCOPE_TITLE.equals(scope) && !SCOPE_CONTENT.equals(scope)) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, StringUtils.escapeXml(scope));
			return null;
		}
		List<T> providers = ProviderUtils.getProviders(providerClass);
		for (T provider : providers) {
			if (providerId.equals(DisplayProvider.Util.getId(provider))) {
				return provider;
			}
		}
		response.sendError(HttpServletResponse.SC_NOT_FOUND, StringUtils.escapeXml(providerId));
		return null;
	}

	/**
	 *
	 * @param request
	 * @param response
	 * @param actionID one of {@link ReloadServerDataProvider#ID}, {@link ReloadApplicationDataProvider#ID} or {@link ReloadSessionDataProvider#ID}
	 * @param provider html id of the {@link DisplayProvider}
	 * @param scope    one of {@link #SCOPE_TITLE}, {@link #SCOPE_CONTENT} or {@code null}
	 * @throws ServletException
	 * @throws IOException
	 */
	public static void sendRedirect(HttpServletRequest request, HttpServletResponse response, String actionID, String provider, String scope) throws ServletException, IOException {
		StringBuffer url = ((BaseAdminActionProvider)AdminActionProvider.Util.getInstance(request, actionID)).getURL(request, response);

		if (ReloadServerDataProvider.ID.equals(actionID) || ReloadApplicationDataProvider.ID.equals(actionID) || ReloadSessionDataProvider.ID.equals(actionID)) {
			url = ((BaseAdminActionProvider)AdminActionProvider.Util.getInstance(request, actionID)).getURL(request, response);
		} else {
			throw new IllegalArgumentException(actionID);
		}
		url.append('&').append(PARAM_PROVIDER).append('=').append(provider);
		if (StringUtils.isNotBlank(scope)) {
			url.append('&').append(PARAM_SCOPE).append('=').append(scope);
		}
		response.sendRedirect(response.encodeRedirectURL(url.toString()));
	}
}
