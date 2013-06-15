/**
 *
 */
package clime.messadmin.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Collections;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import clime.messadmin.core.MessAdmin;
import clime.messadmin.utils.DateUtils;

/**
 * Pass-trough response wrapper, looking up for message injection.
 * IMPLEMENTATION NOTE: do not buffer response here: it can be quite large...
 * TODO: for version 2, try to put the script at an even more suitable location (immediately after &lt;body&gt; or immediately before &lt;/body&gt; or &lt;/head&gt;)
 * @author C&eacute;drik LIME
 */
public class MessAdminResponseWrapper extends HttpServletResponseWrapper {
	private static final boolean DEBUG = false;
	private static Method getStatus = null;
	private static Method getHeaders = null;

	private static String[] SCRIPT_BEGIN = new String[] {"<script language='JavaScript' type='text/javascript'>",
		"<!--",
		"	var messadmin_width = 600; // screen.availWidth - 10",
		"	var messadmin_height = 400; // screen.availHeight - 20",
		"	var messadmin_screenX = Math.floor((screen.width)/2) - Math.floor(messadmin_width/2);",
		"	var messadmin_screenY = Math.floor((screen.height)/2) - Math.floor(messadmin_height/2) - 20;",
		"",
		"	var messadmin_features =",
		"		'toolbar=no,' +",
		"		'scrollbars=yes,' +",
		"		'status=no,' +",
		"		'location=no,' +",
		"		'directories=no,' +",
		"		'menubar=no,' +",
		"		'resizable=yes,' +",
		"		'width=' + messadmin_width + ',' +",
		"		'height=' + messadmin_height + ',' +",
		"		'top=' + messadmin_screenY + ',' +",
		"		'left=' + messadmin_screenX + ',' +",
		"",
		"		// NS only",
		"		'screenX=' + messadmin_screenX + ',' +",
		"		'screenY=' + messadmin_screenY + ',' +",
		"		'alwaysRaised=yes,' +",
		"		'dependent=yes,' +",
		"		'hotkeys=no,' +",
		"		'modal=yes';",
		"",
		"		// IE only",
		"	var messadmin_ieFeatures =",
		"		'center=yes,' +",
		"		'dialogHeight=' + messadmin_height + ',' +",
		"		'dialogWidth=' + messadmin_width + ',' +",
		"		'dialogTop=' + messadmin_screenY + ',' +",
		"		'dialogLeft=' + messadmin_screenX + ',' +",
		"		'resizable=yes,' +",
		"		'help=no';",
		"",
//		"	//URL - string containing the URL of the document to open in the new window. If no URL is specified, an empty window will be created.",
//		"	//name - string containing the name of the new window. This can be used as the 'target' attribute of a <form> or <a> tag to point to the new window.",
//		"	//features - optional string that contains details of which of the standard window features are to be used with the new window.",
		"	var messadminpopup;",
		"	//if (document.all && window.print) { // IE5",
		// Do not confuse Modeless windows (showModelessWindow) of IE5 with Modal Windows (showModalDialog) of IE4.
		// While both remain the active window until closed, the later is somewhat a "pest" in that the rest of the
		// page is "hung up" until the window is dismissed. We like modeless windows better.
		// Note that links inside modeless windows are always launched in a new page.
		"	//	messadminpopup = window.showModelessWindow('', 'MessAdminPopUpPage', messadmin_ieFeatures);",
		"	//} else {",
		"		messadminpopup = window.open('', 'MessAdminPopUpPage', messadmin_features);",
		"		if (! messadminpopup) {",
		"			alert('Your pop-up blocker is preventing us from showing an administrative message. Please change your browser settings to allow popups from ' + window.location.host);",//FIXME i18n
		"		}",
		"		if (messadminpopup && !messadminpopup.closed) { messadminpopup.close(); }",
		"		messadminpopup = window.open('', 'MessAdminPopUpPage', messadmin_features);",
		"	//}",
		"	if (messadminpopup) {//don't do anything if popup was blocked...",
		"	if (!messadminpopup.opener) { messadminpopup.opener = self; }",
		"	messadminpopup.document.writeln('<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">');",
		"	messadminpopup.document.writeln('<html>');",
		"	messadminpopup.document.writeln('<head><title>Administrative message</title></head>');",//FIXME i18n
		"	messadminpopup.document.writeln('<body onload=\"window.focus()\" onblur=\"self.focus()\">');",
		"	messadminpopup.document.writeln('"};
	private static final String[] SCRIPT_END = new String[] {
		"');",
		"	messadminpopup.document.writeln('</body></html>');",
		"	//self.blur();",
		"	setTimeout('messadminpopup.focus()', 500);",
		"	messadminpopup.focus();",
		//"	document.body.getAttributeNode('onunload').value = 'messadminpopup.close();' + document.body.getAttributeNode('onunload').value;",
		"	}",
		"// -->",
		"</script>"};

