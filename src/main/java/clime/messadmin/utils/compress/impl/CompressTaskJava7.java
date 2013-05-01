/**
 *
 */
package clime.messadmin.utils.compress.impl;

import java.util.zip.Deflater;

import clime.messadmin.utils.compress.zip.ZipConfiguration;

/**
 * @author C&eacute;drik LIME
 */
class CompressTaskJava7 implements Runnable {
//	protected static Logger log = LoggerFactory.getLogger(CompressTaskJava7.class);
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
	protected final Deflater def;

	/**
	 *
	 */
	public CompressTaskJava7(Block block, Block previousBlock, ZipConfiguration configuration) {
		long startNanoTime = ((StatisticsImpl)configuration.getStatistics()).nanoTime();
		this.configuration = configuration;
		this.block = block;
		def = new Deflater(configuration.getCompressionLevel(), true);
		if (previousBlock != null) {
			if (! configuration.isIndependentCompressedBlocks()) {
				def.setDictionary(previousBlock.getUncompressed(), previousBlock.getUncompressedSize() - DICTIONARY_SIZE, DICTIONARY_SIZE);
			}
			previousBlock.dictionaryDone();
		}
		long endNanoTime = ((StatisticsImpl)configuration.getStatistics()).nanoTime();
		((StatisticsImpl)configuration.getStatistics()).compressionTimeNano.addAndGet(endNanoTime - startNanoTime);
//		log.trace("new CompressTask() for block #{}", Integer.valueOf(block.blockNumber));
	}

	/** {@inheritDoc} */
	public void run() {
//		log.trace("Compressing block #{}", Integer.valueOf(block.blockNumber));
		long startNanoTime = ((StatisticsImpl)configuration.getStatistics()).nanoTime();
		int len = block.getUncompressedSize();
		// Deflate no more than stride bytes at a time.  This avoids
		// excess copying in deflateBytes (see Deflater.c)
		for (int n = 0; n < len; n+= STRIDE) {
			def.setInput(block.getUncompressed(), n, Math.min(STRIDE, len - n));
			deflate(Java7Deflater.NO_FLUSH);
		}
		if (! block.isLastBlock()) {
			// not last block: simply SYNC
			deflate(Java7Deflater.SYNC_FLUSH);
			assert def.needsInput() : "Deflater synced but still has input!";
		} else {
			// last block: finish compression
			def.finish();
			while (!def.finished()) {
				deflate(Java7Deflater.NO_FLUSH);// flush mode overriden to Z_FINISH when finish() is called
			}
		}
		assert def.getTotalIn() == block.getUncompressedSize() : "Expected "+block.getUncompressedSize()+" but got "+def.getTotalIn();
		def.end();
		long endNanoTime = ((StatisticsImpl)configuration.getStatistics()).nanoTime();
		int compressedSize = block.compressionDone();
		((StatisticsImpl)configuration.getStatistics()).compressedSize.addAndGet(compressedSize);
		((StatisticsImpl)configuration.getStatistics()).compressionTimeNano.addAndGet(endNanoTime - startNanoTime);
	}

	protected void deflate(int flushMode) {
		int len;
		do {
			len = Java7Deflater.deflate(def, buffer, 0, buffer.length, flushMode);
			if (len > 0) {
				block.writeCompressed(buffer, 0, len);
			}
		} while (len != 0);
	}

}
