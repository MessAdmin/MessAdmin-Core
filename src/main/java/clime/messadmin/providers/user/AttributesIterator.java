/**
 * 
 */
package clime.messadmin.providers.user;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

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
		Enumeration attrEnum = httpSession.getAttributeNames();
		while (attrEnum.hasMoreElements()) {
			String name = (String) attrEnum.nextElement();
			Object obj = httpSession.getAttribute(name);
			if (null != obj && (obj instanceof Principal)) {//|| obj instanceof Subject)) {
				principalArray.add(obj);
			}
			// This workaround for JDK 1.3 compatibility. For JDK 1.4+, use previous (commented) instanceof.
			try {
				Class subjectClass = Class.forName("javax.security.auth.Subject", true, Thread.currentThread().getContextClassLoader());//$NON-NLS-1$
				if (subjectClass.isInstance(obj)) {
					principalArray.add(obj);
				}
			} catch (ClassNotFoundException cnfe) {
				// This is JDK 1.3: javax.security.auth.Subject does not exist; do nothing
			}
		}
		if (principalArray.size() == 1) {
			user = principalArray.get(0);
		}

		return user;
	}

}
