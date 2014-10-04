/**
 * 
 */
package clime.messadmin.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * @author C&eacute;drik LIME
 */
public interface IApplicationInfo {
	/**
	 * @return Internal ID of this application. This is unique and has not human meaning.
	 */
	String getInternalContextPath();

	/**
	 * @return ClassLoader associated with this ServletContext.
	 * @see ServletContext#getClassLoader()
	 */
	ClassLoader getClassLoader();

	/**
	 * @return number of exceptions generated during request processing
	 */
	int getNErrors();
	/**
	 * @return last error generated during request processing, or <code>null</code> if none
	 */
	ErrorData getLastError();

	/**
	 * @return number of hits for this application
	 */
	int getHits();

	/**
	 * @return maximum of concurrent sessions for this application
	 */
	long getMaxConcurrentSessions();

	/**
	 * @return date of maximum of concurrent sessions for this application
	 */
	Date getMaxConcurrentSessionsDate();

	/**
	 * @return total number of created sessions for this application
	 */
	long getTotalCreatedSessions();

	/**
	 * @return total number of network bytes received by this application
	 */
	long getRequestTotalLength();

	/**
	 * @return maximum number of network bytes received by this application for a request
	 */
	long getRequestMaxLength();

	/**
	 * @return date at which maximum number of network bytes received by this application for a request
	 */
	Date getRequestMaxLengthDate();

	/**
	 * @return mean of number of network bytes received by this application for a request
	 */
	double getRequestMeanLength();

	/**
	 * @return Std Dev of number of network bytes received by this application for a request
	 */
	double getRequestStdDevLength();

	/**
	 * @return total number of network bytes sent by this application
	 */
	long getResponseTotalLength();

	/**
	 * @return maximum number of network bytes sent by this application for a response
	 */
	long getResponseMaxLength();

	/**
	 * @return date at which maximum number of network bytes sent by this application for a response
	 */
	Date getResponseMaxLengthDate();

	/**
	 * @return mean of number of network bytes sent by this application for a response
	 */
	double getResponseMeanLength();

	/**
	 * @return Std Dev of number of network bytes sent by this application for a response
	 */
	double getResponseStdDevLength();

	/**
	 * @return {@link HttpServletResponse#setStatus(int) Response status} statistics
	 */
	ResponseStatusInfo getResponseStatusInfo();

	/**
	 * @return current number of active HttpSessions for this application
	 */
	int getActiveSessionsCount();

	/**
	 * @return current number of passive HttpSessions for this application
	 */
	int getPassiveSessionsCount();

	/**
	 * @return memory size of all active HttpSessions for this application
	 */
	long getActiveSessionsSize();

	/**
	 * @return startup time of this application
	 */
	Date getStartupTime();

	/**
	 * @return total number of milliseconds this application has used to service requests
	 */
	long getUsedTimeTotal();

	/**
	 * @return maximum number of milliseconds this application has used to service a request
	 */
	long getUsedTimeMax();

	/**
	 * @return date at which maximum number of milliseconds this application has used to service a request
	 */
	Date getUsedTimeMaxDate();

	/**
	 * @return mean of number of milliseconds this application has used to service requests
	 */
	double getUsedTimeMean();

	/**
	 * @return SdDev of number of milliseconds this application has used to service requests
	 */
	double getUsedTimeStdDev();

	/**
	 * @return application-specific data (user plugin)
	 */
	List/*<DisplayDataHolder>*/ getApplicationSpecificData();

	// from HttpServletRequest

	/**
	 * Returns the portion of the request URI that indicates the context
	 * of the request.  The context path always comes first in a request
	 * URI.  The path starts with a "/" character but does not end with a "/"
	 * character.  For servlets in the default (root) context, this method
	 * returns "". The container does not decode this string.
	 *
	 * @return		a <code>String</code> specifying the
	 *			portion of the request URI that indicates the context
	 *			of the request
	 *
	 * @see javax.servlet.http.HttpServletRequest#getContextPath()
	 */
	String getContextPath();

	// from ServletContext

	/**
	 * Returns the name and version of the servlet container on which
	 * the servlet is running. 
	 *
	 * <p>The form of the returned string is 
	 * <i>servername</i>/<i>versionnumber</i>.
	 * For example, the JavaServer Web Development Kit may return the string
	 * <code>JavaServer Web Dev Kit/1.0</code>.
	 *
	 * <p>The servlet container may return other optional information 
	 * after the primary string in parentheses, for example,
	 * <code>JavaServer Web Dev Kit/1.0 (JDK 1.1.6; Windows NT 4.0 x86)</code>.
	 *
	 * @return 		a <code>String</code> containing at least the 
	 *			servlet container name and version number
	 *
	 * @see javax.servlet.ServletContext#getServerInfo()
	 */
	String getServerInfo();

