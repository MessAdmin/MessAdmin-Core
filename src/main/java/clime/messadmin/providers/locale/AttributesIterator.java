/**
 * 
 */
package clime.messadmin.providers.locale;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpSession;

import clime.messadmin.providers.spi.LocaleProvider;

/**
 * Search all attributes for a single java.util.Locale
 * @author C&eacute;drik LIME
 */
public class AttributesIterator implements LocaleProvider {

	/**
	 * 
	 */
	public AttributesIterator() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getPriority() {
		return 200;
	}

	/**
	 * {@inheritDoc}
	 */
	public Locale guessLocaleFromSession(HttpSession httpSession) {
		Locale locale = null;
		
		final List<Locale> localeArray = new ArrayList<Locale>();
		Enumeration<String> attrEnum = httpSession.getAttributeNames();
		while (attrEnum.hasMoreElements()) {
			String name = attrEnum.nextElement();
			Object obj = httpSession.getAttribute(name);
			if (null != obj && obj instanceof Locale) {
				localeArray.add((Locale) obj);
			}
		}
		if (localeArray.size() == 1) {
			locale = localeArray.get(0);
		}

		return locale;
	}

}
