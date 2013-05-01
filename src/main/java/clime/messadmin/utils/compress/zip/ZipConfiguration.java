/**
 *
 */
package clime.messadmin.utils.compress.zip;

import java.util.zip.Deflater;

import clime.messadmin.utils.compress.Statistics;
import clime.messadmin.utils.compress.impl.StatisticsImpl;

/**
 * @author C&eacute;drik LIME
 */
public class ZipConfiguration {
	private static final int MIN_BLOCK_SIZE = 32*1024;// 32k, size of LZ77 sliding window
	private static final int DEFAULT_BLOCK_SIZE = 128*1024;// 128k

	/**
	 * Limit memory usage to ~8 MB
	 */
	private int maxProcessors = 32;
	private int blockSize = DEFAULT_BLOCK_SIZE;
	private boolean independentCompressedBlocks = false;

	private int compressionLevel = Deflater.DEFAULT_COMPRESSION;
	private int compressionMethod = Deflater.DEFLATED;
	private CharSequence comment = null;
	private final Statistics statistics = new StatisticsImpl();

	public ZipConfiguration() {
		super();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder(32);
		str.append(getClass().getName()).append('[');
		str.append("maxProcessors=").append(getMaxProcessors()).append(',');
		str.append("blockSize=").append(getBlockSize()/1024.0).append("k");
		if (isIndependentCompressedBlocks()) {
			str.append(",independent");
		}
		if (getCompressionLevel() != Deflater.DEFAULT_COMPRESSION) {
			str.append(",compressionLevel=").append(getCompressionLevel());
		}
		str.append(']');
		return str.toString();
	}

	public Statistics getStatistics() {
		return statistics;
	}

	/**
	 * Enable or disable the gathering of time statistics.
	 * Disabled by default for maximum performance.
	 */
	public void setEnableTimeStatistics(boolean enableTimeStatistics) {
		((StatisticsImpl)statistics).enableTimeStatistics = enableTimeStatistics;
	}

	public int getBlockPoolSize() {
		return 1 + getMaxProcessors() * 2;
	}

	public int getBlockSize() {
		return blockSize;
	}
	public void setBlockSize(int blockSize) {
		if (blockSize < MIN_BLOCK_SIZE) {
			throw new IllegalArgumentException("Block size must be at least " + MIN_BLOCK_SIZE);
		}
		this.blockSize = blockSize;
	}

	public boolean isIndependentCompressedBlocks() {
		return independentCompressedBlocks;
	}
	public void setIndependentCompressedBlocks(
			boolean independentCompressedBlocks) {
		this.independentCompressedBlocks = independentCompressedBlocks;
	}

	public int getMaxProcessors() {
		return Math.min(maxProcessors, Runtime.getRuntime().availableProcessors());
	}
	public void setMaxProcessors(int maxProcessors) {
		if (maxProcessors < 1) {
			throw new IllegalArgumentException("" + maxProcessors);
		}
		this.maxProcessors = maxProcessors;
	}

	public int getCompressionLevel() {
		return compressionLevel;
	}
	public void setCompressionLevel(int compressionLevel) {
		this.compressionLevel = compressionLevel;
	}

	public int getCompressionMethod() {
		return compressionMethod;
	}
	public void setCompressionMethod(int compressionMethod) {
		this.compressionMethod = compressionMethod;
	}

	public CharSequence getComment() {
		return comment;
	}
	public void setComment(CharSequence comment) {
		this.comment = comment;
	}
}
