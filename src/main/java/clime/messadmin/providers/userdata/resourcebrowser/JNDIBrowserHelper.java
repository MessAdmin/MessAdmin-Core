/**
 *
 */
package clime.messadmin.providers.userdata.resourcebrowser;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.servlet.ServletContext;

import clime.messadmin.admin.AdminActionProvider;
import clime.messadmin.providers.spi.DisplayProvider;
import clime.messadmin.providers.userdata.resourcebrowser.ReflectionDataSourceFinder.DataSourceConfiguration;
import clime.messadmin.utils.StringUtils;

/**
 * Browser for the JNDI directory
 * @author C&eacute;drik LIME
 */
/*
 * Implementation note: in order to avoid creating numerous InitialContext()'s,
 * this class has a request lifecycle...
 */
//
class JNDIBrowserHelper extends BaseBrowserHelper {
	private static final String BUNDLE_NAME = JNDIBrowser.class.getName();
	/**
	 * List of {@code web.xml}'s {@code <env-entry><env-entry-type>} allowed types
	 */
	private static final ThreadLocal<Context> JNDI_CONTEXT = new ThreadLocal<Context>();


	public JNDIBrowserHelper(AdminActionProvider adminActionProviderCallback, DisplayProvider displayProviderCallback) {
		super(adminActionProviderCallback, displayProviderCallback);
	}

	/** {@inheritDoc} */
	@Override
	protected BaseResource getResource(ServletContext context, String resourcePath) {
		return new JNDIResource(resourcePath, getContext());
	}

	//@PostConstruct
	protected void init() throws RuntimeException {
		try {
			JNDI_CONTEXT.set(new InitialContext());
		} catch (NamingException ne) {
			throw new RuntimeException(ne);
		}
	}
	//@PreDestroy
	protected void close() {
		Context context = getContext();
		if (context != null) {
			try {
				context.close();
			} catch (NamingException ignore) {
			}
		}
		JNDI_CONTEXT.remove();
	}
	protected Context getContext() {
		return JNDI_CONTEXT.get();
	}

	/** {@inheritDoc} */
	@Override
	public String getI18nBundleName() {
		return BUNDLE_NAME;
	}

	/** {@inheritDoc} */
	@Override
	protected BaseResource getDefaultRootResource() {
		String result = "java:";//$NON-NLS-1$
		try {
			result = getContext().getNameInNamespace();
		} catch (NamingException ne) {
		}
		return new JNDIResource(result, getContext());
	}

	/** {@inheritDoc} */
	@Override
	protected BaseResource getDefaultUserResource() {
		String result = "java:comp/env/";//$NON-NLS-1$
		try {
			result = ((Context) getContext().lookup(result)).getNameInNamespace();
		} catch (NamingException ne) {
		}
		return new JNDIResource(result, getContext());
	}

	/** {@inheritDoc} */
	@Override
	protected String getEntryTitle(ServletContext context, BaseResource resource) {
		// There is no concept of 'last modified' here. Return Class of entry instead.
		// FIXME should try to get the class from list() instead, as it is less costly
		String title = null;
		try {
			Object element = getContext().lookup(resource.getPath());
			if (! (element instanceof Context)) { // we are not interested in sub-Contexts...
				// Try to fetch interesting data from well-known classes
				//(e.g. org.apache.commons.dbcp.BasicDataSource / org.apache.tomcat.dbcp.dbcp.BasicDataSource:
				//	getDriverClassName() | getUrl() | getUsername()
				//	isClosed()
				//	getNumActive() | getMaxActive()
				//	getMinIdle() | getNumIdle() | getMaxIdle()
				//)
				DataSourceConfiguration dataSourceConfiguration = ReflectionDataSourceFinder.getDataSourceConfiguration(element);
				if (dataSourceConfiguration != null) {
					title = StringUtils.escapeXml(dataSourceConfiguration.toString());
				} else {
					title = element.getClass().getName();
				}
			}
		} catch (NamingException ignore) {
		}
		return title;
	}


	protected <T> void closeQuietly(NamingEnumeration<T> enumeration) {
		if (enumeration != null) {
			try {
				enumeration.close();
			} catch (NamingException ignore) {
			}
		}
	}
}
