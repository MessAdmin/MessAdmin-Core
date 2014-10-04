/**
 * 
 */
package clime.messadmin.admin.actions;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import clime.messadmin.admin.AdminActionProvider;
import clime.messadmin.admin.BaseAdminActionProvider;
import clime.messadmin.utils.Charsets;

/**
 * Debug tool: echo HTTP request headers
 * 
 * @author C&eacute;drik LIME
 * @since 5.4
 */
public class Echo extends BaseAdminActionProvider implements AdminActionProvider {
	public static final String ID = "echo";//$NON-NLS-1$

	public Echo() {
		super();
	}

	/** {@inheritDoc} */
	public String getActionID() {
		return ID;
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		setNoCache(response);
		response.setContentType("text/plain; encoding="+Charsets.UTF_8.name());
		response.setCharacterEncoding(Charsets.UTF_8.name());
		PrintWriter out = response.getWriter();

//		String htmlTable = new clime.messadmin.providers.spi.BaseTabularDataProvider() {
//			public String buildXHTML(ServletRequest request) {
//				return buildXHTML(new String[] {"Method name", "Value"}, new Object[][], "extraAttributesTable-" + getClass().getName(), "tableCaption");
//			}
//		}.buildXHTML(request);

		{
			out.print(request.getMethod());
			out.print(' ');
			out.print(request.getRequestURI());
			if (request.getQueryString() != null) {
				out.print('?');
				out.print(request.getQueryString());
			}
			out.print(' ');
			out.println(request.getProtocol());
		}
		Enumeration<String> headerNames = request.getHeaderNames();
		if (headerNames != null) {
			while (headerNames.hasMoreElements()) {
				String headerName = headerNames.nextElement();
				Enumeration<String> headerValues = request.getHeaders(headerName);
				if (headerValues != null) {
					while (headerValues.hasMoreElements()) {
						String value = headerValues.nextElement();
						out.print(headerName);
						out.print(": ");
						out.println(value);
					}
				}
			}
		}

		out.println();
		out.println();

		out.println("Java HttpServletRequest API");
		out.println("===========================");
		out.println();

		// ServletRequest
		out.print("getCharacterEncoding()\t");
		out.println(request.getCharacterEncoding());
		out.print("getContentLength()\t");
		out.println(request.getContentLength());
		out.print("getContentType()\t");
		out.println(request.getContentType());
//		request.getParameterNames();
//		request.getParameterMap();
		out.print("getProtocol()\t\t");
		out.println(request.getProtocol());
		out.print("getScheme()\t\t");
		out.println(request.getScheme());
		out.print("getServerName()\t\t");
		out.println(request.getServerName());
		out.print("getServerPort()\t\t");
		out.println(request.getServerPort());
		out.print("getRemoteAddr()\t\t");
		out.println(request.getRemoteAddr());
		out.print("getRemoteHost()\t\t");
		out.println(request.getRemoteHost());
		out.print("getRemotePort()\t\t");
		out.println(request.getRemotePort());
		out.print("getLocale()\t\t");
		out.println(request.getLocale());
		out.print("getLocales()\t\t");
		Enumeration<Locale> locales = request.getLocales();
		while (locales.hasMoreElements()) {
			Locale locale = locales.nextElement();
			out.print(locale);
			if (locales.hasMoreElements()) {
				out.print(", ");
			}
		}
		out.println();
		out.print("isSecure()\t\t");
		out.println(request.isSecure());
		out.print("getLocalName()\t\t");
		out.println(request.getLocalName());
		out.print("getLocalAddr()\t\t");
		out.println(request.getLocalAddr());
		out.print("getLocalPort()\t\t");
		out.println(request.getLocalPort());

		out.println();

		// HttpServletRequest
		out.print("getAuthType()\t\t");
		out.println(request.getAuthType());
		out.print("getCookies()\t\t");
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			out.print(Arrays.toString(cookies));
		}
		out.println();
//		Enumeration headerNames = request.getHeaderNames();
		out.print("getMethod()\t\t");
		out.println(request.getMethod());
		out.print("getPathInfo()\t\t");
		out.println(request.getPathInfo());
		out.print("getPathTranslated()\t");
		out.println(request.getPathTranslated());
		out.print("getContextPath()\t");
		out.println(request.getContextPath());
		out.print("getQueryString()\t");
		out.println(request.getQueryString());
		out.print("getRemoteUser()\t\t");
		out.println(request.getRemoteUser());
		out.print("getUserPrincipal()\t");
		Principal userPrincipal = request.getUserPrincipal();
		out.println(userPrincipal);
		out.print("getRequestedSessionId()\t");
		out.println(request.getRequestedSessionId());
		out.print("getRequestURI()\t\t");
		out.println(request.getRequestURI());
		out.print("getRequestURL()\t\t");
		out.println(request.getRequestURL());
		out.print("getServletPath()\t");
		out.println(request.getServletPath());
		out.print("getSession(false)\t");
		HttpSession session = request.getSession(false);
		out.println(session);
		out.print("isRequestedSessionIdValid()\t\t");
		out.println(request.isRequestedSessionIdValid());
		out.print("isRequestedSessionIdFromCookie()\t");
		out.println(request.isRequestedSessionIdFromCookie());
		out.print("isRequestedSessionIdFromURL()\t\t");
		out.println(request.isRequestedSessionIdFromURL());

		out.close();
	}
}
