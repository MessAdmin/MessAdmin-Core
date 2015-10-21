package clime.messadmin.model;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import clime.messadmin.core.Constants;
import clime.messadmin.filter.MessAdminThreadLocal;
import clime.messadmin.providers.spi.UserNameProvider;
import clime.messadmin.utils.Base64;
import clime.messadmin.utils.Charsets;
import clime.messadmin.utils.SessionUtils;
import clime.messadmin.utils.StringUtils;

/**
 * Important: this class should be instantiated on the current request Thread!
 *
 * @author C&eacute;drik LIME
 */
public class HttpServletRequestInfo implements Serializable {
	/**
	 * String identifier for Basic authentication, with space. Value "BASIC ".
	 */
	private static final String BASIC_AUTH = HttpServletRequest.BASIC_AUTH.toUpperCase() + ' ';

	public static final Comparator<HttpServletRequestInfo> START_DATE_COMPARATOR = new Comparator<HttpServletRequestInfo>() {
		/** {@inheritDoc} */
		public int compare(HttpServletRequestInfo r1, HttpServletRequestInfo r2) {
			return (int) (r2.requestDateNano - r1.requestDateNano);
		}
	};

	private static final String X_FORWARDED_FOR = "X-Forwarded-For";//$NON-NLS-1$

	protected long requestDate = -1;// java.util.Date;
	protected long requestDateNano = -1;
	protected boolean hasHttpSession;

	protected Principal userPrincipal;
	protected String remoteUser;
	protected String remoteAddr;
	protected String remoteHost;
	protected String url;
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
	public HttpServletRequestInfo(HttpServletRequest request) {
		super();
		this.requestDate     = MessAdminThreadLocal.getStartTime().getTime();
		this.requestDateNano = MessAdminThreadLocal.getStartTimeNano();
		{
			HttpSession session = request.getSession(false);
			this.hasHttpSession = (session != null);
			if (hasHttpSession) {
				assert session != null; // Eclipse is stupid...
				try {
					session.getAttributeNames(); // this throws an IllegalStateException for an old session
				} catch (IllegalStateException ignore) {
					this.hasHttpSession = false;
				}
			}
		}
		this.authType      = request.getAuthType();
		this.remoteAddr    = getXForwardedFor(request, request.getRemoteAddr());
		this.remoteHost    = getXForwardedFor(request, request.getRemoteHost());
		this.url           = SessionUtils.getRequestURLWithMethodAndQueryString(request);
		this.userPrincipal = request.getUserPrincipal();
		this.remoteUser    = request.getRemoteUser();
		this.isSecure      = request.isSecure();
		this.userAgent     = request.getHeader("user-agent");//$NON-NLS-1$
		this.referer       = request.getHeader("referer");//$NON-NLS-1$
		this.sslCipherSuite   = (String)request.getAttribute(Constants.SSL_CIPHER_SUITE);//$NON-NLS-1$
		this.sslAlgorithmSize = (Integer)request.getAttribute(Constants.SSL_KEY_SIZE);//$NON-NLS-1$
//		this.sslCertificates  = (X509Certificate[])request.getAttribute(Constants.SSL_CERTIFICATE);//$NON-NLS-1$

		// get user name from BASIC Authorization, if any
		if (this.remoteUser == null /*&& HttpServletRequest.BASIC_AUTH.equals(this.authType)*/ && request.getHeader("Authorization") != null) {
			String authorization = request.getHeader("Authorization");//$NON-NLS-1$
			if (authorization.toUpperCase().startsWith(BASIC_AUTH)) {
				try {
					String base64UserPass = authorization.substring(BASIC_AUTH.length()).trim();
					String userPass = new String(Base64.decode(base64UserPass.getBytes()));
					int index = userPass.indexOf(':');
					this.remoteUser = userPass.substring(0, index);
					//String password = userPass.substring(index+1);
				} catch (Exception ignore) {
				}
			}
		}

		// get user name from providers
		if (this.remoteUser == null && this.hasHttpSession) {
			// Be careful here as we are in a constructor, and all structures may not be initialized yet
			// e.g. NPE with HttpRequestRemoteUser (session.getSessionInfo().getRemoteUser()) on 1st http request to an application where a HttpSession was previously created,
			//   picked up by our HttpSessionListener, but with no associated SessionInfo.lastRequestInfo (HttpServletRequestInfo) yet (no 1st hit)
			//   (also seen in some Hybris setup)
			//   Note that this use-case (switching HttpSession within a single HTTP request) is now taken care of elsewhere, and should not pose problems any more.
			try {
				Object user = UserNameProvider.Util.guessUserFromSession(request.getSession(false), null);
				if (user != null) {
					this.remoteUser = user.toString();
				}
			} catch (Exception e) {
				// swallow
			}
		}
	}


	public boolean hasHttpSession() {
		return hasHttpSession;
	}

	public String getURL() {
		return url;
	}

	public Date getRequestDate() {
		return new Date(requestDate);
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

	public String getRemoteUserName() {
		return remoteUser != null ? remoteUser : (userPrincipal != null ? userPrincipal.getName() : null);
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

	/**
	 * {@inheritDoc}
	 */
	public long getCurrentlyUsedTime() {
		return NANOSECONDS.toMillis(System.nanoTime() - requestDateNano);
	}

	protected String getXForwardedFor(final HttpServletRequest request, final String remoteHostToAdd) {
		String remoteHostComplete;
		String xForwardedFor = null;
		Enumeration<String> xffEnum = request.getHeaders(X_FORWARDED_FOR);
		// Concatenate all X-Forwarded-For HTTP headers
		if (xffEnum != null && xffEnum.hasMoreElements()) {
			while (xffEnum.hasMoreElements()) {
				String xForwardedForHeader = xffEnum.nextElement();
				if (StringUtils.isNotBlank(xForwardedForHeader)) {
					if (xForwardedFor == null) {// 1rst time in loop
						xForwardedFor = xForwardedForHeader;
					} else {
						xForwardedFor = xForwardedFor + ", " + xForwardedForHeader;//$NON-NLS-1$
					}
				}
			}
		}
		if (StringUtils.isNotBlank(xForwardedFor)) {
			remoteHostComplete = X_FORWARDED_FOR + ": " + xForwardedFor + ", " + remoteHostToAdd;//$NON-NLS-1$//$NON-NLS-2$
		} else {
			remoteHostComplete = remoteHostToAdd;
		}
		return remoteHostComplete;
	}

}
