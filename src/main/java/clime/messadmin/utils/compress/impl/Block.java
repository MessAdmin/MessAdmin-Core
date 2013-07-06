/**
 *
 */
package clime.messadmin.utils.compress.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

import clime.messadmin.utils.compress.zip.ZipConfiguration;

/**
 * @author C&eacute;drik LIME
 */
public class Block implements Comparable<Block> {
	private volatile byte[] uncompressed;
	private volatile int uncompressedSize;
	private volatile ByteArrayOutputStream compressed;
	protected volatile int blockNumber;
	private volatile boolean lastBlock;
	private volatile boolean compressionDone;
	private volatile boolean checksumDone;
	private volatile boolean dictionaryDone;
	private volatile boolean writeDone;
	private volatile CountDownLatch writeSync;
	private volatile CountDownLatch recycleSync;

	Block(ZipConfiguration configuration) {
		compressed = new ByteArrayOutputStream(Math.min(32*1024, configuration.getBlockSize()));
		initialize();
	}

	/**
	 * Lazily malloc() uncompressed byte array (in BlockManager)
	 */
	protected void initializeIfNeeded(ZipConfiguration configuration) {
		if (uncompressed == null) {
			uncompressed = new byte[configuration.getBlockSize()];//TODO allow to detach for reuse (no need for write)! (create a ReadBufferManager)
		}
	}

	protected void initialize() {
		uncompressedSize = 0;
//		compressed.reset();
		blockNumber = 0;
		lastBlock = false;
		compressionDone = false;
		checksumDone = false;
		dictionaryDone = false;
		writeDone = false;
		writeSync = new CountDownLatch(1);//1: compress
		recycleSync = new CountDownLatch(4);//4: compress + checksum + write + dictionary
	}

	public void waitUntilCanWrite() throws InterruptedException {
		writeSync.await();
		assert compressionDone;
	}

	public void waitUntilCanRecycle() throws InterruptedException {
		recycleSync.await();
		assert compressionDone && checksumDone && writeDone && dictionaryDone;
	}

	/**
	 * @return compressed data size
	 */
	public int compressionDone() {
		compressionDone = true;
		int compressedSize = compressed.size();
		writeSync.countDown();
		recycleSync.countDown();
		return compressedSize;
	}

	public void checksumDone() {
		checksumDone = true;
		recycleSync.countDown();
	}

	public void writeDone() {
		writeDone = true;
		recycleSync.countDown();
	}

	public void dictionaryDone() {
		dictionaryDone = true;
		recycleSync.countDown();
	}


	public void reset() {
		// No need to 0-fill; this might leak data, we we don't care since
		// it will be discarded at the end of the compression process, and
		// no-one (but our code) will be accessing the byte array anyway.
		// This is the same strategy used by ByteArrayOutputStream.
//		Arrays.fill(uncompressed, (byte)0);//TODO System.arraycopy() might be faster
		initialize();
		compressed.reset();// = new ByteArrayOutputStream(32*1024);
	}

	/** {@inheritDoc} */
	public int compareTo(Block o) {
		return o.blockNumber - blockNumber;
	}

	/**
	 * Note: blocking call, until this block is fully read or end of input stream is reached
	 * @param input
	 * @return number of bytes read in this block (same as {@link #uncompressedSize})
	 * @throws IOException
	 */
	public int readFully(InputStream input) throws IOException {
		int totalRead = 0, lastRead = 0;
		while (lastRead != -1 && uncompressedSize < uncompressed.length) {
			lastRead = input.read(uncompressed, uncompressedSize, uncompressed.length - uncompressedSize);
			if (lastRead > 0) {
				uncompressedSize += lastRead;
				totalRead += lastRead;
			}
		}
		return totalRead;
	}

	/**
	 * @return number of bytes read in this block
	 */
	public int read(byte[] b, int offset, int length) {
		if (offset < 0 || length < 0 || offset > b.length - length) {
			throw new ArrayIndexOutOfBoundsException();
		}
		int toCopy = Math.min(length, uncompressed.length-uncompressedSize);
		System.arraycopy(b, offset, uncompressed, uncompressedSize, toCopy);
		uncompressedSize += toCopy;
		return toCopy;
	}

	/**
	 * @return number of bytes read in this block
	 */
	public int read(ByteBuffer src) {
		int toCopy = Math.min(src.remaining(), uncompressed.length-uncompressedSize);
		src.get(uncompressed, uncompressedSize, toCopy);
		uncompressedSize += toCopy;
		return toCopy;
	}

	/**
	 * @return {@code true} if this block's uncompressed data buffer is fully filled in
	 */
	public boolean isComplete() {
		return uncompressedSize == uncompressed.length;
	}

	public void writeCompressed(byte b[], int off, int len) {
		assert ! compressionDone;
		compressed.write(b, off, len);
	}

	public void writeCompressedTo(OutputStream output) throws IOException {
		assert compressionDone;
		compressed.writeTo(output);
	}

	public byte[] getUncompressed() {
		return uncompressed;
	}

	public int getUncompressedSize() {
		return uncompressedSize;
	}

	public boolean isLastBlock() {
		return lastBlock;
	}

	public void setIsLastBlock() {
		if (! lastBlock) {// don't call dictionaryDone() more than once!
			lastBlock = true;
			dictionaryDone();// this last block won't be used as a dictionary
		}
	}

	// This method is dangerous, since it could be called at an inappropriate time:
	// the write thread could (and will!) recycle this block between compressionDone()
	// and getCompressedSize()...
//	public int getCompressedSize() {
//		if (! compressionDone) {
//			throw new IllegalStateException("Compression is not done yet for this block");
//		}
//		return compressed.size();
//	}
}
