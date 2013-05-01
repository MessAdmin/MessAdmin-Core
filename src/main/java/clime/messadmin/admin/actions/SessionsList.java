package clime.messadmin.admin.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import clime.messadmin.admin.AdminActionProvider;
import clime.messadmin.admin.BaseAdminActionWithContext;
import clime.messadmin.admin.BaseSessionComparator;
import clime.messadmin.admin.MessAdminServlet;
import clime.messadmin.core.Constants;
import clime.messadmin.i18n.I18NSupport;
import clime.messadmin.model.ISessionInfo;
import clime.messadmin.model.Server;
import clime.messadmin.providers.spi.DisplayFormatProvider;
import clime.messadmin.providers.spi.LocaleProvider;
import clime.messadmin.providers.spi.UserNameProvider;
import clime.messadmin.utils.ReverseComparator;
import clime.messadmin.utils.SessionUtils;

/**
 * Displays the HttpSessions list for a given WebApp (context).
 * IMPLEMENTATION NOTE: always use include() instead of forward(), to get the real name of this servlet in jsp's
 * @author C&eacute;drik LIME
 * @since 4.2
 */
public class SessionsList extends BaseAdminActionWithContext implements AdminActionProvider {
	public static final String ID = "sessionsList";//$NON-NLS-1$

	public SessionsList() {
		super();
	}

	/** {@inheritDoc} */
	public String getActionID() {
		return ID;
	}

	/** {@inheritDoc} */
	@Override
	public StringBuffer getURL(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		StringBuffer url = super.getURL(request, response);
		if (request.getAttribute("sort") != null) {//$NON-NLS-1$//FIXME shouldn't it be request.getParameter()?
			url.append('&').append("sort=").append(request.getAttribute("sort"));//$NON-NLS-1$//$NON-NLS-2$
		}
		if (request.getAttribute("order") != null) {//$NON-NLS-1$//FIXME shouldn't it be request.getParameter()?
			url.append('&').append("order=").append(request.getAttribute("order"));//$NON-NLS-1$//$NON-NLS-2$
		}
		return url;
	}

	/** {@inheritDoc} */
	@Override
	public void serviceWithContext(HttpServletRequest request, HttpServletResponse response, String context) throws ServletException, IOException {
		if (METHOD_POST.equals(request.getMethod())) {
			sendRedirect(request, response);
			return;
		}
		Collection<ISessionInfo> activeSessions = Server.getInstance().getApplication(context).getActiveSessionInfos();
		String sortBy = request.getParameter("sort");//$NON-NLS-1$
		String orderBy = null;
		if (null != sortBy && !"".equals(sortBy.trim())) {
			activeSessions = new ArrayList<ISessionInfo>(activeSessions);
			Comparator comparator = getSessionComparator(sortBy);
			if (comparator != null) {
				orderBy = request.getParameter("order");//$NON-NLS-1$
				if ("DESC".equalsIgnoreCase(orderBy)) {//$NON-NLS-1$
					comparator = new ReverseComparator(comparator);
					//orderBy = "ASC";
				} else {
					//orderBy = "DESC";
				}
				try {
					Collections.sort((List)activeSessions, comparator);
				} catch (IllegalStateException ise) {
					// at least 1 session is invalidated
					request.setAttribute(Constants.APPLICATION_ERROR,
							I18NSupport.getLocalizedMessage(MessAdminServlet.I18N_BUNDLE_NAME, "sessionsList.sort.error"));//$NON-NLS-1$
				}
			} else {
				log("WARNING: unknown sort order: " + sortBy);
			}
		}
		setNoCache(response);
		DisplayFormatProvider.Util.getInstance(request).displaySessionsListPage(request, response, sortBy, orderBy,
				Server.getInstance().getApplication(context).getApplicationInfo(),
				activeSessions, Server.getInstance().getApplication(context).getPassiveSessionsIds());
	}


	/**
	 * Comparator used on the HttpSessions list, when sorting is required
	 * @param sortBy
	 * @return Comparator
	 */
	protected Comparator getSessionComparator(String sortBy) {
		Comparator comparator = null;
		if ("CreationTime".equalsIgnoreCase(sortBy)) {//$NON-NLS-1$
			comparator = new BaseSessionComparator() {
				@Override
				public Comparable getComparableObject(HttpSession session) {
					return new Date(session.getCreationTime());
				}
			};
		} else if ("id".equalsIgnoreCase(sortBy)) {//$NON-NLS-1$
			comparator = new BaseSessionComparator() {
				@Override
				public Comparable getComparableObject(HttpSession session) {
					return session.getId();
				}
			};
		} else if ("LastAccessedTime".equalsIgnoreCase(sortBy)) {//$NON-NLS-1$
			comparator = new BaseSessionComparator() {
				@Override
				public Comparable getComparableObject(HttpSession session) {
					return new Date(session.getLastAccessedTime());
				}
			};
		} else if ("MaxInactiveInterval".equalsIgnoreCase(sortBy)) {//$NON-NLS-1$
			comparator = new BaseSessionComparator() {
				@Override
				public Comparable getComparableObject(HttpSession session) {
					return new Date(session.getMaxInactiveInterval());
				}
			};
		} else if ("new".equalsIgnoreCase(sortBy)) {//$NON-NLS-1$
			comparator = new BaseSessionComparator() {
				@Override
				public Comparable getComparableObject(HttpSession session) {
					return Boolean.valueOf(session.isNew());
				}
			};
		} else if ("locale".equalsIgnoreCase(sortBy)) {//$NON-NLS-1$
			comparator = new BaseSessionComparator() {
				@Override
				public Comparable getComparableObject(HttpSession session) {
					ClassLoader cl = Server.getInstance().getApplication(session.getServletContext()).getApplicationInfo().getClassLoader();
					Locale locale = LocaleProvider.Util.guessLocaleFromSession(session, cl);
					return (null == locale) ? "" : locale.toString();
				}
			};
		} else if ("user".equalsIgnoreCase(sortBy)) {//$NON-NLS-1$
			comparator = new BaseSessionComparator() {
				@Override
				public Comparable getComparableObject(HttpSession session) {
					ClassLoader cl = Server.getInstance().getApplication(session.getServletContext()).getApplicationInfo().getClassLoader();
					Object user = UserNameProvider.Util.guessUserFromSession(session, cl);
					return (null == user) ? "" : user.toString();
				}
			};
		} else if ("UsedTime".equalsIgnoreCase(sortBy)) {//$NON-NLS-1$
			comparator = new BaseSessionComparator() {
				@Override
				public Comparable getComparableObject(HttpSession session) {
					return new Date(SessionUtils.getUsedTimeForSession(session));
				}
			};
		} else if ("IdleTime".equalsIgnoreCase(sortBy)) {//$NON-NLS-1$
			comparator = new BaseSessionComparator() {
				@Override
				public Comparable getComparableObject(HttpSession session) {
					return new Date(SessionUtils.getIdleTimeForSession(session));
				}
			};
		} else if ("TTL".equalsIgnoreCase(sortBy)) {//$NON-NLS-1$
			comparator = new BaseSessionComparator() {
				@Override
				public Comparable getComparableObject(HttpSession session) {
					return new Date(SessionUtils.getTTLForSession(session));
				}
			};
		}
		//TODO: complete this to TTL, etc.
		return comparator;
	}
}
