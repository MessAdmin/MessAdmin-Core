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
 * Browser the war content
 * @author C&eacute;drik LIME
 */
public class ResourceBrowser extends BaseAdminActionWithContext implements ApplicationDataProvider, AdminActionProvider {
	private static final String BUNDLE_NAME = ResourceBrowser.class.getName();
	private static final String ACTION_ID = "browseWar";//$NON-NLS-1$

	protected transient BaseBrowserHelper helper;

	public ResourceBrowser() {
		super();
		helper = new ResourceBrowserHelper(this, this);
	}

	/** {@inheritDoc} */
	public int getPriority() {
		return 10;
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
		return helper.getXHTMLResourceListing(context);
	}

	/** {@inheritDoc} */
	public void serviceWithContext(HttpServletRequest request, HttpServletResponse response, String context) throws ServletException, IOException {
		//setNoCache(response); // we don't want to prevent caching
		helper.serviceWithContext(request, response, context);
	}
}
