package clime.messadmin.providers.user;

import java.lang.reflect.Method;

import javax.servlet.http.HttpSession;

import clime.messadmin.providers.spi.UserNameProvider;

/**
 * Note: this provider implementation uses reflection, to avoid linking against Seam libs.
 * 
 * @author @author C&eacute;drik LIME
 * @since 4.1
 */
public class JBossSeamProvider implements UserNameProvider {
	protected static final String SEAM_IDENTITY_KEY = "org.jboss.seam.security.identity";//$NON-NLS-1$

	public JBossSeamProvider() {
		super();
	}

	/**
	 * @see clime.messadmin.providers.spi.BaseProvider#getPriority()
	 */
	public int getPriority() {
		return 60;
	}

	/**
	 * @see clime.messadmin.providers.spi.UserNameProvider#guessUserFromSession(javax.servlet.http.HttpSession)
	 */
	public Object guessUserFromSession(HttpSession httpSession) {
		Object identity = httpSession.getAttribute(SEAM_IDENTITY_KEY);
		if (identity != null) {
			try {
				Method getUserNameMethod = identity.getClass().getMethod("getUsername");//$NON-NLS-1$
				return getUserNameMethod.invoke(identity);
			} catch (Exception e) {
				// not a chance...
			}
		}
		return null;
	}

}
