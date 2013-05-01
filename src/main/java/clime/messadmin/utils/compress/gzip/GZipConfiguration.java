/**
 *
 */
package clime.messadmin.utils.compress.gzip;

import java.io.File;

import clime.messadmin.utils.compress.zip.ZipConfiguration;

/**
 * @author C&eacute;drik LIME
 */
public class GZipConfiguration extends ZipConfiguration {
	private long modificationTime = 0;
	private CharSequence fileName = null;

	public GZipConfiguration() {
		super();
	}

	/**
	 * Convenience method.
	 * If you need to set the block size, call {@link #setBlockSize(int)} <em>before</em> this method!
	 * If you need to limit max processors, call {@link #setMaxProcessors(int)} <em>before</em> this method!
	 */
	public void setConfigurationParameters(File file) {
		setModificationTime(file.lastModified());
		setFileName(file.getName());
		// cap maxProcessors for small files
		setMaxProcessors(Math.max(1, (int) Math.min(getMaxProcessors(), (file.length() / getBlockSize()))));
	}

	public long getModificationTime() {
		return modificationTime;
	}
	public void setModificationTime(long modificationTime) {
		this.modificationTime = modificationTime;
	}

	public CharSequence getFileName() {
		return fileName;
	}
	public void setFileName(CharSequence fileName) {
		this.fileName = fileName;
	}
}
