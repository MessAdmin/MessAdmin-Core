package clime.messadmin.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import clime.messadmin.core.Constants;
import clime.messadmin.core.RegistrationTracker;
import clime.messadmin.model.ApplicationInfo;
import clime.messadmin.model.Server;
import clime.messadmin.providers.ProviderUtils;
import clime.messadmin.providers.spi.RequestExceptionProvider;
import clime.messadmin.providers.spi.RequestLifeCycleProvider;

/**
 * Servlet Filter which logs request stats and injects a message in the output html stream
 * @author C&eacute;drik LIME
 */
public class MessAdminFilter implements Filter {
	private static boolean DEBUG = false;
	private static final String NULL_SESSION_REQUEST_KEY = "messadmin.requestInitialized.session.null";//$NON-NLS-1$
	public static final String WRAPPED_REQUEST_KEY = "messadmin.request.wrapped";//$NON-NLS-1$
	public static final String WRAPPED_RESPONSE_KEY = "messadmin.response.wrapped";//$NON-NLS-1$

	private FilterConfig config;
	/** Should this Filter call requestInitialized()/requestDestroyed(), or is this already taken care of? */
	protected volatile boolean shouldManageRequestsLifecycle = true;

	private void log(final String message) {
		if (DEBUG) {
			config.getServletContext().log(message);
		}
	}
	private void log(final String message, final Throwable t) {
		if (DEBUG) {
			config.getServletContext().log(message, t);
		}
	}

	/**
	 *
	 */
	public MessAdminFilter() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(final FilterConfig filterConfig) throws ServletException {
		this.config = filterConfig;
		// check we are the only MessAdminListener registered for this ServletContext
		Object clazz = RegistrationTracker.FILTER_INSTANCE.get(filterConfig.getServletContext());
		if (clazz != null) {
			throw new IllegalStateException("You can have only 1 MessAdminFilter registered for a given application. "
					+ "Maybe you both declared it in your web.xml /and/ are using the MessAdmin-AutoProbe plugin "
					+ "along with a Servlet 3.0+ compliant container?\n"
					+ "Registered classes: " + clazz + " | " + this.getClass().getName());
		}
		RegistrationTracker.FILTER_INSTANCE.register(filterConfig.getServletContext(), this.getClass().getName());
		//
		// register that we are in a full functionality mode
		((ApplicationInfo)Server.getInstance().getApplication(filterConfig.getServletContext()).getApplicationInfo()).setMessAdminFullMode(true);
		// This filter should only manage requests if MessAdmin-AutoProbe is not available, or in a Servlet 2.3 container (no plugin)
		boolean isServlet23 = filterConfig.getServletContext().getMajorVersion() == 2
			&& filterConfig.getServletContext().getMinorVersion() == 3;
		try {
			Class.forName("clime.messadmin.core.autoprobe.MessAdminListener", false, Thread.currentThread().getContextClassLoader());
			// We have both the AutoProbe plugin and this filter
			// The AutoProbe plugin will take care of the request lifecycle, except when in a Servlet 2.3 container
			shouldManageRequestsLifecycle = false || isServlet23;
		} catch (ClassNotFoundException expected) {
			// no AutoProbe plugin
			shouldManageRequestsLifecycle = true;
		}
		// The Servlet-3 AutoProbe plugin correctly sets shouldManageRequestsLifecycle, so no need to test here
	}

