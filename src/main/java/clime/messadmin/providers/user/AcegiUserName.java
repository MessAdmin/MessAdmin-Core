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
 * Fetch the current user name from Acegi, if available.
 *
 * Implementation note: Acegi stores its data in a ThreadLocal object, so we need to sniff it at request time.
 * Copy (cache) the information as RemoteUser at request time. It will be picked by the HttpRequestRemoteUser plugin.
 *
 * @author C&eacute;drik LIME
 * @since 4.1
 */
public class AcegiUserName implements RequestLifeCycleProvider {
	protected Class securityContextHolderClass;
	protected Method securityContextHolder_getContext;
	protected Class securityContextClass;
	protected Method securityContext_getAuthentication;
	protected Class authenticationClass;
	protected Method authentication_getPrincipal;
	protected Class userDetailsClass;
	protected Method userDetails_getUsername;
	protected boolean acegiAvailable = false;

	/**
	 */
	public AcegiUserName() {
		super();
		initialize();
	}

	protected void initialize() {
		try {
			securityContextHolderClass = Class.forName(getSecurityContextHolderClassName());
			securityContextClass = Class.forName(getSecurityContextClassName());
			authenticationClass = Class.forName(getAuthenticationClassName());
			userDetailsClass = Class.forName(getUserDetailsClassName());

			securityContextHolder_getContext = securityContextHolderClass.getMethod("getContext");//$NON-NLS-1$
			securityContext_getAuthentication = securityContextClass.getMethod("getAuthentication");//$NON-NLS-1$
			authentication_getPrincipal = authenticationClass.getMethod("getPrincipal");//$NON-NLS-1$
			userDetails_getUsername = userDetailsClass.getMethod("getUsername");//$NON-NLS-1$

			acegiAvailable = securityContextHolderClass != null && securityContextClass != null
				&& authenticationClass != null && userDetailsClass != null
				&& securityContextHolder_getContext != null
				&& securityContext_getAuthentication != null
				&& authentication_getPrincipal != null
				&& userDetails_getUsername != null;
		} catch (Exception e) {
			// do nothing, Acegi not available
			acegiAvailable = false;
		}
	}

	protected String getSecurityContextHolderClassName() {
		return "org.acegisecurity.context.SecurityContextHolder";//$NON-NLS-1$
	}
	protected String getSecurityContextClassName() {
		return "org.acegisecurity.context.SecurityContext";//$NON-NLS-1$
	}
	protected String getAuthenticationClassName() {
		return "org.acegisecurity.Authentication";//$NON-NLS-1$
	}
	protected String getUserDetailsClassName() {
		return "org.acegisecurity.userdetails.UserDetails";//$NON-NLS-1$
	}

	/**
	 * @see clime.messadmin.providers.spi.BaseProvider#getPriority()
	 */
	public int getPriority() {
		return 50;
	}

	/**
	 * {@inheritDoc}
	 */
	public void requestInitialized(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
		/*
		Object obj = org.acegisecurity.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (obj instanceof org.acegisecurity.userdetails.UserDetails) {
			String username = ((org.acegisecurity.userdetails.UserDetails) obj).getUsername();
		} else {
			String username = obj.toString();
		}
		 */
		if (acegiAvailable) {
			Session session = Server.getInstance().getSession(request.getSession(false));
			if (session != null) {
				ISessionInfo sessionInfo = session.getSessionInfo();
				if (sessionInfo != null && sessionInfo.getRemoteUser() == null) {
					try {
						String userName = null;
						Object securityContext = securityContextHolder_getContext.invoke(null);
						Object authentication = securityContext_getAuthentication.invoke(securityContext);
						Object obj = authentication_getPrincipal.invoke(authentication);
						if (userDetailsClass.isInstance(obj)) { // obj instanceof UserDetails
							userName = (String) userDetails_getUsername.invoke(obj);
						} else {
							userName = (obj == null) ? null : obj.toString();
						}
						if (userName != null) {
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
	public void requestDestroyed(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) {
		// do nothing
	}
}
