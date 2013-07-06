/**
 *
 */
package clime.messadmin.utils.compress.gzip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.WritableByteChannel;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import clime.messadmin.utils.compress.impl.Java7Deflater;
import clime.messadmin.utils.compress.impl.StatisticsImpl;

/**
 * Extension of {@link java.util.zip.GZIPOutputStream} to workaround for a couple of long
 * standing JDK bugs
 * (<a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4255743">Bug
 * 4255743</a> and
 * <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4813885">Bug
 * 4813885</a>) so the GZIP'd output can be flushed.
 * <p>
 * Java 7 finally exposes {@code Z_SYNC_FLUSH} and {@code Z_FULL_FLUSH} in addition to the default {@code Z_NO_FLUSH} (and {@code Z_FINISH} for EOF).
 * <p>
 * Can also set mTime, fileName and comment, exposing gzip features.
 *
 * @see "http://www.ietf.org/rfc/rfc1952.txt"
 * @author C&eacute;drik LIME
 */
// IMPLEMENTATION NOTE: we can not extend java.util.zip.GZIPOutputStream since:
// * writeHeader is private
// * DeflaterOutputStream's constructors are not accessible
public class GZIPOutputStream extends DeflaterOutputStream implements WritableByteChannel {
	private static final Field superUsesDefaultDeflater;
	private static final Field superSyncFlush;

