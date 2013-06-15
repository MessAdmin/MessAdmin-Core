package clime.messadmin.model;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import clime.messadmin.providers.ProviderUtils;
import clime.messadmin.providers.spi.LocaleProvider;
import clime.messadmin.providers.spi.SerializableProvider;
import clime.messadmin.providers.spi.SessionDataProvider;
import clime.messadmin.providers.spi.SizeOfProvider;
import clime.messadmin.providers.spi.UserNameProvider;

/**
 * Stores/computes extra informations related to a session, such as the ones from requests
 * Takes care of HttpSession-related statistics.
 * @author C&eacute;drik LIME
 */
public class SessionInfo implements ISessionInfo {
	private HttpSession httpSession;
	protected String id;
	private ClassLoader classLoader;

	protected final RequestInfo cumulativeRequestStats = new RequestInfo(null);

	// cache from ServletRequest and HttpServletRequest
	protected Principal userPrincipal;
	protected String remoteUser;
	protected String remoteAddr;
	protected String remoteHost;
	protected String lastRequestURL;
	protected boolean isSecure = false;
	protected String userAgent;
	protected String authType;
	protected String referer;
	// If a request has been transmitted over a secure protocol, such as HTTPS, this
	// information must be exposed via the isSecure method of the ServletRequest
	// interface. The web container must expose the following attributes to the servlet
	// programmer:
	protected String sslCipherSuite;
	protected Integer sslAlgorithmSize;
	// The order of this array is defined as being in ascending order of trust. The first
	// certificate in the chain is the one set by the client, the next is the one used to
	// authenticate the first, and so on.
	//protected X509Certificate[] sslCertificates;


	/**
	 *
	 */
	public SessionInfo() {
		super();
//		for (int i = 0; i < responseStatus.length; ++i) {
//			responseStatus[i] = new HitsCounter();
//		}
	}
	/**
	 *
	 */
	public SessionInfo(HttpSession session) {
		this();
		setHttpSession(session);
	}
	/**
	 *
	 */
	public SessionInfo(HttpSession session, ClassLoader cl) {
		this(session);
		classLoader = cl;
	}

	public HttpSession getHttpSession() {
		return httpSession;
	}
	protected void setHttpSession(HttpSession session) {
		httpSession = session;
		if (session != null) {
			id = session.getId();
		}
	}

	ClassLoader getClassLoader() {
		return classLoader;
	}
	void setClassLoader(ClassLoader cl) {
		classLoader = cl;
	}

	/** {@inheritDoc} */
	public Map getAttributes() {
		Map result = new HashMap();
		Enumeration enumeration = getAttributeNames();
		while (enumeration.hasMoreElements()) {
			String name = (String) enumeration.nextElement();
			Object value = getAttribute(name);
			result.put(name, value);
		}
		return result;
	}

	/** {@inheritDoc} */
	public boolean isSerializable() {
		try {
			/*
			Enumeration enumeration = getAttributeNames();
			IdentityHashMap visitedObjects = new IdentityHashMap();
			while (enumeration.hasMoreElements()) {
				String attributeName = (String) enumeration.nextElement();
				Object attributeValue = getAttribute(attributeName);
				if (! SerializableUtils.isMaybeSerializable(attributeValue, visitedObjects)) {
					return false;
				}
			}
			*/
			Enumeration enumeration = getAttributeNames();
			while (enumeration.hasMoreElements()) {
				String attributeName = (String) enumeration.nextElement();
				Object attributeValue = getAttribute(attributeName);
				if (! SerializableProvider.Util.isSerializable(attributeValue, classLoader)) {
					return false;
				}
			}
			return true;
		} catch (RuntimeException rte) {
			return false;
		}
	}

