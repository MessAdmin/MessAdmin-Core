/**
 *
 */
package clime.messadmin.utils.compress.impl;

import java.util.zip.Checksum;

import clime.messadmin.utils.compress.zip.ZipConfiguration;

/**
 * @author C&eacute;drik LIME
 */
public class ChecksumTask extends AbstractSerialExecutor implements Runnable {

	/**
	 * Checksum of uncompressed data.
	 */
	protected Checksum checksum;

	public ChecksumTask(Checksum checksum, ZipConfiguration configuration) {
		super(configuration);
		this.checksum = checksum;
		checksum.reset();
	}

	/** {@inheritDoc} */
//	@Override
	protected void process(Block block) {
		long startNanoTime = ((StatisticsImpl)configuration.getStatistics()).nanoTime();
		checksum.update(block.getUncompressed(), 0, block.getUncompressedSize());
		block.checksumDone();
		long endNanoTime = ((StatisticsImpl)configuration.getStatistics()).nanoTime();
		((StatisticsImpl)configuration.getStatistics()).checksumTimeNano.addAndGet(endNanoTime - startNanoTime);
	}

	/**
	 * @see Checksum#getValue()
	 */
	public long getCheckumValue() {
		long result = checksum.getValue();
//		log.debug("Checksum value: {}", Long.valueOf(result));
		return result;
	}

	/**
	 * @see Checksum#reset()
	 */
	public void reset() {
		checksum.reset();
	}
}
