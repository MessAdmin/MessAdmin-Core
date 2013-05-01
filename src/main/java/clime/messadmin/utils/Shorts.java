/**
 * 
 */
package clime.messadmin.utils;

/**
 * Use Short.valueOf() with Java < 5
 * @author C&eacute;drik LIME
 */
public final class Shorts {

	private Shorts() {
	}

	private static final int cache_low = -128;
	private static final int cache_high = 127;
	private static final Short cache[] = new Short[(cache_high - cache_low) + 1];

	static {
		for(int i = 0; i < cache.length; ++i) {
			cache[i] = new Short((short)(i + cache_low));
		}
	}

	/**
	 * Returns a <tt>Short</tt> instance representing the specified
	 * <tt>short</tt> value.
	 * If a new <tt>Short</tt> instance is not required, this method
	 * should generally be used in preference to the constructor
	 * {@link #Short(short)}, as this method is likely to yield
	 * significantly better space and time performance by caching
	 * frequently requested values.
	 *
	 * @param  s a short value.
	 * @return a <tt>Short</tt> instance representing <tt>s</tt>.
	 * @since  1.5
	 */
	public static Short valueOf(short s) {
		if (s >= cache_low && s <= cache_high) { // must cache
			return cache[s - cache_low];
		} else {
			return new Short(s);
		}
	}
}
