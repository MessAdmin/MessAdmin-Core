/**
 * 
 */
package clime.messadmin.providers.user;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.security.auth.Subject;
import javax.servlet.http.HttpSession;

import clime.messadmin.providers.spi.UserNameProvider;

/**
 * Search all attributes for a single java.security.Principal or javax.security.auth.Subject
 * @author C&eacute;drik LIME
 */
public class AttributesIterator implements UserNameProvider {

	public AttributesIterator() {
		super();
	}

	/** {@inheritDoc} */
	public int getPriority() {
		return 200;
	}

	/** {@inheritDoc} */
	public Object guessUserFromSession(HttpSession httpSession) {
		Object user = null;

		final List principalArray = new ArrayList();
		Enumeration<String> attrEnum = httpSession.getAttributeNames();
		while (attrEnum.hasMoreElements()) {
			String name = attrEnum.nextElement();
			Object obj = httpSession.getAttribute(name);
			if (null != obj && (obj instanceof Principal || obj instanceof Subject)) {
				principalArray.add(obj);
			}
		}
		if (principalArray.size() == 1) {
			user = principalArray.get(0);
		}

		return user;
	}

}
