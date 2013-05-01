/**
 * 
 */
package clime.messadmin.utils;

/**
 * Use Integer.valueOf() with Java < 5
 * @author C&eacute;drik LIME
 */
public final class Integers {

	private Integers() {
	}

	private static final int cache_low = -128;
	private static final int cache_high = 127;
	private static final Integer cache[] = new Integer[(cache_high - cache_low) + 1];

	static {
		for(int i = 0; i < cache.length; ++i) {
			cache[i] = new Integer(i + cache_low);
		}
	}

	/**
	 * Returns a <tt>Integer</tt> instance representing the specified
	 * <tt>int</tt> value.
	 * If a new <tt>Integer</tt> instance is not required, this method
	 * should generally be used in preference to the constructor
	 * {@link #Integer(int)}, as this method is likely to yield
	 * significantly better space and time performance by caching
	 * frequently requested values.
	 *
	 * @param  i an <code>int</code> value.
	 * @return a <tt>Integer</tt> instance representing <tt>i</tt>.
	 * @since  1.5
	 */
	public static Integer valueOf(int i) {
		if (i >= cache_low && i <= cache_high) { // must cache
			return cache[i - cache_low];
		} else {
			return new Integer(i);
		}
	}
}
