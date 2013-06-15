/**
 * 
 */
package clime.messadmin.admin;

import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import clime.messadmin.providers.ProviderUtils;
import clime.messadmin.providers.spi.ApplicationLifeCycleProvider;

/**
 * This class initializes the current webapp's AdminActionProvider's.
 * This enables the use of 3rd-party AdminActions residing in a single webapp (with
 * a centralized administration webapp).
 * Known side effect: administration webapp will have its providers initialized
 * twice (once here, once in {@link MessAdminServlet#init(ServletConfig)}).
 * @author C&eacute;drik LIME
 * @since 4.2
 */
public class AdminActionsInitializer implements ApplicationLifeCycleProvider {

	/**
	 * 
	 */
	public AdminActionsInitializer() {
		super();
	}

	/** {@inheritDoc} */
	public void contextDestroyed(ServletContext servletContext) {
		final ClassLoader thisCL = Thread.currentThread().getContextClassLoader();
		for (AdminActionProvider provider : ProviderUtils.getProviders(AdminActionProvider.class)) {
			if (provider.getClass().getClassLoader() == thisCL) {
				provider.destroy();
			}
		}
	}

	/** {@inheritDoc} */
	public void contextInitialized(final ServletContext servletContext) {
		final ClassLoader thisCL = Thread.currentThread().getContextClassLoader();
		for (final AdminActionProvider provider : ProviderUtils.getProviders(AdminActionProvider.class)) {
			try {
				if (provider.getClass().getClassLoader() == thisCL && provider.getServletConfig() == null) {
					provider.init(new ServletConfig() {
						/** {@inheritDoc} */
						public String getServletName() {
							return provider.getClass().getName();
						}
						/** {@inheritDoc} */
						public ServletContext getServletContext() {
							return servletContext;
						}
						/** {@inheritDoc} */
						public Enumeration getInitParameterNames() {
							return new Enumeration() {
								/** {@inheritDoc} */
								public Object nextElement() {
									return null;
								}
								/** {@inheritDoc} */
								public boolean hasMoreElements() {
									return false;
								}
							};
						}
						/** {@inheritDoc} */
						public String getInitParameter(String name) {
							return null;
						}
					});
				}
			} catch (ServletException se) {
				throw new RuntimeException(se);
			}
		}
	}

	/** {@inheritDoc} */
	public int getPriority() {
		return Integer.MAX_VALUE - 100;
	}
}
