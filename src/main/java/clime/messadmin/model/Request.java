/**
 *
 */
package clime.messadmin.model;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import clime.messadmin.filter.MessAdminRequestWrapper;
import clime.messadmin.filter.MessAdminResponseWrapper;
import clime.messadmin.filter.MessAdminThreadLocal;

/**
 * @author C&eacute;drik LIME
 */
public class Request {

	/**
	 *
	 */
	public Request(String url) {
		super();
	}

	private void hit(final RequestInfo requestInfo, int duration) {
		//++requestInfo.hits;
		requestInfo.usedTime.registerValue(duration);
	}

	private void addRequestLength(final RequestInfo requestInfo, final HttpServletRequest request) {
		if (request instanceof MessAdminRequestWrapper) {
			requestInfo.requestLength.registerValue(((MessAdminRequestWrapper)request).getRequestLength());
		} else {
			long requestSize = MessAdminRequestWrapper.getHeadersSize(request);
			if (-1 != request.getContentLength()) {
				requestSize += request.getContentLength();
			}
			requestInfo.requestLength.registerValue(requestSize);
		}
	}

	private void addResponseLength(final RequestInfo requestInfo, final HttpServletResponse response) {
		if (response == null || !(response instanceof MessAdminResponseWrapper)) {
			return;
		}
//		if (-1 != response.getContentLength()) {
//			requestInfo.responseLength.registerValue(response.getContentLength());
//		} else if (-1 != response.getResponseBodyLength()) {
//			requestInfo.responseLength.registerValue(response.getResponseBodyLength());
//		} else {
//			requestInfo.responseLength.hit();
//		}
		requestInfo.responseLength.registerValue(((MessAdminResponseWrapper)response).getResponseLength());
	}

	/*****************************************/
	/**	Request/Response Listener methods	**/
	/*****************************************/

	/**
	 * @see IRequestListener#requestInitialized(HttpServletRequest, ServletContext)
	 */
	public void requestInitialized(final RequestInfo requestInfo, HttpServletRequest request,
			ServletContext servletContext) {
		requestInfo.lastRequestDate = MessAdminThreadLocal.getStartTime().getTime();
	}

	/**
	 * @see IRequestListener#requestDestroyed(HttpServletRequest, HttpServletResponse, ServletContext)
	 */
	public void requestDestroyed(final RequestInfo requestInfo, HttpServletRequest request,
			HttpServletResponse response, ServletContext servletContext) {
		requestInfo.lastResponseDate = MessAdminThreadLocal.getStopTime().getTime();
		int status = MessAdminResponseWrapper.getStatus(response);
		requestInfo.lastResponseStatus = status;
		requestInfo.responseStatus.register(status);
		addRequestLength(requestInfo, request);
		addResponseLength(requestInfo, response);
		int lastUsedTime = MessAdminThreadLocal.getUsedTime();
		hit(requestInfo, lastUsedTime);
	}

	/**
	 * @see IRequestListener#requestException(Exception, HttpServletRequest, HttpServletResponse, ServletContext)
	 */
	public void requestException(final RequestInfo requestInfo, Exception e, HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
		requestDestroyed(requestInfo, request, response, servletContext);
		++requestInfo.nErrors;
		requestInfo.lastError = new ErrorData(request, e);
	}
}