	static {
		try {
			superUsesDefaultDeflater = DeflaterOutputStream.class.getDeclaredField("usesDefaultDeflater");//$NON-NLS-1$
			superUsesDefaultDeflater.setAccessible(true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		// Since Java 7
		Field syncFlush = null;
		try {
			syncFlush = DeflaterOutputStream.class.getDeclaredField("syncFlush");//$NON-NLS-1$
			syncFlush.setAccessible(true);
		} catch (Throwable ignore) {
		}
		superSyncFlush = syncFlush;
	}

	private static final byte[] EMPTYBYTEARRAY = new byte[0];
	private boolean hasData = false;

	/**
	 * Indicates that the stream has been closed.
	 */
	private boolean closed = false;

	protected GZipConfiguration configuration;
	protected StatisticsImpl statistics;
	private final long startTimeNano = System.nanoTime();

	/**
	 * CRC-32 of uncompressed data.
	 */
	protected CRC32 crc = new CRC32();

	/**
	 * Creates a new output stream with a default buffer size.
	 * @param out the output stream
	 * @exception IOException If an I/O error has occurred.
	 */
	public GZIPOutputStream(OutputStream out) throws IOException {
		this(out, 8192, new GZipConfiguration());
	}

	/**
	 * Creates a new output stream with the specified buffer size.
	 * @param out the output stream
	 * @param size the output buffer size
	 * @exception IOException If an I/O error has occurred.
	 * @exception IllegalArgumentException if size is <= 0
	 */
	public GZIPOutputStream(OutputStream out, int size) throws IOException {
		this(out, size, new GZipConfiguration());
	}

	/**
	 * Creates a new output stream with a default buffer size.
	 * @param out the output stream
	 * @param modificationTime in milliseconds
	 * @param fileName original name of the file being compressed, with any directory components removed. Must consist of ISO 8859-1 (LATIN-1) characters.
	 * @param comments not interpreted; it is only intended for human consumption. Must consist of ISO 8859-1 (LATIN-1) characters. Line breaks should be denoted by a single line feed character (10 decimal).
	 * @exception IOException If an I/O error has occurred.
	 * @exception IllegalArgumentException if size is <= 0
	 */
	public GZIPOutputStream(OutputStream out, GZipConfiguration configuration) throws IOException {
		this(out, 8192, configuration);
	}

	/**
	 * Creates a new output stream with the specified buffer size.
	 * @param out the output stream
	 * @param size the output buffer size
	 * @param modificationTime in milliseconds
	 * @param fileName original name of the file being compressed, with any directory components removed. Must consist of ISO 8859-1 (LATIN-1) characters.
	 * @param comments not interpreted; it is only intended for human consumption. Must consist of ISO 8859-1 (LATIN-1) characters. Line breaks should be denoted by a single line feed character (10 decimal).
	 * @exception IOException If an I/O error has occurred.
	 * @exception IllegalArgumentException if size is <= 0
	 */
	public GZIPOutputStream(OutputStream out, int size, GZipConfiguration configuration) throws IOException {
		super(out, new Deflater(configuration.getCompressionLevel(), true), size);
		//usesDefaultDeflater = true;
		try {
			superUsesDefaultDeflater.setBoolean(this, true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		try {
			superSyncFlush.setBoolean(this, true);
		} catch (Throwable ignore) {
		}
		this.configuration = configuration;
		this.statistics = (StatisticsImpl)configuration.getStatistics();
		long startNanoTime = statistics.nanoTime();
		GZipFileStreamUtil.writeHeader(out, configuration.getCompressionLevel(), configuration.getModificationTime(), configuration.getFileName(), configuration.getComment());
		long endNanoTime = statistics.nanoTime();
		statistics.writeTimeNano.addAndGet(endNanoTime - startNanoTime);
		crc.reset();
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void write(byte[] b, int off, int len) throws IOException {
		super.write(b, off, len);
		if (len > 0) {
			long startNanoTime = statistics.nanoTime();
			crc.update(b, off, len);
			long endNanoTime = statistics.nanoTime();
			statistics.checksumTimeNano.addAndGet(endNanoTime - startNanoTime);
			statistics.uncompressedSize.addAndGet(len);
			hasData = true;
		}
	}

	public synchronized void write(final InputStream input) throws IOException {
		byte[] buffer = new byte[32768];//FIXME magic number
		int read;
		while ((read = input.read(buffer)) >= 0) {
			write(buffer, 0, read);
		}
	}

	public synchronized void write(final FileChannel src) throws IOException, NonReadableChannelException {
		MappedByteBuffer map = src.map(MapMode.READ_ONLY, 0, src.size());
		write(map);
	}

	/** {@inheritDoc} */
	public synchronized int write(final ByteBuffer src) throws IOException {
		int r = src.remaining();
		if (r <= 0) {
			return r;
		}
		if (src.hasArray()) {
			// direct compression from backing array
			write(src.array(), src.arrayOffset(), src.limit() - src.arrayOffset());
		} else {
			// need to copy to heap array first
			byte[] buffer = new byte[Math.min(32768, src.remaining())];//FIXME magic number
			while (src.hasRemaining()) {
				int toRead = Math.min(src.remaining(), buffer.length);
				src.get(buffer, 0, toRead);
				write(buffer, 0, toRead);
			}
		}
		return r;
	}

	/**
	 * Flushes this output stream and forces any buffered output bytes
	 * to be written out to the stream.
	 * Also calls the <code>flush</code> method of its underlying output stream.
	 * <p>
	 * Does Z_SYNC_FLUSH instead of JDK's default Z_NO_FLUSH (and Z_FINISH for EOF).
	 * Note that this decreases the compression ration slightly (Z_SYNC_FLUSH inserts an empty type 0 block).
	 *
	 * @exception  IOException  if an I/O error occurs.
	 * @see        java.io.FilterOutputStream#out
	 */
	@Override
	public synchronized void flush() throws IOException {
		if (!hasData) {
			return; // do not allow the gzip header to be flushed on its own
		}

		if (Java7Deflater.isEnhancedDeflateAvailable) {
			if (!def.finished()) {
				int len = 0;
				long startNanoTime = statistics.nanoTime();
				while ((len = Java7Deflater.deflate(def, buf, 0, buf.length, Java7Deflater.SYNC_FLUSH)) > 0) {
					long midNanoTime = statistics.nanoTime();
					statistics.compressionTimeNano.addAndGet(midNanoTime - startNanoTime);
					startNanoTime = midNanoTime;
					if (len > 0) {
						out.write(buf, 0, len);
						long endNanoTime = statistics.nanoTime();
						statistics.writeTimeNano.addAndGet(endNanoTime - midNanoTime);
						statistics.compressedSize.addAndGet(len);
						startNanoTime = endNanoTime;
					}
					if (len < buf.length) {
						break;
					}
				}
			}
			out.flush();
		} else {
			// trick the deflater to flush
			/**
			 * Now this is tricky: We force the Deflater to flush its data by
			 * switching compression level. As yet, a perplexingly simple workaround for
			 * http://developer.java.sun.com/developer/bugParade/bugs/4255743.html
			 *
			 * WARNING: possible output corruption when compression is enabled and Java <= 6: see
			 * https://issues.apache.org/bugzilla/show_bug.cgi?id=52121
			 */
//			if (!def.finished()) {
//				deflate();
//				def.setInput(EMPTYBYTEARRAY, 0, 0);
//				def.setLevel(configuration.getCompressionLevel() != Deflater.NO_COMPRESSION ? Deflater.NO_COMPRESSION : Deflater.BEST_SPEED);
//				deflate();
//				def.setLevel(configuration.getCompressionLevel());
//				deflate();
//				//def.deflate(buf, 0, buf.length, Deflater.SYNC_FLUSH);// Java 7
//				super.flush();//out.flush();
//			}
			super.flush();//out.flush();
		}

		hasData = false; // no more data to flush
	}

	/*
	 * Keep on calling deflate until it runs dry. The default implementation
	 * only does it once and can therefore hold onto data when they need to be
	 * flushed out.
	 */
	@Override
	protected void deflate() throws IOException {
		int len;
		long startNanoTime = statistics.nanoTime();
		do {
//			super.deflate();
			len = def.deflate(buf, 0, buf.length);
			long midNanoTime = statistics.nanoTime();
			statistics.compressionTimeNano.addAndGet(midNanoTime - startNanoTime);
			startNanoTime = midNanoTime;
			if (len > 0) {
//				startNanoTime = statistics.nanoTime();
				out.write(buf, 0, len);
				long endNanoTime = statistics.nanoTime();
				statistics.writeTimeNano.addAndGet(endNanoTime - midNanoTime);
				statistics.compressedSize.addAndGet(len);
				startNanoTime = endNanoTime;
			}
		} while (len != 0);
	}
	/**
	 * Deflates everything in the def's input buffers.  This will call
	 * <code>def.deflate()</code> until all bytes from the input buffers
	 * are processed.
	 */
//	protected void deflateAll() throws IOException {
//		while (! def.needsInput()) {// This does not work...
//			super.deflate();
//		}
//	}

	/** {@inheritDoc} */
	@Override
	public void finish() throws IOException {
		if (!def.finished()) {
			def.finish();
			while (!def.finished()) {
				deflate();
			}
			// write the trailer
			byte[] trailer = new byte[GZipFileStreamUtil.TRAILER_SIZE];
			GZipFileStreamUtil.writeTrailer(trailer, 0, crc, def);
			long startNanoTime = statistics.nanoTime();
			out.write(trailer);
			out.flush();
			long endNanoTime = statistics.nanoTime();
			statistics.writeTimeNano.addAndGet(endNanoTime - startNanoTime);
			statistics.compressedSize.addAndGet(trailer.length);
		}
	}

	/** {@inheritDoc} */
	public boolean isOpen() {
		return ! closed;
	}

	/** {@inheritDoc} */
	@Override
	public void close() throws IOException {
		super.close();
		long endTimeNano = System.nanoTime();
		statistics.realTimeNano.addAndGet(endTimeNano - startTimeNano);
		closed = true;
	}
}
