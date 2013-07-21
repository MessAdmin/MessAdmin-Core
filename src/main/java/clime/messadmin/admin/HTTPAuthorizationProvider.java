package clime.messadmin.admin;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import clime.messadmin.utils.Base64;

/**
 * @author C&eacute;drik LIME
 */
class HTTPAuthorizationProvider {
	/**
	 * String identifier for Basic authentication, with space. Value "BASIC ".
	 */
	private static final String BASIC_AUTH = HttpServletRequest.BASIC_AUTH.toUpperCase() + ' ';
	/**
	 * Magic request parameter that can contain the administration password (instead of cookies)
	 */
	private static final String J_PASSWORD = "j_password";//$NON-NLS-1$

	private static byte[] sha1(String message) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");//$NON-NLS-1$
			return md.digest(message.getBytes());
		} catch (NoSuchAlgorithmException nsae) {
			throw new RuntimeException("Error while computing sha-1 hash", nsae);//$NON-NLS-1$
		}
	}


	public static boolean checkAccess(final String authorizationPassword, HttpServletRequest request, HttpServletResponse response) throws IOException {
		// no password set => no authorization required
		if (authorizationPassword == null || "".equals(authorizationPassword.trim())) {//$NON-NLS-1$
			return true;
		}

		// password from a previously-set authorization cookie?
		if (request.getCookies() != null) {
			Cookie[] cookies = request.getCookies();
			String authorizationPasswordHash = Base64.encodeToString(sha1(authorizationPassword), false);
			// some browsers always set null for cookie.getPath()...
			String cookieName = getMessAdminCookieName(request);
			for (int i = 0; i < cookies.length; ++i) {
				Cookie cookie = cookies[i];
				if (cookieName.equals(cookie.getName())) {
					String providedPasswordHash = cookie.getValue();
					if (authorizationPasswordHash.equals(providedPasswordHash)) {
						return true;
					}
				}
			}
		}

		// we must authenticate the user before letting her play with us
		String providedPassword = null;
		if (request.getParameter(J_PASSWORD) != null) {//$NON-NLS-1$
			// password in URL
			providedPassword = request.getParameter(J_PASSWORD);//$NON-NLS-1$
			//request.setAttribute(J_PASSWORD, providedPassword);//$NON-NLS-1$
		} else if (request.getHeader("Authorization") != null) {//$NON-NLS-1$
			// password from HTTP Access Authentication
			String authorization = request.getHeader("Authorization");//$NON-NLS-1$
			if (! authorization.toUpperCase().startsWith(BASIC_AUTH)) {
				//TODO we should use "Digest" instead of "Basic", but it is more complicated to code...
				throw new IllegalArgumentException("Only Basic HTTP Access Authentication supported");//$NON-NLS-1$
			}
//			assert request.getAuthType() == HttpServletRequest.BASIC_AUTH : request.getAuthType();
			try {
				String base64UserPass = authorization.substring(BASIC_AUTH.length()).trim();
				String userPass = new String(Base64.decode(base64UserPass.getBytes()));
				int index = userPass.indexOf(':');
				//String user = userPass.substring(0, index);
				String password = userPass.substring(index+1);
				providedPassword = password;
			} catch (Exception ignore) {
				providedPassword = null;
			}
		} else {
			providedPassword = null;
		}

		if (authorizationPassword.equals(providedPassword)) {
			// set authorization Cookie
			// some browsers always set null for cookie.getPath()...
			String cookieName = getMessAdminCookieName(request);
			try {
				Cookie cookie = new Cookie(cookieName, Base64.encodeToString(sha1(authorizationPassword), false));
				cookie.setComment("MessAdmin administration application auto-login");
				cookie.setVersion(1);
				cookie.setMaxAge(-1);// session cookie
				cookie.setPath(request.getContextPath());//URLEncoder.encode(request.getContextPath())
				response.addCookie(cookie);
			} catch (IllegalArgumentException ignore) {
				// Probably the contextPath containing "illegal" (non-ascii) chars...
				// Not a chance, no auto-login Cookie for us!
			}
			return true;
		} else {
			// request Authorization Password
			//TODO we should use "Digest" instead of "Basic", but it is more complicated to code...
			response.setHeader("WWW-Authenticate", "Basic realm=\"MessAdmin Administration for " + request.getContextPath() + '"');
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return false;
		}
	}

	private static String getMessAdminCookieName(HttpServletRequest request) {
		return "MessAdmin_" + stripNameTokens(request.getContextPath());//$NON-NLS-1$
	}

	// From javax.servlet.Cookie

	// Note -- disabled for now to allow full Netscape compatibility
	// from RFC 2068, token special case characters
	//
	// private static final String tspecials = "()<>@,;:\\\"/[]?={} \t";
	private static final String tspecials = ",; ";

	/*
	 * Tests a string and returns true if the string counts as a
	 * reserved token in the Java language.
	 *
	 * @param value		the <code>String</code> to be tested
	 *
	 * @return			<code>true</code> if the <code>String</code> is
	 *				a reserved token; <code>false</code>
	 *				if it is not
	 */
	private static String stripNameTokens(String value) {
		int len = value.length();
		StringBuilder out = new StringBuilder(len);

		for (int i = 0; i < len; ++i) {
			char c = value.charAt(i);
			if (c >= 0x20 && c < 0x7f && tspecials.indexOf(c) == -1) {
				out.append(c);
			}
		}
		return out.toString();
	}

}
