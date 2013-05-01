/**
 * 
 */
package clime.messadmin.providers.locale;

import java.util.Locale;

import javax.servlet.http.HttpSession;

import clime.messadmin.providers.spi.LocaleProvider;

/**
 * @author C&eacute;drik LIME
 */
public class JSTLProvider implements LocaleProvider {
    private static final char HYPHEN = '-';//$NON-NLS-1$
    private static final char UNDERSCORE = '_';//$NON-NLS-1$

    //javax.servlet.jsp.jstl.core.Config.SESSION_SCOPE_SUFFIX
    private static final String SESSION_SCOPE_SUFFIX = ".session";//$NON-NLS-1$

    /**
     * Name of configuration setting for application- (as opposed to browser-)
     * based preferred locale
     */
    //javax.servlet.jsp.jstl.core.Config.FMT_LOCALE
    public static final String FMT_LOCALE = "javax.servlet.jsp.jstl.fmt.locale";//$NON-NLS-1$

    /**
     * Name of configuration setting for fallback locale
     */
    //javax.servlet.jsp.jstl.core.Config.FMT_FALLBACK_LOCALE
    public static final String FMT_FALLBACK_LOCALE = "javax.servlet.jsp.jstl.fmt.fallbackLocale";//$NON-NLS-1$

	/**
	 * 
	 */
	public JSTLProvider() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getPriority() {
		return 20;
	}

	/**
	 * {@inheritDoc}
	 */
	public Locale guessLocaleFromSession(HttpSession httpSession) {
		Locale loc = null;

		Object obj = httpSession.getAttribute(FMT_LOCALE + SESSION_SCOPE_SUFFIX);
		if (obj != null) {
			if (obj instanceof Locale) {
				loc = (Locale) obj;
			} else if (obj instanceof String) {
				try {
					loc = parseLocale((String) obj, null);
				} catch (IllegalArgumentException iae) {
				}
			}
		}

		return loc;
	}

    /**
     * Parses the given locale string into its language and (optionally)
     * country components, and returns the corresponding
     * <tt>java.util.Locale</tt> object.
     *
     * If the given locale string is null or empty, null is returned.
     *
     * @param locale the locale string to parse
     * @param variant the variant
     *
     * @return <tt>java.util.Locale</tt> object corresponding to the given
     * locale string, null if the locale string is null or empty
     *
     * @throws IllegalArgumentException if the given locale does not have a
     * language component or has an empty country component
     */
    public static Locale parseLocale(String locale, String variant) {

		Locale ret = null;
		String language = locale;
		String country = null;
		int index = -1;
	
		if (((index = locale.indexOf(HYPHEN)) > -1)
		        || ((index = locale.indexOf(UNDERSCORE)) > -1)) {
		    language = locale.substring(0, index);
		    country = locale.substring(index+1);
		}
	
		if ((language == null) || (language.length() == 0)) {
		    throw new IllegalArgumentException("Missing language component in 'value' attribute in &lt;setLocale&gt;");
		}
	
		if (country == null) {
		    if (variant != null)
			ret = new Locale(language, "", variant);
		    else
			ret = new Locale(language, "");
		} else if (country.length() > 0) {
		    if (variant != null)
			ret = new Locale(language, country, variant);
		    else
			ret = new Locale(language, country);
		} else {
		    throw new IllegalArgumentException("Empty country component in 'value' attribute in &lt;setLocale&gt;");
		}
	
		return ret;
    }
}
