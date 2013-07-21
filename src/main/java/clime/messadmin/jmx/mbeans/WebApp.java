/**
 * 
 */
package clime.messadmin.jmx.mbeans;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import clime.messadmin.core.MessAdmin;
import clime.messadmin.model.ErrorData;
import clime.messadmin.model.ResponseStatusInfo;
import clime.messadmin.model.Server;

/**
 * IMPLEMENTATION NOTE: don't forget to synchronize setters!
 * @author C&eacute;drik LIME
 */
public class WebApp implements WebAppMBean { //extends NotificationBroadcasterSupport 
	private String context;

	/**
	 * 
	 */
	public WebApp() {
		super();
	}

	public void setContext(String ctx) {
		context = ctx;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getInternalContextPath() {
		return Server.getInstance().getApplication(context).getApplicationInfo().getInternalContextPath();
	}

	/**
	 * {@inheritDoc}
	 */
	public ClassLoader getClassLoader() {
		return Server.getInstance().getApplication(context).getApplicationInfo().getClassLoader();
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<String> getActiveSessionsIds() {
		Set<String> activeSessionsIds = Server.getInstance().getApplication(context).getActiveSessionsIds();
		return new LinkedHashSet<String>(activeSessionsIds);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<String> getPassiveSessionsIds() {
		Set<String> passiveSessionsIds = Server.getInstance().getApplication(context).getPassiveSessionsIds();
		return new LinkedHashSet<String>(passiveSessionsIds);
	}

	// ApplicationInfo data

	/**
	 * {@inheritDoc}
	 */
	public int getHits() {
		return Server.getInstance().getApplication(context).getApplicationInfo().getHits();
	}

	/** {@inheritDoc} */
	public int getNErrors() {
		return Server.getInstance().getApplication(context).getApplicationInfo().getNErrors();
	}

	/** {@inheritDoc} */
	public ErrorData getLastError() {
		return Server.getInstance().getApplication(context).getApplicationInfo().getLastError();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getMaxConcurrentSessions() {
		return Server.getInstance().getApplication(context).getApplicationInfo().getMaxConcurrentSessions();
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getMaxConcurrentSessionsDate() {
		return Server.getInstance().getApplication(context).getApplicationInfo().getMaxConcurrentSessionsDate();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getTotalCreatedSessions() {
		return Server.getInstance().getApplication(context).getApplicationInfo().getTotalCreatedSessions();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getRequestTotalLength() {
		return Server.getInstance().getApplication(context).getApplicationInfo().getRequestTotalLength();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getRequestMaxLength() {
		return Server.getInstance().getApplication(context).getApplicationInfo().getRequestMaxLength();
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getRequestMaxLengthDate() {
		return Server.getInstance().getApplication(context).getApplicationInfo().getRequestMaxLengthDate();
	}

	/**
	 * {@inheritDoc}
	 */
	public double getRequestMeanLength() {
		return Server.getInstance().getApplication(context).getApplicationInfo().getRequestMeanLength();
	}

	/**
	 * {@inheritDoc}
	 */
	public double getRequestStdDevLength() {
		return Server.getInstance().getApplication(context).getApplicationInfo().getRequestStdDevLength();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getResponseTotalLength() {
		return Server.getInstance().getApplication(context).getApplicationInfo().getResponseTotalLength();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getResponseMaxLength() {
		return Server.getInstance().getApplication(context).getApplicationInfo().getResponseMaxLength();
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getResponseMaxLengthDate() {
		return Server.getInstance().getApplication(context).getApplicationInfo().getResponseMaxLengthDate();
	}

	/**
	 * {@inheritDoc}
	 */
	public double getResponseMeanLength() {
		return Server.getInstance().getApplication(context).getApplicationInfo().getResponseMeanLength();
	}

	/**
	 * {@inheritDoc}
	 */
	public double getResponseStdDevLength() {
		return Server.getInstance().getApplication(context).getApplicationInfo().getResponseStdDevLength();
	}

	/**
	 * {@inheritDoc}
	 */
	public ResponseStatusInfo getResponseStatusInfo() {
		return Server.getInstance().getApplication(context).getApplicationInfo().getResponseStatusInfo();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getActiveSessionsCount() {
		return Server.getInstance().getApplication(context).getApplicationInfo().getActiveSessionsCount();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getPassiveSessionsCount() {
		return Server.getInstance().getApplication(context).getApplicationInfo().getPassiveSessionsCount();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getActiveSessionsSize() {
		return Server.getInstance().getApplication(context).getApplicationInfo().getActiveSessionsSize();
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getStartupTime() {
		return Server.getInstance().getApplication(context).getApplicationInfo().getStartupTime();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getUsedTimeTotal() {
		return Server.getInstance().getApplication(context).getApplicationInfo().getUsedTimeTotal();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getUsedTimeMax() {
		return Server.getInstance().getApplication(context).getApplicationInfo().getUsedTimeMax();
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getUsedTimeMaxDate() {
		return Server.getInstance().getApplication(context).getApplicationInfo().getUsedTimeMaxDate();
	}

	/**
	 * {@inheritDoc}
	 */
	public double getUsedTimeMean() {
		return Server.getInstance().getApplication(context).getApplicationInfo().getUsedTimeMean();
	}

	/**
	 * {@inheritDoc}
	 */
	public double getUsedTimeStdDev() {
		return Server.getInstance().getApplication(context).getApplicationInfo().getUsedTimeStdDev();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getContextPath() {
		return Server.getInstance().getApplication(context).getApplicationInfo().getContextPath();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getServletContextName() {
		return Server.getInstance().getApplication(context).getApplicationInfo().getServletContextName();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getServerInfo() {
		return Server.getInstance().getApplication(context).getApplicationInfo().getServerInfo();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Map.Entry<String, String>> getApplicationSpecificData() {
		return Server.getInstance().getApplication(context).getApplicationInfo().getApplicationSpecificData();
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, String> getInitParameters() {
		return Server.getInstance().getApplication(context).getApplicationInfo().getInitParameters();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getInitParameter(String name) {
		return Server.getInstance().getApplication(context).getApplicationInfo().getInitParameter(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, Object> getAttributes() {
		return Server.getInstance().getApplication(context).getApplicationInfo().getAttributes();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getAttribute(String name) {
		return Server.getInstance().getApplication(context).getApplicationInfo().getAttribute(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized void setAttribute(String name, Object object) {
		Server.getInstance().getApplication(context).getApplicationInfo().setAttribute(name, object);
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized void removeAttribute(String name) {
		Server.getInstance().getApplication(context).getApplicationInfo().removeAttribute(name);
	}

	// WebApp-related actions

	/**
	 * {@inheritDoc}
	 */
	public synchronized void sendAllSessionsMessage(String in_message) {
		MessAdmin.injectAllSessions(context, in_message);
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized void setApplicationOnceMessage(String in_message) {
		MessAdmin.injectApplicationsOnce(new String[] {context}, in_message);
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized void setApplicationPermanentMessage(String in_message) {
		Server.getInstance().getApplication(context).injectPermanentMessage(in_message);
	}

}
