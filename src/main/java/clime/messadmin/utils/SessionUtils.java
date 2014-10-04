package clime.messadmin.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import clime.messadmin.model.ISessionInfo;
import clime.messadmin.model.Server;

/**
 * Utility methods on HttpSessions...
 * @author C&eacute;drik LIME
 */
public class SessionUtils {
	private static transient Method ServletContext_getContextPath;
	private static transient Method ServletRequest_getServletContext;
	private static transient Method ServletContext_getClassLoader;
	private static transient Method ServletContext_getVirtualServerName;

	static {
		// @since Servlet 2.5
		try {
			ServletContext_getContextPath = ServletContext.class.getMethod("getContextPath");//$NON-NLS-1$
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		}
		// @since Servlet 3.0
		try {
			ServletRequest_getServletContext = ServletRequest.class.getMethod("getServletContext");//$NON-NLS-1$
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		}
		try {
			ServletContext_getClassLoader = ServletContext.class.getMethod("getClassLoader");//$NON-NLS-1$
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		}
		// @since Servlet 3.1
		try {
			ServletContext_getVirtualServerName = ServletContext.class.getMethod("getVirtualServerName");//$NON-NLS-1$
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		}
	}

	/**
	 *
	 */
	private SessionUtils() {
		super();
	}


	public static int getUsedTimeForSession(HttpSession in_session) {
		try {
			ISessionInfo extraSessionInfo = Server.getInstance().getSession(in_session).getSessionInfo();
			if (null != extraSessionInfo) {
				return extraSessionInfo.getTotalUsedTime();
			} else {
				return -1;
			}
			//long diffMilliSeconds = in_session.getLastAccessedTime() - in_session.getCreationTime();
			//return diffMilliSeconds;
		} catch (IllegalStateException ise) {
			//ignore: invalidated session
			return -1;
		}
	}

	public static int getTTLForSession(HttpSession in_session) {
		try {
			long diffMilliSeconds = (1000*in_session.getMaxInactiveInterval()) - (System.currentTimeMillis() - in_session.getLastAccessedTime());
			return (int) diffMilliSeconds;
		} catch (IllegalStateException ise) {
			//ignore: invalidated session
			return -1;
		}
	}

	public static int getIdleTimeForSession(HttpSession in_session) {
		try {
			long diffMilliSeconds =  System.currentTimeMillis() - in_session.getLastAccessedTime();
			return (int) diffMilliSeconds;
		} catch (IllegalStateException ise) {
			//ignore: invalidated session
			return -1;
		}
	}

	//TODO: move this method in a utility package