	private static final short JS_SCRIPT_SIZE;//bytes

	static {
		short scriptSize = 0;
		for (int i = 0; i < SCRIPT_BEGIN.length; ++i) {
			String line = SCRIPT_BEGIN[i];
			scriptSize += 1+line.length();//FIXME: depends on encoding!
		}
		for (int i = 0; i < SCRIPT_END.length; ++i) {
			String line = SCRIPT_END[i];
			scriptSize += 1+line.length();//FIXME: depends on encoding!
		}
		JS_SCRIPT_SIZE = scriptSize;

		// @since 3.0
		try {
			getStatus = ServletResponse.class.getMethod("getStatus");//$NON-NLS-1$
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		}
		// @since 3.0
		try {
			getHeaders = ServletResponse.class.getMethod("getHeaders", String.class);//$NON-NLS-1$
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		}
	}

	protected String injectedMessageHTML = null;
	protected boolean shouldInject = false;
	protected boolean messageInjected = false;
	private final HttpServletResponse httpResponse;
	private int contentLength = -1;
	private String contentType = null;
	private PrintWriter writer = null;
	private ServletOutputStream stream = null;
	protected long responseBodyLength = -1;
	protected long responseHeaderLength = 0;
	protected int status;

	/**
	 * @param response
	 */
	public MessAdminResponseWrapper(HttpServletResponse response) {
		super(response);
		this.httpResponse = response;
		contentType = response.getContentType();
		setShouldInject();
	}

	public int getContentLength() {
		return contentLength;
	}
	/** {@inheritDoc} */
	@Override
	public void setContentLength(int len) {
		int realLength = len;
		if (shouldInject) {
			realLength += JS_SCRIPT_SIZE + injectedMessageHTML.length();//FIXME: depends on encoding!
		}
		super.setContentLength(realLength);
		contentLength = realLength;
	}

	/** {@inheritDoc} */
	@Override
	public void resetBuffer() throws IllegalStateException {
		super.resetBuffer();
		resetInternal();
	}

	private void resetInternal() {
		responseBodyLength = -1;
		messageInjected = false;
		//FIXME injectedMessageHTML could have been set to null!
		setShouldInject();
	}

	/** {@inheritDoc} */
	@Override
	public void reset() throws IllegalStateException {
		super.reset();
		resetInternal();
		responseHeaderLength = 0;
		contentLength = -1;
		contentType = null;
		status = 0;//needed as getStatus reads this value!
		status = getStatus(httpResponse);
	}

	/**
	 * Will only be valid at end of response cycle, when reponse is flush'ed.
	 * @return byte count of response
	 */
	public long getResponseBodyLength() {
		return (responseBodyLength == -1) ? getContentLength() : responseBodyLength + 1;
	}

	/**
	 * @return the total response length (header + body)
	 */
	public long getResponseLength() {
		long result = getResponseBodyLength();
		if (result == -1) {
			result = 0; // reset
		}
		result += getHeadersSize();
		return result;
	}

	protected long getHeadersSize() {
		return responseHeaderLength + 1;//+1: empty line separator between headers and body
	}

	/** {@inheritDoc} */
	@Override
	public void setContentType(String type) {
		super.setContentType(type);
		if (super.isCommitted()) {
			return;
		}
		contentType = super.getContentType();
		setShouldInject();
	}

