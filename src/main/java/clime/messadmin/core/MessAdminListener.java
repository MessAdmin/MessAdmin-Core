package clime.messadmin.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import clime.messadmin.model.Application;
import clime.messadmin.model.Server;
import clime.messadmin.providers.ProviderUtils;
import clime.messadmin.providers.spi.ApplicationLifeCycleProvider;
import clime.messadmin.providers.spi.SessionLifeCycleProvider;

/**
 * Tracks HttpSessions, taking care of active and passive ones.
 * Note: this is a stateless object.
 * @author C&eacute;drik LIME
 */
public class MessAdminListener implements HttpSessionListener, HttpSessionActivationListener, ServletContextListener {

	/**
	 * 
	 */
	public MessAdminListener() {
		super();
		if (this.getClass().equals(MessAdminListener.class)) {// this class configured in web.xml
			try {
				Class.forName("clime.messadmin.core.autoprobe.MessAdminListener", false, Thread.currentThread().getContextClassLoader());
				// This is not good: we have both the AutoProbe and this listener; bail out
				throw new IllegalStateException("MessAdmin configuration error: you can not have both the MessAdmin listener configured in your web.xml and the MessAdmin-AutoProbe plugin.");
			} catch (ClassNotFoundException expected) {
				// this is good
			}
		}
	}

	/* HttpSession-related events */

	/**
	 * {@inheritDoc}
	 */
	public void sessionCreated(final HttpSessionEvent se) {
		Application app = Server.getInstance().getApplication(se.getSession().getServletContext());
		app.sessionCreated(se);
		ClassLoader cl = app.getApplicationInfo().getClassLoader();
		Iterator iter = ProviderUtils.getProviders(SessionLifeCycleProvider.class, cl).iterator();
		while (iter.hasNext()) {
			SessionLifeCycleProvider lc = (SessionLifeCycleProvider) iter.next();
			try {
				lc.sessionCreated(se.getSession());
			} catch (RuntimeException rte) {
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void sessionDestroyed(final HttpSessionEvent se) {
		ClassLoader cl = Server.getInstance().getApplication(se.getSession().getServletContext()).getApplicationInfo().getClassLoader();
		List providers = new ArrayList(ProviderUtils.getProviders(SessionLifeCycleProvider.class, cl));
		Collections.reverse(providers);
		Iterator iter = providers.iterator();
		while (iter.hasNext()) {
			SessionLifeCycleProvider lc = (SessionLifeCycleProvider) iter.next();
			try {
				lc.sessionDestroyed(se.getSession());
			} catch (RuntimeException rte) {
			}
		}
		Server.getInstance().getApplication(se.getSession().getServletContext()).sessionDestroyed(se);
	}

	/**
	 * {@inheritDoc}
	 */
	public void sessionWillPassivate(final HttpSessionEvent se) {
		ClassLoader cl = Server.getInstance().getApplication(se.getSession().getServletContext()).getApplicationInfo().getClassLoader();
		List providers = new ArrayList(ProviderUtils.getProviders(SessionLifeCycleProvider.class, cl));
		Collections.reverse(providers);
		Iterator iter = providers.iterator();
		while (iter.hasNext()) {
			SessionLifeCycleProvider lc = (SessionLifeCycleProvider) iter.next();
			try {
				lc.sessionWillPassivate(se.getSession());
			} catch (RuntimeException rte) {
			}
		}
		Server.getInstance().getApplication(se.getSession().getServletContext()).sessionWillPassivate(se);
	}

	/**
	 * {@inheritDoc}
	 */
	public void sessionDidActivate(final HttpSessionEvent se) {
		Application app = Server.getInstance().getApplication(se.getSession().getServletContext());
		app.sessionDidActivate(se);
		ClassLoader cl = app.getApplicationInfo().getClassLoader();
		Iterator iter = ProviderUtils.getProviders(SessionLifeCycleProvider.class, cl).iterator();
		while (iter.hasNext()) {
			SessionLifeCycleProvider lc = (SessionLifeCycleProvider) iter.next();
			try {
				lc.sessionDidActivate(se.getSession());
			} catch (RuntimeException rte) {
			}
		}
	}

	/* ServletContext-related events */

	/**
	 * {@inheritDoc}
	 */
	public void contextInitialized(final ServletContextEvent sce) {
		// since we don't have hooks for Server lifecycle, try
		serverInitialized();
		// check we are the only MessAdminListener registered for this ServletContext
		Object clazz = RegistrationTracker.LISTENER_INSTANCE.get(sce.getServletContext());
		if (clazz != null) {
			throw new IllegalStateException("You can have only 1 MessAdminListener registered for a given application. "
					+ "Maybe you both declared it in your web.xml /and/ are using the MessAdmin-AutoProbe plugin?\n"
					+ "Registered classes: " + clazz + " | " + this.getClass().getName());
		}
		RegistrationTracker.LISTENER_INSTANCE.register(sce.getServletContext(), this.getClass().getName());
		//
		Server.getInstance().contextInitialized(sce);
		ClassLoader cl = Server.getInstance().getApplication(sce.getServletContext()).getApplicationInfo().getClassLoader();
		Iterator iter = ProviderUtils.getProviders(ApplicationLifeCycleProvider.class, cl).iterator();
		while (iter.hasNext()) {
			ApplicationLifeCycleProvider lc = (ApplicationLifeCycleProvider) iter.next();
			try {
				lc.contextInitialized(sce.getServletContext());
			} catch (RuntimeException rte) {
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void contextDestroyed(final ServletContextEvent sce) {
		ClassLoader cl = Server.getInstance().getApplication(sce.getServletContext()).getApplicationInfo().getClassLoader();
		List providers = new ArrayList(ProviderUtils.getProviders(ApplicationLifeCycleProvider.class, cl));
		Collections.reverse(providers);
		Iterator iter = providers.iterator();
		while (iter.hasNext()) {
			ApplicationLifeCycleProvider lc = (ApplicationLifeCycleProvider) iter.next();
			try {
				lc.contextDestroyed(sce.getServletContext());
			} catch (RuntimeException rte) {
			}
		}
		Server.getInstance().contextDestroyed(sce);
		ProviderUtils.deregisterCurrent();
		RegistrationTracker.LISTENER_INSTANCE.unregister(sce.getServletContext());
		// since we don't have hooks for Server lifecycle, try
		serverDestroyed();
	}

	/* Server-related events */

	/**
	 */
	public void serverInitialized() {
		Server.getInstance().serverInitialized(); // all work is done there
	}

	/**
	 */
	public void serverDestroyed() {
		Server.getInstance().serverDestroyed(); // all work is done there
	}

}
