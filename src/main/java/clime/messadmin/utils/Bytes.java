/**
 * 
 */
package clime.messadmin.utils;

/**
 * Use Byte.valueOf() with Java < 5
 * @author C&eacute;drik LIME
 */
public final class Bytes {

	private Bytes() {
	}

	private static final int cache_low = Byte.MIN_VALUE;
	private static final int cache_high = Byte.MAX_VALUE;
	private static final Byte cache[] = new Byte[(cache_high - cache_low) + 1];

	static {
		for(int i = 0; i < cache.length; ++i) {
			cache[i] = new Byte((byte)(i + cache_low));
		}
	}

	/**
	 * Returns a <tt>Byte</tt> instance representing the specified
	 * <tt>byte</tt> value.
	 * If a new <tt>Byte</tt> instance is not required, this method
	 * should generally be used in preference to the constructor
	 * {@link #Byte(byte)}, as this method is likely to yield
	 * significantly better space and time performance by caching
	 * frequently requested values.
	 *
	 * @param  b a byte value.
	 * @return a <tt>Byte</tt> instance representing <tt>b</tt>.
	 * @since  1.5
	 */
	public static Byte valueOf(byte b) {
		if (b >= cache_low && b <= cache_high) { // must cache
			return cache[b - cache_low];
		} else {
			return new Byte(b);
		}
	}
}
