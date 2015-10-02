/**
 */
package clime.messadmin.providers.user;

import java.lang.reflect.Method;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import clime.messadmin.model.ISessionInfo;
import clime.messadmin.model.Server;
import clime.messadmin.model.Session;
import clime.messadmin.model.SessionInfo;
import clime.messadmin.providers.spi.RequestLifeCycleProvider;

/**
 * Fetch the current user name from Apache Shiro, if available.
 *
 * Implementation note: Shiro stores its data in a ThreadLocal object, so we need to sniff it at request time.
 * Copy (cache) the information as RemoteUser at request time. It will be picked by the HttpRequestRemoteUser plugin.
 *
 * @author C&eacute;drik LIME
 * @since 5.5
 */
public class ShiroUserName implements RequestLifeCycleProvider {
	protected Class<?> securityUtilsClass;
	protected Method securityUtils_getSubject;
	protected Class<?> subjectClass;
	protected Method subject_getPrincipal;
	protected boolean shiroAvailable = false;

	/**
	 */
	public ShiroUserName() {
		super();
		initialize();
	}

	protected void initialize() {
		try {
			securityUtilsClass = Class.forName("org.apache.shiro.SecurityUtils");
			subjectClass = Class.forName("org.apache.shiro.subject.Subject");

			securityUtils_getSubject = securityUtilsClass.getMethod("getSubject");//$NON-NLS-1$
			subject_getPrincipal = subjectClass.getMethod("getPrincipal");//$NON-NLS-1$

			shiroAvailable = securityUtilsClass != null && subjectClass != null
				&& securityUtils_getSubject != null && subject_getPrincipal != null;
		} catch (Exception e) {
			// do nothing, Shiro not available
			shiroAvailable = false;
		}
	}

	/**
	 * @see clime.messadmin.providers.spi.BaseProvider#getPriority()
	 */
	@Override
	public int getPriority() {
		return 40;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void requestInitialized(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
		/*
		org.apache.shiro.subject.Subject currentUser = org.apache.shiro.SecurityUtils.getSubject();
		if (currentUser != null) {
			Object principal = currentUser.getPrincipal(); // (return null for anonymous)
			if (principal != null) {
				String username = principal.toString();
			}
		}
		 */
		if (shiroAvailable) {
			Session session = Server.getInstance().getSession(request.getSession(false));
			if (session != null) {
				ISessionInfo sessionInfo = session.getSessionInfo();
				if (sessionInfo != null && sessionInfo.getRemoteUser() == null) {
					try {
						String userName = null;
						Object currentUser = securityUtils_getSubject.invoke(null);
						Object principal = subject_getPrincipal.invoke(currentUser);
						if (principal != null) { // null == anonymous
							userName = principal.toString();
							((SessionInfo) sessionInfo).setRemoteUser(userName);
						}
					} catch (Exception e) {
						// shouldn't happen; nothing we can do anyway...
					}
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void requestDestroyed(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
		// do nothing
	}
}
