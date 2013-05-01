/**
 *
 */
package clime.messadmin.utils.compress;

/**
 * A note on times: due to thread scheduling, times may be slightly inaccurate (higher than real)
 * @author C&eacute;drik LIME
 */
public interface Statistics {

	/**
	 * @return wall (clock) time in milliseconds
	 */
	long getRealTime();

	/**
	 * @return used CPU time in milliseconds. Same as <code>{@link #getReadTime()} + {@link #getChecksumTime()} + {@link #getCompressionTime()} + {@link #getWriteTime()}</code>
	 */
	long getCpuTime();

	/**
	 * @return checksum time in milliseconds
	 */
	long getChecksumTime();

	/**
	 * @return compression time in milliseconds
	 */
	long getCompressionTime();

	/**
	 * @return read time in milliseconds
	 */
	long getReadTime();

	/**
	 * @return write time in milliseconds
	 */
	long getWriteTime();

	long getUncompressedSize();

	long getCompressedSize();
}
