package clime.messadmin.admin;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import clime.messadmin.core.Constants;
import clime.messadmin.providers.spi.DisplayFormatProvider;
import clime.messadmin.providers.spi.DisplayProvider;

/**
 * IMPLEMENTATION NOTE: always use include() instead of forward(), to get the real name of this servlet in jsp's
 * @author C&eacute;drik LIME
 * @since 4.2
 */
public abstract class BaseAdminActionProvider extends HttpServlet implements AdminActionProvider {
	protected static final boolean DEBUG = false;

	protected static final String METHOD_DELETE  = "DELETE";//$NON-NLS-1$
	protected static final String METHOD_HEAD    = "HEAD";//$NON-NLS-1$
	protected static final String METHOD_GET     = "GET";//$NON-NLS-1$
	protected static final String METHOD_OPTIONS = "OPTIONS";//$NON-NLS-1$
	protected static final String METHOD_POST    = "POST";//$NON-NLS-1$
	protected static final String METHOD_PUT     = "PUT";//$NON-NLS-1$
	protected static final String METHOD_TRACE   = "TRACE";//$NON-NLS-1$

	protected static final String HEADER_IFMODSINCE   = "If-Modified-Since";//$NON-NLS-1$
	protected static final String HEADER_LASTMOD      = "Last-Modified";//$NON-NLS-1$
	protected static final String HEADER_CACHECONTROL = "Cache-Control";//$NON-NLS-1$
	protected static final String HEADER_EXPIRES      = "Expires";//$NON-NLS-1$

	public BaseAdminActionProvider() {
		super();
	}

	/** {@inheritDoc} */
	public int getPriority() {
		// Useless
		return 0;
	}

	/** {@inheritDoc} */
	public boolean preService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DisplayFormatProvider displayFormatProvider = DisplayFormatProvider.Util.getInstance(request);
		if (displayFormatProvider != null) {
			response.setContentType(displayFormatProvider.getContentType());
			//resp.setCharacterEncoding("UTF-8");//$NON-NLS-1$
			displayFormatProvider.preProcess(request, response);
		}
		return true;
	}

	/** {@inheritDoc} */
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		super.service(request, response);
	}

	/** {@inheritDoc} */
	public void log(String message) {
		if (DEBUG) {
			getServletContext().log(getServletName() + ": " + message);//$NON-NLS-1$
		}
	}

	/** {@inheritDoc} */
	public void log(String message, Throwable t) {
		if (DEBUG) {
			getServletContext().log(getServletName() + ": " + message, t);//$NON-NLS-1$
		}
	}

	public void setNoCache(HttpServletResponse response) {
		// <strong>NOTE</strong> - This header will be overridden
		// automatically if a <code>RequestDispatcher.forward()</code> call is
		// ultimately invoked.
		//resp.setHeader("Pragma", "No-cache"); // HTTP 1.0 //$NON-NLS-1$ //$NON-NLS-2$
		response.setHeader(HEADER_CACHECONTROL, "no-cache,no-store,max-age=0"); // HTTP 1.1 //$NON-NLS-1$
		response.setDateHeader(HEADER_EXPIRES, 0); // 0 means now
		// should we decide to enable caching, here are the current vary:
		response.addHeader("Vary", "Accept-Language,Accept-Encoding,Accept-Charset");
	}

	private StringBuffer getMessagesURL(HttpServletRequest request, HttpServletResponse response) {
		StringBuffer buffer = new StringBuffer();
		String message = (String) request.getAttribute(Constants.APPLICATION_MESSAGE);
		if (message != null && !"".equals(message.trim())) {
			buffer.append('&').append(Constants.APPLICATION_MESSAGE).append('=').
				append(clime.messadmin.taglib.core.Util.URLEncode(message, response.getCharacterEncoding()));
		}
		message = (String) request.getAttribute(Constants.APPLICATION_ERROR);
		if (message != null && !"".equals(message.trim())) {
			buffer.append('&').append(Constants.APPLICATION_ERROR).append('=')
				.append(clime.messadmin.taglib.core.Util.URLEncode(message, response.getCharacterEncoding()));
		}
		return buffer;
	}

	/**
	 * @return the URL to this action servlet
	 */
	public StringBuffer getURL(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		StringBuffer url = request.getRequestURL()
			.append('?').append(ACTION_PARAMETER_NAME).append('=').append(getActionID());
		String format = request.getParameter(DisplayFormatProvider.Util.FORMAT_PARAMETER_NAME);
		if (format != null && !"".equals(format)) {
			url.append('&').append(DisplayFormatProvider.Util.FORMAT_PARAMETER_NAME).append('=').append(format);
		}
		url.append(getMessagesURL(request, response));
		return url;
	}

	public void sendRedirect(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendRedirect(response.encodeRedirectURL(getURL(request, response).toString()));
	}

	public static String buildActionLink(String url, String linkText, DisplayProvider displayProvider) {
		StringBuffer out = new StringBuffer(32);
		out.append("<a href=\"").append(url).append('"').append(onclick(url, displayProvider)).append('>');
		out.append(linkText).append("</a>");
		return out.toString();
	}
	public static String buildActionLink(String url, String linkText, String jsConfirmationMessage, DisplayProvider displayProvider) {
		StringBuffer out = new StringBuffer(32);
		out.append("<a href=\"").append(url).append('"').append(onclick(url, jsConfirmationMessage, displayProvider)).append('>');
		out.append(linkText).append("</a>");
		return out.toString();
	}
	public static String buildSubmitButton(String url, String buttonText, DisplayProvider displayProvider) {
		StringBuffer out = new StringBuffer(32);
		out.append("<input type=\"submit\" value=\"").append(buttonText).append('"').append(onclick(url, displayProvider)).append(" />");
		return out.toString();
	}
	public static String buildSubmitButton(String url, String buttonText, String jsConfirmationMessage, DisplayProvider displayProvider) {
		StringBuffer out = new StringBuffer(32);
		out.append("<input type=\"submit\" value=\"").append(buttonText).append('"').append(onclick(url, jsConfirmationMessage, displayProvider)).append(" />");
		return out.toString();
	}
	private static String onclick(String url, DisplayProvider displayProvider) {
		StringBuffer out = new StringBuffer(32);
		out.append("onclick=\"jah('").append(url).append("','").append(DisplayProvider.Util.getId(displayProvider)).append("','POST'); return false;\"");
		return out.toString();
	}
	private static String onclick(String url, String jsConfirmationMessage, DisplayProvider displayProvider) {
		StringBuffer out = new StringBuffer(32);
		out.append(" onclick=\"if (window.confirm('").append(jsConfirmationMessage).append("')) {jah('").append(url).append("','").append(DisplayProvider.Util.getId(displayProvider)).append("','POST');} return false;\"");
		return out.toString();
	}

	protected String urlEncodeUTF8(String str) {
		try {
			return URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException uue) {
			throw new RuntimeException(uue);
		}
	}
}
