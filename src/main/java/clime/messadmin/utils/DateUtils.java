/**
 *
 */
package clime.messadmin.utils;

import java.text.Format;
import java.util.Date;
import java.util.Locale;

import clime.messadmin.i18n.I18NSupport;

/**
 * @author C&eacute;drik LIME
 */
public abstract class DateUtils {
	private static final String BUNDLE_NAME = DateUtils.class.getName();

	/**
	 * RFC 5322 datetime format: {@value}
	 * @see <a href="http://tools.ietf.org/html/rfc5322">RFC 5322</a>
	 */
	public static final String RFC2822_DATE_TIME = "EEE, d MMM yyyy HH:mm:ss Z";//$NON-NLS-1$
	public static final Format RFC2822_DATE_TIME_FORMAT = FastDateFormat.getInstance(RFC2822_DATE_TIME, Locale.US);

	/**
	 * Default ISO 8601 datetime format: {@value}
	 * @see <a href="http://www.w3.org/TR/NOTE-datetime">ISO 8601 DateTime</a>
	 * @see <a href="http://tools.ietf.org/html/rfc3339">RFC 3339</a>
	 */
	public static final String ISO8601_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZ";//$NON-NLS-1$
	public static final String ISO8601_DATE_FORMAT = "yyyy-MM-dd";//$NON-NLS-1$
	public static final String ISO8601_TIME_FORMAT = "HH:mm:ssZ";//$NON-NLS-1$

	/**
	 * Modified ISO 8601 datetime format for readability: {@value}
	 */
	public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";//$NON-NLS-1$
	public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";//$NON-NLS-1$
	public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";//$NON-NLS-1$


	private DateUtils() {
		super();
	}

	public static String formatRFC2822Date(long date) {
//		DateFormat format = new SimpleDateFormat(RFC2822_DATE_TIME, Locale.US);
//		return format.format(new Date(date));
		return RFC2822_DATE_TIME_FORMAT.format(new Date(date));
	}

	public static String dateToFormattedDateTimeString(long value, String format) {
		String lc_format = format;
		if (null == lc_format) {
			lc_format = DEFAULT_DATE_TIME_FORMAT;
		}
		// This is what we would need to do if not using FastDateFormat's "ZZ" pattern...
//		DateFormat formatter = new SimpleDateFormat(lc_format);
//		String dateStr = formatter.format(new Date(value));
//		if (ISO8601_DATE_TIME_FORMAT.equals(lc_format)) {
//			// hack to be 100% W3/RFC3399 compliant: convert '+0800' timezone to '+08:00', not touching 'Z'
//			// we could skip this by using the 'ZZ' pattern of commons-lang FastDateformat
//			int endPos = dateStr.length() - 1;
//			char plusMinus = dateStr.charAt(endPos-4);
//			if (dateStr.charAt(endPos) != 'Z' && dateStr.charAt(endPos-2) != ':' && (plusMinus == '+' || plusMinus == '-')) {
//				dateStr = dateStr.substring(0, dateStr.length()-2) + ':' + dateStr.substring(dateStr.length()-2);
//			}
//		}
//		return dateStr;
		Format formatter = FastDateFormat.getInstance(lc_format);
		return formatter.format(new Date(value));
	}

	/*
	 * Can't use new Date(value) with formatter... :-(
	 */
	public static String timeIntervalToFormattedString(int in_milliseconds) {
		StringBuilder buff = new StringBuilder(10);
		if (in_milliseconds < 0) {
			buff.append('-');
			in_milliseconds = -in_milliseconds;
		}
		long rest = in_milliseconds / 1000; // seconds
		long hour = rest / 3600;
		rest = rest % 3600;
		long minute = rest / 60;
		rest = rest % 60;
		long second = rest;
		if (hour < 10) {
			buff.append('0');
		}
		buff.append(hour);
		buff.append(':');
		if (minute < 10) {
			buff.append('0');
		}
		buff.append(minute);
		buff.append(':');
		if (second < 10) {
			buff.append('0');
		}
		buff.append(second);
		return buff.toString();
	}
	public static String timeIntervalToFormattedString(long in_milliseconds) {
		StringBuilder buff = new StringBuilder(10);
		if (in_milliseconds < 0) {
			buff.append('-');
			in_milliseconds = -in_milliseconds;
		}
		int millis = (int) (in_milliseconds % 1000);
		long rest = in_milliseconds / 1000; // seconds
		long hour = rest / 3600;
		rest = rest % 3600;
		long minute = rest / 60;
		rest = rest % 60;
		long second = rest;
		if (hour >= 24) {
			buff.append(I18NSupport.getLocalizedMessage(BUNDLE_NAME, "days", new Object[] {Long.valueOf(hour/24)}));//$NON-NLS-1$
			hour = hour % 24;
		}
		if (hour < 10) {
			buff.append('0');
		}
		buff.append(hour);
		buff.append(':');
		if (minute < 10) {
			buff.append('0');
		}
		buff.append(minute);
		buff.append(':');
		if (second < 10) {
			buff.append('0');
		}
		buff.append(second);
		return buff.toString();
	}
}
