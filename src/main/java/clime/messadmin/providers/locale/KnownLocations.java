/**
 * 
 */
package clime.messadmin.providers.locale;

import java.util.Locale;

import javax.servlet.http.HttpSession;

import clime.messadmin.providers.spi.LocaleProvider;

/**
 * Search "known locations" for java.util.Locale
 * IMPLEMENTATION NOTE: this provider has explicit support for Struts 1.x and Spring
 * JSF checks the browser meta tag "accept languages" to choose what langage to display.
 * @author C&eacute;drik LIME
 */
public class KnownLocations implements LocaleProvider {
	/**
	 * The session attributes key under which the user's selected
	 * <code>java.util.Locale</code> is stored, if any.
	 */
	// org.apache.struts.Globals.LOCALE_KEY
	private static final String STRUTS_LOCALE_KEY = "org.apache.struts.action.LOCALE";//$NON-NLS-1$
	// javax.servlet.jsp.jstl.core.Config.FMT_LOCALE
	private static final String JSTL_LOCALE_KEY   = "javax.servlet.jsp.jstl.fmt.locale";//$NON-NLS-1$
	// org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME
	private static final String SPRING_LOCALE_KEY = "org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE";//$NON-NLS-1$
	/**
	 * Lower and upper-case strings will be dynamically generated. Put mid-capitalised strings here!
	 */
	private static final String[] LOCALE_TEST_ATTRIBUTES = new String[] {
		STRUTS_LOCALE_KEY, SPRING_LOCALE_KEY, JSTL_LOCALE_KEY, "Locale", "java.util.Locale" };

	/**
	 * 
	 */
	public KnownLocations() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getPriority() {
		return 10;
	}

	/**
	 * {@inheritDoc}
	 */
	public Locale guessLocaleFromSession(HttpSession httpSession) {
		Locale locale = null;
		
		for (int i = 0; i < LOCALE_TEST_ATTRIBUTES.length; ++i) {
			Object obj = httpSession.getAttribute(LOCALE_TEST_ATTRIBUTES[i]);
			if (null != obj && obj instanceof Locale) {
				locale = (Locale) obj;
				break;
			}
			obj = httpSession.getAttribute(LOCALE_TEST_ATTRIBUTES[i].toLowerCase());
			if (null != obj && obj instanceof Locale) {
				locale = (Locale) obj;
				break;
			}
			obj = httpSession.getAttribute(LOCALE_TEST_ATTRIBUTES[i].toUpperCase());
			if (null != obj && obj instanceof Locale) {
				locale = (Locale) obj;
				break;
			}
		}

		return locale;
	}

}
