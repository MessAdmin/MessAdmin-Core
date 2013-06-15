/**
 * 
 */
package clime.messadmin.providers.user;

import javax.servlet.http.HttpSession;

import clime.messadmin.providers.spi.UserNameProvider;

/**
 * Search "known locations" for user name
 * @author C&eacute;drik LIME
 */
public class KnownLocations implements UserNameProvider {
	/**
	 * The session attributes key under which the user
	 * name is stored, if any.
	 * 
	 * Lower and upper-case strings will be dynamically generated. Put mid-capitalized strings here!
	 */
	private static final String[] USER_TEST_ATTRIBUTES = new String[] {
		"Login", "User", "userName", "UserName", "Utilisateur" };

	public KnownLocations() {
		super();
	}

	/** {@inheritDoc} */
	public int getPriority() {
		return 100;
	}

	/** {@inheritDoc} */
	public Object guessUserFromSession(HttpSession httpSession) {
		Object user = null;

		for (int i = 0; i < USER_TEST_ATTRIBUTES.length; ++i) {
			Object obj = httpSession.getAttribute(USER_TEST_ATTRIBUTES[i]);
			if (null != obj) {
				user = obj;
				break;
			}
			obj = httpSession.getAttribute(USER_TEST_ATTRIBUTES[i].toLowerCase());
			if (null != obj) {
				user = obj;
				break;
			}
			obj = httpSession.getAttribute(USER_TEST_ATTRIBUTES[i].toUpperCase());
			if (null != obj) {
				user = obj;
				break;
			}
		}

		return user;
	}

}
