/**
 *
 */
package clime.messadmin.model;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import clime.messadmin.providers.ProviderUtils;
import clime.messadmin.providers.spi.ServerDataProvider;
import clime.messadmin.utils.JMX;

/**
 * @author C&eacute;drik LIME
 */
public class ServerInfo implements Serializable, IServerInfo {
	private final static String localHostDefaultName = "localhost";//$NON-NLS-1$
	protected final long startupTime;
	private String localHostStr = "";

	/**
	 *
	 */
	public ServerInfo() {
		super();
		//startupTime = System.currentTimeMillis();
		startupTime = ManagementFactory.getRuntimeMXBean().getStartTime();
		try {
			localHostStr = java.net.InetAddress.getLocalHost().getHostName();
			if (localHostDefaultName.equals(localHostStr)) {
				localHostStr = "";
			}
		} catch (java.net.UnknownHostException uhe) {
			// ignore
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List getServerSpecificData() {
		List result = new ArrayList();
		for (ServerDataProvider sd : ProviderUtils.getProviders(ServerDataProvider.class)) {
			result.add(new DisplayDataHolder.ServerDataHolder(sd));
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getServerName() {
		return localHostStr;
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getStartupTime() {
		return new Date(startupTime);
	}

	/**
	 * {@inheritDoc}
	 */
	public long getFreeMemory() {
//		ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();//TODO
//		ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();//TODO
		return Runtime.getRuntime().freeMemory();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getTotalMemory() {
		return Runtime.getRuntime().totalMemory();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getMaxMemory() {
		return Runtime.getRuntime().maxMemory();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getAvailableProcessors() {
		return Runtime.getRuntime().availableProcessors();
	}

	/** {@inheritDoc} */
	public Map/*<String,String>*/ getSystemProperties() {
		return System.getProperties();
	}

	/** {@inheritDoc} */
	public double getSystemLoadAverage() {
		return JMX.getSystemLoadAverage();
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String,String> getSystemEnv() {
		return System.getenv();
	}

	/*
	public String getServerInfo() {
	}
	*/

	/**
	 * {@inheritDoc}
	 */
	public void gc() {
		System.gc();
	}
}
