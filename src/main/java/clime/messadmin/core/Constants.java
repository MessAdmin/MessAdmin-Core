/**
 *
 */
package clime.messadmin.core;

/**
 * Should be a Java 5 Enum...
 * @author C&eacute;drik LIME
 */
public interface Constants {
	/**
	 * Key for a session message
	 */
	public static final String SESSION_MESSAGE_KEY = "clime.messadmin.message";//$NON-NLS-1$
	/**
	 * Key for a global (application-scope) message
	 */
	public static final String GLOBAL_MESSAGE_KEY = "clime.messadmin.message.global";//$NON-NLS-1$
	/**
	 * Key for last application-level message display timestamp
	 */
	public static final String GLOBAL_MESSAGE_TIMESTAMP_KEY = "clime.messadmin.message.global.timestamp";//$NON-NLS-1$
	/**
	 * Minimum time in milliseconds between application-level message display: {@value}
	 */
	public static final long GLOBAL_MESSAGE_DELTA_TIME_MIN = 60*1000;//TODO externalize in .properties

	/* HttpServletRequest parameter constants */

	public static final String APPLICATION_MESSAGE = "message";//$NON-NLS-1$
	public static final String APPLICATION_ERROR   = "error";//$NON-NLS-1$

	// HttpServletRequest attributes

	/** cipher suite (String) */
	public static final String SSL_CIPHER_SUITE = "javax.servlet.request.cipher_suite";//$NON-NLS-1$
	/** bit size of the algorithm (Integer) */
	public static final String SSL_KEY_SIZE     = "javax.servlet.request.key_size";//$NON-NLS-1$
	/**
	 * SSL session id (String)
	 * @since Servlet 3.0
	 */
	public static final String SSL_SESSION_ID   = "javax.servlet.request.ssl_session_id";//$NON-NLS-1$
	/** SSL certificate associated with the request (X509Certificate[] or {@code null}) */
	public static final String SSL_CERTIFICATE  = "javax.servlet.request.X509Certificate";//$NON-NLS-1$

	public static final String INCLUDE_REQUEST_URI  = "javax.servlet.include.request_uri";//$NON-NLS-1$
	public static final String INCLUDE_CONTEXT_PATH = "javax.servlet.include.context_path";//$NON-NLS-1$
	public static final String INCLUDE_SERVLET_PATH = "javax.servlet.include.servlet_path";//$NON-NLS-1$
	public static final String INCLUDE_PATH_INFO    = "javax.servlet.include.path_info";//$NON-NLS-1$
	public static final String INCLUDE_QUERY_STRING = "javax.servlet.include.query_string";//$NON-NLS-1$
	public static final String FORWARD_REQUEST_URI  = "javax.servlet.forward.request_uri";//$NON-NLS-1$
	public static final String FORWARD_CONTEXT_PATH = "javax.servlet.forward.context_path";//$NON-NLS-1$
	public static final String FORWARD_SERVLET_PATH = "javax.servlet.forward.servlet_path";//$NON-NLS-1$
	public static final String FORWARD_PATH_INFO    = "javax.servlet.forward.path_info";//$NON-NLS-1$
	public static final String FORWARD_QUERY_STRING = "javax.servlet.forward.query_string";//$NON-NLS-1$
	public static final String ASYNC_REQUEST_URI    = "javax.servlet.async.request_uri";//$NON-NLS-1$
	public static final String ASYNC_CONTEXT_PATH   = "javax.servlet.async.context_path";//$NON-NLS-1$
	public static final String ASYNC_SERVLET_PATH   = "javax.servlet.async.servlet_path";//$NON-NLS-1$
	public static final String ASYNC_PATH_INFO      = "javax.servlet.async.path_info";//$NON-NLS-1$
	public static final String ASYNC_QUERY_STRING   = "javax.servlet.async.query_string";//$NON-NLS-1$

	/* If the location of the error handler is a servlet or a JSP page:
	 * + The original unwrapped request and response objects created by the container are passed to the servlet or JSP page.
	 * + The request path and attributes are set as if a RequestDispatcher.forward to the error resource had been performed.
	 */
	/** (Integer) */
	public static final String ERROR_STATUS_CODE    = "javax.servlet.error.status_code";//$NON-NLS-1$
	/**
	 * (Class)
	 * @deprecated since Servlet 2.3
	 */
	public static final String ERROR_EXCEPTION_TYPE = "javax.servlet.error.exception_type";//$NON-NLS-1$
	/**
	 * (String)
	 * @deprecated since Servlet 2.3
	 */
	public static final String ERROR_MESSAGE        = "javax.servlet.error.message";//$NON-NLS-1$
	/** (Throwable) */
	public static final String ERROR_EXCEPTION      = "javax.servlet.error.exception";//$NON-NLS-1$
	/**
	 * (Throwable)
	 * @deprecated since JSP 2.0
	 */
	public static final String ERROR_JSP_EXCEPTION  = "javax.servlet.jsp.jspException";//$NON-NLS-1$
	public static final String ERROR_REQUEST_URI    = "javax.servlet.error.request_uri";//$NON-NLS-1$
	/** (String) */
	public static final String ERROR_SERVLET_NAME   = "javax.servlet.error.servlet_name";//$NON-NLS-1$

	// ServletContext attributes

	/** (File) */
	public static final String TEMP_DIR = "javax.servlet.context.tempdir";//$NON-NLS-1$
	/**
	 * List of
	 * names of JAR files in the {@code WEB-INF/lib} directory of the application represented by
	 * the {@code ServletContext}, ordered by their web fragment names (with possible
	 * exclusions if fragment JAR files have been excluded from {@code absolute-ordering}), or
	 * {@code null} if the application does not specify any absolute or relative ordering
	 *
	 * ({@code List<String>} or {@code null})
	 * @since Servlet 3.0
	 */
	public static final String ORDERED_LIBS = "javax.servlet.context.orderedLibs";//$NON-NLS-1$
}
