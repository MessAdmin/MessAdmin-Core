/**
 *
 */
package clime.messadmin.utils.compress.gzip;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;

import clime.messadmin.utils.compress.impl.AbstractSerialExecutor;
import clime.messadmin.utils.compress.impl.Block;
import clime.messadmin.utils.compress.impl.BlockManager;
import clime.messadmin.utils.compress.impl.StatisticsImpl;

/**
 * @author C&eacute;drik LIME
 */
class WriteGZipTask extends AbstractSerialExecutor implements Runnable {
	private final BlockManager blockManager;
	private final OutputStream output;
	private volatile int checksum;
	private volatile int uncompressedSize = 0;
	private final CountDownLatch trailerSync = new CountDownLatch(2);//2: crc, uncompressedSize
	private IOException lastException = null;

	public WriteGZipTask(BlockManager blockManager, OutputStream output, GZipConfiguration configuration) throws IOException {
		super(configuration);
		this.blockManager = blockManager;
		this.output = output;
//		log.debug("Writing header");
		long startNanoTime = ((StatisticsImpl)configuration.getStatistics()).nanoTime();
		int headerSize = GZipFileStreamUtil.writeHeader(output, configuration.getCompressionLevel(), configuration.getModificationTime(), configuration.getFileName(), configuration.getComment());
		((StatisticsImpl)configuration.getStatistics()).compressedSize.addAndGet(headerSize);
		long endNanoTime = ((StatisticsImpl)configuration.getStatistics()).nanoTime();
		((StatisticsImpl)configuration.getStatistics()).writeTimeNano.addAndGet(endNanoTime - startNanoTime);
	}

	public void setCRC(int checksum) {
		this.checksum = checksum;
		trailerSync.countDown();
	}
	public void setUncompressedSize(long uncompressedSize) {
		// rfc1952; ISIZE is the input size modulo 2^32
		int uncompressedSizeInt = (int) (uncompressedSize & 0xffffffffL);
		this.uncompressedSize = uncompressedSizeInt;
		trailerSync.countDown();
	}

	public IOException getLastException() {
		return lastException;
	}

	/** {@inheritDoc} */
	@Override
	public void run() {
		super.run();
		try {
			trailerSync.await();
//			log.debug("Writing trailer: crc={}, uncompressed_size={}", Integer.valueOf(checksum), Integer.valueOf(uncompressedSize));
			long startNanoTime = ((StatisticsImpl)configuration.getStatistics()).nanoTime();
			byte[] trailer = new byte[GZipFileStreamUtil.TRAILER_SIZE];
			GZipFileStreamUtil.writeTrailer(trailer, 0, checksum, uncompressedSize);
			output.write(trailer);
			output.flush();
			((StatisticsImpl)configuration.getStatistics()).compressedSize.addAndGet(GZipFileStreamUtil.TRAILER_SIZE);
			long endNanoTime = ((StatisticsImpl)configuration.getStatistics()).nanoTime();
			((StatisticsImpl)configuration.getStatistics()).writeTimeNano.addAndGet(endNanoTime - startNanoTime);
		} catch (InterruptedException ignore) {
		} catch (IOException e) {
			lastException = e;
			throw new RuntimeException(e);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void process(Block block) throws InterruptedException, IOException {
		block.waitUntilCanWrite();
//		log.debug("Writing block #{}", Integer.valueOf(block.blockNumber));
		long startNanoTime = ((StatisticsImpl)configuration.getStatistics()).nanoTime();
//		block.uncompressed = null;//TODO recycle buffer instead
		block.writeCompressedTo(output);
		block.writeDone();
		long endNanoTime = ((StatisticsImpl)configuration.getStatistics()).nanoTime();
		((StatisticsImpl)configuration.getStatistics()).writeTimeNano.addAndGet(endNanoTime - startNanoTime);
		// recycle block
		blockManager.releaseBlockToPool(block);
	}

}
