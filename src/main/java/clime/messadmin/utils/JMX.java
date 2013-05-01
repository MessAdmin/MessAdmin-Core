/**
 *
 */
package clime.messadmin.utils;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.util.List;

/**
 * JMX utilities for Java < 6
 * @author C&eacute;drik LIME
 */
public final class JMX {
	private static transient Method OperatingSystemMXBean_getSystemLoadAverage;
	private static transient Method OperatingSystemMXBean_getOpenFileDescriptorCount;
	private static transient Method OperatingSystemMXBean_getMaxFileDescriptorCount;

	private static transient Method ThreadMXBean_isSynchronizerUsageSupported;
	private static transient Method ThreadMXBean_findDeadlockedThreads;

	private static transient Method GarbageCollectorMXBean_getName;
	private static transient Method GarbageCollectorMXBean_isValid;
	private static transient Method GarbageCollectorMXBean_getCollectionCount;
	private static transient Method GarbageCollectorMXBean_getCollectionTime;

	static {
		// @since Java 5
		try {
//			Class ManagementFactory;
//			Class ClassLoadingMXBean;
//			Class MemoryMXBean;
//			Class ThreadMXBean;
//			Class RuntimeMXBean;
//			Class OperatingSystemMXBean;
//			Class CompilationMXBean;
//			Class GarbageCollectorMXBean;
			GarbageCollectorMXBean_getName            = GarbageCollectorMXBean.class.getMethod("getName");//$NON-NLS-1$
			GarbageCollectorMXBean_isValid            = GarbageCollectorMXBean.class.getMethod("isValid");//$NON-NLS-1$
			GarbageCollectorMXBean_getCollectionCount = GarbageCollectorMXBean.class.getMethod("getCollectionCount");//$NON-NLS-1$
			GarbageCollectorMXBean_getCollectionTime  = GarbageCollectorMXBean.class.getMethod("getCollectionTime");//$NON-NLS-1$
//			Class MemoryManagerMXBean;
//			Class MemoryPoolMXBean;
		} catch (LinkageError e) {
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		}
		// @since Java 6
		try {
//			Class ThreadMXBean;
			ThreadMXBean_isSynchronizerUsageSupported = ThreadMXBean.class.getMethod("isSynchronizerUsageSupported");//$NON-NLS-1$
			ThreadMXBean_findDeadlockedThreads = ThreadMXBean.class.getMethod("findDeadlockedThreads");//$NON-NLS-1$
//			Class OperatingSystemMXBean;
			OperatingSystemMXBean_getSystemLoadAverage = OperatingSystemMXBean.class.getMethod("getSystemLoadAverage");//$NON-NLS-1$
		} catch (LinkageError e) {
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		}
		try {
			Class UnixOperatingSystemMXBean = Class.forName("com.sun.management.UnixOperatingSystemMXBean");//$NON-NLS-1$
			OperatingSystemMXBean_getOpenFileDescriptorCount = UnixOperatingSystemMXBean.getMethod("getOpenFileDescriptorCount");//$NON-NLS-1$
			OperatingSystemMXBean_getMaxFileDescriptorCount = UnixOperatingSystemMXBean.getMethod("getMaxFileDescriptorCount");//$NON-NLS-1$
		} catch (LinkageError e) {
		} catch (SecurityException e) {
		} catch (ClassNotFoundException e) {
		} catch (NoSuchMethodException e) {
		}
		// initialize
		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		if (threadMXBean.isThreadContentionMonitoringSupported() && ! threadMXBean.isThreadContentionMonitoringEnabled()) {
			threadMXBean.setThreadContentionMonitoringEnabled(true);
		}
	}

	/**
	 *
	 */
	private JMX() {
		throw new AssertionError();
	}

	/**
	 * Returns the system load average for the last minute.
	 * The system load average is the sum of the number of runnable entities
	 * queued to the {@link Runtime#availableProcessors() available processors}
	 * and the number of runnable entities running on the available processors
	 * averaged over a period of time.
	 * The way in which the load average is calculated is operating system
	 * specific but is typically a damped time-dependent average.
	 * <p>
	 * If the load average is not available, a negative value is returned.
	 * <p>
	 * This method is designed to provide a hint about the system load
	 * and may be queried frequently.
	 * The load average may be unavailable on some platform where it is
	 * expensive to implement this method.
	 *
	 * @return the system load average; or a negative value if not available.
	 *
	 * @see OperatingSystemMXBean#getSystemLoadAverage
	 * @since Java 6
	 */
	public static double getSystemLoadAverage() {
		// return ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
		OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
		return ((Double)invoke(OperatingSystemMXBean_getSystemLoadAverage, operatingSystemMXBean, new Double(-1))).doubleValue();
	}