	/** {@inheritDoc} */
	public long getSize() {
		Object objectToSize = null;
		try {
			// when sizing an HttpSession, we are really only interested in its attributes!
			if (httpSession != null) {
				Map attributes = new HashMap();
				Enumeration enumeration = httpSession.getAttributeNames();
				while (enumeration.hasMoreElements()) {
					String name = (String) enumeration.nextElement();
					Object attribute = httpSession.getAttribute(name);
					attributes.put(name, attribute);
				}
				objectToSize = attributes;
			}
            return SizeOfProvider.Util.getObjectSize(objectToSize, classLoader);
		} catch (IllegalStateException ise) {
			// Session is invalidated: do nothing
			return -1;
		}
	}

	/** {@inheritDoc} */
	public int getNErrors() {
		return cumulativeRequestStats.getNErrors();
	}
	/** {@inheritDoc} */
	public ErrorData getLastError() {
		return cumulativeRequestStats.getLastError();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLastRequestURL() {
		return lastRequestURL;
	}

	/**{@inheritDoc} */
	public String getRemoteAddr() {
		return remoteAddr;
	}

	/**{@inheritDoc} */
	public String getRemoteHost() {
		return remoteHost;
	}

	/**{@inheritDoc} */
	public Principal getUserPrincipal() {
		return userPrincipal;
	}

	/**{@inheritDoc} */
	public String getRemoteUser() {
		return remoteUser;
	}
	public void setRemoteUser(String remoteUser) {
		this.remoteUser = remoteUser;
	}

	/**{@inheritDoc} */
	public int getHits() {
		return cumulativeRequestStats.getHits();// hits;
	}

	/**{@inheritDoc} */
	public long getRequestLastLength() {
		return cumulativeRequestStats.getRequestLastLength();
	}

	/**{@inheritDoc} */
	public long getResponseLastLength() {
		return cumulativeRequestStats.getResponseLastLength();
	}

	/**{@inheritDoc} */
	public long getRequestMinLength() {
		return cumulativeRequestStats.getRequestMinLength();
	}

	/**{@inheritDoc} */
	public long getResponseMinLength() {
		return cumulativeRequestStats.getResponseMinLength();
	}

	/**{@inheritDoc} */
	public Date getRequestMinLengthDate() {
		return cumulativeRequestStats.getRequestMinLengthDate();
	}

	/**{@inheritDoc} */
	public Date getResponseMinLengthDate() {
		return cumulativeRequestStats.getResponseMinLengthDate();
	}

	/**{@inheritDoc} */
	public long getRequestMaxLength() {
		return cumulativeRequestStats.getRequestMaxLength();
	}

	/**{@inheritDoc} */
	public long getResponseMaxLength() {
		return cumulativeRequestStats.getResponseMaxLength();
	}

	/**{@inheritDoc} */
	public Date getRequestMaxLengthDate() {
		return cumulativeRequestStats.getRequestMaxLengthDate();
	}

	/**{@inheritDoc} */
	public Date getResponseMaxLengthDate() {
		return cumulativeRequestStats.getResponseMaxLengthDate();
	}

	/**{@inheritDoc} */
	public long getRequestTotalLength() {
		return cumulativeRequestStats.getRequestTotalLength();
	}

	/**{@inheritDoc} */
	public long getResponseTotalLength() {
		return cumulativeRequestStats.getResponseTotalLength();
	}

	/**{@inheritDoc} */
	public double getRequestMeanLength() {
		return cumulativeRequestStats.getRequestMeanLength();
	}

	/**{@inheritDoc} */
	public double getResponseMeanLength() {
		return cumulativeRequestStats.getResponseMeanLength();
	}

	/**{@inheritDoc} */
	public double getRequestStdDevLength() {
		return cumulativeRequestStats.getRequestStdDevLength();
	}

	/**{@inheritDoc} */
	public double getResponseStdDevLength() {
		return cumulativeRequestStats.getResponseStdDevLength();
	}

	/**{@inheritDoc} */
	public Date getLastRequestDate() {
		return cumulativeRequestStats.getLastRequestDate(); //requestLength.getLastAccessTime();
	}

	/**{@inheritDoc} */
	public Date getLastResponseDate() {
		return cumulativeRequestStats.getLastResponseDate(); //responseLength.getLastAccessTime();
	}

	/**{@inheritDoc} */
	public boolean isSecure() {
		return isSecure;
	}

	/**{@inheritDoc} */
	public String getUserAgent() {
		return userAgent;
	}

	/**{@inheritDoc} */
	public String getAuthType() {
		return authType;
	}

	/**{@inheritDoc} */
	public String getReferer() {
		return referer;
	}

	/**{@inheritDoc} */
	public int getLastResponseStatus() {
		return cumulativeRequestStats.getLastResponseStatus();
	}

	/**{@inheritDoc} */
	public int getLastUsedTime() {
		return (int) cumulativeRequestStats.getLastUsedTime();
	}

	/**{@inheritDoc} */
	public int getMinUsedTime() {
		return (int) cumulativeRequestStats.getMinUsedTime();
	}

	/**{@inheritDoc} */
	public Date getMinUsedTimeDate() {
		return cumulativeRequestStats.getMinUsedTimeDate();
	}

	/**{@inheritDoc} */
	public int getMaxUsedTime() {
		return (int) cumulativeRequestStats.getMaxUsedTime();
	}

	/**{@inheritDoc} */
	public Date getMaxUsedTimeDate() {
		return cumulativeRequestStats.getMaxUsedTimeDate();
	}

	/**{@inheritDoc} */
	public int getTotalUsedTime() {
		return (int) cumulativeRequestStats.getTotalUsedTime();
	}

	/**{@inheritDoc} */
	public double getMeanUsedTime() {
		return cumulativeRequestStats.getMeanUsedTime();
	}

	/**{@inheritDoc} */
	public double getStdDevUsedTime() {
		return cumulativeRequestStats.getStdDevUsedTime();
	}

	/**{@inheritDoc} */
	public Locale getGuessedLocale() {
		return LocaleProvider.Util.guessLocaleFromSession(httpSession, classLoader);
	}

	/**{@inheritDoc} */
	public Object getGuessedUser() {
		return UserNameProvider.Util.guessUserFromSession(httpSession, classLoader);
	}

	/**{@inheritDoc} */
	public int getIdleTime() {
		try {
			long diffMilliSeconds =  System.currentTimeMillis() - getLastAccessedTime();
			return (int) diffMilliSeconds;
		} catch (IllegalStateException ise) {
			//ignore: invalidated session
			return -1;
		}
	}

	/**{@inheritDoc} */
	public int getTTL() {
		try {
			long diffMilliSeconds = (1000*getMaxInactiveInterval()) - (System.currentTimeMillis() - getLastAccessedTime());
			return (int) diffMilliSeconds;
		} catch (IllegalStateException ise) {
			//ignore: invalidated session
			return -1;
		}
	}

	/**{@inheritDoc} */
	public int getAge() {
		try {
			long diffMilliSeconds = getLastAccessedTime() - getCreationTime();
			return (int) diffMilliSeconds;
		} catch (IllegalStateException ise) {
			//ignore: invalidated session
			return -1;
		}
	}

	/**{@inheritDoc} */
	public List getSessionSpecificData() {
		List result = new ArrayList();
		for (SessionDataProvider sd : ProviderUtils.getProviders(SessionDataProvider.class, classLoader)) {
			result.add(new DisplayDataHolder.SessionDataHolder(sd, httpSession));
		}
		return result;
	}

	/**{@inheritDoc} */
	public String getSslCipherSuite() {
		return sslCipherSuite;
	}

	/**{@inheritDoc} */
	public Integer getSslAlgorithmSize() {
		return sslAlgorithmSize;
	}

//	/**{@inheritDoc} */
//	public X509Certificate[] getSslCertificates() {
//		return sslCertificates;
//	}

	/*********************************************************************/
	/***	HttpSession wrapped methods; no not call when passivated!	**/
	/*********************************************************************/

	/**
	 * A enumerator class for empty session attributes, specializes
	 * the general Enumerator
	 */
	private static class EmptyEnumerator implements Enumeration {
		static final Enumeration INSTANCE = new EmptyEnumerator();
		private EmptyEnumerator() {
		}
		/** {@inheritDoc} */
		public boolean hasMoreElements() {
			return false;
		}
		/** {@inheritDoc} */
		public Object nextElement() {
			throw new NoSuchElementException("SessionAttribute Enumerator");//$NON-NLS-1$
		}
	}

	/**{@inheritDoc} */
	public Object getAttribute(String name) {
		try {
			return (httpSession==null) ? null : httpSession.getAttribute(name);
		} catch (IllegalStateException ise) {
			//ignore: invalidated session
			return null;
		}
	}

	/**{@inheritDoc} */
	public Enumeration getAttributeNames() {
		try {
			return (httpSession==null) ? EmptyEnumerator.INSTANCE : httpSession.getAttributeNames();
		} catch (IllegalStateException ise) {
			//ignore: invalidated session
			return EmptyEnumerator.INSTANCE;
		}
	}

	/**{@inheritDoc} */
	public long getCreationTime() {
		try {
			return (httpSession==null) ? -1 : httpSession.getCreationTime();
		} catch (IllegalStateException ise) {
			//ignore: invalidated session
			return -1;
		}
	}

	/**{@inheritDoc} */
	public String getId() {
		return id;//httpSession.getId();
	}

	/**{@inheritDoc} */
	public long getLastAccessedTime() {
		try {
			return (httpSession==null) ? -1 : httpSession.getLastAccessedTime();
		} catch (IllegalStateException ise) {
			//ignore: invalidated session
			return -1;
		}
	}

	/**{@inheritDoc} */
	public int getMaxInactiveInterval() {
		try {
			return (httpSession==null) ? -1 : httpSession.getMaxInactiveInterval();
		} catch (IllegalStateException ise) {
			//ignore: invalidated session
			return 0;
		}
	}

	/**{@inheritDoc} */
	public ServletContext getServletContext() {
		return httpSession.getServletContext();
	}

	/**{@inheritDoc} */
	public void invalidate() {
		httpSession.invalidate();
	}

	/**{@inheritDoc} */
	public boolean isNew() {
		try {
			return (httpSession==null) ? false : httpSession.isNew();
		} catch (IllegalStateException ise) {
			//ignore: invalidated session
			return false;
		}
	}

	/**{@inheritDoc} */
	public void removeAttribute(String name) {
		try {
			if (httpSession != null) {
				httpSession.removeAttribute(name);
			}
		} catch (IllegalStateException ise) {
			//ignore: invalidated session
		}
	}

	/**{@inheritDoc} */
	public void setAttribute(String name, Object value) {
		try {
			if (httpSession != null) {
				httpSession.setAttribute(name, value);
			}
		} catch (IllegalStateException ise) {
			//ignore: invalidated session
		}
	}

	/**{@inheritDoc} */
	public void setMaxInactiveInterval(int interval) {
		try {
			if (httpSession != null) {
				httpSession.setMaxInactiveInterval(interval);
			}
		} catch (IllegalStateException ise) {
			//ignore: invalidated session
		}
	}
	/**
	 * {@inheritDoc}
	 * @deprecated no replacement
	 */
	@Deprecated
	public HttpSessionContext getSessionContext() {
		return httpSession.getSessionContext();
	}
	/**
	 * {@inheritDoc}
	 * @deprecated replaced by {@link #getAttribute}
	 */
	@Deprecated
	public Object getValue(String name) {
		return httpSession.getValue(name);
	}
	/**
	 * {@inheritDoc}
	 * @deprecated replaced by {@link #getAttributeNames}
	 */
	@Deprecated
	public String[] getValueNames() {
		return httpSession.getValueNames();
	}
	/**
	 * {@inheritDoc}
	 * @deprecated replaced by {@link #setAttribute}
	 */
	@Deprecated
	public void putValue(String name, Object value) {
		httpSession.putValue(name, value);
	}
	/**
	 * {@inheritDoc}
	 * @deprecated replaced by {@link #removeAttribute}
	 */
	@Deprecated
	public void removeValue(String name) {
		httpSession.removeValue(name);
	}
}
