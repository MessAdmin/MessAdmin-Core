/**
 * 
 */
package clime.messadmin.providers.spi;

import java.util.Iterator;

import javax.servlet.http.HttpSession;

import clime.messadmin.providers.ProviderUtils;

/**
 * @author C&eacute;drik LIME
 */
public interface UserNameProvider extends BaseProvider {
	public static class Util {
		/**
		 * Try to get user from the session, if possible.
		 * @param httpSession
		 * @return Object
		 */
		public static Object guessUserFromSession(final HttpSession httpSession, ClassLoader cl) {
			if (null == httpSession) {
				return null;
			}
			try {
				Iterator ps = ProviderUtils.getProviders(UserNameProvider.class, cl).iterator();
				while (ps.hasNext()) {
					UserNameProvider provider = (UserNameProvider) ps.next();
					Object user = provider.guessUserFromSession(httpSession);
					if (user != null) {
						return user;
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
	 * @return user name for given HttpSession, or null if it can be determined
	 */
	Object guessUserFromSession(HttpSession httpSession);
}
