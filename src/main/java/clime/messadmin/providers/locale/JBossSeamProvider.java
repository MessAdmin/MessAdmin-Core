package clime.messadmin.providers.locale;

import java.lang.reflect.Method;
import java.util.Locale;

import javax.servlet.http.HttpSession;

import clime.messadmin.providers.spi.LocaleProvider;

/**
 * Note: this provider implementation uses reflection, to avoid linking against Seam libs.
 * 
 * @author @author C&eacute;drik LIME
 * @since 4.1
 */
public class JBossSeamProvider implements LocaleProvider {
	protected static final String SEAM_LOCALE_KEY = "org.jboss.seam.core.localeSelector";//$NON-NLS-1$

	public JBossSeamProvider() {
		super();
	}

	/**
	 * @see clime.messadmin.providers.spi.BaseProvider#getPriority()
	 */
	public int getPriority() {
		return 40;
	}

	/**
	 * @see clime.messadmin.providers.spi.UserNameProvider#guessUserFromSession(javax.servlet.http.HttpSession)
	 */
	public Locale guessLocaleFromSession(HttpSession httpSession) {
		Object identity = httpSession.getAttribute(SEAM_LOCALE_KEY);
		if (identity != null) {
			try {
				Method getLocaleMethod = identity.getClass().getMethod("getLocale");//$NON-NLS-1$
				return (Locale) getLocaleMethod.invoke(identity);
			} catch (Exception e) {
				// not a chance...
			}
		}
		return null;
	}

}