	/**
	 * Returns the name of this web application corresponding to this ServletContext as specified in the deployment
	 * descriptor for this web application by the display-name element.
	 *
	 * @return		The name of the web application or null if no name has been declared in the deployment descriptor.
	 * @since Servlet 2.3
	 *
	 * @see javax.servlet.ServletContext#getServletContextName()
	 */
	String getServletContextName();

	/**
	 * Returns a <code>Map&lt;String,String&gt;</code> containing the
	 * context-wide initialization parameters.
	 *
	 * <p>This method can make available configuration information useful
	 * to an entire "web application".  For example, it can provide a 
	 * webmaster's email address or the name of a system that holds 
	 * critical data.
	 *
	 * @return 		a <code>Map&lt;String,String&gt;</code> containing at least the 
	 *			servlet container name and version number
	 *
	 * @see javax.servlet.ServletContext#getInitParameterNames()
	 * @see javax.servlet.ServletContext#getInitParameter(java.lang.String)
	 */
	Map<String, String> getInitParameters();

	/**
	 * Returns a <code>String</code> containing the value of the named
	 * context-wide initialization parameter, or <code>null</code> if the 
	 * parameter does not exist.
	 *
	 * <p>This method can make available configuration information useful
	 * to an entire "web application".  For example, it can provide a 
	 * webmaster's email address or the name of a system that holds 
	 * critical data.
	 *
	 * @param	name	a <code>String</code> containing the name of the
	 *				  parameter whose value is requested
	 * 
	 * @return 		a <code>String</code> containing at least the 
	 *			servlet container name and version number
	 *
	 * @see javax.servlet.ServletContext#getInitParameter(java.lang.String)
	 */
	String getInitParameter(String name);

	/**
	 * Returns the servlet container attributes.
	 * An attribute allows a servlet container to give the
	 * servlet additional information not
	 * already provided by this interface. See your
	 * server documentation for information about its attributes.
	 *
	 * <p>The attribute is returned as a <code>java.lang.Object</code>
	 * or some subclass.
	 * Attribute names should follow the same convention as package
	 * names. The Java Servlet API specification reserves names
	 * matching <code>java.*</code>, <code>javax.*</code>,
	 * and <code>sun.*</code>.
	 *
	 * @return 		an <code>Map&lt;String,Object&gt;</code> containing the
	 *			attributes
	 *
	 * @see javax.servlet.ServletContext#getAttributeNames()
	 * @see javax.servlet.ServletContext#getAttribute(java.lang.String)
	 */
	Map<String, Object> getAttributes();

	/**
	 * Returns the servlet container attribute with the given name, 
	 * or <code>null</code> if there is no attribute by that name.
	 * An attribute allows a servlet container to give the
	 * servlet additional information not
	 * already provided by this interface. See your
	 * server documentation for information about its attributes.
	 * A list of supported attributes can be retrieved using
	 * <code>getAttributeNames</code>.
	 *
	 * <p>The attribute is returned as a <code>java.lang.Object</code>
	 * or some subclass.
	 * Attribute names should follow the same convention as package
	 * names. The Java Servlet API specification reserves names
	 * matching <code>java.*</code>, <code>javax.*</code>,
	 * and <code>sun.*</code>.
	 *
	 *
	 * @param name 	a <code>String</code> specifying the name 
	 *			of the attribute
	 *
	 * @return 		an <code>Object</code> containing the value 
	 *			of the attribute, or <code>null</code>
	 *			if no attribute exists matching the given
	 *			name
	 *
	 * @see 	javax.servlet.ServletContext#getAttribute(java.lang.String)
	 *
	 */
	Object getAttribute(String name);

	/**
	 * Binds an object to a given attribute name in this servlet context. If
	 * the name specified is already used for an attribute, this
	 * method will replace the attribute with the new to the new attribute.
	 * <p>If listeners are configured on the <code>ServletContext</code> the  
	 * container notifies them accordingly.
	 * <p>
	 * If a null value is passed, the effect is the same as calling 
	 * <code>removeAttribute()</code>.
	 * 
	 * <p>Attribute names should follow the same convention as package
	 * names. The Java Servlet API specification reserves names
	 * matching <code>java.*</code>, <code>javax.*</code>, and
	 * <code>sun.*</code>.
	 *
	 *
	 * @param name 	a <code>String</code> specifying the name 
	 *			of the attribute
	 *
	 * @param object 	an <code>Object</code> representing the
	 *			attribute to be bound
	 *
	 * @see javax.servlet.ServletContext#setAttribute(java.lang.String, java.lang.Object)
	 */
	void setAttribute(String name, Object object);

	/**
	 * Removes the attribute with the given name from 
	 * the servlet context. After removal, subsequent calls to
	 * {@link #getAttribute} to retrieve the attribute's value
	 * will return <code>null</code>.

	 * <p>If listeners are configured on the <code>ServletContext</code> the 
	 * container notifies them accordingly.
	 *
	 * @param name	a <code>String</code> specifying the name 
	 * 			of the attribute to be removed
	 *
	 * @see javax.servlet.ServletContext#removeAttribute(java.lang.String)
	 */
	void removeAttribute(String name);

}
