//package javax.servlet.jsp;
package clime.messadmin.model;

import java.io.Serializable;
import java.util.Date;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import clime.messadmin.core.Constants;
import clime.messadmin.utils.SessionUtils;

/**
 * Contains information about an error, for error pages. The information
 * contained in this instance is meaningless if not used in the context of an
 * error page. To indicate a JSP is an error page, the page author must set the
 * isErrorPage attribute of the page directive to "true".
 * 
 * @see javax.servlet.jsp.PageContext#getErrorData
 * @since JSP 2.0
 * @author C&eacute;drik LIME
 */
public final class ErrorData implements Serializable {

	protected Throwable throwable;
//	protected int statusCode;
	protected String uri;
//	protected String servletName;

	private long date;

	/**
	 * Creates a new ErrorData object.
	 * 
	 * @param throwable    The Throwable that is the cause of the error
	 * @param statusCode   The status code of the error
	 * @param uri          The request URI
	 * @param servletName  The name of the servlet invoked
	 */
	public ErrorData(Throwable throwable, int statusCode, String uri, String servletName) {
		this.throwable = throwable;
//		this.statusCode = statusCode;
		this.uri = uri;
//		this.servletName = servletName;
		this.date = System.currentTimeMillis();
	}

	/**
	 * Creates a new ErrorData object.
	 * 
	 * @param request  The ServletRequest to extract information from
	 */
	public ErrorData(ServletRequest request) {
		// Servlet 2.2
//		Integer statusCodeInt = (Integer) request.getAttribute(Constants.ERROR_STATUS_CODE);
//		this.statusCode = (statusCodeInt == null ? 0 : statusCodeInt.intValue());//$NON-NLS-1$
		/*
		 * With the introduction of the exception object to the attributes list for version
		 * 2.3 of this specification, the exception type and error message attributes are
		 * redundant. They are retained for backwards compatibility with earlier versions of
		 * the API.
		 */
		//Class exceptionType = (Class) request.getAttribute(Constants.ERROR_EXCEPTION_TYPE);//$NON-NLS-1$
		/** the exception message, passed to the exception constructor */
		//String message = (String) request.getAttribute(Constants.ERROR_MESSAGE);//$NON-NLS-1$
		// Servlet 2.3
		this.throwable = (Throwable) request.getAttribute(Constants.ERROR_EXCEPTION);//$NON-NLS-1$
		if (this.throwable == null) {
			this.throwable = (Throwable) request.getAttribute(Constants.ERROR_JSP_EXCEPTION);//$NON-NLS-1$
		}
		this.uri = (String) request.getAttribute(Constants.ERROR_REQUEST_URI);//$NON-NLS-1$
//		this.servletName = (String) request.getAttribute(Constants.ERROR_SERVLET_NAME);//$NON-NLS-1$
		this.date = System.currentTimeMillis();
	}

	/**
	 * Creates a new ErrorData object.
	 * 
	 * @param request  The ServletRequest to extract information from
	 * @param t        The Throwable that is the cause of the error
	 */
	public ErrorData(HttpServletRequest request, Throwable t) {
		this(request);
		if (throwable == null) {
			throwable = t;
		}
		if (uri == null) {
			uri = SessionUtils.getRequestURLWithMethodAndQueryString(request);
		}
//		if (statusCode == 0) {
//			statusCode = response.getStatus();
//		}
	}

	/**
	 * Returns the Throwable that caused the error.
	 * 
	 * @return The Throwable that caused the error
	 */
	public Throwable getThrowable() {
		return throwable;
	}

	/**
	 * Returns the status code of the error.
	 * 
	 * @return The status code of the error
	 */
//	public int getStatusCode() {
//		return statusCode;
//	}

	/**
	 * Returns the request URI.
	 * 
	 * @return The request URI
	 */
	public String getRequestURI() {
		return uri;
	}

	/**
	 * Returns the name of the servlet invoked.
	 * 
	 * @return The name of the servlet invoked
	 */
//	public String getServletName() {
//		return servletName;
//	}

	/**
	 * @return Returns the date.
	 */
	public Date getDate() {
		return new Date(date);
	}
}
