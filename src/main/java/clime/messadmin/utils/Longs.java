/**
 * 
 */
package clime.messadmin.utils;

/**
 * Use Long.valueOf() with Java < 5
 * @author C&eacute;drik LIME
 */
public final class Longs {

	private Longs() {
	}

	private static final int cache_low = -128;
	private static final int cache_high = 127;
	private static final Long cache[] = new Long[(cache_high - cache_low) + 1];

	static {
		for(int i = 0; i < cache.length; ++i) {
			cache[i] = new Long(i + cache_low);
		}
	}

	/**
	 * Returns a <tt>Long</tt> instance representing the specified
	 * <tt>long</tt> value.
	 * If a new <tt>Long</tt> instance is not required, this method
	 * should generally be used in preference to the constructor
	 * {@link #Long(long)}, as this method is likely to yield
	 * significantly better space and time performance by caching
	 * frequently requested values.
	 *
	 * @param  l a long value.
	 * @return a <tt>Long</tt> instance representing <tt>l</tt>.
	 * @since  1.5
	 */
	public static Long valueOf(long l) {
		if (l >= cache_low && l <= cache_high) { // must cache
			return cache[(int)l - cache_low];
		} else {
			return new Long(l);
		}
	}
}
