/**
 *
 */
package clime.messadmin.utils;

import java.io.File;
import java.lang.reflect.Method;

/**
 * Access Java 6 File.getUsableSpace() with no Java 6 dependency
 * @author C&eacute;drik LIME
 */
public final class Files {
	private static transient Method getUsableSpace = null;
	private static transient Method getTotalSpace = null;

	static {
		// @since 1.6
		try {
			getUsableSpace = File.class.getMethod("getUsableSpace");//$NON-NLS-1$
			getTotalSpace = File.class.getMethod("getTotalSpace");//$NON-NLS-1$
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		}
	}

	private Files() {
	}

	/**
	 * @return number of bytes available on the partition and includes checks for write permissions and other operating system restrictions
	 * @since 1.6
	 * @see File#getUsableSpace()
	 */
	public static long getUsableSpaceForFile(String fileName) {
		File file = new File(fileName);
		return getUsableSpaceForFile(file);
	}

	/**
	 * @return number of bytes available on the partition and includes checks for write permissions and other operating system restrictions
	 * @since 1.6
	 * @see File#getUsableSpace()
	 */
	public static long getUsableSpaceForFile(File file) {
		if (getUsableSpace == null || file == null || !file.exists()) {
			return -1;
		}
		//return file.getUsableSpace();
		try {
			long usableSpace = ((Long)getUsableSpace.invoke(file)).longValue();
			if (usableSpace == 0) {
				usableSpace = -1;
			}
			return usableSpace;
		} catch (Exception e) {
			return -1;
		}
	}

	/**
	 * @return [0..1]
	 * @since 1.6
	 * @see File#getUsableSpace()
	 * @see File#getTotalSpace()
	 */
	public static double getUsableSpacePercentForFile(File file) {
		if (getUsableSpace == null || getTotalSpace == null || file == null || !file.exists()) {
			return -1;
		}
		//return file.getUsableSpace() / (double)file.getTotalSpace();
		try {
			long usableSpace = ((Long)getUsableSpace.invoke(file)).longValue();
			long totalSpace = ((Long)getTotalSpace.invoke(file)).longValue();
			return usableSpace / (double)totalSpace;
		} catch (Exception e) {
			return -1;
		}
	}
}
