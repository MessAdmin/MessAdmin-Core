/**
 *
 */
package clime.messadmin.providers.userdata.resourcebrowser;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import clime.messadmin.admin.AdminActionProvider;
import clime.messadmin.admin.BaseAdminActionWithContext;
import clime.messadmin.i18n.I18NSupport;
import clime.messadmin.model.Server;
import clime.messadmin.providers.spi.ApplicationDataProvider;

/**
 * Browser for JNDI resources
 * @author C&eacute;drik LIME
 */
public class JNDIBrowser extends BaseAdminActionWithContext implements ApplicationDataProvider, AdminActionProvider {
	private static final String BUNDLE_NAME = JNDIBrowser.class.getName();
	private static final String ACTION_ID = "browseJNDI";//$NON-NLS-1$

	protected transient JNDIBrowserHelper helper;

	public JNDIBrowser() {
		super();
		helper = new JNDIBrowserHelper(this, this);
	}

	/** {@inheritDoc} */
	@Override
	public int getPriority() {
		return 11;
	}

	/** {@inheritDoc} */
	public String getActionID() {
		return ACTION_ID;
	}

	/** {@inheritDoc} */
	public String getApplicationDataTitle(ServletContext context) {
		final ClassLoader cl = Server.getInstance().getApplication(context).getApplicationInfo().getClassLoader();
		return I18NSupport.getLocalizedMessage(BUNDLE_NAME, cl, "title");//$NON-NLS-1$
	}

	/**
	 * This method will only be used for the initial display. Assume a {@code RESOURCE_ID} of {@code /}.
	 */
	public String getXHTMLApplicationData(ServletContext context) {
		try {
			try {
				helper.init();
				return helper.getXHTMLResourceListing(context);
			} finally {
				helper.close();
			}
		} catch (NoClassDefFoundError ignore) {
			// javax.naming.InitialContext is a restricted class in Google App Engine
			return ignore.toString();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void serviceWithContext(HttpServletRequest request, HttpServletResponse response, String context) throws ServletException, IOException {
		try {
			try {
				helper.init();
				//setNoCache(response); // we don't want to prevent caching
				helper.serviceWithContext(request, response, context);
			} finally {
				helper.close();
			}
		} catch (NoClassDefFoundError ignore) {
			// javax.naming.InitialContext is a restricted class in Google App Engine
		}
	}
}
