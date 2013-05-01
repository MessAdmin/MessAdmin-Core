/**
 * 
 */
package clime.messadmin.providers.user;

import javax.servlet.http.HttpSession;

import clime.messadmin.model.ISessionInfo;
import clime.messadmin.model.Server;
import clime.messadmin.model.Session;
import clime.messadmin.providers.spi.UserNameProvider;

/**
 * See if there is a HttpRequest remote user
 * @author C&eacute;drik LIME
 */
public class HttpRequestRemoteUser implements UserNameProvider {

	public HttpRequestRemoteUser() {
		super();
	}

	/** {@inheritDoc} */
	public int getPriority() {
		return 10;
	}

	/** {@inheritDoc} */
	public Object guessUserFromSession(HttpSession httpSession) {
		Object user = null;

		Session session = Server.getInstance().getSession(httpSession);
		if (session != null) {
			ISessionInfo sessionInfo = session.getSessionInfo();
			if (sessionInfo != null) {
				if (sessionInfo.getRemoteUser() != null) {
					return sessionInfo.getRemoteUser();
				} else if (sessionInfo.getUserPrincipal() != null) {
					return sessionInfo.getUserPrincipal();
				}
			}
		}

		return user;
	}

}