	/**
	 * Finds cycles of threads that are in deadlock waiting to acquire
	 * object monitors or
	 * {@link java.lang.management.LockInfo#OwnableSynchronizer ownable synchronizers}.
	 *
	 * Threads are <em>deadlocked</em> in a cycle waiting for a lock of
	 * these two types if each thread owns one lock while
	 * trying to acquire another lock already held
	 * by another thread in the cycle.
	 * <p>
	 * This method is designed for troubleshooting use, but not for
	 * synchronization control.  It might be an expensive operation.
	 *
	 * @return an array of IDs of the threads that are
	 * deadlocked waiting for object monitors or ownable synchronizers, if any;
	 * {@code null} otherwise.
	 *
	 * @throws java.lang.SecurityException if a security manager
	 *	     exists and the caller does not have
	 *	     ManagementPermission("monitor").
	 * @throws java.lang.UnsupportedOperationException if the Java virtual
	 * machine does not support monitoriing of ownable synchronizer usage.
	 *
	 * @see ThreadMXBean#isThreadContentionMonitoringSupported
	 * @see ThreadMXBean#isThreadContentionMonitoringEnabled
	 * @see ThreadMXBean#isSynchronizerUsageSupported
	 * @see ThreadMXBean#findMonitorDeadlockedThreads
	 * @see ThreadMXBean#findDeadlockedThreads
	 * @since Java 5
	 */
	public static long[] findDeadlockedThreadsIDs() {
		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		boolean isThreadContentionMonitoringSupported = threadMXBean.isThreadContentionMonitoringSupported();
		if (! isThreadContentionMonitoringSupported) {
			return new long[0];
		}
		long[] result;
		boolean isSynchronizerUsageSupported = invokeBoolean(ThreadMXBean_isSynchronizerUsageSupported, threadMXBean, false);
		if (isSynchronizerUsageSupported) { // Java 6
			result = (long[]) invoke(ThreadMXBean_findDeadlockedThreads, threadMXBean, null);
		} else { // Java 5
			result = threadMXBean.findMonitorDeadlockedThreads();
		}
		if (result == null) {
			result = new long[0];
		}
		return result;
	}

	/**
	 * Returns the total number of collections that have occurred.
	 * This method returns {@code -1} if the collection count is undefined.
	  *
	 * @return the total number of collections that have occurred.
	 * @see GarbageCollectorMXBean
	 * @since Java 5
	*/
	public static long getGCCollectionCount() {
		return getGCTotal(GarbageCollectorMXBean_getCollectionCount);
	}

	/**
	 * Returns the approximate accumulated collection elapsed time
	 * in milliseconds.  This method returns {@code -1} if the collection
	 * elapsed time is undefined.
	 * <p>
	 * The Java virtual machine implementation may use a high resolution
	 * timer to measure the elapsed time.  This method may return the
	 * same value even if the collection count has been incremented
	 * if the collection elapsed time is very short.
	 *
	 * @return the approximate accumulated collection elapsed time in milliseconds.
	 * @see GarbageCollectorMXBean
	 * @since Java 5
	 */
	public static long getGCCollectionTime() {
		return getGCTotal(GarbageCollectorMXBean_getCollectionTime);
	}

	private static long getGCTotal(Method method) {
		List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
		if (garbageCollectorMXBeans == null) {
			return -1;
		}
		long total = 0;
		for (GarbageCollectorMXBean garbageCollectorMXBean : garbageCollectorMXBeans) {
			long count = invokeLong(method, garbageCollectorMXBean, -1);
			if (count > 0) {
				total += count;
			}
		}
		return total;
	}

	/**
	 * Returns the number of open file descriptors.
	 *
	 * @return the number of open file descriptors.
	 */
	public long getOpenFileDescriptorCount() {
		OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
		return ((Long)invoke(OperatingSystemMXBean_getOpenFileDescriptorCount, operatingSystemMXBean, Long.valueOf(-1))).longValue();
	}

	/**
	 * Returns the maximum number of file descriptors.
	 *
	 * @return the maximum number of file descriptors.
	 */
	public long getMaxFileDescriptorCount() {
		OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
		return ((Long)invoke(OperatingSystemMXBean_getMaxFileDescriptorCount, operatingSystemMXBean, Long.valueOf(-1))).longValue();
	}


	// Utility methods

	private static Object invoke(Method method, Object delegate, Object defaultValue) {
		if (method == null) {
			return defaultValue;
		}
		try {
			return method.invoke(delegate);
		} catch (Exception ignore) {
			return defaultValue;
		}
	}

	private static long invokeLong(Method method, Object delegate, long defaultValue) {
		return ((Long)invoke(method, delegate, Long.valueOf(defaultValue))).longValue();
	}

	private static boolean invokeBoolean(Method method, Object delegate, boolean defaultValue) {
		return ((Boolean)invoke(method, delegate, Boolean.valueOf(defaultValue))).booleanValue();
	}


	public static void main(String[] args) {
		double systemLoadAverage = getSystemLoadAverage();
		System.out.println("getSystemLoadAverage(): " + systemLoadAverage);
		long[] deadlockedThreads = findDeadlockedThreadsIDs();
		if (deadlockedThreads == null) {
			deadlockedThreads = new long[0];
		}
		System.out.println("findDeadlockedThreads(): " + deadlockedThreads.length);
		for (int i = 0; i < deadlockedThreads.length; i++) {
			long t = deadlockedThreads[i];
			System.out.println("\t" + t);
		}
		System.gc();
		System.out.println("getGCCollectionCount(): " + getGCCollectionCount());
		System.out.println("getGCCollectionTime(): " + getGCCollectionTime() + "ms");
	}
}
