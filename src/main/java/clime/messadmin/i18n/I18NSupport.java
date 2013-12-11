/**
 *
 */
package clime.messadmin.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import clime.messadmin.model.Server;
import clime.messadmin.taglib.fmt.MessageTag;
import clime.messadmin.utils.StackIntrospector;

/**
 * ThreadLocal containing administration locale + i18n utilities.
 * @author C&eacute;drik LIME
 * @since 4.1
 */
public class I18NSupport {
	private static final ThreadLocal<Locale> adminLocale = new ThreadLocal<Locale>();
	/**
	 * Default Locale to use for the administration application, if none is requested: {@value}
	 */
	public static final Locale DEFAULT_ADMIN_LOCALE = Locale.ENGLISH;
	private static final Locale EMPTY_LOCALE = new Locale("", "");//$NON-NLS-1$//$NON-NLS-2$

	/**
	 *
	 */
	private I18NSupport() {
		super();
	}

	public static Locale getAdminLocale() {
		Locale locale = adminLocale.get();
		return locale != null ? locale : DEFAULT_ADMIN_LOCALE;
	}

	public static void setAdminLocale(Locale locale) {
		adminLocale.set(locale);
	}

	/**
	 * Gets the resource bundle with the given base name and preferred locale.
	 *
	 * This method calls java.util.ResourceBundle.getBundle(), but ignores
	 * its return value unless its locale represents an exact or language match
	 * with the given preferred locale.
	 *
	 * @param baseName the resource bundle base name. Defaults to caller class name.
	 * @param locale   the preferred locale. Defaults to current admin locale if any (en otherwise).
	 * @param cl       the ClassLoader to use to load the resource bundle. Defaults to this Thread's context ClassLoader.
	 *
	 * @return the requested resource bundle, or <tt>null</tt> if no resource
	 * bundle with the given base name exists or if there is no exact- or
	 * language-match between the preferred locale and the locale of
	 * the bundle returned by java.util.ResourceBundle.getBundle().
	 *
	 * @see clime.messadmin.taglib.fmt.BundleTag#findMatch(String, Locale)
	 */
	public static ResourceBundle getResourceBundle(String baseName, Locale locale, ClassLoader cl) {
		ResourceBundle match = null;

		try {
			if (baseName == null) {
				baseName = StackIntrospector.getCallerClass().getName();
			}
			if (locale == null) {
				locale = getAdminLocale();
			}
			if (cl == null) {
				cl = Thread.currentThread().getContextClassLoader();
			}
			ResourceBundle bundle;
			try {
				bundle = ResourceBundle.getBundle(baseName, locale, cl);
			} catch (MissingResourceException mre) {// try context classloader
				bundle = ResourceBundle.getBundle(baseName, locale, Thread.currentThread().getContextClassLoader());
			}
			Locale avail = bundle.getLocale();
			if (locale.equals(avail)) {
				// Exact match
				match = bundle;
			} else {
				/*
				 * We have to make sure that the match we got is for
				 * the specified locale. The way ResourceBundle.getBundle()
				 * works, if a match is not found with (1) the specified locale,
				 * it tries to match with (2) the current default locale as
				 * returned by Locale.getDefault() or (3) the root resource
				 * bundle (basename).
				 * We must ignore any match that could have worked with (2).
				 * So if an exact match is not found, we make the following extra
				 * tests:
				 *     - avail locale must be equal to preferred locale
				 *     - avail country must be empty or equal to preferred country
				 *       (the equality match might have failed on the variant)
				 */
				if ("".equals(avail.getLanguage())//$NON-NLS-1$
						|| (locale.getLanguage().equals(avail.getLanguage())
								&& ("".equals(avail.getCountry())//$NON-NLS-1$
										|| locale.getCountry().equals(avail.getCountry())))) {
					/*
					 * Language match.
					 * By making sure the available locale does not have a
					 * country and matches the preferred locale's language, we
					 * rule out "matches" based on the container's default
					 * locale. For example, if the preferred locale is
					 * "en-US", the container's default locale is "en-UK", and
					 * there is a resource bundle (with the requested base
					 * name) available for "en-UK", ResourceBundle.getBundle()
					 * will return it, but even though its language matches
					 * that of the preferred locale, we must ignore it,
					 * because matches based on the container's default locale
					 * are not portable across different containers with
					 * different default locales.
					 */
					match = bundle;
				}
				if (match == null) {
					/* Try to fetch (3) the root resource bundle (basename). This case is
					 * when (2) the current default locale as returned by Locale.getDefault()
					 * was used (ResourceBundle.getBundle() algorithm stopped at step 2).
					 */
					try {
						bundle = ResourceBundle.getBundle(baseName, EMPTY_LOCALE, cl);
					} catch (MissingResourceException mre) {// try context classloader
						bundle = ResourceBundle.getBundle(baseName, EMPTY_LOCALE, Thread.currentThread().getContextClassLoader());
					}
					avail = bundle.getLocale();
					if ("".equals(avail.getLanguage())//$NON-NLS-1$
							&& "".equals(avail.getCountry())//$NON-NLS-1$
							&& "".equals(avail.getVariant())) {//$NON-NLS-1$
						// we got a match
						match = bundle;
					}
				}
			}
		} catch (MissingResourceException mre) {
		}

		return match;
	}
	/**
	 * @deprecated use {@link #getResourceBundle(String, Locale, ClassLoader)}
	 */
	@Deprecated
	public static ResourceBundle getResourceBundle(String baseName, Locale locale) {
		return getResourceBundle(baseName, locale, null);
	}
	public static ResourceBundle getResourceBundle(String baseName, ClassLoader cl) {
		return getResourceBundle(baseName, getAdminLocale(), cl);
	}
	public static ResourceBundle getResourceBundle(Locale locale, ClassLoader cl) {
		return getResourceBundle(StackIntrospector.getCallerClass().getName(), locale, cl);
	}
	/**
	 * @deprecated use {@link #getResourceBundle(String, ClassLoader)}
	 */
	@Deprecated
	public static ResourceBundle getResourceBundle(String baseName) {
		return getResourceBundle(baseName, getAdminLocale(), null);
	}
	/**
	 * @deprecated use {@link #getResourceBundle(Locale, ClassLoader)}
	 */
	@Deprecated
	public static ResourceBundle getResourceBundle(Locale locale) {
		return getResourceBundle(StackIntrospector.getCallerClass().getName(), locale, null);
	}
	public static ResourceBundle getResourceBundle(ClassLoader cl) {
		return getResourceBundle(StackIntrospector.getCallerClass().getName(), getAdminLocale(), cl);
	}
	/**
	 * @deprecated use {@link #getResourceBundle(ClassLoader)}
	 */
	@Deprecated
	public static ResourceBundle getResourceBundle() {
		return getResourceBundle(StackIntrospector.getCallerClass().getName(), getAdminLocale(), null);
	}

