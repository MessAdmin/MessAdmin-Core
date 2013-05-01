/**
 *
 */
package clime.messadmin.model;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import clime.messadmin.providers.ProviderUtils;
import clime.messadmin.providers.spi.ServerDataProvider;
import clime.messadmin.utils.JMX;

/**
 * @author C&eacute;drik LIME
 */
public class ServerInfo implements Serializable, IServerInfo {
	private static transient Method getenv = null;

	protected final long startupTime = System.currentTimeMillis();
	private static String localHostStr = "";
	private final static String localHostDefaultName = "localhost";//$NON-NLS-1$

	static {
		// @since 1.5
		try {
			getenv = System.class.getMethod("getenv", null);//$NON-NLS-1$
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		}
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
	 *
	 */
	public ServerInfo() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	public List getServerSpecificData() {
		Iterator iter = ProviderUtils.getProviders(ServerDataProvider.class).iterator();
		List result = new ArrayList();
		while (iter.hasNext()) {
			ServerDataProvider sd = (ServerDataProvider) iter.next();
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
	public Map/*<String,String>*/ getSystemEnv() {
		//return System.getenv();
		if (getenv != null) {
			try {
				Object systemEnv = getenv.invoke(null, null);
				return (Map) systemEnv;
			} catch (Exception e) {
				return Collections.EMPTY_MAP;
			}
		} else {
			return Collections.EMPTY_MAP;
		}
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
