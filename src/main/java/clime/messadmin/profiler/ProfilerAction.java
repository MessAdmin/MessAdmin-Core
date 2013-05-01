/**
 *
 */
package clime.messadmin.profiler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import clime.messadmin.admin.AdminActionProvider;
import clime.messadmin.admin.BaseAdminActionProvider;

/**
 * @author C&eacute;drik LIME
 */
public class ProfilerAction extends BaseAdminActionProvider implements AdminActionProvider {
	public static final String ACTION_ID = "profiler";//$NON-NLS-1$

	/**
	 *
	 */
	public ProfilerAction() {
		super();
	}

	/** {@inheritDoc} */
	public String getActionID() {
		return ACTION_ID;
	}

	/** {@inheritDoc} */
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		setNoCache(response);
		request.getRequestDispatcher("/MessAdmin/diagnostic/prof.jsp").include(request, response);//FIXME hard-coded path
	}
}
