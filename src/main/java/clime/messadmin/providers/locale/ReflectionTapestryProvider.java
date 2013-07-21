/**
 * 
 */
package clime.messadmin.providers.locale;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpSession;

import clime.messadmin.providers.spi.LocaleProvider;

/**
 * Note: this provider implementation uses reflection, to avoid linking against Tapestry libs.
 * @author C&eacute;drik LIME
 */
public class ReflectionTapestryProvider implements LocaleProvider {

	/**
	 * 
	 */
	public ReflectionTapestryProvider() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getPriority() {
		return 30;
	}

	/**
	 * {@inheritDoc}
	 */
	public Locale guessLocaleFromSession(HttpSession httpSession) {
		Locale locale = null;

		// Tapestry 3.0: Engine stored in session under "org.apache.tapestry.engine:" + config.getServletName()
		// TODO: Tapestry 4+
		final List tapestryArray = new ArrayList();
		Enumeration<String> attrEnum = httpSession.getAttributeNames();
		while (attrEnum.hasMoreElements()) {
			String name = attrEnum.nextElement();
			if (name.indexOf("tapestry") > -1 && name.indexOf("engine") > -1 && null != httpSession.getAttribute(name)) {//$NON-NLS-1$ //$NON-NLS-2$
				tapestryArray.add(httpSession.getAttribute(name));
			}
		}
		if (tapestryArray.size() == 1) {
			// found a potential Engine! Let's call getLocale() on it.
			Object probableEngine = tapestryArray.get(0);
			if (null != probableEngine) {
				try {
					Method readMethod = probableEngine.getClass().getMethod("getLocale");//$NON-NLS-1$
					if (null != readMethod) {
						// Call the property getter and return the value
						Object possibleLocale = readMethod.invoke(probableEngine);
						if (null != possibleLocale && possibleLocale instanceof Locale) {
							locale = (Locale) possibleLocale;
						}
					}
				} catch (Exception e) {
					// stay silent
				}
			}
		}

		return locale;
	}

}
