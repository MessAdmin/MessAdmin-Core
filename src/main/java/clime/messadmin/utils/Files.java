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

	static {
		// @since 1.6
		try {
			getUsableSpace = File.class.getMethod("getUsableSpace");//$NON-NLS-1$
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
			Object usableSpace = getUsableSpace.invoke(file);
			return ((Long) usableSpace).longValue();
		} catch (Exception e) {
			return -1;
		}
	}
}