	/**
	 * Retrieves the localized message corresponding to the given key, and
	 * performs parametric replacement using the arguments specified via
	 * <tt>args</tt>.
	 *
	 * <p>
	 * See the specification of {@link java.text.MessageFormat} for a
	 * description of how parametric replacement is implemented.
	 *
	 * <p>
	 * If no resource bundle with the given base name exists, or the given key
	 * is undefined in the resource bundle, the string "???&lt;key&gt;???" is
	 * returned, where "&lt;key&gt;" is replaced with the given key.
	 *
	 * @param baseName  the resource bundle base name. Defaults to caller class name.
	 * @param locale    the requested Locale. Defaults to current admin locale if any (en otherwise).
	 * @param cl        the ClassLoader to use to load the resource bundle. Defaults to this Thread's context ClassLoader.
	 * @param key       the message key
	 * @param args      the arguments for parametric replacement
	 *
	 * @return the localized message corresponding to the given key
	 */
	public static String getLocalizedMessage(String baseName, Locale locale, ClassLoader cl, String key, Object... args) {
		String message = MessageTag.UNDEFINED_KEY + key + MessageTag.UNDEFINED_KEY;
		if (baseName == null) {
			baseName = StackIntrospector.getCallerClass().getName();
		}
		if (locale == null) {
			locale = getAdminLocale();
		}
		ResourceBundle bundle = getResourceBundle(baseName, locale, cl);
		if (bundle != null) {
			try {
				message = bundle.getString(key);
				if (args != null) {
					MessageFormat formatter = new MessageFormat("");//$NON-NLS-1$
					if (locale != null) {
						formatter.setLocale(locale);
					}
					formatter.applyPattern(message);
					message = formatter.format(args);
				}
			} catch (MissingResourceException mre) {
			}
		}

		return message;
	}
	public static String getLocalizedMessage(String baseName, Locale locale, ClassLoader cl, String key) {
		return getLocalizedMessage(baseName, locale, cl, key, (Object[])null);
	}
	/**
	 * @deprecated use {@link #getLocalizedMessage(String, Locale, ClassLoader, String, Object...)}
	 */
	@Deprecated
	public static String getLocalizedMessage(String baseName, Locale locale, String key, Object... args) {
		return getLocalizedMessage(baseName, locale, null, key, args);
	}
	/**
	 * @deprecated use {@link #getLocalizedMessage(String, Locale, ClassLoader)}
	 */
	@Deprecated
	public static String getLocalizedMessage(String baseName, Locale locale, String key) {
		return getLocalizedMessage(baseName, locale, null, key, (Object[])null);
	}
	public static String getLocalizedMessage(String baseName, ClassLoader cl, String key, Object... args) {
		return getLocalizedMessage(baseName, getAdminLocale(), cl, key, args);
	}
	public static String getLocalizedMessage(String baseName, ClassLoader cl, String key) {
		return getLocalizedMessage(baseName, getAdminLocale(), cl, key, (Object[])null);
	}
	public static String getLocalizedMessage(Locale locale, ClassLoader cl, String key, Object... args) {
		return getLocalizedMessage(StackIntrospector.getCallerClass().getName(), locale, cl, key, args);
	}
	public static String getLocalizedMessage(Locale locale, ClassLoader cl, String key) {
		return getLocalizedMessage(StackIntrospector.getCallerClass().getName(), locale, cl, key, (Object[])null);
	}
	/**
	 * @deprecated use {@link #getLocalizedMessage(String, ClassLoader, String, Object...)}
	 */
	@Deprecated
	public static String getLocalizedMessage(String baseName, String key, Object... args) {
		return getLocalizedMessage(baseName, getAdminLocale(), null, key, args);
	}
	/**
	 * @deprecated use {@link #getLocalizedMessage(String, ClassLoader, String)}
	 */
	@Deprecated
	public static String getLocalizedMessage(String baseName, String key) {
		return getLocalizedMessage(baseName, getAdminLocale(), null, key, (Object[])null);
	}
	/**
	 * @deprecated use {@link #getLocalizedMessage(Locale, ClassLoader, String, Object...)}
	 */
	@Deprecated
	public static String getLocalizedMessage(Locale locale, String key, Object... args) {
		return getLocalizedMessage(StackIntrospector.getCallerClass().getName(), locale, null, key, args);
	}
	/**
	 * @deprecated use {@link #getLocalizedMessage(Locale, ClassLoader, String)}
	 */
	@Deprecated
	public static String getLocalizedMessage(Locale locale, String key) {
		return getLocalizedMessage(StackIntrospector.getCallerClass().getName(), locale, null, key, (Object[])null);
	}
	public static String getLocalizedMessage(ClassLoader cl, String key, Object... args) {
		return getLocalizedMessage(StackIntrospector.getCallerClass().getName(), getAdminLocale(), cl, key, args);
	}
	public static String getLocalizedMessage(ClassLoader cl, String key) {
		return getLocalizedMessage(StackIntrospector.getCallerClass().getName(), getAdminLocale(), cl, key, (Object[])null);
	}
	/**
	 * @deprecated use {@link #getLocalizedMessageClassLoader, String, Object...)}
	 */
	@Deprecated
	public static String getLocalizedMessage(String key, Object... args) {
		return getLocalizedMessage(StackIntrospector.getCallerClass().getName(), getAdminLocale(), null, key, args);
	}
	/**
	 * @deprecated use {@link #getLocalizedMessage(ClassLoader, String)}
	 */
	@Deprecated
	public static String getLocalizedMessage(String key) {
		return getLocalizedMessage(StackIntrospector.getCallerClass().getName(), getAdminLocale(), null, key, (Object[])null);
	}

	/* Utility methods */

	public static ClassLoader getClassLoader(ServletContext context) {
		if (context != null) {
			try {
				return Server.getInstance().getApplication(context).getApplicationInfo().getClassLoader();
			} catch (NullPointerException npe) {
			}
		} // else
		return null;
	}

	public static ClassLoader getClassLoader(HttpSession session) {
		try {
			return getClassLoader(session.getServletContext());
		} catch (IllegalStateException ise) {
			return null;
		}
	}
}
