/*
 * @(#)ZipOutputStream.java
 *
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package clime.messadmin.utils.compress.zip;

import static clime.messadmin.utils.compress.zip.ZipConstants64.*;
import static java.util.zip.ZipEntry.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashSet;
import java.util.Vector;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

import clime.messadmin.utils.Charsets;
import clime.messadmin.utils.compress.impl.Java7Deflater;
import clime.messadmin.utils.compress.impl.StatisticsImpl;

/**
 * This class implements an output stream filter for writing files in the
 * ZIP file format. Includes support for both compressed and uncompressed
 * entries.
 *
 * @author      David Connelly
 * @author      C&eacute;drik LIME
 */
public class ZipOutputStream extends DeflaterOutputStream implements WritableByteChannel {
	private static final Field superUsesDefaultDeflater;

	static {
		try {
			superUsesDefaultDeflater = DeflaterOutputStream.class.getDeclaredField("usesDefaultDeflater");//$NON-NLS-1$
			superUsesDefaultDeflater.setAccessible(true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static class XEntry {
		public final ZipEntry entry;
		public final long offset;
		public final int flag;
		public XEntry(ZipEntry entry, long offset, boolean isUTF8, int compressionLevel) {
			this.entry = entry;
			this.offset = offset;
			int flag =
					 (entry.getSize()  == -1 ||
					  entry.getCompressedSize() == -1 ||
					  entry.getCrc()   == -1)
				// store size, compressed size, and crc-32 in data descriptor
				// immediately following the compressed entry data
				? 8
				// store size, compressed size, and crc-32 in LOC header
				: 0;
			if (entry.getMethod() == DEFLATED) {
				// set bits 2 & 1: compression level
				switch (compressionLevel) {
				case Deflater.DEFAULT_COMPRESSION:
					flag |= 0;
					break;
				case Deflater.BEST_COMPRESSION:
					flag |= 3;
					break;
				case Deflater.BEST_SPEED:
					flag |= 4;
					break;
				case Deflater.NO_COMPRESSION:
					flag |= 6;
					break;
				default:
				}
			}
			if (isUTF8) {
				flag |= EFS;
			}
			this.flag = flag;
		}
	}

	private XEntry current;
	private final Vector<XEntry> xentries = new Vector<XEntry>();
	private final HashSet<String> names = new HashSet<String>();
	private final CRC32 crc = new CRC32();
	private long written = 0;
	private long locoff = 0;
	private byte[] comment;
	private int method = DEFLATED;
	private int level = Deflater.DEFAULT_COMPRESSION;
	private boolean finished;

	private boolean closed = false;

	private final ZipCoder zc;

	protected final ZipConfiguration configuration;
	protected StatisticsImpl statistics;
	private final long startTimeNano = System.nanoTime();

	private static int version(ZipEntry e) throws ZipException {
		switch (e.getMethod()) {
		case DEFLATED: return 20;
		case STORED:   return 20;//was: 10; need 2.0 to store folders
		default: throw new ZipException("unsupported compression method");
		}
	}

	/**
	 * Checks to make sure that this stream has not been closed.
	 */
	private void ensureOpen() throws IOException {
		if (closed) {
			throw new IOException("Stream closed");
		}
	}
	/**
	 * Compression method for uncompressed (STORED) entries.
	 */
	public static final int STORED = ZipEntry.STORED;

	/**
	 * Compression method for compressed (DEFLATED) entries.
	 */
	public static final int DEFLATED = ZipEntry.DEFLATED;

	/**
	 * Creates a new ZIP output stream.
	 *
	 * <p>The UTF-8 {@link java.nio.charset.Charset charset} is used
	 * to encode the entry names and comments.
	 *
	 * @param out the actual output stream
	 */
	public ZipOutputStream(OutputStream out) {
		this(out, Charsets.UTF_8, new ZipConfiguration());
	}

	/**
	 * Creates a new ZIP output stream.
	 *
	 * <p>The UTF-8 {@link java.nio.charset.Charset charset} is used
	 * to encode the entry names and comments.
	 *
	 * @param out the actual output stream
	 */
	public ZipOutputStream(OutputStream out, ZipConfiguration configuration) {
		this(out, Charsets.UTF_8, configuration);
	}

	/**
	 * Creates a new ZIP output stream.
	 *
	 * @param out the actual output stream
	 *
	 * @param charset the {@linkplain java.nio.charset.Charset charset}
	 *                to be used to encode the entry names and comments
	 *
	 * @since 1.7
	 */
	public ZipOutputStream(OutputStream out, Charset charset) {
		this(out, charset, new ZipConfiguration());
	}

	/**
	 * Creates a new ZIP output stream.
	 *
	 * @param out the actual output stream
	 *
	 * @param charset the {@linkplain java.nio.charset.Charset charset}
	 *                to be used to encode the entry names and comments
	 *
	 * @since 1.7
	 */
	public ZipOutputStream(OutputStream out, Charset charset, ZipConfiguration configuration) {
		super(out, new Deflater(configuration.getCompressionLevel(), true), 8192);
		if (charset == null) {
			throw new NullPointerException("charset is null");
		}
		this.zc = ZipCoder.get(charset);
		//usesDefaultDeflater = true;
		try {
			superUsesDefaultDeflater.setBoolean(this, true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		this.configuration = configuration;
		this.statistics = (StatisticsImpl)configuration.getStatistics();
		setMethod(configuration.getCompressionMethod());
		if (configuration.getComment() != null) {
			this.setComment(configuration.getComment().toString());
		}
	}

	/**
	 * Sets the ZIP file comment.
	 * @param comment the comment string
	 * @exception IllegalArgumentException if the length of the specified
	 *            ZIP file comment is greater than 0xFFFF bytes
	 */
	public void setComment(String comment) {
		if (comment != null) {
			this.comment = zc.getBytes(comment);
			if (this.comment.length > 0xffff) {
				throw new IllegalArgumentException("ZIP file comment too long.");
			}
		}
	}

	/**
	 * Sets the default compression method for subsequent entries. This
	 * default will be used whenever the compression method is not specified
	 * for an individual ZIP file entry, and is initially set to DEFLATED.
	 * @param method the default compression method
	 * @exception IllegalArgumentException if the specified compression method
	 *            is invalid
	 */
	public void setMethod(int method) {
		if (method != DEFLATED && method != STORED) {
			throw new IllegalArgumentException("invalid compression method: " + method);
		}
		this.method = method;
	}

	/**
	 * Sets the compression level for subsequent entries which are DEFLATED.
	 * The default setting is DEFAULT_COMPRESSION.
	 * @param level the compression level (0-9)
	 * @exception IllegalArgumentException if the compression level is invalid
	 */
	public void setLevel(int level) {
		def.setLevel(level);
		this.level = level;
	}

	/**
	 * Begins writing a new ZIP file entry and positions the stream to the
	 * start of the entry data. Closes the current entry if still active.
	 * The default compression method will be used if no compression method
	 * was specified for the entry, and the current time will be used if
	 * the entry has no set modification time.
	 * @param e the ZIP entry to be written
	 * @exception ZipException if a ZIP format error has occurred
	 * @exception IOException if an I/O error has occurred
	 */
	public void putNextEntry(ZipEntry e) throws IOException {
		ensureOpen();
		if (current != null) {
			closeEntry();       // close previous entry
		}
		if (e.getTime() == -1) {
			e.setTime(System.currentTimeMillis());
		}
		if (e.getMethod() == -1) {
			e.setMethod(method);  // use default method
		}
		switch (e.getMethod()) {
		case DEFLATED:
			break;
		case STORED:
			// compressed size, uncompressed size, and crc-32 must all be
			// set for entries using STORED compression method
			if (e.getSize() == -1) {
				e.setSize(e.getCompressedSize());
			} else if (e.getCompressedSize() == -1) {
				e.setCompressedSize(e.getSize());
			} else if (e.getSize() != e.getCompressedSize()) {
				throw new ZipException(
					"STORED entry where compressed != uncompressed size");
			}
			if (e.getSize() == -1 || e.getCrc() == -1) {
				throw new ZipException(
					"STORED entry missing size, compressed size, or crc-32");
			}
			break;
		default:
			throw new ZipException("unsupported compression method");
		}
		if (! names.add(e.getName())) {
			throw new ZipException("duplicate entry: " + e.getName());
		}
		current = new XEntry(e, written, zc.isUTF8(), level);
//		if (zc.isUTF8())
//			current.flag |= EFS;
		xentries.add(current);
		writeLOC(current);
	}

	/**
	 * Closes the current ZIP entry and positions the stream for writing
	 * the next entry.
	 * @exception ZipException if a ZIP format error has occurred
	 * @exception IOException if an I/O error has occurred
	 */
	public void closeEntry() throws IOException {
		ensureOpen();
		if (current != null) {
			ZipEntry e = current.entry;
			switch (e.getMethod()) {
			case DEFLATED:
				def.finish();
				while (!def.finished()) {
					deflate();
				}
				if ((current.flag & 8) == 0) {
					// verify size, compressed size, and crc-32 settings
					if (e.getSize() != def.getBytesRead()) {
						throw new ZipException(
							"invalid entry size (expected " + e.getSize() +
							" but got " + def.getBytesRead() + " bytes)");
					}
					if (e.getCompressedSize() != def.getBytesWritten()) {
						throw new ZipException(
							"invalid entry compressed size (expected " +
							e.getCompressedSize() + " but got " + def.getBytesWritten() + " bytes)");
					}
					if (e.getCrc() != crc.getValue()) {
						throw new ZipException(
							"invalid entry CRC-32 (expected 0x" +
							Long.toHexString(e.getCrc()) + " but got 0x" +
							Long.toHexString(crc.getValue()) + ")");
					}
				} else {
					e.setSize(def.getBytesRead());
					e.setCompressedSize(def.getBytesWritten());
					e.setCrc(crc.getValue());
					writeEXT(e);
				}
				def.reset();
				written += e.getCompressedSize();
				break;
			case STORED:
				// we already know that both e.size and e.csize are the same
				if (e.getSize() != written - locoff) {
					throw new ZipException(
						"invalid entry size (expected " + e.getSize() +
						" but got " + (written - locoff) + " bytes)");
				}
				if (e.getCrc() != crc.getValue()) {
					throw new ZipException(
						 "invalid entry crc-32 (expected 0x" +
						 Long.toHexString(e.getCrc()) + " but got 0x" +
						 Long.toHexString(crc.getValue()) + ")");
				}
				break;
			default:
				throw new ZipException("invalid compression method");
			}
			crc.reset();
			current = null;
			statistics.uncompressedSize.addAndGet(e.getSize());
		}
	}

	/**
	 * Writes an array of bytes to the current ZIP entry data. This method
	 * will block until all the bytes are written.
	 * @param b the data to be written
	 * @param off the start offset in the data
	 * @param len the number of bytes that are written
	 * @exception ZipException if a ZIP file error has occurred
	 * @exception IOException if an I/O error has occurred
	 */
	@Override
	public synchronized void write(byte[] b, int off, int len)
		throws IOException
	{
		ensureOpen();
		if (off < 0 || len < 0 || off > b.length - len) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return;
		}

		if (current == null) {
			throw new ZipException("no current ZIP entry");
		}
		ZipEntry entry = current.entry;
		switch (entry.getMethod()) {
		case DEFLATED:
			super.write(b, off, len);
			break;
		case STORED:
			written += len;
			if (written - locoff > entry.getSize()) {
				throw new ZipException(
					"attempt to write past end of STORED entry");
			}
			long startNanoTime = statistics.nanoTime();
			out.write(b, off, len);
			long endNanoTime = statistics.nanoTime();
			statistics.writeTimeNano.addAndGet(endNanoTime - startNanoTime);
			break;
		default:
			throw new ZipException("invalid compression method");
		}
		long startNanoTime = statistics.nanoTime();
		crc.update(b, off, len);
		long endNanoTime = statistics.nanoTime();
		statistics.checksumTimeNano.addAndGet(endNanoTime - startNanoTime);
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

	/**
	 * Finishes writing the contents of the ZIP output stream without closing
	 * the underlying stream. Use this method when applying multiple filters
	 * in succession to the same output stream.
	 * @exception ZipException if a ZIP file error has occurred
	 * @exception IOException if an I/O exception has occurred
	 */
	@Override
	public void finish() throws IOException {
		ensureOpen();
		if (finished) {
			return;
		}
		if (current != null) {
			closeEntry();
		}
		// write central directory
		long off = written;
		long startNanoTime = statistics.nanoTime();
		for (XEntry xentry : xentries) {
			writeCEN(xentry);
		}
		writeEND(off, written - off);
		long endNanoTime = statistics.nanoTime();
		statistics.writeTimeNano.addAndGet(endNanoTime - startNanoTime);
		finished = true;
	}

	/** {@inheritDoc} */
	public boolean isOpen() {
		return ! closed;
	}

	/**
	 * Closes the ZIP output stream as well as the stream being filtered.
	 * @exception ZipException if a ZIP file error has occurred
	 * @exception IOException if an I/O error has occurred
	 */
	@Override
	public void close() throws IOException {
		if (!closed) {
			super.close();
			closed = true;
			statistics.compressedSize.addAndGet(written);
			long endTimeNano = System.nanoTime();
			statistics.realTimeNano.addAndGet(endTimeNano - startTimeNano);
		}
	}

	/*
	 * Writes local file (LOC) header for specified entry.
	 */
	private void writeLOC(XEntry xentry) throws IOException {
		long startNanoTime = statistics.nanoTime();
		ZipEntry e = xentry.entry;
		int flag = xentry.flag;
		int elen = (e.getExtra() != null) ? e.getExtra().length : 0;
		boolean hasZip64 = false;

		writeInt(LOCSIG);               // LOC header signature

		if ((flag & 8) == 8) {
			writeShort(version(e));     // version needed to extract
			writeShort(flag);           // general purpose bit flag
			writeShort(e.getMethod());  // compression method
			writeInt(javaToDosTime(e.getTime()));           // last modification time

			// store size, uncompressed size, and crc-32 in data descriptor
			// immediately following compressed entry data
			writeInt(0);
			writeInt(0);
			writeInt(0);
		} else {
			if (e.getCompressedSize() >= ZIP64_MAGICVAL || e.getSize() >= ZIP64_MAGICVAL) {
				hasZip64 = true;
				writeShort(45);         // ver 4.5 for zip64
			} else {
				writeShort(version(e)); // version needed to extract
			}
			writeShort(flag);           // general purpose bit flag
			writeShort(e.getMethod());  // compression method
			writeInt(javaToDosTime(e.getTime()));           // last modification time
			writeInt(e.getCrc());       // crc-32
			if (hasZip64) {
				writeInt(ZIP64_MAGICVAL);
				writeInt(ZIP64_MAGICVAL);
				elen += 20;        //headid(2) + size(2) + size(8) + csize(8)
			} else {
				writeInt(e.getCompressedSize());  // compressed size
				writeInt(e.getSize());   // uncompressed size
			}
		}
		byte[] nameBytes = zc.getBytes(e.getName());
		writeShort(nameBytes.length);
		writeShort(elen);
		writeBytes(nameBytes, 0, nameBytes.length);
		if (hasZip64) {
			writeShort(ZIP64_EXTID);
			writeShort(16);
			writeLong(e.getSize());
			writeLong(e.getCompressedSize());
		}
		if (e.getExtra() != null) {
			writeBytes(e.getExtra(), 0, e.getExtra().length);
		}
		locoff = written;
		long endNanoTime = statistics.nanoTime();
		statistics.writeTimeNano.addAndGet(endNanoTime - startNanoTime);
	}

	/*
	 * Writes extra data descriptor (EXT) for specified entry.
	 */
	private void writeEXT(ZipEntry e) throws IOException {
		long startNanoTime = statistics.nanoTime();
		writeInt(EXTSIG);           // EXT header signature
		writeInt(e.getCrc());       // crc-32
		if (e.getCompressedSize() >= ZIP64_MAGICVAL || e.getSize() >= ZIP64_MAGICVAL) {
			writeLong(e.getCompressedSize());
			writeLong(e.getSize());
		} else {
			writeInt(e.getCompressedSize()); // compressed size
			writeInt(e.getSize());           // uncompressed size
		}
		long endNanoTime = statistics.nanoTime();
		statistics.writeTimeNano.addAndGet(endNanoTime - startNanoTime);
	}

	/*
	 * Write central directory (CEN) header for specified entry.
	 * REMIND: add support for file attributes
	 */
	private void writeCEN(XEntry xentry) throws IOException {
		ZipEntry e  = xentry.entry;
		int flag = xentry.flag;
		int version = version(e);

		long csize = e.getCompressedSize();
		long size = e.getSize();
		long offset = xentry.offset;
		int e64len = 0;
		boolean hasZip64 = false;
		if (e.getCompressedSize() >= ZIP64_MAGICVAL) {
			csize = ZIP64_MAGICVAL;
			e64len += 8;              // csize(8)
			hasZip64 = true;
		}
		if (e.getSize() >= ZIP64_MAGICVAL) {
			size = ZIP64_MAGICVAL;    // size(8)
			e64len += 8;
			hasZip64 = true;
		}
		if (xentry.offset >= ZIP64_MAGICVAL) {
			offset = ZIP64_MAGICVAL;
			e64len += 8;              // offset(8)
			hasZip64 = true;
		}
		writeInt(CENSIG);           // CEN header signature
		if (hasZip64) {
			writeShort(45);         // ver 4.5 for zip64
			writeShort(45);
		} else {
			writeShort(version);    // version made by
			writeShort(version);    // version needed to extract
		}
		writeShort(flag);           // general purpose bit flag
		writeShort(e.getMethod());  // compression method
		writeInt(javaToDosTime(e.getTime()));           // last modification time
		writeInt(e.getCrc());       // crc-32
		writeInt(csize);            // compressed size
		writeInt(size);             // uncompressed size
		byte[] nameBytes = zc.getBytes(e.getName());
		writeShort(nameBytes.length);
		if (hasZip64) {
			// + headid(2) + datasize(2)
			writeShort(e64len + 4 + (e.getExtra() != null ? e.getExtra().length : 0));
		} else {
			writeShort(e.getExtra() != null ? e.getExtra().length : 0);
		}
		byte[] commentBytes;
		if (e.getComment() != null) {
			commentBytes = zc.getBytes(e.getComment());
			writeShort(Math.min(commentBytes.length, 0xffff));
		} else {
			commentBytes = null;
			writeShort(0);
		}
		writeShort(0);              // starting disk number
		writeShort(0);              // internal file attributes (unused)
		writeInt(0);                // external file attributes (unused)
		writeInt(offset);           // relative offset of local header
		writeBytes(nameBytes, 0, nameBytes.length);
		if (hasZip64) {
			writeShort(ZIP64_EXTID);// Zip64 extra
			writeShort(e64len);
			if (size == ZIP64_MAGICVAL) {
				writeLong(e.getSize());
			}
			if (csize == ZIP64_MAGICVAL) {
				writeLong(e.getCompressedSize());
			}
			if (offset == ZIP64_MAGICVAL) {
				writeLong(xentry.offset);
			}
		}
		if (e.getExtra() != null) {
			writeBytes(e.getExtra(), 0, e.getExtra().length);
		}
		if (commentBytes != null) {
			writeBytes(commentBytes, 0, Math.min(commentBytes.length, 0xffff));
		}
	}

	/*
	 * Writes end of central directory (END) header.
	 */
	private void writeEND(long off, long len) throws IOException {
		boolean hasZip64 = false;
		long xlen = len;
		long xoff = off;
		if (xlen >= ZIP64_MAGICVAL) {
			xlen = ZIP64_MAGICVAL;
			hasZip64 = true;
		}
		if (xoff >= ZIP64_MAGICVAL) {
			xoff = ZIP64_MAGICVAL;
			hasZip64 = true;
		}
		int count = xentries.size();
		if (count >= ZIP64_MAGICCOUNT) {
			count = ZIP64_MAGICCOUNT;
			hasZip64 = true;
		}
		if (hasZip64) {
			long off64 = written;
			//zip64 end of central directory record
			writeInt(ZIP64_ENDSIG);        // zip64 END record signature
			writeLong(ZIP64_ENDHDR - 12);  // size of zip64 end
			writeShort(45);                // version made by
			writeShort(45);                // version needed to extract
			writeInt(0);                   // number of this disk
			writeInt(0);                   // central directory start disk
			writeLong(xentries.size());    // number of directory entires on disk
			writeLong(xentries.size());    // number of directory entires
			writeLong(len);                // length of central directory
			writeLong(off);                // offset of central directory

			//zip64 end of central directory locator
			writeInt(ZIP64_LOCSIG);        // zip64 END locator signature
			writeInt(0);                   // zip64 END start disk
			writeLong(off64);              // offset of zip64 END
			writeInt(1);                   // total number of disks (?)
		}
		writeInt(ENDSIG);                 // END record signature
		writeShort(0);                    // number of this disk
		writeShort(0);                    // central directory start disk
		writeShort(count);                // number of directory entries on disk
		writeShort(count);                // total number of directory entries
		writeInt(xlen);                   // length of central directory
		writeInt(xoff);                   // offset of central directory
		if (comment != null) {            // zip file comment
			writeShort(comment.length);
			writeBytes(comment, 0, comment.length);
		} else {
			writeShort(0);
		}
	}

	/*
	 * Writes a 16-bit short to the output stream in little-endian byte order.
	 */
	private void writeShort(int v) throws IOException {
		OutputStream out = this.out;
		out.write((v >>> 0) & 0xff);
		out.write((v >>> 8) & 0xff);
		written += 2;
	}

	/*
	 * Writes a 32-bit int to the output stream in little-endian byte order.
	 */
	private void writeInt(long v) throws IOException {
		OutputStream out = this.out;
		out.write((int)((v >>>  0) & 0xff));
		out.write((int)((v >>>  8) & 0xff));
		out.write((int)((v >>> 16) & 0xff));
		out.write((int)((v >>> 24) & 0xff));
		written += 4;
	}

	/*
	 * Writes a 64-bit int to the output stream in little-endian byte order.
	 */
	private void writeLong(long v) throws IOException {
		OutputStream out = this.out;
		out.write((int)((v >>>  0) & 0xff));
		out.write((int)((v >>>  8) & 0xff));
		out.write((int)((v >>> 16) & 0xff));
		out.write((int)((v >>> 24) & 0xff));
		out.write((int)((v >>> 32) & 0xff));
		out.write((int)((v >>> 40) & 0xff));
		out.write((int)((v >>> 48) & 0xff));
		out.write((int)((v >>> 56) & 0xff));
		written += 8;
	}

	/*
	 * Writes an array of bytes to the output stream.
	 */
	private void writeBytes(byte[] b, int off, int len) throws IOException {
		super.out.write(b, off, len);
		written += len;
	}

	/* Copied from java.util.zip.ZipEntry */

	/*
	 * Converts Java time to DOS time.
	 */
//	@SuppressWarnings("deprecation")
	private static long javaToDosTime(long time) {
		if (time == -1) {
			return -1;
		}
		Date d = new Date(time);
		int year = d.getYear() + 1900;
		if (year < 1980) {
			return (1 << 21) | (1 << 16);
		}
		return (year - 1980) << 25 | (d.getMonth() + 1) << 21 |
		           d.getDate() << 16 | d.getHours() << 11 | d.getMinutes() << 5 |
		           d.getSeconds() >> 1;
	}
}
