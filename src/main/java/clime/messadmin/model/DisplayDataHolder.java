/**
 *
 */
package clime.messadmin.model;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import clime.messadmin.providers.spi.ApplicationDataProvider;
import clime.messadmin.providers.spi.DisplayProvider;
import clime.messadmin.providers.spi.ServerDataProvider;
import clime.messadmin.providers.spi.SessionDataProvider;

/**
 * Holder to pass DisplayProvider to the JSPs.
 * Should not be used outside this DTO function.
 * @author C&eacute;drik LIME
 */
public abstract class DisplayDataHolder {
	protected DisplayProvider displayProvider;
	DisplayDataHolder(DisplayProvider dp) {
		super();
		this.displayProvider = dp;
	}
	public String getHTMLId() {
		return DisplayProvider.Util.getId(displayProvider);
	}
	public abstract String getTitle();
	public abstract String getXHTMLData();


	static class ErrorDataHolder extends DisplayDataHolder {
		protected Exception e;
		public ErrorDataHolder(DisplayProvider dp, Exception e) {
			super(dp);
			this.e = e;
		}
		/** {@inheritDoc} */
		public String getTitle() {
			return displayProvider.getClass().getName();
		}
		/** {@inheritDoc} */
		public String getXHTMLData() {
			return e.toString();
		}
	}

	static class ServerDataHolder extends DisplayDataHolder {
		public ServerDataHolder(ServerDataProvider dp) {
			super(dp);
		}
		/** {@inheritDoc} */
		public String getTitle() {
			try {
				return ((ServerDataProvider)displayProvider).getServerDataTitle();
			} catch (RuntimeException rte) {
				return new ErrorDataHolder(displayProvider, rte).getTitle();
			}
		}
		/** {@inheritDoc} */
		public String getXHTMLData() {
			try {
				return ((ServerDataProvider)displayProvider).getXHTMLServerData();
			} catch (RuntimeException rte) {
				return new ErrorDataHolder(displayProvider, rte).getXHTMLData();
			}
		}
	}

	static class ApplicationDataHolder extends DisplayDataHolder {
		protected ServletContext servletContext;
		public ApplicationDataHolder(ApplicationDataProvider dp, ServletContext sc) {
			super(dp);
			this.servletContext = sc;
		}
		/** {@inheritDoc} */
		public String getTitle() {
			try {
				return ((ApplicationDataProvider)displayProvider).getApplicationDataTitle(servletContext);
			} catch (RuntimeException rte) {
				return new ErrorDataHolder(displayProvider, rte).getTitle();
			}
		}
		/** {@inheritDoc} */
		public String getXHTMLData() {
			try {
				return ((ApplicationDataProvider)displayProvider).getXHTMLApplicationData(servletContext);
			} catch (RuntimeException rte) {
				return new ErrorDataHolder(displayProvider, rte).getXHTMLData();
			}
		}
	}

	static class SessionDataHolder extends DisplayDataHolder {
		protected HttpSession session;
		public SessionDataHolder(SessionDataProvider dp, HttpSession session) {
			super(dp);
			this.session = session;
		}
		/** {@inheritDoc} */
		public String getTitle() {
			try {
				return ((SessionDataProvider)displayProvider).getSessionDataTitle(session);
			} catch (RuntimeException rte) {
				return new ErrorDataHolder(displayProvider, rte).getTitle();
			}
		}
		/** {@inheritDoc} */
		public String getXHTMLData() {
			try {
				return ((SessionDataProvider)displayProvider).getXHTMLSessionData(session);
			} catch (RuntimeException rte) {
				return new ErrorDataHolder(displayProvider, rte).getXHTMLData();
			}
		}
	}
}