	/**
	 * Returns the context path of the web application. The context path is
	 * the portion of the request URI that is used to select the context of
	 * the request. The context path always comes first in a request URI. The
	 * path starts with a "/" character but does not end with a "/" character.
	 * For servlets in the default (root) context, this method returns "".
	 *
	 * It is possible that a servlet container may match a context by more than
	 * one context path. In such cases getContextPath() will return the actual
	 * context path used by the request and it may differ from the path returned
	 * by this method. The context path returned by this method should be considered
	 * as the prime or preferred context path of the application.
	 *
	 * @return The context path of the web application.
	 */
	/**
	 * Returns the portion of the request URI that indicates the context
	 * of the request.  The context path always comes first in a request
	 * URI.  The path starts with a "/" character but does not end with a "/"
	 * character.  For servlets in the default (root) context, this method
	 * returns "". The container does not decode this string.
	 *
	 * It is possible that a servlet container may match a context by more
	 * than one context path. In such cases this method will return the actual
	 * context path used by the request and it may differ from the path returned
	 * by the ServletContext.getContextPath() method. The context path returned
	 * by ServletContext.getContextPath() should be considered as the prime or
	 * preferred context path of the application.
	 *
	 * @param session
	 * @return	a <code>String</code> specifying the portion of
	 *			the request URI that indicates the context of the request
	 */
	public static String getContext(HttpSession session) {
		return getContext(session.getServletContext());
	}
	public static String getContext(ServletContext context) {
		String result = null;
		// 1. Try "/" resource path
		{
			try {
				URL resource = context.getResource("/");//$NON-NLS-1$
				if (resource != null) {
					result = resource.getPath();
				}
			} catch (MalformedURLException shouldNeverHappen) {
				throw new RuntimeException(shouldNeverHappen);
			}
		}
		// 2. Try another non-directory resource path
		if (result == null) {
			// There is no resource named "/". Try with the 1st of all available resources, if any.
			String nonDirResourcePath = null;
			// Try well-known location (mandatory for Servlet specs 2.x; can be inexistant for 3.x)
			if (context.getResourcePaths("/WEB-INF/web.xml") != null) {//$NON-NLS-1$
				nonDirResourcePath = "/WEB-INF/web.xml";//$NON-NLS-1$
			} else {
				Set resourcePaths = context.getResourcePaths("/");//$NON-NLS-1$
				if (resourcePaths != null && !resourcePaths.isEmpty()) {
					Iterator iter = resourcePaths.iterator();
					// Since "/" does not exist, bets are that any directory will not exist either.
					// Trying to find a non-directory resource
					while (nonDirResourcePath == null && iter.hasNext()) {
						String nextResourcePath = (String) iter.next();
						if (! nextResourcePath.endsWith("/")) {
							nonDirResourcePath = nextResourcePath;
						}
					}
					//TODO iterate inside directories to find a non-directory resource!
				}
			}
			if (nonDirResourcePath != null) {
				String rootPath;
				try {
					rootPath = context.getResource(nonDirResourcePath).getPath();
					// trim the extra baggage
					if (rootPath.endsWith(nonDirResourcePath)) {
						rootPath = rootPath.substring(0, rootPath.length()-nonDirResourcePath.length()+1);//+1: trailing '/'
					}
					result = rootPath;
				} catch (MalformedURLException e) {
					throw new RuntimeException(e);
				}
			}
		}
		// 3. Try new 2.5 ServletContext.getContextPath()
		if (result == null) {
			if (ServletContext_getContextPath != null) {
				try {
					result = (String) ServletContext_getContextPath.invoke(context);
				} catch (IllegalArgumentException e) {
					throw e;
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				} catch (InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			}
		}
		// 4. Abort :-/
		if (result == null) {
			throw new UnsupportedOperationException("Can not compute root context for web application '" + context.getServletContextName()+'\'');
		}
		return result;
	}

	/**
	 * Reconstructs the URL the client used to make the request,
	 * using information in the <code>HttpServletRequest</code> object.
	 * The returned URL contains a protocol, server name, port
	 * number, and server path, and include query
	 * string parameters.
	 *
	 * <p>This method is useful for creating redirect messages
	 * and for reporting errors.
	 *
	 * @param req	a <code>HttpServletRequest</code> object
	 *			containing the client's request
	 *
	 * @return		a <code>String</code> object containing
	 *			the reconstructed URL
	 */
	public static String getRequestURLWithMethodAndQueryString(HttpServletRequest req) {
		/*
		StringBuilder url = new StringBuilder(32);
		String scheme = req.getScheme();
		int port = req.getServerPort();
		if (port < 0) {
			port = 80; // Work around java.net.URL bug
		}
		//String 	pathInfo = req.getPathInfo();
		String queryString = req.getQueryString();

		url.append(scheme);	// http, https
		url.append("://"); //$NON-NLS-1$
		url.append(req.getServerName());
		if ((scheme.equals ("http") && port != 80) //$NON-NLS-1$
			|| (scheme.equals ("https") && port != 443)) { //$NON-NLS-1$
			url.append (':'); //$NON-NLS-1$
			url.append (port);
		}
		//url.append(req.getContextPath());
		//url.append (req.getServletPath());
		//if (pathInfo != null)
		//	url.append (pathInfo);
		url.append(req.getRequestURI());
		if (queryString != null) {
			url.append('?').append(queryString); //$NON-NLS-1$
		}
		return url.toString();
		*/
		String method = req.getMethod();
		StringBuffer requestURL = req.getRequestURL();
		String queryString = req.getQueryString();
		int totalLength = method.length() + 1 + requestURL.length() + (queryString==null?0:1+queryString.length());
		StringBuilder buffer = new StringBuilder(totalLength);
		buffer.append(method).append(' ').append(requestURL);
		if (queryString != null) {
			buffer.append('?').append(queryString);
		}
		return buffer.toString();
	}

	/**
	 * Gets the servlet context to which this ServletRequest was last dispatched.
	 * @return the servlet context to which this ServletRequest was last dispatched
	 * @since 3.0
	 */
	public static ServletContext getServletContext(HttpServletRequest request) {
		if (ServletRequest_getServletContext != null) {
			try {
				return (ServletContext) ServletRequest_getServletContext.invoke(request);
			} catch (IllegalArgumentException e) {
				throw e;
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
		HttpSession httpSession = request.getSession(false);
		if (httpSession != null) {
			return httpSession.getServletContext();
		}
		return null;
	}

	/**
	 * Gets the class loader of the web application represented by this ServletContext.
	 * <p>If a security manager exists, and the caller's class loader is not the same as, or an ancestor of the requested class loader, then the security manager's
	 * {@code checkPermission} method is called with a {@code RuntimePermission("getClassLoader")} permission to check whether access to the requested class loader should be granted.
	 * @return the class loader of the web application represented by this ServletContext
	 * @since 3.0
	 */
//	* @throws UnsupportedOperationException - if this ServletContext was passed to the ServletContextListener.contextInitialized(javax.servlet.ServletContextEvent) method of a ServletContextListener that was neither declared in web.xml or web-fragment.xml, nor annotated with WebListener
//	* @throws SecurityException - if a security manager denies access to the requested class loader
	public static ClassLoader getClassLoader(ServletContext servletContext) {
		if (ServletContext_getClassLoader != null) {
			try {
				return (ClassLoader) ServletContext_getClassLoader.invoke(servletContext);
			} catch (IllegalArgumentException e) {
				throw e;
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				if ( ! (e.getCause() instanceof UnsupportedOperationException)) {
					throw new RuntimeException(e);
				} // else: ignore
			}
		}
		return null;
	}
	
	/**
	 * Returns the configuration name of the logical host on which the ServletContext is deployed. Servlet containers may support multiple logical hosts. This method must return the same name for all the servlet contexts deployed on a logical host, and the name returned by this method must be distinct, stable per logical host, and suitable for use in associating server configuration information with the logical host. The returned value is NOT expected or required to be equivalent to a network address or hostname of the logical host.
	 * @return a {@code String} containing the configuration name of the logical host on which the servlet context is deployed.
	 * @since 3.1
	 */
//	* @throws UnsupportedOperationException - if this ServletContext was passed to the ServletContextListener.contextInitialized(javax.servlet.ServletContextEvent) method of a ServletContextListener that was neither declared in web.xml or web-fragment.xml, nor annotated with WebListener
	public static String getVirtualServerName(ServletContext servletContext) {
		if (ServletContext_getVirtualServerName != null) {
			try {
				return (String) ServletContext_getVirtualServerName.invoke(servletContext);
			} catch (IllegalArgumentException e) {
				throw e;
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				if ( ! (e.getCause() instanceof UnsupportedOperationException)) {
					throw new RuntimeException(e);
				} // else: ignore
			}
		}
		return null;
	}
}