	/**
	 * {@inheritDoc}
	 */
	public void destroy() {
		RegistrationTracker.FILTER_INSTANCE.unregister(config.getServletContext());
		config = null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void doFilter(final ServletRequest request, final ServletResponse response,
			final FilterChain chain) throws IOException, ServletException {
		if ( ! (request instanceof HttpServletRequest && response instanceof HttpServletResponse)) {
			// not http, don't do anything
			chain.doFilter(request, response);
			return;
		}
//		final long before0 = System.nanoTime();
		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		final HttpSession session = httpRequest.getSession(false);
		final ServletContext servletContext = config.getServletContext();
		final MessAdminRequestWrapper wrappedRequest = new MessAdminRequestWrapper(httpRequest);
		final MessAdminResponseWrapper wrappedResponse = new MessAdminResponseWrapper((HttpServletResponse) response);
		httpRequest.setAttribute(WRAPPED_REQUEST_KEY, wrappedRequest);
		httpRequest.setAttribute(WRAPPED_RESPONSE_KEY, wrappedResponse);

		//assert session == null || session.getServletContext() == config.getServletContext();

		// See if there is a message to inject in this request/response, and if so record it
		boolean messageFromSession = fetchDisplayMessage(session, servletContext, wrappedResponse);

		if (shouldManageRequestsLifecycle) {
			requestInitialized(wrappedRequest, wrappedResponse, servletContext);
		}

		try {
//			final long before1 = System.nanoTime();
			chain.doFilter(wrappedRequest, wrappedResponse);
			try {
				wrappedResponse.flushBuffer();
			} catch (IOException ignore) {
				// response stream was probably closed in application code
			}
//			final long after0 = System.nanoTime();

			try {
				wrappedResponse.finish();
			} catch (IOException ignore) {
			}

			HttpSession sessionAfter = httpRequest.getSession(false);
			// If injected message came from session and was not injected, put it back in session
			if (messageFromSession) {
				restoreDisplayMessage(session, wrappedResponse);
			} else if (sessionAfter != null && wrappedResponse.isMessageInjected()) {
				// message is application-level and has been injected: record timestamp
				try {
					sessionAfter.setAttribute(Constants.GLOBAL_MESSAGE_TIMESTAMP_KEY, new Long(System.currentTimeMillis()));
				} catch (IllegalStateException ise) {
					// session is invalidated: do nothing
				}
			}

			if (shouldManageRequestsLifecycle) {
				requestDestroyed(wrappedRequest, wrappedResponse, servletContext);
			}

//			final long after1 = System.nanoTime();

//			long before = before1 - before0;
//			long after = after1 - after0;
//			System.out.println("Before: " + before/1000000.0 + " ms\tAfter: " + after/1000000.0 + " ms\tTotal: " + (before+after)/1000000.0 + " ms.");
		} catch (IOException ioe) {
			reportException(wrappedRequest, wrappedResponse, ioe);
			throw ioe;
		} catch (ServletException se) {
			reportException(wrappedRequest, wrappedResponse, se);
			throw se;
		} catch (RuntimeException rte) {
			reportException(wrappedRequest, wrappedResponse, rte);
			throw rte;
		} finally {
			if (shouldManageRequestsLifecycle) {
				MessAdminThreadLocal.remove();
			}
		}
	}

	/**
	 * @see RequestLifeCycleProvider#requestInitialized(HttpServletRequest, HttpServletResponse, ServletContext)
	 * Implementation note: this is {@code public static} only for the AutoProbe plugin...
	 * @param httpResponse  WARNING: can be {@code null}!
	 */
	public static void requestInitialized(HttpServletRequest httpRequest, HttpServletResponse httpResponse, ServletContext servletContext) {
		MessAdminThreadLocal.start();
		// Sniff request infos for future usage
		Server.getInstance().requestInitialized(httpRequest, servletContext);
		final HttpSession session = httpRequest.getSession(false);
		if (session == null) {
			httpRequest.setAttribute(NULL_SESSION_REQUEST_KEY, Boolean.TRUE);
		}
		// pre-request plugin
		List requestProviders = ProviderUtils.getProviders(RequestLifeCycleProvider.class);
		Iterator iter = requestProviders.iterator();
		while (iter.hasNext()) {
			RequestLifeCycleProvider lc = (RequestLifeCycleProvider) iter.next();
			try {
				lc.requestInitialized(httpRequest, httpResponse, servletContext);
			} catch (RuntimeException rte) {
			}
		}
	}

	/**
	 * @see RequestLifeCycleProvider#requestDestroyed(HttpServletRequest, HttpServletResponse, ServletContext)
	 * Implementation note: this is {@code public static} only for the AutoProbe plugin...
	 * @param httpResponse  WARNING: can be {@code null}!
	 */
	public static void requestDestroyed(HttpServletRequest httpRequest, HttpServletResponse httpResponse, ServletContext servletContext) {
		// If this Filter is configured, we can get more information with the MessAdmin wrappers
		if (httpRequest.getAttribute(WRAPPED_REQUEST_KEY) != null) {
			httpRequest = (HttpServletRequest) httpRequest.getAttribute(WRAPPED_REQUEST_KEY);
		}
		if (httpResponse == null) {
			httpResponse = (HttpServletResponse) httpRequest.getAttribute(WRAPPED_RESPONSE_KEY);
		}
		HttpSession sessionAfter = httpRequest.getSession(false);
		if (httpRequest.getAttribute(NULL_SESSION_REQUEST_KEY) != null && sessionAfter != null) {
			// Session was created
			Server.getInstance().requestInitialized(httpRequest, servletContext);
		}
		MessAdminThreadLocal.stop();
		Server.getInstance().requestDestroyed(httpRequest, httpResponse, servletContext);
		// post-request plugin
		List requestProviders = new ArrayList(ProviderUtils.getProviders(RequestLifeCycleProvider.class));
		Collections.reverse(requestProviders);
		Iterator iter = requestProviders.iterator();
		while (iter.hasNext()) {
			RequestLifeCycleProvider lc = (RequestLifeCycleProvider) iter.next();
			try {
				lc.requestDestroyed(httpRequest, httpResponse, servletContext);
			} catch (RuntimeException rte) {
			}
		}
	}

	private void reportException(MessAdminRequestWrapper request, MessAdminResponseWrapper response, Exception e) {
		MessAdminThreadLocal.stop();
		// config can be null if the server is shutting down. We need to prevent a potential NPE here.
		if (config != null) {
			Server.getInstance().requestException(e, request, response, config.getServletContext());
			Iterator iter = ProviderUtils.getProviders(RequestExceptionProvider.class).iterator();
			while (iter.hasNext()) {
				RequestExceptionProvider lc = (RequestExceptionProvider) iter.next();
				try {
					lc.requestException(e, request, response, config.getServletContext());
				} catch (RuntimeException rte) {
				}
			}
		}
	}

	/**
	 * Get the session-specific message to display, or the application-specific one if no session-message exist.
	 * @param wrappedResponse
	 * @param session
	 * @return <code>true</code> if message was from user session, <code>false</code>otherwise
	 */
	private boolean fetchDisplayMessage(final HttpSession session, final ServletContext servletContext, final MessAdminResponseWrapper wrappedResponse) {
		String message = null;
		boolean fromSession = false;
		// try session-level message
		if (session != null) {
			try {
				message = (String) session.getAttribute(Constants.SESSION_MESSAGE_KEY);
				if (message != null) {
					// display session messages only once
					log("Removing session message for session id " + session.getId());//$NON-NLS-1$
					session.removeAttribute(Constants.SESSION_MESSAGE_KEY);
					fromSession = true;
				}
			} catch (IllegalStateException ise) {
				// invalidated session: don't do anything
			}
		}
		// try application-level message
		if (message == null) {
			message = (String) servletContext.getAttribute(Constants.GLOBAL_MESSAGE_KEY);
			// only display application-level message if not displayed "too" recently
			if (message != null && session != null) {
				try {
					Long lastTimeStamp = (Long) session.getAttribute(Constants.GLOBAL_MESSAGE_TIMESTAMP_KEY);
					if (lastTimeStamp != null) {
						if (System.currentTimeMillis() - lastTimeStamp.longValue() < Constants.GLOBAL_MESSAGE_DELTA_TIME_MIN) {
							message = null;
						}
					}
				} catch (IllegalStateException ise) {
					// invalidated session: don't do anything
				}
			}
		}
		//TODO try server-level message
		if (message != null) {
			wrappedResponse.setInjectedMessageHTML(message);
		}
		return fromSession;
	}

	private void restoreDisplayMessage(final HttpSession session, final MessAdminResponseWrapper wrappedResponse) {
		String message = wrappedResponse.getInjectedMessageHTML();
		if (session != null && message != null) {
			try {
				String newMessage = (String) session.getAttribute(Constants.SESSION_MESSAGE_KEY);
				if (newMessage == null) {
					// only put back message if there isn't a new one
					log("Putting back session message for session id " + session.getId());//$NON-NLS-1$
					session.setAttribute(Constants.SESSION_MESSAGE_KEY, message);
				}
			} catch (IllegalStateException ise) {
				// invalidated session: don't do anything
			}
		}
	}
}
