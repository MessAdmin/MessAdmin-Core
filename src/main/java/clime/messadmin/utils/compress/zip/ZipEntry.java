/**
 * 
 */
package clime.messadmin.utils.compress.zip;

import java.lang.reflect.Field;

/**
 * Fix Java <= 6 ZipEntry to accept > 4GB file sizes (ZIP64)
 * @author C&eacute;drik LIME
 */
public class ZipEntry extends java.util.zip.ZipEntry {
	private static final Field superSize;

	static {
		try {
			superSize = java.util.zip.ZipEntry.class.getDeclaredField("size");//$NON-NLS-1$
			superSize.setAccessible(true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param name
	 */
	public ZipEntry(String name) {
		super(name);
	}

	/**
	 * @param e
	 */
	public ZipEntry(ZipEntry e) {
		super(e);
	}

	/**
     * Sets the uncompressed size of the entry data.
     * @param size the uncompressed size in bytes
     * @exception IllegalArgumentException if the specified size is less
     *            than 0, is greater than 0xFFFFFFFF when
     *            <a href="package-summary.html#zip64">ZIP64 format</a> is not supported,
     *            or is less than 0 when ZIP64 is supported
     * @see #getSize()
     */
	@Override
	public void setSize(long size) {
		//super.setSize(size);
		if (size < 0) {
			throw new IllegalArgumentException("invalid entry size: " + size);//$NON-NLS-1$
		}
		//super.size = size;
		try {
			superSize.set(this, Long.valueOf(size));
		} catch (Exception e) {
			super.setSize(size);
		}
	}
}
