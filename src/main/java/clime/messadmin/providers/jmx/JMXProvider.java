/**
 * 
 */
package clime.messadmin.providers.jmx;

import java.lang.management.ManagementFactory;

import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.OperationsException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import clime.messadmin.jmx.mbeans.Session;
import clime.messadmin.jmx.mbeans.WebApp;
import clime.messadmin.providers.spi.ApplicationLifeCycleProvider;
import clime.messadmin.providers.spi.SessionLifeCycleProvider;
import clime.messadmin.utils.SessionUtils;

/**
 * Tomcat needs to be started with JMX enabled (warning: insecure setup!):
 * JAVA_OPTS=-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9004 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
 * @see <a href="http://docs.oracle.com/javase/1.5.0/docs/guide/management/agent.html">Monitoring and Management Using JMX</a>
 * 
 * @author C&eacute;drik LIME
 */
public class JMXProvider implements ApplicationLifeCycleProvider, SessionLifeCycleProvider {
	private static final String WEBAPP_OBJECTNAME_PRE = "messadmin.";//$NON-NLS-1$
	private static final String WEBAPP_OBJECTNAME_POST = ":type=WebApp";//$NON-NLS-1$
	private static final String SESSION_OBJECTNAME_PRE = "messadmin.";//$NON-NLS-1$
	private static final String SESSION_OBJECTNAME_POST = ".sessions:type=";//$NON-NLS-1$

	/**
	 * 
	 */
	public JMXProvider() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getPriority() {
		return 0;
	}

	/**
	 * register MBean for this session
	 * @see clime.messadmin.providers.spi.SessionLifeCycleProvider#sessionCreated(javax.servlet.http.HttpSession)
	 */
	public void sessionCreated(HttpSession httpSession) {
		String context = getContext(httpSession);
		String sessionId = httpSession.getId();
		String beanName = getBeanName(httpSession);
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		try {
			Session sessionBean = new Session();
			sessionBean.setContext(context);
			sessionBean.setSessionId(sessionId);
			ObjectName name = ObjectName.getInstance(SESSION_OBJECTNAME_PRE + context + SESSION_OBJECTNAME_POST + beanName);
			mbs.registerMBean(sessionBean, name);
		} catch (OperationsException oe) {
			throw new RuntimeException(oe);
		} catch (MBeanRegistrationException e) {
			throw new RuntimeException(e);
		} catch (NullPointerException npe) {
			throw npe;
		}
	}

	/**
	 * unregister MBean for this session
	 * @see clime.messadmin.providers.spi.SessionLifeCycleProvider#sessionDestroyed(javax.servlet.http.HttpSession)
	 */
	public void sessionDestroyed(HttpSession httpSession) {
		String context = getContext(httpSession);
		String beanName = getBeanName(httpSession);
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		try {
			ObjectName name = ObjectName.getInstance(SESSION_OBJECTNAME_PRE + context + SESSION_OBJECTNAME_POST + beanName);
			mbs.unregisterMBean(name);
		} catch (OperationsException oe) {
			throw new RuntimeException(oe);
		} catch (MBeanRegistrationException e) {
			throw new RuntimeException(e);
		} catch (NullPointerException npe) {
			throw npe;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void sessionWillPassivate(HttpSession httpSession) {
		sessionDestroyed(httpSession);
	}

	/**
	 * {@inheritDoc}
	 */
	public void sessionDidActivate(HttpSession httpSession) {
		sessionCreated(httpSession);
	}

	/**
	 * {@inheritDoc}
	 */
	public void contextInitialized(ServletContext servletContext) {
		String context = getContext(servletContext);
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		try {
			WebApp webAppMBean = new WebApp();
			webAppMBean.setContext(context);
			ObjectName webAppName = ObjectName.getInstance(WEBAPP_OBJECTNAME_PRE + context + WEBAPP_OBJECTNAME_POST);
			mbs.registerMBean(webAppMBean, webAppName);
		} catch (OperationsException oe) {
			throw new RuntimeException(oe);
		} catch (MBeanRegistrationException e) {
			throw new RuntimeException(e);
		} catch (NullPointerException npe) {
			throw npe;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void contextDestroyed(ServletContext servletContext) {
		String context = getContext(servletContext);
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		try {
			ObjectName webAppName = ObjectName.getInstance(WEBAPP_OBJECTNAME_PRE + context + WEBAPP_OBJECTNAME_POST);
			mbs.unregisterMBean(webAppName);
		} catch (OperationsException oe) {
			throw new RuntimeException(oe);
		} catch (MBeanRegistrationException e) {
			throw new RuntimeException(e);
		} catch (NullPointerException npe) {
			throw npe;
		}
	}


	protected String getContext(HttpSession httpSession) {
		String context = SessionUtils.getContext(httpSession);
		return context;
	}
	protected String getBeanName(HttpSession httpSession) {
		String beanName = httpSession.getId();//TODO find a better name (session user name?)
		return beanName;
	}

	protected String getContext(ServletContext servletContext) {
		String context = SessionUtils.getContext(servletContext);
		return context;
	}
}
