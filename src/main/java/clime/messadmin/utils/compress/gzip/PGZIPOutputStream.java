/**
 *
 */
package clime.messadmin.utils.compress.gzip;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.CRC32;

import clime.messadmin.utils.compress.impl.Block;
import clime.messadmin.utils.compress.impl.BlockManager;
import clime.messadmin.utils.compress.impl.ChecksumTask;
import clime.messadmin.utils.compress.impl.Compressor;
import clime.messadmin.utils.compress.impl.ReadTask;
import clime.messadmin.utils.compress.impl.StatisticsImpl;

/**
 * {@link java.util.zip.GZIPOutputStream} that does multi-threaded compression.
 * For best performance, use {@link #write(InputStream)} or {@link #write(byte[])} with big buffers, preferably of size {@link GZipConfiguration#getBlockSize()}.
 *
 * @author C&eacute;drik LIME
 */
public class PGZIPOutputStream extends FilterOutputStream {
//	protected static Logger log = LoggerFactory.getLogger(PGZIPOutputStream.class);
	private final long startTimeNano;
	private final GZipConfiguration configuration;
	private Exception lastException = null;
	private BlockManager blockManager;
	private ReadTask readTask;
	private WriteGZipTask writeTask;
	private Thread writeThread;
	private ChecksumTask checksumTask;
	private Thread checksumThread;
	private Compressor compressor;
	private Block previousBlock = null;
	private Block currentBlock = null;
	private boolean closed = false;

	public PGZIPOutputStream(OutputStream out, GZipConfiguration configuration) throws IOException {
		super(out);
		this.configuration = configuration;
		startTimeNano = System.nanoTime();
		try {
			blockManager = new BlockManager(configuration);
			readTask = new ReadTask(configuration, blockManager);
			checksumTask = new ChecksumTask(new CRC32(), configuration);
			checksumThread = new Thread(checksumTask, "PIGZ checksum thread");
			compressor = new Compressor(configuration);
			writeTask = new WriteGZipTask(blockManager, out, configuration);
			writeThread = new Thread(writeTask, "PIGZ write thread");
			checksumThread.start();
			writeThread.start();
		} catch (IOException e) {
			exceptionCleanup(e);
			throwLastIOorRuntimeException();
		} catch (RuntimeException e) {
			exceptionCleanup(e);
			throwLastIOorRuntimeException();
		}
	}

	/**
	 * {@inheritDoc}
	 * WARNING: using this method will lead to very poor performance!
	 */
	@Override
	public void write(int b) throws IOException, RuntimeException {
		byte[] buf = new byte[1];
		buf[0] = (byte)(b & 0xff);
		write(buf, 0, 1);
	}

	/** {@inheritDoc} */
	@Override
	public void write(byte[] buf, int offset, int length) throws IOException, RuntimeException {
		if (buf == null || length == 0) {
			return;
		}
		readTask.setInput(buf, offset, length);
		write();
	}

	/**
	 * Writes the content of the specified {@code InputStream} to this output stream.
	 */
	public void write(InputStream in) throws IOException, RuntimeException {
		readTask.setInput(in);
		write();
	}

	private void write() throws IOException, RuntimeException {
		try {
			currentBlock = readTask.getNextBlock();
			while (currentBlock != null) {
				compressor.compress(currentBlock, previousBlock);
				checksumTask.submit(currentBlock);
				writeTask.submit(currentBlock);
				previousBlock = currentBlock;
				currentBlock = readTask.getNextBlock();
			}
			assert readTask.needsInput();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			exceptionCleanup(e);
			throwLastIOorRuntimeException();
		} catch (RuntimeException e) {
			exceptionCleanup(e);
			throwLastIOorRuntimeException();
		}
	}

	/**
	 * cleanly exit threads
	 */
	private void exceptionCleanup(Exception e) {
		checksumTask.prepareForInterrupt();
		checksumThread.interrupt();
		writeTask.prepareForInterrupt();
		if (writeThread != null) {
			writeThread.interrupt();
		}
		compressor.shutdownNow();
		if (writeTask.getLastException() != null) {
			lastException = writeTask.getLastException();
		} else if (readTask.getLastException() != null) {
			lastException = readTask.getLastException();
		} else {
			lastException = e;
		}
	}

	private void throwLastIOorRuntimeException() throws IOException, RuntimeException {
		if (lastException != null) {
			if (lastException instanceof RuntimeException) {
				throw (RuntimeException) lastException;
			} else if (lastException instanceof IOException) {
				throw (IOException) lastException;
			} else {
				throw new RuntimeException(lastException);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void finalize() throws Throwable {
		close();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException, RuntimeException {
		if (closed) {
			return;
		}
		readTask.finish();
		write();
		//XXX temporary hack to avoid blocking on empty input files
		if (readTask.getUncompressedSize() == 0) {
			try {
				Block emptyBlock = blockManager.getBlockFromPool();
				emptyBlock.setIsLastBlock();
				compressor.compress(emptyBlock, null);
				checksumTask.submit(emptyBlock);
				writeTask.submit(emptyBlock);
			} catch (InterruptedException ignore) {
			}
		}
		// join()
		try {
			checksumThread.join();
			writeTask.setCRC((int)checksumTask.getCheckumValue());
			writeTask.setUncompressedSize(readTask.getUncompressedSize());
			writeThread.join();
			super.close();
			assert readTask.getUncompressedSize() == configuration.getStatistics().getUncompressedSize();
		} catch (InterruptedException e) {
			exceptionCleanup(e);
		} finally {
			if (compressor != null) {
				compressor.shutdownNow();
				compressor = null;
			}
		}
		// note: do not set lastException to null, to enable throwLastIOorRuntimeException() after close()
		blockManager = null;
		readTask = null;
		writeTask = null;
		writeThread = null;
		checksumTask = null;
		checksumThread = null;
		previousBlock = null;
		currentBlock = null;
		closed = true;
		long endTimeNano = System.nanoTime();
		((StatisticsImpl)configuration.getStatistics()).realTimeNano.addAndGet(endTimeNano - startTimeNano);
//		log.info("{}", configuration.getStatistics());
		throwLastIOorRuntimeException();
	}
}
