/**
 *
 */
package clime.messadmin.utils.compress.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import clime.messadmin.utils.compress.zip.ZipConfiguration;

/**
 * Reads and store {@code byte[]} or {@code InputStream} inputs into {@link Block}s.<br>
 * Implementation note: stateful, to properly manage incomplete blocks / input data reads, and {@code block.isLastBlock}<br>
 * Usage:
 * <pre>
 * ReadTask rt = new ReadTask(...);
 * rt.setInput(...);
 * while ((Block block = rt.getNextBlock()) != null) {
 * 	...
 * }
 * rt.finish();
 * while ((Block block = rt.getNextBlock()) != null) {
 * 	...
 * }
 * </pre>
 * @author C&eacute;drik LIME
 */
public class ReadTask {
//	protected Logger log = LoggerFactory.getLogger(this.getClass());
	private final ZipConfiguration configuration;
	private final BlockManager blockManager;
	/* Input data: byte[] or InputStream */
	private InputStream inputStream = null;
	private byte[] inputBytes = null;
	private int inputBytesPos = -1;
	private int inputBytesEnd = -1;
	private ByteBuffer inputByteBuffer = null;
	/* Current Block being read */
	private Block currentBlock = null;
	private long uncompressedSize = 0;
	private boolean finish = false;
	private IOException lastException = null;

	public ReadTask(ZipConfiguration configuration, BlockManager blockManager) {
		this.configuration = configuration;
		this.blockManager = blockManager;
	}

	public IOException getLastException() {
		return lastException;
	}

	public boolean needsInput() {
		return inputStream == null && inputBytesPos == -1 && inputByteBuffer == null;
	}

	/**
	 * Note: blocking call, if reading from an {@code InputStream}
	 * @return {@code null} no more block is available (i.e. end of input stream)
	 */
	public Block getNextBlock() throws IOException, InterruptedException {
		if (currentBlock == null || ! currentBlock.isComplete()) {
			currentBlock = readCurrentOrNewBlock();
		}
		if (finish) {
			// Return the current block, whatever its completed state.
			// Set the isLastBlock flag if necessary.
			// The 1st "currentBlock = readCurrentOrNewBlock()" line will
			// have already filled in the current block, or drained the available input.
			Block nextBlock = readCurrentOrNewBlock();// can return currentBlock if no more input!
			if ((nextBlock == null || nextBlock == currentBlock) && currentBlock != null) {
				currentBlock.setIsLastBlock();
			}
			Block block = currentBlock;
			currentBlock = (nextBlock!=currentBlock) ? nextBlock : null;
			return block;
		}
		if (currentBlock == null || ! currentBlock.isComplete()) {
			return null;
		}
		// current block is now complete: see if there is a next one
		Block nextBlock = readCurrentOrNewBlock();
		if (nextBlock != null) {
			// there is a next block, so current one is not the last one: return it!
			Block toReturn  = currentBlock;
			currentBlock = nextBlock;
			return toReturn;
		}
		// There is no next block, maybe due to lack of input.
		// Wait till we have more input or we are finished
		return null;
	}

	/**
	 * Fills in (completes) the current block if it is not complete yet.
	 * Creates and fill in a new block otherwise.
	 */
	private Block readCurrentOrNewBlock() throws IOException, InterruptedException {
		if (needsInput()) {
			return (currentBlock != null && ! currentBlock.isComplete()) ? currentBlock : null;
		}
		Block block;
		if (currentBlock != null && ! currentBlock.isComplete()) {
			block = currentBlock;
		} else {
			block = blockManager.getBlockFromPool();
		}
//		log.debug("Reading block #{}", Integer.valueOf(blockNumber));
		if (inputStream != null) {
			long startNanoTime = ((StatisticsImpl)configuration.getStatistics()).nanoTime();
			int nRead = block.readFully(inputStream);
			if (nRead > 0) {
				uncompressedSize += nRead;
				((StatisticsImpl)configuration.getStatistics()).uncompressedSize.addAndGet(nRead);
			}
			if (nRead == 0 || !block.isComplete()) {
				inputStream = null;
			}
			long endNanoTime = ((StatisticsImpl)configuration.getStatistics()).nanoTime();
			((StatisticsImpl)configuration.getStatistics()).readTimeNano.addAndGet(endNanoTime - startNanoTime);
		} else if (inputBytes != null) {
			int nRead = block.read(inputBytes, inputBytesPos, inputBytesEnd-inputBytesPos);
			uncompressedSize += nRead;
			((StatisticsImpl)configuration.getStatistics()).uncompressedSize.addAndGet(nRead);
			inputBytesPos += nRead;
			if (inputBytesPos >= inputBytesEnd) {
				inputBytes = null;
				inputBytesPos = inputBytesEnd = -1;
			}
		} else {
			assert inputByteBuffer != null;
			int nRead = block.read(inputByteBuffer);
			uncompressedSize += nRead;
			((StatisticsImpl)configuration.getStatistics()).uncompressedSize.addAndGet(nRead);
			if ( ! inputByteBuffer.hasRemaining()) {
				inputByteBuffer = null;
			}
		}
		if (block.getUncompressedSize() <= 0) {
			blockManager.forceReleaseBlockToPool(block);
			return (currentBlock != null && ! currentBlock.isComplete()) ? currentBlock : null;
		}
		return block;
	}

	public void setInput(InputStream input) throws IllegalStateException {
		if (inputBytes != null || inputStream != null || inputByteBuffer != null) {
			throw new IllegalStateException("setInput() called when there was unread input");
		}
		this.inputStream = input;
	}

	public void setInput(byte[] buff, int off, int len) throws IllegalStateException {
		if (buff == null) {
			throw new NullPointerException();
		}
		if (off < 0 || len < 0 || off > buff.length - len) {
			throw new ArrayIndexOutOfBoundsException();
		}
		if (inputBytes != null || inputStream != null || inputByteBuffer != null) {
			throw new IllegalStateException("setInput() called when there was unread input");
		}
		this.inputBytes = buff;
		this.inputBytesPos = off;
		this.inputBytesEnd = off + len;
	}

	public void setInput(ByteBuffer src) throws IllegalStateException {
		if (inputBytes != null || inputStream != null || inputByteBuffer != null) {
			throw new IllegalStateException("setInput() called when there was unread input");
		}
		if (src.hasArray()) {
			// direct compression from backing array
			setInput(src.array(), src.arrayOffset(), src.remaining());
		} else {
			this.inputByteBuffer = src;
		}
	}

	/**
	 * When called, indicates that there will be no more input.
	 */
	public void finish() {
		finish = true;
	}

	/**
	 * Resets this reader so that a new set of input data can be processed.
	 */
	public void reset() {
		finish = false;
		inputStream = null;
		inputBytes = null;
		inputBytesPos = inputBytesEnd = -1;
		currentBlock = null;
		uncompressedSize = 0;
		finish = false;
		lastException = null;
	}

	public long getUncompressedSize() {
		return uncompressedSize;
	}
}
