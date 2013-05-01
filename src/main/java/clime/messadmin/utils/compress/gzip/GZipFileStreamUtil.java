/**
 *
 */
package clime.messadmin.utils.compress.gzip;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

import clime.messadmin.utils.Charsets;

/**
 * @author C&eacute;drik LIME
 * @see "http://www.ietf.org/rfc/rfc1952.txt"
 * @see "http://www.gnu.org/software/gzip/"
 */
class GZipFileStreamUtil {

	private GZipFileStreamUtil() {
	}

	/**
	 * GZIP header magic number.
	 */
	public final static int GZIP_MAGIC = 0x8b1f;

	/**
	 * Trailer size in bytes.
	 */
	public final static int TRAILER_SIZE = 8;

	/*
	 * File header flags (FLG).
	 */
	private final static byte FTEXT    = 1;  // Extra text   (bit 0: (byte)1<<0)
	/**@deprecated*/
	private final static byte FHCRC    = 2;  // Header CRC16 (bit 1: (byte)1<<1) -- Do not set! gzip considers this flag as continuation of multi-part gzip file!
	private final static byte FEXTRA   = 4;  // Extra field  (bit 2: (byte)1<<2)
	private final static byte FNAME    = 8;  // File name    (bit 3: (byte)1<<3)
	private final static byte FCOMMENT = 16; // File comment (bit 4: (byte)1<<4)
	// bit 5: rfc: reserved; gzip: file is encrypted
	// bits 6 & 7 are reserved

	private final static byte[] default_header = {
		(byte) GZIP_MAGIC,                // Magic number (short)
		(byte)(GZIP_MAGIC >> 8),          // Magic number (short)
		Deflater.DEFLATED,                // Compression method (CM)
		0,                                // Flags (FLG)
		0,                                // Modification time MTIME (int)
		0,                                // Modification time MTIME (int)
		0,                                // Modification time MTIME (int)
		0,                                // Modification time MTIME (int)
		0,                                // Extra flags (XFLG)
		(byte)0xff                        // Operating system (OS): unknown
	};

	/**
	 * Writes GZIP member header.
	 */
	public static int writeHeader(OutputStream out, int compressionLevel, long modificationTime, CharSequence fileName, CharSequence comment) throws IOException {
		byte[] header = new byte[10];
		int headerSize = 0;
		byte[] fileNameBytes = getISOLatin1(fileName);
		byte[] commentBytes = getISOLatin1(comment);
		int modTimeSeconds = (int) (modificationTime / 1000);
		System.arraycopy(default_header, 0, header, 0, default_header.length);
		if (fileNameBytes != null && fileNameBytes.length > 0) {
			header[3] |= FNAME;
		}
		if (commentBytes != null && commentBytes.length > 0) {
			header[3] |= FCOMMENT;
		}
		writeInt(modTimeSeconds, header, 4);
		if (Deflater.DEFLATED == header[2]) {
			header[8] = compressionLevel == Deflater.BEST_COMPRESSION ? (byte)2 :
				(compressionLevel == Deflater.BEST_SPEED||compressionLevel == Deflater.NO_COMPRESSION ? (byte)4 : 0);
		}
		headerSize += header.length;
		out.write(header);
		if (fileNameBytes != null && fileNameBytes.length > 0) {
			out.write(fileNameBytes);
			out.write(0);// file name is zero-terminated
			headerSize += fileNameBytes.length + 1;
		}
		if (commentBytes != null && commentBytes.length > 0) {
			out.write(commentBytes);
			out.write(0);// comment is zero-terminated
			headerSize += commentBytes.length + 1;
		}
		return headerSize;
	}

	private static byte[] getISOLatin1(CharSequence str) {
		if (str == null || str.length() == 0) {
			return null;
		}
		byte[] result = null;
		try {
			Charset charset = Charsets.forName("ISO-8859-1");//$NON-NLS-1$
			CharsetEncoder encoder = charset.newEncoder();
			ByteBuffer bbuffer = encoder.encode(CharBuffer.wrap(str));
			result = bbuffer.array();
		} catch (CharacterCodingException ignore) {
		}
		return result;
	}

	/**
	 * Writes GZIP member trailer to a byte array, starting at a given
	 * offset.
	 */
	public static void writeTrailer(byte[] buf, int offset, CRC32 crc, Deflater def) throws IOException {
		writeTrailer(buf, offset, (int)crc.getValue(), def.getTotalIn());
	}
	public static void writeTrailer(byte[] buf, int offset, int crc32, int uncompressedBytes) throws IOException {
		writeInt(crc32, buf, offset); // CRC-32 of uncompr. data
		writeInt(uncompressedBytes, buf, offset + 4); // Number of uncompr. bytes
	}

	/**
	 * Writes integer in Intel byte order to a byte array, starting at a
	 * given offset.
	 */
	private static void writeInt(int i, byte[] buf, int offset) throws IOException {
		writeShort(i & 0xffff, buf, offset);
		writeShort((i >> 16) & 0xffff, buf, offset + 2);
	}

	/**
	 * Writes short integer in Intel byte order to a byte array, starting
	 * at a given offset
	 */
	private static void writeShort(int s, byte[] buf, int offset) throws IOException {
		buf[offset] = (byte)(s & 0xff);
		buf[offset + 1] = (byte)((s >> 8) & 0xff);
	}
}
