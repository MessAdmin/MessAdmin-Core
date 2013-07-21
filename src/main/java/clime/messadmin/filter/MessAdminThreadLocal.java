/**
 * 
 */
package clime.messadmin.filter;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import clime.messadmin.model.HttpServletRequestInfo;

/**
 * ThreadLocal containing some interesting data for internal usage.
 * An alternative would be to put data into the request, but this could look like pollution...
 * @author C&eacute;drik LIME
 */
public class MessAdminThreadLocal {
	private static final ThreadLocal<Date> startTime = new ThreadLocal<Date>();
	private static final ThreadLocal<Date> stopTime = new ThreadLocal<Date>();
	private static final ThreadLocal<Long> startTimeNano = new ThreadLocal<Long>();
	private static final ThreadLocal<Long> stopTimeNano = new ThreadLocal<Long>();
	private static final ThreadLocal<HttpServletRequestInfo> currentRequest = new ThreadLocal<HttpServletRequestInfo>();

	/**
	 * 
	 */
	private MessAdminThreadLocal() {
		super();
	}

	public static void start(HttpServletRequest request) {
		startTime.set(new Date());
		startTimeNano.set(System.nanoTime());
		currentRequest.set(new HttpServletRequestInfo(request)); // leave this line /after/ startTime*
	}

	public static void stop() {
		stopTime.set(new Date());
		stopTimeNano.set(System.nanoTime());
	}

	public static void remove() {
		startTime.remove();
		stopTime.remove();
		startTimeNano.remove();
		stopTimeNano.remove();
		currentRequest.remove();
	}

	public static Date getStartTime() {
		Date date = startTime.get();
		// Workaround for bug of BEA Weblogic 8.1.5 / Java 1.4.2_05 / XP
		return date != null ? date : new Date(0);
	}

	public static long getStartTimeNano() {
		return startTimeNano.get();
	}
	
	public static Date getStopTime() {
		Date date = stopTime.get();
		// Workaround for bug of BEA Weblogic 8.1.5 / Java 1.4.2_05 / XP
		return date != null ? date : new Date(0);
	}

	/**
	 * @return this request's wall clock time, in milliseconds
	 */
	public static int getUsedTime() {
		try {
			//return (int) (getStopTime().getTime() - getStartTime().getTime()); // NTP is prone to time correction, so don't use System.currentTimeMillis()
			long diffNano = stopTimeNano.get() - startTimeNano.get();
			return (int) NANOSECONDS.toMillis(diffNano);
		} catch (NullPointerException npe) {
			// Workaround for bug of BEA Weblogic 8.1.5 / Java 1.4.2_05 / XP
			//e.printStackTrace();
			return 0;
		}
	}

	public static HttpServletRequestInfo getCurrentRequestInfo() {
		return currentRequest.get();
	}
}
