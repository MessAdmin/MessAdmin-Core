/**
 * 
 */
package clime.messadmin.providers.spi;

import java.util.Locale;

import javax.servlet.http.HttpSession;

import clime.messadmin.providers.ProviderUtils;

/**
 * @author C&eacute;drik LIME
 */
public interface LocaleProvider extends BaseProvider {
	public static class Util {
		/**
		 * Try to get user locale from the session, if possible.
		 * @param httpSession
		 * @return Locale
		 */
		public static Locale guessLocaleFromSession(final HttpSession httpSession, ClassLoader cl) {
			if (null == httpSession) {
				return null;
			}
			try {
				for (LocaleProvider provider : ProviderUtils.getProviders(LocaleProvider.class, cl)) {
					Locale locale = provider.guessLocaleFromSession(httpSession);
					if (locale != null) {
						return locale;
					}
				}
				return null;
			} catch (IllegalStateException ise) {
				//ignore: invalidated session
				return null;
			}
		}
	}

	/**
	 * @param httpSession
	 * @return user locale for given HttpSession, or null if it can be determined
	 */
	Locale guessLocaleFromSession(HttpSession httpSession);
}