	/** {@inheritDoc} */
	@Override
	public void setStatus(int sc) {
		super.setStatus(sc);
		status = sc;
	}

	/** {@inheritDoc} */
	@Override
	public void setStatus(int sc, String sm) {
		super.setStatus(sc, sm);
		status = sc;
	}

	/** {@inheritDoc} */
	@Override
	public void sendError(int sc) throws IOException {
		super.sendError(sc);
		status = sc;
	}

	/** {@inheritDoc} */
	@Override
	public void sendError(int sc, String msg) throws IOException {
		super.sendError(sc, msg);
		status = sc;
	}

	/** {@inheritDoc} */
	@Override
	public void sendRedirect(String location) throws IOException {
		super.sendRedirect(location);
		status = HttpServletResponse.SC_MOVED_TEMPORARILY;//SC_FOUND
	}

	/**
	 * {@inheritDoc}
	 * @since Servlet 3.0
	 */
//	@Override
	public int getStatus() {
		return status;
	}

	/**
	 * {@inheritDoc}
	 * Warning: this method will return {@code 0} for Servlet < 3.0!
	 * @since Servlet 3.0
	 */
//	@Override
	public static int getStatus(HttpServletResponse httpResponse) {
		int status = 0;
		if (httpResponse instanceof MessAdminResponseWrapper) {
			status = ((MessAdminResponseWrapper)httpResponse).getStatus();
		}
		if (getStatus != null) {
			//status = response.getStatus();
			try {
				status = ((Integer) getStatus.invoke(httpResponse)).intValue();
			} catch (IllegalArgumentException iae) {
			} catch (IllegalAccessException iae) {
			} catch (InvocationTargetException ite) {
			}
		}
		return status;
	}

	/**
	 * {@inheritDoc}
	 * Warning: this method will return an empty {@code Collection} for Servlet < 3.0!
	 * @since Servlet 3.0
	 */
//	@Override
	public static Collection<String> getHeaders(HttpServletResponse httpResponse, String name) {
		Collection<String> values = Collections.emptyList();
		if (getHeaders != null) {
			try {
				values = (Collection<String>) getHeaders.invoke(httpResponse, new Object[] {name});
			} catch (IllegalArgumentException iae) {
			} catch (IllegalAccessException iae) {
			} catch (InvocationTargetException ite) {
			}
		}
		return values;
	}

	/***********************************************************************/
	/* Header size counting */
	/* Note that this is not very accurate.
	 * In particular, this does not count Server, Content-Type, Content-Length and Date */
	/* To be accurate means we must account for all setXXXHeader(),
	 * which could be expansive and wasteful in this particular context */
	/***********************************************************************/

	private void registerSetHeader(String name, String value) {
		if (super.containsHeader(name)) {
			// Substract old value and add new one. There is no API to get a header until Servlet 3.0,
			// and we don't want to keep a heavy structure in place for that,
			// so don't do anything for old containers (i.e. consider oldValue.length == newValue.length).
			if (getHeaders != null) {
				//oldValues = response.getHeaders(name);
				for (String oldValue : getHeaders(httpResponse, name)) {
					responseHeaderLength -= name.length() + 3 + oldValue.length();//+3: "name: value\n"
				}
				registerAddHeader(name, value);
			} // else do nothing (i.e. consider oldValue.length == newValue.length)
		} else {
			registerAddHeader(name, value);
		}
	}
	private void registerAddHeader(String name, String value) {
		responseHeaderLength += name.length() + 3 + value.length();//+3: "name: value\n"
	}

	/** {@inheritDoc} */
	@Override
	public void setDateHeader(String name, long date) {
		registerSetHeader(name, DateUtils.formatRFC2822Date(date));
		super.setDateHeader(name, date);
	}

	/** {@inheritDoc} */
	@Override
	public void addDateHeader(String name, long date) {
		registerAddHeader(name, DateUtils.formatRFC2822Date(date));
		super.addDateHeader(name, date);
	}

