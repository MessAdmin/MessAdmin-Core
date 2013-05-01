/**
 * 
 */
package clime.messadmin.utils;

import javax.servlet.jsp.JspTagException;

import junit.framework.TestCase;

/**
 * @author C&eacute;drik LIME
 */
public class DateUtilsTest extends TestCase {
    /**
     * Number of milliseconds in a standard second: {@value}
     */
    public static final long MILLIS_PER_SECOND = 1000;
    /**
     * Number of milliseconds in a standard minute: {@value}
     */
    public static final long MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;
    /**
     * Number of milliseconds in a standard hour: {@value}
     */
    public static final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;
    /**
     * Number of milliseconds in a standard day: {@value}
     */
    public static final long MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR;

	public static void main(String[] args) {
		junit.textui.TestRunner.run(DateUtilsTest.class);
	}

	/**
	 * Constructor for DateUtilsTest.
	 * @param name
	 */
	public DateUtilsTest(String name) {
		super(name);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/**
	 * {@inheritDoc}
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * Test method for 'clime.messadmin.taglib.fmt.setValue(long)'
	 */
	public void testSetValueLong() throws JspTagException {
		long value = 3*MILLIS_PER_SECOND;
		String formated = DateUtils.timeIntervalToFormattedString(value);
		assertEquals("00:00:03", formated);

		value = 5*MILLIS_PER_MINUTE;
		formated = DateUtils.timeIntervalToFormattedString(value);
		assertEquals("00:05:00", formated);

		value = 7*MILLIS_PER_HOUR;
		formated = DateUtils.timeIntervalToFormattedString(value);
		assertEquals("07:00:00", formated);

		value = 2*MILLIS_PER_DAY;
		formated = DateUtils.timeIntervalToFormattedString(value);
		assertEquals("2 days, 00:00:00", formated);

		value = 1*MILLIS_PER_DAY
				+ 2*MILLIS_PER_HOUR
				+ 42*MILLIS_PER_MINUTE
				+ 8*MILLIS_PER_SECOND;
		formated = DateUtils.timeIntervalToFormattedString(value);
		assertEquals("1 day, 02:42:08", formated);

		value = 5*MILLIS_PER_DAY
				+ 2*MILLIS_PER_HOUR
				+ 42*MILLIS_PER_MINUTE
				+ 8*MILLIS_PER_SECOND;
		formated = DateUtils.timeIntervalToFormattedString(value);
		assertEquals("5 days, 02:42:08", formated);
	}

}
