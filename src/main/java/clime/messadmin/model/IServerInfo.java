/**
 *
 */
package clime.messadmin.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author C&eacute;drik LIME
 */
public interface IServerInfo {

	/**
	 * @return server-specific data (user plugin)
	 */
	List/*<DisplayDataHolder>*/ getServerSpecificData();

	/**
	 * @return name of this server (as is {@code java.net.InetAddress.getLocalHost().getHostName()})
	 */
	public String getServerName();

	/**
	 * @return startup time of this server
	 */
	Date getStartupTime();

	/**
	 * @see Runtime#freeMemory()
	 */
	long getFreeMemory();

	/**
	 * @see Runtime#totalMemory()
	 */
	long getTotalMemory();

	/**
	 * @since 1.4
	 * @see Runtime#maxMemory()
	 */
	long getMaxMemory();

	/**
	 * @since 1.4
	 * @see Runtime#availableProcessors()
	 */
	int getAvailableProcessors();

	/**
	 * @return the system load average for the last minute (OS-specific, value in relation with {@link #getAvailableProcessors()})
	 * @since 1.6
	 * @see OperatingSystemMXBean#getSystemLoadAverage
	 */
	double getSystemLoadAverage();

	Map/*<String,String>*/ getSystemProperties();

	/**
	 * @since 1.5
	 * @see System#getenv()
	 */
	Map<String,String> getSystemEnv();

	/*
	String getServerInfo();
	*/

	/**
	 * @see System#gc()
	 */
	void gc();
}