	/** {@inheritDoc} */
	@Override
	public void setHeader(String name, String value) {
		registerSetHeader(name, value);
		super.setHeader(name, value);
		// special cases
		if (name.equalsIgnoreCase("Content-Type")) {
			contentType = value;
			setShouldInject();
		}
		if (name.equalsIgnoreCase("Content-Length")) {
		}
	}

	/** {@inheritDoc} */
	@Override
	public void addHeader(String name, String value) {
		registerAddHeader(name, value);
		super.addHeader(name, value);
	}

	/** {@inheritDoc} */
	@Override
	public void setIntHeader(String name, int value) {
		registerSetHeader(name, Integer.toString(value));
		super.setIntHeader(name, value);
	}

	/** {@inheritDoc} */
	@Override
	public void addIntHeader(String name, int value) {
		registerAddHeader(name, Integer.toString(value));
		super.addIntHeader(name, value);
	}

	/***********************************************************************/
	/* Message injection */
	/***********************************************************************/

	protected void setWarning() {
		setShouldInject();
		if (shouldInject && ! httpResponse.isCommitted()) {
			String warnAgent = null;
			try {
				warnAgent = InetAddress.getLocalHost().toString();
			} catch (Exception e) {
				warnAgent = "MessAdmin/"+MessAdmin.getVersion();
			}
			httpResponse.addHeader("Warning", "199 "+warnAgent+" MessAdmin injected a popup message in this message.");
			// 199 Miscellaneous warning The warning text MAY include arbitrary information to be presented to a human user, or logged.
			// A system receiving this warning MUST NOT take any automated action, besides presenting the warning to the user.
		}
	}

	/** {@inheritDoc} */
	@Override
	public synchronized PrintWriter getWriter() throws IOException {
		setWarning();
		if (writer == null) {
			writer = new CountingInjectorPrintWriter(super.getWriter());
		}
		return writer;
	}

	/** BUG: if <head> has attributes, it won't be catched. FIXME! Look for </head> instead (more difficult: buffering) or knowledge of spaces+attributes */
	protected class CountingInjectorPrintWriter extends PrintWriter {
		private final char[] search = new char[] {'<', 'h', 'e', 'a', 'd', '>'};
		private byte pos = 0;
		public CountingInjectorPrintWriter(PrintWriter outWriter) {
			super(outWriter);
		}
		/** {@inheritDoc} */
		@Override
		public void write(int c) {
			if (shouldInject) {
				// look for <head> and inject
				if (Character.toLowerCase((char)c) == search[pos]) {
					++pos;
				} else {
					pos = 0;
				}
				if (pos >= search.length) {
					try {
						super.write(c);
						++responseBodyLength;
						// inject message immediatly after '<head>'
						injectMessage((PrintWriter)out);
					} catch (IOException e) {
						setError();
					}
					shouldInject = false;//should not be required, but more prudent...
					pos = 0;
					return;
				}
			}
			super.write(c);
			++responseBodyLength;
		}
		/** {@inheritDoc} */
		@Override
		public void write(char[] cbuf, int off, int len) {
			if (shouldInject) {
				// look for <head> and inject
				for (int i = off; i < off+len; ++i) {
					if (Character.toLowerCase(cbuf[i]) == search[pos]) {
						++pos;
					} else {
						pos = 0;
					}
					if (pos >= search.length) {
						try {
							++i;//we can because of return at end
							super.write(cbuf, off, i-off);
							// inject message immediatly after '<head>'
							injectMessage((PrintWriter)out);
							super.write(cbuf, off+i, len-(i-off));
							responseBodyLength += len - off;
						} catch (IOException e) {
							setError();
						}
						shouldInject = false;//should not be required, but more prudent...
						pos = 0;
						return;
					}
				}
			}
			super.write(cbuf, off, len);
			responseBodyLength += len - off;
		}
		/** {@inheritDoc} */
		@Override
		public void write(String s, int off, int len) {
			if (shouldInject) {
				// look for <head> and inject
				for (int i = off; i < off+len; ++i) {
					if (Character.toLowerCase(s.charAt(i)) == search[pos]) {
						++pos;
					} else {
						pos = 0;
					}
					if (pos >= search.length) {
						try {
							++i;//we can because of return at end
							super.write(s, off, i-off);
							// inject message immediatly after '<head>'
							injectMessage((PrintWriter)out);
							super.write(s, off+i, len-(i-off));
							responseBodyLength += len - off;//FIXME: depends on encoding!
						} catch (IOException e) {
							setError();
						}
						shouldInject = false;//should not be required, but more prudent...
						pos = 0;
						return;
					}
				}
			}
			super.write(s, off, len);
			responseBodyLength += len - off;//FIXME: depends on encoding!
		}

	}

