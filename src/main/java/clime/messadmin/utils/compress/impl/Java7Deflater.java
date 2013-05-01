/**
 *
 */
package clime.messadmin.utils.compress.impl;

import java.lang.reflect.Method;
import java.util.zip.Deflater;

/**
 * Java 7 support for SYNC_FLUSH
 * Java 5 support for getBytesRead() and getBytesWritten()
 *
 * @author C&eacute;drik LIME
 */
public final class Java7Deflater {
	/**
	 * Compression flush mode used to achieve best compression result.
	 *
	 * @see Deflater#deflate(byte[], int, int, int)
	 * @since Java 1.7
	 */
	public static final int NO_FLUSH;// = 0;

	/**
	 * Compression flush mode used to flush out all pending output; may
	 * degrade compression for some compression algorithms.
	 *
	 * @see Deflater#deflate(byte[], int, int, int)
	 * @since Java 1.7
	 */
	public static final int SYNC_FLUSH;// = 2;

	/**
	 * Compression flush mode used to flush out all pending output and
	 * reset the deflater. Using this mode too often can seriously degrade
	 * compression.
	 *
	 * @see Deflater#deflate(byte[], int, int, int)
	 * @since Java 1.7
	 */
	public static final int FULL_FLUSH;// = 3;

	public static final boolean isEnhancedDeflateAvailable;

	private static final Method deflater_deflate;

	static {
		// @since Java 7
		int noFlush = 0;
		int syncFlush = 2;
		int fullFlush = 3;
		Method deflate = null;
		boolean available = false;
		try {
			noFlush = Deflater.class.getDeclaredField("NO_FLUSH").getInt(null);//$NON-NLS-1$
			syncFlush = Deflater.class.getDeclaredField("SYNC_FLUSH").getInt(null);//$NON-NLS-1$
			fullFlush = Deflater.class.getDeclaredField("FULL_FLUSH").getInt(null);//$NON-NLS-1$
			deflate = Deflater.class.getDeclaredMethod("deflate", byte[].class, int.class, int.class, int.class);//$NON-NLS-1$
			available = true;
		} catch (Throwable ignore) {
		}
		NO_FLUSH = noFlush;
		SYNC_FLUSH = syncFlush;
		FULL_FLUSH = fullFlush;
		deflater_deflate = deflate;
		isEnhancedDeflateAvailable = available;
	}

	private Java7Deflater() {
		assert false;
	}

	/**
	 * @see Deflater#deflate(byte[], int, int, int)
	 */
	public static int deflate(Deflater def, byte[] b, int off, int len, int flush) {
		if (b == null) {
			throw new NullPointerException();
		}
		if (off < 0 || len < 0 || off > b.length - len) {
			throw new ArrayIndexOutOfBoundsException();
		}
		if (flush == NO_FLUSH) {
			// default pre-Java 7
			return def.deflate(b, off, len);
		} else {
			// Java 7
			if (! isEnhancedDeflateAvailable) {
				throw new IllegalStateException("This method requires Java 7 for flush mode != NO_FLUSH");
			}
			try {
				Integer result = (Integer) deflater_deflate.invoke(def, new Object[] {b, Integer.valueOf(off), Integer.valueOf(len), Integer.valueOf(flush)});
				return result.intValue();
			} catch (Exception e) {
				throw (e instanceof RuntimeException) ? (RuntimeException)e : new RuntimeException(e);
			}
		}
	}
}
