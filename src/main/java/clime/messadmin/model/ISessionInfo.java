/**
 *
 */
package clime.messadmin.model;

import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpSession;

/**
 * @author C&eacute;drik LIME
 */
public interface ISessionInfo extends HttpSession {

	// Inherited methods from HttpSession

	//long getCreationTime();
	//long getLastAccessedTime();
	//int getMaxInactiveInterval();
	//void invalidate();
	//boolean isNew();
	//void removeAttribute(String name);
	//void setMaxInactiveInterval(int interval);

	// Methods from ISessionInfo

	/**
	 * Returns the session attributes as a Map.
	 *
	 * @return 		an <code>Map&lt;String,Object&gt;</code> containing the
	 *			attributes
	 *
	 * @exception IllegalStateException	if this method is called on an
	 *					invalidated session
	 *
	 * @see javax.servlet.http.HttpSession#getAttributeNames()
	 * @see javax.servlet.http.HttpSession#getAttribute(java.lang.String)
	 */
	Map/*<String,Object>*/ getAttributes();

	/**
	 * @return number of exceptions generated during request processing
	 */
	int getNErrors();
	/**
	 * @return last error generated during request processing, or <code>null</code> if none
	 */
	ErrorData getLastError();

	String getLastRequestURL();

	/**
	 * Also prepends the "X-Forwarded-For" HTTP header content, if any
	 * @see  javax.servlet.http.HttpServletRequest#getRemoteAddr()
	 */
	String getRemoteAddr();

	/**
	 * Also prepends the "X-Forwarded-For" HTTP header content, if any
	 * @see  javax.servlet.http.HttpServletRequest#getRemoteHost()
	 */
	String getRemoteHost();

	/** @see  javax.servlet.http.HttpServletRequest#getUserPrincipal() */
	Principal getUserPrincipal();

	/** @see  javax.servlet.http.HttpServletRequest#getRemoteUser() */
	String getRemoteUser();

	int getLastResponseStatus();

	int getHits();

	long getRequestLastLength();

	long getResponseLastLength();

	long getRequestMinLength();

	long getResponseMinLength();

	Date getRequestMinLengthDate();

	Date getResponseMinLengthDate();

	long getRequestMaxLength();

	long getResponseMaxLength();

	Date getRequestMaxLengthDate();

	Date getResponseMaxLengthDate();

	long getRequestTotalLength();

	long getResponseTotalLength();

	double getRequestMeanLength();

	double getResponseMeanLength();

	double getRequestStdDevLength();

	double getResponseStdDevLength();

	Date getLastRequestDate();

	Date getLastResponseDate();

	boolean isSecure();

	boolean isSerializable();

	long getSize();

	String getUserAgent();

	/** @see javax.servlet.http.HttpServletRequest#getAuthType() */
	String getAuthType();

	String getReferer();

	int getLastUsedTime();

	int getMinUsedTime();

	Date getMinUsedTimeDate();

	int getMaxUsedTime();

	Date getMaxUsedTimeDate();

	int getTotalUsedTime();

	double getMeanUsedTime();

	double getStdDevUsedTime();

	int getIdleTime();
	int getTTL();
	int getAge();
	Object getGuessedUser();
	Locale getGuessedLocale();

	/**
	 * cipher suite
	 * @see #isSecure()
	 */
	String getSslCipherSuite();
	/**
	 * bit size of the algorithm
	 * @see #isSecure()
	 */
	Integer getSslAlgorithmSize();
	// TODO see if this is not too expensive (memory) to keep
//	/**
//	 * The order of this array is defined as being in ascending order of trust. The first
//	 * certificate in the chain is the one set by the client, the next is the one used to
//	 * authenticate the first, and so on.
//	 * @see #isSecure()
//	 */
//	X509Certificate[] getSslCertificates();

	/**
	 * @return session-specific data (user plugin)
	 */
	List/*<DisplayDataHolder>*/ getSessionSpecificData();
}