	/** {@inheritDoc} */
	@Override
	public synchronized ServletOutputStream getOutputStream() throws IOException {
		setWarning();
		if (stream == null) {
			stream = new CountingServletOutputStream(super.getOutputStream());
		}
		return stream;
	}

	protected class CountingServletOutputStream extends ServletOutputStream {
		protected ServletOutputStream out;
		public CountingServletOutputStream(ServletOutputStream out) {
			this.out = out;
		}
		/** {@inheritDoc} */
		@Override
		public void write(int b) throws IOException {
			out.write(b);
			++responseBodyLength;
		}
		/** {@inheritDoc} */
		@Override
		public void write(byte[] b) throws IOException {
			out.write(b);
			responseBodyLength += b.length;
		}
		/** {@inheritDoc} */
		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			out.write(b, off, len);
			responseBodyLength += len;
		}
		@Override
		public void flush() throws IOException {
			out.flush();
		}
		@Override
		public void close() throws IOException {
			out.close();
		}
	}

	/**
	 * Injects code in output stream
	 * mess up with response encoding (convert message from UTF to XYZ)? No: handled by the PrintWriter
	 * @return true if message was injected, false otherwise
	 * @throws IOException
	 */
	public boolean finish() throws IOException {
		try {
			PrintWriter out = httpResponse.getWriter();
			return injectMessage(out);
		} catch (IllegalStateException ise) {
			// abort, don't do anything: getOutputStream() was previously called
			if (DEBUG) {
				System.err.println(this.getClass().getName() + ": can't inject: " + ise.getLocalizedMessage());//$NON-NLS-1$
			}
		}
		return false;
	}

	protected boolean injectMessage(PrintWriter out) throws IOException {
		setShouldInject();
		if (! shouldInject) {
			// no message to inject
			return false;
		}
		out.println();
		for (int i = 0; i < SCRIPT_BEGIN.length-1; ++i) {
			String line = SCRIPT_BEGIN[i];
			out.println(line);
		}
		out.print(SCRIPT_BEGIN[SCRIPT_BEGIN.length-1]);
		escapeJavaStyleString(out, injectedMessageHTML, true, true);
		for (int i = 0; i < SCRIPT_END.length; ++i) {
			String line = SCRIPT_END[i];
			out.println(line);
		}
		if (DEBUG) {
			System.out.println(this.getClass().getName() + ": injected message");//$NON-NLS-1$
		}
		responseBodyLength += JS_SCRIPT_SIZE + injectedMessageHTML.length();//FIXME: depends on encoding!
		shouldInject = false;
		injectedMessageHTML = null; // message is gone; information used by MessAdminFilter
		messageInjected = true;
		return true;
	}

	public String getInjectedMessageHTML() {
		return injectedMessageHTML;
	}
	public void setInjectedMessageHTML(String in_injectedMessage) {
		injectedMessageHTML = in_injectedMessage;
		setShouldInject();
	}

	private void setShouldInject() {
		// is there a message to inject?
		shouldInject = (injectedMessageHTML != null && injectedMessageHTML.length() != 0 && !"".equals(injectedMessageHTML.trim()));//$NON-NLS-1$
		// does the contentType allow HTML injection?
		final String l_contentType = (contentType != null) ? contentType.toLowerCase() : "";//$NON-NLS-1$
		if (shouldInject && (l_contentType.indexOf("text/html")==-1) && (l_contentType.indexOf("application/xhtml+xml")==-1)) {//$NON-NLS-1$ //$NON-NLS-2$
			// don't inject anything in non-html stuff or if no message to inject!
			shouldInject = false;
			if (DEBUG) {
				System.out.println(this.getClass().getName() + ": no injection (not html)");//$NON-NLS-1$
			}
		}
	}

	public boolean isMessageInjected() {
		return messageInjected;
	}

	/***********************************************************************/

	/**
	 * COPIED FROM COMMONS-LANG 2.5 org.apache.commons.lang.StringEscapeUtils
	 *
	 * <p>Escapes the characters in a <code>String</code> using JavaScript String rules
	 * to a <code>Writer</code>.</p>
	 *
	 * <p>Escapes any values it finds into their JavaScript String form.
	 * Deals correctly with quotes and control-chars (tab, backslash, cr, ff, etc.) </p>
	 *
	 * <p>So a tab becomes the characters <code>'\\'</code> and
	 * <code>'t'</code>.</p>
	 *
	 * <p>The only difference between Java strings and JavaScript strings
	 * is that in JavaScript, a single quote must be escaped.</p>
	 *
	 * <p>Example:
	 * <pre>
	 * input string: He didn't say, "Stop!"
	 * output string: He didn\'t say, \"Stop!\"
	 * </pre>
	 * </p>
	 *
	 * <p>A <code>null</code> string input has no effect.</p>
	 *
	 * @param out  Writer to write escaped string into
	 * @param str  String to escape values in, may be null
	 * @param escapeSingleQuote escapes single quotes if <code>true</code>
	 * @param escapeForwardSlash
	 * @throws IllegalArgumentException if the Writer is <code>null</code>
	 * @throws IOException if error occurs on undelying Writer
	 **/
	private static void escapeJavaStyleString(Writer out, String str, boolean escapeSingleQuote, boolean escapeForwardSlash) throws IOException {
		if (out == null) {
			throw new IllegalArgumentException("The Writer must not be null");//$NON-NLS-1$
		}
		if (str == null) {
			return;
		}
		int sz;
		sz = str.length();
		for (int i = 0; i < sz; ++i) {
			char ch = str.charAt(i);

			// handle unicode
			if (ch > 0xfff) {
				out.write("\\u" + hex(ch));//$NON-NLS-1$
			} else if (ch > 0xff) {
				out.write("\\u0" + hex(ch));//$NON-NLS-1$
			} else if (ch > 0x7f) {
				out.write("\\u00" + hex(ch));//$NON-NLS-1$
			} else if (ch < 32) {
				switch (ch) {
					case '\b':
						out.write('\\');
						out.write('b');
						break;
					case '\n':
						out.write('\\');
						out.write('n');
						break;
					case '\t':
						out.write('\\');
						out.write('t');
						break;
					case '\f':
						out.write('\\');
						out.write('f');
						break;
					case '\r':
						out.write('\\');
						out.write('r');
						break;
					default :
						if (ch > 0xf) {
							out.write("\\u00" + hex(ch));//$NON-NLS-1$
						} else {
							out.write("\\u000" + hex(ch));//$NON-NLS-1$
						}
						break;
				}
			} else {
				switch (ch) {
					case '\'':
						if (escapeSingleQuote) {
							out.write('\\');
						}
						out.write('\'');
						break;
					case '"':
						out.write('\\');
						out.write('"');
						break;
					case '\\':
						out.write('\\');
						out.write('\\');
						break;
					case '/' :
						if (escapeForwardSlash) {
							out.write('\\');
						}
						out.write('/');
						break;
					default :
						out.write(ch);
						break;
				}
			}
		}
	}
	/**
	 * COPIED FROM COMMONS-LANG 2.5 org.apache.commons.lang.StringEscapeUtils
	 *
	 * <p>Returns an upper case hexadecimal <code>String</code> for the given
	 * character.</p>
	 *
	 * @param ch The character to convert.
	 * @return An upper case hexadecimal <code>String</code>
	 */
	private static String hex(char ch) {
		return Integer.toHexString(ch).toUpperCase();
	}

}
