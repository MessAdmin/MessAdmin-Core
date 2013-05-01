/**
 *
 */
package clime.messadmin.utils.compress.impl;

import clime.messadmin.utils.backport.com.jcraft.jzlib.JZlib;
import clime.messadmin.utils.backport.com.jcraft.jzlib.ZStream;
import clime.messadmin.utils.compress.zip.ZipConfiguration;

/**
 * @author C&eacute;drik LIME
 */
class CompressTaskJZlib implements Runnable {
//	protected static Logger log = LoggerFactory.getLogger(CompressTaskJZlib.class);
	private static final int DICTIONARY_SIZE = 32*1024;// 32k
	/**
	 * Output buffer size for writing compressed data.
	 */
	protected static final int STRIDE = 8192;

	protected final ZipConfiguration configuration;

	protected final byte[] buffer = new byte[STRIDE];
	protected final Block block;
	/**
	 * Compressor for this stream.
	 */
	protected final ZStream z;

	/**
	 *
	 */
	public CompressTaskJZlib(Block block, Block previousBlock, ZipConfiguration configuration) {
		long startNanoTime = ((StatisticsImpl)configuration.getStatistics()).nanoTime();
		this.configuration = configuration;
		this.block = block;
		z = new ZStream();
		int err = z.deflateInit(configuration.getCompressionLevel(), true);
		if (err != JZlib.Z_OK) {
			throw new RuntimeException("ZStream deflateInit: " + z.msg);
		}
		if (previousBlock != null) {
			if (! configuration.isIndependentCompressedBlocks()) {
				err = z.deflateSetDictionary(previousBlock.getUncompressed(), previousBlock.getUncompressedSize());// JZlib will use the tail of the dictionary
				if (err != JZlib.Z_OK) {
					throw new RuntimeException("ZStream deflateSetDictionary: " + z.msg);
				}
			}
			previousBlock.dictionaryDone();
		}
		long endNanoTime = ((StatisticsImpl)configuration.getStatistics()).nanoTime();
		((StatisticsImpl)configuration.getStatistics()).compressionTimeNano.addAndGet(endNanoTime - startNanoTime);
//		log.trace("new CompressTask() for block #{}", Integer.valueOf(block.blockNumber));
	}

	/** {@inheritDoc} */
	public void run() {
//		log.debug("Compressing block #{}", Integer.valueOf(block.blockNumber));
		long startNanoTime = ((StatisticsImpl)configuration.getStatistics()).nanoTime();
		z.next_in = block.getUncompressed();
		z.next_in_index = 0;
		z.avail_in = block.getUncompressedSize();
		z.avail_out = STRIDE;
		while (z.avail_in>0 || z.avail_out==0) {
			z.next_out = buffer;
			z.next_out_index = 0;
			z.avail_out = STRIDE;
			int err = z.deflate(JZlib.Z_NO_FLUSH);
			if (err!=JZlib.Z_OK) {
				throw new RuntimeException("ZStream deflating: " + z.msg);
			}
			block.writeCompressed(buffer, 0, STRIDE-z.avail_out);
		}
		// finish
		do {
			z.next_out = buffer;
			z.next_out_index = 0;
			z.avail_out = STRIDE;
			int err = z.deflate(block.isLastBlock() ? JZlib.Z_FINISH : JZlib.Z_SYNC_FLUSH);
			if (err!=JZlib.Z_STREAM_END && err != JZlib.Z_OK) {
				throw new RuntimeException("ZStream deflating (finish): " + z.msg);
			}
			if (STRIDE-z.avail_out>0) {
				block.writeCompressed(buffer, 0, STRIDE-z.avail_out);
			}
		} while (z.avail_in>0 || z.avail_out==0);
		assert z.total_in == block.getUncompressedSize();
		int err = z.deflateEnd();
		// do not check return value here, as Z_SYNC_FLUSH means the deflater's state is still BUSY_STATE
		// This is normal, since the stream is not Z_FINISH yet!
//		if (err!=JZlib.Z_OK) {
//			throw new RuntimeException("ZStream deflateEnd: " + z.msg);
//		}
		z.free();

		long endNanoTime = ((StatisticsImpl)configuration.getStatistics()).nanoTime();
		int compressedSize = block.compressionDone();
		((StatisticsImpl)configuration.getStatistics()).compressedSize.addAndGet(compressedSize);
		((StatisticsImpl)configuration.getStatistics()).compressionTimeNano.addAndGet(endNanoTime - startNanoTime);
	}

}
