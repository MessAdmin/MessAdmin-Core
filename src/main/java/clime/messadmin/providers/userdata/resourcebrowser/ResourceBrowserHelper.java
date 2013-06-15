/**
 *
 */
package clime.messadmin.providers.userdata.resourcebrowser;

import javax.servlet.ServletContext;

import clime.messadmin.admin.AdminActionProvider;
import clime.messadmin.providers.spi.DisplayProvider;

/**
 * Browser the war content
 * @author C&eacute;drik LIME
 */
class ResourceBrowserHelper extends BaseBrowserHelper {
	private static final String BUNDLE_NAME = ResourceBrowser.class.getName();

	private static final Resource ROOT_RESOURCE = new Resource("/");//$NON-NLS-1$

	public ResourceBrowserHelper(AdminActionProvider adminActionProviderCallback, DisplayProvider displayProviderCallback) {
		super(adminActionProviderCallback, displayProviderCallback);
	}

	/** {@inheritDoc} */
	@Override
	protected BaseResource getResource(ServletContext context, String resourcePath) {
		return new Resource(resourcePath);
	}

	/** {@inheritDoc} */
	@Override
	public String getI18nBundleName() {
		return BUNDLE_NAME;
	}

	/** {@inheritDoc} */
	@Override
	protected BaseResource getDefaultRootResource() {
		return ROOT_RESOURCE;
	}

}
