/**
 * 
 */
package clime.messadmin.filter;

import java.util.Date;

/**
 * ThreadLocal containing some interesting data for internal usage.
 * An alternative would be to put data into the request, but this could look like pollution...
 * @author C&eacute;drik LIME
 */
public class MessAdminThreadLocal {
	private static final ThreadLocal/*<Date>*/ startTime = new ThreadLocal/*<Date>*/();
	private static final ThreadLocal/*<Date>*/ stopTime = new ThreadLocal/*<Date>*/();

	/**
	 * 
	 */
	private MessAdminThreadLocal() {
		super();
	}

	public static void start() {
		startTime.set(new Date());
	}

	public static void stop() {
		stopTime.set(new Date());
	}

	public static void remove() {
		//startTime.remove();
		//stopTime.remove();
		startTime.set(null);
		stopTime.set(null);
	}

	public static Date getStartTime() {
		Date date = (Date) startTime.get();
		// Workaround for bug of BEA Weblogic 8.1.5 / Java 1.4.2_05 / XP
		return date != null ? date : new Date(0);
	}

	public static Date getStopTime() {
		Date date = (Date) stopTime.get();
		// Workaround for bug of BEA Weblogic 8.1.5 / Java 1.4.2_05 / XP
		return date != null ? date : new Date(0);
	}

	public static int getUsedTime() {
		try {
			return (int) (getStopTime().getTime() - getStartTime().getTime());
		} catch (NullPointerException npe) {
			// Workaround for bug of BEA Weblogic 8.1.5 / Java 1.4.2_05 / XP
			//e.printStackTrace();
			return 0;
		}
	}
}
