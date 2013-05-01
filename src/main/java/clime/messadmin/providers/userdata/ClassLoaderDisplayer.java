/**
 *
 */
package clime.messadmin.providers.userdata;

import java.net.URL;
import java.net.URLClassLoader;

import javax.servlet.ServletContext;

import clime.messadmin.i18n.I18NSupport;
import clime.messadmin.model.Server;
import clime.messadmin.providers.spi.ApplicationDataProvider;

/**
 * Displays the URLClassLoader for the current webapp.
 * @author C&eacute;drik LIME
 */
public class ClassLoaderDisplayer implements ApplicationDataProvider {
	private static final String BUNDLE_NAME = ClassLoaderDisplayer.class.getName();

	/**
	 *
	 */
	public ClassLoaderDisplayer() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getPriority() {
		return 50;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getApplicationDataTitle(ServletContext context) {
		return I18NSupport.getLocalizedMessage(BUNDLE_NAME, "title");//$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public String getXHTMLApplicationData(ServletContext context) {
		ClassLoader cl = Server.getInstance().getApplication(context).getApplicationInfo().getClassLoader();
		StringBuilder buffer = new StringBuilder(16384);
		buffer.append("<dl>");
		while (cl != null) {
			dump(cl, buffer);
			cl = cl.getParent();
		}
		buffer.append("</dl>");
		return buffer.toString();
	}

	protected void dump(ClassLoader cl, StringBuilder out) {
		out.append("<dt>");
		out.append(cl.getClass().getName() + "@" + Integer.toHexString(cl.hashCode()));
		out.append("</dt>");
		if (cl instanceof URLClassLoader) {
			URLClassLoader urlcl = (URLClassLoader) cl;
			URL[] urls = urlcl.getURLs();
			out.append("<dd>");
			out.append("<ol>");
			for (int i = 0; i < urls.length; ++i) {
				URL url = urls[i];
				out.append("<li>").append(url).append("</li>\n");
			}
			out.append("</ol>\n");
			out.append("</dd>\n");
		}
	}
}
